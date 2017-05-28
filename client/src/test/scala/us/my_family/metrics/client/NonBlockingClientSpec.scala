package us.my_family.metrics.client

import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec

import us.my_family.metrics.Counter
import us.my_family.metrics.Gauge
import us.my_family.metrics.Metric
import us.my_family.metrics.MetricType
import us.my_family.metrics.TagMap
import us.my_family.metrics.Timer
import us.my_family.metrics.implicits.Converters.metricToString
import us.my_family.metrics.networking.UdpConnection

class NonBlockingClientSpec extends WordSpec with Matchers with MockFactory {

	trait BaseFixture {

		lazy val metricKey = "unit.test"
		lazy val metricType : MetricType = Counter
		lazy val value = 1L
		lazy val tags : TagMap = Map("a" -> Option("b"))
		lazy val sampleRate = 1.0D

		lazy val metric = Metric(metricKey, metricType, value.toString, tags, sampleRate)

		lazy val udpConnection = mock[UdpConnection]

		lazy val client = new NonBlockingClient {
			override lazy val udpConnection = BaseFixture.this.udpConnection
		}
	}

	"counter" should {

		"send a Counter metric" in new BaseFixture {

			(udpConnection.send _) expects (metric : String)

			client.counter(metricKey, value, tags, sampleRate)
		}

		"default the tags" in new BaseFixture {

			override lazy val tags : TagMap = Map()

			(udpConnection.send _) expects (metric : String)

			client.counter(metricKey, value, sampleRate = sampleRate)
		}

		"default the sample rate" in new BaseFixture {

			override lazy val sampleRate = 1.0

			(udpConnection.send _) expects (metric : String)

			client.counter(metricKey, value, tags)
		}
	}

	"gauge" should {

		trait NonDeltaGauge { self : BaseFixture =>
			lazy val gauge = Seq((metric.copy(value = "0.0") : String),
				(metric.copy(value = value.toDouble.toString) : String)).mkString("\n")
		}

		"send a Gauge metric" in new BaseFixture with NonDeltaGauge {

			override lazy val metricType = Gauge

			(udpConnection.send _) expects gauge

			client.gauge(metricKey, value, tags, sampleRate)
		}

		"default the tags" in new BaseFixture with NonDeltaGauge {

			override lazy val metricType = Gauge

			(udpConnection.send _) expects gauge

			client.gauge(metricKey, value, sampleRate = sampleRate)
		}

		"default the sample rate" in new BaseFixture with NonDeltaGauge {

			override lazy val metricType = Gauge
			override lazy val sampleRate = 1.0

			(udpConnection.send _) expects gauge

			client.gauge(metricKey, value, tags)
		}

		"clear the previous gauge" in new BaseFixture {

			override lazy val metricType = Gauge

			(udpConnection.send _) expects (metric.copy(value = value.toDouble.toString) : String)

			client.gauge(metricKey, value.toDouble, tags, sampleRate, true)
		}

		"not clear the previous gauge" in new BaseFixture with NonDeltaGauge {

			override lazy val metricType = Gauge

			(udpConnection.send _) expects gauge

			client.gauge(metricKey, value.toDouble, tags, sampleRate, false)
		}
	}

	"timer" should {

		"send a Timer metric" in new BaseFixture {

			override lazy val metricType = Timer

			(udpConnection.send _) expects (metric : String)

			client.timer(metricKey, value, tags, sampleRate)
		}

		"deafult the tags" in new BaseFixture {

			override lazy val metricType = Timer
			override lazy val tags : TagMap = Map()

			(udpConnection.send _) expects (metric : String)

			client.timer(metricKey, value, sampleRate = sampleRate)
		}

		"default the sample rate" in new BaseFixture {

			override lazy val metricType = Timer
			override lazy val sampleRate = 1.0

			(udpConnection.send _) expects (metric : String)

			client.timer(metricKey, value, tags)
		}
	}
}
