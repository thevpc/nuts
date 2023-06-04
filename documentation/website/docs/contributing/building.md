---
id: building
title: Building
sidebar_label: Building Nuts Projects
---


To build`nuts` Package Management you need the following software installed on your machine:
* java JDK 8 (`nuts` is still compatible with java 8)
* maven 3.8+
* You favorite IDE (I'm using Netbeans and sometimes IntellijIdea and very sporadically Eclipse)

## Compiling Nuts
Here is the typical commands to get your own local copy of `nuts` sources and to compile them.

First of all, get your local copy of `nuts` source code from `github` 

```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
```

Then, you need to invoke `mvn install` to compile all of the project:

```bash
mvn clean install
```

That being done, `nuts` will be compiled and installed to your local maven repository.


## Building Documentation and preparing development scripts

The next thing we need to worry about is the building of nuts community website and to have a working development version of `nuts` you can rely on in your tests manipulations.

To do so we will need to install locally ```nuts``` and ```nsh```.

Assuming you are always under `nuts` repo root folder, issue the following command (its a bash command, so you need to be on linux or MacOS for it to work)

```bash
./nuts-build-release
```

You can now play with your development version of nuts using the generated `nuts-dev` script.
You may want to update the following line to match your java 8 JDK install location or simply add it to your shell `rcfile`.

```sh
NUTS_JAVA_HOME=/usr/lib64/jvm/java-1.8.0-openjdk-1.8.0
```

Indeed, you must compile `nuts` with java 8 because nuts needs to be working on Java 8 and all later java versions (
this means that compiling on more recent versions of java should pass successfully as well). So you must
not use deprecated features (in java9+) in nuts source code (examples : js nashorn, rmi activation, etc...)


## Running, testing and working with nuts-dev, in development environment

Here are some tips when working on nuts project or even working on an application that builds on `nuts` using NAF (aka Nuts Application Framework) for example:

* ```nuts-dev``` is the script you are most of the time using when developing `nuts` project.

* ```nuts-dev``` script uses a special workspace called ```development```, so it does not interfere with your local `nuts` installation.

* you can always change the workspace in ```nuts-dev``` using ```-w``` option
```sh 
./nuts-dev -w test
```


* You can run nuts in debug mode with `--debug` that shall be the very **FIRST** option. The following example will spawn a jvm listening on the 5005/tcp port you can attach to your favorite IDE.

```sh 
./nuts-dev --debug
```

* Always make sure you are working on a clean workspace, to have a reproducible environment,
```sh 
./nuts-dev -Zy
```

* If you are willing to run directly from your IDE, make sure you add `-w=development` or `-w=test` to
  the program command line arguments as an example to work on a separate workspace than the one used in production or locally

* If you want to debug an application running under `nuts`, you can just debug `nuts` project using the embedded flag
  ( `-b` or `--embedded`) to run that application in the same virtual machine
```sh 
nuts -w test -b my-app
```

* If you want to debug an application running under `nuts` in a separate virtual machine, use the option `--debug` option to run the application in debug mode listening to the 5005 tcp port, then attach it to your IDE. This will debug  `nuts` itself but will make possible running spawn jvm as well, the effective debugging port will be randomly selected and  displayed on your stdout. You will need to attach another jvm to your IDE using that port.

```sh 
nuts -w test --debug my-app
# or
nuts -w test --debug=5010 my-app
```

* When you need to have more information about what `nuts` is doing under the hood, just run it in verbose mode

```sh 
./nuts-dev --verbose install some-application
```

* You may want to disable creation of shortcuts and desktop icons in development mode:
```sh 
./nuts-dev --!init-launchers
```


* You may want to disable all repositories and use solely your local maven repo:

```sh 
./nuts-dev -r=maven-local
```
