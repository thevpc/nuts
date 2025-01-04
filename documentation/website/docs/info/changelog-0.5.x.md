---
id: changelog050
title: Change Log 0.5.0
sidebar_label: Change Log
order: 50
---

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.5.8.0
WARNING: this version is not deployed to maven-central
- ```2019/09/02 	nuts 0.5.8.0 (*)``` released [download nuts-0.5.8.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.7/nuts-0.5.7.jar) 
- ADDED    : support for Custom Monitor in Copy Command
- ADDED    : support to javaw for windows (exec  command supports --javaw or --win flag)
- ADDED    : support to workspace custom logging (with support for colouring)
- ADDED    : support to userProperties per repository
- ADDED    : NutsString and NutsStringFormat to support 'Nuts Stream Format'
- ADDED    : NutsWarkspaceAware to support initialize/dispose of NutsComponents
- ADDED    : I/O Delete action
- ADDED    : I/O Lock action
- ADDED    : I/O Compress and Uncompress actions
- CHANGE   : now if a command to execute ends with '!', we will force searching in installed only.
- CHANGE   : removed install/uninstall in Terminal, replaced by NutsWorkspaceAware

## nuts 0.5.7.0
WARNING: this version is not deployed to maven-central
- ```2019/07/23 	nuts 0.5.7.0 (*)``` released [download nuts-0.5.7.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.7/nuts-0.5.7.jar) 
- ADDED    : support to Windows (Tested on Win 7) and MacOS(Not Tested) ofr Desktop Integration
- ADDED    : added session and Nuts(Add/Update/Remove)Options where applicable
- ADDED    : Initial support for uri based workspaces
- ADDED    : --dry option to help dry-run commands (test execution without side effects)
- ADDED    : NutsApplication getShared*Folder() method for configuration shared between versions
- ADDED    : flags (in Definition and search) : api,runtime,extension,companion
- CHANGED  : Improved compatibility with Maven
- CHANGED  : Improved Documentation (still to much to go though)
- CHANGED  : Changed NutsCommandLine main api to simplify boot time implementations 
- CHANGED  : Renamed NutsEffectiveUser->NutsUser 
- CHANGED  : Renamed NutsRight->NutsPermission (and all subsequent methods) 
- CHANGED  : NutsExtensionInfo->NutsExtensionInformation
- CHANGED  : NutsHttpConnectionFacade->NutsHttpConnection 
- CHANGED  : Added java.io.Serializable anchor when applicable
- REMOVED  : NutsDefaultRepositoriesProvider,NutsSingletonClassLoaderProvider,NutsDefaultClassLoaderProvider,NutsWorkspaceSPI 
- REMOVED  : NutsRepositoryListener.onInstall(...) 
- REMOVED  : 'alternative' concept, and added NutsClassifierMapping so that classifier can be resolved according to env 

## nuts 0.5.6.0
WARNING: this version is not deployed to maven-central
- ```2019/06/23 	nuts 0.5.6.0``` released [download nuts-0.5.6.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.6/nuts-0.5.6.jar) 
- ADDED    : Implements XDG Base Directory Specification
- ADDED    : Added Json Path support
- ADDED    : Added NutsQuestionParser and NutsQuestionFormat 
- CHANGED  : Extensions are loaded by boot instead of impl so that one can change default impl behavour
- CHANGED  : All repositories are now cache aware.
- CHANGED  : Refactored *Format to extends the very same interface.
- CHANGED  : Using to java.time package instead of older Date class
- CHANGED  : Improved Documentation (still to much to go though)
- CHANGED  : Prefer https repository urls
- FIXED    : Fixed several issues
- REMOVED  : [CommandLine] IMMEDIATE
- REMOVED  : [Options] --term
- REMOVED  : [Extensions] add/remove extensions from extension manager (should use install/uninstall commands)

## nuts 0.5.5.0
WARNING: this version is not deployed to maven-central
- ```2019/06/08 	nuts 0.5.5.0``` released [download nuts-0.5.5.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.5/nuts-0.5.5.jar) 
- REMOVED  : Removed Nsh commands Console Deploy Info Install Fetch Uninstall,Push Update Exec Which
- REMOVED  : Removed maven-github repository type support (web API)
- REMOVED  : Removed nuts-cmd-app project dependency. A built-in NutsApplication is included in the api the help simplify extension.
- ADDED    : Added support for XML,TABLE and TREE (along with JSON, PROPS and PLAIN) printing format to help automate result parsing
- ADDED    : Added Better api in Nuts IO to handle SHA and MD5
- ADDED    : json and xml nsh commands to help manipulating json and xml in commands outputs
- FIXED    : Fixed fprint issue with "" (empty string)
- FIXED    : Fixed Update indexes/stats command
- FIXED    : When installing nuts, lookup latest core implementation
- CHANGED  : Renamed FindCommand to SearchCommand (and some of their methods too)
- CHANGED  : NutsIdFilter.accept accepts workspace as a second argument
- CHANGED  : Improved Help text
- CHANGED  : Improved Documentation (still to much to go through)
- ADDED    : (nsh) Builtin nsh commands basename and dirname 
- CHANGED  : (nsh) Builtin nsh command who renamed to whoami 
- REMOVED  : (nfind) Removed nfind companion (the built-in search command is a better replacement)

