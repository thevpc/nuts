# Nuts Text Coloring Format
Nuts comes up with a simple coloring syntax that helps writing better looking portable command line programs.
standard output is automatically configured to accept the "Nuts Text Coloring Format" (NTCF) syntax. 
Though it remains possible to disable this ability using the --no-color standard option (or programmatically, 
see nuts api documentation). NTCF will be translated to the underlying terminal implementation using ANSI 
escape code on linux/windows terminals if available.

Here after a showcase of available NTCF syntax.

![04-nuts-text-coloring-format](04-nuts-text-coloring-format.png)