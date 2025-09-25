---
title: Command Line Elements
---

## Options vs Non-options
In `NAF`, command line arguments are categorized into three main types:

- Options : Arguments that start with - or + and may carry a value.
- Non-Options : Arguments that do not start with - or +. Typically filenames, commands, or positional arguments.
- Comments : Arguments that are ignored, starting with -// or +//.

## Short vs Long Options
- Short options: single prefix - or + followed by a single character, e.g., -o or +x.
- Long options: double prefix -- or ++ followed by a word, e.g., --output or ++enable-feature.

Short options can be combined:
```bash
-ex   # equivalent to -e -x
```

Exception: certain options like -version or +version are treated as single options and not expanded.

## Options With Values
- Options can carry a value (string, boolean, number).
- Values can be attached with = or provided as the next argument:

```bash
--name=Alice   # attached value
+name Alice    # next-argument value
```

## Boolean Options

Boolean options do not always require a value:

```
--install      # equivalent to true
+install=true
--install=false  # or --!install-companions / --~install-companions
```

`!` and `~` are treated as negation. This is useful in shells where `!` has special meaning.

Boolean options in NAF can be true or false without explicitly writing true or false. The parser recognizes multiple synonyms for convenience:

### True Values
```
true, enable, enabled, yes, always, y, on, ok, t, o
```
### False Values
```
false, disable, disabled, no, none, never, n, off, ko, f
```


## Non-Options
Non-options are all other arguments — files, commands, or positional values:

```
my-app -o --name Alice file1.txt file2.txt
```

Here, file1.txt and file2.txt are non-options.

## Ignored Arguments

Arguments starting with -// or +// are ignored and can be used for comments or metadata in the command line.
