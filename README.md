# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.5.4.0
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
components, installing these  components to the current machine and executing such  components on need.
Each managed package  is also called a **nuts** which  is a **Network Updatable Thing Service** .
Nuts components are  stored  into repositories. A  *repository*  may be local for  storing local Nuts
or remote for accessing  remote components (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote components are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed **nuts**  (components)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to
resolve and download on-need dependencies over the wire.

**nuts** is a swiss army knife tool as it acts like (and supports) *maven* build tool to have an abstract
view of the the  components dependency and like  *npm*, **pip** or *zypper/apt-get*  package manager tools  
to  install and uninstall components allowing multiple versions of the very same component to  be installed.

## COMMON VERBS:
+ deploy,undeploy   : handle components (package installers) on the local repositories
+ install,uninstall : install/uninstall a package (using its fetched/deployed installer)
+ update            : update a package (using its fetched/deployed installer)
+ fetch,push        : download, upload to remote repositories
+ find              : searches for existing/installable components

## Download Latest stable version
+ Java or any Java enabled OS : Linux,Windows,iOS, ... :: [nuts-0.5.4.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.4/nuts-0.5.4.jar)

## Requirements
Java Runtime Environment (JRE) or Java Development Kit (JDK) version 8 or later

## Installation
java -jar nuts-0.5.4.jar

## Launching
+ [Linux] nuts ...your command here...
    + nuts --version
      show version and exit  
    
    + nuts help
        show help

    + nuts install derby
        install derby

    + nuts derby start
        start derby

    + nuts -y netbeans-launcher
        install and run netbeans-launcher

    + nuts update --all
        update nuts and all installed components

+ [Windows,iOS] java -jar nuts-0.5.4.jar ...your command here...

## Latest News

+ 2019/04/21 	nuts 0.5.4.0 released [nuts-0.5.4.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.4/nuts-0.5.4.jar) [change log](https://github.com/thevpc/nuts/blob/master/docs/change-log/v0.5.4.md)
+ 2019/01/05 	nuts 0.5.3.0 released [nuts-0.5.3.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.3/nuts-0.5.3.jar) [change log](https://github.com/thevpc/nuts/blob/master/docs/change-log/v0.5.4.md)
+ 2018/12/28 	nuts 0.5.2.0 released [nuts-0.5.2.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.2/nuts-0.5.2.jar) [change log](https://github.com/thevpc/nuts/blob/master/docs/change-log/v0.5.1.md)
+ 2018/12/18 	nuts 0.5.1.0 released [nuts-0.5.1.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.1/nuts-0.5.1.jar) [change log](https://github.com/thevpc/nuts/blob/master/docs/change-log/v0.5.1.md)
+ 2018/11/25 	nuts 0.5.0.0 released [nuts-0.5.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.5.0/nuts-0.5.0.jar) [change log](https://github.com/thevpc/nuts/blob/master/docs/change-log/v0.5.0.md)

## Getting started

 You may consider browsing the Nuts official [wiki](https://github.com/thevpc/nuts/wiki) .
