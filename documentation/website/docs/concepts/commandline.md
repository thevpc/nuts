---
id: commandline
title: Command Line Arguments
sidebar_label: Command Line Arguments
---

**nuts** supports a specific format for command line arguments. This format is the format supported in **```nuts```** Application Framework (NAF) and as such all NAF applications support the same command line arguments format.
Arguments in **```nuts```** can be options or non options. Options always start with hyphen (-). 

## Short vs Long Options
Options can be long options (starts with double hyphen) or short options (start with a single hyphen). 
Many arguments support both forms. For instance "-w" and "--workspace" are the supported forms to define the workspace location in the nuts command.

## Valued / Non-valued Options
Options can also support a value of type string or boolean. The value can be suffixed to the option while separated with '=' sign or immediately after the option. 

As an example, all are equivalent.  

```sh
nuts -w=/myfolder/myworkspace
nuts -w /myfolder/myworkspace
nuts --workspace /myfolder/myworkspace
nuts --workspace=/myfolder/myworkspace
```

Of course, not all options can support values, an not all options neither support the suffixed and/or the non-suffixed mode. Please relate to the documentation of nuts or the application you are using to know how to use the options.

## Boolean Options
Particularly, when the value is a boolean, the value do not need to be defined. As a result "--install-companions" and "--install-companions=true" are equivalent. However "--install-companions true" is not (because the option is of type boolean) and "true" will be parsed as a NonOption.

To define a "false" value to the boolean option we can either suffix with "=false" or prefix with "!" or "~" sign. 
Hence, "--install-companions=false", "--!install-companions" and "--~install-companions" are all equivalent.
Note also that `~` if referred to `!` because in bash shells (and som other shells) `!` will be expanded in a special manner.

## Combo Simple Options
Simple options can be grouped in a single word. "-ls" is equivalent to "-l -s". So one should be careful. 
One exception though. For portability reasons, "-version" is considered a single short option.

## Ignoring Options, Comments
Options starting with "-//" and "--//" are simply ignored by the command line parser.

## Nuts Option Types

Options in **```nuts```** are can be of one of the following categories :

* Create Options : such options are only relevant when creating a new workspace. They define the configuration of the workspace to create. They will be ignored when the workspace already exists. They will be ignored too, in sub-processes. Examples include
    * --install-companions
    * --archetype
    * --store-strategy
    * --standalone

* Runtime Options : such options are relevant when running a workspace (be it existing or to be created) and are not passed to sub-processes
    * --reset
    * --recover
    * --dry
    * --version

* Exported Options : are passed to sub-**nuts**-processes that will be created by **nuts**. For instance when nuts will call the **nsh** command it will spawn a new process. In such case, these options are passed to the sub-process as environment variable.
    * --workspace
    * --global
    * --color
    * --bot

* Executor Options : are options that are supported byte the package executor. Most of the time this will be the java executor and hence this coincides with the JVM options) 
    * -Xmx...
    * -Xmx
    * -D...

* Custom Nuts options : are special `nuts` options that are specific to nuts implementation or validation process to be promoted to standard options. The arguments parser will never report an error regarding such options. They are used when available and valid. they will be ignored in all other cases. Such options start with triple hyphen (---)
  * ---monitor.enabled
  * ---monitor.start
  * ---show-command
  * ---perf
  * ---init-platforms
  * ---init-scripts
  * ---init-java
  * ---system-desktop-launcher
  * ---system-menu-launcher
  * ---system-custom-launcher

* Application Options : are options that are by default supported by Applications using NAF (Nuts Application Framework) (as well as Nuts it self).
    * --help
    * --version

all **```nuts```** options are described in the command help. Just type :
```
nuts --help
```
 
