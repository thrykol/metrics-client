package us.my_family.metrics.test

import org.scalamock.scalatest.MockFactory

import com.typesafe.config.Config
import com.typesafe.config.ConfigObject

import us.my_family.metrics.configuration.Configuration

trait ConfigurationFixture extends MockFactory {

	lazy val config = mock[Config]
	lazy val configObject = mock[ConfigObject]
	lazy val configuration = mock[Configuration]
}
