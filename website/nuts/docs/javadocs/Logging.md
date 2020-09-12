---
id: javadoc_Logging
title: Logging
sidebar_label: Logging
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsLogConfig
```java
public net.vpc.app.nuts.NutsLogConfig
```
 log configuration for running nuts
 \@author vpc
 \@since 0.5.4
 \@category Logging

### 🪄 Constructors
#### 🪄 NutsLogConfig()


```java
NutsLogConfig()
```

#### 🪄 NutsLogConfig(other)


```java
NutsLogConfig(NutsLogConfig other)
```
- **NutsLogConfig other** : 

### 🎛 Instance Properties
#### 📄🎛 logFileBase

```java
[read-only] String public logFileBase
public String getLogFileBase()
```
#### 📄🎛 logFileCount

```java
[read-only] int public logFileCount
public int getLogFileCount()
```
#### 📄🎛 logFileLevel

```java
[read-only] Level public logFileLevel
public Level getLogFileLevel()
```
#### 📄🎛 logFileName

```java
[read-only] String public logFileName
public String getLogFileName()
```
#### 📄🎛 logFileSize

```java
[read-only] int public logFileSize
public int getLogFileSize()
```
#### 📄🎛 logInherited

```java
[read-only] boolean public logInherited
public boolean isLogInherited()
```
#### 📄🎛 logTermLevel

