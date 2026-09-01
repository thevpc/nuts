---
title: Customizing CmdLine parsing
---

## Configuring NCmdLine
`setCommandName(String)`
This method help defining the name of the command supporting this command line. This is helpful when generating errors/exception so that the message is relevant
for instance, you would call ```setCommandName("ls")```, so that all errors are in the form of ```ls: unexpected argument --how```

`setExpandSimpleOptions(true|false)`
This method can change the default behavior of NCmdLine (defaults to `true`). When `true`, options in the form `-ex` are expanded to `-e -x`.


`registerSpecialSimpleOption(argName)`
This method limits `setExpandSimpleOptions` application so that for some options that start with `-` (simple options), they are not expanded. 
A useful example is '-version'. You wouldn't want it to be interpreted as '-v -e -r -s -i -o -n', would you?

`setExpandArgumentsFile(true|false)`
This method can change the default behavior of NCmdLine (defaults to `true`). When `false`, options in the form `@path/to/arg/file` are interpreted as non options.
When true (which is the default), the parser will load arguments from the given file/location.

