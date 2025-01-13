---
id: NCmdLine
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

## Nuts Application Framework Default Options

All Applications inherit some default and useful options (see Nuts Command Line options for details). These options affect current session behaviour.

* -T
* --output-format-option
* -O
* --output-format
* --tson
* --yaml
* --json
* --props
* --plain
* --table
* --tree
* --xml
* -y
* --yes
* --ask
* -n
* --no
* --error
* --trace
* --solver
* --progress
* --debug
* -f
* --fetch
* -a
* --anywhere
* -F
* --offline
* --online
* --remote
* -c
* --color
* -B
* --bot
* --dry
* -D
* --out-line-prefix
* --err-line-prefix
* --line-prefix
* --embedded
* -b
* --external
* --spawn
* -x
* --system
* --current-user
* --as-root
* --sudo
* --as-user
* --verbose
* --log-verbose
* --log-finest
* --log-finer
* --log-fine
* --log-info
* --log-warning
* --log-severe
* --log-config
* --log-all
* --log-off
* --log-term-verbose
* --log-term-finest
* --log-term-finer
* --log-term-fine
* --log-term-info
* --log-term-warning
* --log-term-severe
* --log-term-config
* --log-term-all
* --log-term-off
* --log-file-verbose
* --log-file-finest
* --log-file-finer
* --log-file-fine
* --log-file-info
* --log-file-warning
* --log-file-severe
* --log-file-config
* --log-file-all
* --log-file-off
* --log-file-size
* --log-file-name
* --log-file-base
* --log-file-count
* -?
* -h
* --help
* --skip-event
* --version


## Creating NCmdLine

Command line can either created manually or parsed.

You can create a command by simply providing  the arguments:

```java
    NCmdLine c1= NCmdLine.of("ls","-l");
```

You can also create a commandline by parsing a string.

*nuts* supports multiple commandline dialects (bash/linux, bat/Windows,...)

```java
    NCmdLine c1= NCmdLine.parse("ls -l", NShellFamily.BASH,session);
```

When you do not specify the NShellFamily, runtime OS default is considered.

```java
    NCmdLine c1= NCmdLine.parse("ls -l", session);
```

You would want to be portable across all operating systems, you can use "parseDefault" method.

```java
    NCmdLine c1= NCmdLine.parseDefault("ls -l");
```

## Using CommandLine

```java
NCmdLine cmdLine = yourCommandLine();
boolean boolOption = false;
String stringOption = null;
List  others = new ArrayList<>();
NArg a;
while (cmdLine. hasNext()) {
  a = cmdLine. peek().get();
  if (a.isOption()) {
      switch (a.key()) {
          case "-o":
          case "--option": {
              a = cmdLine. nextFlag().get(session);
              if (a.isEnabled()) {
                  boolOption = a.getBooleanValue().get(session);
              }
              break;
          }
          case "-n":
          case "--name": {
              a = cmdLine.nextEntry().get(session);
              if (a.isEnabled()) {
                  stringOption = a.getStringValue().get(session);
              }
              break;
          }
          default: {
              session. configureLast(cmdLine);
          }
      }
  } else {
      others. add(cmdLine. next().get().toString());
  }
}
// test if application is running in exec mode
// (and not in autoComplete mode)
if (cmdLine. isExecMode()) {
  // do the good staff here
  session. out().println(NMsg. ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
}
```

## Using CommandLine, The recommended way...


```java
NCmdLine cmdLine = session. getCmdLine();
NRef<Boolean>  boolOption = NRef.of(false);
NRef<String>  stringOption = NRef.ofNull();
List<String>  others = new ArrayList<>();
cmdLine.forEachPeek ((a,l,c)-> {
  if (a.isOption()) {
      switch (a.key()) {
          case "-o":
          case "--option": {
              cmdLine.withNextFlag((v, e, s)->boolOption.set(v));
              return true;
          }
          case "-n":
          case "--name": {
              cmdLine.withNextEntry((v, e, s)->stringOption.set(v));
              return true;
          }
      }
      return false;
  } else {
      nonOptions.add(l.next().get().toString());
      return true;
  }
});

```

