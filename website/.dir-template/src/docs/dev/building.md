---
id: building
title: Building
sidebar_label: Building Nuts Projects
---

${include($"${resources}/header.md")}

To build **nuts**  from sources you must
* compile main project using maven
* run command nsh script (that uses nuts) to build the website

## Compiling with maven

In the root directory issue the following command
```sh
    mvn install
```

That being done, nuts will be compiled and installed into your local maven repository

## Building website

The next thing we need to worry about is the building of nuts community website.
To do so we will need to install locally ```nuts``` and ```nsh``` and run a
In the root directory issue the following command

```sh
    JAR="~/.m2/repository/net/thevpc/nuts/nuts/0.8.1/nuts-0.8.1.jar"
    java -jar $JAR build-website.nsh
    mvn install
```
