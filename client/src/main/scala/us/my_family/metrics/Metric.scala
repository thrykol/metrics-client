package us.my_family.metrics

case class Metric(metric : String, value : String, tags : TagMap, sampleRate : Double)
