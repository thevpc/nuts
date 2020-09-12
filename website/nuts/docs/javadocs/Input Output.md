---
id: javadoc_Input_Output
title: Input Output
sidebar_label: Input Output
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsIOCopyAction
```java
public interface net.vpc.app.nuts.NutsIOCopyAction
```
 I/O Action that help monitored copy of one or multiple resource types.
 Implementation should at least handle the following types as valid sources :
 \<ul\>
     \<li\>InputStream\</li\>
     \<li\>string (as path or url)\</li\>
     \<li\>File (file or directory)\</li\>
     \<li\>Path (file or directory)\</li\>
     \<li\>URL\</li\>
 \</ul\>
 and the following types as valid targets :
 \<ul\>
     \<li\>OutputStream\</li\>
     \<li\>string (as path or url)\</li\>
     \<li\>File (file or directory)\</li\>
     \<li\>Path (file or directory)\</li\>
 \</ul\>
 \@author vpc
 \@since 0.5.4
 \@category Input Output

### 🎛 Instance Properties
#### 📄🎛 byteArrayResult
run this copy action with \{\@link java.io.ByteArrayOutputStream\} target and return bytes result
```java
[read-only] byte[] public byteArrayResult
public byte[] getByteArrayResult()
```
#### 📝🎛 interruptible
mark created stream as interruptible so that one can call \{\@link #interrupt()\}
```java
[read-write] NutsIOCopyAction public interruptible
public boolean isInterruptible()
public NutsIOCopyAction setInterruptible(interruptible)
```
#### 📝🎛 logProgress
switch log progress flag to \{\@code value\}.
```java
[read-write] NutsIOCopyAction public logProgress
public boolean isLogProgress()
public NutsIOCopyAction setLogProgress(value)
```
#### ✏🎛 progressMonitor
set progress monitor. Will create a singleton progress monitor factory
```java
[write-only] NutsIOCopyAction public progressMonitor
public NutsIOCopyAction setProgressMonitor(value)
```
#### 📝🎛 progressMonitorFactory
set progress factory responsible of creating progress monitor
```java
[read-write] NutsIOCopyAction public progressMonitorFactory
public NutsProgressFactory getProgressMonitorFactory()
public NutsIOCopyAction setProgressMonitorFactory(value)
```
#### 📝🎛 safe
switch safe copy flag to \{\@code value\}
```java
[read-write] NutsIOCopyAction public safe
public boolean isSafe()
public NutsIOCopyAction setSafe(value)
```
#### 📝🎛 session
update current session
```java
[read-write] NutsIOCopyAction public session
public NutsSession getSession()
public NutsIOCopyAction setSession(session)
```
#### 📝🎛 skipRoot
set skip root flag to \{\@code value\}
```java
[read-write] NutsIOCopyAction public skipRoot
public boolean isSkipRoot()
public NutsIOCopyAction setSkipRoot(value)
```
#### 📝🎛 source
update source to copy from
```java
[read-write] NutsIOCopyAction public source
public Object getSource()
public NutsIOCopyAction setSource(source)
```
#### 📝🎛 target
update target to copy from
```java
[read-write] NutsIOCopyAction public target
public Object getTarget()
public NutsIOCopyAction setTarget(target)
```
#### 📝🎛 validator
update validator
```java
[read-write] NutsIOCopyAction public validator
public NutsIOCopyValidator getValidator()
public NutsIOCopyAction setValidator(validator)
```
### ⚙ Instance Methods
#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(Object source)
```
**return**:NutsIOCopyAction
- **Object source** : source to copy from

#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(String source)
```
**return**:NutsIOCopyAction
- **String source** : source to copy from

#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(InputStream source)
```
**return**:NutsIOCopyAction
- **InputStream source** : source to copy from

#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(File source)
```
**return**:NutsIOCopyAction
- **File source** : source to copy from

#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(Path source)
```
**return**:NutsIOCopyAction
- **Path source** : source to copy from

#### ⚙ from(source)
update source to copy from

```java
NutsIOCopyAction from(URL source)
```
**return**:NutsIOCopyAction
- **URL source** : source to copy from

#### ⚙ interrupt()
interrupt last created stream. An exception is throws when the stream is read.

```java
NutsIOCopyAction interrupt()
```
**return**:NutsIOCopyAction

#### ⚙ logProgress()
switch log progress flag to to true.

```java
NutsIOCopyAction logProgress()
```
**return**:NutsIOCopyAction

#### ⚙ logProgress(value)
switch log progress to \{\@code value\}

```java
NutsIOCopyAction logProgress(boolean value)
```
**return**:NutsIOCopyAction
- **boolean value** : log progress

#### ⚙ progressMonitor(value)
set progress monitor. Will create a singleton progress monitor factory

```java
NutsIOCopyAction progressMonitor(NutsProgressMonitor value)
```
**return**:NutsIOCopyAction
- **NutsProgressMonitor value** : new value

#### ⚙ progressMonitorFactory(value)
set progress factory responsible of creating progress monitor

```java
NutsIOCopyAction progressMonitorFactory(NutsProgressFactory value)
```
**return**:NutsIOCopyAction
- **NutsProgressFactory value** : new value

#### ⚙ run()
run this copy action

```java
NutsIOCopyAction run()
```
**return**:NutsIOCopyAction

#### ⚙ safe()
arm safe copy flag

```java
NutsIOCopyAction safe()
```
**return**:NutsIOCopyAction

#### ⚙ safe(value)
switch safe copy flag to \{\@code value\}

```java
NutsIOCopyAction safe(boolean value)
```
**return**:NutsIOCopyAction
- **boolean value** : value

#### ⚙ skipRoot()
set skip root flag to \{\@code true\}

```java
NutsIOCopyAction skipRoot()
```
**return**:NutsIOCopyAction

#### ⚙ skipRoot(value)
set skip root flag to \{\@code value\}

```java
NutsIOCopyAction skipRoot(boolean value)
```
**return**:NutsIOCopyAction
- **boolean value** : new value

#### ⚙ to(target)
update target

```java
NutsIOCopyAction to(Object target)
```
**return**:NutsIOCopyAction
- **Object target** : target

#### ⚙ to(target)
update target

```java
NutsIOCopyAction to(OutputStream target)
```
**return**:NutsIOCopyAction
- **OutputStream target** : target

#### ⚙ to(target)
update target to copy from

```java
NutsIOCopyAction to(String target)
```
**return**:NutsIOCopyAction
- **String target** : target to copy to

#### ⚙ to(target)
update target to copy from

```java
NutsIOCopyAction to(Path target)
```
**return**:NutsIOCopyAction
- **Path target** : target to copy to

#### ⚙ to(target)
update target to copy from

```java
NutsIOCopyAction to(File target)
```
**return**:NutsIOCopyAction
- **File target** : target to copy to

#### ⚙ validator(validator)
update validator

```java
NutsIOCopyAction validator(NutsIOCopyValidator validator)
```
**return**:NutsIOCopyAction
- **NutsIOCopyValidator validator** : validator

## ☕ NutsIOCopyValidator
```java
public interface net.vpc.app.nuts.NutsIOCopyValidator
```
 classes implementing this interface should check the validity of the stream that was copied.
 \@since 0.5.8
 \@category Input Output

### ⚙ Instance Methods
#### ⚙ validate(targetContent)
Check the validity of the stream that was copied.

```java
void validate(InputStream targetContent)
```
- **InputStream targetContent** : targetContent

## ☕ NutsIODeleteAction
```java
public interface net.vpc.app.nuts.NutsIODeleteAction
```
 I/O Action that help monitored delete.

 \@author vpc
 \@since 0.5.8
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 failFast
update fail fast flag
```java
[read-write] NutsIODeleteAction public failFast
public boolean isFailFast()
public NutsIODeleteAction setFailFast(failFast)
```
#### 📝🎛 session
update session
```java
[read-write] NutsIODeleteAction public session
public NutsSession getSession()
public NutsIODeleteAction setSession(session)
```
#### 📝🎛 target
update target to delete
```java
[read-write] NutsIODeleteAction public target
public Object getTarget()
public NutsIODeleteAction setTarget(target)
```
### ⚙ Instance Methods
#### ⚙ failFast()
set fail fast flag

```java
NutsIODeleteAction failFast()
```
**return**:NutsIODeleteAction

#### ⚙ failFast(failFast)
update fail fast flag

```java
NutsIODeleteAction failFast(boolean failFast)
```
**return**:NutsIODeleteAction
- **boolean failFast** : value

#### ⚙ run()
run delete action and return \{\@code this\}

```java
NutsIODeleteAction run()
```
**return**:NutsIODeleteAction

#### ⚙ target(target)
update target to delete

```java
NutsIODeleteAction target(Object target)
```
**return**:NutsIODeleteAction
- **Object target** : target

## ☕ NutsIOHashAction
```java
public interface net.vpc.app.nuts.NutsIOHashAction
```
 I/O command to hash contents.
 \@author vpc
 \@since 0.5.5
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 algorithm
select hash algorithm.
```java
[read-write] NutsIOHashAction public algorithm
public String getAlgorithm()
public NutsIOHashAction setAlgorithm(algorithm)
```
### ⚙ Instance Methods
#### ⚙ algorithm(algorithm)
select hash algorithm.

```java
NutsIOHashAction algorithm(String algorithm)
```
**return**:NutsIOHashAction
- **String algorithm** : hash algorithm. may be any algorithm supported by
             {@link MessageDigest#getInstance(String)}
             including 'MD5' and 'SHA1'

#### ⚙ computeBytes()
compute hash digest and return it as byte array

```java
byte[] computeBytes()
```
**return**:byte[]

#### ⚙ computeString()
compute hash digest and return it as hexadecimal string

```java
String computeString()
```
**return**:String

#### ⚙ md5()
select MD5 hash algorithm

```java
NutsIOHashAction md5()
```
**return**:NutsIOHashAction

#### ⚙ sha1()
select MD5 hash algorithm

```java
NutsIOHashAction sha1()
```
**return**:NutsIOHashAction

#### ⚙ source(descriptor)
source stream to  hash

```java
NutsIOHashAction source(NutsDescriptor descriptor)
```
**return**:NutsIOHashAction
- **NutsDescriptor descriptor** : source descriptor to  hash

#### ⚙ source(file)
file to  hash

```java
NutsIOHashAction source(File file)
```
**return**:NutsIOHashAction
- **File file** : source file to  hash

#### ⚙ source(input)
source stream to  hash

```java
NutsIOHashAction source(InputStream input)
```
**return**:NutsIOHashAction
- **InputStream input** : source stream to  hash

#### ⚙ source(path)
file to  hash

```java
NutsIOHashAction source(Path path)
```
**return**:NutsIOHashAction
- **Path path** : source path to  hash

#### ⚙ writeHash(out)
compute hash and writes it to the output stream

```java
NutsIOHashAction writeHash(OutputStream out)
```
**return**:NutsIOHashAction
- **OutputStream out** : output stream

## ☕ NutsIOLockAction
```java
public interface net.vpc.app.nuts.NutsIOLockAction
```
 Lock builder to create mainly File based Locks
 \@author vpc
 \@since 0.5.8
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 resource
update resource
```java
[read-write] NutsIOLockAction public resource
public Object getResource()
public NutsIOLockAction setResource(source)
```
#### 📝🎛 session
update session
```java
[read-write] NutsIOLockAction public session
public NutsSession getSession()
public NutsIOLockAction setSession(session)
```
#### 📝🎛 source
update source
```java
[read-write] NutsIOLockAction public source
public Object getSource()
public NutsIOLockAction setSource(source)
```
### ⚙ Instance Methods
#### ⚙ call(runnable)
create lock object for the given source and resource

```java
Object call(Callable runnable)
```
**return**:Object
- **Callable runnable** : runnable

#### ⚙ call(runnable, time, unit)
create lock object for the given source and resource

```java
Object call(Callable runnable, long time, TimeUnit unit)
```
**return**:Object
- **Callable runnable** : runnable
- **long time** : time
- **TimeUnit unit** : unit

#### ⚙ create()
create lock object for the given source and resource

```java
NutsLock create()
```
**return**:NutsLock

#### ⚙ resource(source)
update resource

```java
NutsIOLockAction resource(Object source)
```
**return**:NutsIOLockAction
- **Object source** : resource

#### ⚙ run(runnable)
create lock object for the given source and resource

```java
void run(Runnable runnable)
```
- **Runnable runnable** : runnable

#### ⚙ run(runnable, time, unit)
create lock object for the given source and resource

```java
void run(Runnable runnable, long time, TimeUnit unit)
```
- **Runnable runnable** : runnable
- **long time** : time
- **TimeUnit unit** : unit

#### ⚙ source(source)
update source

```java
NutsIOLockAction source(Object source)
```
**return**:NutsIOLockAction
- **Object source** : source

## ☕ NutsIOManager
```java
public interface net.vpc.app.nuts.NutsIOManager
```
 I/O Manager supports a set of operations to manipulate terminals and files in a
 handy manner that is monitorable and Workspace aware.

 \@author vpc
 \@since 0.5.4
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 systemTerminal
update workspace wide system terminal
```java
[read-write] NutsIOManager public systemTerminal
public NutsSystemTerminal getSystemTerminal()
public NutsIOManager setSystemTerminal(terminal)
```
#### 📝🎛 terminal
update workspace wide terminal
```java
[read-write] NutsIOManager public terminal
public NutsSessionTerminal getTerminal()
public NutsIOManager setTerminal(terminal)
```
#### 📄🎛 terminalFormat
return terminal format that handles metrics and format/escape methods.
```java
[read-only] NutsTerminalFormat public terminalFormat
public NutsTerminalFormat getTerminalFormat()
```
### ⚙ Instance Methods
#### ⚙ compress()
create new \{\@link NutsIOCompressAction\} instance

```java
NutsIOCompressAction compress()
```
**return**:NutsIOCompressAction

#### ⚙ copy()
create new \{\@link NutsIOCopyAction\} instance

```java
NutsIOCopyAction copy()
```
**return**:NutsIOCopyAction

#### ⚙ createApplicationContext(args, appClass, storeId, startTimeMillis)
create a new instance of \{\@link NutsApplicationContext\}

```java
NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis)
```
**return**:NutsApplicationContext
- **String[] args** : application arguments
- **Class appClass** : application class
- **String storeId** : application store id or null
- **long startTimeMillis** : application start time

#### ⚙ createPrintStream(out, mode)
create print stream that supports the given \{\@code mode\}.
 If the given \{\@code out\} is a PrintStream that supports \{\@code mode\}, it should be
 returned without modification.

```java
PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode)
```
**return**:PrintStream
- **OutputStream out** : stream to wrap
- **NutsTerminalMode mode** : mode to support

#### ⚙ createTempFile(name)
create temp file in the workspace\'s temp folder

```java
Path createTempFile(String name)
```
**return**:Path
- **String name** : file name

#### ⚙ createTempFile(name, repository)
create temp file in the repository\'s temp folder

```java
Path createTempFile(String name, NutsRepository repository)
```
**return**:Path
- **String name** : file name
- **NutsRepository repository** : repository

#### ⚙ createTempFolder(name)
create temp folder in the workspace\'s temp folder

```java
Path createTempFolder(String name)
```
**return**:Path
- **String name** : folder name

#### ⚙ createTempFolder(name, repository)
create temp folder in the repository\'s temp folder

```java
Path createTempFolder(String name, NutsRepository repository)
```
**return**:Path
- **String name** : folder name
- **NutsRepository repository** : repository

#### ⚙ createTerminal()
return new terminal bound to system terminal

```java
NutsSessionTerminal createTerminal()
```
**return**:NutsSessionTerminal

#### ⚙ createTerminal(parent)
return new terminal bound to the given \{\@code parent\}

```java
NutsSessionTerminal createTerminal(NutsTerminalBase parent)
```
**return**:NutsSessionTerminal
- **NutsTerminalBase parent** : parent terminal or null

#### ⚙ delete()
create new \{\@link NutsIODeleteAction\} instance

```java
NutsIODeleteAction delete()
```
**return**:NutsIODeleteAction

#### ⚙ executorService()
return non null executor service

```java
ExecutorService executorService()
```
**return**:ExecutorService

#### ⚙ expandPath(path)
expand path to Workspace Location

```java
String expandPath(String path)
```
**return**:String
- **String path** : path to expand

#### ⚙ expandPath(path, baseFolder)
expand path to \{\@code baseFolder\}.
 Expansion mechanism supports \'~\' prefix (linux like) and will expand path to \{\@code baseFolder\}
 if it was resolved as a relative path.

```java
String expandPath(String path, String baseFolder)
```
**return**:String
- **String path** : path to expand
- **String baseFolder** : base folder to expand relative paths to

#### ⚙ hash()
create new \{\@link NutsIOHashAction\} instance that helps
 hashing streams and files.

```java
NutsIOHashAction hash()
```
**return**:NutsIOHashAction

#### ⚙ loadFormattedString(reader, classLoader)
load resource as a formatted string to be used mostly as a help string.

```java
String loadFormattedString(Reader reader, ClassLoader classLoader)
```
**return**:String
- **Reader reader** : resource reader
- **ClassLoader classLoader** : class loader

#### ⚙ loadFormattedString(resourcePath, classLoader, defaultValue)
load resource as a formatted string to be used mostly as a help string.

```java
String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue)
```
**return**:String
- **String resourcePath** : resource path
- **ClassLoader classLoader** : class loader
- **String defaultValue** : default value if the loading fails

#### ⚙ lock()
create new \{\@link NutsIOLockAction\} instance

```java
NutsIOLockAction lock()
```
**return**:NutsIOLockAction

#### ⚙ monitor()
create new \{\@link NutsMonitorAction\} instance that helps
 monitoring streams.

```java
NutsMonitorAction monitor()
```
**return**:NutsMonitorAction

#### ⚙ nullInputStream()
create a null input stream instance

```java
InputStream nullInputStream()
```
**return**:InputStream

#### ⚙ nullPrintStream()
create a null print stream instance

```java
PrintStream nullPrintStream()
```
**return**:PrintStream

#### ⚙ parseExecutionEntries(file)
parse Execution Entries

```java
NutsExecutionEntry[] parseExecutionEntries(File file)
```
**return**:NutsExecutionEntry[]
- **File file** : jar file

#### ⚙ parseExecutionEntries(file)
parse Execution Entries

```java
NutsExecutionEntry[] parseExecutionEntries(Path file)
```
**return**:NutsExecutionEntry[]
- **Path file** : jar file

#### ⚙ parseExecutionEntries(inputStream, type, sourceName)
parse Execution Entries

```java
NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type, String sourceName)
```
**return**:NutsExecutionEntry[]
- **InputStream inputStream** : stream
- **String type** : stream type
- **String sourceName** : stream source name (optional)

#### ⚙ ps()
create new \{\@link NutsIOProcessAction\} instance

```java
NutsIOProcessAction ps()
```
**return**:NutsIOProcessAction

#### ⚙ systemTerminal()
return terminal format that handles metrics and format/escape methods.

```java
NutsSystemTerminal systemTerminal()
```
**return**:NutsSystemTerminal

#### ⚙ terminal()
return workspace default terminal

```java
NutsSessionTerminal terminal()
```
**return**:NutsSessionTerminal

#### ⚙ terminalFormat()
return terminal format that handles metrics and format/escape methods

```java
NutsTerminalFormat terminalFormat()
```
**return**:NutsTerminalFormat

#### ⚙ uncompress()
create new \{\@link NutsIOUncompressAction\} instance

```java
NutsIOUncompressAction uncompress()
```
**return**:NutsIOUncompressAction

## ☕ NutsIOProcessAction
```java
public interface net.vpc.app.nuts.NutsIOProcessAction
```
 I/O Action that help monitoring processes

 \@author vpc
 \@since 0.5.8
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 failFast
update fail fast flag
```java
[read-write] NutsIOProcessAction public failFast
public boolean isFailFast()
public NutsIOProcessAction setFailFast(failFast)
```
#### 📄🎛 resultList
list all processes of type \{\@link #getType()\}
```java
[read-only] NutsResultList public resultList
public NutsResultList getResultList()
```
#### 📝🎛 session
update session
```java
[read-write] NutsIOProcessAction public session
public NutsSession getSession()
public NutsIOProcessAction setSession(session)
```
#### 📝🎛 type
set process type to consider.
 Supported \'java\' or \'java#version\'
```java
[read-write] NutsIOProcessAction public type
public String getType()
public NutsIOProcessAction setType(processType)
```
### ⚙ Instance Methods
#### ⚙ failFast()
set fail fast flag

```java
NutsIOProcessAction failFast()
```
**return**:NutsIOProcessAction

#### ⚙ failFast(failFast)
update fail fast flag

```java
NutsIOProcessAction failFast(boolean failFast)
```
**return**:NutsIOProcessAction
- **boolean failFast** : value

#### ⚙ type(processType)
set process type to consider.
 Supported \'java\' or \'java#version\'

```java
NutsIOProcessAction type(String processType)
```
**return**:NutsIOProcessAction
- **String processType** : new type

## ☕ NutsIOUncompressAction
```java
public interface net.vpc.app.nuts.NutsIOUncompressAction
```
 I/O Action that help monitored uncompress of one or multiple resource types.
 \@author vpc
 \@since 0.5.8
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 format
update format
```java
[read-write] NutsIOUncompressAction public format
public String getFormat()
public NutsIOUncompressAction setFormat(format)
```
#### 📝🎛 logProgress
switch log progress flag to \{\@code value\}.
```java
[read-write] NutsIOUncompressAction public logProgress
public boolean isLogProgress()
public NutsIOUncompressAction setLogProgress(value)
```
#### ✏🎛 progressMonitor
set progress monitor. Will create a singleton progress monitor factory
```java
[write-only] NutsIOUncompressAction public progressMonitor
public NutsIOUncompressAction setProgressMonitor(value)
```
#### 📝🎛 progressMonitorFactory
set progress factory responsible of creating progress monitor
```java
[read-write] NutsIOUncompressAction public progressMonitorFactory
public NutsProgressFactory getProgressMonitorFactory()
public NutsIOUncompressAction setProgressMonitorFactory(value)
```
#### 📝🎛 safe
switch safe flag to \{\@code value\}
```java
[read-write] NutsIOUncompressAction public safe
public boolean isSafe()
public NutsIOUncompressAction setSafe(value)
```
#### 📝🎛 session
update current session
```java
[read-write] NutsIOUncompressAction public session
public NutsSession getSession()
public NutsIOUncompressAction setSession(session)
```
#### 📝🎛 skipRoot
set skip root flag to \{\@code value\}
```java
[read-write] NutsIOUncompressAction public skipRoot
public boolean isSkipRoot()
public NutsIOUncompressAction setSkipRoot(value)
```
#### 📝🎛 source
update source to uncompress from
```java
[read-write] NutsIOUncompressAction public source
public Object getSource()
public NutsIOUncompressAction setSource(source)
```
#### 📝🎛 target
update target
```java
[read-write] NutsIOUncompressAction public target
public Object getTarget()
public NutsIOUncompressAction setTarget(target)
```
### ⚙ Instance Methods
#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(InputStream source)
```
**return**:NutsIOUncompressAction
- **InputStream source** : source to uncompress from

