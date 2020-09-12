---
id: javadoc_Extensions
title: Extensions
sidebar_label: Extensions
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsRepositoryModel
```java
public interface net.vpc.app.nuts.NutsRepositoryModel
```
 
 \@author vpc
 \@category Extensions

### 📢❄ Constant Fields
#### 📢❄ CACHE
```java
public static final int CACHE = 48
```
#### 📢❄ CACHE_READ
```java
public static final int CACHE_READ = 16
```
#### 📢❄ CACHE_WRITE
```java
public static final int CACHE_WRITE = 32
```
#### 📢❄ LIB
```java
public static final int LIB = 14
```
#### 📢❄ LIB_OVERRIDE
```java
public static final int LIB_OVERRIDE = 8
```
#### 📢❄ LIB_READ
```java
public static final int LIB_READ = 2
```
#### 📢❄ LIB_WRITE
```java
public static final int LIB_WRITE = 4
```
#### 📢❄ MIRRORING
```java
public static final int MIRRORING = 1
```
### 🎛 Instance Properties
#### 📄🎛 deployOrder

```java
[read-only] int public deployOrder
public int getDeployOrder()
```
#### 📄🎛 mode

```java
[read-only] int public mode
public int getMode()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 repositoryType

```java
[read-only] String public repositoryType
public String getRepositoryType()
```
#### 📄🎛 speed

```java
[read-only] int public speed
public int getSpeed()
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
### ⚙ Instance Methods
#### ⚙ acceptDeploy(id, mode, repository, session)


```java
boolean acceptDeploy(NutsId id, NutsFetchMode mode, NutsRepository repository, NutsSession session)
```
**return**:boolean
- **NutsId id** : 
- **NutsFetchMode mode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ acceptFetch(id, mode, repository, session)


```java
boolean acceptFetch(NutsId id, NutsFetchMode mode, NutsRepository repository, NutsSession session)
```
**return**:boolean
- **NutsId id** : 
- **NutsFetchMode mode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ fetchContent(id, descriptor, localPath, fetchMode, repository, session)


```java
NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, Path localPath, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session)
```
**return**:NutsContent
- **NutsId id** : 
- **NutsDescriptor descriptor** : 
- **Path localPath** : 
- **NutsFetchMode fetchMode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ fetchDescriptor(id, fetchMode, repository, session)


```java
NutsDescriptor fetchDescriptor(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session)
```
**return**:NutsDescriptor
- **NutsId id** : 
- **NutsFetchMode fetchMode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ search(filter, roots, fetchMode, repository, session)


```java
Iterator search(NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session)
```
**return**:Iterator
- **NutsIdFilter filter** : 
- **String[] roots** : 
- **NutsFetchMode fetchMode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ searchLatestVersion(id, filter, fetchMode, repository, session)


```java
NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session)
```
**return**:NutsId
- **NutsId id** : 
- **NutsIdFilter filter** : 
- **NutsFetchMode fetchMode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ searchVersions(id, idFilter, fetchMode, repository, session)


```java
Iterator searchVersions(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session)
```
**return**:Iterator
- **NutsId id** : 
- **NutsIdFilter idFilter** : 
- **NutsFetchMode fetchMode** : 
- **NutsRepository repository** : 
- **NutsSession session** : 

#### ⚙ updateStatistics(repository, session)


```java
void updateStatistics(NutsRepository repository, NutsSession session)
```
- **NutsRepository repository** : 
- **NutsSession session** : 

## ☕ NutsWorkspaceExtension
```java
public interface net.vpc.app.nuts.NutsWorkspaceExtension
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Extensions

### 🎛 Instance Properties
#### 📄🎛 id
extension id pattern (configured)
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 wiredId
extension id resolved and wired
```java
[read-only] NutsId public wiredId
public NutsId getWiredId()
```
## ☕ NutsWorkspaceExtensionManager
```java
public interface net.vpc.app.nuts.NutsWorkspaceExtensionManager
```

 \@author vpc
 \@since 0.5.4
 \@category Extensions

### 🎛 Instance Properties
#### 📄🎛 extensionObjects

```java
[read-only] List public extensionObjects
public List getExtensionObjects(extensionPoint)
```
#### 📄🎛 extensionPoints

```java
[read-only] Set public extensionPoints
public Set getExtensionPoints()
```
#### 📄🎛 extensionTypes

```java
[read-only] Set public extensionTypes
public Set getExtensionTypes(extensionPoint)
```
#### 📄🎛 extensions
return loaded extensions
```java
[read-only] NutsId[] public extensions
public NutsId[] getExtensions()
```
#### 📄🎛 implementationTypes

```java
[read-only] List public implementationTypes
public List getImplementationTypes(type)
```
#### 📄🎛 registeredInstance

```java
[read-only] boolean public registeredInstance
public boolean isRegisteredInstance(extensionPointType, extensionImpl)
```
#### 📄🎛 registeredType

```java
[read-only] boolean public registeredType
public boolean isRegisteredType(extensionPointType, extensionType)
```
### ⚙ Instance Methods
#### ⚙ createAll(type)


```java
List createAll(Class type)
```
**return**:List
- **Class type** : 

#### ⚙ createAllSupported(type, supportCriteria)


```java
List createAllSupported(Class type, NutsSupportLevelContext supportCriteria)
```
**return**:List
- **Class type** : 
- **NutsSupportLevelContext supportCriteria** : 

#### ⚙ createServiceLoader(serviceType, criteriaType)


```java
NutsServiceLoader createServiceLoader(Class serviceType, Class criteriaType)
```
**return**:NutsServiceLoader
- **Class serviceType** : 
- **Class criteriaType** : 

#### ⚙ createServiceLoader(serviceType, criteriaType, classLoader)


```java
NutsServiceLoader createServiceLoader(Class serviceType, Class criteriaType, ClassLoader classLoader)
```
**return**:NutsServiceLoader
- **Class serviceType** : 
- **Class criteriaType** : 
- **ClassLoader classLoader** : 

#### ⚙ createSupported(type, supportCriteria)
create supported extension implementation or return null.

```java
NutsComponent createSupported(Class type, NutsSupportLevelContext supportCriteria)
```
**return**:NutsComponent
- **Class type** : extension type
- **NutsSupportLevelContext supportCriteria** : context

#### ⚙ createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters)
create supported extension implementation or return null.

```java
NutsComponent createSupported(Class type, NutsSupportLevelContext supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters)
```
**return**:NutsComponent
- **Class type** : extension type
- **NutsSupportLevelContext supportCriteria** : context
- **Class[] constructorParameterTypes** : constructor Parameter Types
- **Object[] constructorParameters** : constructor Parameters

#### ⚙ discoverTypes(classLoader)


```java
List discoverTypes(ClassLoader classLoader)
```
**return**:List
- **ClassLoader classLoader** : 

#### ⚙ installWorkspaceExtensionComponent(extensionPointType, extensionImpl)


```java
boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl)
```
**return**:boolean
- **Class extensionPointType** : 
- **Object extensionImpl** : 

#### ⚙ registerInstance(extensionPoint, implementation)


```java
boolean registerInstance(Class extensionPoint, Object implementation)
```
**return**:boolean
- **Class extensionPoint** : 
- **Object implementation** : 

#### ⚙ registerType(extensionPointType, extensionType)


```java
boolean registerType(Class extensionPointType, Class extensionType)
```
**return**:boolean
- **Class extensionPointType** : 
- **Class extensionType** : 

