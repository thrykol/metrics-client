package us.my_family.metrics.test

import org.scalamock.scalatest.MockFactory
import org.slf4j.{ Logger => UnderlyingLogger }

import com.typesafe.scalalogging.Logger

trait LoggingFixture extends MockFactory {

	lazy val underlying = mock[UnderlyingLogger]

	lazy val logger = Logger(underlying)

	lazy val isInfoEnabled : Boolean = false
	lazy val isWarnEnabled : Boolean = false
	lazy val isErrorEnabled : Boolean = false

	(underlying.isDebugEnabled _) expects () anyNumberOfTimes () returning true
	(underlying.isTraceEnabled _) expects () anyNumberOfTimes () returning true
	(underlying.isInfoEnabled _) expects () anyNumberOfTimes () returning isInfoEnabled
	(underlying.isWarnEnabled _) expects () anyNumberOfTimes () returning isWarnEnabled
	(underlying.isErrorEnabled _) expects () anyNumberOfTimes () returning isErrorEnabled
}
