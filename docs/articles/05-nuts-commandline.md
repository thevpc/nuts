# Nuts Commandline
**Nuts** supports a specific format for command line arguments. This format is the format supported in Nuts Application Framewok (NAF) and as such all NAF application support the same command line arguments format.
Arguments in Nuts **Nuts** can be options or non options. Options always start with dash (-). 
## String Options
Options can be long options (starts with double dash) or short options (start with a single dash). 
Many arguments support the two forms. For instance "-w" and "--workspace" are the spported forms to define the workspace location in the nuts command.
Options can also support a value of type string or boolean.  The value can be suffixed to the option while separated with '=' sign or immediately after the option. As an example "-w=/myfolder/myworkspace" and  "--workspace /myfolder/myworkspace" are equivalent.
## Boolean Options
Particularly, when the value is a boolean, the value do not need to be defined. As a result "--skip-companions" and "--skip-companions=true" are equivalent. However "--skip-companions true" is not (because the option is of type boolean) and true will be considered as a NonOption .

To define a false value to the boolean option we can either suffix with "=false" or prefix with "!" or "~" sign. 
Hence, "--skip-companions=false", "--!skip-companions" and , "--~skip-companions" are all equivalent.

## Combo Simple Options
Simple options can be grouped in a single word. "-ls" is equivalent to "-l -s". So one should be careful. 
One exception though. For portability reasons, "-version" is considered a single short option.

## Ignoring Options
Options starting with "-//" and "--//" are simply ignored by the command line parser.


