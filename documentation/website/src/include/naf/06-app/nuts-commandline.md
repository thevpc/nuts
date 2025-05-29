---
id: NCmdLine
title: Command Line Arguments
sidebar_label: Command Line Arguments
---


**nuts** supports a specific format for command line arguments. This format is the format supported in **```nuts```** Application Framework (NAF) and as such all NAF applications support the same command line arguments format.
Arguments in **```nuts```** can be options or non options. Options always start with hyphen (-). 


## Nuts Application Framework CommandLine

Application Command line can be retrieved via `NApp` instance:

```java
    NCmdLine c1= NApp.of().getCmdine();
```

## Exec / Autocomplete modes

```java
    NCmdLine c= NApp.of().getCmdine();
    if(c.isExecMode()){
        ///    
    }
```

## Default Options

All Applications inherit some default and useful options (see Nuts Command Line options for details). These options affect current session behaviour.

* `-T`
* `--output-format-option`
* `-O`
* `--output-format`
* `--tson`
* `--yaml`
* `--json`
* `--props`
* `--plain`
* `--table`
* `--tree`
* `--xml`
* `-y`
* `--yes`
* `--ask`
* `-n`
* `--no`
* `--error`
* `--trace`
* `--solver`
* `--progress`
* `--debug`
* `-f`
* `--fetch`
* `-a`
* `--anywhere`
* `-F`
* `--offline`
* `--online`
* `--remote`
* `-c`
* `--color`
* `-B`
* `--bot`
* `--dry`
* `-D`
* `--out-line-prefix`
* `--err-line-prefix`
* `--line-prefix``
* `--embedded`
* `-b`
* `--external`
* `--spawn`
* `-x`
* `--system`
* `--current-user`
* `--as-root`
* `--sudo`
* `--as-user`
* `--verbose`
* `--log-verbose`
* `--log-finest`
* `--log-finer`
* `--log-fine`
* `--log-info`
* `--log-warning`
* `--log-severe`
* `--log-config`
* `--log-all`
* `--log-off`
* `--log-term-verbose`
* `--log-term-finest`
* `--log-term-finer`
* `--log-term-fine`
* `--log-term-info`
* `--log-term-warning`
* `--log-term-severe`
* `--log-term-config`
* `--log-term-all`
* `--log-term-off`
* `--log-file-verbose`
* `--log-file-finest`
* `--log-file-finer`
* `--log-file-fine`
* `--log-file-info`
* `--log-file-warning`
* `--log-file-severe`
* `--log-file-config`
* `--log-file-all`
* `--log-file-off`
* `--log-file-size`
* `--log-file-name`
* `--log-file-base`
* `--log-file-count`
* `-?`
* `-h`
* `--help`
* `--skip-event`
* `--version`

