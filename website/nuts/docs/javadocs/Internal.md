---
id: javadoc_Internal
title: Internal
sidebar_label: Internal
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ PrivateNutsLog
```java
public net.vpc.app.nuts.PrivateNutsLog
```
 
 \@author vpc
 \@category Internal

### 📢❄ Constant Fields
#### 📢❄ CACHE
```java
public static final String CACHE = "CACHE"
```
#### 📢❄ DEFAULT_DATE_TIME_FORMATTER
```java
public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
```
#### 📢❄ FAIL
```java
public static final String FAIL = "FAIL"
```
#### 📢❄ READ
```java
public static final String READ = "READ"
```
#### 📢❄ START
```java
public static final String START = "START"
```
#### 📢❄ SUCCESS
```java
public static final String SUCCESS = "SUCCESS"
```
#### 📢❄ WARNING
```java
public static final String WARNING = "WARNING"
```
### 🪄 Constructors
#### 🪄 PrivateNutsLog()


```java
PrivateNutsLog()
```

### 🎛 Instance Properties
#### 📄🎛 loggable

```java
[read-only] boolean public loggable
public boolean isLoggable(lvl)
```
### ⚙ Instance Methods
#### ⚙ log(lvl, logVerb, message)


```java
void log(Level lvl, String logVerb, String message)
```
- **Level lvl** : 
- **String logVerb** : 
- **String message** : 

#### ⚙ log(lvl, message, err)


```java
void log(Level lvl, String message, Throwable err)
```
- **Level lvl** : 
- **String message** : 
- **Throwable err** : 

#### ⚙ log(lvl, logVerb, message, object)


```java
void log(Level lvl, String logVerb, String message, Object object)
```
- **Level lvl** : 
- **String logVerb** : 
- **String message** : 
- **Object object** : 

#### ⚙ log(lvl, logVerb, message, objects)


```java
void log(Level lvl, String logVerb, String message, Object[] objects)
```
- **Level lvl** : 
- **String logVerb** : 
- **String message** : 
- **Object[] objects** : 

#### ⚙ setOptions(options)


```java
void setOptions(NutsWorkspaceOptions options)
```
- **NutsWorkspaceOptions options** : 