#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(File source)
```
**return**:NutsIOUncompressAction
- **File source** : source to uncompress from

#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(Path source)
```
**return**:NutsIOUncompressAction
- **Path source** : source to uncompress from

#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(URL source)
```
**return**:NutsIOUncompressAction
- **URL source** : source to uncompress from

#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(String source)
```
**return**:NutsIOUncompressAction
- **String source** : source to uncompress from

#### ⚙ from(source)
update source to uncompress from

```java
NutsIOUncompressAction from(Object source)
```
**return**:NutsIOUncompressAction
- **Object source** : source to uncompress from

#### ⚙ getFormatOption(option)
return format option

```java
Object getFormatOption(String option)
```
**return**:Object
- **String option** : option name

#### ⚙ logProgress()
switch log progress flag to to true.

```java
NutsIOUncompressAction logProgress()
```
**return**:NutsIOUncompressAction

#### ⚙ logProgress(value)
switch log progress flag to \{\@code value\}.

```java
NutsIOUncompressAction logProgress(boolean value)
```
**return**:NutsIOUncompressAction
- **boolean value** : value

#### ⚙ progressMonitor(value)
set progress monitor. Will create a singleton progress monitor factory

```java
NutsIOUncompressAction progressMonitor(NutsProgressMonitor value)
```
**return**:NutsIOUncompressAction
- **NutsProgressMonitor value** : new value

