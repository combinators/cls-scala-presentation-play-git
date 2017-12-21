package org.combinators.cls.git

import java.nio.file.Path

import org.combinators.cls.inhabitation.Tree
import org.combinators.cls.interpreter.InhabitationResult
import org.combinators.cls.types.Type
import org.combinators.templating.persistable.Persistable
import shapeless.feat.Enumeration

/** A collection of persistable inhabitation results. */
sealed trait Results {
  self =>
  /** Location which results are stored relative to. */
  val storeRelativeTo: Path
  /** Targets for this result collection. */
  val targets: Seq[(Type, Option[BigInt])]
  /** Raw inhabitant terms without any interpretation. */
  val raw: Enumeration[Seq[Tree]]
  /** Are there infinitely many inhabitants? */
  val infinite: Boolean
  /** Did any target not produce an inhabitant? */
  val incomplete: Boolean
  /** Actions to perform with each inhabitant (e.g. store to disk). */
  val persistenceActions: Enumeration[Seq[() => Unit]]

  /** Runs the action associated with the `number`-th inhabitant.
    * Most actions will just store the inhabitants to disk/Git (hence the method name).
    *
    * @param number index of the action to run.
    */
  def storeToDisk(number: Long): Unit = {
    val result = persistenceActions.index(number)
    result.foreach(_.apply())
  }


  /**
    * Adds an inhabitation result to the collection.
    * Create a default persistable using `inhabitationResult.toString` for serialization and `repositoryPath`.
    *
    * @param inhabitationResult the result to store.
    * @param repositoryPath     where to store `inhabitationResult` relative to [[storeRelativeTo]].
    * @return a new collection including `inhabitationResult`.
    */
  def add[R](inhabitationResult: InhabitationResult[R], repositoryPath: Path): Results =
    add[R](inhabitationResult)(new Persistable {
      type T = R
      override def rawText(elem: T): Array[Byte] = elem.toString.getBytes
      override def path(elem: T): Path = repositoryPath
    })

  /** Adds a [[Persistable]] result to the collection.
    * Persistance actions will be performed relative to [[storeRelativeTo]].
    *
    * @param inhabitationResult the result to store.
    * @return a new collection including `inhabitationResult`.
    */
  def add[T](inhabitationResult: InhabitationResult[T])(implicit persistable: Persistable.Aux[T]): Results = {
    val size = inhabitationResult.size

    size match {
      case Some(x) if x == 0 => new Results {
        val storeRelativeTo = self.storeRelativeTo
        val targets = self.targets :+ (inhabitationResult.target, size)
        val raw = self.raw
        val persistenceActions = self.persistenceActions
        val infinite = self.infinite
        val incomplete = true
      }
      case _ => new Results {
        val storeRelativeTo = self.storeRelativeTo
        val targets = self.targets :+ (inhabitationResult.target, inhabitationResult.size)
        val raw = self.raw.product(inhabitationResult.terms).map {
          case (others, next) => others :+ next
        }
        val persistenceActions = self.persistenceActions.product(inhabitationResult.interpretedTerms).map {
          case (ps, r) => ps :+ (() => { persistable.persist(storeRelativeTo, r); () })
        }
        val infinite = self.infinite || inhabitationResult.isInfinite
        val incomplete = self.incomplete
      }
    }
  }

  /** Adds an external (non-inhabitation generated) artifact to the result repository. */
  def addExternalArtifact[T](inhabitationResult: T)(implicit persistable: Persistable.Aux[T]): Results = new Results {
    val storeRelativeTo = self.storeRelativeTo
    val targets = self.targets
    val raw = self.raw
    val persistenceActions = self.persistenceActions.map { actions: Seq[() => Unit] =>
      actions :+ (() => { persistable.persist(storeRelativeTo, inhabitationResult); () })
    }
    val infinite = self.infinite
    val incomplete = self.incomplete
  }

  /** Adds all results of an InhabitationResultVector. **/
  def addAll[R](results: R)(implicit canAddAll: InhabitationResultVector[R]): Results =
    canAddAll.add(results, this)
}

/** Wrapper for a configured location relative to which inhabitation results are stored. */
case class ResultLocation(relativeTo: Path)

/** An empty collection of inhabitation results. */
case class EmptyResults()(implicit resultLocation: ResultLocation) extends Results {
  val storeRelativeTo: Path = resultLocation.relativeTo
  val targets: Seq[(Type, Option[BigInt])] = Seq.empty
  val raw: Enumeration[Seq[Tree]] = Enumeration.singleton(Seq.empty)
  val persistenceActions: Enumeration[Seq[() => Unit]] = Enumeration.singleton(Seq.empty)
  val infinite: Boolean = false
  val incomplete: Boolean = false
}