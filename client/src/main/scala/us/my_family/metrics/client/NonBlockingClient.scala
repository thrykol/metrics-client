package us.my_family.metrics.client

import us.my_family.metrics.Counter
import us.my_family.metrics.Gauge
import us.my_family.metrics.Metric
import us.my_family.metrics.TagMap
import us.my_family.metrics.Timer
import us.my_family.metrics.implicits.Converters.metricToString
import us.my_family.metrics.networking.UdpConnectionProvider

case object NonBlockingClient extends NonBlockingClient

trait NonBlockingClient extends MetricsClient[Unit] with UdpConnectionProvider {

	def counter(metric : String, value : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : Unit = udpConnection.send(Metric(metric, Counter, value.toString, tags, sampleRate))

	def gauge(metric : String, value : Double, tags : TagMap = Map(), sampleRate : Double = 1.0, isDelta : Boolean = false) : Unit = {
		lazy val clear : String = Metric(metric, Gauge, "0.0", tags, sampleRate)
		val gauge : String = Metric(metric, Gauge, value.toString, tags, sampleRate)

		if (isDelta)
			udpConnection.send(gauge)
		else
			udpConnection.send(s"${clear}\n${gauge}")
	}

	def timer(metric : String, duration : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : Unit = udpConnection.send(Metric(metric, Timer, duration.toString, tags, sampleRate))

	override def close() = udpConnection.close()
}