#### ⚙ progressMonitorFactory(value)
set progress factory responsible of creating progress monitor

```java
NutsIOUncompressAction progressMonitorFactory(NutsProgressFactory value)
```
**return**:NutsIOUncompressAction
- **NutsProgressFactory value** : new value

#### ⚙ run()
run this uncompress action

```java
NutsIOUncompressAction run()
```
**return**:NutsIOUncompressAction

#### ⚙ safe()
arm safe flag

```java
NutsIOUncompressAction safe()
```
**return**:NutsIOUncompressAction

#### ⚙ safe(value)
switch safe flag to \{\@code value\}

```java
NutsIOUncompressAction safe(boolean value)
```
**return**:NutsIOUncompressAction
- **boolean value** : value

#### ⚙ setFormatOption(option, value)
update format option

```java
NutsIOUncompressAction setFormatOption(String option, Object value)
```
**return**:NutsIOUncompressAction
- **String option** : option name
- **Object value** : value

#### ⚙ skipRoot()
set skip root flag to \{\@code true\}

```java
NutsIOUncompressAction skipRoot()
```
**return**:NutsIOUncompressAction

#### ⚙ skipRoot(value)
set skip root flag to \{\@code value\}

```java
NutsIOUncompressAction skipRoot(boolean value)
```
**return**:NutsIOUncompressAction
- **boolean value** : new value

