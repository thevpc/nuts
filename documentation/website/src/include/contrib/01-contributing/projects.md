---
id: projects
title: Repository Structure
sidebar_label: Nuts Projects
---


## Quick Lookup on sources organization
The repository is organized in several folders described here after:

* **[.dir-template]** : contains template files for generating `README.md` and `METADATA` (among other) files according to the current `nuts` development version
* **[core]**          : contains the core of `nuts` package manager (and the only required pieces for `nuts` to work). Practically this contains the Bootstrap (and API) project (called `nuts`) and the Runtime (Implementation) project (called `nuts-runtime`)
* **[docs]**          : contains a generated (using docusaurus) website that is deployed to github pages (https://thevpc.github.io/nuts/)
* **[extensions]**           : contains some `nuts` extensions/plugins. as an example it includes and extension for nuts terminal features implemented using `jline` library
* **[incubating]**    : ignore this for the moment :), It's a work in progress and an attempt to simplify `nuts` installation process and other frozen features. Still very embryonic.
* **[installers]**    : native image generators, gui installers and repository website generator goes here. 
* **[libraries]**           : contains a suite of libraries that are based on `nuts` and that can be used by other applications. This includes markdown parsers, ssh support, etc...
* **[test]**          : contains unit test projects
* **[companions]**    : contains a suite of applications that are tightly coupled with `nuts`. This includes `nsh` the shell companion
* **[third-party-companions]**    : contains a suite of third party wrapper applications. This includes `ndocusaurus` documentation generator
* **[toolbox]**       : contains a suite of applications that are based on `nuts` and that complement `nuts` features.
* **[web-toolbox]**   : contains a suite of web applications that are based on `nuts` and that complement `nuts` features. This includes `nwar`, a servlet implementation to serve `nuts` workspaces.
* **[documentation]** : contains the sources of `nuts`'s docusaurus based website and documentation files
    * **[website/.dir-template]**       : contains the effective sources of `nuts`'s documentation (used to create the website as well). To be more precise, the website is built using a two steps process: first we pre-process the markdown files with template processing (using `ndoc`) that will handle things like `nuts` version variables, documentation structure etc and hence prepare the `docusaurus` base folder. And then, we run `docusaurus` to generate the effective `html`/`js` for the statically compiled website.


## nuts projects

**```nuts```** repository is composed of several projects that can be organized in 5 categories

* **Core ```nuts```** : These projects are the core/base of the **```nuts```** package manager
* **Companion Tools** : These projects are applications and tools to install with **```nuts```** itself. Their installation are prompted at first install of **nuts**
* **Toolbox** : These projects are applications and tools built on top of **```nuts```** Application Framework and are of common interest
* **Lib** : These projects are common libraries that can be used to enabled some **```nuts```** features in your application
* **Extension** : These projects are add features to the nuts platform. on example is the ability to add JLine library support to have smarter terminals.
* **Other** : All other apps that doe no fit in the previous categories

## Core Nuts projects
Core **```nuts```** projects include **nuts-builder**, **nuts-api** (/core/nuts), **nuts-runtime** (/core/nuts-runtime).

### **nuts-builder**
**nuts-builder** is a meta project (parent maven pom project) that helps building all the other projects.

### **nuts-api**
**nuts-api** is the effective "nuts" only required dependency. 
It defines the bootstrap application that is responsible of loading all necessary libraries for its execution. 
**nuts-api** starts to load **nuts-runtime** which is responsible of implementing all features and interfaces declared by the **nuts-api** library. 
That implementation will handle further download, version dependency etc. Such architecture is considered to force loose coupling with nuts binaries.
**nuts-api** is a very thin boostrapper : its size is about 300k. It can be used as a standalone application or as an embedded library.

### **nuts-runtime**
**nuts-runtime** is the effective and standard implementation of **nuts-api**. 
**nuts-runtime** has a faster update pace than **nuts-api**. 
It focuses on performance an compliance to the **nuts** specifications declared by **nuts-api** interfaces. 
You are not required to add this dependency to your application if you want to embed **nuts**. 
The library will be loaded on the wire (if not yet present in the classpath of course).
**nuts-runtime** is designed to have very few dependencies : **mslinks** and **jansi**. Both are supports/used only on windows platform. They wont be used on linux or MacOS 

* **mslinks** trivially is used to support windows shortcut menus
* **jansi** is used to support terminal coloring and the "Nuts Text Format" (NTF), a simple text format (markdown like) that helps creating colorful terminal applications.

## Companion tools projects
Companion tools include mainly **nsh** 
This application is implemented following the "**```nuts```** Application Framework" and hence is dependent on **nuts-api** library.
**nsh** is a recommended for installation because it adds portable bash like features to the tool, however is is mandatory and may be ignored particularly when using **nuts-api** as library.

### **nsh**
**nsh** (for **```nuts```** shell) is simply a portable POSIX bash compatible implementation. 
It supports all common builtin commands (ls, cd, rm, ...) and adds support to grep, ssh and scp in a seamless manner. 
It also supports command line, scripts (including commons constructs with if, do, case, ...), pipes (|) and common bash syntax.


## Toolbox projects
**```nuts```** comes with an array of tools out of the box you can install and play with. Here are some of them:

