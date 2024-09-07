---
id: changelog
title: Change Log
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.3.1 (PUBLISHED VERSION)
- ```2022/02/01 	nuts 0.8.3.1``` released nuts-runtime-0.8.3.1.jar
- UPDATED   : move support to repositories "nuts-public" and "nuts-preview"
- FIXED   : Updated README 
- FIXED   : ```runtime``` Fixed problem with dependency resolution whit maven's "import" scope
- ROLLBACK : Rolled back test on maven-local
- FIXED : Fixed nuts api without changing the version and without breaking the API, changes will be promoted to next version later 
- UPDATED : Updated NEXT API CHANGES
- UPDATED : Updated .gitignore
- FIXED : Fixed Help files
- FIXED : Changed $* by $@
- FIXED : Fixed NAF support in ncode
- UPDATED : Updated TEST
- FIXED : Fixed display of URL paths
- PERF : Optimize Maven Dependency Resolver
- FIXED : Fix reinstall so that it calls uninstaller component
- FIXED : Fix recommendation connector API
- FIXED : Fix Class Name resolution when using CGLIB
- FIXED : Call clearLine before Progress
- FIXED : Call resetLine before Log

## nuts 0.8.3.0 (PUBLISHED VERSION)
- ```2021/01/05 	nuts 0.8.3.0 (*)``` released [download nuts-0.8.3.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar)
- WARNING : ```api```  API has evolved with incompatibilities with previous versions
- ADDED   : ```runtime```  now search --dry displays the search query plan
- ADDED   : ```api```  added command "settings install-log" to display installation logs
- ADDED   : ```api```  added NutsExpr to help parsing simple expressions (used or will be used in almost all commands)
- CHANGED : ```api```  Simplified API
- ADDED   : ```api```  added NutsDescriptorFlag to match multiple descriptor info such as app, executable etc.
- ADDED   : ```api```  added NutsIOCopyAction.setSource(byte[]) and NutsIOHashAction.setSource(byte[]) 
- ADDED   : ```api```  removed NutsId.compatFilter and NutsVersion.compatFilter and replaced by compatNewer/compatOlder
- ADDED   : ```api```  replaced string messages with NutsMessage in NutsLogger
- ADDED   : ```api```  removed 'NutsInput' and 'NutsOutput'
- ADDED   : ```api```  removed 'NutsCommandlineFamily' and replaced by 'NutsShellFamily'
- ADDED   : ```api```  added 'NutsBootTerminal' to help nuts bootstrap using custom stdin/out end err  
- CHANGED : ```api```  added 'NutsHomeLocation' to replace compound key NutsOSFamily and NutsStoreLocation  
- ADDED   : ```api```  added 'NutsPath.isDirectory' and 'NutsPath.isRegularFile' 
- CHANGED : ```api```  removed commandline options '-C' and '--no-color', you can use '--!color' instead
- CHANGED : ```api```  removed commandline options '--no-switch' and '--no-progress', you can use '--!switch' and '--!progress' instead
- CHANGED : ```api```  NutsResultList renamed to NutsStream and revamped with handy stream features and added ws.util.streamOf(...)
- CHANGED : ```api```  ws.io.expandPath replaced by NutsPath.builder.setExpanded(true)
- REMOVED : ```api```  removed deprecated ClassifierMapping
- REMOVED : ```api```  removed NutsTokenFilter (little to no interest)
- REMOVED : ```api```  removed deprecated feature inheritedLog
- ADDED   : ```api```  NutsVal, a simple wrapper for strings and objects with helpful converters used in args, env, options and properties.
- CHANGED : ```api```  changed descriptor to add maven profiles support, mainly added platform for dependency and added os/platform etc to property
- ADDED   : ```api```  added NutsShellFamily to support bash, csh, and other shell families
- ADDED   : ```pom``` add Manifest Entry 'Automatic-Module-Name' in all projects to support j9+ module technology
- FIXED   : ```impl``` NutsFormat now creates any missing parent folder when calling print(Path/File) or println(Path/File)