#### ⚙ to(target)
update target

```java
NutsIOUncompressAction to(String target)
```
**return**:NutsIOUncompressAction
- **String target** : target

#### ⚙ to(target)
update target

```java
NutsIOUncompressAction to(Path target)
```
**return**:NutsIOUncompressAction
- **Path target** : target

#### ⚙ to(target)
update target

```java
NutsIOUncompressAction to(File target)
```
**return**:NutsIOUncompressAction
- **File target** : target

#### ⚙ to(target)
update target

```java
NutsIOUncompressAction to(Object target)
```
**return**:NutsIOUncompressAction
- **Object target** : target

## ☕ NutsLock
```java
public interface net.vpc.app.nuts.NutsLock
```
 NutsLock is simply an adapter to standard \{\@link Lock\}.
 It adds no extra functionality but rather is provided as
 a base for future changes.
 \@since 0.5.8
 \@category Input Output

## ☕ NutsMonitorAction
```java
public interface net.vpc.app.nuts.NutsMonitorAction
```
 Monitor action enables monitoring a long lasting operation such as copying a big file.

 \@author vpc
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 length
update operation length
```java
[read-write] NutsMonitorAction public length
public long getLength()
public NutsMonitorAction setLength(len)
```
#### 📝🎛 logProgress
when true, will include default factory (console) even if progressFactory is defined
```java
[read-write] NutsMonitorAction public logProgress
public boolean isLogProgress()
public NutsMonitorAction setLogProgress(value)
```
#### 📝🎛 name
update action name
```java
[read-write] NutsMonitorAction public name
public String getName()
public NutsMonitorAction setName(name)
```
#### 📝🎛 origin
update action source origin
```java
[read-write] NutsMonitorAction public origin
public Object getOrigin()
public NutsMonitorAction setOrigin(origin)
```
#### 📝🎛 progressFactory
set progress factory responsible of creating progress monitor
```java
[read-write] NutsMonitorAction public progressFactory
public NutsProgressFactory getProgressFactory()
public NutsMonitorAction setProgressFactory(value)
```
#### ✏🎛 progressMonitor
set progress monitor. Will create a singleton progress monitor factory
```java
[write-only] NutsMonitorAction public progressMonitor
public NutsMonitorAction setProgressMonitor(value)
```
#### 📝🎛 session
update current session
```java
[read-write] NutsMonitorAction public session
public NutsSession getSession()
public NutsMonitorAction setSession(session)
```
#### ✏🎛 source
update operation source
```java
[write-only] NutsMonitorAction public source
public NutsMonitorAction setSource(path)
```
### ⚙ Instance Methods
#### ⚙ create()
Create monitored input stream

