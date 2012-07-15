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
      implicitly[context.TypeTag[Double]].tpe,
      implicitly[context.TypeTag[Long]].tpe,
      implicitly[context.TypeTag[Int]].tpe,
      implicitly[context.TypeTag[Char]].tpe,
      implicitly[context.TypeTag[Short]].tpe,
      implicitly[context.TypeTag[Byte]].tpe,
      implicitly[context.TypeTag[Unit]].tpe,
      implicitly[context.TypeTag[Boolean]].tpe,
      implicitly[context.TypeTag[String]].tpe
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
      path.reverse.foldLeft(Ident(value.tree.symbol): context.Tree)((working, contextName) => Select(working, newTermName(contextName.toString)))
    }

    def createComparison(path: Seq[context.Name]): context.Expr[Option[ValueDifference]] = {
      val leftExpr = context.Expr[LeftType](createLookup(left, path))
      val rightExpr = context.Expr[RightType](createLookup(right, path))
      context.reify{
        if (leftExpr.splice == rightExpr.splice) None
        else Some(ValueDifference(path.map(_.toString), leftExpr.splice, rightExpr.splice))
      }
    }

    if (left.actualTpe == right.actualTpe) {
      val paths = determinePaths(left.actualTpe)
      paths.foldLeft(context.reify(Seq[ValueDifference]())){(expr, path) =>
        context.reify(expr.splice ++ createComparison(path).splice.toSeq)
      }
    } else  {
      sys.error("Left type %s does not match right type %s.".format(left.actualTpe, right.actualTpe))
    }
  }
}