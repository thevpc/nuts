# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.3.5.0
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
packages, installing these  packages to the current machine and executing such  packages on need.
Each managed package  is also called a **nuts** which  is a **Network Updatable Thing Service** .
Nuts packages are  stored  into repositories. A  *repository*  may be local for  storing local Nuts
or remote for accessing  remote packages (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote packages are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed **nuts**  (packages)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to
resolve and download on-need dependencies over the wire.

**nuts** is a swiss army knife tool as it acts like (and supports) *maven* build tool to have an abstract
view of the the  packages dependency, like  *zypper/apt-get*  package manager tools  to  install and
uninstall packages allowing multiple versions of the very same package to  be installed, and like
*git/svn* source version tools to support package (re)-building and deploying.

## COMMON VERBS:
+ deploy,undeploy   : to handle packages (package installers) on the local repositories
+ install,uninstall : to install/uninstall a package (using its fetched/deployed installer)
+ checkout,commit   : create new versions of the packages
+ fetch,push        : download, upload to remote repositories
+ find              : searches for existing/installable packages

## Download Latest stable version

+ Linux   :: [nuts linux bash script](https://github.com/thevpc/nuts/raw/master/nuts/deploy/nuts)
+ Windows :: [nuts.jar](https://github.com/thevpc/nuts/raw/master/nuts/deploy/nuts.jar)
+ iOS     :: [nuts.jar](https://github.com/thevpc/nuts/raw/master/nuts/deploy/nuts.jar)
+ Java    :: [nuts.jar](https://github.com/thevpc/nuts/raw/master/nuts/deploy/nuts.jar)

## Run
### Linux
```bash
nuts console
```
or
```bash
java -jar nuts.jar console
```
if you have the appropriate shell file "nuts", just put it under $HOME/bin or /local/bin
### Any other platform (Windows, iOS, ...)
```bash
java -jar nuts.jar console
```

### Running a local jar with external dependencies
Let's suppose that my-app.jar is maven created jar (contains META-INF/maven files) with a number of dependencies. Nuts 
is able to download on the fly needed dependencies, detect the Main class (no need for MANIFEST.MF) and run the 
application. If main classes have been detected with main method, nuts will ask for the current class to run.

#### Running installed nuts
Before running an application you have to install it. Il will be downloaded and all its dependencies. Then you can call the exec command.

```bash
java -jar nuts.jar install my-app
java -jar nuts.jar exec my-app some-argument-of-my-app
```

Alternatively, the 'exec' command can be omitted

```bash
java -jar nuts.jar my-app some-argument-of-my-app
```

#### Running local file
You also may run a local file, nuts will behave as if the app is installed (in the given path). You just have to run it 
directly. Dependencies will be downloaded as well (and cached in the workspace ~/.nuts/default-workspace)

```bash
java -jar nuts.jar my-app.jar some-argument-of-my-app
```

#### Passing VM arguments
If you need to pass JVM arguments you have to prefix them with "--nuts" so if you want to fix maximum heap size use 
--nuts-Xmx2G instead of -Xmx2G

```bash
java -jar nuts.jar my-app.jar  --nutsXms1G --nuts-Xmx2G --nuts-Dother-vm-arg=3 some-argument-of-my-app some-app-argument
```




