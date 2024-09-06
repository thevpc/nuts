---
id: search-cmds
title: Search Command
sidebar_label: Search Command
---


Artifact can be in multiple states. they can be
+ 'unavailable' if no registered repository can serve that artifact
+ 'available' if there is at least one repository that can serve that artifact
+ 'fetched' if there is a repository that can serve the artifact from local machine. This happens either if the repository is a local one (for instance a folder repository) or the repository has already downloaded and cached the artifact
+ 'installed' if the artifact is fetched and installed in the the machine.
+ 'installed default' if the artifact is installed and marked as default

To search for these artifacts status you will use the appropriate option flag with an artifact query.
An artifact query is a generalization of an artifact id where you may use wild cards and version intervals in it.
These are some examples of artifact queries.
```
# all artifacts that start with netbeans, whatever groupId they belong to
# nuts search netbeans*

# all artifacts that start with netbeans, whatever groupId they belong to. same as the latter.
# nuts search *:netbeans*

# all artifacts in the net.thevpc.app groupId
# nuts search net.thevpc.*:*

# all artifacts in the net.thevpc.* groupId which includes all of net.thevpc.app and net.thevpc.app.example for instance.
# nuts search net.thevpc.*:*

# all artifacts that start with netbeans and is available for windows operating system in x86_64 architecture
# nuts search netbeans*?os=windows&arch=x86_64

# all netbeans launcher version that are greater than 1.2.0 (excluding 1.2.0)
# nuts search netbeans-launcher#]1.2.0,[

# all netbeans launcher version that are greater than 1.2.0 (including 1.2.0)
# nuts search netbeans-launcher#[1.2.0,[

```
You can then use the these flags to tighten your search :
+ --installed (or -i) : search only for installed artifacts
+ --local     : search only for fetched artifacts
+ --remote    : search only for non fetched artifacts
+ --online    : search in installed then in local then in remote, stop when you first find a result.
+ --anywhere  (or -a) : search in installed and local and remote, return all results.

You can also change the output layout using --long (or -l) flag
```
me@linux:~> nuts search -i -l
I-X 2019-08-26 09:53:53.141 anonymous vpc-public-maven net.thevpc.app:netbeans-launcher#1.2.1
IcX 2019-08-24 11:05:49.591 admin     maven-local      net.thevpc.app.nuts.toolbox:nsh#0.8.4.0
I-x 2019-08-26 09:50:03.423 anonymous vpc-public-maven net.thevpc.app:kifkif#1.3.3
```
you can even change the output format
```
me@linux:~> nuts search -i -l --json
```
```json
[
{
  "id": "vpc-public-maven://net.thevpc.app:netbeans-launcher#1.2.1",
  "descriptor": {
    "id": "net.thevpc.app:netbeans-launcher#1.2.1",
    "parents": [],
    "packaging": "jar",
    "executable": true,
    ...
  }
 }
]
```
Indeed, all of **nuts** commands support the following formats : **plain**, **json**, **xml**, **table** and **tree** because **nuts** adds support to multi format output by default. You can switch to any of them for any command by adding the right option in **nuts** (typically --plain, --json, --xml, --table and --tree). I know this is awesome!.

**search** is a very versatile command, you are welcome to run "nuts search --help" to get more information.
