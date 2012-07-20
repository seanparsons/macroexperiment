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

    def adtLikeDeclarations(symbol: context.Type): Seq[(String, context.Symbol)] = {
      def findAccessor(name: String): Option[context.Symbol] = {
        symbol
          .declarations
          .find(sym => sym.isTerm && sym.isMethod && sym.name.decoded + " " == name)
      }

      symbol
        .declarations
        .filter(sym => sym.isTerm && !sym.isMethod)
        .flatMap(sym => findAccessor(sym.name.decoded).map(accessor => (accessor.name.decoded, accessor)))
        .toSeq
    }

    def isComparableType(valueType: context.Type): Boolean = {
      comparableTypes.contains(valueType.resultType)
    }

    def determinePaths(valueType: context.Type, pathSoFar: Seq[(String, context.Name)] = Seq.empty): Seq[Seq[(String, context.Name)]] = {
      if (comparableTypes.contains(valueType)) {
        Seq(pathSoFar)
      } else {
        adtLikeDeclarations(valueType)
          .flatMap{
            case abstractType if (abstractType._2.typeSignature.typeSymbol.isAbstractType) => context.abort(context.enclosingPosition, "%s at %s is abstract.".format(abstractType, pathSoFar.map(_._1)))
            case primitive if(isComparableType(primitive._2.typeSignature)) => Seq(pathSoFar :+ (primitive._1, primitive._2.name))
            case possibleADT if(adtLikeDeclarations(possibleADT._2.typeSignature).size > 1) => determinePaths(possibleADT._2.typeSignature, pathSoFar :+ (possibleADT._1, possibleADT._2.name))
            case other => context.abort(context.enclosingPosition, "%s is not suitable for match for %s.".format(other, pathSoFar.map(_._1)))
          }
      }
    }

    def createLookup(value: context.Expr[_], path: Seq[(String, context.Name)]): context.Tree = {
      if (path.isEmpty) {
        value.tree
      } else {
        path.foldLeft(Ident(value.tree.symbol): context.Tree){(working, contextName) =>
          Select(working, newTermName(contextName._2.decoded))
        }
      }
    }

    def createComparison(path: Seq[(String, context.Name)]): context.Expr[Option[ValueDifference]] = {
      val leftExpr = context.Expr[LeftType](createLookup(left, path))
      val rightExpr = context.Expr[RightType](createLookup(right, path))
      val pathExpr = context.Expr[List[String]](Apply(definitions.ListModule, path.map(pathPart => Literal(Constant(pathPart._1))): _*))
      context.reify{
        if (leftExpr.splice == rightExpr.splice) None
        else Some(ValueDifference(pathExpr.splice, leftExpr.splice, rightExpr.splice))
      }
    }

    if (left.actualTpe.widen == right.actualTpe.widen) {
      val paths = determinePaths(left.actualTpe.widen)
      val pathComparisons = paths.map(path => createComparison(path))
      pathComparisons.foldLeft(context.reify(Seq[ValueDifference]())){(expr, pathComparison) =>
        context.reify(expr.splice ++ pathComparison.splice.toSeq)
      }
    } else  {
      sys.error("Left type %s does not match right type %s.".format(left.actualTpe.widen, right.actualTpe.widen))
    }
  }
}