```java
InputStream create()
```
**return**:InputStream

#### ⚙ length(len)
update operation length

```java
NutsMonitorAction length(long len)
```
**return**:NutsMonitorAction
- **long len** : operation length

#### ⚙ logProgress()
will include default factory (console) even if progressFactory is defined

```java
NutsMonitorAction logProgress()
```
**return**:NutsMonitorAction

#### ⚙ logProgress(value)
when true, will include default factory (console) even if progressFactory is defined

```java
NutsMonitorAction logProgress(boolean value)
```
**return**:NutsMonitorAction
- **boolean value** : value

#### ⚙ name(name)
update action name

```java
NutsMonitorAction name(String name)
```
**return**:NutsMonitorAction
- **String name** : action name

#### ⚙ origin(origin)
update action source origin

```java
NutsMonitorAction origin(Object origin)
```
**return**:NutsMonitorAction
- **Object origin** : source origin

#### ⚙ progressFactory(value)
set progress factory responsible of creating progress monitor

```java
NutsMonitorAction progressFactory(NutsProgressFactory value)
```
**return**:NutsMonitorAction
- **NutsProgressFactory value** : new value

#### ⚙ progressMonitor(value)
set progress monitor. Will create a singleton progress monitor factory

```java
NutsMonitorAction progressMonitor(NutsProgressMonitor value)
```
**return**:NutsMonitorAction
- **NutsProgressMonitor value** : new value

