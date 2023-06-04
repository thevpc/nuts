---
id: install-cmd
title: Install Command
sidebar_label: Install Command
---

A part from URL and path based executions, an artifact should be installed to be run. Installation can be auto fired when you first execute the artifact (you will be prompted to install the artifact) or manually using the **install** command. Note that when you run directly a jar file as a path or url, the artifact will not be installed!
to install an application just type
```
nuts install <your-artifact-query-here>
```
For example
```
nuts install net.vpc.app:netbeans-launcher#1.2.2
```
you may use any artifact query (see search command section) to install a command.
```
nuts install net.vpc.app:netbeans-*
```
if the artifact is already installed, you should use the force flag (--force)
```
nuts install net.vpc.app:netbeans-launcher#1.2.2
#this second time we have to force install
nuts install -- force net.vpc.app:netbeans-launcher#1.2.2
```
One exception is when you want to switch between multiple versions installed to set the default one, you can omit the --force flag. Actually, when multiple version of the same artifact are installed all of them are executable directly by specifying the right version. When you specify no version, the default one is selected for you. And to make is simple, the default one is the last one you ran an install command for it.

```
me@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.2
me@linux:~> nuts netbeans-launcher
1.2.2
me@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.1
me@linux:~> nuts netbeans-launcher
1.2.1
me@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.2
1.2.2
```
You can find all installed artifacts using 'nuts search --installed' command

## Purpose
The install command is used to install or reinstall packages.

- A+B  : read A main package and B dependencies
- A+B? : ask, if confirmed, read A main package and B dependencies.
- require : deploy package as 'required'
- install : deploy package as 'installed'
- nothing : do nothing

The available strategies are

- require   : install the package and all of its dependencies as required class installed package
- install   : install the package and all of its dependencies as first class installed package
- reinstall : re-install or re-required the package and all of its dependencies
- repair    : repair (re-install or re-required) the given dependency

"required class installed package" can be removed (uninstalled automatically by nuts when none 
of the depending package is nomore installed.


| Status/Strategy -> Status  | REQUIRE                    |INSTALL              |REINSTALL           |REPAIR              |
|--------------              |----------------            |-----------------    |-----------------   |----------------    |
|NOT_INSTALLED               |REQUIRED                    | INSTALLED           |INSTALLED?          | ERROR              |
|INSTALLED                   |INSTALLED REQUIRED          | INSTALLED?          |INSTALLED           | INSTALLED          |
|INSTALLED REQUIRED          |INSTALLED REQUIRED          | INSTALLED REQUIRED  |INSTALLED REQUIRED  | INSTALLED REQUIRED |
|REQUIRED                    |REQUIRED                    | INSTALLED REQUIRED  |REQUIRED            | REQUIRED           |
|INSTALLED OBSOLETE          |INSTALLED REQUIRED OBSOLETE | INSTALLED           |INSTALLED           | INSTALLED          |
|INSTALLED REQUIRED OBSOLETE |INSTALLED REQUIRED          | INSTALLED REQUIRED  |INSTALLED REQUIRED  | INSTALLED REQUIRED |
|REQUIRED OBSOLETE           |REQUIRED OBSOLETE           | INSTALLED REQUIRED  |REQUIRED            | REQUIRED           |


| Status/Strategy -> action  | REQUIRE        |INSTALL           |REINSTALL        |REPAIR           |
|--------------              |----------------|----------------- |-----------------|---------------- |
|NOT_INSTALLED               |require+require | install+require  |install+require? | error           |
|INSTALLED                   |nothing+nothing | install+require? |install+require  | install+nothing |
|INSTALLED REQUIRED          |nothing+nothing | install+require? |install+require  | install+nothing |
|REQUIRED                    |nothing+nothing | install+nothing  |require+require  | require+nothing |
|INSTALLED OBSOLETE          |install+require | install+require  |install+require  | install+nothing |
|INSTALLED REQUIRED OBSOLETE |install+require | install+require  |install+require  | install+nothing |
|REQUIRED OBSOLETE           |require+require | install+require  |require+require  | require+nothing |
