# Unmanaged Dependencies

This directory contains the latest JOGL snapshots from 
[https://jogamp.org/deployment/v2.4.0-rc-20200307/]

We have to package these libraries in a fat jar since at
the time of this writing (04/2020) there are no respective
artifacts yet on the [Central Repository](https://mvnrepository.com/artifact/org.jogamp.jogl)
but version 2.4+ is required to run WorldWind on Java 9+.

We will remove the unmanaged dependencies once such artifacts
become available.
