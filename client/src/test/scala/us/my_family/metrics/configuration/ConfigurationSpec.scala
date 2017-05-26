package us.my_family.metrics.configuration

import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec

import com.typesafe.config.ConfigRenderOptions

import us.my_family.metrics.test.ConfigurationFixture
import us.my_family.metrics.test.LoggingFixture

class ConfigurationSpec extends WordSpec with Matchers with MockFactory {

	trait BaseFixture extends ConfigurationFixture with LoggingFixture {

		lazy val configString = "configs"

		lazy val instance = new Configuration {
			override lazy val logger = BaseFixture.this.logger
			override lazy val config = BaseFixture.this.config
		}

		(underlying.info(_ : String)) expects ("configs")
	}

	"instantiation" should {

		"log config formatted" in new BaseFixture {

			override lazy val isInfoEnabled = true

			(config.getBoolean _) expects ("log.format") returning true
			(config.root _) expects () returning configObject
			(configObject.render(_ : ConfigRenderOptions)) expects (where { options : ConfigRenderOptions =>
				!options.getComments && options.getFormatted && options.getJson && !options.getOriginComments
			}) returning configString
			(underlying.info(_ : String)) expects configString

			instance.base
		}

		"log config unformatted" in new BaseFixture {

			override lazy val isInfoEnabled = true

			(config.getBoolean _) expects (*) returning false
			(config.root _) expects () returning configObject
			(configObject.render(_ : ConfigRenderOptions)) expects (where { options : ConfigRenderOptions =>
				!options.getComments && !options.getFormatted && options.getJson && !options.getOriginComments
			}) returning configString
			(underlying.info(_ : String)) expects configString

			instance.base
		}
	}

	"base" should {

		"be the correct configuration path" in new BaseFixture {

			instance.base shouldBe "us.my_family.metrics"
		}
	}

	"host" should {

		"default to localhost for unset" in new BaseFixture {

			(config.getString _) expects ("host") throwing new Exception("")

			instance.host shouldBe "localhost"
		}

		"default to localhost for empty string" in new BaseFixture {

			(config.getString _) expects (*) returning ""

			instance.host shouldBe "localhost"
		}

		"trim whitespace" in new BaseFixture {

			(config.getString _) expects (*) returning "  "

			instance.host shouldBe "localhost"
		}

		"provide the configured host" in new BaseFixture {

			(config.getString _) expects (*) returning "test.host"

			instance.host shouldBe "test.host"
		}
	}

	"port" should {

		"default to 8125 for unset" in new BaseFixture {

			(config.getInt _) expects ("port") throwing new Exception("")

			instance.port shouldBe 8125
		}

		"provide the configured port" in new BaseFixture {

			(config.getInt _) expects (*) returning 9000

			instance.port shouldBe 9000
		}
	}

	"enabled" should {

		"default to false for unset" in new BaseFixture {

			(config.getBoolean _) expects ("enabled") throwing new Exception("")

			instance.enabled shouldBe false
		}

		"provide the enabled state" in new BaseFixture {

			(config.getBoolean _) expects (*) returning true

			instance.enabled shouldBe true
		}
	}

	"format" should {

		"default to StatsDFormat for unset" in new BaseFixture {

			(config.getString _) expects ("format") throwing new Exception("")

			instance.format shouldBe StatsDFormat
		}

		"default to StatsDFormat for empty string" in new BaseFixture {

			(config.getString _) expects (*) returning ""

			instance.format shouldBe StatsDFormat
		}

		"trim whitespace" in new BaseFixture {

			(config.getString _) expects (*) returning "  "

			instance.format shouldBe StatsDFormat
		}

		"provide the configured format" in new BaseFixture {

			(config.getString _) expects (*) returning "dogstatsd"

			instance.format shouldBe DogStatsDFormat
		}
	}

}
