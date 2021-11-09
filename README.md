# nuts
Network Updatable Things Services
<pre>
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   dev version 0.8.3.0 (accessible on thevpc.net)
\_\ \/\__,_/\__/____/    production version 0.8.2.1 (accessible on maven central)
</pre>

website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)

```nuts``` is a Java™ Package Manager that helps discovering, downloading, assembling and executing local and remote artifacts (packages) in a very handy way.

Unlike maven which resolves dependencies at compile time, ```nuts``` solves dependencies and builds the classpath at install time and, as a result, saves disk and bandwidth by downloading and caching only libraries required for the current environment and share them between multiple installed applications.

```nuts``` is unique in that it reuses maven and other build tool descriptor formats to solve dependency graph, and does not, whatsoever, have any requirement on existing maven created packages.

```nuts``` is the ultimate solution to get rid of the ugly lib jars, fat-jars, uber-jars and one-jars used for deploying java applications.

## SYNOPSYS

This is just enough info to get you up and running ```nuts``` .
Much more info is available in [nuts documentation website](https://thevpc.github.io/nuts).
You can have some information also browsing [thevpc.net](http://thevpc.net/nuts/)
Even more information will is available via ```nuts help``` once it's installed.

To check current nuts version
```
nuts --version
```

It should show a result in the format : nuts-api-version/nuts-impl-version

```
0.8.3/0.8.3.0
```

## Installing Nuts (Development, recommended for testing)

The very same procedure applies whether you already have an existing version of nuts installed or not.
If you want to install (or update from existing rolling version) you just need to do the following.
Please that nuts rolling version (which is the most recent version) is quite stable and you can use it for personal usage, for development or for testing but we recommend you consider official versions for production systems.

First you need to download nuts.jar

```
wget http://thevpc.net/nuts.jar -O nuts.jar
```

Then you must run, in a terminal, the following command. This will install or update nuts bootstrap binaries.

```
java -jar nuts.jar -r dev -N -y
```

That’s it, now you must relaunch the terminal window (close the terminal and start it again).

The following command should show you the actual version


## Installing Nuts (Stable, recommended for Production)

The very same procedure applies whether you already have an existing version of nuts installed or not.

First you need to download nuts.jar

```
wget wget https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/0.8.2/nuts-0.8.2.jar -O nuts.jar
```

If you want a shorter link, use this one!
```
wget http://thevpc.net/nuts-stable.jar -O nuts.jar
```


Then you must run, in a terminal, the following command. This will install or update nuts bootstrap binaries.

```
java -jar nuts.jar -N -y
```

That’s it, now you must relaunch the terminal window (close the terminal and start it again).

The following command should show you the actual version

## Updating from previously installed version

```
nuts update
```

## Run a command


To install a command using **nuts** just type

```
nuts install <package>
```

To run an artifact using **nuts** just type

```
nuts <package>
```

Several commands are available, and you can always manually run any java and non java application. More info is available in the Nuts official website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts).

## Call for Contribution
Nuts have lots of ways to be improved. Please feel free to join the journey.
