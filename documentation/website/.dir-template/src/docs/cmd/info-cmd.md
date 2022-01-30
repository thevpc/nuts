---
id: info-cmd
title: Info Command
sidebar_label: Info Command
---
${{include($"${resources}/header.md")}}


**info** command is a more verbose command than version. It shows a lot of other **nuts** properties that describe the booted workspace, such as the workspace name, the store locations (artifacts, caches, ....)
```
me@linux:~> nuts info
name                     = default-workspace
nuts-api-version         = ${{latestApiVersion}}
nuts-api-id              = net.vpc.app.nuts:nuts#${{latestApiVersion}}
nuts-runtime-id          = net.vpc.app.nuts:nuts-core#${{latestRuntimeVersion}}
nuts-runtime-path        = ~/.cache/nuts/default-workspace/boot/net/vpc/app/nuts/nuts-core/${{latestRuntimeVersion}}/nuts-core-${{latestRuntimeVersion}}.jar;~/.cache/nuts/default-workspace/boot/net/vpc/app/nuts/nuts/${{latestApiVersion}}/nuts-${{latestApiVersion}}.jar;~/.cache/nuts/default-workspace/boot/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar;~/.cache/nuts/default-workspace/boot/org/fusesource/jansi/jansi/1.17.1/jansi-1.17.1.jar
nuts-workspace-id        = 99b73002-804d-4e4c-9a13-f57ac1f40b3d
nuts-store-layout        = linux
nuts-store-strategy      = exploded
nuts-repo-store-strategy = exploded
nuts-global              = false
nuts-workspace           = ~/.config/nuts/default-workspace
nuts-workspace-apps      = ~/.local/share/nuts/apps/default-workspace
nuts-workspace-config    = ~/.config/nuts/default-workspace/config
nuts-workspace-var       = ~/.local/share/nuts/var/default-workspace
nuts-workspace-log       = ~/.local/log/nuts/default-workspace
nuts-workspace-temp      = ~/nuts/default-workspace
nuts-workspace-cache     = ~/.cache/nuts/default-workspace
nuts-workspace-lib       = ~/.local/share/nuts/lib/default-workspace
nuts-workspace-run       = /run/user/1000/nuts/default-workspace
nuts-open-mode           = open-or-create
nuts-secure              = false
nuts-gui                 = false
nuts-inherited           = false
nuts-recover             = false
nuts-reset               = false
nuts-debug               = false
nuts-trace               = true
nuts-read-only           = false
nuts-skip-companions     = false
nuts-skip-welcome        = false
java-version             = 1.8.0_222
platform                 = java#1.8.0_222
java-home                = /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0/jre
java-executable          = /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0/jre/bin/java
java-classpath           = ~/.m2/repository/net/vpc/app/nuts/nuts/${{latestApiVersion}}/nuts-${{latestApiVersion}}.jar
java-library-path        = /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
os-name                  = linux#4.12.14-lp151.28.13-default
os-family                = linux
os-dist                  = opensuse-leap#15.1
os-arch                  = x86_64
user-name                = me
user-home                = /home/me
user-dir                 = /home/me
command-line-long        = --color=system --trace --open-or-create --exec info
command-line-short       = -t info
inherited                = false
inherited-nuts-boot-args = 
inherited-nuts-args      = 
creation-started         = 2019-08-26 00:02:10.903
creation-finished        = 2019-08-26 00:02:11.223
creation-within          = 320ms
repositories-count       = 5
```
