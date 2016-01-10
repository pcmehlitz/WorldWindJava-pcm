// basic SBT project definition to generate and publish WorldWindJava artifacts

import scala.util.matching.Regex

shellPrompt in ThisBuild := { state => "[" + Project.extract(state).currentRef.project + "]> " }

//--- but define the external dependencies here so that we can publish binary artifacts
val jogl = "org.jogamp.jogl" % "jogl-all-main" % "2.1.5-01"
val gluegen = "org.jogamp.gluegen" % "gluegen-rt-main" % "2.1.5-01"
val gdal = "org.gdal" % "gdal" % "2.0.0" // 1.11.2"

val worldwindxPattern = ".*/worldwindx/.*".r

lazy val gitRev = Process("git rev-list --count HEAD").lines.head

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

//--- project definition

lazy val root = (project in file(".")).
  settings(
    organization := "gov.nasa",
    name := "worldwind",
    libraryDependencies ++= Seq(jogl,gluegen,gdal),

    scalaVersion := "2.11.7",
    crossPaths := false,

    version := "2.0-pcm-r" + Process("git rev-list --count HEAD").lines.head, 
    javaSource in Compile := baseDirectory.value / "src",
    excludeFilter in Compile := fileFilter( worldwindxPattern),

    //artifactPath in (Compile, packageBin) := file(s"${target.value}/${name.value}-2.0-pcm-r${version.value}.jar"),
    publishArtifact in (Compile, packageBin) := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,

    // we have to copy resources explicitly since there is no resource dir hierarchy
    mappings in (Compile, packageBin) ++= resourceDirMap( (javaSource in Compile).value, "config"),
    mappings in (Compile, packageBin) ++= resourceDirMap( (javaSource in Compile).value, "images"),
    mappings in (Compile, packageBin) ++= patternMap( (javaSource in Compile).value, "gov/nasa/worldwind/util", "*.properties"),

    // unmanaged libs are still in the base dir
    unmanagedBase in Compile := baseDirectory.value
  )

