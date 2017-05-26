package us.my_family.metrics.writers

import us.my_family.metrics.Counter
import us.my_family.metrics.Gauge
import us.my_family.metrics.Metric
import us.my_family.metrics.MetricType
import us.my_family.metrics.TagMap
import us.my_family.metrics.Timer

sealed trait MetricWriter { self : Metric =>

	type OS = PartialFunction[OptTag, String]
	type SS = PartialFunction[String, String]
	type OptTag = Option[String]

	/*
	 * Different writers treat tags differently.  To account for this, each
	 * writer which uses tags must explicitly define the concatenation string.
	 */
	lazy val tagConcat = "|"

	/*
	 * String with which to separate a tag from its value.  For example,
	 * if the value is set to `:` then ("A" -> Some("a")) will become "A:a"
	 */
	lazy val tagSeparator = "="

	implicit def metricTypeToString(mt : MetricType) = mt match {
		case Counter => "c"
		case Gauge => "g"
		case Timer => "ms"
	}

	implicit def tagMapToString(m : TagMap) = Option(m map { e => e._2.fold[String](e._1)(v => s"${e._1}${tagSeparator}${v}") } mkString (","))

	lazy val appendTags : SS = { case m => (tags : OptTag).filterNot(_.isEmpty).fold(m)(t => s"${m}${tagConcat}${t}") }

	lazy val appendValue : SS = { case m => s"${m}:${value}" }

	lazy val appendType : SS = { case m => s"${m}|${metricType : String}" }

	lazy val appendRate : SS = { case m => s"${m}|@${sampleRate}" }

	/*
	 * PartialFunction composition which will be used to write the metric
	 * for example: lazy val convert = appendValue andThen appendRate
	 */
	def convert : SS

	final def write() = convert(metric)
}

trait DogStatsDWriter extends MetricWriter { self : Metric =>

	override lazy val tagConcat = "|#"

	override lazy val tagSeparator = ":"

	lazy val convert = appendValue andThen appendType andThen appendRate andThen appendTags
}

trait StatsDWriter extends MetricWriter { self : Metric =>

	lazy val convert = appendValue andThen appendType andThen appendRate
}

trait TelegrafWriter extends MetricWriter { self : Metric =>

	override lazy val tagConcat = ","

	lazy val convert = appendTags andThen appendValue andThen appendType andThen appendRate
}
