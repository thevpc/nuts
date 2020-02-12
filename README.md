# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.6.0.0
</pre>

**nuts** is a Package manager for Java (and other things). It stands for **Network Updatable Things Services** tool. Think of it as **npm** for javascript or **pip** for python. But it lots more... It is a simple tool  for managing remote artifacts, installing these  artifacts to the current machine and executing such  artifacts on need. Each managed artifact  is also called a **nuts** which  is a **Network Updatable Thing Service** . Nuts artifacts are  stored  into repositories. A  **repository**  may be local for  storing local Nuts or remote for accessing  remote artifacts (good examples  are  remote maven  repositories). 

One manages a set of repositories called a **workspace**. Managed **nuts**  (artifacts)  have descriptors that depict dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to resolve and download on-need dependencies over the wire. 

**nuts** is a swiss army knife tool as it acts like (and supports) **maven** build tool to have an abstract view of the the  artifacts dependency and like  **npm**, **pip** or **zypper/apt-get**  package manager tools to  install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed.

What makes **nuts** very helpful is that it simplifies application deployments by not including dependencies in the release bundle. All dependencies will be downloaded at installation time. They also will be shared among all application which reduces storage space as well. Offline deployment is still supported though. Besides, multiple versions of the same application can be installed simultaneously.

## COMMON VERBS:
+ exec               : execute an artifact or a command
+ install, uninstall : install/uninstall an artifact (using its fetched/deployed installer)
+ update             : update an artifact (using its fetched/deployed installer)
+ deploy, undeploy   : manage artifacts (artifact installers) on the local repositories
+ fetch, push        : download from, upload to remote repositories
+ search             : search for existing/installable artifacts
+ welcome            : a command that does nothing but bootstrapping nuts and showing a welcome message.

## Download Latest stable version
+ Linux,Windows,iOS, and Java enabled OS : [nuts-0.6.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.6.0/nuts-0.6.0.jar)
+ On Unix/Linux platforms you may use :
    + using **wget**
        ```
        wget https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.6.0/nuts-0.6.0.jar
        ```
    + or **curl**
        ```
        curl https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.6.0/nuts-0.6.0.jar > nuts-0.6.0.jar 
        ```
    Note that you will need a valid Java Runtime Environment (JRE) or Java Development Kit (JDK) version **8** or later to run **nuts**.

## Installation
Nuts needs no installation. 
It will create all needed configuration files upon the very first launch. 
More details are available at [wiki installation page](https://github.com/thevpc/nuts/wiki/Installation)

```
java -jar nuts-0.6.0.jar
```
Yous should then see some log like the following :

![install-log-example](docs/install-log-example.png)

As you can see, installation upon first launch, will also trigger installation of other optional programs called "companion tools".
Actually they are recommended helpful tools :
  + **ndi** which stands for __Nuts Desktop Integration__ that helps configuring the desktop to better 
    interact with nuts by for instance creating shortcuts.
  + **nsh** which stands for __Nuts Shell__ , a bash compatible shell implementation program that will run equally on linux an windows systems.
  + **nadmin** an administration tool for **nuts** 


## Unix-Like Systems (Linux,MacOS,Unix)
Unix-like Systems installation is based on bash shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using nuts on unix-like system should be seamless. A simple bash terminal (MacOs Terminal App, Gnome Terminal, KDE Konsole,...) is already a nuts-aware terminal.

All Linux versions and distributions should work with or without XWindow (or equivalent). Graphical system is required only if you plan to run a gui application using **nuts**.
All tests where performed on OpenSuse Tumbleweed.

## Windows Systems
On Windows systems, first launch will create a new Nuts Menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal.
  + **nuts-cmd-0.6.0** : this shortcut will open a configured command terminal. **nuts** command will be available as well 
                         as several nuts companion tools installed by **ndi** by default
  + **nuts-cmd**       : this shortcut will point to the last installed **nuts** version, here 0.6.0  

Any of these shortcuts will launch a nuts-aware terminal.

Supported Windows systems include Window 7 and later.

## MacOS Systems
Installation will worl seemlely on MacOS as far as you are using bash shell. All Linux installatio notes apply then.

## Test Installation
To test installation the simplest way is to open a nuts-aware terminal and type : 

```
nuts --version
```

It should show a result in the format : nuts-api-version/nuts-impl-version

```
0.6.0/0.6.0.0
```

## Run a command

To run a command using **nuts** just type

```
nuts <command>
```

Several commands are available, and you still be able to run any java and non java application. More info is available in the Nuts official [wiki](https://github.com/thevpc/nuts/wiki) .

# Troubleshooting
Whenever installation or running fails, it is more likely that there is a misconfiguration or invalid libraries that are used. 
See [troubleshooting documentation](docs/troubleshooting.md) for more details


## Getting started
You may consider browsing the Nuts official [wiki](https://github.com/thevpc/nuts/wiki) .

## Releases
View stable releases in [official releases page](https://github.com/thevpc/nuts/releases).

View all releases in [release details page](docs/change-log/release-details.md).
