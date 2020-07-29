# Nuts FAQ

## Why do we need a package manager for Java. Isn't **Maven** enough?
please read [Nuts Introduction, Why and What for](01-nuts-introduction.md)


## What does Nuts mean and why ?
Nuts stands for "Network Updatable Things Services". It helps managing things (artifacts of any type, not only java).
The Name also helps depicting another idea : Nuts is a good companion and complement to Maven tool. The word maven (MAY-vin), from Yiddish, means a super-enthusiastic expert/fan/connoisseur/Wizard.
And where wizards are, fools and nuts must be. Nuts is the other around, it's a foolish, tool to support the deployment and not the build. 
Hence the name.


## Does nuts support only jar packaging
Not only. Nuts supports all packagings supported by maven. This includes  pom , jar , maven-plugin , ejb , war , ear , rar.
However **Nuts** is also intended to support any "thing" including "exe" ,"dll", "so", "zip" files, etc.
Nuts differs from maven as it defines other properties to the artifact descriptor (aka pom in maven) : os (operating system), 
arch (hardware architecture), osdist (relevant for linux for instance : opensuse, ubuntu) and platform (relevant to vm platforms like java vm, dotnet clr, etc).
Such properties are queried to download the most appropriate binaries the the current characteristics.


## Can I contribute to the project
I hoped you would ask this question. Sure. please fork the repository and ping a pull request. If you also open a new issue for feature implementation to invite any other contributor to implement that feature (or even implement it your self).

## Where can I find Documentation about the Project
The doc folder is intended to include all documentation. May be we will handle it differently later.

## How can i make my application Nuts aware
If by nuts aware you mean that you would download your application and run it using nuts, then you just need to create the application using maven and deploy your application the public maven central.
Nothing really special is to be done from your side. You do not have to use plugins like 'maven-assembly-plugin' and 'maven-shade-plugin' to include your dependencies.

## Why should I consider implementing my terminal application using Nuts Application Framework
First NAF is simple a 300k jar so for what it provided to you, you would be surprised. 
Indeed, implementing your application using NAF will provide you a clean way to :
* seamless integration with nuts and all other NAF applications

* support standard file system layout (XDG) where config files and log files are not necessarily in the same folder see [Nuts File System](03-nuts-filesystem.md) for more details.

* support application life cycle events (onInstall, onUninstall, onUpgrade), 

* standard support of command line arguments

* dynamic dependency aware class loading

* terminal coloring and components (progress bar, etc...)

* json,xml,table,tree and plain format support out of the box

* pipe manipulation when calling sub processes

* advanced io features (persistence Locks, monitored downloads, compression, file hashing....)


## Can I use NAF ofr non terminal applications, Swing per JavaFX perhaps
Sure, you will be able to benefit of all the items in the preceding question but terminal coloring. 
Check netbeans-launcher in github. Its a good example that shows how interesting is to use NAF in non terminal application. 


## What is the License used in Nuts
Nuts is published under GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later.

