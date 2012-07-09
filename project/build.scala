import sbt._
import Keys._

object build extends Build {
	val sharedSettings = Defaults.defaultSettings ++ Seq(
		organization := "com.github.seanparsons.macroexperiment",
                scalaVersion := "2.10.0-M4",
		version := "0.1-SNAPSHOT",
		scalacOptions ++= Seq("-Xlog-free-terms", "-unchecked", "-Xexperimental", "-language:_" /*, "-Ymacro-debug"*/),
		resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
		libraryDependencies <+= (scalaVersion)(sv => "org.scala-lang" % "scala-compiler" % sv),
    initialCommands := """
    case class ExperimentValue(x: Int, y: String)
    import com.github.seanparsons.macroexperiment.Experiment._
    //isDifferent(new ExperimentValue(1, "Test1"), new ExperimentValue(2, "Test2"))
    """
	)

	lazy val root = Project(
		id = "macroexperiment",
		base = file("."),
		settings = sharedSettings
	)
}
