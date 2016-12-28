# WorldWindJava-PCM
 
This is a fork of the original [NASA WorldWind Java](https://github.com/NASAWorldWind/WorldWindJava)
repository. 

This repository is not intended as an alternative but rather as a staging area for:

 * publishing artifacts to the [Central Repository](http://central.sonatype.org/)
 * potential bug fixes 
 * additional features
 
Please refer to the [original README.md](https://github.com/NASAWorldWind/WorldWindJava/blob/master/README.md) for
general WorldWindJava information, which is also included in this distribution as a renamed
`README_ORIGINAL.md`.
 
The `master` branch of this repository is only intended to publish artifacts (jars) to local caches
(~/.m2 or ~/.ivy2) and servers such as the [Central Repository](http://central.sonatype.org/).  It
does *not* contain re-distributed 3rd party libraries, and hence does not support the [Apache
Ant](http://ant.apache.org/) based build process of the original project, which depends on
re-distributed jars.

To import the binary artifacts produced by the `master` branch, add the following line to your SBT build configuration:
```scala
libraryDependencies += "com.github.pcmehlitz" % "worldwind-pcm" % "2.1.0.+"
```

The underlying version of the original repository is kept in a `base` branch. Use `git diff base`
to quickly assess changes made in the `master` branch.
 
Current changes in WorldWindJava-PCM fall into four areas:
 
(1) update to current versions of [JOGL](http://jogamp.org/) (2.3.2) and [GDAL](http://www.gdal.org/) (2.1.0)
 
(2) support publication of build artifacts to Maven Central, enabling client projects to 
add WorldWindJava as a normal 3rd party dependency (e.g. in build systems such as SBT, Gradle
or Maven). Note this is transitive, i.e. client projects do not have to copy or otherwise
resolve any of the libraries used by WorldWindJava. Note that WorldWindJava-PCM uses a 
different build system [SBT](http://www.scala-sbt.org/) to create/publish artifacts than 
the original WorldWindJava, which uses [Apache Ant](http://ant.apache.org/)

(3) thread safety related fixes - WorldWindJava-PCM is mostly used in projects such as
[RACE](https://github.com/nasarace/race) that extensively use concurrency and distributed
operation to import and process large and highly dynamic data sets. While not all rendering 
relevant  information can be updated outside the UI thread(s), some can with small respective
changes within WorldWind

(4) observable animation targets - WorldWindJava-PCM is used for viewers that can be synchronized
across the network (e.g. for applications such as situation rooms). To avoid serious lag and
potential loss of synchronization between collaborating viewers it is essential that animations
such as re-centering or zoom-in/-out are not transmitted as a stream of transient eye positions
but as the targeted end-position of the respective animation. To that end, WorldWindJava-PCM 
contains small, minimally-invasive extensions that allow observation of animation end points 
in user provided input handlers by overriding a new stub method of `OrbitViewInputHandler`

```java
protected void setTargetEyePosition(Position targetPosition, AnimationController controller, String actionKey)
```


Please note this repository uses the same 
[NOSA v1.3 license](https://github.com/NASAWorldWind/WorldWindJava/blob/master/LICENSE.txt) as the
original.