package test.assignment

import scala.concurrent.{blocking, Future}
import scala.concurrent.ExecutionContext.Implicits._

object Test {

  def main(args: Array[String]): Unit = {
    val service = new ThrottlingService {
      override val slaService = new SlaService {
        val persisted = {
          val harry = Sla("Harry Potter", 100)
          val albus = Sla("Albus Dumbledore", 1000)

          Map(
            "token123" -> harry,
            "token456" -> albus,
            "token789" -> harry
          )
        }

        override def getSlaByToken(token: String) = Future {
          blocking {
            Thread.sleep(250)
            persisted(token)
          }
        }
      }
      override val graceRps = 10
    }

    doRequests(5, None)(service)
    assert(service.isRequestAllowed(None), "Anonymous should still have some requests left")

    doRequests(10, None)(service)
    assert(!service.isRequestAllowed(None), "Anonymous should not be allowed to do any requests")
  }

  def doRequests(iterations: Int, token: Option[String])(service: ThrottlingService) =
    for(_ <- 0 to iterations) {
      service.isRequestAllowed(token)
    }

}
