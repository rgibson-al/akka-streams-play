package streams

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl._

import FlowGraph.Implicits._

import scala.concurrent.{Await, Future}
import scala.util.{Success, Failure}


/**
 * Created by rgibson on
 *
 * 6/14/15.
 */
object BasicPipeline extends App {

  //Setup Actor System
  implicit val system = ActorSystem("Sys")
  //Setup materialzer
  implicit val materializer = ActorFlowMaterializer()


  val processingMap:Int => String = i => s"processed int: $i"
  def loggingMap[T](t:T) = {println(s"processed int: $t"); t}

  val modFilter:Int => Boolean = _ % 5 == 0
  val source = Source(1 until (1000,3))

  //basic filter and tranform pipeline
  val basicPipeline = source
                       .filter(modFilter)
                       .map(processingMap)

//  basicPipeline.runWith(sinkConsole)

  //pipeline to aggregate every 15 elements and calc mean for aggregation
   val sampleAvgPipeline =
     source
      .mapConcat(n => n to n + 2 )
      .grouped(15)
      .map(group => group.sum / group.size)

  sampleAvgPipeline.runWith(sinkConsole)


  // example of using combinators to build flow
  val modFilterFlow: Flow[Int, Int, Unit] = Flow[Int].filter(modFilter)
  val processingMapFlow: Flow[Int, String, Unit] = Flow[Int].map(processingMap)

  def sinkConsole[Out]: Sink[Out, Future[Unit]] = Sink.foreach[Out](println)

  val runnableFlow: RunnableFlow[Unit] = source via modFilterFlow via processingMapFlow to sinkConsole

  //Composition of types
  val sourceWithFilter: Source[Int, Unit] = source via modFilterFlow
  val filterAndMap: Flow[Int, String, Unit] = modFilterFlow via processingMapFlow
  val sinkWithMap: Sink[Int, Unit] = processingMapFlow to sinkConsole

  //runnableFlow.run() //We materialize the flow



}
