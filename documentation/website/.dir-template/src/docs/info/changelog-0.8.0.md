---
id: changelog080
title: Change Log 0.8.0
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.0.0 (PUBLISHED VERSION)
- ```2020/11/8? 	nuts 0.8.0.0 (*)``` released [download nuts-0.8.0.jar](https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.0/nuts-0.8.0.jar)
- WARNING: this is the first version to be deployed to maven central. previous versions will no longer be supported
- WARNING: this is a **major version**, API has evolved with multiple incompatibilities with previous versions
- WARNING: The OSS License has changed from GPL3 to the more permessive Apache Licence v2.0
- CHANGED: changed packages from net.vpc to net.thevpc (required for central to be aligned with website)
- CHANGED: removed support for vpc-public-maven and vpc-public-nuts
- CHANGED: ```nuts -Z``` will update ```.bashrc``` file and switch back to default workspace
- ADDED  : when a dependency is missing it will be shown in the error message
- ADDED  : nuts commandline argument --N (--expire) to force reloading invoked artifacts (expire fetched jars). a related NSession.expireTime is introduced to force reinstall of any launched application and it dependencies, example: ```nuts -N ndi```
- ADDED  : install --strategy=install|reinstall|require|repair introduced to select install strategy (or sub command)
- ADDED  : NutsInput & NutsOutput to help considering reusable sources/targets
- ADDED  : nuts commandline argument --skip-errors  to ignore unsupported commandline args
- ADDED  : new toolbox njob, to track service jobs (how many hours you are working on each service project)
- ADDED  : new next-term, to support jline console extension into nuts
- ADDED  : workspace.str() to create NutsStringBuilder
- ADDED  : 'switch' command in ndi to support switching from one workspace to another. example : ```ndi switch -w other-workspace -a 0.8.0```
