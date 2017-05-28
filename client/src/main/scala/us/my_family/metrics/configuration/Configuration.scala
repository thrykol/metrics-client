package us.my_family.metrics.configuration

import scala.util.Try

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.impl.ConfigImpl
import com.typesafe.scalalogging.LazyLogging

import us.my_family.metrics.implicits.Converters.stringToMetricFormat

trait ConfigurationProvider {
	lazy val configuration : Configuration = Configuration
}

object Configuration extends Configuration

trait Configuration extends LazyLogging {

	if (logger.underlying.isInfoEnabled()) {
		val formatConfig = Try(config.getBoolean("log.format")).toOption.fold(true)(b => b)

		logger.info(config.root.render(ConfigRenderOptions.concise().setFormatted(formatConfig)).trim())
	}

	lazy val base = "us.my_family.metrics"

	lazy val config = Try(ConfigFactory.load().getConfig(base)) orElse (Try(ConfigImpl.emptyConfig(base))) get

	lazy val host = Try(config.getString("host")).toOption.filterNot(_.trim.isEmpty()).fold("localhost")(h => h)

	lazy val port = Try(config.getInt("port")).toOption.fold(8125)(p => p)

	lazy val enabled = Try(config.getBoolean("enabled")).toOption.fold(false)(b => b)

	lazy val format = Try(config.getString("format")).toOption.fold[MetricFormat](StatsDFormat)(f => f)
}
