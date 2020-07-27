# You'd still be Maven, yet you gonna be Nuts
Is there any package manager for Java(TM) applications? You can google for it and you will find that many have 
queried this on blogs and forums. In most cases responses point to maven and gradle, the tremendous build tools. However, 
both maven and gradle are build tools, while helping build packages they lack of deployment features. They bundle every 
dependency in every package (think of wars, ears and standalone jars). They do not handles installation or upgrading.
Apache ivy, as well, while competing with maven build tool does not provide more than transitive dependency management.   
The main idea behind a package manager is the automation of installation, update, configuration and removal of programs 
or libraries in a coherent manner with the help of a database that manages binaries and metadata. maven, to consider one, 
sticks to the build process, an goes no further.

You may also ask, "Why ever, do we need a package manager for Java(TM) applications". Okkay, let's take some 
example of Java(TM) applications. How can we install apache netbeans IDE ? The proper way is to browse to the editor's 
website, select the proper mirror if applicable, download the archive, uncompress it, chmod the main binary (i'm a linux 
guy) and adjust PATH environment variable to point to this binary; Quite a pain. What do we do to update it now? Hopefully, 
the IDE has a solid plugin architecture and an in-app update/upgrade tool that will help the process (in a gui manner of 
course). The same applies to eclipse and apache tomcat with the exception that apache tomcat does not even bundle an in-app 
update tool. The same applies too when dealing with other operating systems (Windows, MacOS, ...). Managing Java(TM) 
applications is far from helpful.

Furthermore, as Java(TM) applications are (usually) not bundled in OS-aware installers, you will end up with a spaguetty 
home directory with applications installed all over your partitions, which - simply - does not mix up with all the work 
OS-developers have done to separate logs from data, from temporary files, from binaries, etc. Each application will handle 
it's files in a very specific manner that would make it hard to manage own's disk (automatic archive/backup/restore) or roaming
applications across machines, etc.

Moreover, in a world of containers and devops, deployments of Java(TM) applications need to be automatable and reproducible 
with the highest level of simplicity, configurability and integrability. Installing tomcat on a custom port should ne not 
be as painful as using a custom Docker image or a complicated Dockerfile or even a custom apache tomcat bundle. 

When we recall that Java(TM) is the one language that has the more versatile number of libraries, frameworks and tools, 
I find it annoying not to have a decent package manager to make the leap and provide facilities I find prime in other 
languages and platforms (pip/python, npm/nodejs/javascript) and most of linux distribution (zypper/opsensuse, dnf/redhat 
apt-get/debian/ubuntu)

Hence I'm introducing here a humble attempt to provide a tiny (300ko) yet powerful package manager for Java(TM) 
applications (but not only) that should handle jar files seamlessly (with little or no modification) and that comes with 
a set of portable tools that makes this management at a higher level. I'm not talking about redefining the wheel. 
I'm aware that many tools such as maven, are already very good at what they do, I just needed to make the leap for deployments. 
You will be able to deploy your applications without bundling all of their dependencies : **Nuts** will take care of that. 

So you'd still be maven, yet you gonna be **Nuts**.
  
## Nuts Package manager
**Nuts** is actually :
+ a transitive dependency resolution manager
+ package manager (backports maven and supports maven repositories)
+ automation tool
+ feature rich toolset
+ application framework
+ decentralized
+ sandbox based

### Transitive dependency resolution manager
**Nuts** calculates transitive dependencies of an application to resolve other packages to download at install or 
update/upgrade time. So typically, deployed applications should no more bundle their dependencies within the deployed archive.
Thus we avoid the annoying fat jars (using maven plugins like 'maven-assembly-plugin' and 'maven-shade-plugin') and lib folders 
(using 'maven-dependency-plugin'). It will also reuse dependencies and packages across multiple installed applications 
and hence save disk space, and network bandwidth.

All what **Nuts** needs is a descriptor file withing the jar file that defines the immediate dependencies. It then 
calculates all transitive dependencies automatically. And guess what, all maven built jars already contain that 
descriptor : the pom.xml file. So basically all maven applications are already **Nuts** aware applications.

### Package manager
**Nuts** uses this dependency resolution to help install, update, remove and search for applications. To be able to use an 
application, it has to be installed and configured with all of its dependencies. This is the main goal of Nuts.
When we ask to install tomcat, for instance, it will search for the best version in registered repositories, download it,
and configure it to be ready for execution. The best version is not always the the latest one. Actually it would be the 
latest valid one, thus the latest one that matches some constraints.  
Constraints include the version of the running java (tomcat 8 works on java 7 but not 6 for instance), the running operating 
system (windows, linux, ... to help selecting the proper binaries), may be the hardware architecture or even the 
operating distribution (for linux based systems). Constraints will filter the search result to include the best, the most 
accurate version to install. Installation also would configure the installed application and even may run another 
artifact to help this configuration.

