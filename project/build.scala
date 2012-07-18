import sbt._
import Keys._

object build extends Build {
	val sharedSettings = Defaults.defaultSettings ++ Seq(
		organization := "com.github.seanparsons.macroexperiment",
                scalaVersion := "2.10.0-M5",
		version := "0.1-SNAPSHOT",
		scalacOptions ++= Seq("-Xlog-free-terms", "-unchecked", "-Xexperimental", "-language:_" /*, "-Ymacro-debug"*/),
		resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
		libraryDependencies <+= (scalaVersion)(sv => "org.scala-lang" % "scala-compiler" % sv),
    initialCommands := """
    import com.github.seanparsons.macroexperiment.Experiment._
    case class Test(first: Int, second: String)
    case class Test2(test: Test)
    val instance = new Test2(new Test(1, "Cake"))
    val instance2 = Test2(Test(1,"Ca"))
    """
	)

	lazy val root = Project(
		id = "macroexperiment",
		base = file("."),
		settings = sharedSettings
	)
}