## nuts 0.5.4.0 Change Log
WARNING: this version is not deployed to maven-central
- ```2019/04/21 	nuts 0.5.4.0 (*)``` released [download nuts-0.5.4.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.4/nuts-0.5.4.jar) 
-Added lucene indexing facility (thanks to the excellent work of nasreddine bac ali)
- Removed dependencies to common,strings, io and utils (utility classes).
- Removed dependencies to asm (bytecode manipulation).
- From Now on only gson and jansi are retained.
- Layout changes
    - from now on configuration will be version specific. some migration should be done to import previous configs
    - system (global) repo is no more created under the workspace. Only a link to is is registered in nuts-workspace.json
    - added MacOs Layout. Help is needed for testing this !
- Better support for JDK 8+ (New IO,Predicates, Streams, ...)
- Added Comprehensive implementation of Iterator (Stream Like) to better handle result iteration while search is in progress
- Speed improvements
- Added JUnit test battery
- Added support to JSON,PROPS and PLAIN result, implemented in version and info. Should continue implementing in other commands.
- Removed --license, --update, --install, ... options, replaced by workspace "internal" commands new concept.
- Workspaces handle several type of executables that will be resolved in that order : "internal command","aliases : aka workspace command aliases", "components",
  "path/unmanaged components" and system/native commands.
- Several Fixes
    - Fixed Problem with Layout
    - Fixed Problem coloring (fprint embedded library)
    - All System properties now start with "nuts."
    - System properties starting with "nuts.export." are exported to children processes
    - Added watch dog agains infinite child process creation

## nuts 0.5.3.0 Change Log
WARNING: this version is not deployed to maven-central
- ```2019/01/05 	nuts 0.5.3.0``` released [download nuts-0.5.3.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.3/nuts-0.5.3.jar)
- (WINDOWS) First support to Windows platform
    - Support for Console coloring on windows
    - Storing to AppData\\Local and AppData\\Roaming folders
    - ndi is not yet supported!
- (LINUX,UNIX) ndi no more stores to ~/bin but instead it updates .bashrc to point to current workspace added a confirmation question.
- API Change
    - Moved getStoreRoot from NutsWorkspace to NutsWorkspaceConfigManager
    - Added StoreType : CACHE,LIB
    - Introduced NutsDeploymentBuilder,NutsIoManager,NutsParseManager,NutsFormatManager,DescriptorFormat
    - Introduced NutsSessionTerminal,NutsSystemTerminal
    - Added description, alternative (to support multi architecture nuts) descriptor properties
    - Removed descriptor/id 'ext' and 'file' parameters. 'packaging' should be more than enough
    - Removed Maps from config. Replaced by plain arrays
    - Removed workspace.cwd
    - Removed Temp File/Folder support
- Added Archetype "standalone" to help bundling and application with all its dependencies
- Several fixes
    - Fixed Log configuration, introduced --log-inherited to enable inherited log-handlers
    - Fixed support for install/uninstall hooks
    - Fixed Repository Layout where ref repo folder is created twice
    - Fixed Multiple pom download issue
    - Fixed Gson parsing issue
    - Fixed autocomplete support
    - Fixed bad json format recovery
- nsh 
    - introduced pwd,set unset,alias,unalias,autocomplete commands
    - fixed support to autocomplete
- TODO 
    - Code Comments
    - Help files

## nuts 0.5.2.0 Change Log
WARNING: this version is not deployed to maven-central
- ```2018/12/28 	nuts 0.5.2.0``` released [download nuts-0.5.2.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.2/nuts-0.5.2.jar) 
- Global refactoring
    - Introduced NutsCommandExecBuilder, NutsDependencyBuilder, NutsDeploymentBuilder, NutsIdBuilder, NutsClassLoaderBuilder
- Extracted nsh commands as regular nuts package (nadmin, nfind)
    WORKING-ON : Fixing "mvn" start from nuts (handling, exclude, pom import and classifiers from maven)

## nuts 0.5.1.0 Change Log
WARNING: this version is not deployed to maven-central
- ```2018/12/18 	nuts 0.5.1.0 released``` [download nuts-0.5.1.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.1/nuts-0.5.1.jar) 
- FIXED : Fixed problem with inheritIO from child process (added InputStreamTransparentAdapter and OutputStreamTransparentAdapter interfaces)
- FIXED : Added distinction  between workspace config and runtime boot api/runtime values
- FIXED : Do not read workspace version and dependency config from child process (because it may require distinct version of nuts)
- FIXED : Mkdir,cp, etc... used incorrectly cwd. Fixed.
- CHANGED : Optimized pom.xml parse execution time (using DOM instead of SAX)
- CHANGED : moved cache from bootstrap folder to default-workspace/cache

## nuts 0.5.0.0 Change Log
WARNING: this version is not deployed to maven-central
- ```2018/11/25 	nuts 0.5.0.0 released``` [download nuts-0.5.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.0/nuts-0.5.0.jar) 
- Very first published version. older ones were used internally for internal projects only.