### **nversion**
**nversion** is a small tool that helps detecting files versions. 
It supports jar, war, ear, dll and exe file versions. It opens a file and looks for it's version in its meta-data.

### **ndb**
**ndb** is a companion tool to the relational databased. **mysql**, **mariadb** and **nderby** servers are supported. 
The main actions supported are backup and restore including push/pull mechanism from/to a couple of databases for synchronization. 
It supports jdbc and ssh based access to remote mysql/mariadb installation.

### **ntomcat**
**ntomcat** is a companion tool to the tomcat http server. 
The main actions supported are start, stop, status, configure (http ports etc..) and deploy. 
It supports as well installation of several versions of Tomcat and multi domain configuration for deployments.

### **nmvn**
**nmvn** is a companion tool to maven. 
It supports installations of several versions of maven and running them seamlessly.

### **noapi**
**noapi** (for Nuts OpenApi) is an OpenAPI documentation generator.

### **ncode**
**ncode** is a small code search tool. It searches for files, file contents and classes within jars.
You can search for files than contains some text or jars that contain some class, or jars of a specific version of java.

### **nwork**
**nwork** is a developer centered tool. **nwork** is the tool we - maven users - need to check if the version of project we are working on is yet to be deployed to nexus or not. So basically it checks if the version is the same, and downloads the server's version and then compares binaries to local project's to check if we have missed to update the version in our pom.xml. I know I'm not the only one having pain with jar deployments to nexus. **nwork** does other things as well to help me on on daily basis.

### **ndoc**
**ndoc** is a file templating tool that replaces place-holders in the files with an evaluated expression. 

### **njob**
**njob** is a powerful terminal todo list

### **ndocusaurus**
**ndocusaurus** is a [Docusaurus 2](https://docusaurus.io) toolbox that adds several features to the tool such as: 
* templating (using ndoc)
* pdf generation

### **ntalk-agent**
**ntalk-agent** is a client-to-client communication broker used for sharing **nuts** workspaces

### **nclown**
**nclown** is an angular web application frontend for **```nuts```**. It helps navigating, searching and installing artifacts. It's intended to be a web admin tool as well.

### **nserver**
**nserver** is a standalone application that runs a small http server that will expose a workspace as a remote repository to other **nuts** installations. This is the simplest way to mirror a workspace and share artifacts between networked nodes.

### **nwar**
**nwar** (for **```nuts```** Web Application Archive) is a web application that exposes **nserver** as a war to be deployed on a more mature http server or container.

### **ndexer**
**ndexer** (for Indexer) is a lucene powered index for **```nuts```**. It can be shared across multiple **```nuts```** workspaces and processes.

## Library Projects
Library projects are several libraries that add **```nuts```** support in a particular environment or domain.
### **nlib-tomcat-classloader**
This is a must-have feature in your web application if deployed on Tomcat. It solves the following problem : a simple war application is surprisingly fat with too many jars (hundreds of Megas) you need to upload each time you change a single file or class in your web project. Basically all the jars included in the lib folder of the war are to be uploaded each time to the remote Tomcat server. The common solution is to use "provided" scope in maven and put your jars in Tomcat lib or ext folders. This is a bad approach if you are using a single Tomcat process for multiple applications. **nuts-tomcat-classloader** simply uses **nuts** to download libraries when the application is deployed based on the **pom.xml** you provide and include them in the current web application class loader. Hence, the war becomes lighter than ever. **nuts** cache mechanisms optimizes bandwidth and makes this more convenient by sharing the same jar files between applications depending on the same versions. 
All you have to do is to add this library to your application and configure your **pom.xml** accordingly.


### **nlib-servlet**
Basically this is the simplest way to include **nserver** into your web application.

### **ndoc-lib**
This library provides helper methods to manipulate maven pom.xml and generate simple Java files while supporting **```nuts```** concepts. It's used in other tools that are meant to generate maven projects.

### **nlib-talkagent**
This library provides support for client to client communication

## Extensions
Extensions provide extra feature to nuts.

### **next-term**
This library provides rich terminal support (auto-complete, history) based on the JLine library

## Other Projects
Other projects you may encounter in the repository are WIP projects that may be continued or discontinued.
This includes : **nutsc** (a native c bootstrapper) and **nuts-installer** (a **nuts** installer tool)

## Honorable mentions
Although not included in this Git repository some other tools are based on **```nuts```** and hence are installable using ```nuts install the-app``` command. Those tools are published in other repositories.

### **netbeans-launcher** 
this tool supports installation and launch of multiple netbeans instances in parallel. See [Netbeans Launcher GitHub Repository](https://github.com/thevpc/netbeans-launcher)

### **pnote**
this tool is a multi purpose, developer oriented, Note taking application. See [Pangaea Note](https://github.com/thevpc/pangaea-note)

### **upa-box** (deprecated)
this tool supports creation of UPA aware projects. UPA is a non structured ORM for the Java Language. See [UPA GitHub Repository](https://github.com/thevpc/upa)

### **vr-box** (deprecated)
this tool supports creation of VR aware projects. VR is a web portal framework. See [VR GitHub Repository](https://github.com/thevpc/vr)

