---
id: changelog
title: Change Log
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.3.0 (DEVELOPMENT VERSION)
- ```2021/XX/XX 	nuts 0.8.3.0 (*)``` not released yet [download nuts-0.8.3.jar](http://thevpc.net/nuts.jar)
- WARNING : {api}  API has evolved with incompatibilities with previous versions
- ADDED   : {api}  added NutsIOCopyAction.setSource(byte[]) and NutsIOHashAction.setSource(byte[]) 
- ADDED   : {api}  removed NutsId.compatFilter and NutsVersion.compatFilter and replaced by compatNewer/compatOlder
- ADDED   : {api}  replaced string messages with NutsMessage in NutsLogger
- ADDED   : {api}  removed 'NutsInput' and 'NutsOutput'
- ADDED   : {api}  removed 'NutsCommandlineFamily' and replaced by 'NutsShellFamily'
- ADDED   : {api}  added 'NutsBootTerminal' to help nuts bootstrap using custom stdin/out end err  
- CHANGED : {api}  added 'NutsHomeLocation' to replace compound key NutsOSFamily and NutsStoreLocation  
- ADDED   : {api}  added 'NutsPath.isDirectory' and 'NutsPath.isRegularFile' 
- CHANGED : {api}  removed commandline options '-C' and '--no-color', you can use '--!color' instead
- CHANGED : {api}  removed commandline options '--no-switch' and '--no-progress', you can use '--!switch' and '--!progress' instead
- CHANGED : {api}  NutsResultList renamed to NutsStream and revamped with handy stream features and added ws.util.streamOf(...)
- CHANGED : {api}  ws.io.expandPath replaced by NutsPath.builder.setExpanded(true)
- REMOVED : {api}  removed deprecated ClassifierMapping
- REMOVED : {api}  removed NutsTokenFilter (little to no interest)
- REMOVED : {api}  removed deprecated feature inheritedLog
- ADDED   : {api}  NutsVal, a simple wrapper for strings and objects with helpful converters used in args, env, options and properties.
- CHANGED : {api}  changed descriptor to add maven profiles support, mainly added platform for dependency and added os/platform etc to property
- ADDED   : {api}  added NutsShellFamily to support bash, csh, and other shell families
- ADDED   : {pom } add Manifest Entry 'Automatic-Module-Name' in all projects to support j9+ module technology
- FIXED   : {impl} NutsFormat now creates any missing parent folder when calling print(Path/File) or println(Path/File)


## nuts 0.8.2.0 (PUBLISHED VERSION)
- ```2021/09/04 	nuts 0.8.2.0 (*)``` released [download nuts-0.8.2.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.2/nuts-0.8.2.jar)
- WARNING: API has evolved with multiple incompatibilities with previous versions  
- FIXED: Fixed problem that requires reinstalling nuts each time we have a new version
- FIXED: Fixed some Documentation issues (still fixing)
- 

## nuts 0.8.1.0 (PUBLISHED VERSION)
- ```2021/08/24 	nuts 0.8.1.0 (*)``` released [download nuts-0.8.1.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.1/nuts-0.8.1.jar)
- WARNING: API has evolved with multiple incompatibilities with previous versions
- ADDED: {api} added static methods of() in interfaces to simplify instantiation
- ADDED: {api} parseLenient to all NutsEnum classes
- CHANGED: {nadmin} removed nadmin and merged into runtime (tight coupling!!)
- REMOVED: {api}   removed session.formatObject() as the session is now propagated silently
- CHANGED: {api}   removed NutsApplicationLifeCycle and replaced with NutsApplication (an interface instead of a class)
- ADDED  : {api}   added support for parsing pom.xml (MAVEN) along with *.nuts (nuts descriptors)
- ADDED  : {api}   added io killProcess support
- CHANGED: {api}   added path API, implemented via nlib-ssh to add ssh support for paths
- CHANGED: {all}   remove dependencies, runtime has no dependencies, and others have the bare minimum
- CHANGED: {api}   session is from now on mandatory to perform any operation. A simple way to make it simple to use is to get a "session aware" workspace with session.getWorkspace()
- ADDED  : {api}  added support for Yaml with minimal implementation
- ADDED  : {api}  element now supports complex keys in Map Entries (Objects)
- ADDED  : {api}{cmdline} added support for History and implemented in JLine extension
- ADDED  : {api}{cmdline} added support for readline syntax coloring (using jline)
- ADDED  : {api}{cmdline} added --locale option to support multi languages. The option is reflected to Session as well 
- ADDED  : {api}{cmdline} added ---key=value options to support extra properties 
- ADDED  : {api}{cmdline} added -S short option, equivalent to --standalone 
- ADDED  : {api}{cmdline} added NutsFormattedMessage to support formatted messages in a uniform manner (C-style, {} positional) 
- CHANGED: {api}{cmdline} both list and tree dependencies are now accessible as NutsDependencies  
- ADDED  : {runtime} added support to community maven repositories : jcenter, jboss, spring, clojars, atlassian, atlassian-snapshot, google, oracle
  to use the repository you can add it as a permanent repository or temporary. here are some examples:
  - nuts nadmin add repository jcenter // add permanently the repository
  - nuts -r jcenter my-command // use temporarily the repository top run my-command 
- FIXED  : {runtime} extension support (for JLine)
- ADDED  : {runtime} added minimal implementation for YAM
- ADDED  : {runtime} added fast implementation for JSON and removed gson dependency
- CHANGED: {runtime} revamped Nuts Text Format to support simplified syntax but more verbose styles.
  Now supports #), ##), ###) and so on as Title Nodes.
  It supports as well the common markdown 'code' format with anti-quotes such as
  ```java code goes here...```
  Other supported examples are:
  ```sh some command...```
  ```error error message...```
  ```kw someKeyword```
- CHANGED: {runtime} help files now have extensions ".ntf" (for nuts text format) instead of ".help"
- ADDED  : {njob} added --help sub-command
- FIXED  : {nsh}  fixed multiple inconsistencies and implemented a brand new parser
- REMOVED: {docusaurus-to-ascidoctor} tool fully removed as replaced by a more mature ndocusaurus
- REMOVED: {ndi}, removed project, merged into nadmin
- REMOVED: {nded}, removed project, temporarily code added to nadmin, needs to be refactored
- ADDED  : {ntalk-agent} new modules nlib-talk-agent (library) and ntalk-agent (application using the library) that enable client to client communication.
  nlib-talk-agent is a broker that helps communication between nuts components with minimum overhead.
  nlib-talk-agent enables one workspace to talk with any other workspace without having to create one server socket for each workspace.
  It also enables singleton per location implementation

## nuts 0.8.0.0 (PUBLISHED VERSION)
- ```2020/11/8? 	nuts 0.8.0.0 (*)``` released [download nuts-0.8.0.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.0/nuts-0.8.0.jar)
- WARNING: this is the first version to be deployed to maven central. previous versions will no longer be supported
- WARNING: this is a **major version**, API has evolved with multiple incompatibilities with previous versions  
- WARNING: The OSS License has changed from GPL3 to the more permessive Apache Licence v2.0
- CHANGED: changed packages from net.vpc to net.thevpc (required for central to be aligned with website)
- CHANGED: removed support for vpc-public-maven and vpc-public-nuts
- CHANGED: ```nuts -Z``` will update ```.bashrc``` file and switch back to default workspace
- ADDED  : when a dependency is missing it will be shown in the error message
- ADDED  : nuts commandline argument --N (--expire) to force reloading invoked artifacts (expire fetched jars). a related NutsSession.expireTime is introduced to force reinstall of any launched application and it dependencies, example: ```nuts -N ndi```
- ADDED  : install --strategy=install|reinstall|require|repair introduced to select install strategy (or sub command)
- ADDED  : NutsInput & NutsOutput to help considering reusable sources/targets
- ADDED  : nuts commandline argument --skip-errors  to ignore unsupported commandline args
- ADDED  : new toolbox njob, to track service jobs (how many hours you are working on each service project)
- ADDED  : new next-term, to support jline console extension into nuts
- ADDED  : workspace.str() to create NutsStringBuilder
- ADDED  : 'switch' command in ndi to support switching from one workspace to another. example : ```ndi switch -w other-workspace -a 0.8.0```

## nuts 0.7.2.0
WARNING: this version is not deployed to maven-central
- ```2020/09/23 	nuts 0.7.2.0 (*)``` released [download nuts-0.7.2.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.2/nuts-0.7.2.jar) 
- FIXED  : execute non installed artifacts sometimes do not ask for confirmation
- ADDED  : NutsCommandLineProcessor.prepare/exec/autoComplete
- ADDED  : NutsApplicationContext.processCommandLine(cmdLine)
- ADDED  : NutsApplicationContext.configureLast(cmdLine)
- RENAMED: feenoo renamed to ncode
- ADDED  : Docusaurus Website
- ADDED  : new toolbox ndocusaurus : Docusaurus Website templating

## nuts 0.7.1.0
WARNING: this version is not deployed to maven-central
- ```2020/09/14 	nuts 0.7.1.0 (*)``` released [download nuts-0.7.1.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.1/nuts-0.7.1.jar)
- FIXED  : reset stdout line when calling external processes
- FIXED  : fixed several display issues.

## nuts 0.7.0.0
WARNING: this version is not deployed to maven-central
- ```2020/07/26 	nuts 0.7.0.0 (*)``` released [download nuts-0.7.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.0/nuts-0.7.0.jar)
- ADDED  : NutsApplicationContext.processCommandLine(c)
- ADDED  : NutsWorkspaceCommand.copySession()
- RENAMED: derby renamed to nderby
- RENAMED: mysql renamed to nmysql
- RENAMED: tomcat renamed to ntomcat
- RENAMED: mvn renamed to nmvn

## nuts 0.6.0.0
WARNING: this version is not deployed to maven-central
- ```2020/01/15 	nuts 0.6.0.0 (*)``` released [download nuts-0.6.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.6.0/nuts-0.6.0.jar) 
- CHANGED  : config file format changed
- CHANGED  : now installed packages are stored in 'installed' meta repository
- CHANGED  : alias files have extension changed form *.njc to *.cmd-alias.json
- CHANGED  : now nuts looks for system env variable NUTS_WORKSPACE for default workspace location
- CHANGED  : api and runtime are installed by default
- CHANGED  : now distinguishes between installed primary and installed dependencies packages.
- ADDED    : support for ROOT_CMD execution (SYSCALL was renamed USER_CMD)
- ADDED    : support for Interrupting Copy
- ADDED    : support to ps (list processes)
- ADDED    : support progress options
- CHANGED  : worky, searches now for modified deployments with same version but different content
- FIXED    : encoding problem with json/xml
- REMOVED  : NutsRepositorySession

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
- CHANGE   : removed install/uninstall in Terminal, replaced by NutsWarkspaceAware

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
