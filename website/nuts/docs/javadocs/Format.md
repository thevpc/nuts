---
id: javadoc_Format
title: Format
sidebar_label: Format
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsDependencyFormat
```java
public interface net.vpc.app.nuts.NutsDependencyFormat
```
 Dependency Format Helper
 \@author vpc
 \@since 0.5.6
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 highlightImportedGroup
if true omit (do not include) name space when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public highlightImportedGroup
public boolean isHighlightImportedGroup()
public NutsDependencyFormat setHighlightImportedGroup(highlightImportedGroup)
```
#### 📝🎛 highlightOptional
if true omit (do not include) name space when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public highlightOptional
public boolean isHighlightOptional()
public NutsDependencyFormat setHighlightOptional(highlightOptional)
```
#### 📝🎛 highlightScope
if true omit (do not include) name space when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public highlightScope
public boolean isHighlightScope()
public NutsDependencyFormat setHighlightScope(highlightScope)
```
#### 📝🎛 omitClassifier
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitClassifier
public boolean isOmitClassifier()
public NutsDependencyFormat setOmitClassifier(value)
```
#### 📝🎛 omitExclusions
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitExclusions
public boolean isOmitExclusions()
public NutsDependencyFormat setOmitExclusions(value)
```
#### 📝🎛 omitGroupId
if true omit (do not include) group when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitGroupId
public boolean isOmitGroupId()
public NutsDependencyFormat setOmitGroupId(omitGroup)
```
#### ✏🎛 omitImportedGroup
if true omit (do not include) group (if the group is imported) when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[write-only] NutsDependencyFormat public omitImportedGroup
public NutsDependencyFormat setOmitImportedGroup(omitEnv)
```
#### 📄🎛 omitImportedGroupId
omit imported group
```java
[read-only] boolean public omitImportedGroupId
public boolean isOmitImportedGroupId()
```
#### 📝🎛 omitNamespace
if true omit (do not include) namespace when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitNamespace
public boolean isOmitNamespace()
public NutsDependencyFormat setOmitNamespace(omitNamespace)
```
#### 📝🎛 omitOptional
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitOptional
public boolean isOmitOptional()
public NutsDependencyFormat setOmitOptional(value)
```
#### 📝🎛 omitOtherProperties
if true omit (do not include) query (scope and optional) when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitOtherProperties
public boolean isOmitOtherProperties()
public NutsDependencyFormat setOmitOtherProperties(value)
```
#### 📄🎛 omitQueryProperties
list of all omitted query properties
```java
[read-only] String[] public omitQueryProperties
public String[] getOmitQueryProperties()
```
#### 📝🎛 omitScope
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .
```java
[read-write] NutsDependencyFormat public omitScope
public boolean isOmitScope()
public NutsDependencyFormat setOmitScope(value)
```
#### ✏🎛 session
update session
```java
[write-only] NutsDependencyFormat public session
public NutsDependencyFormat setSession(session)
```
#### 📝🎛 value
value dependency to format
```java
[read-write] NutsDependencyFormat public value
public NutsDependency getValue()
public NutsDependencyFormat setValue(dependency)
```
### ⚙ Instance Methods
#### ⚙ builder()
return mutable id builder instance initialized with \{\@code this\} instance.

```java
NutsDependencyBuilder builder()
```
**return**:NutsDependencyBuilder

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsDependencyFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsDependencyFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ isOmitQueryProperty(name)
return true if omit query property named \{\@code name\}

```java
boolean isOmitQueryProperty(String name)
```
**return**:boolean
- **String name** : property name

#### ⚙ parse(dependency)
parse dependency in the form
 namespace://group:name#version?scope=&lt;scope&gt;\{\@code &\}optional=&lt;optional&gt;
 If the string cannot be evaluated, return null.

```java
NutsDependency parse(String dependency)
```
**return**:NutsDependency
- **String dependency** : dependency

#### ⚙ parseRequired(dependency)
parse dependency in the form
 namespace://group:name#version?scope=&lt;scope&gt;\{\@code &\}optional=&lt;optional&gt;
 If the string cannot be evaluated, return null.

```java
NutsDependency parseRequired(String dependency)
```
**return**:NutsDependency
- **String dependency** : dependency

