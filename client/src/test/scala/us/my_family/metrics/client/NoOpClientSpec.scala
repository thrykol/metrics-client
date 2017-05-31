package us.my_family.metrics.client

import org.scalatest.WordSpec

class NoOpClientSpec extends WordSpec {

	"counter" should {

		"be defined" in {
			NoOpClient.counter("", 1)
		}
	}

	"gauge" should {

		"be defined" in {
			NoOpClient.gauge("", 1)
		}
	}

	"timer" should {

		"be defined" in {
			NoOpClient.timer("", 1)
		}
	}
}
