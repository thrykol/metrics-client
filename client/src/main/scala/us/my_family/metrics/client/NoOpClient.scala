package us.my_family.metrics.client

import us.my_family.metrics.TagMap

case object NoOpClient extends MetricsClient[Unit] {

	def counter(metric : String, value : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : Unit = {}

	def gauge(metric : String, value : Double, tags : TagMap = Map(), sampleRate : Double = 1.0) : Unit = {}

	def timer(metric : String, duration : Long, tags : TagMap = Map(), sampleRate : Double = 1.0) : Unit = {}
}
