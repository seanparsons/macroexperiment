package com.github.seanparsons.macroexperiment

object ExperimentApp extends App {
  val ru = scala.reflect.runtime.universe
  println(ru.reflectClass(classOf[ValueDifference]))
}
