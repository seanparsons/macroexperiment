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
    case class DateOfBirth(date: Int, month: Int, year: Int)
    case class Player(id: Int, name: String, dob: DateOfBirth)
    case class Game(firstPlayer: Player, secondPlayer: Player)
    val instance1 = Game(Player(23, "Sean", DateOfBirth(1, 9, 1980)), Player(20, "Sean", DateOfBirth(1, 9, 1980)))
    val instance2 = Game(Player(22, "Dave", DateOfBirth(1, 9, 1980)), Player(20, "Sean", DateOfBirth(2, 9, 1981)))
    """
	)

	lazy val root = Project(
		id = "macroexperiment",
		base = file("."),
		settings = sharedSettings
	)
}
