resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

libraryDependencies += "com.github.mpeltonen" % "sbt-idea" % "1.1.0-SNAPSHOT" from "http://mpeltonen.github.com/maven/com/github/mpeltonen/sbt-idea_2.9.2_0.12.0-Beta2/1.1.0-SNAPSHOT/sbt-idea-1.1.0-SNAPSHOT.jar"

addSbtPlugin(
  "com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0-RC1",
  sbtVersion = "0.12.0-Beta2"
)
