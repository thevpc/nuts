---
id: commandline
title: Command Line Arguments
sidebar_label: Command Line Arguments
---
${include($"${resources}/header.md")}

# Nuts Commandline
**Nuts** supports a specific format for command line arguments. This format is the format supported in **```nuts```** Application Framewok (NAF) and as such all NAF applications support the same command line arguments format.
Arguments in **```nuts```** can be options or non options. Options always start with dash (-). 

## Short vs Long Options
Options can be long options (starts with double dash) or short options (start with a single dash). 
Many arguments support both forms. For instance "-w" and "--workspace" are the spported forms to define the workspace location in the nuts command.

## Option Values
Options can also support a value of type string or boolean.  The value can be suffixed to the option while separated with '=' sign or immediately after the option. As an example "-w=/myfolder/myworkspace" and  "--workspace /myfolder/myworkspace" are equivalent.

## Boolean Options
Particularly, when the value is a boolean, the value do not need to be defined. As a result "--skip-companions" and "--skip-companions=true" are equivalent. However "--skip-companions true" is not (because the option is of type boolean) and "true" will be parsed as a NonOption.

To define a "false" value to the boolean option we can either suffix with "=false" or prefix with "!" or "~" sign. 
Hence, "--skip-companions=false", "--!skip-companions" and "--~skip-companions" are all equivalent.

## Combo Simple Options
Simple options can be grouped in a single word. "-ls" is equivalent to "-l -s". So one should be careful. 
One exception though. For portability reasons, "-version" is considered a single short option.

## Ignoring Options, Comments
Options starting with "-//" and "--//" are simply ignored by the command line parser.

## Nuts Option Types

Options in **```nuts```** are regrouped in multiple categories. An option can belong to multiple categories though.

* Create Options : such options are only relevant when creating a new workspace. They define the configuration of the workspace to create. They will be ignored when the workspace already exists. Examples include
    * --skip-companions
    * --archetype
    * --store-strategy
    * --standalone

* Open Options : such options are relevant when creating a new workspace or when opening an existing workspace. They define the way commands are executed. Examples include
    * --workspace
    * --bot
    * --reset

* Exported Options : are passed to sub-**nuts**-processes that will be created by **nuts**. For instance when nuts will call the **nsh** command it will spawn a new process. In such case, these options are passed to the sub-process as environment variable.
    * --workspace
    * --bot
    * --no-color

* Application Options : are options that are by default supported by Applications using NAF (Nuts Application Framework) (as well as Nuts it self).
    * --help
    * --version

all **```nuts```** options are described in the command help. Just type :
```
nuts --help
```
 
