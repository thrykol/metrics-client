package us.my_family.metrics.writers

import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

import us.my_family.metrics.Counter
import us.my_family.metrics.Gauge
import us.my_family.metrics.Metric
import us.my_family.metrics.MetricType
import us.my_family.metrics.TagMap
import us.my_family.metrics.Timer

class MetricWriterSpec extends WordSpec with Matchers with MockFactory {

	trait BaseFixture {

		lazy val metric = "unit.test"
		lazy val metricType : MetricType = Counter
		lazy val value = "true"
		lazy val tags : TagMap = Map()
		lazy val sampleRate : Double = 0

		lazy val statsdMetric = new Metric(metric, metricType, value, tags, sampleRate) with StatsDWriter
		lazy val dogStatsdMetric = new Metric(metric, metricType, value, tags, sampleRate) with DogStatsDWriter
		lazy val telegrafMetric = new Metric(metric, metricType, value, tags, sampleRate) with TelegrafWriter
	}

	"tagConcat" should {

		"be a pipe" in new BaseFixture {

			statsdMetric.tagConcat shouldBe "|"
		}
	}

	"tagSeparator" should {

		"be an equals sign" in new BaseFixture {

			statsdMetric.tagSeparator shouldBe "="
		}
	}

	"metricTypeToString" should {

		"return the correct string" in new BaseFixture with TableDrivenPropertyChecks {

			forAll(Table(("metricType", "expected"),
				(Counter, "c"),
				(Gauge, "g"),
				(Timer, "ms"))) { (metricType, expected) =>
				statsdMetric.metricTypeToString(metricType) shouldBe expected
			}
		}
	}

	"appendTags" should {

		"handle empty tag map" in new BaseFixture {

			statsdMetric.appendTags(metric) shouldBe metric
		}

		"concatenate using the specified symbol" in new BaseFixture {

			val instance = new Metric(metric, metricType, value, Map("a" -> Option("b"), "c" -> None), sampleRate) with StatsDWriter

			instance.appendTags(metric) shouldBe "unit.test|a=b,c"
		}
	}

	"appendValue" should {

		"append the value to the message" in new BaseFixture {

			statsdMetric.appendValue(metric) shouldBe "unit.test:true"
		}
	}

	"appendType" should {

		"append the metric type to the message" in new BaseFixture {

			statsdMetric.appendType(metric) shouldBe "unit.test|c"
		}
	}

	"appendRate" should {

		"append the sample rate to the message" in new BaseFixture {

			statsdMetric.appendRate(metric) shouldBe "unit.test|@0.0"
		}
	}

	"DogStatsDWriter" should {

		"append expected parts" in new BaseFixture {

			override lazy val tags = Map("a" -> Option("b"), "c" -> None)

			dogStatsdMetric.write() shouldBe "unit.test:true|c|@0.0|#a:b,c"
		}
	}

	"StatsDWriter" should {

		"append expected parts" in new BaseFixture {

			override lazy val tags = Map("a" -> Option("b"), "c" -> None)

			statsdMetric.write() shouldBe "unit.test:true|c|@0.0"
		}
	}

	"TelegrafWriter" should {

		"append expected parts" in new BaseFixture {

			override lazy val tags = Map("a" -> Option("b"), "c" -> None)

			telegrafMetric.write() shouldBe "unit.test,a=b,c:true|c|@0.0"
		}
	}
}
