package us.my_family.metrics.implicits

import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

import us.my_family.metrics.Counter
import us.my_family.metrics.Metric
import us.my_family.metrics.configuration.DogStatsDFormat
import us.my_family.metrics.configuration.StatsDFormat
import us.my_family.metrics.configuration.TelegrafFormat
import us.my_family.metrics.test.ConfigurationFixture
import us.my_family.metrics.test.LoggingFixture
import us.my_family.metrics.writers.DogStatsDWriter
import us.my_family.metrics.writers.StatsDWriter
import us.my_family.metrics.writers.TelegrafWriter
import us.my_family.metrics.configuration.MetricFormat
import us.my_family.metrics.configuration.Configuration

class ConvertersSpec extends WordSpec with Matchers {

	trait BaseFixture extends LoggingFixture {
		lazy val metric = Metric("metric.name", Counter, "metric.value", Map(), 1.0)
		lazy val format : MetricFormat = StatsDFormat
		lazy val configuration = new Configuration {
			override lazy val format = BaseFixture.this.format
		}

		lazy val converters = new Converters {
			override protected lazy val logger = BaseFixture.this.logger
			override lazy val configuration = BaseFixture.this.configuration
		}
	}

	"optionMap" should {

		"convert a map of string values to option values" in {

			val data = Map("defined" -> "defined", "null" -> None.orNull)
			val expected = Map("defined" -> Option("defined"), "null" -> None)

			Converters.optionMap(data) shouldBe expected
		}
	}

	"metricToString" should {

		"convert to StatsDFormat" in new BaseFixture {

			val statsd = new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with StatsDWriter

			converters.metricToString(statsd) shouldBe statsd.write()
		}

		"convert to DogStatsDFormat" in new BaseFixture {

			override lazy val format = DogStatsDFormat

			val dogstatsd = new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with DogStatsDWriter

			converters.metricToString(dogstatsd) shouldBe dogstatsd.write()
		}

		"convert to TelegrafFormat" in new BaseFixture {

			override lazy val format = TelegrafFormat

			val telegraf = new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with TelegrafWriter

			converters.metricToString(telegraf) shouldBe telegraf.write()
		}
	}

	"stringToMetricFormat" should {

		"convert the expected strings" in new BaseFixture with TableDrivenPropertyChecks {

			forAll(Table(("text", "format"),
				("statsd", StatsDFormat),
				("dogstatsd", DogStatsDFormat),
				("telegraf", TelegrafFormat))) { (text, format) =>
				converters.stringToMetricFormat(text) shouldBe format
				converters.stringToMetricFormat(text.toUpperCase()) shouldBe format
				converters.stringToMetricFormat(text.capitalize) shouldBe format
			}
		}

		"treat unknown strings as StatsD" in new BaseFixture {

			override lazy val isWarnEnabled = true

			lazy val other = "bad-wolf"

			(underlying.warn(_ : String)) expects (s"Unknown metric format '${other}': defaulting to StatsD")

			converters.stringToMetricFormat(other) shouldBe StatsDFormat
		}
	}
}
