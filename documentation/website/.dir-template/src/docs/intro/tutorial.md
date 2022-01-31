---
id: tutorial
title: Tutorial
sidebar_label: Tutorial
order: 5
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

${{include($"${resources}/header.md")}}

## Tutorial for Windows Users
If you are a Linux/Unix/MaxOS user, scroll down to the appropriate section.

TODO...

## Tutorial for Linux/Unix/MaxOS users
If you are a Windows user, scroll up to the appropriate section.

TODO...

Check your java version 

```bash
$> java -version

        java version "1.8.0_211"
        Java(TM) SE Runtime Environment (build 1.8.0_211-b12)
        Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)

$> wget https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/0.8.2/nuts-0.8.2.jar -O nuts.jar

        --2021-11-10 00:53:52--  https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/0.8.2/nuts-0.8.2.jar
        Resolving repo1.maven.org (repo1.maven.org)... 151.101.240.209
        Connecting to repo1.maven.org (repo1.maven.org)|151.101.240.209|:443... connected.
        HTTP request sent, awaiting response... 200 OK
        Length: 501378 (490K) [application/java-archive]
        Saving to: ‘nuts.jar’
        nuts.jar                      100%[==============================================>] 489.63K   734KB/s    in 0.7s    
        2021-11-10 00:53:53 (734 KB/s) - ‘nuts.jar’ saved [501378/501378]
        FINISHED --2021-11-10 00:53:53--
        Total wall clock time: 4.9s
        Downloaded: 1 files, 490K in 0.7s (734 KB/s)

$> java -jar nuts.jar -zyN -r dev

$> exit # must restart the terminal

$> nuts

$> nuts search

$> nuts search 'net.thevpc.nuts.toolbox:*' --anywhere
 
        net.thevpc.nuts.toolbox:nsh#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ndoc#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ndexer#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:njob#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:nmvn#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:nwork#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:nsh#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ntomcat#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ntalk-agent#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ndiff#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:nserver#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ndb#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:nversion#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ntemplate#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ncode#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:ndocusaurus#${{latestRuntimeVersion}}
        net.thevpc.nuts.toolbox:noapi#${{latestRuntimeVersion}}

$> nuts install njob

$> nuts update njob
 
$> nuts install netbeans-launcher

$> nuts netbeans-launcher
 


```


