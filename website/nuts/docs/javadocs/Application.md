---
id: javadoc_Application
title: Application
sidebar_label: Application
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsApplication
```java
public abstract net.vpc.app.nuts.NutsApplication
```
 Nuts Application is the Top Level class to be handled by nuts as rich console
 application. By default NutsApplication classes :
 \<ul\>
 \<li\>have a nutsApplication=true in their descriptor file\</li\>
 \<li\>support inheritance of all workspace options (from caller nuts
 process)\</li\>
 \<li\>enables auto-complete mode to help forecasting the next token in the
 command line\</li\>
 \<li\>enables install mode to be executed when the jar is installed in nuts
 repos\</li\>
 \<li\>enables uninstall mode to be executed when the jar is uninstaleld from
 nuts repos\</li\>
 \<li\>enables update mode to be executed when the a new version of the same jar
 has been installed\</li\>
 \<li\>have many default options enabled (such as --help, --version, --json,
 --table, etc.) and thus support natively multi output channels\</li\>
 \</ul\>
 Typically a Nuts Application follows this code pattern :
 \<pre\>
   public class MyApplication extends NutsApplication\{
     public static void main(String[] args) \{
         // just create an instance and call runAndExit in the main method
         new MyApplication().runAndExit(args);
     \}
     // do the main staff in launch method
     public void run(NutsApplicationContext appContext) \{
         boolean myBooleanOption=false;
         NutsCommandLine cmdLine=appContext.getCommandLine()
         boolean boolOption=false;
         String stringOption=null;
         Argument a;
         while(cmdLine.hasNext())\{
             if(appContext.configureFirst(cmdLine))\{
                 //do nothing
             \}else \{
                  a=cmdLine.peek();
                  switch(a.getStringKey())[
                      case "-o": case "--option":\{
                          boolOption=cmdLine.nextBoolean().getBooleanValue();
                          break;
                      \}
                      case "-n": case "--name":\{
                          stringOption=cmdLine.nextString().getStringValue();
                          break;
                      \}
                      default:\{
                          cmdLine.unexpectedArgument();
                      \}
                  \}
             \}
         \}
         // test if application is running in exec mode
         // (and not in autoComplete mode)
         if(cmdLine.isExecMode())\{
              //do the good staff here
         \}
     \}
   \}
 \</pre\>
 another example of using this class is :
 \<pre\>
     public class HLMain extends NutsApplication \{
         public static void main(String[] args) \{
            // just create an instance and call runAndExit in the main method
            new HLMain().runAndExit(args);
         \}

         &#64;Override
         public void run(NutsApplicationContext applicationContext) \{
             applicationContext.processCommandLine(new NutsCommandLineProcessor() \{
                 HLCWithOptions hl = new HL().withOptions();
                 boolean noMoreOptions=false;
                 &#64;Override
                 public boolean processOption(NutsArgument argument, NutsCommandLine cmdLine) \{
                     if(!noMoreOptions)\{
                         return false;
                     \}
                     switch (argument.getStringKey()) \{
                         case "--clean": \{
                             hl.clean(cmdLine.nextBoolean().getBooleanValue());
                             return true;
                         \}
                         case "-i":
                         case "--incremental":\{
                             hl.setIncremental(cmdLine.nextBoolean().getBooleanValue());
                             return true;
                         \}
                         case "-r":
                         case "--root":\{
                             hl.setProjectRoot(cmdLine.nextString().getStringValue());
                             return true;
                         \}
                     \}
                     return false;
                 \}

                 &#64;Override
                 public boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine) \{
                     String s = argument.getString();
                     if(isURL(s))\{
                         hl.includeFileURL(s);
                     \}else\{
                         hl.includeFile(s);
                     \}
                     noMoreOptions=true;
                     return true;
                 \}

                 private boolean isURL(String s) \{
                     return
                             s.startsWith("file:")
                             ||s.startsWith("http:")
                             ||s.startsWith("https:")
                             ;
                 \}

                 &#64;Override
                 public void exec() \{
                     hl.compile();
                 \}
             \});
         \}
     \}
 \</pre\>

 \@since 0.5.5
 \@category Application

### 🪄 Constructors
#### 🪄 NutsApplication()


```java
NutsApplication()
```

