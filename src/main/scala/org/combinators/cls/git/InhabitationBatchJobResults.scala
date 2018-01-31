package org.combinators.cls.git

import org.combinators.cls.interpreter.{InhabitationResult, ReflectedRepository}
import org.combinators.cls.types.Type
import org.combinators.templating.persistable.Persistable

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.WeakTypeTag

/** Collects requests in an [[ReflectedRepository#InhabitationBatchJob]], which will
  * compute [[Results]] for an [[InhabitationController]]. */
sealed trait InhabitationBatchJobResults { self =>
  /** The type of the reflected repository relative to which the job will be performed */
  type R
  /** The reflected repository relative to which the job will be performed */
  val repository: ReflectedRepository[R]
  /** Location where [[InhabitationController]] will store results. */
  implicit val resultLocation: ResultLocation

  /** Adds a job to collect results from.
    *
    * @see [[ReflectedRepository#InhabitationBatchJob]]
    * @param semanticTypes the requested semantic types.
    * @param tag evidence to reflect on `Q`.
    * @param persistable evidence how to persist `Q` in a git repository.
    * @tparam Q the requested native type.
    * @return a result collection augmented by the specified request.
    */
  def addJob[Q](semanticTypes: Type*)(implicit
    tag: WeakTypeTag[Q],
    persistable: Persistable.Aux[Q]
  ): NonEmptyInhabitationBatchJobResults

  /** Add a job with native request type `Q` for each semantic type in `semanticTypes`. */
  def addJobs[Q](semanticTypes: Seq[Type])(implicit
    tag: WeakTypeTag[Q],
    persistable: Persistable.Aux[Q]
  ): InhabitationBatchJobResults

  /** Computes the results for the underlying [[ReflectedRepository#InhabitationBatchJob]] */
  def compute(): Results
}

/** Collect at least one request in an [[ReflectedRepository#InhabitationBatchJob]], which will
  * compute non empty [[Results]] for an [[InhabitationController]].
  */
sealed trait NonEmptyInhabitationBatchJobResults extends InhabitationBatchJobResults { self =>
  /** The batch job to perform */
  val job: repository.InhabitationBatchJob
  /** Evidence that the job results can be used within [[InhabitationController]]. */
  val canAddAll: InhabitationResultVector[job.ResultType]

  override def compute(): Results = EmptyResults().addAll(job.run())(canAddAll)

  override def addJob[Q](semanticTypes: Type*)(implicit
      tag: WeakTypeTag[Q],
      persistable: Persistable.Aux[Q]
    ): NonEmptyInhabitationBatchJobResults =
    new NonEmptyInhabitationBatchJobResults {
      override type R = self.R
      lazy val repository: self.repository.type = self.repository
      lazy val job: repository.InhabitationBatchJob.AuxWithPrior[Q, self.job.ResultType] =
        self.job.addJob[Q](semanticTypes:_*)
      lazy val canAddAll: InhabitationResultVector[job.ResultType] = {
        implicit val last: InhabitationResultVector[self.job.ResultType] = self.canAddAll
        InhabitationResultVector[job.ResultType]
      }
      implicit lazy val resultLocation: ResultLocation = self.resultLocation
    }

  override def addJobs[Q](semanticTypes: Seq[Type])(implicit
    tag: WeakTypeTag[Q],
    persistable: Persistable.Aux[Q]
  ): NonEmptyInhabitationBatchJobResults =
    semanticTypes.foldLeft[NonEmptyInhabitationBatchJobResults](self) { (results, tgt) =>
      results.addJob[Q](tgt)
    }
}

/** Starts collections of requests in an [[ReflectedRepository#InhabitationBatchJob]], which will
  * compute [[Results]] for an [[InhabitationController]]. */
case class EmptyInhabitationBatchJobResults[Rep](repository: ReflectedRepository[Rep])(implicit
    val resultLocation: ResultLocation
  ) extends InhabitationBatchJobResults { self =>
  type R = Rep
  override def addJob[Q](semanticTypes: Type*)(implicit
      tag: WeakTypeTag[Q],
      persistable: Persistable.Aux[Q]
    ): NonEmptyInhabitationBatchJobResults =
    new NonEmptyInhabitationBatchJobResults {
      type R = self.R
      lazy val repository: ReflectedRepository[R] = self.repository

      lazy val job: repository.InhabitationBatchJob.Aux[Q] =
        repository.InhabitationBatchJob[Q](semanticTypes:_*)

      lazy val canAddAll: InhabitationResultVector[job.ResultType] =
        InhabitationResultVector[job.ResultType]
      implicit lazy val resultLocation: ResultLocation = self.resultLocation
    }

  override def addJobs[Q](semanticTypes: Seq[Type])(implicit
    tag: WeakTypeTag[Q],
    persistable: Persistable.Aux[Q]
  ): InhabitationBatchJobResults =
    semanticTypes.foldLeft[InhabitationBatchJobResults](self)((results, tgt) =>
      results.addJob(tgt)
    )

  override def compute(): Results = EmptyResults()
}