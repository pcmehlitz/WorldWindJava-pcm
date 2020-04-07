// basic SBT project definition to generate and publish WorldWindJava artifacts

// note: we need to preserve the WorldWind directory structure, which does not 
// conform with Maven/Gradle/SBT. Since WWJ keeps resources (properties, images)
// within the Java source directories, we have to copy them explicitly

import scala.util.matching.Regex
import scala.sys.process.Process

shellPrompt in ThisBuild := { state => "[" + Project.extract(state).currentRef.project + "]> " }

//--- external dependencies
// since there still is no 2.4 release of jogl-all and gluegen we have to turn this for now
// into a unmanaged dependency. We will reverse this once they become available
//val jogl = "org.jogamp.jogl" % "jogl-all-main" % "2.3.2" // "2.1.5-01"
//val gluegen = "org.jogamp.gluegen" % "gluegen-rt-main" % "2.3.2" // "2.1.5-01"
val gdal = "org.gdal" % "gdal" % "3.0.0" // "2.4.0" // "2.3.0"

val worldwindxPattern = ".*/worldwindx/.*".r

def fileFilter (regex: Regex) = new FileFilter {
  def accept(f: File) = regex.pattern.matcher(f.getAbsolutePath).matches
}

def resourceDirMap (srcRoot: File, subDirName: String) = {
  val files = ((srcRoot / subDirName) ** "*").get
  files.map( f => (f, f.relativeTo(srcRoot).get.getPath))
}

def patternMap (srcRoot: File, subDir: String, glob: String) = {
  val files = ((srcRoot / subDir) ** glob).get
  files.map( f => (f, f.relativeTo(srcRoot).get.getPath))
}

val gitRev = settingKey[String]("retrieve git rev-list count")


//--- project definition

lazy val wwjRoot = Project("wwjRoot", file(".")).
  settings(
    organization := "com.github.pcmehlitz",
    name := "worldwind-pcm",
    libraryDependencies ++= Seq(gdal), // Seq(jogl,gluegen,gdal),

    scalaVersion := "2.13.1", // not really used yet
    crossPaths := false,

    // our versioning scheme consists of the 3-number original WWJ version (e.g. 2.1.0) base, followed by
    // our Git revision number (e.g. 2.1.0.177)
    gitRev := Process("git rev-list --count HEAD", baseDirectory.value).lineStream.head,
    version := "2.1.0." + gitRev.value,
    javaSource in Compile := baseDirectory.value / "src",

    // we omit example applications from the build artifacts
    excludeFilter in Compile := fileFilter( worldwindxPattern),

    publishArtifact in Test := false,

    javacOptions ++= Seq(
      "-source","9",
      "-target","9"
    ),

    javacOptions in (Compile,doc) := Seq(
      "-J-Xmx1024m",
      "-splitindex",
      "-exclude","gov.nasa.worldwindx:gov.nasa.worldwind.util.webview",
      "-Xdoclint:none"
    ),

    // we have to copy resources explicitly since there is no resource dir hierarchy
    // (note we have to do this for both packageBin and compile tasks)
    mappings in Compile := resourceDirMap( (javaSource in Compile).value, "config"),
    mappings in Compile ++= resourceDirMap( (javaSource in Compile).value, "images"),
    mappings in Compile ++= patternMap( (javaSource in Compile).value, "gov/nasa/worldwind/util", "*.properties"),

    // we need to copy resources into target/classes in case we run this with a directory based classpath
    compile in Compile := {
      val clsDir = (classDirectory in Compile).value
      val fileMappings = (mappings in Compile).value 
      IO.copy(fileMappings.map( e => (e._1, clsDir / e._2)))

      (compile in Compile).value
    },

    // NOTE - it is considered to be bad practice to publish fat jars but until jogl and gluegen
    // artifacts for version 2.4.x are published on Maven Central we have to resort to a fat jar unless
    // we add the jogl and gluegen jars as unmanaged depenencies to clients, which is worse in
    // case these libs are only used by worldwind. We will revert when the jogamp artifacts are
    // updated (which will not require client build changes) but until then there is only the
    // choice between a bad and a worse option. 
    // The 2.3.2 artifacts do not work with contemporary Java versions (>12)
    packageBin in Compile := assembly.value
  )

addArtifact(artifact in (Compile, assembly), assembly)

//---- publishing meta data
pomExtra in Global := {
  <url>https://github.com/pcmehlitz/WorldWindJava-pcm.git</url>
    <licenses>
      <license>
        <name>NASA OPEN SOURCE AGREEMENT VERSION 1.3</name>
        <url>https://ti.arc.nasa.gov/opensource/nosa/</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/pcmehlitz/WorldWindJava-pcm.git</url>
      <connection>scm:git:github.com/pcmehlitz/WorldWindJava-pcm.git</connection>
    </scm>
    <developers>
      <developer>
        <id>pcmehlitz</id>
        <name>Peter Mehlitz</name>
        <url>https://github.com/pcmehlitz</url>
      </developer>
    </developers>
}
