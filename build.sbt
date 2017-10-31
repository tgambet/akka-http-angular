import scala.sys.process.Process

lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion    = "2.5.6"

val ng = inputKey[Int]("The angular-cli command.")

lazy val root = (project in file(".")).
  settings(
    name            := "Akka Angular Seed",
    version         := "0.1",
    organization    := "net.creasource",
    scalaVersion    := "2.12.4",
    scalacOptions   := Seq("-unchecked", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.4"         % Test
    ),
    ng := {
      import complete.DefaultParsers._
      val args = spaceDelimited("<arg>").parsed.mkString(" ")
      val command = {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
          s"powershell -Command ng $args"
        } else {
          s"ng $args"
        }
      }
      Process(command, new File("./web").getAbsoluteFile).!
    }
  )

enablePlugins(JavaAppPackaging)
