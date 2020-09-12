---
id: javadoc_Exception
title: Exception
sidebar_label: Exception
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsAlreadyDeployedException
```java
public net.vpc.app.nuts.NutsAlreadyDeployedException
```
 Exception fired in \{\@link NutsWorkspace#deploy()\} method if the package is
 already deployed Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsAlreadyDeployedException(workspace, id)
Custom Constructor

```java
NutsAlreadyDeployedException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id

#### 🪄 NutsAlreadyDeployedException(workspace, id)
Custom Constructor

```java
NutsAlreadyDeployedException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id

#### 🪄 NutsAlreadyDeployedException(workspace, id, msg, cause)
Custom Constructor

```java
NutsAlreadyDeployedException(NutsWorkspace workspace, String id, String msg, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id
- **String msg** : message
- **Exception cause** : cuse

#### 🪄 NutsAlreadyDeployedException(workspace, id, msg, ex)
Custom Constructor

```java
NutsAlreadyDeployedException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id
- **String msg** : message
- **Exception ex** : exception

## ☕ NutsAlreadyInstalledException
```java
public net.vpc.app.nuts.NutsAlreadyInstalledException
```
 Thrown to indicate that the artifact is already installed and should not be
 reinstalled Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsAlreadyInstalledException(workspace, id)
Custom Constructor

```java
NutsAlreadyInstalledException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id

#### 🪄 NutsAlreadyInstalledException(workspace, id)
Custom Constructor

```java
NutsAlreadyInstalledException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id

#### 🪄 NutsAlreadyInstalledException(workspace, id, message, cause)
Custom Constructor

```java
NutsAlreadyInstalledException(NutsWorkspace workspace, NutsId id, String message, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id
- **String message** : message
- **Exception cause** : exception

#### 🪄 NutsAlreadyInstalledException(workspace, id, msg, cause)
Custom Constructor

```java
NutsAlreadyInstalledException(NutsWorkspace workspace, String id, String msg, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id
- **String msg** : message
- **Exception cause** : exception

## ☕ NutsElementNotFoundException
```java
public net.vpc.app.nuts.NutsElementNotFoundException
```
 Generic exception to be thrown when an element is not found.
 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsElementNotFoundException(workspace)
Constructs a new runtime exception with \{\@code null\} as its
 detail message.  The cause is not initialized, and may subsequently be
 initialized by a call to \{\@link #initCause\}.

```java
NutsElementNotFoundException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception

#### 🪄 NutsElementNotFoundException(workspace, cause)
Constructs a new runtime exception with the specified cause and a
 detail message of \<tt\>(cause==null ? null : cause.toString())\</tt\>
 (which typically contains the class and detail message of
 \<tt\>cause\</tt\>).  This constructor is useful for runtime exceptions
 that are little more than wrappers for other throwables.

```java
NutsElementNotFoundException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **Throwable cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

#### 🪄 NutsElementNotFoundException(workspace, message)
Constructs a new runtime exception with the specified detail message.
 The cause is not initialized, and may subsequently be initialized by a
 call to \{\@link #initCause\}.

```java
NutsElementNotFoundException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message. The detail message is saved for
          later retrieval by the {@link #getMessage()} method.

#### 🪄 NutsElementNotFoundException(workspace, message, cause)
Constructs a new runtime exception with the specified detail message and
 cause.  \<p\>Note that the detail message associated with
 \{\@code cause\} is \<i\>not\</i\> automatically incorporated in
 this runtime exception\'s detail message.

```java
NutsElementNotFoundException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message (which is saved for later retrieval
         by the {@link #getMessage()} method).
- **Throwable cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

#### 🪄 NutsElementNotFoundException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new runtime exception with the specified detail
 message, cause, suppression enabled or disabled, and writable
 stack trace enabled or disabled.

```java
NutsElementNotFoundException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message.
- **Throwable cause** : the cause.  (A {@code null} value is permitted,
 and indicates that the cause is nonexistent or unknown.)
- **boolean enableSuppression** : whether or not suppression is enabled
                          or disabled
- **boolean writableStackTrace** : whether or not the stack trace should
                           be writable

## ☕ NutsException
```java
public net.vpc.app.nuts.NutsException
```
 Base Nuts Exception. Parent of all Nuts defined Exceptions.
 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsException(workspace)
Constructs a new runtime exception with \{\@code null\} as its
 detail message.  The cause is not initialized, and may subsequently be
 initialized by a call to \{\@link #initCause\}.

```java
NutsException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception

#### 🪄 NutsException(workspace, cause)
Constructs a new runtime exception with the specified cause and a
 detail message of \<tt\>(cause==null ? null : cause.toString())\</tt\>
 (which typically contains the class and detail message of
 \<tt\>cause\</tt\>).  This constructor is useful for runtime exceptions
 that are little more than wrappers for other throwables.

```java
NutsException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **Throwable cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

#### 🪄 NutsException(workspace, cause)
Constructs a new runtime exception with the specified cause and a
 detail message of \<tt\>(cause==null ? null : cause.toString())\</tt\>
 (which typically contains the class and detail message of
 \<tt\>cause\</tt\>).  This constructor is useful for runtime exceptions
 that are little more than wrappers for other throwables.

```java
NutsException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **IOException cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

#### 🪄 NutsException(workspace, message)
Constructs a new runtime exception with the specified detail message.
 The cause is not initialized, and may subsequently be initialized by a
 call to \{\@link #initCause\}.

```java
NutsException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message. The detail message is saved for
          later retrieval by the {@link #getMessage()} method.

#### 🪄 NutsException(workspace, message, cause)
Constructs a new runtime exception with the specified detail message and
 cause.  \<p\>Note that the detail message associated with
 \{\@code cause\} is \<i\>not\</i\> automatically incorporated in
 this runtime exception\'s detail message.

```java
NutsException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message (which is saved for later retrieval
         by the {@link #getMessage()} method).
- **Throwable cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

#### 🪄 NutsException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new runtime exception with the specified detail
 message, cause, suppression enabled or disabled, and writable
 stack trace enabled or disabled.

```java
NutsException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String message** : the detail message.
- **Throwable cause** : the cause.  (A {@code null} value is permitted,
 and indicates that the cause is nonexistent or unknown.)
- **boolean enableSuppression** : whether or not suppression is enabled
                          or disabled
- **boolean writableStackTrace** : whether or not the stack trace should
                           be writable

### 🎛 Instance Properties
#### 📄🎛 workspace
Returns the workspace of this Nuts Exception.
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsExecutionException
```java
public net.vpc.app.nuts.NutsExecutionException
```
 Standard Execution thrown when an artifact fails to run.

 \@since 0.5.4
 \@category Exception

### 📢❄ Constant Fields
#### 📢❄ DEFAULT_ERROR_EXIT_CODE
```java
public static final int DEFAULT_ERROR_EXIT_CODE = 244
```
### 🪄 Constructors
#### 🪄 NutsExecutionException(workspace)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsExecutionException(workspace, exitCode)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **int exitCode** : exit code

#### 🪄 NutsExecutionException(workspace, cause, exitCode)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, Throwable cause, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause
- **int exitCode** : exit code

#### 🪄 NutsExecutionException(workspace, message, cause)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsExecutionException(workspace, message, exitCode)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, String message, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **int exitCode** : exit code

#### 🪄 NutsExecutionException(workspace, message, cause, exitCode)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **int exitCode** : exit code

#### 🪄 NutsExecutionException(workspace, message, cause, enableSuppression, writableStackTrace, exitCode)
Constructs a new NutsExecutionException exception

```java
NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable
- **int exitCode** : exit code

### 🎛 Instance Properties
#### 📄🎛 exitCode
artifact exit code
```java
[read-only] int public exitCode
public int getExitCode()
```
## ☕ NutsExtensionAlreadyRegisteredException
```java
public net.vpc.app.nuts.NutsExtensionAlreadyRegisteredException
```
 Exception thrown when extension is already registered.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsExtensionAlreadyRegisteredException(workspace, id, installed)
Constructs a new NutsExtensionAlreadyRegisteredException exception

```java
NutsExtensionAlreadyRegisteredException(NutsWorkspace workspace, String id, String installed)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id
- **String installed** : installed id

#### 🪄 NutsExtensionAlreadyRegisteredException(workspace, id, installed, cause)
Constructs a new NutsExtensionAlreadyRegisteredException exception

```java
NutsExtensionAlreadyRegisteredException(NutsWorkspace workspace, String id, String installed, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id
- **String installed** : installed id
- **Throwable cause** : cause

### 🎛 Instance Properties
#### 📄🎛 installed
registered/installed extension
```java
[read-only] String public installed
public String getInstalled()
```
## ☕ NutsExtensionException
```java
public abstract net.vpc.app.nuts.NutsExtensionException
```
 Base exception for Extension related exceptions

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsExtensionException(workspace, extensionId, message, cause)
Constructs a new runtime exception with the specified detail message and
 cause.  \<p\>Note that the detail message associated with
 \{\@code cause\} is \<i\>not\</i\> automatically incorporated in
 this runtime exception\'s detail message.

```java
NutsExtensionException(NutsWorkspace workspace, String extensionId, String message, Throwable cause)
```
- **NutsWorkspace workspace** : the workspace of this Nuts Exception
- **String extensionId** : extension id
- **String message** : the detail message (which is saved for later retrieval
         by the {@link #getMessage()} method). if the message is null, a
         default one is provided
- **Throwable cause** : the cause (which is saved for later retrieval by the
         {@link #getCause()} method).  (A <tt>null</tt> value is
         permitted, and indicates that the cause is nonexistent or
         unknown.)

### 🎛 Instance Properties
#### 📄🎛 id
extension id
```java
[read-only] String public id
public String getId()
```
## ☕ NutsExtensionNotFoundException
```java
public net.vpc.app.nuts.NutsExtensionNotFoundException
```
 Exception thrown when extension could not be resolved.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsExtensionNotFoundException(workspace, missingType, extensionName)
Constructs a new NutsExtensionNotFoundException exception

```java
NutsExtensionNotFoundException(NutsWorkspace workspace, Class missingType, String extensionName)
```
- **NutsWorkspace workspace** : workspace
- **Class missingType** : missing type
- **String extensionName** : extension name

### 🎛 Instance Properties
#### 📄🎛 extensionName
extension name
```java
[read-only] String public extensionName
public String getExtensionName()
```
#### 📄🎛 missingType
missing type
```java
[read-only] Class public missingType
public Class getMissingType()
```
## ☕ NutsFactoryException
```java
public net.vpc.app.nuts.NutsFactoryException
```
 Exception thrown when a component cannot be resolved by the factory.
 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsFactoryException(workspace)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsFactoryException(workspace, cause)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsFactoryException(workspace, cause)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : workspace
- **IOException cause** : cause

#### 🪄 NutsFactoryException(workspace, message)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsFactoryException(workspace, message, cause)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsFactoryException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsFactoryException exception

```java
NutsFactoryException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsFetchModeNotSupportedException
```java
public net.vpc.app.nuts.NutsFetchModeNotSupportedException
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsFetchModeNotSupportedException(workspace, repo, fetchMode, id, message)
Constructs a new NutsFetchModeNotSupportedException exception

```java
NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String message)
```
- **NutsWorkspace workspace** : workspace
- **NutsRepository repo** : repository
- **NutsFetchMode fetchMode** : fetch mode
- **String id** : artifact id
- **String message** : message

#### 🪄 NutsFetchModeNotSupportedException(workspace, repo, fetchMode, id, message, cause)
Constructs a new NutsFetchModeNotSupportedException exception

```java
NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String message, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **NutsRepository repo** : repository
- **NutsFetchMode fetchMode** : fetch mode
- **String id** : artifact id
- **String message** : message
- **Exception cause** : cause

### 🎛 Instance Properties
#### 📄🎛 fetchMode
fetch mode
```java
[read-only] NutsFetchMode public fetchMode
public NutsFetchMode getFetchMode()
```
#### 📄🎛 id
artifact id
```java
[read-only] String public id
public String getId()
```
#### 📄🎛 repositoryName
repository name
```java
[read-only] String public repositoryName
public String getRepositoryName()
```
#### 📄🎛 repositoryUuid
repository uuid
```java
[read-only] String public repositoryUuid
public String getRepositoryUuid()
```
## ☕ NutsIllegalArgumentException
```java
public net.vpc.app.nuts.NutsIllegalArgumentException
```
 Thrown to indicate that a method has been passed an illegal or inappropriate argument.
 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsIllegalArgumentException(workspace)
Constructs a new NutsIllegalArgumentException exception

```java
NutsIllegalArgumentException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsIllegalArgumentException(workspace, cause)
Constructs a new NutsIllegalArgumentException exception

```java
NutsIllegalArgumentException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsIllegalArgumentException(workspace, message)
Constructs a new NutsIllegalArgumentException exception

```java
NutsIllegalArgumentException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsIllegalArgumentException(workspace, message, cause)
Constructs a new NutsIllegalArgumentException exception

```java
NutsIllegalArgumentException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsIllegalArgumentException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsIllegalArgumentException exception

```java
NutsIllegalArgumentException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsInstallException
```java
public net.vpc.app.nuts.NutsInstallException
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsInstallException(workspace, id)
Custom Constructor

```java
NutsInstallException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id

#### 🪄 NutsInstallException(workspace, id)
Custom Constructor

```java
NutsInstallException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id

#### 🪄 NutsInstallException(workspace, id, msg, ex)
Custom Constructor

```java
NutsInstallException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id
- **String msg** : message
- **Exception ex** : exception

#### 🪄 NutsInstallException(workspace, id, msg, ex)
Custom Constructor

```java
NutsInstallException(NutsWorkspace workspace, String id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id
- **String msg** : message
- **Exception ex** : exception

## ☕ NutsInstallationException
```java
public abstract net.vpc.app.nuts.NutsInstallationException
```
 Base exception for installation fails.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsInstallationException(workspace, id, msg, ex)
Custom Constructor

```java
NutsInstallationException(NutsWorkspace workspace, String id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id
- **String msg** : message
- **Exception ex** : exception

#### 🪄 NutsInstallationException(workspace, id, msg, ex)
Custom Constructor

```java
NutsInstallationException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id
- **String msg** : message
- **Exception ex** : exception

### 🎛 Instance Properties
#### 📄🎛 id
nuts id
```java
[read-only] String public id
public String getId()
```
## ☕ NutsInvalidRepositoryException
```java
public net.vpc.app.nuts.NutsInvalidRepositoryException
```
 This Exception is thrown when the repository fails to initialize.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsInvalidRepositoryException(workspace, repository, message)
Constructs a new NutsInvalidRepositoryException exception

```java
NutsInvalidRepositoryException(NutsWorkspace workspace, String repository, String message)
```
- **NutsWorkspace workspace** : workspace
- **String repository** : repository
- **String message** : message

## ☕ NutsInvalidWorkspaceException
```java
public net.vpc.app.nuts.NutsInvalidWorkspaceException
```
 This Exception is thrown when the workspace fails to initialize.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsInvalidWorkspaceException(workspace, workspaceLocation, errorMessage)
Constructs a new NutsInvalidWorkspaceException exception

```java
NutsInvalidWorkspaceException(NutsWorkspace workspace, String workspaceLocation, String errorMessage)
```
- **NutsWorkspace workspace** : workspace
- **String workspaceLocation** : workspaceLocation
- **String errorMessage** : errorMessage

### 🎛 Instance Properties
#### 📄🎛 workspaceLocation
workspace location
```java
[read-only] String public workspaceLocation
public String getWorkspaceLocation()
```
## ☕ NutsLockAcquireException
```java
public net.vpc.app.nuts.NutsLockAcquireException
```
 Exception Thrown when a locked object is invoked.
 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsLockAcquireException(workspace, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockAcquireException(NutsWorkspace workspace, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **Object lockedObject** : locked object
- **Object lockObject** : lock Object

#### 🪄 NutsLockAcquireException(workspace, message, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockAcquireException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockAcquireException(workspace, message, lockedObject, lockObject, cause)
Constructs a new ock exception.

```java
NutsLockAcquireException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object
- **Throwable cause** : cause

## ☕ NutsLockBarrierException
```java
public net.vpc.app.nuts.NutsLockBarrierException
```
 Exception Thrown when a locked object is invoked.
 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsLockBarrierException(workspace, lockedObject, lockObject)
Constructs a new lock exception.

```java
NutsLockBarrierException(NutsWorkspace workspace, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockBarrierException(workspace, message, lockedObject, lockObject)
Constructs a new lock exception.

```java
NutsLockBarrierException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockBarrierException(workspace, message, lockedObject, lockObject, cause)
Constructs a new lock exception.

```java
NutsLockBarrierException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object
- **Throwable cause** : cause

## ☕ NutsLockException
```java
public net.vpc.app.nuts.NutsLockException
```
 Exception Thrown when a locked object is invoked.
 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsLockException(workspace, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockException(NutsWorkspace workspace, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockException(workspace, message, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockException(workspace, message, lockedObject, lockObject, cause)
Constructs a new ock exception.

```java
NutsLockException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object
- **Throwable cause** : cause

### 🎛 Instance Properties
#### 📄🎛 lockObject
return lock object
```java
[read-only] Object public lockObject
public Object getLockObject()
```
#### 📄🎛 lockedObject
return locked object
```java
[read-only] Object public lockedObject
public Object getLockedObject()
```
## ☕ NutsLockReleaseException
```java
public net.vpc.app.nuts.NutsLockReleaseException
```
 Exception Thrown when a locked object is invoked.
 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsLockReleaseException(workspace, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockReleaseException(NutsWorkspace workspace, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockReleaseException(workspace, message, lockedObject, lockObject)
Constructs a new ock exception.

```java
NutsLockReleaseException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object

#### 🪄 NutsLockReleaseException(workspace, message, lockedObject, lockObject, cause)
Constructs a new ock exception.

```java
NutsLockReleaseException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message or null
- **Object lockedObject** : locked Object
- **Object lockObject** : lock Object
- **Throwable cause** : cause

## ☕ NutsLoginException
```java
public net.vpc.app.nuts.NutsLoginException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsLoginException(workspace)
Constructs a new NutsLoginException exception

```java
NutsLoginException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsLoginException(workspace, cause)
Constructs a new NutsLoginException exception

```java
NutsLoginException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsLoginException(workspace, message)
Constructs a new NutsLoginException exception

```java
NutsLoginException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsLoginException(workspace, message, cause)
Constructs a new NutsLoginException exception

```java
NutsLoginException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsLoginException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsLoginException exception

```java
NutsLoginException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsNotExecutableException
```java
public net.vpc.app.nuts.NutsNotExecutableException
```
 Exception thrown when a non executable nuts id is requested to run.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsNotExecutableException(workspace, id)
Constructs a new NutsNotExecutableException exception

```java
NutsNotExecutableException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id

#### 🪄 NutsNotExecutableException(workspace, id)
Constructs a new NutsNotExecutableException exception

```java
NutsNotExecutableException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id

### 🎛 Instance Properties
#### 📄🎛 id
artifact id
```java
[read-only] String public id
public String getId()
```
## ☕ NutsNotFoundException
```java
public net.vpc.app.nuts.NutsNotFoundException
```
 Exception thrown when the package could not be resolved

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsNotFoundException(workspace, id)
Constructs a new NutsNotFoundException exception

```java
NutsNotFoundException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id

#### 🪄 NutsNotFoundException(workspace, id)
Constructs a new NutsNotFoundException exception

```java
NutsNotFoundException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id

#### 🪄 NutsNotFoundException(workspace, id, cause)
Constructs a new NutsNotFoundException exception

```java
NutsNotFoundException(NutsWorkspace workspace, NutsId id, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id
- **Exception cause** : cause

#### 🪄 NutsNotFoundException(workspace, id, message, cause)
Constructs a new NutsNotFoundException exception

```java
NutsNotFoundException(NutsWorkspace workspace, String id, String message, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id
- **String message** : message
- **Exception cause** : cause

#### 🪄 NutsNotFoundException(workspace, id, message, cause)
Constructs a new NutsNotFoundException exception

```java
NutsNotFoundException(NutsWorkspace workspace, NutsId id, String message, Exception cause)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id
- **String message** : message
- **Exception cause** : cause

### 🎛 Instance Properties
#### 📄🎛 id
artifact id
```java
[read-only] String public id
public String getId()
```
## ☕ NutsNotInstallableException
```java
public net.vpc.app.nuts.NutsNotInstallableException
```
 This exception is thrown when an artifact fails to be installed.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsNotInstallableException(workspace, id)
Constructs a new NutsNotInstallableException exception

```java
NutsNotInstallableException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact

#### 🪄 NutsNotInstallableException(workspace, id)
Constructs a new NutsNotInstallableException exception

```java
NutsNotInstallableException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact

#### 🪄 NutsNotInstallableException(workspace, id, msg, ex)
Constructs a new NutsNotInstallableException exception

```java
NutsNotInstallableException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact
- **String msg** : message
- **Exception ex** : exception

#### 🪄 NutsNotInstallableException(workspace, id, msg, ex)
Constructs a new NutsNotInstallableException exception

```java
NutsNotInstallableException(NutsWorkspace workspace, String id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact
- **String msg** : message
- **Exception ex** : exception

## ☕ NutsNotInstalledException
```java
public net.vpc.app.nuts.NutsNotInstalledException
```
 This Exception is fired when an artifact fails to be uninstalled for the artifact not being installed yet.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsNotInstalledException(workspace, id)
Constructs a new NutsNotInstalledException exception

```java
NutsNotInstalledException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact

#### 🪄 NutsNotInstalledException(workspace, id)
Constructs a new NutsNotInstalledException exception

```java
NutsNotInstalledException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact

#### 🪄 NutsNotInstalledException(workspace, id, msg, ex)
Constructs a new NutsNotInstalledException exception

```java
NutsNotInstalledException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact
- **String msg** : message
- **Exception ex** : error

#### 🪄 NutsNotInstalledException(workspace, id, msg, ex)
Constructs a new NutsNotInstalledException exception

```java
NutsNotInstalledException(NutsWorkspace workspace, String id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact
- **String msg** : message
- **Exception ex** : exception

## ☕ NutsParseEnumException
```java
public net.vpc.app.nuts.NutsParseEnumException
```
 Exception Thrown when for any reason, the enum value is not expected/supported.
 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsParseEnumException(workspace, invalidValue, enumType)
create new instance of NutsUnexpectedEnumException

```java
NutsParseEnumException(NutsWorkspace workspace, String invalidValue, Class enumType)
```
- **NutsWorkspace workspace** : workspace
- **String invalidValue** : invalid value
- **Class enumType** : enumeration instance (cannot be null)

#### 🪄 NutsParseEnumException(workspace, message, invalidValue, enumType)
create new instance of NutsUnexpectedEnumException

```java
NutsParseEnumException(NutsWorkspace workspace, String message, String invalidValue, Class enumType)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **String invalidValue** : invalid value
- **Class enumType** : enumeration instance (cannot be null)

### 🎛 Instance Properties
#### 📄🎛 enumType
enum type
```java
[read-only] Class public enumType
public Class getEnumType()
```
#### 📄🎛 invalidValue
return invalid value
```java
[read-only] String public invalidValue
public String getInvalidValue()
```
## ☕ NutsParseException
```java
public net.vpc.app.nuts.NutsParseException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsParseException(workspace)
Constructs a new NutsParseException exception

```java
NutsParseException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsParseException(workspace, cause)
Constructs a new NutsParseException exception

```java
NutsParseException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsParseException(workspace, message)
Constructs a new NutsParseException exception

```java
NutsParseException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsParseException(workspace, message, cause)
Constructs a new NutsParseException exception

```java
NutsParseException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsParseException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsParseException exception

```java
NutsParseException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsPushException
```java
public net.vpc.app.nuts.NutsPushException
```
 Push Exception

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsPushException(workspace, id)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id

#### 🪄 NutsPushException(workspace, id)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id

#### 🪄 NutsPushException(workspace, id, message)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, String id, String message)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id
- **String message** : message

#### 🪄 NutsPushException(workspace, id, message)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, NutsId id, String message)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id
- **String message** : message

#### 🪄 NutsPushException(workspace, id, message, cause)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, String id, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String id** : artifact id
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsPushException(workspace, id, message, cause)
Constructs a new NutsPushException exception

```java
NutsPushException(NutsWorkspace workspace, NutsId id, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : artifact id
- **String message** : message
- **Throwable cause** : cause

### 🎛 Instance Properties
#### 📄🎛 id
artifact id
```java
[read-only] String public id
public String getId()
```
## ☕ NutsReadOnlyException
```java
public net.vpc.app.nuts.NutsReadOnlyException
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsReadOnlyException(workspace)
Constructs a new NutsReadOnlyException exception

```java
NutsReadOnlyException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsReadOnlyException(workspace, location)
Constructs a new NutsReadOnlyException exception

```java
NutsReadOnlyException(NutsWorkspace workspace, String location)
```
- **NutsWorkspace workspace** : workspace
- **String location** : location

## ☕ NutsRepositoryAlreadyRegisteredException
```java
public net.vpc.app.nuts.NutsRepositoryAlreadyRegisteredException
```
 This exception is thrown when a repository location could no be loaded because
  the repository is already registered for the actual workspace.
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsRepositoryAlreadyRegisteredException(workspace, repository)
Constructs a new NutsNotInstalledException exception

```java
NutsRepositoryAlreadyRegisteredException(NutsWorkspace workspace, String repository)
```
- **NutsWorkspace workspace** : workspace
- **String repository** : repository

#### 🪄 NutsRepositoryAlreadyRegisteredException(workspace, repository, err)
Constructs a new NutsNotInstalledException exception

```java
NutsRepositoryAlreadyRegisteredException(NutsWorkspace workspace, String repository, Throwable err)
```
- **NutsWorkspace workspace** : workspace
- **String repository** : repository
- **Throwable err** : error

## ☕ NutsRepositoryException
```java
public abstract net.vpc.app.nuts.NutsRepositoryException
```
 Base exception for Repository related exceptions

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsRepositoryException(workspace, repository, message, ex)
Constructs a new NutsRepositoryException exception

```java
NutsRepositoryException(NutsWorkspace workspace, String repository, String message, Throwable ex)
```
- **NutsWorkspace workspace** : workspace
- **String repository** : repository
- **String message** : message
- **Throwable ex** : exception

### 🎛 Instance Properties
#### 📄🎛 repository
the repository of this exception
```java
[read-only] String public repository
public String getRepository()
```
## ☕ NutsRepositoryNotFoundException
```java
public net.vpc.app.nuts.NutsRepositoryNotFoundException
```
 This exception is thrown when a repository location could no be loaded because
 the repository config files are missing.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsRepositoryNotFoundException(workspace, repository)
Constructs a new NutsRepositoryNotFoundException exception

```java
NutsRepositoryNotFoundException(NutsWorkspace workspace, String repository)
```
- **NutsWorkspace workspace** : workspace
- **String repository** : repository

## ☕ NutsSecurityException
```java
public net.vpc.app.nuts.NutsSecurityException
```
 Thrown by Nuts Workspace to indicate a security violation.
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsSecurityException(workspace)
Constructs a \<code\>NutsSecurityException\</code\> with the specified
 parameters.

```java
NutsSecurityException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsSecurityException(workspace, cause)
Constructs a \<code\>NutsSecurityException\</code\> with the specified
 parameters.

```java
NutsSecurityException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsSecurityException(workspace, message)
Constructs a \<code\>NutsSecurityException\</code\> with the specified
 parameters.

```java
NutsSecurityException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : the detail message.

#### 🪄 NutsSecurityException(workspace, message, cause)
Constructs a \<code\>NutsSecurityException\</code\> with the specified
 parameters.

```java
NutsSecurityException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

### 🎛 Instance Properties
#### 📄🎛 workspace
current workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsTooManyElementsException
```java
public net.vpc.app.nuts.NutsTooManyElementsException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsTooManyElementsException(workspace)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsTooManyElementsException(workspace, cause)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsTooManyElementsException(workspace, cause)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : workspace
- **IOException cause** : cause

#### 🪄 NutsTooManyElementsException(workspace, message)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsTooManyElementsException(workspace, message, cause)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsTooManyElementsException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsTooManyElementsException exception

```java
NutsTooManyElementsException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsUnexpectedException
```java
public net.vpc.app.nuts.NutsUnexpectedException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUnexpectedException(workspace)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsUnexpectedException(workspace, cause)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsUnexpectedException(workspace, cause)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : workspace
- **IOException cause** : cause

#### 🪄 NutsUnexpectedException(workspace, message)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsUnexpectedException(workspace, message, cause)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsUnexpectedException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsUnexpectedException exception

```java
NutsUnexpectedException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsUninstallException
```java
public net.vpc.app.nuts.NutsUninstallException
```
 This Exception is thrown when an artifact fails to be uninstalled

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUninstallException(workspace, id)
Custom Constructor

```java
NutsUninstallException(NutsWorkspace workspace, NutsId id)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id

#### 🪄 NutsUninstallException(workspace, id)
Custom Constructor

```java
NutsUninstallException(NutsWorkspace workspace, String id)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id

#### 🪄 NutsUninstallException(workspace, id, msg, ex)
Custom Constructor

```java
NutsUninstallException(NutsWorkspace workspace, NutsId id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **NutsId id** : nuts id
- **String msg** : message
- **Exception ex** : exception

#### 🪄 NutsUninstallException(workspace, id, msg, ex)
Custom Constructor

```java
NutsUninstallException(NutsWorkspace workspace, String id, String msg, Exception ex)
```
- **NutsWorkspace workspace** : workspace
- **String id** : nuts id
- **String msg** : message
- **Exception ex** : exception

## ☕ NutsUnsatisfiedRequirementsException
```java
public net.vpc.app.nuts.NutsUnsatisfiedRequirementsException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUnsatisfiedRequirementsException(workspace)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsUnsatisfiedRequirementsException(workspace, cause)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsUnsatisfiedRequirementsException(workspace, cause)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : workspace
- **IOException cause** : cause

#### 🪄 NutsUnsatisfiedRequirementsException(workspace, message)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsUnsatisfiedRequirementsException(workspace, message, cause)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsUnsatisfiedRequirementsException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsUnsatisfiedRequirementsException exception

```java
NutsUnsatisfiedRequirementsException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsUnsupportedArgumentException
```java
public net.vpc.app.nuts.NutsUnsupportedArgumentException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUnsupportedArgumentException(workspace)
Constructs a new NutsUnsupportedArgumentException exception

```java
NutsUnsupportedArgumentException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsUnsupportedArgumentException(workspace, cause)
Constructs a new NutsUnsupportedArgumentException exception

```java
NutsUnsupportedArgumentException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsUnsupportedArgumentException(workspace, message)
Constructs a new NutsUnsupportedArgumentException exception

```java
NutsUnsupportedArgumentException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsUnsupportedArgumentException(workspace, message, cause)
Constructs a new NutsUnsupportedArgumentException exception

```java
NutsUnsupportedArgumentException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsUnsupportedArgumentException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsUnsupportedArgumentException exception

```java
NutsUnsupportedArgumentException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsUnsupportedEnumException
```java
public net.vpc.app.nuts.NutsUnsupportedEnumException
```
 Exception Thrown when for any reason, the enum value is not expected/supported.

 \@author vpc
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUnsupportedEnumException(workspace, enumValue)
create new instance of NutsUnexpectedEnumException

```java
NutsUnsupportedEnumException(NutsWorkspace workspace, Enum enumValue)
```
- **NutsWorkspace workspace** : workspace
- **Enum enumValue** : enumeration instance (cannot be null)

#### 🪄 NutsUnsupportedEnumException(workspace, message, enumValue)
create new instance of NutsUnexpectedEnumException

```java
NutsUnsupportedEnumException(NutsWorkspace workspace, String message, Enum enumValue)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Enum enumValue** : enumeration instance (cannot be null)

#### 🪄 NutsUnsupportedEnumException(workspace, message, stringValue, enumValue)
create new instance of NutsUnexpectedEnumException

```java
NutsUnsupportedEnumException(NutsWorkspace workspace, String message, String stringValue, Enum enumValue)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **String stringValue** : invalid value
- **Enum enumValue** : enumeration instance (cannot be null)

### 🎛 Instance Properties
#### 📄🎛 enumValue
enum value
```java
[read-only] Enum public enumValue
public Enum getEnumValue()
```
## ☕ NutsUnsupportedOperationException
```java
public net.vpc.app.nuts.NutsUnsupportedOperationException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsUnsupportedOperationException(workspace)
Constructs a new NutsUnsupportedOperationException exception

```java
NutsUnsupportedOperationException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsUnsupportedOperationException(workspace, cause)
Constructs a new NutsUnsupportedOperationException exception

```java
NutsUnsupportedOperationException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsUnsupportedOperationException(workspace, message)
Constructs a new NutsUnsupportedOperationException exception

```java
NutsUnsupportedOperationException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsUnsupportedOperationException(workspace, message, cause)
Constructs a new NutsUnsupportedOperationException exception

```java
NutsUnsupportedOperationException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsUnsupportedOperationException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsUnsupportedOperationException exception

```java
NutsUnsupportedOperationException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsUserCancelException
```java
public net.vpc.app.nuts.NutsUserCancelException
```

 \@author vpc
 \@since 0.5.4
 \@category Exception

### 📢❄ Constant Fields
#### 📢❄ DEFAULT_CANCEL_EXIT_CODE
```java
public static final int DEFAULT_CANCEL_EXIT_CODE = 245
```
### 🪄 Constructors
#### 🪄 NutsUserCancelException(workspace)
Constructs a new NutsUserCancelException exception

```java
NutsUserCancelException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsUserCancelException(workspace, message)
Constructs a new NutsUserCancelException exception

```java
NutsUserCancelException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsUserCancelException(workspace, message, exitCode)
Constructs a new NutsUserCancelException exception

```java
NutsUserCancelException(NutsWorkspace workspace, String message, int exitCode)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **int exitCode** : exit code

## ☕ NutsValidationException
```java
public net.vpc.app.nuts.NutsValidationException
```

 \@author vpc
 \@since 0.5.5
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsValidationException(workspace)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace)
```
- **NutsWorkspace workspace** : workspace

#### 🪄 NutsValidationException(workspace, cause)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **Throwable cause** : cause

#### 🪄 NutsValidationException(workspace, cause)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace, IOException cause)
```
- **NutsWorkspace workspace** : workspace
- **IOException cause** : cause

#### 🪄 NutsValidationException(workspace, message)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace, String message)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message

#### 🪄 NutsValidationException(workspace, message, cause)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace, String message, Throwable cause)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause

#### 🪄 NutsValidationException(workspace, message, cause, enableSuppression, writableStackTrace)
Constructs a new NutsValidationException exception

```java
NutsValidationException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable cause** : cause
- **boolean enableSuppression** : whether or not suppression is enabled or disabled
- **boolean writableStackTrace** : whether or not the stack trace should be writable

## ☕ NutsWorkspaceAlreadyExistsException
```java
public net.vpc.app.nuts.NutsWorkspaceAlreadyExistsException
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsWorkspaceAlreadyExistsException(workspace, workspaceLocation)
Constructs a new NutsWorkspaceAlreadyExistsException exception

```java
NutsWorkspaceAlreadyExistsException(NutsWorkspace workspace, String workspaceLocation)
```
- **NutsWorkspace workspace** : workspace
- **String workspaceLocation** : location

#### 🪄 NutsWorkspaceAlreadyExistsException(workspace, workspaceLocation, err)
Constructs a new NutsWorkspaceAlreadyExistsException exception

```java
NutsWorkspaceAlreadyExistsException(NutsWorkspace workspace, String workspaceLocation, Throwable err)
```
- **NutsWorkspace workspace** : workspace
- **String workspaceLocation** : location
- **Throwable err** : exception

### 🎛 Instance Properties
#### 📄🎛 workspaceLocation
workspace location
```java
[read-only] String public workspaceLocation
public String getWorkspaceLocation()
```
## ☕ NutsWorkspaceException
```java
public abstract net.vpc.app.nuts.NutsWorkspaceException
```
 NutsWorkspaceException is the base class for Workspace related exceptions.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsWorkspaceException(workspace, message, ex)
Constructs a new NutsWorkspaceException exception

```java
NutsWorkspaceException(NutsWorkspace workspace, String message, Throwable ex)
```
- **NutsWorkspace workspace** : workspace
- **String message** : message
- **Throwable ex** : exception

## ☕ NutsWorkspaceNotFoundException
```java
public net.vpc.app.nuts.NutsWorkspaceNotFoundException
```
 This Exception is thrown when the workspace does not exist.

 \@since 0.5.4
 \@category Exception

### 🪄 Constructors
#### 🪄 NutsWorkspaceNotFoundException(workspace, workspaceLocation)
Constructs a new NutsWorkspaceNotFoundException exception

```java
NutsWorkspaceNotFoundException(NutsWorkspace workspace, String workspaceLocation)
```
- **NutsWorkspace workspace** : workspace
- **String workspaceLocation** : location

### 🎛 Instance Properties
#### 📄🎛 workspaceLocation
workspace location
```java
[read-only] String public workspaceLocation
public String getWorkspaceLocation()
```