#### ⚙ source(path)
update operation source

```java
NutsMonitorAction source(String path)
```
**return**:NutsMonitorAction
- **String path** : operation source

#### ⚙ source(path)
update operation source

```java
NutsMonitorAction source(Path path)
```
**return**:NutsMonitorAction
- **Path path** : operation source

#### ⚙ source(path)
update operation source

```java
NutsMonitorAction source(File path)
```
**return**:NutsMonitorAction
- **File path** : operation source

#### ⚙ source(path)
update operation source
 TODO: should this handle only streams?

```java
NutsMonitorAction source(InputStream path)
```
**return**:NutsMonitorAction
- **InputStream path** : operation source

## ☕ NutsNonFormattedPrintStream
```java
public interface net.vpc.app.nuts.NutsNonFormattedPrintStream
```
 Non formatted Print Stream Anchor Interface
 \@author vpc
 \@since 0.5.4
 \@category Input Output

## ☕ NutsOutputStreamTransparentAdapter
```java
public interface net.vpc.app.nuts.NutsOutputStreamTransparentAdapter
```
 Interface to enable marking system streams. When creating new processes nuts
 will dereference NutsOutputStreamTransparentAdapter to check if the
 OutputStream i a system io. In that cas nuts will "inherit" output/error
 stream

 \@since 0.5.4
 \@category Input Output

