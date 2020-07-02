import sbt.Keys._
import sbt._
import sbt.librarymanagement._
import sbtrelease.ReleasePlugin.autoImport.releaseCommitMessage

object ReleaseSettings extends LibraryManagementSyntax {

  lazy val settings = Seq(
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [ci-skip]"
  )
}
