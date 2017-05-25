package us.my_family.metrics.configuration

trait MetricFormat

case object StatsDFormat extends MetricFormat

case object DogStatsDFormat extends MetricFormat

case object TelegrafFormat extends MetricFormat
