package us.my_family.metrics.networking

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.scalalogging.LazyLogging
import us.my_family.metrics.configuration.ConfigurationProvider

trait UdpConnectionProvider {
	lazy val udpConnection = UdpConnection()
}

case class UdpConnection() extends ConfigurationProvider with LazyLogging {

	lazy val executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		val delegate = Executors.defaultThreadFactory()

		def newThread(r : Runnable) = {
			val result = delegate.newThread(r)
			result.setName("MetricsClients-" + result.getName())
			result.setDaemon(true)
			result
		}
	})

	lazy val channel = {
		val channel = DatagramChannel.open()
		channel.connect(new InetSocketAddress(configuration.host, configuration.port))
		channel
	}

	def close() = {

		Try {
			executor.shutdown()
			executor.awaitTermination(30000, TimeUnit.MILLISECONDS)
		} recover {
			case NonFatal(cause) => logger.warn("Failed to shut down thread executor", cause)
		}

		Try {
			channel.close
		} recover {
			case NonFatal(cause) => logger.warn("Failed to close datagram channel", cause)
		}
	}

	def send(message : String) = {
		Try {
			executor.execute(new Runnable() {
				def run() = {
					logger.trace("Sending message: {}", message)
					channel.write(ByteBuffer.wrap(message.getBytes(Charset.forName("UTF8"))))
				}
			})
		} recover {
			case NonFatal(cause) => {
				logger.warn("Failed to send message", cause)
				logger.debug(s"Failed message: ${message}")
			}
		}
	}
}
