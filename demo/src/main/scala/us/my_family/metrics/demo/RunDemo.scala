package us.my_family.metrics.demo

import com.typesafe.scalalogging.LazyLogging

import us.my_family.metrics.client.MetricsClientProvider
import us.my_family.metrics.client.NoOpClient
import us.my_family.metrics.client.NonBlockingClient

object RunDemo extends App with MetricsClientProvider with LazyLogging {

	val duration = metrics.time("timer.demo") {
		val date = System.currentTimeMillis()

		metrics match {
			case NoOpClient => logger.info("Metrics client is disabled")
			case NonBlockingClient => logger.info("Metrics client is enabled")
		}

		if (date % 2 == 1) {
			logger.info("Incrementing 'counter.demo' by 1")
			metrics.increment("counter.demo")

			logger.info("Setting 'gauge.demo' to 5")
			metrics.gauge("gauge.demo", 5)
		}

		if (date % 2 == 0) {
			logger.info("Decrementing 'counter.demo' by 1")
			metrics.decrement("counter.demo")

			logger.info("Increasing 'gauge.demo' by 3")
			metrics.gauge("gauge.demo", 3, isDelta = true)
		}

		logger.info("Updating 'counter.demo' by 3")
		metrics.counter("counter.demo", 3)

		Thread.sleep(date % 3 * 100)

		System.currentTimeMillis() - date
	}

	logger.info(s"Set 'timing.demo' to approximately ${duration}")

	logger.info("Waiting for metrics to clear")
	Thread.sleep(10000)

	logger.info("Resetting 'gauge.demo'")
	metrics.gauge("gauge.demo", 0)

	logger.info("Closing metrics client")
	metrics.close()
}
