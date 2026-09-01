---
title: Creating NCmdLine instance
---


Command line can either be created manually or parsed.

## Manual creation

You can create a command by providing the arguments:

```java
    NCmdLine c1= NCmdLine.ofArgs("ls","-l");
```

## Parsing from string

You can also create a commandline by parsing a string.

`NAF` supports multiple commandline dialects (bash/linux, bat/Windows,...)

```java
    NCmdLine c1= NCmdLine.of("ls -l", NShellFamily.BASH);
```

When you do not specify the NShellFamily, runtime OS default is considered.

```java
    NCmdLine c1= NCmdLine.parse("ls -l");
```

## Portable Parsing
You would want to be portable across all operating systems, you can use ```ofDefault``` method.

```java
    NCmdLine c1= NCmdLine.ofDefault("ls -l");
```
