package org.combinators.cls.git

import org.combinators.cls.interpreter.InhabitationResult
import org.combinators.templating.persistable.Persistable

/** A type class for heterogeneous vector of inhabitation Results */
sealed trait InhabitationResultVector[R] {
  def add(newResults: R, oldResults: Results): Results
}

/** Type class instances to build up result vectors */
sealed trait InhabitationResultVectorInstances {
  implicit def persistable[R](implicit persist: Persistable.Aux[R]): InhabitationResultVector[InhabitationResult[R]] =
    new InhabitationResultVector[InhabitationResult[R]] {
      def add(newResults: InhabitationResult[R], oldResults: Results): Results =
        oldResults.add[R](newResults)(persist)
    }

  implicit def product[L, R]
  (implicit persist: Persistable.Aux[R],
    vector: InhabitationResultVector[L]): InhabitationResultVector[(L, InhabitationResult[R])] =
    new InhabitationResultVector[(L, InhabitationResult[R])] {
      def add(newResults: (L, InhabitationResult[R]), oldResults: Results): Results =
        vector.add(newResults._1, oldResults).add[R](newResults._2)(persist)
    }
}

/** Collection of type class instances to build up result vectors */
object InhabitationResultVector extends InhabitationResultVectorInstances {
  def apply[R](implicit vectorInst: InhabitationResultVector[R]): InhabitationResultVector[R] =
    vectorInst
}
