package us.my_family.metrics.client

import us.my_family.metrics.TagMap
import us.my_family.metrics.configuration.ConfigurationProvider

trait MetricsClientProvider extends ConfigurationProvider {

	lazy val metrics = if (configuration.enabled) NonBlockingClient else NoOpClient
}

trait MetricsClient[T] {

	/** Increment a counter by one.
	 *
	 *  @param metric Name of the metric
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 */
	def increment(metric : String, tags : TagMap = Map(), sampleRate : Double = 1.0) : T = counter(metric, 1, tags, sampleRate)

	/** Decrement a counter by one.
	 *
	 *  @param metric Name of the metric
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 */
	def decrement(metric : String, tags : TagMap = Map(), sampleRate : Double = 1.0) : T = counter(metric, -1, tags, sampleRate)

	/** Time a unit of work.
	 *
	 *  @param metric Name of the metric
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 *  @param uow Unit of work to be timed
	 *  @return The results of the unit of work
	 */
	def time[R](metric : String, tags : TagMap = Map(), sampleRate : Double = 1.0)(uow : => R) : R = {
		val start = System.currentTimeMillis()
		val result = uow
		timer(metric, System.currentTimeMillis() - start, tags, sampleRate)
		result
	}

	/** Close the client
	 */
	def close() {}

	/** Set a gauged value.
	 *
	 *  @param metric Name of the metric
	 *  @param value New value of the metric
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 *  @param isDelta Whether the gauge is a delta of the previous recording
	 */
	def gauge(metric : String, value : Double, tags : TagMap = Map(), sampleRate : Double = 1.0, isDelta : Boolean = false) : T

	/** Set a counter value.
	 *
	 *  @param metric Name of the metric
	 *  @param value New value of the metric
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 */
	def counter(metric : String, value : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : T

	/** Set a timer value.
	 *
	 *  @param metric Name of the metric
	 *  @param duration Duration of the timing
	 *  @param tags Tags to apply to the metric
	 *  @param sampleRate Rate at which the metric should be sampled
	 */
	def timer(metric : String, duration : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : T
}