**Nuts** also handles search for newer versions and update the installed application at request. Updating a software does not 
necessarily delete the older version. Both version can coexist and it is up to the user the decide whether or 
not to retain both versions. Indeed, one of the key features of **Nuts** is the ability to install and hence run multiple versions 
of the same software in parallel. You would never see an error message telling you can't install that software because a 
dependencies of it is installed with different version. All software and all libraries can coexist peacefully.

Software artifacts are stored in repositories. **Nuts** can handle multiple repositories, remote and local ones. 
Installed software are store in special local repositories. Supported repositories include maven repositories and github 
repositories. Actually a fresh installation of **Nuts** is configured with maven central repository so that, you already have access 
to thousands of installable artifacts.

At some point, you may need to uninstall an artifact and that's to undo the artifact installation.  
Installation will help you choose between uninstalling binaries only and keeping data/config files or remove permanently 
all of the artifact files. In all ways, uninstalling will not affect other artifacts that use the same dependencies if ever. 
 
### Feature rich Toolset
**Nuts** is intended to be used either by human users or by robots and other applications. It comes with portable,
feature rich toolset, a versatile library and an a handy parsable result. 

**Nuts** is mainly a commandline program that helps installing, uninstalling, searching, updating and running artifacts. 
To help desktop integration, **Nuts** installs by default a set of other companion tools such as nsh (a portable 
bash-compatible implementation), nadmin (an administration tool for **Nuts** to configure users, authorizations, repositories, 
...) and ndi (desktop integration) to help creating application shortcuts and scripts;

**nsh** brings the bash  facilities to all environments (windows included) in a very portable manner. Besides it integrates
well with the installed **Nuts** version. Several common commands are ported to nsh such as cat,head, and ssh, as well core
features like pipes, redirection and scripts. 

**nadmin** is intended for configuring **Nuts** workspaces, managing repositories and users. It helps also configuring 
sub commands and aliases make **Nuts** usage even easier.

**ndi**, is the tool for a seamless integration in you operating system. Il mainly configures user PATH environment and 
creates scripts that point to your **Nuts** installation on linux desktops and shortcuts to a well configured environment 
on windows.

## Nuts Workspaces
One of the key features of **Nuts** is the ability to support multiple isolated workspaces, each managing it's own 
repositories, applications and libraries; each defining it's sandbox security constraints.
Thus non-root installation is made easy while it remains possible to overlap between workspaces by sharing repositories.
Roaming is also supported, so that a workspaces can be copied/moved across machines.

## Application Framework
**Nuts** can also be embedded as a library in you application. This enables you to wire classes on the fly by its network 
dependency-aware classloading mechanisms. The library allows as well building solid and well integrated applications, 
mainly console applications. Indeed, it comes with rich outputs that support automatic formatting to json, xml, table, 
tree and plain texts. It handles as well standard File Systems layouts; XDG Base Directory Specification is implemented 
for linux and MacOS. A compatible one is also implemented in Windows systems. And of course, it helps seamlessly install,
update and remove events. 

## Nuts ? Really ?
In every palace you will find the wizard and the fool, the **Maven** and the **Nuts**; There's no 
exception in the java kingdom! If you do prefer acronyms here is another reason : **Nuts** stands for Network 
Updatable Things Services. It should be able to facilitate things deployment and update over the 
wire where things resolve here to any piece of software depending (or not) on other piece of software.

## Let's start the journey
we start by opening anew terminal (termm, konsole or whatever you prefer) then download **Nuts** using this command : 
On linux/MacOS system we issue :
```
wget https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.0/nuts-0.7.0.jar
```

Let's check that java is installed :
```
java --version
```

Now we run **Nuts**
```
java -jar nuts-0.7.0.jar -y -z
```
We used the flags -y to auto-confirm and -z to ignore cached binaries. These flags are not required. We use them here to make installation work in all cases.
Installation may last several minutes as it will download all required dependencies, companions and tools.

You should then see this message

```
Welcome to nuts. Yeah, it is working...
```

Nuts is well installed, just restart your terminal.

Now we will install apache tomcat. So in your terminal issue :

```
nuts install ntomcat
nuts ntomcat start --set-port 9090
```
The first two commands will install tomcat helper tool (ntomcat) that will download latest version of tomcat and configure it to 9090 port.
The last command will start tomcat.
Let's check tomcat status now
```
nuts tomcat status
```

Now we will do the same with derby database. We will install it and run it.
```
nuts install nderby
nuts nderby start
```

As you can see, simple commands are all you need to download, install, configure and run tomcat or derby or any application that is deployed in the maven repository.

So please visit [**Nuts**](https://github.com/thevpc/nuts) website for more information.