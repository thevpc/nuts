# nuts
Network Updatable Things Services
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.3.2


nuts stands for Network Updatable Things Services tool. It is a simple tool  for managing remote,
packages, installing these  packages to the current machine and executing such  packages on need.
Each managed package  is also called a Nuts which  is a Network Updatable Thing Service .
Nuts packages are  stored  into repositories. A  repository  may be local for  storing local Nuts
or remote for accessing  remote packages (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote packages are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed Nuts  (packages)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  nuts  (tool) to
resolve and download on-need dependencies over the wire.

nuts is a swiss army knife tool as it acts like (and supports) maven build tool to have an abstract
view of the the  packages dependency, like  zypper/ap-get  package manager tools  to  install and
uninstall packages allowing multiple versions of the very same package to  be installed, and like
git/svn source version tools to support package (re)-building and deploying.

COMMON VERBS:
deploy,undeploy   : to handle packages (package installers) on the local repositories
install,uninstall : to install/uninstall a package (using its fetched/deployed installer)
checkout,commit   : create new versions of the packages
fetch,push        : download, upload to remote repositories
find              : searches for existing/installable packages
