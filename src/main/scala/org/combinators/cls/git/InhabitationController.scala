/*
 * Copyright 2017 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.combinators.cls.git

import java.nio.file._

import org.apache.commons.io.FileUtils
import org.combinators.cls.interpreter._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.revwalk.RevCommit
import org.webjars.play.WebJarsUtil
import play.api.inject.ApplicationLifecycle
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try

/** Serves a website to access Git repositories with computed and serialized inhabitants.
  * To get the website, just inherit from this class, implement the abstract fields/methods
  * in `myresults.MyResults` and route like this:
  * <pre>
  * GET   &sol;myresults                        myresults.MyResults.overview()
  * GET   &sol;myresults&sol;raw_:number            myresults.MyResults.raw(number: Long)
  * GET   &sol;myresults&sol;prepare                myresults.MyResults.prepare(number: Long)
  * GET   &sol;myresults&sol;myresults.git&sol;*file    myresults.MyResults.serveFile(file)
  * </pre>
  *
  * If there are multiple inhabitants they go into branches of the git hostet at `/myresults/myresults.git`.
  *
  * @param webJars Helper to get WebJars like Bootstrap.
  * @param applicationLifecycle application lifecycle to register cleanup of temporary files on shutdown
  */
abstract class InhabitationController(webJars: WebJarsUtil, applicationLifecycle: ApplicationLifecycle) extends InjectedController {
  /** A temporary place to store the Git repository on disk. */
  private lazy val root: Path = {
    val tmp = Files.createTempDirectory("inhabitants")
    applicationLifecycle.addStopHook(() => {
      Future.fromTry(Try {
        FileUtils.deleteDirectory(tmp.toFile)
      })
    })
    tmp
  }

  /** The temporary Git file structure. */
  private lazy val git = Git.init().setDirectory(root.toFile).call()
  /** A mutable collection to store which inhabitants are already serialized. */
  private lazy val computedVariations = collection.mutable.Set.empty[Long]

  /** All combinator names together with their reflected type information. */
  val combinatorComponents: Map[String, CombinatorInfo]

  /** The path (relative to the Git root) where to store solutions. */
  val sourceDirectory: Path = Paths.get(".")

  /** The computed result location (root/sourceDirectory) */
  implicit final val resultLocation: ResultLocation = ResultLocation(root.resolve(sourceDirectory))

  /** The results to present. */
  val results: Results

  /** Creates a new variation branch. */
  private def checkoutEmptyBranch(id: Long): Unit = {
    git
      .checkout()
      .setOrphan(true)
      .setName(s"variation_$id")
      .call()
    git.reset()
      .setMode(ResetType.HARD)
      .call()
  }

  /** Commits all files to the current branch */
  private def addAllFilesToCurrentBranch(): RevCommit = {
    git
      .add()
      .addFilepattern(".")
      .call()
    git
      .commit()
      .setMessage("Next variation")
      .call()
  }

  /** Creates the dumb protocol [[https://git-scm.com/book/en/v2/Git-Internals-Transfer-Protocols]] file.
    *
    * @param rev branch head to create the file for.
    * @param id  variation number of the result.
    */
  private def updateInfo(rev: RevCommit, id: Long): Unit = {
    val info = Paths.get(root.toString, ".git", "info")
    Files.createDirectories(info)
    val refs = Paths.get(root.toString, ".git", "info", "refs")
    val line = s"${rev.getName}\trefs/heads/variation_$id\n"
    Files.write(refs, line.getBytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  }

  /** Creates a Git containing the `number`-th result.
    *
    * @param number index of the result to store.
    * @return term representation of the uninterpreted result.
    */
  def prepare(number: Long) = Action {
    val branchFile = Paths.get(root.toString, ".git", "refs", "heads", s"variation_$number")
    val result = results.raw.index(number).toString
    if (!Files.exists(branchFile)) {
      checkoutEmptyBranch(number)
      results.storeToDisk(number)
      val rev = addAllFilesToCurrentBranch()
      updateInfo(rev, number)
      computedVariations.add(number)
    }
    Ok(result)
  }

  /** Renders an overview page with access to all inhabitation results.
    *
    * @return the html code of the page.
    */
  def overview() = Action { request =>
    val combinators = combinatorComponents.mapValues {
      case staticInfo: StaticCombinatorInfo =>
        (ReflectedRepository.fullTypeOf(staticInfo),
          s"${scala.reflect.runtime.universe.show(staticInfo.fullSignature)}")
      case dynamicInfo: DynamicCombinatorInfo[_] =>
        (ReflectedRepository.fullTypeOf(dynamicInfo),
          dynamicInfo.position.mkString("\n"))
    }
    Ok(html.overview.render(
      request.path,
      webJars,
      combinators,
      results.targets,
      results.raw,
      computedVariations.toSet,
      results.infinite,
      results.incomplete))
  }

  /** Returns the uninterpreted raw representation of the `number`-th inhabitant. */
  def raw(number: Long) = Action {
    try {
      Ok(results.raw.index(number).toString())
    } catch {
      case _: IndexOutOfBoundsException => play.api.mvc.Results.NotFound(s"404, Inhabitant not found: $number")
    }
  }

  /** Serves a file from the Git of all inhabitants.
    *
    * @param name file name relative to the Git root.
    * @return the file contents as an array of bytes.
    */
  def serveFile(name: String) = Action {
    try {
      Ok(Files.readAllBytes(root.resolve(Paths.get(".git", name))))
    } catch {
      case _: NoSuchFileException => play.api.mvc.Results.NotFound(s"404, File not found: $name")
      case _: AccessDeniedException => play.api.mvc.Results.Forbidden(s"403, Forbidden: $name")
    }
  }
}