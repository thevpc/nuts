# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.3.3.3
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote,
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

## Download

[Latest stable version jar](https://github.com/thevpc/vpc-public-maven/blob/c9adda023b6e9b8d4e38210e0b77d620011b9b66/net/vpc/app/nuts/nuts/0.3.3.3/nuts-0.3.3.3.jar)

## Run
### Linux
java -jar nuts-0.3.3.3.jar console
or
nuts console
if you have the appropriate shell file "nuts"
### Windows
java -jar nuts-0.3.3.3.jar console



