---
id: javadoc_Command_Line
title: Command Line
sidebar_label: Command Line
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsArgument
```java
public interface net.vpc.app.nuts.NutsArgument
```
 Command Line Argument

 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 🎛 Instance Properties
#### 📄🎛 argumentKey
return new instance (never null) of the key part of the argument. The key
 does not include neither ! nor // or = argument parts as they are parsed
 separately. Here example of getArgumentKey result of some arguments
 \<ul\>
 \<li\>Argument("key").getArgumentKey() ==&gt; Argument("key") \</li\>
 \<li\>Argument("key=value").getArgumentKey() ==&gt; Argument("key") \</li\>
 \<li\>Argument("--key=value").getArgumentKey() ==&gt; Argument("--key")
 \</li\>
 \<li\>Argument("--!key=value").getArgumentKey() ==&gt; Argument("--key")
 \</li\>
 \<li\>Argument("--!//key=value").getArgumentKey() ==&gt; Argument("--key")
 \</li\>
 \</ul\>
```java
[read-only] NutsArgument public argumentKey
public NutsArgument getArgumentKey()
```
#### 📄🎛 argumentOptionName
return option key part excluding prefix (\'-\' and \'--\')
```java
[read-only] NutsArgument public argumentOptionName
public NutsArgument getArgumentOptionName()
```
#### 📄🎛 argumentValue
return new instance (never null) of the value part of the argument (after
 =). However Argument\'s value may be null (
 \{\@code getArgumentValue().getString() == null\}). Here are some examples of
 getArgumentValue() result for some common arguments
 \<ul\>
 \<li\>Argument("key").getArgumentValue() ==&gt; Argument(null) \</li\>
 \<li\>Argument("key=value").getArgumentValue() ==&gt; Argument("value")
 \</li\>
 \<li\>Argument("key=").getArgumentValue() ==&gt; Argument("") \</li\>
 \<li\>Argument("--key=value").getArgumentValue() ==&gt; Argument("value")
 \</li\>
 \<li\>Argument("--!key=value").getArgumentValue() ==&gt; Argument("value")
 \</li\>
 \<li\>Argument("--!//key=value").getArgumentValue() ==&gt;
 Argument("value") \</li\>
 \</ul\>
```java
[read-only] NutsArgument public argumentValue
public NutsArgument getArgumentValue()
```
#### 📄🎛 boolean
test if the argument is valid boolean. a valid boolean mush match one of
 the following regular expressions :
 "true|enable|enabled|yes|always|y|on|ok|t" : will be evaluated as true
 boolean. "false|disable|disabled|no|none|never|n|off|ko" : will be
 evaluated as false boolean. In both cases, this method returns true.
 Otherwise, it will return false.
```java
[read-only] boolean public boolean
public boolean isBoolean()
```
#### 📄🎛 booleanValue
parse argument\'s value as boolean equivalent to
 \{\@code getArgumentValue().getBoolean()\}
```java
[read-only] boolean public booleanValue
public boolean getBooleanValue()
```
#### 📄🎛 double
parse number and return double.
```java
[read-only] double public double
public double getDouble()
```
#### 📄🎛 enabled
false if option is in one of the following forms :
 \<ul\>
 \<li\>-//name\</li\>
 \<li\>--//name\</li\>
 \</ul\>
 where name is any valid identifier
```java
[read-only] boolean public enabled
public boolean isEnabled()
```
#### 📄🎛 int
parse number and return integer.
```java
[read-only] int public int
public int getInt()
```
#### 📄🎛 keyValue
true if the argument is in the form key=value
```java
[read-only] boolean public keyValue
public boolean isKeyValue()
```
#### 📄🎛 keyValueSeparator
return query value separator
```java
[read-only] String public keyValueSeparator
public String getKeyValueSeparator()
```
#### 📄🎛 long
parse number and return long.
```java
[read-only] long public long
public long getLong()
```
#### 📄🎛 negated
true if option is in one of the following forms :
 \<ul\>
 \<li\>-!name\[=...]\</li\>
 \<li\>--!name\[=...]\</li\>
 \<li\>!name\[=...]\</li\>
 \</ul\>
 where name is any valid identifier
