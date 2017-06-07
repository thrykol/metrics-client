package us.my_family.metrics.networking

import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketAddress
import java.net.SocketOption
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.MembershipKey
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec

import us.my_family.metrics.configuration.Configuration
import us.my_family.metrics.test.LoggingFixture

class UdpConnectionSpec extends WordSpec with Matchers with MockFactory {

	trait BaseFixture {

		lazy val host = "localhost"
		lazy val port = 8125

		lazy val threadFactory = mock[ThreadFactory]
		lazy val executorService = mock[ExecutorService]
		lazy val thread = mock[Thread]
		lazy val selectorProvider = mock[SelectorProvider]
		lazy val channel = {
			class Mockable extends DatagramChannel(selectorProvider) {
				protected def implCloseSelectableChannel() : Unit = ???
				protected def implConfigureBlocking(x$1 : Boolean) : Unit = ???
				def bind(x$1 : SocketAddress) : DatagramChannel = ???
				def connect(x$1 : SocketAddress) : DatagramChannel = ???
				def disconnect() : DatagramChannel = ???
				def getLocalAddress() : SocketAddress = ???
				def getRemoteAddress() : SocketAddress = ???
				def isConnected() : Boolean = ???
				def read(x$1 : Array[ByteBuffer], x$2 : Int, x$3 : Int) : Long = ???
				def read(x$1 : ByteBuffer) : Int = ???
				def receive(x$1 : ByteBuffer) : SocketAddress = ???
				def send(x$1 : ByteBuffer, x$2 : SocketAddress) : Int = ???
				def setOption[T](x$1 : SocketOption[T], x$2 : T) : DatagramChannel = ???
				def socket() : DatagramSocket = ???
				def write(x$1 : Array[ByteBuffer], x$2 : Int, x$3 : Int) : Long = ???
				final def write(x$1 : ByteBuffer) : Int = BaseFixture.this.write(x$1)
				def join(x$1 : InetAddress, x$2 : NetworkInterface, x$3 : InetAddress) : MembershipKey = ???
				def join(x$1 : InetAddress, x$2 : NetworkInterface) : MembershipKey = ???
				def getOption[T](x$1 : SocketOption[T]) : T = ???
				def supportedOptions() : java.util.Set[SocketOption[_]] = ???
			}
			mock[Mockable]
		}
		lazy val newSingleThreadExecutor = mockFunction[ThreadFactory, ExecutorService]
		lazy val defaultThreadFactory = mockFunction[ThreadFactory]
		lazy val threadFactoryFunc = mockFunction[ThreadFactory]
		lazy val openDatagramChannel = mockFunction[DatagramChannel]
		lazy val write = mockFunction[ByteBuffer, Int]

		lazy val udpConnection = new UdpConnection {

			override def newSingleThreadExecutor(f : ThreadFactory) = BaseFixture.this.newSingleThreadExecutor(f)
			override def threadFactory() = BaseFixture.this.threadFactoryFunc()
			override def defaultThreadFactory() = BaseFixture.this.defaultThreadFactory()
			override def openDatagramChannel() = BaseFixture.this.openDatagramChannel()

			override lazy val configuration = new Configuration {
				override lazy val host = BaseFixture.this.host
				override lazy val port = BaseFixture.this.port
			}
		}
	}

	"provider" should {

		"create a new UdpConnection" in {

			val con1 = new UdpConnectionProvider {}
			val con2 = new UdpConnectionProvider {}

			con1.udpConnection shouldBe a[UdpConnection]

			con1 should not be theSameInstanceAs(con2)
		}
	}

	"ExecutorFactory.newSingleThreadFactory" should {

		"create an executor service" in new BaseFixture {

			val service = new ExecutorFactory {}.newSingleThreadExecutor(threadFactory)
			service shouldBe an[ExecutorService]

			service.shutdownNow()
		}
	}

	"ExecutorFactory.defaultThreadFactory" should {

		"create a default thread factory" in new BaseFixture {

			new ExecutorFactory {}.defaultThreadFactory() shouldBe a[ThreadFactory]
		}
	}

	"ExecutorFactory.threadFactory" should {

		"create a named, daemon thread" in new BaseFixture {

			lazy val _defaultThreadFactory = defaultThreadFactory

			lazy val runnable = new Runnable() { def run() {} }

			lazy val executorFactory = new ExecutorFactory {
				override def defaultThreadFactory() = _defaultThreadFactory()
			}

			defaultThreadFactory expects () returning threadFactory
			(threadFactory.newThread _) expects (runnable) returning thread

			val result = executorFactory.threadFactory().newThread(runnable)

			result.getName should startWith("MetricsClients-")
			result.isDaemon() shouldBe true
		}
	}

	"DatagramFactory.openDatagramChannel" should {

		"open a new datagram channel" in new BaseFixture {

			override lazy val channel = new DatagramFactory {}.openDatagramChannel()

			channel shouldBe a[DatagramChannel]
			channel.close()
		}
	}

	"executor" should {

		"initialize the expected thread factory" in new BaseFixture {

			threadFactoryFunc expects () returning threadFactory
			newSingleThreadExecutor expects (threadFactory) returning executorService

			udpConnection.executor
		}
	}

	"channel" should {

		"connect to the configured host and port" in new BaseFixture {

			openDatagramChannel expects () returning channel
			(channel.connect _) expects (new InetSocketAddress(host, port))

			udpConnection.channel shouldBe a[DatagramChannel]
		}
	}

	"close" should {

		trait Fixture extends BaseFixture with LoggingFixture {

			override lazy val udpConnection = new UdpConnection {
				override protected lazy val logger = Fixture.this.logger
				override lazy val executor = Fixture.this.executorService
				override lazy val channel = Fixture.this.channel
			}
		}

		"shutdown the executor service" in new Fixture {

			(executorService.shutdown _) expects ()
			(executorService.awaitTermination _) expects (30000, TimeUnit.MILLISECONDS)

			udpConnection.close()
		}

		"log warning when the executor fails to shutdown" in new Fixture {

			val cause = new Exception("bad wolf")
			(executorService.shutdown _) expects () throwing cause
			(underlying.warn(_ : String, _ : Throwable)) expects ("Failed to shut down thread executor", cause)

			udpConnection.close()
		}
	}

	"send" should {

		trait Fixture extends BaseFixture with LoggingFixture {

			override lazy val udpConnection = new UdpConnection {
				override protected lazy val logger = Fixture.this.logger
				override lazy val executor = Fixture.this.executorService
				override lazy val channel = Fixture.this.channel
			}
		}

		"write the message to the channel" in new Fixture {

			write expects (ByteBuffer.wrap("mock message".getBytes)) returning 1
			(executorService.execute _) expects (where { r : Runnable =>
				r.run()
				true
			})
			(underlying.trace(_ : String)) expects (*)

			udpConnection.send("mock message")
		}

		"log a failed message warning" in new Fixture {

			val cause = new Exception("bad wolf")

			(executorService.execute _) expects (*) throwing cause
			(underlying.warn(_ : String, _ : Throwable)) expects ("Failed to send message", cause)
			(underlying.debug(_ : String)) expects ("Failed message: mock messages")

			udpConnection.send("mock message")
		}
	}
}
