---
id: changelog081
title: Change Log 0.8.1
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.1.0 (PUBLISHED VERSION)
- ```2021/08/24 	nuts 0.8.1.0 (*)``` released [download nuts-0.8.1.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.1/nuts-0.8.1.jar)
- WARNING: API has evolved with multiple incompatibilities with previous versions
- ADDED: ```api``` added static methods of() in interfaces to simplify instantiation
- ADDED: ```api``` parseLenient to all NutsEnum classes
- CHANGED: ```nadmin``` removed nadmin and merged into runtime (tight coupling!!)
- REMOVED: ```api```   removed session.formatObject() as the session is now propagated silently
- CHANGED: ```api```   removed NutsApplicationLifeCycle and replaced with NutsApplication (an interface instead of a class)
- ADDED  : ```api```   added support for parsing pom.xml (MAVEN) along with *.nuts (nuts descriptors)
- ADDED  : ```api```   added io killProcess support
- CHANGED: ```api```   added path API, implemented via nlib-ssh to add ssh support for paths
- CHANGED: ```all```   remove dependencies, runtime has no dependencies, and others have the bare minimum
- CHANGED: ```api```   session is from now on mandatory to perform any operation. A simple way to make it simple to use is to get a "session aware" workspace with session.getWorkspace()
- ADDED  : ```api```  added support for Yaml with minimal implementation
- ADDED  : ```api```  element now supports complex keys in Map Entries (Objects)
- ADDED  : ```api``` ```cmdline``` added support for History and implemented in JLine extension
- ADDED  : ```api``` ```cmdline``` added support for readline syntax coloring (using jline)
- ADDED  : ```api``` ```cmdline``` added --locale option to support multi languages. The option is reflected to Session as well
- ADDED  : ```api``` ```cmdline``` added ---key=value options to support extra properties
- ADDED  : ```api``` ```cmdline``` added -S short option, equivalent to --standalone
- ADDED  : ```api``` ```cmdline``` added NutsFormattedMessage to support formatted messages in a uniform manner (C-style, positional)
- CHANGED: ```api``` ```cmdline``` both list and tree dependencies are now accessible as NutsDependencies
- ADDED  : ```runtime``` added support to community maven repositories : jcenter, jboss, spring, clojars, atlassian, atlassian-snapshot, google, oracle
  to use the repository you can add it as a permanent repository or temporary. here are some examples:
    - nuts nadmin add repository jcenter // add permanently the repository
    - nuts -r jcenter my-command // use temporarily the repository top run my-command
- FIXED  : ```runtime``` extension support (for JLine)
- ADDED  : ```runtime``` added minimal implementation for YAM
- ADDED  : ```runtime``` added fast implementation for JSON and removed gson dependency
- CHANGED: ```runtime``` revamped Nuts Text Format to support simplified syntax but more verbose styles.
  Now supports #), ##), ###) and so on as Title Nodes.
  It supports as well the common markdown 'code' format with anti-quotes such as
  ```java code goes here...```
  Other supported examples are:
  ```sh some command...```
  ```error error message...```
  ```kw someKeyword```
- CHANGED: ```runtime``` help files now have extensions ".ntf" (for nuts text format) instead of ".help"
- ADDED  : ```njob``` added --help sub-command
- FIXED  : ```nsh```  fixed multiple inconsistencies and implemented a brand new parser
- REMOVED: ```docusaurus-to-ascidoctor``` tool fully removed as replaced by a more mature ndocusaurus
- REMOVED: ```ndi```, removed project, merged into nadmin
- REMOVED: ```nded```, removed project, temporarily code added to nadmin, needs to be refactored
- ADDED  : ```ntalk-agent``` new modules nlib-talk-agent (library) and ntalk-agent (application using the library) that enable client to client communication.
  nlib-talk-agent is a broker that helps communication between nuts components with minimum overhead.
  nlib-talk-agent enables one workspace to talk with any other workspace without having to create one server socket for each workspace.
  It also enables singleton per location implementation
