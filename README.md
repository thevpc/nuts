# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.5.3.0
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
packages, installing these  packages to the current machine and executing such  packages on need.
Each managed package  is also called a **nuts** which  is a **Network Updatable Thing Service** .
Nuts packages are  stored  into repositories. A  *repository*  may be local for  storing local Nuts
or remote for accessing  remote packages (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote packages are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed **nuts**  (packages)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to
resolve and download on-need dependencies over the wire.

**nuts** is a swiss army knife tool as it acts like (and supports) *maven* build tool to have an abstract
view of the the  packages dependency and like  *npm*, **pip** or *zypper/apt-get*  package manager tools  
to  install and uninstall packages allowing multiple versions of the very same package to  be installed.

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
### nuts 0.5.4.0 (IN PROGRESS)
    - Added lucene indexing facility (thanks to the excellent work of nasreddine bac ali)
    - Removed dependencies to common,strings, io and utils (utility classes).
    - Removed dependencies to asm (bytecode manipulation).
    - From Now on only gson and jansi are retained.
    - Layout changes
        * from now on configuration will be version specific. some migration should be done to import previous configs
        * system (global) repo is no more created under the workspace. Only a link to is is registered in nuts-workspace.json
        * added MacOs Layout. Help is needed for testing this !
    - Better support for JDK 8+ (New IO,Predicates, Streams, ...)
    - Migrated from File to New I/O Path
    - Added Stream Support ("findStream" in NutsQuery)
    - Added Comprehensive implementation of Iterator (Stream Like) to better handle result iteration while search is in progress
    - Introduced NutsSearchIdFilter to speedup search time
    - Added JUnit test battery
    - Added support to JSON,PROPS and PLAIN result, implemented in version and info. Should continue implementing in other commands.
    - Removed --license, --update, --instal, ... options, replaced by workspace "internal" commands new concept.
    - Workspaces handle several type of executables that will be resolved in that order : "internal command","aliases : aka workspace commands", "components",
      "path/unmanaged components" and system/native commands.
    - Several Fixes
        * Fixed Problem with Layout
        * Fixed Problem coloring (fprint embedded library)
        * All System properties now start with "nuts."
        * System properties starting with "nuts.export." are exported to children processes
        * Added watch dog agains infinite child process creation
    - TODO
        * Add maven-github repository type support (web API)
        * FIX : executable and appExecutable are not well supported in nfind!

### nuts 0.5.3.0
    1- (WINDOWS) First support to Windows platform
        * Support for Console coloring on windows
        * Storing to AppData\\Local and AppData\\Roaming folders
        * ndi is not yet supported!
    2- (LINUX,UNIX) ndi no more stores to ~/bin but instead it updates .bashrc to point to current workspace
        added a confirmation question.
    3- API Change
        * Moved getStoreRoot from NutsWorkspace to NutsWorkspaceConfigManager
        * Added StoreType : CACHE,LIB
        * Introduced NutsDeploymentBuilder,NutsIoManager,NutsParseManager,NutsFormatManager,DescriptorFormat
        * Introduced NutsSessionTerminal,NutsSystemTerminal
        * Added description, alternative (to support multi architecture nuts) descriptor properties
        * Removed descriptor/id 'ext' and 'file' parameters. 'packaging' should be more than enough
        * Removed Maps from config. Replaced by plain arrays
        * Removed workspace.cwd
        * Removed Temp File/Folder support
    4- Added Archetype "standalone" to help bundling and application with all its dependencies
    5- Several fixes
        * Fixed Log configuration, introduced --log-inherited to enable inherited log-handlers
        * Fixed support for install/uninstall hooks
        * Fixed Repository Layout where ref repo folder is created twice
        * Fixed Multiple pom download issue
        * Fixed Gson parsing issue
        * Fixed autocomplete support
        * Fixed bad json format recovery
    6- nsh 
        * introduced pwd,set unset,alias,unalias,autocomplete commands
        * fixed support to autocomplete
    7- TODO 
        * Code Comments
        * Help files

### nuts 0.5.2.0
    1- Global refactoring
        * Introduced NutsCommandExecBuilder, NutsDependencyBuilder, NutsDeploymentBuilder, 
            NutsIdBuilder, NutsClassLoaderBuilder
    2- Extracted nsh commands as regular nuts package (nadmin, nfind)
