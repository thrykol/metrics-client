package us.my_family.metrics.implicits

import us.my_family.metrics.TagMap

object Converters {

	implicit def optionMap(data : Map[String, String]) : TagMap = data map { case (k, v) => (k -> Option(v)) }
}
