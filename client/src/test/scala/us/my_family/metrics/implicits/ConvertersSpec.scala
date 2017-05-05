package us.my_family.metrics.implicits

import org.scalatest.Matchers
import org.scalatest.WordSpec

class ConvertersSpec extends WordSpec with Matchers {

	"optionMap" should {

		"convert a map of string values to option values" in {

			val data = Map("defined" -> "defined", "null" -> None.orNull)
			val expected = Map("defined" -> Option("defined"), "null" -> None)

			Converters.optionMap(data) shouldBe expected
		}
	}
}