#### ⚙ setOmitQueryProperty(name, value)
if true omit (do not include) query property named \{\@code name\} when formatting the value
 set using \{\@link #setValue(NutsDependency)\} .

```java
NutsDependencyFormat setOmitQueryProperty(String name, boolean value)
```
**return**:NutsDependencyFormat
- **String name** : property name
- **boolean value** : new value

## ☕ NutsDescriptorFormat
```java
public interface net.vpc.app.nuts.NutsDescriptorFormat
```
 Descriptor Format class that help building, formatting and parsing Descriptors.
 \@author vpc
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 compact
value compact flag.
 When true, formatted Descriptor will compact JSON result.
```java
[read-write] NutsDescriptorFormat public compact
public boolean isCompact()
public NutsDescriptorFormat setCompact(compact)
```
### ⚙ Instance Methods
#### ⚙ callBuilder()
create executor builder.

```java
NutsArtifactCallBuilder callBuilder()
```
**return**:NutsArtifactCallBuilder

#### ⚙ classifierBuilder()
create classifier mappings builder.

```java
NutsClassifierMappingBuilder classifierBuilder()
```
**return**:NutsClassifierMappingBuilder

#### ⚙ compact()
value compact flag to true.
 When true, formatted Descriptor will compact JSON result.

```java
NutsDescriptorFormat compact()
```
**return**:NutsDescriptorFormat

#### ⚙ compact(compact)
value compact flag.
 When true, formatted Descriptor will compact JSON result.

```java
NutsDescriptorFormat compact(boolean compact)
```
**return**:NutsDescriptorFormat
- **boolean compact** : compact value

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsDescriptorFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsDescriptorFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ descriptorBuilder()
create descriptor builder.

```java
NutsDescriptorBuilder descriptorBuilder()
```
**return**:NutsDescriptorBuilder

#### ⚙ locationBuilder()
create descriptor builder.

```java
NutsIdLocationBuilder locationBuilder()
```
**return**:NutsIdLocationBuilder

#### ⚙ parse(bytes)
parse descriptor.

```java
NutsDescriptor parse(byte[] bytes)
```
**return**:NutsDescriptor
- **byte[] bytes** : value to parse

#### ⚙ parse(descriptorString)
parse descriptor.

```java
NutsDescriptor parse(String descriptorString)
```
**return**:NutsDescriptor
- **String descriptorString** : string to parse

#### ⚙ parse(file)
parse descriptor.

```java
NutsDescriptor parse(File file)
```
**return**:NutsDescriptor
- **File file** : file to parse

#### ⚙ parse(path)
parse descriptor.

```java
NutsDescriptor parse(Path path)
```
**return**:NutsDescriptor
- **Path path** : path to parse

#### ⚙ parse(stream)
parse descriptor.

```java
NutsDescriptor parse(InputStream stream)
```
**return**:NutsDescriptor
- **InputStream stream** : stream to parse

#### ⚙ parse(url)
parse descriptor.

```java
NutsDescriptor parse(URL url)
```
**return**:NutsDescriptor
- **URL url** : URL to parse

#### ⚙ value(descriptor)
set the descriptor instance to print

```java
NutsDescriptorFormat value(NutsDescriptor descriptor)
```
**return**:NutsDescriptorFormat
- **NutsDescriptor descriptor** : value to format

## ☕ NutsElementFormat
```java
public interface net.vpc.app.nuts.NutsElementFormat
```
 Class responsible of manipulating \{\@link NutsElement\} type. It help parsing
 from, converting to and formatting such types.

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### ✏🎛 session
set current session.
```java
[write-only] NutsElementFormat public session
public NutsElementFormat setSession(session)
```
#### 📝🎛 value
set current value to format.
```java
[read-write] NutsElementFormat public value
public Object getValue()
public NutsElementFormat setValue(value)
```
### ⚙ Instance Methods
#### ⚙ builder()
element builder

```java
NutsElementBuilder builder()
```
**return**:NutsElementBuilder

#### ⚙ compilePath(pathExpression)
compile pathExpression into a valid NutsElementPath that helps filtering
 elements tree.
 JSONPath expressions refer to a JSON structure the same way as XPath expression are used with XML documents. 
 JSONPath expressions can use the dot notation and/or bracket  notations
  .store.book[0].title
  The trailing root is not necessary : 
  .store.book[0].title
  You can also use  bracket notation
  store[\'book\'][0].title
  for input paths.

```java
NutsElementPath compilePath(String pathExpression)
```
**return**:NutsElementPath
- **String pathExpression** : element path expression

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsElementFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsElementFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ fromElement(element, clazz)
convert element to the specified object if applicable or throw an
 exception.

```java
Object fromElement(NutsElement element, Class clazz)
```
**return**:Object
- **NutsElement element** : element to convert
- **Class clazz** : class type

#### ⚙ set(value)
set current value to format.

```java
NutsElementFormat set(Object value)
```
**return**:NutsElementFormat
- **Object value** : value to format

#### ⚙ toElement(object)
convert any object to valid \{\@link NutsElement\}.

```java
NutsElement toElement(Object object)
```
**return**:NutsElement
- **Object object** : object to convert

## ☕ NutsExecCommandFormat
```java
public interface net.vpc.app.nuts.NutsExecCommandFormat
```
 Format used to format command line by \{\@link NutsExecCommand\}

 \@author vpc
 \@see NutsExecCommand#format()
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 argumentFilter
set arg filter
```java
[read-write] NutsExecCommandFormat public argumentFilter
public Predicate getArgumentFilter()
public NutsExecCommandFormat setArgumentFilter(filter)
```
#### 📝🎛 argumentReplacer
set arg replacer
```java
[read-write] NutsExecCommandFormat public argumentReplacer
public Function getArgumentReplacer()
public NutsExecCommandFormat setArgumentReplacer(replacer)
```
#### 📝🎛 envFilter
set env filter
```java
[read-write] NutsExecCommandFormat public envFilter
public Predicate getEnvFilter()
public NutsExecCommandFormat setEnvFilter(filter)
```
#### 📝🎛 envReplacer
set env replacer
```java
[read-write] NutsExecCommandFormat public envReplacer
public Function getEnvReplacer()
public NutsExecCommandFormat setEnvReplacer(replacer)
```
#### 📝🎛 redirectError
if true error redirection is displayed
```java
[read-write] NutsExecCommandFormat public redirectError
public boolean isRedirectError()
public NutsExecCommandFormat setRedirectError(redirectError)
```
#### 📝🎛 redirectInput
if true input redirection is displayed
```java
[read-write] NutsExecCommandFormat public redirectInput
public boolean isRedirectInput()
public NutsExecCommandFormat setRedirectInput(redirectInput)
```
#### 📝🎛 redirectOutput
if true output redirection is displayed
```java
[read-write] NutsExecCommandFormat public redirectOutput
public boolean isRedirectOutput()
public NutsExecCommandFormat setRedirectOutput(redirectOutput)
```
#### 📝🎛 value
set value to format
```java
[read-write] NutsExecCommandFormat public value
public NutsExecCommand getValue()
public NutsExecCommandFormat setValue(value)
```
### ⚙ Instance Methods
#### ⚙ value(value)
set value to format

```java
NutsExecCommandFormat value(NutsExecCommand value)
```
**return**:NutsExecCommandFormat
- **NutsExecCommand value** : value to format

## ☕ NutsFormat
```java
public interface net.vpc.app.nuts.NutsFormat
```
 Base Format Interface used to print "things".
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 session
update session
```java
[read-write] NutsFormat public session
public NutsSession getSession()
public NutsFormat setSession(session)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ format()
format current value and return the string result

```java
String format()
```
**return**:String

#### ⚙ print()
format current value and write result to \{\@code getSession().out()\}.

```java
void print()
```

#### ⚙ print(out)
format current value and write result to \{\@code out\}

```java
void print(PrintStream out)
```
- **PrintStream out** : recipient print stream

#### ⚙ print(out)
format current value and write result to \{\@code out\}

```java
void print(Writer out)
```
- **Writer out** : recipient writer

#### ⚙ print(out)
format current value and write result to \{\@code out\}

```java
void print(OutputStream out)
```
- **OutputStream out** : recipient writer

#### ⚙ print(out)
format current value and write result to \{\@code out\}

```java
void print(Path out)
```
- **Path out** : recipient path

#### ⚙ print(out)
format current value and write result to \{\@code out\}

```java
void print(File out)
```
- **File out** : recipient file

#### ⚙ print(terminal)
format current value and write result to \{\@code terminal\}

```java
void print(NutsTerminal terminal)
```
- **NutsTerminal terminal** : recipient terminal

#### ⚙ println()
format current value and write result to \{\@code getSession().out()\} and
 finally appends a new line.

```java
void println()
```

#### ⚙ println(file)
format current value and write result to \{\@code out\} and finally appends
 a new line.

```java
void println(File file)
```
- **File file** : recipient file

#### ⚙ println(out)
format current value and write result to \{\@code out\} and finally appends
 a new line.

```java
void println(Writer out)
```
- **Writer out** : recipient

#### ⚙ println(out)
format current value and write result to \{\@code out\} and finally appends
 a new line.

```java
void println(PrintStream out)
```
- **PrintStream out** : recipient print stream

#### ⚙ println(out)
format current value and write result to \{\@code out\} and finally appends
 a new line.

```java
void println(Path out)
```
- **Path out** : recipient path

#### ⚙ println(terminal)
format current value and write result to \{\@code terminal\} and finally appends
 a new line.

```java
void println(NutsTerminal terminal)
```
- **NutsTerminal terminal** : recipient terminal

#### ⚙ toString()
equivalent to \{\@link #format() \}

```java
String toString()
```
**return**:String

## ☕ NutsIdFormat
```java
public interface net.vpc.app.nuts.NutsIdFormat
```
 Class responsible of manipulating  \{\@link NutsId\} instances:
 \<ul\>
     \<li\>formatting (in Nuts Stream Format)\</li\>
     \<li\>parsing\</li\>
 \</ul\>
 \@author vpc
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 highlightImportedGroupId
update highlightImportedGroupId
```java
[read-write] NutsIdFormat public highlightImportedGroupId
public boolean isHighlightImportedGroupId()
public NutsIdFormat setHighlightImportedGroupId(value)
```
#### 📝🎛 highlightOptional
update highlightOptional
```java
[read-write] NutsIdFormat public highlightOptional
public boolean isHighlightOptional()
public NutsIdFormat setHighlightOptional(value)
```
#### 📝🎛 highlightScope
update highlightScope
```java
[read-write] NutsIdFormat public highlightScope
public boolean isHighlightScope()
public NutsIdFormat setHighlightScope(value)
```
#### 📝🎛 omitClassifier
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsId)\} .
```java
[read-write] NutsIdFormat public omitClassifier
public boolean isOmitClassifier()
public NutsIdFormat setOmitClassifier(value)
```
#### 📝🎛 omitFace
update omitFace
```java
[read-write] NutsIdFormat public omitFace
public boolean isOmitFace()
public NutsIdFormat setOmitFace(value)
```
#### 📝🎛 omitGroupId
update omitGroup
```java
[read-write] NutsIdFormat public omitGroupId
public boolean isOmitGroupId()
public NutsIdFormat setOmitGroupId(value)
```
#### 📝🎛 omitImportedGroupId
update omitImportedGroupId
```java
[read-write] NutsIdFormat public omitImportedGroupId
public boolean isOmitImportedGroupId()
public NutsIdFormat setOmitImportedGroupId(value)
```
#### 📝🎛 omitNamespace
update omitNamespace
```java
[read-write] NutsIdFormat public omitNamespace
public boolean isOmitNamespace()
public NutsIdFormat setOmitNamespace(value)
```
#### 📝🎛 omitOtherProperties
update omitOtherProperties
```java
[read-write] NutsIdFormat public omitOtherProperties
public boolean isOmitOtherProperties()
public NutsIdFormat setOmitOtherProperties(value)
```
#### 📄🎛 omitProperties
query properties omitted
```java
[read-only] String[] public omitProperties
public String[] getOmitProperties()
```
#### ✏🎛 session
update session
```java
[write-only] NutsIdFormat public session
public NutsIdFormat setSession(session)
```
#### 📝🎛 value
id to format
```java
[read-write] NutsIdFormat public value
public NutsId getValue()
public NutsIdFormat setValue(id)
```
### ⚙ Instance Methods
#### ⚙ builder()
create new instance of id builder

```java
NutsIdBuilder builder()
```
**return**:NutsIdBuilder

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsIdFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsIdFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ highlightImportedGroupId()
update highlightImportedGroupId to true

```java
NutsIdFormat highlightImportedGroupId()
```
**return**:NutsIdFormat

#### ⚙ highlightImportedGroupId(value)
update highlightImportedGroupId

```java
NutsIdFormat highlightImportedGroupId(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ highlightOptional()
update highlightOptional tot true

```java
NutsIdFormat highlightOptional()
```
**return**:NutsIdFormat

#### ⚙ highlightOptional(value)
update highlightOptional

```java
NutsIdFormat highlightOptional(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ highlightScope()
update highlightScope to true

```java
NutsIdFormat highlightScope()
```
**return**:NutsIdFormat

#### ⚙ highlightScope(value)
update highlightScope

```java
NutsIdFormat highlightScope(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ isOmitProperty(name)
return true if omit query property named \{\@code name\}

```java
boolean isOmitProperty(String name)
```
**return**:boolean
- **String name** : property name

#### ⚙ omitClassifier()
omit scope

```java
NutsIdFormat omitClassifier()
```
**return**:NutsIdFormat

#### ⚙ omitClassifier(value)
if true omit (do not include) face when formatting the value
 set using \{\@link #setValue(NutsId)\} .

```java
NutsIdFormat omitClassifier(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : new value

#### ⚙ omitFace()
update omitFace to true

```java
NutsIdFormat omitFace()
```
**return**:NutsIdFormat

#### ⚙ omitFace(value)
update omitFace

```java
NutsIdFormat omitFace(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ omitGroupId()
update omitGroup to true

```java
NutsIdFormat omitGroupId()
```
**return**:NutsIdFormat

#### ⚙ omitGroupId(value)
update omitGroup

```java
NutsIdFormat omitGroupId(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : new value

#### ⚙ omitImportedGroupId()
update omitImportedGroupId to ture

```java
NutsIdFormat omitImportedGroupId()
```
**return**:NutsIdFormat

#### ⚙ omitImportedGroupId(value)
update omitImportedGroupId

```java
NutsIdFormat omitImportedGroupId(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ omitNamespace()
update omitNamespace to true

```java
NutsIdFormat omitNamespace()
```
**return**:NutsIdFormat

#### ⚙ omitNamespace(value)
update omitNamespace

```java
NutsIdFormat omitNamespace(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : true when the namespace should not be included in formatted instance

#### ⚙ omitOtherProperties()
update omitOtherProperties to true

```java
NutsIdFormat omitOtherProperties()
```
**return**:NutsIdFormat

#### ⚙ omitOtherProperties(value)
update omitOtherProperties

```java
NutsIdFormat omitOtherProperties(boolean value)
```
**return**:NutsIdFormat
- **boolean value** : value

#### ⚙ omitProperty(name)
omit query property named \{\@code name\}

```java
NutsIdFormat omitProperty(String name)
```
**return**:NutsIdFormat
- **String name** : property name

#### ⚙ omitProperty(name, value)
if true omit (do not include) query property named \{\@code name\} when formatting the value
 set using \{\@link #setValue(NutsId)\} .

```java
NutsIdFormat omitProperty(String name, boolean value)
```
**return**:NutsIdFormat
- **String name** : property name
- **boolean value** : new value

#### ⚙ parse(id)
parse id or null if not valid.
 id is parsed in the form
 namespace://group:name#version?key=&lt;value&gt;\{\@code &\}key=&lt;value&gt; ...

```java
NutsId parse(String id)
```
**return**:NutsId
- **String id** : to parse

#### ⚙ parseRequired(id)
parse id or error if not valid

```java
NutsId parseRequired(String id)
```
**return**:NutsId
- **String id** : to parse

#### ⚙ resolveId(clazz)
detect nuts id from resources containing the given class
 or null if not found. If multiple resolutions return the first.

```java
NutsId resolveId(Class clazz)
```
**return**:NutsId
- **Class clazz** : to search for

#### ⚙ resolveIds(clazz)
detect all nuts ids from resources containing the given class.

```java
NutsId[] resolveIds(Class clazz)
```
**return**:NutsId[]
- **Class clazz** : to search for

#### ⚙ set(id)
id to format

```java
NutsIdFormat set(NutsId id)
```
**return**:NutsIdFormat
- **NutsId id** : id to format

#### ⚙ setOmitProperty(name, value)
if true omit (do not include) query property named \{\@code name\} when formatting the value
 set using \{\@link #setValue(NutsId)\} .

```java
NutsIdFormat setOmitProperty(String name, boolean value)
```
**return**:NutsIdFormat
- **String name** : property name
- **boolean value** : new value

#### ⚙ value(id)
set id to format

```java
NutsIdFormat value(NutsId id)
```
**return**:NutsIdFormat
- **NutsId id** : id to format

## ☕ NutsInfoFormat
```java
public interface net.vpc.app.nuts.NutsInfoFormat
```
 this class is responsible of displaying general information about the current workspace and repositories.
 It is invoked by the "info" standard commmad,
 \@author vpc
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 fancy
enable fancy (custom, pretty) display mode
```java
[read-write] NutsInfoFormat public fancy
public boolean isFancy()
public NutsInfoFormat setFancy(fancy)
```
#### ✏🎛 session
update session
```java
[write-only] NutsInfoFormat public session
public NutsInfoFormat setSession(session)
```
#### 📝🎛 showRepositories
enable or disable display of all repositories information
```java
[read-write] NutsInfoFormat public showRepositories
public boolean isShowRepositories()
public NutsInfoFormat setShowRepositories(enable)
```
### ⚙ Instance Methods
#### ⚙ addProperties(customProperties)
include custom properties from the given map

```java
NutsInfoFormat addProperties(Map customProperties)
```
**return**:NutsInfoFormat
- **Map customProperties** : custom properties

#### ⚙ addProperty(key, value)
include a custom property

```java
NutsInfoFormat addProperty(String key, String value)
```
**return**:NutsInfoFormat
- **String key** : custom property key
- **String value** : custom property value

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsInfoFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsInfoFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ showRepositories()
enable display of all repositories information

```java
NutsInfoFormat showRepositories()
```
**return**:NutsInfoFormat

#### ⚙ showRepositories(enable)
enable or disable display of all repositories information

```java
NutsInfoFormat showRepositories(boolean enable)
```
**return**:NutsInfoFormat
- **boolean enable** : if true enable

## ☕ NutsIterableFormat
```java
public interface net.vpc.app.nuts.NutsIterableFormat
```
 This class handles formatting of iterable items in Search.
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 outputFormat
Current output format
```java
[read-only] NutsOutputFormat public outputFormat
public NutsOutputFormat getOutputFormat()
```
### ⚙ Instance Methods
#### ⚙ complete(count)
called at the iteration completing

```java
void complete(long count)
```
- **long count** : iterated items count

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsIterableFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsIterableFormat
- **boolean skipUnsupported** : 
- **String[] args** : argument to configure with

#### ⚙ next(object, index)
called at each new item visited

```java
void next(Object object, long index)
```
- **Object object** : visited item
- **long index** : visited item index

#### ⚙ start()
called at the iteration start

```java
void start()
```

## ☕ NutsIterableOutput
```java
public interface net.vpc.app.nuts.NutsIterableOutput
```
 Classes implementing this interface are responsible of printing objects in multiple format
 using \{\@link NutsIterableFormat\}.
 TODO : should merge with NutsIterableFormat
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### ✏🎛 out
configure out c
```java
[write-only] NutsIterableOutput public out
public NutsIterableOutput setOut(out)
```
#### ✏🎛 session
configure session
```java
[write-only] NutsIterableOutput public session
public NutsIterableOutput setSession(session)
```
### ⚙ Instance Methods
#### ⚙ complete()
called at the iteration completing

```java
void complete()
```

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsIterableOutput configure(boolean skipUnsupported, String[] args)
```
**return**:NutsIterableOutput
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ next(object)
called at the each visited item

```java
void next(Object object)
```
- **Object object** : visited item

#### ⚙ out(out)
configure out stream

```java
NutsIterableOutput out(PrintStream out)
```
**return**:NutsIterableOutput
- **PrintStream out** : out stream

#### ⚙ out(out)
configure out stream

```java
NutsIterableOutput out(PrintWriter out)
```
**return**:NutsIterableOutput
- **PrintWriter out** : out stream

#### ⚙ start()
called at the iteration start

```java
void start()
```

## ☕ NutsJsonFormat
```java
public interface net.vpc.app.nuts.NutsJsonFormat
```
 Implementation of this interface will provide
 simple mechanism to write json text from given object.
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 compact
enable or disable compact json
```java
[read-write] NutsJsonFormat public compact
public boolean isCompact()
public NutsJsonFormat setCompact(compact)
```
#### ✏🎛 session
update session
```java
[write-only] NutsJsonFormat public session
public NutsJsonFormat setSession(session)
```
#### 📝🎛 value
return value to format
```java
[read-write] NutsJsonFormat public value
public Object getValue()
public NutsJsonFormat setValue(value)
```
### ⚙ Instance Methods
#### ⚙ compact()
enable compact json

```java
NutsJsonFormat compact()
```
**return**:NutsJsonFormat

#### ⚙ compact(compact)
enable or disable compact json

```java
NutsJsonFormat compact(boolean compact)
```
**return**:NutsJsonFormat
- **boolean compact** : enable when true

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsJsonFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsJsonFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ parse(bytes, clazz)
parse bytes as a valid object of the given type

```java
Object parse(byte[] bytes, Class clazz)
```
**return**:Object
- **byte[] bytes** : source bytes
- **Class clazz** : target type

#### ⚙ parse(file, clazz)
parse file as a valid object of the given type

```java
Object parse(Path file, Class clazz)
```
**return**:Object
- **Path file** : source url
- **Class clazz** : target type

#### ⚙ parse(file, clazz)
parse file as a valid object of the given type

```java
Object parse(File file, Class clazz)
```
**return**:Object
- **File file** : source url
- **Class clazz** : target type

#### ⚙ parse(inputStream, clazz)
parse inputStream as a valid object of the given type

```java
Object parse(InputStream inputStream, Class clazz)
```
**return**:Object
- **InputStream inputStream** : source inputStream
- **Class clazz** : target type

#### ⚙ parse(jsonString, clazz)
parse inputStream as a valid object of the given type

```java
Object parse(String jsonString, Class clazz)
```
**return**:Object
- **String jsonString** : source as json string
- **Class clazz** : target type

#### ⚙ parse(reader, clazz)
parse reader as a valid object of the given type

```java
Object parse(Reader reader, Class clazz)
```
**return**:Object
- **Reader reader** : source reader
- **Class clazz** : target type

#### ⚙ parse(url, clazz)
parse url as a valid object of the given type

```java
Object parse(URL url, Class clazz)
```
**return**:Object
- **URL url** : source url
- **Class clazz** : target type

#### ⚙ value(value)


```java
NutsJsonFormat value(Object value)
```
**return**:NutsJsonFormat
- **Object value** : value to format

## ☕ NutsMutableTableModel
```java
public interface net.vpc.app.nuts.NutsMutableTableModel
```
 Mutable Table Model
 \@author vpc
 \@category Format

### ⚙ Instance Methods
#### ⚙ addCell(value)
add row cell

```java
NutsMutableTableModel addCell(Object value)
```
**return**:NutsMutableTableModel
- **Object value** : cell

#### ⚙ addCells(values)
add row cells

```java
NutsMutableTableModel addCells(Object[] values)
```
**return**:NutsMutableTableModel
- **Object[] values** : row cells

#### ⚙ addHeaderCell(value)
add header cell

```java
NutsMutableTableModel addHeaderCell(Object value)
```
**return**:NutsMutableTableModel
- **Object value** : cell

#### ⚙ addHeaderCells(values)
add header cells

```java
NutsMutableTableModel addHeaderCells(Object[] values)
```
**return**:NutsMutableTableModel
- **Object[] values** : cells

#### ⚙ addRow(values)
add row cells

```java
NutsMutableTableModel addRow(Object[] values)
```
**return**:NutsMutableTableModel
- **Object[] values** : row cells

#### ⚙ clearHeader()
clear header

```java
NutsMutableTableModel clearHeader()
```
**return**:NutsMutableTableModel

#### ⚙ newRow()
add new row to the model

```java
NutsMutableTableModel newRow()
```
**return**:NutsMutableTableModel

#### ⚙ setCellColSpan(row, column, value)
update cell colspan

```java
NutsMutableTableModel setCellColSpan(int row, int column, int value)
```
**return**:NutsMutableTableModel
- **int row** : row index
- **int column** : column index
- **int value** : new value

#### ⚙ setCellRowSpan(row, column, value)
update cell rowspan

```java
NutsMutableTableModel setCellRowSpan(int row, int column, int value)
```
**return**:NutsMutableTableModel
- **int row** : row index
- **int column** : column index
- **int value** : new value

#### ⚙ setCellValue(row, column, value)
update cell at the given position

```java
NutsMutableTableModel setCellValue(int row, int column, Object value)
```
**return**:NutsMutableTableModel
- **int row** : row index
- **int column** : column index
- **Object value** : cell value

#### ⚙ setHeaderColSpan(column, value)
update header colspan

```java
NutsMutableTableModel setHeaderColSpan(int column, int value)
```
**return**:NutsMutableTableModel
- **int column** : new value
- **int value** : new value

#### ⚙ setHeaderValue(column, value)
update header value

```java
NutsMutableTableModel setHeaderValue(int column, Object value)
```
**return**:NutsMutableTableModel
- **int column** : header column
- **Object value** : new value

## ☕ NutsNamedElement
```java
public interface net.vpc.app.nuts.NutsNamedElement
```
 Named Element
 \@author vpc
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 name
element name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value
element value
```java
[read-only] NutsElement public value
public NutsElement getValue()
```
## ☕ NutsObjectElementBuilder
```java
public interface net.vpc.app.nuts.NutsObjectElementBuilder
```
 Builder for manipulating \{\@link NutsObjectElement\} instances
 \@author vpc
 \@category Format

### ⚙ Instance Methods
#### ⚙ add(other)
set all properties from the given \{\@code other\} instance.
 all properties not found in \{\@code other\} will be retained.

```java
NutsObjectElementBuilder add(NutsObjectElement other)
```
**return**:NutsObjectElementBuilder
- **NutsObjectElement other** : other instance

#### ⚙ add(other)
set all properties from the given \{\@code other\} instance.
 all properties not found in \{\@code other\} will be retained.

```java
NutsObjectElementBuilder add(NutsObjectElementBuilder other)
```
**return**:NutsObjectElementBuilder
- **NutsObjectElementBuilder other** : other instance

#### ⚙ build()
create a immutable instance of \{\@link NutsObjectElement\} representing
 this builder.

```java
NutsObjectElement build()
```
**return**:NutsObjectElement

#### ⚙ children()
object (key,value) attributes

```java
Collection children()
```
**return**:Collection

#### ⚙ clear()
remove all properties

```java
NutsObjectElementBuilder clear()
```
**return**:NutsObjectElementBuilder

#### ⚙ get(name)
return value for name or null.
 If multiple values are available return any of them.

```java
NutsElement get(String name)
```
**return**:NutsElement
- **String name** : key name

#### ⚙ remove(name)
remove property

```java
NutsObjectElementBuilder remove(String name)
```
**return**:NutsObjectElementBuilder
- **String name** : property name

#### ⚙ set(other)
set all properties from the given \{\@code other\} instance.
 all properties not found in \{\@code other\} will be removed.

```java
NutsObjectElementBuilder set(NutsObjectElement other)
```
**return**:NutsObjectElementBuilder
- **NutsObjectElement other** : other instance

#### ⚙ set(other)
set all properties from the given \{\@code other\} instance.
 all properties not found in \{\@code other\} will be removed.

```java
NutsObjectElementBuilder set(NutsObjectElementBuilder other)
```
**return**:NutsObjectElementBuilder
- **NutsObjectElementBuilder other** : other instance

#### ⚙ set(name, value)
set value for property \{\@code name\}

```java
NutsObjectElementBuilder set(String name, NutsElement value)
```
**return**:NutsObjectElementBuilder
- **String name** : property name
- **NutsElement value** : property value. should not be null

#### ⚙ size()
element count

```java
int size()
```
**return**:int

## ☕ NutsObjectFormat
```java
public interface net.vpc.app.nuts.NutsObjectFormat
```
 Object format is responsible of formatting to terminal
 a given object. Multiple implementation should be available
 to support tables, trees, json, xml,...
 \@author vpc
 \@category Format

### 🎛 Instance Properties
#### ✏🎛 session
update session
```java
[write-only] NutsObjectFormat public session
public NutsObjectFormat setSession(session)
```
#### 📝🎛 value
set value to format
```java
[read-write] NutsObjectFormat public value
public Object getValue()
public NutsObjectFormat setValue(value)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsObjectFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsObjectFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ value(value)
set value to format

```java
NutsObjectFormat value(Object value)
```
**return**:NutsObjectFormat
- **Object value** : value to format

## ☕ NutsOutputFormat
```java
public final net.vpc.app.nuts.NutsOutputFormat
```
 Formats supported by Nuts
 \@author vpc
 \@since 0.5.4
 \@category Format

### 📢❄ Constant Fields
#### 📢❄ JSON
```java
public static final NutsOutputFormat JSON
```
#### 📢❄ PLAIN
```java
public static final NutsOutputFormat PLAIN
```
#### 📢❄ PROPS
```java
public static final NutsOutputFormat PROPS
```
#### 📢❄ TABLE
```java
public static final NutsOutputFormat TABLE
```
#### 📢❄ TREE
```java
public static final NutsOutputFormat TREE
```
#### 📢❄ XML
```java
public static final NutsOutputFormat XML
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsOutputFormat valueOf(String name)
```
**return**:NutsOutputFormat
- **String name** : 

#### 📢⚙ values()


```java
NutsOutputFormat[] values()
```
**return**:NutsOutputFormat[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsPositionType
```java
public final net.vpc.app.nuts.NutsPositionType
```
 Text align constants

 \@author vpc
 \@since 0.5.5
 \@category Format

### 📢❄ Constant Fields
#### 📢❄ CENTER
```java
public static final NutsPositionType CENTER
```
#### 📢❄ FIRST
```java
public static final NutsPositionType FIRST
```
#### 📢❄ HEADER
```java
public static final NutsPositionType HEADER
```
#### 📢❄ LAST
```java
public static final NutsPositionType LAST
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsPositionType valueOf(String name)
```
**return**:NutsPositionType
- **String name** : 

#### 📢⚙ values()


```java
NutsPositionType[] values()
```
**return**:NutsPositionType[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsPropertiesFormat
```java
public interface net.vpc.app.nuts.NutsPropertiesFormat
```
 Class formatting Map/Properties objects
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 model
set model to format
```java
[read-write] NutsPropertiesFormat public model
public Map getModel()
public NutsPropertiesFormat setModel(map)
```
#### 📝🎛 separator
set key/value separator
```java
[read-write] NutsPropertiesFormat public separator
public String getSeparator()
public NutsPropertiesFormat setSeparator(separator)
```
#### ✏🎛 session
update session
```java
[write-only] NutsPropertiesFormat public session
public NutsPropertiesFormat setSession(session)
```
#### 📝🎛 sort
enable/disable key sorting
```java
[read-write] NutsPropertiesFormat public sort
public boolean isSort()
public NutsPropertiesFormat setSort(sort)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsPropertiesFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsPropertiesFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ model(map)
set model to format

```java
NutsPropertiesFormat model(Map map)
```
**return**:NutsPropertiesFormat
- **Map map** : model to format

#### ⚙ separator(separator)
set key/value separator

```java
NutsPropertiesFormat separator(String separator)
```
**return**:NutsPropertiesFormat
- **String separator** : key/value separator

#### ⚙ sort()
enable key sorting

```java
NutsPropertiesFormat sort()
```
**return**:NutsPropertiesFormat

#### ⚙ sort(sort)
enable/disable key sorting

```java
NutsPropertiesFormat sort(boolean sort)
```
**return**:NutsPropertiesFormat
- **boolean sort** : when true enable sorting

## ☕ NutsQuestionFormat
```java
public interface net.vpc.app.nuts.NutsQuestionFormat
```

 \@author vpc
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 defaultValues

```java
[read-only] Object[] public defaultValues
public Object[] getDefaultValues(type, question)
```
### ⚙ Instance Methods
#### ⚙ format(value, question)


```java
String format(Object value, NutsQuestion question)
```
**return**:String
- **Object value** : 
- **NutsQuestion question** : 

## ☕ NutsString
```java
public net.vpc.app.nuts.NutsString
```
 
 \@author vpc
 \@category Format

### 🪄 Constructors
#### 🪄 NutsString(value)


```java
NutsString(String value)
```
- **String value** : 

### 🎛 Instance Properties
#### 📄🎛 value

```java
[read-only] String public value
public String getValue()
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

## ☕ NutsStringFormat
```java
public interface net.vpc.app.nuts.NutsStringFormat
```
 Class responsible of formatting a formatted string.

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 parameters

```java
[read-only] Object[] public parameters
public Object[] getParameters()
```
#### ✏🎛 session
set current session.
```java
[write-only] NutsStringFormat public session
public NutsStringFormat setSession(session)
```
#### 📝🎛 string
set current value to format.
```java
[read-write] NutsStringFormat public string
public String getString()
public NutsStringFormat setString(value)
```
#### 📄🎛 style

```java
[read-only] NutsTextFormatStyle public style
public NutsTextFormatStyle getStyle()
```
### ⚙ Instance Methods
#### ⚙ addParameters(parameters)


```java
NutsStringFormat addParameters(Object[] parameters)
```
**return**:NutsStringFormat
- **Object[] parameters** : 

#### ⚙ append(value, parameters)


```java
NutsStringFormat append(String value, Object[] parameters)
```
**return**:NutsStringFormat
- **String value** : 
- **Object[] parameters** : 

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, String...)
 \}
 to help return a more specific return type;

```java
NutsStringFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsStringFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ of(value, parameters)


```java
NutsStringFormat of(String value, Object[] parameters)
```
**return**:NutsStringFormat
- **String value** : 
- **Object[] parameters** : 

#### ⚙ set(value)
set current value to format.

```java
NutsStringFormat set(String value)
```
**return**:NutsStringFormat
- **String value** : value to format

#### ⚙ setParameters(parameters)


```java
NutsStringFormat setParameters(Object[] parameters)
```
**return**:NutsStringFormat
- **Object[] parameters** : 

#### ⚙ setParameters(parameters)


```java
NutsStringFormat setParameters(List parameters)
```
**return**:NutsStringFormat
- **List parameters** : 

#### ⚙ setStyle(style)


```java
NutsStringFormat setStyle(NutsTextFormatStyle style)
```
**return**:NutsStringFormat
- **NutsTextFormatStyle style** : 

#### ⚙ style(style)


```java
NutsStringFormat style(NutsTextFormatStyle style)
```
**return**:NutsStringFormat
- **NutsTextFormatStyle style** : 

## ☕ NutsTableBordersFormat
```java
public interface net.vpc.app.nuts.NutsTableBordersFormat
```

 \@since 0.5.5
 \@category Format

### ⚙ Instance Methods
#### ⚙ format(s)


```java
String format(Separator s)
```
**return**:String
- **Separator s** : 

## ☕ NutsTableCell
```java
public interface net.vpc.app.nuts.NutsTableCell
```

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 colspan

```java
[read-only] int public colspan
public int getColspan()
```
#### 📄🎛 rowspan

```java
[read-only] int public rowspan
public int getRowspan()
```
#### 📄🎛 value

```java
[read-only] Object public value
public Object getValue()
```
#### 📄🎛 x

```java
[read-only] int public x
public int getX()
```
#### 📄🎛 y

```java
[read-only] int public y
public int getY()
```
### ⚙ Instance Methods
#### ⚙ setColspan(colspan)


```java
NutsTableCell setColspan(int colspan)
```
**return**:NutsTableCell
- **int colspan** : 

#### ⚙ setRowspan(rowspan)


```java
NutsTableCell setRowspan(int rowspan)
```
**return**:NutsTableCell
- **int rowspan** : 

#### ⚙ setValue(value)


```java
NutsTableCell setValue(Object value)
```
**return**:NutsTableCell
- **Object value** : 

## ☕ NutsTableCellFormat
```java
public interface net.vpc.app.nuts.NutsTableCellFormat
```

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 horizontalAlign

```java
[read-only] NutsPositionType public horizontalAlign
public NutsPositionType getHorizontalAlign(row, col, value)
```
#### 📄🎛 verticalAlign

```java
[read-only] NutsPositionType public verticalAlign
public NutsPositionType getVerticalAlign(row, col, value)
```
### ⚙ Instance Methods
#### ⚙ format(row, col, value)


```java
String format(int row, int col, Object value)
```
**return**:String
- **int row** : 
- **int col** : 
- **Object value** : 

## ☕ NutsTableFormat
```java
public interface net.vpc.app.nuts.NutsTableFormat
```

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 border

```java
[read-only] NutsTableBordersFormat public border
public NutsTableBordersFormat getBorder()
```
#### 📄🎛 model

```java
[read-only] NutsTableModel public model
public NutsTableModel getModel()
```
#### ✏🎛 session
update session
```java
[write-only] NutsTableFormat public session
public NutsTableFormat setSession(session)
```
#### 📄🎛 visibleColumn

```java
[read-only] Boolean public visibleColumn
public Boolean getVisibleColumn(col)
```
#### 📄🎛 visibleHeader

```java
[read-only] boolean public visibleHeader
public boolean isVisibleHeader()
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsTableFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsTableFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ createModel()


```java
NutsMutableTableModel createModel()
```
**return**:NutsMutableTableModel

#### ⚙ setBorder(border)


```java
NutsTableFormat setBorder(NutsTableBordersFormat border)
```
**return**:NutsTableFormat
- **NutsTableBordersFormat border** : 

#### ⚙ setCellFormat(formatter)


```java
NutsTableFormat setCellFormat(NutsTableCellFormat formatter)
```
**return**:NutsTableFormat
- **NutsTableCellFormat formatter** : 

#### ⚙ setModel(model)


```java
NutsTableFormat setModel(NutsTableModel model)
```
**return**:NutsTableFormat
- **NutsTableModel model** : 

#### ⚙ setVisibleColumn(col, visible)


```java
NutsTableFormat setVisibleColumn(int col, boolean visible)
```
**return**:NutsTableFormat
- **int col** : 
- **boolean visible** : 

#### ⚙ setVisibleHeader(visibleHeader)


```java
NutsTableFormat setVisibleHeader(boolean visibleHeader)
```
**return**:NutsTableFormat
- **boolean visibleHeader** : 

#### ⚙ unsetVisibleColumn(col)


```java
NutsTableFormat unsetVisibleColumn(int col)
```
**return**:NutsTableFormat
- **int col** : 

## ☕ NutsTableModel
```java
public interface net.vpc.app.nuts.NutsTableModel
```

 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 cellColSpan

```java
[read-only] int public cellColSpan
public int getCellColSpan(row, column)
```
#### 📄🎛 cellRowSpan

```java
[read-only] int public cellRowSpan
public int getCellRowSpan(row, column)
```
#### 📄🎛 cellValue

```java
[read-only] Object public cellValue
public Object getCellValue(row, column)
```
#### 📄🎛 columnsCount

```java
[read-only] int public columnsCount
public int getColumnsCount()
```
#### 📄🎛 headerColSpan

```java
[read-only] int public headerColSpan
public int getHeaderColSpan(column)
```
#### 📄🎛 headerValue

```java
[read-only] Object public headerValue
public Object getHeaderValue(column)
```
#### 📄🎛 rowsCount

```java
[read-only] int public rowsCount
public int getRowsCount()
```
## ☕ NutsTerminalFormat
```java
public interface net.vpc.app.nuts.NutsTerminalFormat
```
 Filtered Terminal Format Helper

 \@see NutsIOManager#getTerminalFormat()
 \@see NutsWorkspace#io()
 \@author vpc
 \@since 0.5.5
 \@category Format

### ⚙ Instance Methods
#### ⚙ escapeText(value)
This method escapes all special characters that are interpreted by
 "nuts print format" o that this exact string is printed on
 such print streams When str is null, an empty string is return

```java
String escapeText(String value)
```
**return**:String
- **String value** : input string

#### ⚙ filterText(value)
this method removes all special "nuts print format" sequences support
 and returns the raw string to be printed on an
 ordinary \{\@link PrintStream\}

```java
String filterText(String value)
```
**return**:String
- **String value** : input string

#### ⚙ formatText(style, format, args)
format string. supports \{\@link Formatter#format(java.lang.String, java.lang.Object...)
 \}
 pattern format and adds NutsString special format to print unfiltered strings.

```java
String formatText(NutsTextFormatStyle style, String format, Object[] args)
```
**return**:String
- **NutsTextFormatStyle style** : format style
- **String format** : format
- **Object[] args** : arguments

#### ⚙ formatText(style, locale, format, args)
format string. supports \{\@link Formatter#format(java.util.Locale, java.lang.String, java.lang.Object...)
 \}
 pattern format and adds NutsString special format to print unfiltered strings.

```java
String formatText(NutsTextFormatStyle style, Locale locale, String format, Object[] args)
```
**return**:String
- **NutsTextFormatStyle style** : style
- **Locale locale** : locale
- **String format** : format
- **Object[] args** : arguments

#### ⚙ isFormatted(out)
true if the stream is not null and could be resolved as Formatted Output
 Stream. If False is returned this does no mean necessarily that the
 stream is not formatted.

```java
boolean isFormatted(OutputStream out)
```
**return**:boolean
- **OutputStream out** : stream to check

#### ⚙ isFormatted(out)
true if the stream is not null and could be resolved as Formatted Output
 Stream. If False is returned this does no mean necessarily that the
 stream is not formatted.

```java
boolean isFormatted(Writer out)
```
**return**:boolean
- **Writer out** : stream to check

#### ⚙ prepare(out)
prepare PrintStream to handle NutsString aware format pattern. If the instance
 already supports Nuts specific pattern it will be returned unmodified.

```java
PrintStream prepare(PrintStream out)
```
**return**:PrintStream
- **PrintStream out** : PrintStream to check

#### ⚙ prepare(out)
prepare PrintWriter to handle %N (escape) format pattern. If the instance
 already supports Nuts specific pattern it will be returned unmodified.

```java
PrintWriter prepare(PrintWriter out)
```
**return**:PrintWriter
- **PrintWriter out** : PrintWriter to check

#### ⚙ textLength(value)


```java
int textLength(String value)
```
**return**:int
- **String value** : 

## ☕ NutsTextFormatStyle
```java
public final net.vpc.app.nuts.NutsTextFormatStyle
```
 
 \@author vpc
 \@category Format

### 📢❄ Constant Fields
#### 📢❄ CSTYLE
```java
public static final NutsTextFormatStyle CSTYLE
```
#### 📢❄ POSITIONAL
```java
public static final NutsTextFormatStyle POSITIONAL
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsTextFormatStyle valueOf(String name)
```
**return**:NutsTextFormatStyle
- **String name** : 

#### 📢⚙ values()


```java
NutsTextFormatStyle[] values()
```
**return**:NutsTextFormatStyle[]

## ☕ NutsTreeFormat
```java
public interface net.vpc.app.nuts.NutsTreeFormat
```
 Tree Format handles terminal output in Tree format.
 It is one of the many formats supported bu nuts such as plain,table, xml, json.
 To use Tree format, given an instance ws of Nuts Workspace you can :
 \<pre\>
     ws.
 \</pre\>
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 linkFormat
update link format
```java
[read-write] NutsTreeFormat public linkFormat
public NutsTreeLinkFormat getLinkFormat()
public NutsTreeFormat setLinkFormat(linkFormat)
```
#### 📝🎛 model
update tree model
```java
[read-write] NutsTreeFormat public model
public NutsTreeModel getModel()
public NutsTreeFormat setModel(tree)
```
#### 📝🎛 nodeFormat
update node format
```java
[read-write] NutsTreeFormat public nodeFormat
public NutsTreeNodeFormat getNodeFormat()
public NutsTreeFormat setNodeFormat(nodeFormat)
```
#### ✏🎛 session
update session
```java
[write-only] NutsTreeFormat public session
public NutsTreeFormat setSession(session)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsTreeFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsTreeFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ linkFormat(linkFormat)
update link format

```java
NutsTreeFormat linkFormat(NutsTreeLinkFormat linkFormat)
```
**return**:NutsTreeFormat
- **NutsTreeLinkFormat linkFormat** : new link format

#### ⚙ model(tree)
update tree model

```java
NutsTreeFormat model(NutsTreeModel tree)
```
**return**:NutsTreeFormat
- **NutsTreeModel tree** : new tree model

#### ⚙ nodeFormat(nodeFormat)
update node format

```java
NutsTreeFormat nodeFormat(NutsTreeNodeFormat nodeFormat)
```
**return**:NutsTreeFormat
- **NutsTreeNodeFormat nodeFormat** : new node format

## ☕ NutsTreeLinkFormat
```java
public interface net.vpc.app.nuts.NutsTreeLinkFormat
```
 Format class responsible of formatting prefix of a tree
 \@author vpc
 \@since 0.5.5
 \@category Format

### ⚙ Instance Methods
#### ⚙ formatChild(type)
return prefix for node child for the given layout

```java
String formatChild(NutsPositionType type)
```
**return**:String
- **NutsPositionType type** : position type

#### ⚙ formatMain(type)
return prefix for node root for the given layout

```java
String formatMain(NutsPositionType type)
```
**return**:String
- **NutsPositionType type** : position type

## ☕ NutsTreeModel
```java
public interface net.vpc.app.nuts.NutsTreeModel
```
 Tree Model to use in tree format
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📄🎛 root
tree node
```java
[read-only] Object public root
public Object getRoot()
```
### ⚙ Instance Methods
#### ⚙ getChildren(node)
return children of the given \{\@code node\}

```java
List getChildren(Object node)
```
**return**:List
- **Object node** : node to retrieve children for

## ☕ NutsTreeNodeFormat
```java
public interface net.vpc.app.nuts.NutsTreeNodeFormat
```
 classes implementing this interface handle formatting of the tree node.
 \@author vpc
 \@since 0.5.5
 \@category Format

### ⚙ Instance Methods
#### ⚙ format(object, depth)
format (transform to rich string) object at the given depth

```java
String format(Object object, int depth)
```
**return**:String
- **Object object** : object to transform
- **int depth** : tree node depth

## ☕ NutsVersionFormat
```java
public interface net.vpc.app.nuts.NutsVersionFormat
```

 \@author vpc
 \@since 0.5.4
 \@category Format

### 🎛 Instance Properties
#### ✏🎛 session
update session
```java
[write-only] NutsVersionFormat public session
public NutsVersionFormat setSession(session)
```
#### 📝🎛 version
set version to print. if null, workspace version will be considered.
```java
[read-write] NutsVersionFormat public version
public NutsVersion getVersion()
public NutsVersionFormat setVersion(version)
```
#### 📄🎛 workspaceVersion
return true if version is null (default). In such case, workspace version
 is considered.
```java
[read-only] boolean public workspaceVersion
public boolean isWorkspaceVersion()
```
### ⚙ Instance Methods
#### ⚙ addProperties(p)


```java
NutsVersionFormat addProperties(Map p)
```
**return**:NutsVersionFormat
- **Map p** : 

#### ⚙ addProperty(key, value)


```java
NutsVersionFormat addProperty(String key, String value)
```
**return**:NutsVersionFormat
- **String key** : 
- **String value** : 

#### ⚙ parse(version)
return version instance representing the \{\@code version\} string

```java
NutsVersion parse(String version)
```
**return**:NutsVersion
- **String version** : string (may be null)

## ☕ NutsWorkspaceOptionsFormat
```java
public net.vpc.app.nuts.NutsWorkspaceOptionsFormat
```
 \@author vpc
 \@category Format

### 🪄 Constructors
#### 🪄 NutsWorkspaceOptionsFormat(options)


```java
NutsWorkspaceOptionsFormat(NutsWorkspaceOptions options)
```
- **NutsWorkspaceOptions options** : 

### 🎛 Instance Properties
#### 📄🎛 bootCommand

```java
[read-only] String[] public bootCommand
public String[] getBootCommand()
```
#### 📄🎛 bootCommandLine

```java
[read-only] String public bootCommandLine
public String getBootCommandLine()
```
#### 📄🎛 exported

```java
[read-only] boolean public exported
public boolean isExported()
```
#### 📄🎛 init

```java
[read-only] boolean public init
public boolean isInit()
```
#### 📄🎛 runtime

```java
[read-only] boolean public runtime
public boolean isRuntime()
```
### ⚙ Instance Methods
#### ⚙ compact()


```java
NutsWorkspaceOptionsFormat compact()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ compact(compact)


```java
NutsWorkspaceOptionsFormat compact(boolean compact)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean compact** : 

#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ exported()


```java
NutsWorkspaceOptionsFormat exported()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ exported(e)


```java
NutsWorkspaceOptionsFormat exported(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ init()


```java
NutsWorkspaceOptionsFormat init()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ init(e)


```java
NutsWorkspaceOptionsFormat init(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ runtime()


```java
NutsWorkspaceOptionsFormat runtime()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ runtime(e)


```java
NutsWorkspaceOptionsFormat runtime(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ setCompact(compact)


```java
NutsWorkspaceOptionsFormat setCompact(boolean compact)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean compact** : 

#### ⚙ setExported(e)


```java
NutsWorkspaceOptionsFormat setExported(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ setInit(e)


```java
NutsWorkspaceOptionsFormat setInit(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ setRuntime(e)


```java
NutsWorkspaceOptionsFormat setRuntime(boolean e)
```
**return**:NutsWorkspaceOptionsFormat
- **boolean e** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsXmlFormat
```java
public interface net.vpc.app.nuts.NutsXmlFormat
```
 Xml Format Helper class
 \@author vpc
 \@since 0.5.5
 \@category Format

### 🎛 Instance Properties
#### 📝🎛 compact
if true compact xml generated. if false, sue more versatile/formatted output.
```java
[read-write] NutsXmlFormat public compact
public boolean isCompact()
public NutsXmlFormat setCompact(compact)
```
#### ✏🎛 session
update session
```java
[write-only] NutsXmlFormat public session
public NutsXmlFormat setSession(session)
```
#### 📝🎛 value
set value to format
```java
[read-write] NutsXmlFormat public value
public Object getValue()
public NutsXmlFormat setValue(value)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsXmlFormat configure(boolean skipUnsupported, String[] args)
```
**return**:NutsXmlFormat
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ fromXmlElement(xmlElement, clazz)
convert \{\@code xmlElement\} to a valid instance of type \{\@code clazz\}

```java
Object fromXmlElement(Element xmlElement, Class clazz)
```
**return**:Object
- **Element xmlElement** : xmlElement to convert
- **Class clazz** : target class

#### ⚙ parse(bytes, clazz)
parse bytes as xml to the given class

```java
Object parse(byte[] bytes, Class clazz)
```
**return**:Object
- **byte[] bytes** : bytes to parse
- **Class clazz** : target class

#### ⚙ parse(file, clazz)
Parse Xml Content as given class type.

```java
Object parse(File file, Class clazz)
```
**return**:Object
- **File file** : input content
- **Class clazz** : type to parse to

#### ⚙ parse(inputStream, clazz)
parse inputStream as xml to the given class

```java
Object parse(InputStream inputStream, Class clazz)
```
**return**:Object
- **InputStream inputStream** : inputStream to parse
- **Class clazz** : target class

#### ⚙ parse(path, clazz)
Parse Xml Content as given class type.

```java
Object parse(Path path, Class clazz)
```
**return**:Object
- **Path path** : input content
- **Class clazz** : type to parse to

#### ⚙ parse(reader, clazz)
Parse Xml Content as given class type.

```java
Object parse(Reader reader, Class clazz)
```
**return**:Object
- **Reader reader** : input content
- **Class clazz** : type to parse to

#### ⚙ parse(url, clazz)
parse url content as xml to the given class

```java
Object parse(URL url, Class clazz)
```
**return**:Object
- **URL url** : url to parse
- **Class clazz** : target class

#### ⚙ toXmlDocument(value)
convert \{\@code value\} to an xml document.

```java
Document toXmlDocument(Object value)
```
**return**:Document
- **Object value** : value to convert

#### ⚙ toXmlElement(value, xmlDocument)
convert \{\@code value\} to a valid root element to add to the given \{\@code xmlDocument\}.
 if the document is null, a new one will be created.

```java
Element toXmlElement(Object value, Document xmlDocument)
```
**return**:Element
- **Object value** : value to convert
- **Document xmlDocument** : target document

#### ⚙ value(value)
set value to format

```java
NutsXmlFormat value(Object value)
```
**return**:NutsXmlFormat
- **Object value** : value to format