```java
[read-only] Level public logTermLevel
public Level getLogTermLevel()
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

#### ⚙ setLogFileBase(logFileBase)


```java
NutsLogConfig setLogFileBase(String logFileBase)
```
**return**:NutsLogConfig
- **String logFileBase** : 

#### ⚙ setLogFileCount(logFileCount)


```java
NutsLogConfig setLogFileCount(int logFileCount)
```
**return**:NutsLogConfig
- **int logFileCount** : 

#### ⚙ setLogFileLevel(logFileLevel)


```java
NutsLogConfig setLogFileLevel(Level logFileLevel)
```
**return**:NutsLogConfig
- **Level logFileLevel** : 

#### ⚙ setLogFileName(logFileName)


```java
NutsLogConfig setLogFileName(String logFileName)
```
**return**:NutsLogConfig
- **String logFileName** : 

#### ⚙ setLogFileSize(logFileSize)


```java
NutsLogConfig setLogFileSize(int logFileSize)
```
**return**:NutsLogConfig
- **int logFileSize** : 

#### ⚙ setLogInherited(logInherited)


```java
NutsLogConfig setLogInherited(boolean logInherited)
```
**return**:NutsLogConfig
- **boolean logInherited** : 

#### ⚙ setLogTermLevel(logTermLevel)


```java
NutsLogConfig setLogTermLevel(Level logTermLevel)
```
**return**:NutsLogConfig
- **Level logTermLevel** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsLogManager
```java
public interface net.vpc.app.nuts.NutsLogManager
```
 Nuts Log Manager
 \@category Logging

### 🎛 Instance Properties
#### 📄🎛 fileHandler
file handler
```java
[read-only] Handler public fileHandler
public Handler getFileHandler()
```
#### 📄🎛 fileLevel
return file logger level
```java
[read-only] Level public fileLevel
public Level getFileLevel()
```
#### 📄🎛 handlers
Log handler
```java
[read-only] Handler[] public handlers
public Handler[] getHandlers()
```
#### 📄🎛 termHandler
terminal handler
```java
[read-only] Handler public termHandler
public Handler getTermHandler()
```
#### 📄🎛 termLevel
return terminal logger level
```java
[read-only] Level public termLevel
public Level getTermLevel()
```
### ⚙ Instance Methods
#### ⚙ addHandler(handler)
add the given handler

```java
void addHandler(Handler handler)
```
- **Handler handler** : handler to add

#### ⚙ of(clazz)
create an instance of \{\@link NutsLogger\}

```java
NutsLogger of(Class clazz)
```
**return**:NutsLogger
- **Class clazz** : logger clazz

#### ⚙ of(name)
create an instance of \{\@link NutsLogger\}

```java
NutsLogger of(String name)
```
**return**:NutsLogger
- **String name** : logger name

#### ⚙ removeHandler(handler)
remove the given handler

```java
void removeHandler(Handler handler)
```
- **Handler handler** : handler to remove

#### ⚙ setFileLevel(level, options)
set file logger level

```java
void setFileLevel(Level level, NutsUpdateOptions options)
```
- **Level level** : new level
- **NutsUpdateOptions options** : update options

#### ⚙ setTermLevel(level, options)
set terminal logger level

```java
void setTermLevel(Level level, NutsUpdateOptions options)
```
- **Level level** : new level
- **NutsUpdateOptions options** : update options

## ☕ NutsLogger
```java
public interface net.vpc.app.nuts.NutsLogger
```
 Workspace aware Logger
 \@category Logging

### ⚙ Instance Methods
#### ⚙ isLoggable(level)
Check if a message of the given level would actually be logged
 by this logger.  This check is based on the Loggers effective level,
 which may be inherited from its parent.

```java
boolean isLoggable(Level level)
```
**return**:boolean
- **Level level** : a message logging level

#### ⚙ log(record)
Log a LogRecord.
 \<p\>
 All the other logging methods in this class call through
 this method to actually perform any logging.  Subclasses can
 override this single method to capture all log activity.

```java
void log(LogRecord record)
```
- **LogRecord record** : the LogRecord to be published

#### ⚙ log(level, msg, thrown)
log message using \'FAIL\' verb

```java
void log(Level level, String msg, Throwable thrown)
```
- **Level level** : message level
- **String msg** : message
- **Throwable thrown** : error thrown

#### ⚙ log(level, verb, msg)
log message using the given verb and level

```java
void log(Level level, String verb, String msg)
```
- **Level level** : message level
- **String verb** : message verb / category
- **String msg** : message

#### ⚙ log(level, verb, msgSupplier)
log message using the given verb and level

```java
void log(Level level, String verb, Supplier msgSupplier)
```
- **Level level** : message level
- **String verb** : message verb / category
- **Supplier msgSupplier** : message supplier

#### ⚙ log(level, verb, msg, params)
log message using the given verb and level

```java
void log(Level level, String verb, String msg, Object[] params)
```
- **Level level** : message level
- **String verb** : message verb / category
- **String msg** : message
- **Object[] params** : message parameters

#### ⚙ with()
create a logger op.
 A Logger Op handles all information to log in a custom manner.

```java
NutsLoggerOp with()
```
**return**:NutsLoggerOp

## ☕ NutsLoggerOp
```java
public interface net.vpc.app.nuts.NutsLoggerOp
```
 Log operation
 \@category Logging

### ⚙ Instance Methods
#### ⚙ error(error)
set log error

```java
NutsLoggerOp error(Throwable error)
```
**return**:NutsLoggerOp
- **Throwable error** : error thrown

#### ⚙ formatted()
set formatted mode (Nuts Stream Format)

```java
NutsLoggerOp formatted()
```
**return**:NutsLoggerOp

#### ⚙ formatted(value)
set or unset formatted mode (Nuts Stream Format)

```java
NutsLoggerOp formatted(boolean value)
```
**return**:NutsLoggerOp
- **boolean value** : formatted flag

#### ⚙ level(level)
set operation level

```java
NutsLoggerOp level(Level level)
```
**return**:NutsLoggerOp
- **Level level** : message level

#### ⚙ log(msgSupplier)
log the given message

```java
void log(Supplier msgSupplier)
```
- **Supplier msgSupplier** : message supplier

#### ⚙ log(msg, params)
log the given message

```java
void log(String msg, Object[] params)
```
- **String msg** : message
- **Object[] params** : message params

#### ⚙ style(style)
set message style (cstyle or positional)

```java
NutsLoggerOp style(NutsTextFormatStyle style)
```
**return**:NutsLoggerOp
- **NutsTextFormatStyle style** : message format style

#### ⚙ time(time)
set operation time

```java
NutsLoggerOp time(long time)
```
**return**:NutsLoggerOp
- **long time** : operation time in ms

#### ⚙ verb(verb)
set log verb

```java
NutsLoggerOp verb(String verb)
```
**return**:NutsLoggerOp
- **String verb** : verb or category

