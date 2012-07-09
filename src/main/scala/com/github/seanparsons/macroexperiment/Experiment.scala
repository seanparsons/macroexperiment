package com.github.seanparsons.macroexperiment

import scala.reflect.makro.Context
import language.experimental.macros

case class ValueDifference(path: Seq[String], left: Any, right: Any)

object Experiment {

  /*
  def isDifferent[LeftType, RightType](left: LeftType, right: RightType): Seq[ValueDifference] = macro isDifferentImpl[LeftType, RightType]
  
  def isDifferentImpl[LeftType: context.TypeTag, RightType: context.TypeTag](context: Context)(left: context.Expr[LeftType], right: context.Expr[RightType]): context.Expr[Seq[ValueDifference]] = {
    import context.mirror._

    val comparableTypes: Seq[context.Type] = Seq(
      implicitly[context.TypeTag[Int]].tpe,
      implicitly[context.TypeTag[String]].tpe
    )

    def determinePaths(valueType: context.Type, pathSoFar: Seq[context.Name] = Seq.empty): Seq[Seq[String]] = {
      valueType
        .declarations
        .filter(symbol => !symbol.isMethod)
        .toSeq
        .flatMap{symbol: Symbol =>
          symbol match {
            case symbol if symbol.isAbstractType => context.abort(context.enclosingPosition, "Unable to handle the abstract type %s".format(symbol))
            case symbol if comparableTypes.contains(symbol.typeSignature) => pathSoFar :+ symbol.name
            case _ => determinePaths(symbol.typeSignature, pathSoFar :+ symbol.name)
          }
    }

    if (left.actualTpe == right.actualTpe) {
      val paths = determinePaths(left.actualTpe)
      val elementsToCheck = paths.map(path => path.foldLeft())
    } else  {
      sys.error("Left type %s does not match right type %s.".format(left.actualTpe, right.actualTpe))
    }
  }
  */
}
