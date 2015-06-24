package streams

import akka.actor.{Cancellable, ActorSystem}
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import FlowGraph.Implicits._
import akka.stream.scaladsl._
import scala.concurrent.duration._

/**
 * Created by rgibson
 *
 * 6/14/15.
 */
object FanOutIn extends App {
  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorFlowMaterializer()
  case class Tick()

  val source: Source[Int, Unit] = Source(1 to 50)
  val tickSource = Source(initialDelay = 1 second, interval = 1 second, Tick())

  val dbg:String => Int => (String,Int) = s => i => (s,i)

  val f1 = Flow[Int] map dbg("flow 1")
  val f2 = Flow[Int] map dbg("flow 2")
  val f3 = Flow[Int] map dbg("flow 3")


  def sinkConsole[I]: String =>
    Sink[I, Future[Unit]] =
    sinkName => Sink.foreach[I](x => println(s"$sinkName: $x"))

  //basic fanOut/fanIn
  val mergeGraph = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
    val broadcast = builder.add(Broadcast[Int](3))
    val merge = builder.add(Merge[(String, Int)](2))

    source ~> broadcast ~> f1 ~> sinkConsole("Sink 1")
              broadcast ~> f2 ~> merge ~> sinkConsole("Sink 2")
              broadcast ~> f3 ~> merge
  }
  mergeGraph.run()

  //zip example
  val zipGraph = FlowGraph.closed() { implicit builder =>
    val zip = builder.add(Zip[(String, Int), (String, Int)]())
    val broadcast = builder.add(Broadcast[Int](3))

    source ~> broadcast ~> f1 ~> sinkConsole("Sink 1")
              broadcast ~> f2 ~> zip.in0
              broadcast ~> f3 ~> zip.in1
                                 zip.out ~> sinkConsole("Zip Sink")
  }
  zipGraph.run()

  //fast source zipped with slow source to demo backpresssure
  val throttleGraph = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
    val zip = builder.add(Zip[Int,Tick])

    source     ~> zip.in0
    tickSource ~> zip.in1
                  zip.out ~> sinkConsole("Zip Sink")
  }
  throttleGraph.run

}
