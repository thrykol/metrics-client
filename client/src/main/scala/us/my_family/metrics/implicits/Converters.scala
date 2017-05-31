package us.my_family.metrics.implicits

import com.typesafe.scalalogging.LazyLogging

import us.my_family.metrics.Metric
import us.my_family.metrics.TagMap
import us.my_family.metrics.configuration.Configuration
import us.my_family.metrics.configuration.ConfigurationProvider
import us.my_family.metrics.configuration.DogStatsDFormat
import us.my_family.metrics.configuration.MetricFormat
import us.my_family.metrics.configuration.StatsDFormat
import us.my_family.metrics.configuration.TelegrafFormat
import us.my_family.metrics.writers.DogStatsDWriter
import us.my_family.metrics.writers.StatsDWriter
import us.my_family.metrics.writers.TelegrafWriter

object Converters extends Converters

trait Converters extends ConfigurationProvider with LazyLogging {

	implicit def optionMap(data : Map[String, String]) : TagMap = data map { case (k, v) => (k -> Option(v)) }

	implicit def metricToString(metric : Metric) = {

		configuration.format match {
			case StatsDFormat => new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with StatsDWriter write
			case DogStatsDFormat => new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with DogStatsDWriter write
			case TelegrafFormat => new Metric(metric.metric, metric.metricType, metric.value, metric.tags, metric.sampleRate) with TelegrafWriter write
		}
	}

	implicit def stringToMetricFormat(format : String) : MetricFormat = format.toLowerCase match {
		case "dogstatsd" => DogStatsDFormat
		case "telegraf" => TelegrafFormat
		case "statsd" => StatsDFormat
		case other => {
			logger.warn(s"Unknown metric format '${other}': defaulting to StatsD")
			StatsDFormat
		}
	}
}
