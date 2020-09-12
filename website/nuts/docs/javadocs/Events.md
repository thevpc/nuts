---
id: javadoc_Events
title: Events
sidebar_label: Events
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsInstallListener
```java
public interface net.vpc.app.nuts.NutsInstallListener
```
 A class can implement the \<code\>NutsInstallListener\</code\> interface when it
 wants to be informed of install artifacts actions.

 \@author vpc
 \@since 0.5.4
 \@category Events

### ⚙ Instance Methods
#### ⚙ onInstall(event)
This method is called whenever the observed workspace installs an artifact.

```java
void onInstall(NutsInstallEvent event)
```
- **NutsInstallEvent event** : event

#### ⚙ onUninstall(event)
This method is called whenever the observed workspace uninstalls an artifact.

```java
void onUninstall(NutsInstallEvent event)
```
- **NutsInstallEvent event** : event

#### ⚙ onUpdate(event)
This method is called whenever the observed workspace updates an artifact.

```java
void onUpdate(NutsUpdateEvent event)
```
- **NutsUpdateEvent event** : event

## ☕ NutsRepositoryEvent
```java
public interface net.vpc.app.nuts.NutsRepositoryEvent
```
 Repository Event
 \@author vpc
 \@since 0.5.4
 \@category Events

### 🎛 Instance Properties
#### 📄🎛 parent
Parent repository when this event is about creating
 a new repository with a parent one.
```java
[read-only] NutsRepository public parent
public NutsRepository getParent()
```
#### 📄🎛 propertyName
event property name
```java
[read-only] String public propertyName
public String getPropertyName()
```
#### 📄🎛 propertyOldValue
event property old value
```java
[read-only] Object public propertyOldValue
public Object getPropertyOldValue()
```
#### 📄🎛 propertyValue
event property new value
```java
[read-only] Object public propertyValue
public Object getPropertyValue()
```
#### 📄🎛 repository
repository that fires this event or the new repository
 when creating a new one with parent.
```java
[read-only] NutsRepository public repository
public NutsRepository getRepository()
```
#### 📄🎛 session
current session
```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace
current workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsRepositoryListener
```java
public interface net.vpc.app.nuts.NutsRepositoryListener
```
 Created by vpc on 1/20/17.

 \@since 0.5.4
 \@category Events

### ⚙ Instance Methods
#### ⚙ onAddRepository(event)


```java
void onAddRepository(NutsRepositoryEvent event)
```
- **NutsRepositoryEvent event** : 

#### ⚙ onConfigurationChanged(event)


```java
void onConfigurationChanged(NutsRepositoryEvent event)
```
- **NutsRepositoryEvent event** : 

#### ⚙ onDeploy(event)


```java
void onDeploy(NutsContentEvent event)
```
- **NutsContentEvent event** : 

#### ⚙ onPush(event)


```java
void onPush(NutsContentEvent event)
```
- **NutsContentEvent event** : 

#### ⚙ onRemoveRepository(event)


```java
void onRemoveRepository(NutsRepositoryEvent event)
```
- **NutsRepositoryEvent event** : 

#### ⚙ onUndeploy(event)


```java
void onUndeploy(NutsContentEvent event)
```
- **NutsContentEvent event** : 

## ☕ NutsUpdateEvent
```java
public interface net.vpc.app.nuts.NutsUpdateEvent
```

 \@author vpc
 \@since 0.5.6
 \@category Events

### 🎛 Instance Properties
#### 📄🎛 force

```java
[read-only] boolean public force
public boolean isForce()
```
#### 📄🎛 newValue

```java
[read-only] NutsDefinition public newValue
public NutsDefinition getNewValue()
```
#### 📄🎛 oldValue

```java
[read-only] NutsDefinition public oldValue
public NutsDefinition getOldValue()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsWorkspaceEvent
```java
public interface net.vpc.app.nuts.NutsWorkspaceEvent
```

 \@author vpc
 \@since 0.5.4
 \@category Events

### 🎛 Instance Properties
#### 📄🎛 propertyName

```java
[read-only] String public propertyName
public String getPropertyName()
```
#### 📄🎛 propertyOldValue

```java
[read-only] Object public propertyOldValue
public Object getPropertyOldValue()
```
#### 📄🎛 propertyValue

```java
[read-only] Object public propertyValue
public Object getPropertyValue()
```
#### 📄🎛 repository

```java
[read-only] NutsRepository public repository
public NutsRepository getRepository()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsWorkspaceListener
```java
public interface net.vpc.app.nuts.NutsWorkspaceListener
```
 Created by vpc on 1/20/17.

 \@since 0.5.4
 \@category Events

### ⚙ Instance Methods
#### ⚙ onAddRepository(event)


```java
void onAddRepository(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

#### ⚙ onConfigurationChanged(event)


```java
void onConfigurationChanged(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

#### ⚙ onCreateWorkspace(event)


```java
void onCreateWorkspace(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

#### ⚙ onReloadWorkspace(event)


```java
void onReloadWorkspace(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

#### ⚙ onRemoveRepository(event)


```java
void onRemoveRepository(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

#### ⚙ onUpdateProperty(event)


```java
void onUpdateProperty(NutsWorkspaceEvent event)
```
- **NutsWorkspaceEvent event** : 

