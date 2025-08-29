---
id: NCmdLine
title: NCmdLine
sidebar_label: NCmdLine
---


**nuts** provides a useful commandline parser ```NCmdLine```  
It supports command lines in the following form :

```sh
my-app -o=y --name='some name' -ex --extra value arg1 arg2
```
where the command here supports short and long options (short ones are ```-o```, ```-e``` and ```-x```, where ```-e``` and ```-x``` are combined as ```-ex```), 
and of course non options or regular arguments (here ```arg1``` and ```arg2```).
Note also that ```value``` could be interpreted as a value for ```--extra``` (or not; depending on how you configure your parser, for  this option).

## Short vs Long Options
Options can be long options (starts with double hyphen) or short options (start with a single hyphen). 
Many arguments support both forms. For instance "-w" and "--workspace" are the supported forms to define the workspace location in the nuts command.

## Valued / Non-valued Options
Options can also support a value of type string or boolean. The value can be suffixed to the option while separated with '=' sign or immediately after the option. 

As an example, all are equivalent.  


Of course, not all options can support values, an not all options neither support the suffixed and/or the non-suffixed mode. Please relate to the documentation of nuts or the application you are using to know how to use the options.

## Boolean Options
Particularly, when the value is a boolean, the value do not need to be defined. As a result "--install-companions" and "--install-companions=true" are equivalent. However "--install-companions true" is not (because the option is of type boolean) and "true" will be parsed as a NonOption.

To define a ```false``` value to the boolean option we can either suffix with ```=false``` or prefix with `!` or `~` sign. 
Hence, ``` --install-companions=false```, ``` --!install-companions``` and ``` --~install-companions``` are all equivalent.

Note also that `~` is equivalent to `!` because in bash shells (and some other shells) `!` will be interpreted as expansion operator.

## Combo Simple Options
Simple options can be grouped in a single word. "-ls" is equivalent to "-l -s". So one should be careful. 
One exception though. For portability reasons, "-version" is considered a single short option.

## Ignoring Options, Comments
Options starting with "-//" and "--//" are simply ignored by the command line parser.


## Creating NCmdLine

Command line can either be created manually or parsed.

You can create a command by providing the arguments:

```java
    NCmdLine c1= NCmdLine.ofArgs("ls","-l");
```

You can also create a commandline by parsing a string.

*nuts* supports multiple commandline dialects (bash/linux, bat/Windows,...)

```java
    NCmdLine c1= NCmdLine.of("ls -l", NShellFamily.BASH);
```

When you do not specify the NShellFamily, runtime OS default is considered.

```java
    NCmdLine c1= NCmdLine.parse("ls -l");
```

You would want to be portable across all operating systems, you can use ```ofDefault``` method.

```java
    NCmdLine c1= NCmdLine.ofDefault("ls -l");
```

## Configuring NCmdLine
`setCommandName(true|false)`
This method help defining the name of the command supporting this command line. This is helpful when generating errors/exception so that the message is relevant
for instance, you would call ```setCommandName("ls")```, so that all errors are in the form of ```ls: unexpected argument --how```

`setExpandSimpleOptions(true|false)`
This method can change the default behavior of NCmdLine (defaulted to `true`). When `true`, options in the form `-ex` are expanded to `-e -x`.


`registerSpecialSimpleOption(argName)`
This method limits `setExpandSimpleOptions` application so that for some options that start with `-` (simple options), they are not expanded. 
A useful example is '-version'. You wouldn't want it to be interpreted as '-v -e -r -s -i -o -n', would you?

`setExpandArgumentsFile(true|false)`
This method can change the default behavior of NCmdLine (defaulted to `true`). When `false`, options in the form `@path/to/arg/file` are interpreted as non options.
When true (which is the default), the parser will load arguments from the given file/location.


## Using CommandLine, The recommended way...
NCmdLine has a versatile parsing API.
One way to use it is as follows :

```java
    NCmdLine cmdLine = NApp.of().getCmdLine(); // or from somewhere else
    NRef<Boolean> boolOption = NRef.of(false);
    NRef<String> stringOption = NRef.ofNull();
    List<String> others = new ArrayList<>();
    while (cmdLine.hasNext()) {
        cmdLine.matcher()
                .with("-o", "--option").matchFlag((v) -> boolOption.set(v.booleanValue()))
                .with("-n", "--name").matchEntry((v) -> stringOption.set(v.stringValue()))
                .withNonOption().matchAny((v) -> stringOption.set(v.image()))
                .requireDefaults()
        ;
    }
    // test if application is running in exec mode
    // (and not in autoComplete mode)
    if (cmdLine.isExecMode()) {
        //do the good staff here
        NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
    }

```


## Using CommandLine, The simple and legacy way...


```java
NCmdLine cmdLine = NApp.of().getCmdLine();
boolean boolOption = false;
String stringOption = null;
List<String> others = new ArrayList<>();
NArg a;
while (cmdLine.hasNext()) {
    a = cmdLine.peek().get();
    if (a.isOption()) {
        switch (a.key()) {
            case "-o":
            case "--option": {
                a = cmdLine.nextFlag().get();
                if (a.isUncommented()) {
                    boolOption = a.getValue().asBoolean().get();
                }
                break;
            }
            case "-n":
            case "--name": {
                a = cmdLine.nextEntry().get();
                if (a.isUncommented()) {
                    stringOption = a.getValue().asString().get();
                }
                break;
            }
            default: {
                NSession.of().configureLast(cmdLine);
            }
        }
    } else {
        others.add(cmdLine.next().get().image());
    }
}
// test if application is running in exec mode
// (and not in autoComplete mode)
if (cmdLine.isExecMode()) {
    //do the good staff here
    NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
}
```

