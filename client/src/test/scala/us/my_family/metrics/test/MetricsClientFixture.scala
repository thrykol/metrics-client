package us.my_family.metrics.test

import org.scalamock.scalatest.MockFactory

import us.my_family.metrics.TagMap
import us.my_family.metrics.client.MetricsClient

trait MetricsClientFixture extends MockFactory {

	lazy val counter = mockFunction[String, Long, TagMap, Double, Unit]
	lazy val gauge = mockFunction[String, Double, TagMap, Double, Boolean, Unit]
	lazy val timer = mockFunction[String, Long, TagMap, Double, Unit]

	lazy val metrics = new MetricsClient[Unit] {
		def counter(metric : String, value : Long, tags : TagMap, sampleRate : Double) : Unit = MetricsClientFixture.this.counter(metric, value, tags, sampleRate)
		def gauge(metric : String, value : Double, tags : TagMap, sampleRate : Double, isDelta : Boolean) : Unit = MetricsClientFixture.this.gauge(metric, value, tags, sampleRate, isDelta)
		def timer(metric : String, duration : Long, tags : TagMap, sampleRate : Double) : Unit = MetricsClientFixture.this.timer(metric, duration, tags, sampleRate)
	}
}
