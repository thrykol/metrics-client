package us.my_family.metrics.client

import org.scalatest.Matchers
import org.scalatest.WordSpec

import us.my_family.metrics.configuration.Configuration
import us.my_family.metrics.test.MetricsClientFixture

class MetricsClientSpec extends WordSpec with Matchers {

	"provider" should {

		"provide the NoOpClient singleton" in {
			new MetricsClientProvider {}.metrics shouldBe theSameInstanceAs(NoOpClient)
		}

		"provide the NonBlockingClient singleton" in {

			new MetricsClientProvider {
				override lazy val configuration = new Configuration {
					override lazy val enabled = true
				}
			}.metrics shouldBe theSameInstanceAs(NonBlockingClient)
		}
	}

	"close" should {

		"be defined" in new MetricsClientFixture {
			metrics.close()
		}
	}

	"increment" should {

		"increment the counter by one" in new MetricsClientFixture {

			counter expects ("test", 1, Map[String, Option[String]](), 1.0)
			metrics.increment("test")
		}

		"pass through the specified tags" in new MetricsClientFixture {

			val tagMap = Map("host" -> Option("localhost"))

			counter expects (*, *, tagMap, *)
			metrics.increment("", tagMap)
		}

		"pass through the specified sample rate" in new MetricsClientFixture {

			counter expects (*, *, *, 0.5)
			metrics.increment("", sampleRate = 0.5)
		}
	}

	"decrement" should {

		"decrement the counter by one" in new MetricsClientFixture {

			counter expects ("test", -1, Map[String, Option[String]](), 1.0)
			metrics.decrement("test")
		}

		"pass through the specified tags" in new MetricsClientFixture {

			val tagMap = Map("host" -> Option("localhost"))

			counter expects (*, *, tagMap, *)
			metrics.decrement("", tagMap)
		}

		"pass through the specified sample rate" in new MetricsClientFixture {

			counter expects (*, *, *, 0.5)
			metrics.decrement("", sampleRate = 0.5)
		}
	}

	"time" should {

		"execute the unit of work" in new MetricsClientFixture {

			val uow = mockFunction[Unit, Unit]

			uow expects ()
			timer expects ("test", *, Map[String, Option[String]](), 1.0)

			metrics.time("test")(uow)
		}

		"pass through the specified tags" in new MetricsClientFixture {

			val tagMap = Map("host" -> Option("localhost"))

			timer expects (*, *, tagMap, *)
			metrics.time("", tagMap) {}
		}

		"pass through the specified sample rate" in new MetricsClientFixture {

			timer expects (*, *, *, 0.5)
			metrics.time("", sampleRate = 0.5) {}
		}
	}
}