```java
[read-only] boolean public negated
public boolean isNegated()
```
#### 📄🎛 nonOption
true if the argument do not start with \'-\' or \'+\' or is blank. this is
 equivalent to \{\@code !isOption()\}.
```java
[read-only] boolean public nonOption
public boolean isNonOption()
```
#### 📄🎛 option
true if the argument starts with \'-\' or \'+\'
```java
[read-only] boolean public option
public boolean isOption()
```
#### 📄🎛 string
string representation of the argument or null
```java
[read-only] String public string
public String getString()
```
#### 📄🎛 stringKey
return key part (never null) of the argument. The key does not include
 neither ! nor // or = argument parts as they are parsed separately. Here
 are some examples of getStringKey() result for some common arguments
 \<ul\>
 \<li\>Argument("key").getArgumentKey() ==&gt; "key" \</li\>
 \<li\>Argument("key=value").getArgumentKey() ==&gt; "key" \</li\>
 \<li\>Argument("--key=value").getArgumentKey() ==&gt; "--key"
 \</li\>
 \<li\>Argument("--!key=value").getArgumentKey() ==&gt; "--key"
 \</li\>
 \<li\>Argument("--!//key=value").getArgumentKey() ==&gt; "--key"
 \</li\>
 \<li\>Argument("--//!key=value").getArgumentKey() ==&gt; "--key"
 \</li\>
 \</ul\>
 equivalent to \{\@code getArgumentKey().getString()\}
