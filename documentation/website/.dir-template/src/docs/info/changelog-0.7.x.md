---
id: changelog070
title: Change Log 0.7.0
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

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
