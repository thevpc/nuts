---
id: changelog
title: Change Log
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

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
