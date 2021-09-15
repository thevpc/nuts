---
id: install-cmd
title: Install Command
sidebar_label: Install Command
---
${{include($"${resources}/header.md")}}

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
