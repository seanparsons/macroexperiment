package com.github.seanparsons.macroexperiment

import scala.reflect.makro.Context
import language.experimental.macros

case class ValueDifference(path: Seq[String], left: Any, right: Any)

object Experiment {
  def isDifferent[LeftType, RightType](left: LeftType, right: RightType): Seq[ValueDifference] = macro isDifferentImpl[LeftType, RightType]
  
  def isDifferentImpl[LeftType: context.TypeTag, RightType: context.TypeTag](context: Context)(left: context.Expr[LeftType], right: context.Expr[RightType]): context.Expr[Seq[ValueDifference]] = {
    import context.mirror._
    import context.universe._

    val comparableTypes: Set[context.Type] = Set(
      context.typeOf[Double],
      context.typeOf[Long],
      context.typeOf[Int],
      context.typeOf[Char],
      context.typeOf[Short],
      context.typeOf[Byte],
      context.typeOf[Unit],
      context.typeOf[Boolean],
      context.typeOf[String]
    )

    def adtLikeDeclarations(symbol: context.Type): Seq[context.Symbol] = {
      symbol
        .declarations
        .filter(symbol => !symbol.isMethod)
        .toSeq
    }

    def isComparableType(valueType: context.Type): Boolean = comparableTypes.contains(valueType)

    def determinePaths(valueType: context.Type, pathSoFar: Seq[context.Name] = Seq.empty): Seq[Seq[context.Name]] = {
      adtLikeDeclarations(valueType)
        .flatMap{
          case abstractType if (!abstractType.typeSignature.isConcrete) => context.abort(context.enclosingPosition, "%s at %s is abstract.".format(abstractType, pathSoFar))
          case primitive if(isComparableType(primitive.typeSignature)) => Seq(pathSoFar :+ primitive.name)
          case possibleADT if(adtLikeDeclarations(possibleADT.typeSignature).size > 1) => determinePaths(possibleADT.typeSignature, pathSoFar :+ possibleADT.name)
          case other => context.abort(context.enclosingPosition, "%s is not suitable for match for %s.".format(other, pathSoFar))
        }
    }

    def createLookup(value: context.Expr[_], path: Seq[context.Name]): context.Tree = {
      path.foldLeft(Ident(value.tree.symbol): context.Tree){(working, contextName) =>
        println("[%s]".format(contextName.decoded.trim))
        Select(working, newTermName(contextName.decoded.trim))
      }
    }

    def createComparison(path: Seq[context.Name]): context.Expr[Option[ValueDifference]] = {
      val leftExpr = context.Expr[LeftType](createLookup(left, path))
      val rightExpr = context.Expr[RightType](createLookup(right, path))
      val pathExpr = context.Expr[List[String]](Apply(definitions.ListModule, path.map(name => Literal(Constant(name.decoded.trim))): _*))
      context.reify{
        if (leftExpr.splice == rightExpr.splice) None
        else Some(ValueDifference(pathExpr.splice, leftExpr.splice, rightExpr.splice))
      }
    }

    println(left.actualTpe)
    if (left.actualTpe == right.actualTpe) {
      val paths = determinePaths(left.actualTpe)
      val pathComparisons = paths.map(path => createComparison(path))
      pathComparisons.foldLeft(context.reify(Seq[ValueDifference]())){(expr, pathComparison) =>
        context.reify(expr.splice ++ pathComparison.splice.toSeq)
      }
    } else  {
      sys.error("Left type %s does not match right type %s.".format(left.actualTpe, right.actualTpe))
    }
  }
}