### ⚙ Instance Methods
#### ⚙ baseOutputStream()
de-referenced stream

```java
OutputStream baseOutputStream()
```
**return**:OutputStream

## ☕ NutsSystemTerminal
```java
public interface net.vpc.app.nuts.NutsSystemTerminal
```

 \@author vpc
 \@since 0.5.4
 \@category Input Output

### 🎛 Instance Properties
#### 📄🎛 standardErrorStream

```java
[read-only] boolean public standardErrorStream
public boolean isStandardErrorStream(out)
```
#### 📄🎛 standardInputStream

```java
[read-only] boolean public standardInputStream
public boolean isStandardInputStream(in)
```
#### 📄🎛 standardOutputStream

```java
[read-only] boolean public standardOutputStream
public boolean isStandardOutputStream(out)
```
## ☕ NutsTerminal
```java
public interface net.vpc.app.nuts.NutsTerminal
```
 A Terminal handles in put stream, an output stream and an error stream to communicate
 with user.
 \@since 0.5.4
 \@category Input Output

### 🎛 Instance Properties
#### 📝🎛 errMode
change terminal mode for err
```java
[read-write] NutsTerminal public errMode
public NutsTerminalMode getErrMode()
public NutsTerminal setErrMode(mode)
```
#### ✏🎛 mode
change terminal mode for both out and err
```java
[write-only] NutsTerminal public mode
public NutsTerminal setMode(mode)
```
#### 📝🎛 outMode
change terminal mode for out
```java
[read-write] NutsTerminal public outMode
public NutsTerminalMode getOutMode()
public NutsTerminal setOutMode(mode)
```
### ⚙ Instance Methods
#### ⚙ ask()
create a \{\@link NutsQuestion\} to write a question to the terminal\'s output stream
 and read a typed value from the terminal\'s input stream.