### ⚙ Instance Methods
#### ⚙ createApplicationContext(ws, args, startTimeMillis)
create application context or return null for default

```java
NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis)
```
**return**:NutsApplicationContext
- **NutsWorkspace ws** : workspace
- **String[] args** : arguments
- **long startTimeMillis** : start time

#### ⚙ onInstallApplication(applicationContext)
this method should be overridden to perform specific business when
 application is installed

```java
void onInstallApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ onUninstallApplication(applicationContext)
this method should be overridden to perform specific business when
 application is uninstalled

```java
void onUninstallApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ onUpdateApplication(applicationContext)
this method should be overridden to perform specific business when
 application is updated

```java
void onUpdateApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ run(applicationContext)
run application within the given context

```java
void run(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : app context

#### ⚙ run(args)
run the application with the given arguments. If the first arguments is
 in the form of --nuts-exec-mode=... the argument will be removed and the
 corresponding mode is activated.

```java
void run(String[] args)
```
- **String[] args** : application arguments. should not be null or contain nulls

#### ⚙ run(session, args)
run the application with the given arguments against the given workspace
 If the first arguments is in the form of --nuts-exec-mode=... the
 argument will be removed and the corresponding mode is activated.

```java
void run(NutsSession session, String[] args)
```
- **NutsSession session** : session (can be null)
- **String[] args** : application arguments. should not be null or contain nulls

#### ⚙ runAndExit(args)
run the application and \<strong\>EXIT\</strong\> process

```java
void runAndExit(String[] args)
```
- **String[] args** : arguments

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsApplicationContext
```java
public interface net.vpc.app.nuts.NutsApplicationContext
```
 Application context that store all relevant information about application
 execution mode, workspace, etc.

 \@author vpc
 \@since 0.5.5
 \@category Application

### 📢❄ Constant Fields
#### 📢❄ AUTO_COMPLETE_CANDIDATE_PREFIX
```java
public static final String AUTO_COMPLETE_CANDIDATE_PREFIX = "@@Candidate@@: "
```
### 🎛 Instance Properties
#### 📄🎛 appClass
application class reference
```java
[read-only] Class public appClass
public Class getAppClass()
```
#### 📄🎛 appId
application nuts id
```java
[read-only] NutsId public appId
public NutsId getAppId()
```
#### 📄🎛 appPreviousVersion
previous version (applicable in update mode)
```java
[read-only] NutsVersion public appPreviousVersion
public NutsVersion getAppPreviousVersion()
```
#### 📄🎛 appVersion
application version
```java
[read-only] NutsVersion public appVersion
public NutsVersion getAppVersion()
```
#### 📄🎛 appsFolder
path to the apps folder of this application
```java
[read-only] Path public appsFolder
public Path getAppsFolder()
```
#### 📄🎛 arguments
application arguments
```java
[read-only] String[] public arguments
public String[] getArguments()
```
#### 📄🎛 autoComplete
Auto complete instance associated with the
 \{\@link NutsApplicationMode#AUTO_COMPLETE\} mode
```java
[read-only] NutsCommandAutoComplete public autoComplete
public NutsCommandAutoComplete getAutoComplete()
```
#### 📄🎛 cacheFolder
path to the cache files folder of this application
```java
[read-only] Path public cacheFolder
public Path getCacheFolder()
```
#### 📄🎛 commandLine
a new instance of command line arguments to process filled 
 with application\'s arguments.
```java
[read-only] NutsCommandLine public commandLine
public NutsCommandLine getCommandLine()
```
#### 📄🎛 configFolder
path to the configuration folder of this application
```java
[read-only] Path public configFolder
public Path getConfigFolder()
```
#### 📄🎛 execMode
return true if \{\@code getAutoComplete()==null \}
```java
[read-only] boolean public execMode
public boolean isExecMode()
```
#### 📄🎛 libFolder
path to the libraries files (non applications) folder of this application
```java
[read-only] Path public libFolder
public Path getLibFolder()
```
#### 📄🎛 logFolder
path to the log folder of this application
```java
[read-only] Path public logFolder
public Path getLogFolder()
```
#### 📄🎛 mode
application execution mode
```java
[read-only] NutsApplicationMode public mode
public NutsApplicationMode getMode()
```
#### 📄🎛 modeArguments
application execution mode extra arguments
```java
[read-only] String[] public modeArguments
public String[] getModeArguments()
```
#### 📄🎛 runFolder
path to the temporary run files (non essential sockets etc...) folder of
 this application
```java
[read-only] Path public runFolder
public Path getRunFolder()
```
#### 📝🎛 session
update session
```java
[read-write] NutsApplicationContext public session
public NutsSession getSession()
public NutsApplicationContext setSession(session)
```
#### 📄🎛 sharedAppsFolder

```java
[read-only] Path public sharedAppsFolder
public Path getSharedAppsFolder()
```
#### 📄🎛 sharedConfigFolder

```java
[read-only] Path public sharedConfigFolder
public Path getSharedConfigFolder()
```
#### 📄🎛 sharedFolder

```java
[read-only] Path public sharedFolder
public Path getSharedFolder(location)
```
#### 📄🎛 sharedLibFolder

```java
[read-only] Path public sharedLibFolder
public Path getSharedLibFolder()
```
#### 📄🎛 sharedLogFolder

```java
[read-only] Path public sharedLogFolder
public Path getSharedLogFolder()
```
#### 📄🎛 sharedRunFolder

```java
[read-only] Path public sharedRunFolder
public Path getSharedRunFolder()
```
#### 📄🎛 sharedTempFolder

```java
[read-only] Path public sharedTempFolder
public Path getSharedTempFolder()
```
#### 📄🎛 sharedVarFolder

```java
[read-only] Path public sharedVarFolder
public Path getSharedVarFolder()
```
#### 📄🎛 startTimeMillis
application start time in milli-seconds
```java
[read-only] long public startTimeMillis
public long getStartTimeMillis()
```
#### 📄🎛 tempFolder
path to the temporary files folder of this application
```java
[read-only] Path public tempFolder
public Path getTempFolder()
```
#### 📄🎛 varFolder
path to the variable files (aka /var in POSIX systems) folder of this
 application
```java
[read-only] Path public varFolder
public Path getVarFolder()
```
#### 📄🎛 workspace
current workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsApplicationContext configure(boolean skipUnsupported, String[] args)
```
**return**:NutsApplicationContext
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ getFolder(location)
application store folder path for the given \{\@code location\}

```java
Path getFolder(NutsStoreLocation location)
```
**return**:Path
- **NutsStoreLocation location** : location type

#### ⚙ printHelp()
print application help to the default out (\{\@code getSession().out()\}) 
 print stream.

```java
void printHelp()
```

#### ⚙ processCommandLine(commandLineProcessor)
create new NutsCommandLine and consume it with the given processor.
 This method is equivalent to the following code
 \<pre\>
         NutsCommandLine cmdLine=getCommandLine();
         NutsArgument a;
         while (cmdLine.hasNext()) \{
             if (!this.configureFirst(cmdLine)) \{
                 a = cmdLine.peek();
                 if(a.isOption())\{
                     if(!commandLineProcessor.processOption(a,cmdLine))\{
                         cmdLine.unexpectedArgument();
                     \}
                 \}else\{
                     if(!commandLineProcessor.processNonOption(a,cmdLine))\{
                         cmdLine.unexpectedArgument();
                     \}
                 \}
             \}
         \}
         // test if application is running in exec mode
         // (and not in autoComplete mode)
         if (cmdLine.isExecMode()) \{
             //do the good staff here
             commandLineProcessor.exec();
         \}
 \</pre\>

 This as an example of its usage
 \<pre\>
     applicationContext.processCommandLine(new NutsCommandLineProcessor() \{
             HLCWithOptions hl = new HL().withOptions();
             boolean noMoreOptions=false;
             &#64;Override
             public boolean processOption(NutsArgument argument, NutsCommandLine cmdLine) \{
                 if(!noMoreOptions)\{
                     return false;
                 \}
                 switch (argument.getStringKey()) \{
                     case "--clean": \{
                         hl.clean(cmdLine.nextBoolean().getBooleanValue());
                         return true;
                     \}
                     case "-i":
                     case "--incremental":\{
                         hl.setIncremental(cmdLine.nextBoolean().getBooleanValue());
                         return true;
                     \}
                     case "-r":
                     case "--root":\{
                         hl.setProjectRoot(cmdLine.nextString().getStringValue());
                         return true;
                     \}
                 \}
                 return false;
             \}

             &#64;Override
             public boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine) \{
                 String s = argument.getString();
                 if(isURL(s))\{
                     hl.includeFileURL(s);
                 \}else\{
                     hl.includeFile(s);
                 \}
                 noMoreOptions=true;
                 return true;
             \}

             private boolean isURL(String s) \{
                 return
                         s.startsWith("file:")
                         ||s.startsWith("http:")
                         ||s.startsWith("https:")
                         ;
             \}

             &#64;Override
             public void exec() \{
                 hl.compile();
             \}
         \});
 \</pre\>

```java
void processCommandLine(NutsCommandLineProcessor commandLineProcessor)
```
- **NutsCommandLineProcessor commandLineProcessor** : commandLineProcessor

## ☕ NutsApplicationLifeCycle
```java
public interface net.vpc.app.nuts.NutsApplicationLifeCycle
```
 Application Life Cycle interface define methods to be overridden to
 perform specific business for each of the predefined application execution 
 modes \{\@link NutsApplicationMode\}.

 \@author vpc
 \@since 0.5.5
 \@category Application

### ⚙ Instance Methods
#### ⚙ createApplicationContext(ws, args, startTimeMillis)
this method should be implemented to create specific ApplicationContext
 implementation or return null to use default one.

```java
NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis)
```
**return**:NutsApplicationContext
- **NutsWorkspace ws** : workspace
- **String[] args** : application arguments
- **long startTimeMillis** : start time in milliseconds

#### ⚙ onInstallApplication(applicationContext)
this method should be implemented to perform specific business when
 application is installed.

```java
void onInstallApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ onRunApplication(applicationContext)
this method should be implemented to perform specific business when
 application is running (default mode)

```java
void onRunApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ onUninstallApplication(applicationContext)
this method should be implemented to perform specific business when
 application is un-installed.

```java
void onUninstallApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

#### ⚙ onUpdateApplication(applicationContext)
this method should be implemented to perform specific business when
 application is updated.

```java
void onUpdateApplication(NutsApplicationContext applicationContext)
```
- **NutsApplicationContext applicationContext** : context

## ☕ NutsApplicationMode
```java
public final net.vpc.app.nuts.NutsApplicationMode
```
 Modes Application can run with

 \@since 0.5.5
 \@category Application

### 📢❄ Constant Fields
#### 📢❄ AUTO_COMPLETE
```java
public static final NutsApplicationMode AUTO_COMPLETE
```
#### 📢❄ INSTALL
```java
public static final NutsApplicationMode INSTALL
```
#### 📢❄ RUN
```java
public static final NutsApplicationMode RUN
```
#### 📢❄ UNINSTALL
```java
public static final NutsApplicationMode UNINSTALL
```
#### 📢❄ UPDATE
```java
public static final NutsApplicationMode UPDATE
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsApplicationMode valueOf(String name)
```
**return**:NutsApplicationMode
- **String name** : 

#### 📢⚙ values()


```java
NutsApplicationMode[] values()
```
**return**:NutsApplicationMode[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsApplications
```java
public final net.vpc.app.nuts.NutsApplications
```
 Helper class for Nuts Applications

 \@author vpc
 \@since 0.5.5
 \@category Application

### 📢🎛 Static Properties
#### 📄📢🎛 sharedMap
a thread local map used to share information between workspace
 and embedded applications.
```java
[read-only] Map public static sharedMap
public static Map getSharedMap()
```
### 📢⚙ Static Methods
#### 📢⚙ processThrowable(ex, args, out)
process throwables and return exit code

```java
int processThrowable(Throwable ex, String[] args, PrintStream out)
```
**return**:int
- **Throwable ex** : exception
- **String[] args** : application arguments to check from if a '--verbose' or
 '--debug' option is armed
- **PrintStream out** : out stream

#### 📢⚙ runApplication(args, session, appClass, lifeCycle)
run application with given life cycle.

```java
void runApplication(String[] args, NutsSession session, Class appClass, NutsApplicationLifeCycle lifeCycle)
```
- **String[] args** : application arguments
- **NutsSession session** : session
- **Class appClass** : application class
- **NutsApplicationLifeCycle lifeCycle** : application life cycle

