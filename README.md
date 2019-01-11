# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.5.2.0
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
packages, installing these  packages to the current machine and executing such  packages on need.
Each managed package  is also called a ==nuts== which  is a **Network Updatable Thing Service** .
Nuts packages are  stored  into repositories. A  *repository*  may be local for  storing local Nuts
or remote for accessing  remote packages (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote packages are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed ==nuts==  (packages)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  ==nuts==  (tool) to
resolve and download on-need dependencies over the wire.

==nuts== is a swiss army knife tool as it acts like (and supports) *maven* build tool to have an abstract
view of the the  packages dependency, like  *zypper/apt-get*  package manager tools  to  install and
uninstall packages allowing multiple versions of the very same package to  be installed.

## COMMON VERBS:
+ deploy,undeploy   : to handle packages (package installers) on the local repositories
+ install,uninstall : to install/uninstall a package (using its fetched/deployed installer)
+ fetch,push        : download, upload to remote repositories
+ find              : searches for existing/installable packages

## Download Latest stable version
+ Java or any Java enabled OS : Linux,Windows,iOS, ... :: [nuts-0.5.3.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.3/nuts-0.5.3.jar)

## Requirements
Java Runtime Environment (JRE) or Java Development Kit (JDK) version 8 or later

## Installation
java -jar nuts-0.5.3.jar

## Launching
+ [Linux] nuts ...your command here...
    + Example 1 : nuts --version
    + Example 2 : nuts --install derby
    + Example 3 : nuts --install derby
+ [Windows,iOS] java -jar nuts-0.5.2.jar ...your command here...

## Latest News

+ 2019/01/05 	nuts 0.5.3.0 released [nuts-0.5.3.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.3/nuts-0.5.3.jar)
+ 2018/12/28 	nuts 0.5.2.0 released [nuts-0.5.2.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.2/nuts-0.5.2.jar)
+ 2018/12/18 	nuts 0.5.1.0 released [nuts-0.5.1.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.1/nuts-0.5.1.jar)
+ 2018/11/25 	nuts 0.5.0.0 released [nuts-0.5.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.0/nuts-0.5.0.jar)

## Getting started

 You may consider browsing the Nuts official [wiki](https://github.com/thevpc/nuts/wiki) .


## Change Log
### nuts 0.5.3.0
    1- First support to Windows platform
        * Support for Console coloring on windows
        * Storing to AppData\\Local and AppData\\Roaming folders
        * [[DOING]] jline works poorly...
    2- API Change
        * Moved getStoreRoot from NutsWorkspace to NutsWorkspaceConfigManager
        * Added StoreType : CACHE
        * Introduced NutsDeploymentBuilder
        * Introduced NutsIoManager,NutsParseManager,NutsFormatManager
    3- Fixed Log configuration, introduced --log-inherited to enable inherited log-handlers
    4- Fixed support for install/uninstall hooks
    5- Added "description" section to pom files
    6- Fixed Archetype support
    7- Added Archetype "standalone" to help bundling and application with all its dependencies
    8- Fixed Repository Layout where ref repo folder is created twice
    9- Fixed Multiple pom download issue
    10- Fixed Gson parsing issue
    11- Added "Lib" layout store folder for both bootstrap jars and repo jars
    12- Added alternative descriptor property to support multi architecture nuts
    13- Added DescriptorFormat interface

### nuts 0.5.2.0
    1- Global refactoring
        * Introduced NutsCommandExecBuilder
        * Introduced NutsDependencyBuilder
        * Introduced NutsDeploymentBuilder
        * Introduced NutsIdBuilder
        * Introduced NutsClassLoaderBuilder
    2- Extracted nsh commands as regular nuts package (nadmin, nfind)