```java
NutsQuestion ask()
```
**return**:NutsQuestion

#### ⚙ err()
return terminal\'s error stream

```java
PrintStream err()
```
**return**:PrintStream

#### ⚙ errMode(mode)
change terminal mode for out

```java
NutsTerminal errMode(NutsTerminalMode mode)
```
**return**:NutsTerminal
- **NutsTerminalMode mode** : mode

#### ⚙ in()
return terminal\'s input stream

```java
InputStream in()
```
**return**:InputStream

#### ⚙ mode(mode)
change terminal mode for both out and err

```java
NutsTerminal mode(NutsTerminalMode mode)
```
**return**:NutsTerminal
- **NutsTerminalMode mode** : mode

#### ⚙ out()
return terminal\'s output stream

```java
PrintStream out()
```
**return**:PrintStream

#### ⚙ outMode(mode)
change terminal mode for out

```java
NutsTerminal outMode(NutsTerminalMode mode)
```
**return**:NutsTerminal
- **NutsTerminalMode mode** : mode

#### ⚙ readLine(promptFormat, params)
Reads a single line of text from the terminal\'s input stream.

```java
String readLine(String promptFormat, Object[] params)
```
**return**:String
- **String promptFormat** : prompt message format (cstyle)
- **Object[] params** : prompt message parameters

#### ⚙ readPassword(promptFormat, params)
Reads password as a single line of text from the terminal\'s input stream.

```java
char[] readPassword(String promptFormat, Object[] params)
```
**return**:char[]
- **String promptFormat** : prompt message format (cstyle)
- **Object[] params** : prompt message parameters

