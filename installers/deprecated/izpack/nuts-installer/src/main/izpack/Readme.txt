Network Updatable Things Services

     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   dev version 0.8.3.0 (accessible on thevpc.net)
\_\ \/\__,_/\__/____/    production version 0.8.3.0 (accessible on maven central)


website : https://thevpc.github.io/nuts

nuts is a Java™ Package Manager that helps discovering, downloading, assembling and executing local and remote artifacts (packages) in a very handy way.

Unlike maven which resolves dependencies at compile time, nuts solves dependencies and builds the classpath at install time and, as a result, saves disk and bandwidth by downloading and caching only libraries required for the current environment and share them between multiple installed applications.

nuts is unique in that it reuses maven and other build tool descriptor formats to solve dependency graph, and does not, whatsoever, have any requirement on existing maven created packages.

nuts is the ultimate solution to get rid of the ugly lib jars, fat-jars, uber-jars and one-jars used for deploying java applications.