```java
[read-only] String public stringKey
public String getStringKey()
```
#### 📄🎛 stringOptionName
return option key part excluding prefix (\'-\' and \'--\')
```java
[read-only] String public stringOptionName
public String getStringOptionName()
```
#### 📄🎛 stringOptionPrefix
return option prefix part  (\'-\' and \'--\')
```java
[read-only] String public stringOptionPrefix
public String getStringOptionPrefix()
```
#### 📄🎛 stringValue
equivalent to \{\@code getArgumentValue().getString()\}
```java
[read-only] String public stringValue
public String getStringValue()
```
### ⚙ Instance Methods
#### ⚙ getBoolean(defaultValue)
return boolean value if the current argument can be parsed as valid
 boolean of defaultValue if not

 "true|enable|enabled|yes|always|y|on|ok|t" are considered \'true\'.
 "false|disable|disabled|no|none|never|n|off|ko" are considered \'false\'.

```java
Boolean getBoolean(Boolean defaultValue)
```
**return**:Boolean
- **Boolean defaultValue** : default value

#### ⚙ getDouble(defaultValue)
parse number and return double or \{\@code defaultValue\} if not parsable.

```java
double getDouble(double defaultValue)
```
**return**:double
- **double defaultValue** : defaultValue

#### ⚙ getInt(defaultValue)
parse number and return integer or \{\@code defaultValue\} if not parsable.

```java
int getInt(int defaultValue)
```
**return**:int
- **int defaultValue** : defaultValue

#### ⚙ getLong(defaultValue)
parse number and return long or \{\@code defaultValue\} if not parsable.

```java
long getLong(long defaultValue)
```
**return**:long
- **long defaultValue** : defaultValue

#### ⚙ getString(defaultValue)
string representation of the argument or the given defaultValue

```java
String getString(String defaultValue)
```
**return**:String
- **String defaultValue** : returned when this argument references null value

#### ⚙ getStringValue(defaultValue)
equivalent to \{\@code getArgumentValue().getString(value)\}

```java
String getStringValue(String defaultValue)
```
**return**:String
- **String defaultValue** : default value

#### ⚙ required()
Throw an exception if the argument is null

```java
NutsArgument required()
```
**return**:NutsArgument

## ☕ NutsArgumentCandidate
```java
public interface net.vpc.app.nuts.NutsArgumentCandidate
```
 Argument Candidate used in Auto Complete.
 
 Created by vpc on 3/7/17.

 \@since 0.5.5
 \@category Command Line

### 🎛 Instance Properties
#### 📄🎛 display
human display
```java
[read-only] String public display
public String getDisplay()
```
#### 📄🎛 value
argument value
```java
[read-only] String public value
public String getValue()
```
## ☕ NutsArgumentName
```java
public interface net.vpc.app.nuts.NutsArgumentName
```
 Non Option Argument specification

 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 🎛 Instance Properties
#### 📄🎛 name
argument name
```java
[read-only] String public name
public String getName()
```
### ⚙ Instance Methods
#### ⚙ getCandidates(context)
argument candidate values

```java
List getCandidates(NutsCommandAutoComplete context)
```
**return**:List
- **NutsCommandAutoComplete context** : autocomplete

## ☕ NutsArgumentType
```java
public final net.vpc.app.nuts.NutsArgumentType
```
 Argument parse Type

 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 📢❄ Constant Fields
#### 📢❄ ANY
```java
public static final NutsArgumentType ANY
```
#### 📢❄ BOOLEAN
```java
public static final NutsArgumentType BOOLEAN
```
#### 📢❄ STRING
```java
public static final NutsArgumentType STRING
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsArgumentType valueOf(String name)
```
**return**:NutsArgumentType
- **String name** : 

#### 📢⚙ values()


```java
NutsArgumentType\[] values()
```
**return**:NutsArgumentType\[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsCommandAutoComplete
```java
public interface net.vpc.app.nuts.NutsCommandAutoComplete
```
 Auto Complete Helper class used to collect argument candidates
 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 🎛 Instance Properties
#### 📄🎛 candidates
current candidates
```java
[read-only] List public candidates
public List getCandidates()
```
#### 📄🎛 currentWordIndex
candidates index
```java
[read-only] int public currentWordIndex
public int getCurrentWordIndex()
```
#### 📄🎛 line
command line string
```java
[read-only] String public line
public String getLine()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 words
command line arguments
```java
[read-only] List public words
public List getWords()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ addCandidate(value)
add candidate

```java
void addCandidate(NutsArgumentCandidate value)
```
- **NutsArgumentCandidate value** : candidate

#### ⚙ get(t)


```java
Object get(Class t)
```
**return**:Object
- **Class t** : 

## ☕ NutsCommandAutoCompleteBase
```java
public abstract net.vpc.app.nuts.NutsCommandAutoCompleteBase
```
 Base (Abstract) implementation of NutsCommandAutoComplete
 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 🪄 Constructors
#### 🪄 NutsCommandAutoCompleteBase()


```java
NutsCommandAutoCompleteBase()
```

### 🎛 Instance Properties
#### 📄🎛 candidates
possible candidates
```java
[read-only] List public candidates
public List getCandidates()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ addCandidate(value)
add candidate

```java
void addCandidate(NutsArgumentCandidate value)
```
- **NutsArgumentCandidate value** : candidate

#### ⚙ addCandidatesImpl(value)
simple add candidates implementation

```java
NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value)
```
**return**:NutsArgumentCandidate
- **NutsArgumentCandidate value** : candidate

#### ⚙ get(t)


```java
Object get(Class t)
```
**return**:Object
- **Class t** : 

## ☕ NutsCommandExecOptions
```java
public net.vpc.app.nuts.NutsCommandExecOptions
```
 Command execution options
 \@author vpc
 \@since 0.5.4
 \@category Command Line

### 🪄 Constructors
#### 🪄 NutsCommandExecOptions()


```java
NutsCommandExecOptions()
```

### 🎛 Instance Properties
#### 📝🎛 directory
execution directory
```java
[read-write] NutsCommandExecOptions public directory
public String getDirectory()
public NutsCommandExecOptions setDirectory(directory)
```
#### 📝🎛 env
execution environment variables
```java
[read-write] NutsCommandExecOptions public env
public Map getEnv()
public NutsCommandExecOptions setEnv(env)
```
#### 📝🎛 executionType
execution type
```java
[read-write] NutsCommandExecOptions public executionType
public NutsExecutionType getExecutionType()
public NutsCommandExecOptions setExecutionType(executionType)
```
#### 📝🎛 executorOptions
execution options
```java
[read-write] NutsCommandExecOptions public executorOptions
public String\[] getExecutorOptions()
public NutsCommandExecOptions setExecutorOptions(executorOptions)
```
#### 📝🎛 failFast
when fail fast,non zero exit value will raise NutsExecutionException
```java
[read-write] NutsCommandExecOptions public failFast
public boolean isFailFast()
public NutsCommandExecOptions setFailFast(failFast)
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsCommandLine
```java
public interface net.vpc.app.nuts.NutsCommandLine
```
 Simple Command line parser implementation. The command line supports
 arguments in the following forms :
 \<ul\>
 \<li\> non option arguments : any argument that does not start with \'-\'\</li\>

 \<li\>
 long option arguments : any argument that starts with a single \'--\' in the
 form of
 \<pre\>--\[//\]\[!\]?\[^=]*\[=.*]\</pre\>
 \<ul\>
 \<li\>// means disabling the option\</li\>
 \<li\>! means switching (to \'false\') the option\'s value\</li\>
 \<li\>the string before the \'=\' is the option\'s key\</li\>
 \<li\>the string after the \'=\' is the option\'s value\</li\>
 \</ul\>
 Examples :
 \<ul\>
 \<li\>--!enable : option \'enable\' with \'false\' value\</li\>
 \<li\>--enable=yes : option \'enable\' with \'yes\' value\</li\>
 \<li\>--!enable=yes : invalid option (no error will be thrown but the result
 is undefined)\</li\>
 \</ul\>
 \</li\>
 \<li\>
 simple option arguments : any argument that starts with a single \'-\' in the
 form of
 \<pre\>-\[//\]\[!]?\[a\-z\]\[=.*]\</pre\> This is actually very similar to long options
 \<ul\>
 \<li\>-!enable (with expandSimpleOptions=false) : option \'enable\' with \'false\'
 value\</li\>
 \<li\>--enable=yes : option \'enable\' with \'yes\' value\</li\>
 \<li\>--!enable=yes : invalid option (no error will be thrown but the result
 is undefined)\</li\>
 \</ul\>

 \</li\>
 \<li\>
 condensed simple option arguments : any argument that starts with a single \'-\' in the
 form of
 \<pre\>-\[//](\[!]?\[a\-z\])+\[=.*]\</pre\> This is actually very similar to long options
 and is parsable when expandSimpleOptions=true. When activating expandSimpleOptions, multi
 characters key will be expanded as multiple separate simple options Examples
 :
 \<ul\>
 \<li\>-!enable (with expandSimpleOptions=false) : option \'enable\' with \'false\'
 value\</li\>
 \<li\>--enable=yes : option \'enable\' with \'yes\' value\</li\>
 \<li\>--!enable=yes : invalid option (no error will be thrown but the result
 is undefined)\</li\>
 \</ul\>

 \</li\>

 \<li\>long option arguments : any argument that starts with a \'--\' \</li\>
 \</ul\>
 option may start with \'!\' to switch armed flags expandSimpleOptions : when
 activated

 

 \@author vpc
 \@since 0.5.5
 \@category Command Line

### 🎛 Instance Properties
#### ✏🎛 arguments
reset this instance with the given arguments
```java
[write-only] NutsCommandLine public arguments
public NutsCommandLine setArguments(arguments)
```
#### 📝🎛 autoComplete
set autocomplete instance
```java
[read-write] NutsCommandLine public autoComplete
public NutsCommandAutoComplete getAutoComplete()
public NutsCommandLine setAutoComplete(autoComplete)
```
#### 📄🎛 autoCompleteMode
true if auto complete instance is registered (is not null)
```java
[read-only] boolean public autoCompleteMode
public boolean isAutoCompleteMode()
```
#### 📝🎛 commandName
set command name that will be used as an extra info in thrown exceptions
```java
[read-write] NutsCommandLine public commandName
public String getCommandName()
public NutsCommandLine setCommandName(commandName)
```
#### 📄🎛 empty
true if no more arguments are available
```java
[read-only] boolean public empty
public boolean isEmpty()
```
#### 📄🎛 execMode
true if auto complete instance is not registered (is null)
```java
[read-only] boolean public execMode
public boolean isExecMode()
```
#### 📝🎛 expandSimpleOptions
enable or disable simple option expansion
```java
[read-write] NutsCommandLine public expandSimpleOptions
public boolean isExpandSimpleOptions()
public NutsCommandLine setExpandSimpleOptions(expand)
```
#### 📄🎛 specialSimpleOptions
list of registered simple options
```java
[read-only] String\[] public specialSimpleOptions
public String\[] getSpecialSimpleOptions()
```
#### 📄🎛 wordIndex
current word index
```java
[read-only] int public wordIndex
public int getWordIndex()
```
### ⚙ Instance Methods
#### ⚙ accept(values)
true if arguments start with the given suite.

```java
boolean accept(String\[] values)
```
**return**:boolean
- **String\[] values** : arguments suite

#### ⚙ accept(index, values)
true if arguments start at index \{\@code index\} with the given suite.

```java
boolean accept(int index, String\[] values)
```
**return**:boolean
- **int index** : starting index
- **String\[] values** : arguments suite

#### ⚙ contains(name)
return true if any argument is equal to the given name

```java
boolean contains(String name)
```
**return**:boolean
- **String name** : argument name

#### ⚙ find(name)
find first argument with argument key name

```java
NutsArgument find(String name)
```
**return**:NutsArgument
- **String name** : argument key name

#### ⚙ get(index)
return argument at given index

```java
NutsArgument get(int index)
```
**return**:NutsArgument
- **int index** : argument index

#### ⚙ hasNext()
true if there still at least one argument to consume

```java
boolean hasNext()
```
**return**:boolean

#### ⚙ indexOf(name)
first  argument index (or -1 if not found) with value \{\@code name\}

```java
int indexOf(String name)
```
**return**:int
- **String name** : argument key name

#### ⚙ isNonOption(index)
true if the argument and index exists and is non option

```java
boolean isNonOption(int index)
```
**return**:boolean
- **int index** : index

#### ⚙ isOption(index)
true if the argument and index exists and is option

```java
boolean isOption(int index)
```
**return**:boolean
- **int index** : index

#### ⚙ isSpecialSimpleOption(option)
test if the option is a registered simple option
 This method helps considering \'-version\' as a single simple options when
 \{\@code isExpandSimpleOptions()==true\}

```java
boolean isSpecialSimpleOption(String option)
```
**return**:boolean
- **String option** : option

#### ⚙ length()
number of arguments available to retrieve

```java
int length()
```
**return**:int

#### ⚙ next()
consume (remove) the first argument and return it return null if not
 argument is left

```java
NutsArgument next()
```
**return**:NutsArgument

#### ⚙ next(name)
consume (remove) the first argument and return it while adding a hint to
 Auto Complete about expected argument candidates return null if not
 argument is left

```java
NutsArgument next(NutsArgumentName name)
```
**return**:NutsArgument
- **NutsArgumentName name** : expected argument name

#### ⚙ next(names)
next argument with any value type (may having not a value). equivalent to
 \{\@code next(NutsArgumentType.ANY,names)\}

```java
NutsArgument next(String\[] names)
```
**return**:NutsArgument
- **String\[] names** : names

#### ⚙ next(expectValue, names)
next argument with any value type (may having not a value).

```java
NutsArgument next(NutsArgumentType expectValue, String\[] names)
```
**return**:NutsArgument
- **NutsArgumentType expectValue** : expected value type
- **String\[] names** : names

#### ⚙ nextBoolean(names)
next argument with boolean value equivalent to
 next(NutsArgumentType.STRING,names)

```java
NutsArgument nextBoolean(String\[] names)
```
**return**:NutsArgument
- **String\[] names** : names

#### ⚙ nextNonOption()
next argument if it exists and it is a non option. Return null in all
 other cases.

```java
NutsArgument nextNonOption()
```
**return**:NutsArgument

#### ⚙ nextNonOption(name)
next argument if it exists and it is a non option. Return null in all
 other cases.

```java
NutsArgument nextNonOption(NutsArgumentName name)
```
**return**:NutsArgument
- **NutsArgumentName name** : argument specification (may be null)

#### ⚙ nextRequiredNonOption(name)
next argument if it exists and it is a non option. Throw an error in all
 other cases.

```java
NutsArgument nextRequiredNonOption(NutsArgumentName name)
```
**return**:NutsArgument
- **NutsArgumentName name** : argument specification (may be null)

#### ⚙ nextString(names)
next argument with string value. equivalent to
 next(NutsArgumentType.STRING,names)

```java
NutsArgument nextString(String\[] names)
```
**return**:NutsArgument
- **String\[] names** : names

#### ⚙ parseLine(commandLine)
reset this instance with the given parsed arguments

```java
NutsCommandLine parseLine(String commandLine)
```
**return**:NutsCommandLine
- **String commandLine** : to parse

#### ⚙ peek()
the first argument to consume without removing/consuming it or null if
 not argument is left

```java
NutsArgument peek()
```
**return**:NutsArgument

#### ⚙ pushBack(arg)
push back argument so that it will be first to be retrieved (using next methods)

```java
NutsCommandLine pushBack(NutsArgument arg)
```
**return**:NutsCommandLine
- **NutsArgument arg** : argument

#### ⚙ registerSpecialSimpleOption(option)
register \{\@code options\} as simple (with simple \'-\') option.
 This method helps considering \'-version\' as a single simple options when
 \{\@code isExpandSimpleOptions()==true\}

```java
NutsCommandLine registerSpecialSimpleOption(String option)
```
**return**:NutsCommandLine
- **String option** : option

#### ⚙ requireNonOption()
throw exception if command line is empty or the first word is an option

```java
NutsCommandLine requireNonOption()
```
**return**:NutsCommandLine

#### ⚙ required()
throw exception if command line is empty

```java
NutsCommandLine required()
```
**return**:NutsCommandLine

#### ⚙ required(errorMessage)
throw exception if command line is empty

```java
NutsCommandLine required(String errorMessage)
```
**return**:NutsCommandLine
- **String errorMessage** : message to throw

#### ⚙ skip()
skip next argument

```java
int skip()
```
**return**:int

#### ⚙ skip(count)
consume \{\@code count\} words and return how much it was able to consume

```java
int skip(int count)
```
**return**:int
- **int count** : count

#### ⚙ skipAll()
consume all words and return consumed count

```java
int skipAll()
```
**return**:int

#### ⚙ toArray()
returns un-parsed (or partially parsed) available arguments

```java
String\[] toArray()
```
**return**:String\[]

#### ⚙ unexpectedArgument()
throw exception if command line is not empty

```java
NutsCommandLine unexpectedArgument()
```
**return**:NutsCommandLine

#### ⚙ unexpectedArgument(errorMessage)
throw exception if command line is not empty

```java
NutsCommandLine unexpectedArgument(String errorMessage)
```
**return**:NutsCommandLine
- **String errorMessage** : message to throw

#### ⚙ unregisterSpecialSimpleOption(option)
unregister \{\@code options\} as simple (with simple \'-\') option.
 This method helps considering \'-version\' as a single simple options when
 \{\@code isExpandSimpleOptions()==true\}

```java
NutsCommandLine unregisterSpecialSimpleOption(String option)
```
**return**:NutsCommandLine
- **String option** : option

## ☕ NutsCommandLineFormat
```java
public interface net.vpc.app.nuts.NutsCommandLineFormat
```
 Simple Command line Format

 \@author vpc
 \@since 0.5.7
 \@category Command Line

### 🎛 Instance Properties
#### ✏🎛 session
update session
```java
[write-only] NutsCommandLineFormat public session
public NutsCommandLineFormat setSession(session)
```
#### 📝🎛 value
set command line from parsed string
```java
[read-write] NutsCommandLineFormat public value
public NutsCommandLine getValue()
public NutsCommandLineFormat setValue(args)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsCommandLineFormat configure(boolean skipUnsupported, String\[] args)
```
**return**:NutsCommandLineFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String\[] args** : argument to configure with

#### ⚙ create(args)
return new Command line instance

```java
NutsCommandLine create(String\[] args)
```
**return**:NutsCommandLine
- **String\[] args** : command line args

#### ⚙ create(args)
return new Command line instance

```java
NutsCommandLine create(List args)
```
**return**:NutsCommandLine
- **List args** : command line args

#### ⚙ createArgument(argument)
create new argument

```java
NutsArgument createArgument(String argument)
```
**return**:NutsArgument
- **String argument** : new argument

#### ⚙ createCandidate(value)
create argument candidate

```java
NutsArgumentCandidate createCandidate(String value)
```
**return**:NutsArgumentCandidate
- **String value** : candidate value

#### ⚙ createCandidate(value, label)
create argument candidate

```java
NutsArgumentCandidate createCandidate(String value, String label)
```
**return**:NutsArgumentCandidate
- **String value** : candidate value
- **String label** : candidate label

#### ⚙ createName(type)
create argument name

```java
NutsArgumentName createName(String type)
```
**return**:NutsArgumentName
- **String type** : create argument type

#### ⚙ createName(type, label)
create argument name

```java
NutsArgumentName createName(String type, String label)
```
**return**:NutsArgumentName
- **String type** : argument type
- **String label** : argument label

#### ⚙ parse(line)
return new Command line instance

```java
NutsCommandLine parse(String line)
```
**return**:NutsCommandLine
- **String line** : command line to parse

## ☕ NutsCommandLineProcessor
```java
public interface net.vpc.app.nuts.NutsCommandLineProcessor
```

 \@category Command Line

### ⚙ Instance Methods
#### ⚙ exec()
execute options, called after all options was processed and
 cmdLine.isExecMode() return true.

```java
void exec()
```

#### ⚙ processNonOption(argument, cmdLine)
process the given non option argument that was peeked from the command line.
 Implementations \<strong\>MUST\</strong\> call one of
 the "next" methods to

```java
boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine)
```
**return**:boolean
- **NutsArgument argument** : peeked argument
- **NutsCommandLine cmdLine** : associated commandline

#### ⚙ processOption(argument, cmdLine)
process the given option argument that was peeked from the command line.
 Implementations \<strong\>MUST\</strong\> call one of
 the "next" methods to

```java
boolean processOption(NutsArgument argument, NutsCommandLine cmdLine)
```
**return**:boolean
- **NutsArgument argument** : peeked argument
- **NutsCommandLine cmdLine** : associated commandline

## ☕ NutsConfigurable
```java
public interface net.vpc.app.nuts.NutsConfigurable
```
 Configurable interface define a extensible way to configure nuts commands
 and objects using simple argument line options.
 \@author vpc
 \@since 0.5.5
 \@category Command Line

### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments.

```java
Object configure(boolean skipUnsupported, String\[] args)
```
**return**:Object
- **boolean skipUnsupported** : when true, all unsupported options are skipped
 silently
- **String\[] args** : arguments to configure with

#### ⚙ configure(skipUnsupported, commandLine)
configure the current command with the given arguments.

```java
boolean configure(boolean skipUnsupported, NutsCommandLine commandLine)
```
**return**:boolean
- **boolean skipUnsupported** : when true, all unsupported options are skipped
 silently
- **NutsCommandLine commandLine** : arguments to configure with

#### ⚙ configureFirst(commandLine)
ask \{\@code this\} instance to configure with the very first argument of
 \{\@code commandLine\}. If the first argument is not supported, return
 \{\@code false\} and consume (skip/read) the argument. If the argument
 required one or more parameters, these arguments are also consumed and
 finally return \{\@code true\}

```java
boolean configureFirst(NutsCommandLine commandLine)
```
**return**:boolean
- **NutsCommandLine commandLine** : arguments to configure with

