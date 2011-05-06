import java.io.File
import java.util.jar.Manifest
import org.apache.commons.io.FileUtils
import sbt._

class VideoTrainingProject(info: ProjectInfo) extends DefaultWebProject(info) with IdeaProject {

  val jetty6version = "6.1.26"
  val slf4jversion = "1.6.1"

  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
  val scalaToolsReleases = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-releases"
  val gsonMavenRepo = "Gson Maven2 repository" at "http://google-gson.googlecode.com/svn/mavenrepo"

  val scalatra = "org.scalatra" %% "scalatra" % "2.0.0.M2" withSources
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % "2.0.0.M2" % "test" withSources
  val scalatraFileUpload = "org.scalatra" %% "scalatra-fileupload" % "2.0.0.M2" withSources
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources
  val process = "org.scala-tools.sbt" % "process" % "0.1"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jversion
  val slf4jImpl = "org.slf4j" % "slf4j-jdk14" % slf4jversion
  val slf4jJclBridge = "org.slf4j" % "jcl-over-slf4j" % slf4jversion
  val mockito = "org.mockito" % "mockito-core" % "1.8.5" % "test" withSources
  val servletapi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val jetty6 = "org.mortbay.jetty" % "jetty" % jetty6version % "provided"
  val jetty6tester = "org.mortbay.jetty" % "jetty-servlet-tester" % jetty6version % "test"
  val gson = "com.google.code.gson" % "gson" % "1.4" withSources
  val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.1" withSources
  

  override def jettyContextPath = "/video-training"

  //use this if need to compile with 1.5
  //override def compileOptions = super.compileOptions ++ Seq(target(Target.Java1_5))

  lazy val standalone = standaloneWarAction dependsOn(`package`) describedAs("create standalone überwar")
  def standaloneWarAction = task {
    val originalWebapp = (outputPath / "webapp" ##)
    val targetWar = outputPath / ("geoip-standalone.war")
    val launcherClass = (mainClasses ** ("JettyLauncher.class"))
    val stopMonitorClass = (mainClasses ** ("JettyLauncher$JettyStopMonitorThread.class"))
    val bootstrapDir = (outputPath / "standalone" ##)

    unzipBootstrapLibraries(bootstrapDir)

    val sources = originalWebapp.get ++ bootstrapDir.get ++ launcherClass.get ++ stopMonitorClass.get
    val manifest = createManifest(launcherClass.getFiles.toSeq.first)
    FileUtilities.jar(sources, targetWar, manifest, true, log)
  }

  private def unzipBootstrapLibraries(outputDir: Path) {
    val includes = Seq("jetty-", "servlet-api-")
    providedClasspath.getFiles
      .filter(file => includes.filter(file.getName.contains(_)).size > 0)
      .foreach { FileUtilities.unzip(_, outputDir, log) }
    FileUtils.deleteDirectory((outputDir / "META-INF").asFile)
  }

  private def createManifest(mainClassFile: File) = {
    val manifest = new Manifest
    manifest.getMainAttributes.putValue("Main-Class", mainClassFile.getName.replace(".class", ""))
    manifest
  }

  override def ivyXML =
    <dependencies>
      <dependency org="net.lag" name="configgy" rev="2.0.0">
        <exclude module="vscaladoc"/>
      </dependency>
      <exclude module="slf4j-log4j12"/>
      <exclude module="commons-logging"/>
    </dependencies>
}

