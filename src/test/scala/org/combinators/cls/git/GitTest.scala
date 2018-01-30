package org.combinators.cls.git

import collection.JavaConverters._
import java.io.File
import java.nio.file.{Path, Paths}
import javax.inject.Inject

import org.apache.commons.io.FileUtils
import org.combinators.cls.interpreter.{CombinatorInfo, ReflectedRepository, combinator}
import org.combinators.templating.persistable._
import org.eclipse.jgit.api.{CreateBranchCommand, Git}
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.webjars.play.WebJarsUtil
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

object Expected {
  val expectedPaths: Set[Path] = Set(
    Paths.get("test.txt"),
    Paths.get("test", "test.txt")
  )
}

class GitTestController @Inject()(webJars: WebJarsUtil, applicationLifecycle: ApplicationLifecycle)
  extends InhabitationController(webJars, applicationLifecycle) {
  implicit val persistable: Persistable.Aux[Path] = new Persistable {
    override type T = Path
    override def rawText(elem: T): Array[Byte] = elem.toString.getBytes
    override def path(elem: T): Path = elem
  }

  class Repository {}
  class TestCombinator(path: Path) {
    def apply: Path = path
  }
  val Gamma: ReflectedRepository[Repository] =
    Expected.expectedPaths.foldLeft(ReflectedRepository(new Repository, classLoader = getClass.getClassLoader)) {
      (repo, path) => repo.addCombinator(new TestCombinator(path))
    }

  override val combinatorComponents: Map[String, CombinatorInfo] = Gamma.combinatorComponents
  override val results: Results =
    EmptyResults().add(Gamma.inhabit[Path]())
}

class GitTestRoutes @Inject()(controller: GitTestController) extends InhabitationRouter("gittest", controller)

class GitTest extends PlaySpec with GuiceOneServerPerSuite {
  val client = app.injector.instanceOf[WSClient]
  "Calling the gittest overview" must {
    "result in a valid response" in {
      val request = s"/gittest"
      val url = s"http://localhost:$port$request"
      val response = await(client.url(url).get())
      response.status mustBe OK
      response.body.toLowerCase.indexOf("repository") must be > 0
    }
  }
  "Preparing the results" must {
    "result in a valid response" in {
      for (resultNumber <- Expected.expectedPaths.toSeq.indices) {
        val request = s"/gittest/prepare?number=$resultNumber"
        val url = s"http://localhost:$port$request"
        val response = await(client.url(url).get())
        response.status mustBe OK
      }
    }
    "create valid git repositories" in {
      val request = s"/gittest/gittest.git"
      val url = s"http://localhost:$port$request"
      val tmpDir = File.createTempFile("gittest", "")
      tmpDir.delete() mustBe true // we want just the location
      val toClone = Expected.expectedPaths.toSeq.indices map (n => s"variation_$n")
      try {
        val resultGit: Git =
          Git.cloneRepository()
            .setURI(url)
            .setDirectory(tmpDir)
            .setBranchesToClone(toClone.asJava)
            .call()
        resultGit.fetch().call()
        toClone.foldLeft[Set[String]](Expected.expectedPaths.map(_.toString))((remainingPaths, branch) => {
          resultGit.checkout()
            .setName(s"origin/$branch")
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            //.setStartPoint(s"origin/$branch")
            .call()
          val repository = resultGit.getRepository
          val treeWalk = new TreeWalk(repository)
          try {
            treeWalk.addTree(new RevWalk(repository)
              .parseCommit(repository.resolve(Constants.HEAD)).getTree)
            treeWalk.setRecursive(true)
            treeWalk.next mustBe true
            val currentPath = treeWalk.getPathString
            remainingPaths.contains(currentPath) mustBe true
            new String(repository.open(treeWalk.getObjectId(0)).getBytes) mustBe currentPath
            treeWalk.next mustBe false
            remainingPaths - currentPath
          } finally {
            treeWalk.close()
          }
        })
      } finally {
        FileUtils.deleteDirectory(tmpDir)
      }
    }
  }
}