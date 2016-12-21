// basic SBT project definition to generate and publish WorldWindJava artifacts

// note: we need to preserve the WorldWind directory structure, which does not 
// conform with Maven/Gradle/SBT. Since WWJ keeps resources (properties, images)
// within the Java source directories, we have to copy them explicitly

import scala.util.matching.Regex

shellPrompt in ThisBuild := { state => "[" + Project.extract(state).currentRef.project + "]> " }

//--- but define the external dependencies here so that we can publish binary artifacts
//    (latest jogl-all-main and gluegen-rt-main are 2.3.2, latest gdal is 2.1.0, but WWJ does not compile against these)
val jogl = "org.jogamp.jogl" % "jogl-all-main" % "2.1.5-01"
val gluegen = "org.jogamp.gluegen" % "gluegen-rt-main" % "2.1.5-01"
val gdal = "org.gdal" % "gdal" % "2.0.0" // 1.11.2"

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

val gitRef = settingKey[String]("retrieve git rev-list count")


//--- project definition

lazy val wwjRoot = Project("wwjRoot", file(".")).
  settings(
    organization := "gov.nasa",
    name := "worldwind",
    libraryDependencies ++= Seq(jogl,gluegen,gdal),

    scalaVersion := "2.11.7",
    crossPaths := false,

    gitRef := Process("git rev-list --count HEAD", baseDirectory.value).lines.head,
    version := "2.0-pcm-r" + gitRef.value,
    javaSource in Compile := baseDirectory.value / "src",
    excludeFilter in Compile := fileFilter( worldwindxPattern),

    //artifactPath in (Compile, packageBin) := file(s"${target.value}/${name.value}-2.0-pcm-r${version.value}.jar"),
    publishArtifact in (Compile, packageBin) := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,

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

    // unmanaged libs are still in the base dir
    unmanagedBase in Compile := baseDirectory.value
  )

