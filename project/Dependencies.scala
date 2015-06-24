import sbt._

object Version {
  val akkaStreamV = "1.0-RC3"
}

object Library {
  val akkaStream = "com.typesafe.akka" %% "akka-stream-experimental" % Version.akkaStreamV
}

object Dependencies {

  import Library._

  val akkaStPlayDeps = Seq(
    akkaStream
  )
}
