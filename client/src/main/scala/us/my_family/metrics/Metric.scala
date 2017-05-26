package us.my_family.metrics

case class Metric(metric : String, metricType : MetricType, value : String, tags : TagMap, sampleRate : Double)
