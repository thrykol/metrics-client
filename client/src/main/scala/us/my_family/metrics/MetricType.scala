package us.my_family.metrics

sealed trait MetricType

case object Counter extends MetricType

case object Gauge extends MetricType

case object Timer extends MetricType
