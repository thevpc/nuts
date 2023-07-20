---
id: faq
title: Frequently Asked Questions
sidebar_label: Frequently Asked Questions
order: 3
---


## Why not maven?
or, "Why do we need a package manager for Java. Isn't **Maven** enough?".

Please read [Nuts Introduction, Why and What for](../intro/nuts-and-maven.md).
In few words maven manages dependencies to build applications, nuts uses maven dependencies system to install applications.

## What does Nuts mean and why ?
**```nuts```** stands for "Network Updatable Things Services". It helps managing things (artifacts of any type, not only java).
The Name also helps depicting another idea : **```nuts```** is a good companion and complement to Maven tool. 
The word maven (MAY-vin), from Yiddish, means a super-enthusiastic expert/fan/connoisseur/Wizard.
And where wizards are, fools and **```nuts```** must be. 
**```nuts```** is the foolish tool to support the deployment and not the build. 
Hence the name.


## Does nuts support only jar packaging
Not only. **```nuts```** supports all types of packaging, particularly, those supported by maven. 
This includes  pom , jar , maven-plugin , ejb , war , ear , rar.
**```nuts```** is also intended to support any "thing" including "exe" ,"dll", "so", "zip" files, etc.
**```nuts```** differs from maven as it defines other properties to the artifact descriptor (aka pom in maven) : os (operating system), arch (hardware architecture), osdist (relevant for linux for instance : opensuse, ubuntu) and platform (relevant to vm platforms like java vm, dotnet clr, etc).
Such properties are queried to download the most appropriate binaries for the the current environment.


## Can I contribute to the project
I hoped you would ask this question. Of course. 
You can drop me an email (see my github profile email) to add you as contributor or fork the repository and ping a pull request. 
You can also open a new issue for feature implementation to invite any other contributor to implement that feature (or even implement it your self).

## Where can I find Documentation about the Project
Mainly all of the documentation can be found in 2 places:

* this website: it includes both user documentation and javadocs (code documentation)
* each command help option. when you type 

```sh 
  nuts --help
  ``` 
 
or 

  ```sh 
  nsh --help
  ``` 

  you will get more details on nuts or on the tool (here nsh)

## How can I make my application "Nuts aware"
If by **```nuts```** aware you mean that you would download your application and run it using **```nuts```**, then you just need to create the application using maven and deploy your application to the public maven central.
Nothing really special is to be done from your side. You do not have to use plugins like 'maven-assembly-plugin' and 'maven-shade-plugin' to include your dependencies.
Or, you can also use NAF (**```nuts```** Application Framework) to make your application full featured "Nuts aware" application.

## Why Nuts Application Framework (NAF)
or, "Why should I consider implementing my terminal application using Nuts Application Framework (NAF)?"
First of all, NAF is a simple 300k jar so for what it provided to you, you would be surprised. 
Indeed, implementing your application using NAF will provide you a clean way to :

* seamless integration with **```nuts```** and all other NAF applications (obviously!)

* support standard file system layout (XDG) where config files and log files are not necessarily in the same folder see [Nuts File System](../concepts/filesystem.md) for more details.

* support application life cycle events (onInstall, onUninstall, onUpdate), 

* standard support of command line arguments

* dynamic dependency aware class loading

* terminal coloring, and terminal components (progress bar, etc...)

* json, yaml, xml, table, tree and plain format support out of the box as output to all your commands

* pipe manipulation when calling sub processes

* advanced io features (persistence Locks, monitored downloads, compression, file hashing....)

* standard ways to support and use installed platforms (installed JRE, JDK, ...)

* and lots more...


## Can I use NAF for non terminal applications, Swing or JavaFX perhaps
Sure, you will be able to benefit of all the items in the preceding question but terminal coloring wont be relevant of course. 
Check netbeans-launcher in github. It's a good example of how interesting is to use NAF in non terminal applications. 


## What is the License used in Nuts
**```nuts```** is published under Licensed under the Apache License, Version 2.0. 
 

