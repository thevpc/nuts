# nuts
Network Updatable Things Services
<pre>
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   dev version 0.8.5.0 
\_\ \/\__,_/\__/____/    production version 0.8.5.0 
</pre>

Website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)

```nuts``` is a Java™ Package Manager that helps discover, download, assemble and execute local and remote artifacts (packages) in an easy and straightforward way.

Unlike Maven which resolves dependencies at compile time, ```nuts``` solves dependencies and builds the classpath at install time and, as a result, saves disk storage and bandwidth by downloading and caching only libraries required for the current environment and share them between multiple installed applications.

```nuts``` is unique in that it reuses Maven and other build tool descriptor formats to solve dependency graphs, and does not, whatsoever, have any requirement on existing Maven created packages.

```nuts``` is the ultimate solution to get rid of the ugly lib jars, fat-jars, uber-jars and one-jars used for deploying java applications.

## SYNOPSIS

This is just enough info to get you up and running ```nuts``` .
Much more info is available on [nuts documentation website](https://thevpc.github.io/nuts).
You can also have more information also browsing [thevpc.net](https://thevpc.net/nuts/).
Even more information is available via ```nuts help``` once it's installed.

To check current nuts version

```
nuts --version
```

It should show a result in the format : nuts-api-version/nuts-runtime-version

```
0.8.5/0.8.5.0
```

## Installing Nuts Preview (Development, recommended for testing)

If you want to install ```nuts``` (or update from an existing rolling version) you just need to do the following. <br />
<br />
Please note that ```nuts``` rolling version (which is the most recent version) is quite stable and you can use it for personal usage, for development or for testing but we recommend you consider official versions for production systems.

First you need to download nuts-preview.jar

```
curl -sL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar && java -jar nuts.jar -Zy
```

That’s it, now you must relaunch the terminal window (close the terminal and start it again).

The following command should show you the current version

```
nuts --version
```


## Installing Nuts (Stable, recommended for Production)

The very same procedure applies whether you already have an existing version of nuts installed or not.

First you need to download nuts-stable.jar

```
curl -sL https://thevpc.net/nuts/nuts-stable.jar -o nuts.jar && java -jar nuts.jar -Zy
```

That’s it, now you must relaunch the terminal window (close the terminal and start it again).

The following command should show you the current version

```
nuts --version
```

## Updating from a previously installed version

Run the following command to update to the latest version 

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

Several commands are available, and you can always manually run any java and non java application. More info is available on the Nuts official website: [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts).

## Call for Contribution
```nuts``` has lots of ways to be improved. Please feel free to join the journey.
