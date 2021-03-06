[---
id: projects
title: Repository Structure
sidebar_label: Nuts Projects
---

${include($"${resources}/header.md")}

# Nuts projects
**```nuts```** repository is composed of several projects that can be organized in 5 categories

* **Core ```nuts```** : These projects are the core/base of the **```nuts```** package manager
* **Companion Tools** : These projects are applications and tools to install with **```nuts```** itself. Their installation are prompted at first install of **nuts**
* **Toolbox** : These projects are applications and tools built on top of **```nuts```** Application Framework and are of common interest
* **Lib** : These projects are common libraries that can be used to enabled some **```nuts```** features in your application
* **Extension** : These projects are add features to the nuts platform. on example is the ability to add JLine library support to have smarter terminals.
* **Other** : All other apps that doe no fit in the previous categories

## 1- Core Nuts projects
Core **```nuts```** projects include **nuts-builder**, **nuts-api** (/core/nuts), **nuts-runtime** (/core/nuts-runtime).

### 1.1 **nuts-builder**
**nuts-builder** is a meta project (parent maven pom project) that helps building all the other projects.

### 1.2 **nuts-api**
**nuts-api** is the effective "nuts" only required dependency. 
It defines the bootstrap application that is responsible of loading all necessary libraries for its execution. 
**nuts-api** starts to load **nuts-runtime** which is responsible of implementing all features and interfaces declared by the **nuts-api** library. 
That implementation will handle further download, version dependency etc. Such architecture is considered to force loose coupling with nuts binaries.
**nuts-api** is a very thin boostrapper : its size is about 300k. It can be used as a standalone application or as an embedded library.

### 1.3 **nuts-runtime**
**nuts-runtime** is the effective and standard implementation of **nuts-api**. 
**nuts-runtime** has a faster update pace than **nuts-api**. 
It focuses on performance an compliance to the **nuts** specifications declared by **nuts-api** interfaces. 
You are not required to add this dependency to your application if you want to embed **nuts**. 
The library will be loaded on the wire (if not yet present in the classpath of course).
**nuts-runtime** is designed to have very few dependencies : **gson** and **jansi**.

* **gson** trivially is used to support json serialization : the main format used in **nuts** to support configuration and descriptors. 
* **jansi** is used to support terminal coloring and the "Nuts Text Format" (NTF), a simple text format (markdown like) that helps creating colorful terminal applications.

## 2. Companion tools projects
Companion tools include **nadmin** and **nsh** 
These two applications are implemented following the "**```nuts```** Application Framework" and hence are dependent on **nuts-api** library. 
They are recommended applications to install with **nuts** itself, however they are not mandatory and may be ignored particularly when using **nuts-api** as library.

### 2.1 **nadmin**
**nadmin** (for **```nuts```** admin) is an administration tool to manage the **```nuts```** workspaces. 
It adds support for managing users, credentials, authorizations, workspaces and repositories by providing command line support for such actions.
**nadmin** adds also seamless integration of **```nuts```** commands in your favorite operating system and environment by creating scripts that point to your favorite workspaces and applications. 
**nadmin** is responsible of creating script shortcuts to your common commands so that you can invoke tem directly from your environment. 
For instance it creates an "nadmin" script and configures your PATH environment to help calling the nuts admin tool instead of the common way to do so "nuts nadmin". 
On window system, **nadmin** will create shortcuts and menus.


### 2.2 **nsh**
**nsh** (for **```nuts```** shell) is simply a portable POSIX bash compatible implementation. 
It supports all common builtin commands (ls, cd, rm, ...) and adds support to grep, ssh and scp in a seamless manner. 
It also supports command line, scripts (including commons constructs with if, do, case, ...) and pipes (|) and common bash syntax.


## 3. Toolbox projects
**```nuts```** comes with an array of tools out of the box you can install and play with. Here are some of them:


### 3.1 **nversion**
**nversion** is a small tool that helps detecting files versions. 
It supports jar, war, ear, dll and exe file versions. It opens a file and looks for it's version in its meta-data.

### 3.2 **nmysql**
**nmysql** is a companion tool to the mysql and mariadb servers. 
The initial actions supported are backup and restore including push/pull mechanism from/to a couple of databases for synchronization. 
It supports jdbc and ssh based access to remote mysql/mariadb installation.

### 3.3 **ntomcat**
**ntomcat** is a companion tool to the tomcat http server. 
The initial actions supported are start, stop, status, configure (http ports etc..) and deploy. 
It supports as well installation of several versions of Tomcat and multi domain configuration for deployments.

### 3.4 **nderby**
**nderby** is a companion tool to the derby database server. 
The initial actions supported are start, stop, status and configure. 
It supports as well installation of several versions of Derby.

### 3.5 **nmvn**
**nmvn** is a companion tool to maven. 
It supports installations of several versions of it and running them seamlessly.

### 3.6 **noapi**
**noapi** (for Nuts OpenApi) is an OpenAPI documentation generator.

### 3.7 **ncode**
**ncode** is a small code search tool. It searches for files, files contents and classes within jars.
You ca search for files than contains some text or jars that contain some class, or jars of a specific version of java.

### 3.8 **nwork**
**worky** is a developer centered tool. The 'y' in **worky** refers to 'my' in the "Tunisian Dialect" and hence means "my work". **Worky** is the tool we - maven users - need to check if the version of project we are working on is yet to be deployed to nexus or not. So basically it checks if the version is the same, and downloads the server's version and then compares binaries to local project's to check if we have missed to update the version in our pom.xml. I know I'm not the only one having pain with jar deployments to nexus. **Worky** does other things as well to help me on on daily basis.

### 3.9 **ntemplate**
**ntemplate** is a file templating tool that replaces place-holders in the files with an evaluated expression. 

### 3.10 **njob**
**njob** is powerful terminal task manager

### 3.11 **ndoc**
**ndoc** is a javadoc generator. It supports standard format and adds markdown format.

### 3.12 **ndocusaurus**
**ndocusaurus** is a [Docusaurus 2](https://docusaurus.io) toolbox that adds several features to the initial tool: 
* adds templating (using ntemplate)
* adds pdf generation

### 3.13 **ntalk-agent**
**ntalk-agent** is a client-to-client communication broker

### 3.14 **nclown**
**nclown** is an angular web application frontend for **```nuts```**. It helps navigating, searching and installing artifacts. It is intended to be a web admin tool as well.

### 3.15 **nserver**
**nserver** is a standalone application that runs a small http server that will expose a workspace as a remote repository to other **nuts** installations. This is the simplest way to mirror a workspace and share artifacts between networked nodes.

### 3.16 **nwar**
**nwar** (for **```nuts```** Web Application Archive) is a web application that exposes **nserver** as a war to be deployed on a more mature http server or container.

### 3.17 **ndexer**
**ndexer** (for Indexer) is a lucene powered index for **```nuts```**. It can be shared across multiple **```nuts```** workspaces and processes.

## 4. Library Projects
Library projects are several libraries that add **```nuts```** support in a particular environment or domain.
### 4.1 **nlib-tomcat-classloader**
This is a must-have feature in your web application if deployed on Tomcat. It solves the following problem : a simple war application is surprisingly fat with too many jars (hundreds of Megas) you need to upload each time you change a single file or class in your web project. Basically all the jars included in the lib folder of the war are to be uploaded each time to the remote Tomcat server. The common solution is to use "provided" scope in maven and put your jars in Tomcat lib or ext folders. This is a bad approach if you are using a single Tomcat process for multiple applications. **nuts-tomcat-classloader** simply uses **nuts** to download libraries when the application is deployed based on the **pom.xml** you provide and include them in the current web application class loader. Hence, the war becomes lighter than ever. **nuts** cache mechanisms optimizes bandwidth and makes this more convenient by sharing the same jar files between applications depending on the same versions. 
All you have to do is to add this library to your application and configure your **pom.xml** accordingly.


### 4.2 **nlib-servlet**
Basically this is the simplest way to include **nserver** into your web application.

### 4.3 **nlib-template**
This library provides helper methods to manipulate maven pom.xml and generate simple Java files while supporting **```nuts```** concepts. It is used in other tools that are meant to generate maven projects.

### 4.3 **nlib-talkagent**
This library provides support for client to client communication

## 5. Extensions
Extensions provide extra feature to nuts.

### 5.1 **next-term**
This library provides rich terminal support (auto-complete, history) based on the JLine library

## 5. Other Projects
Other projects you may encounter in the repository are WIP projects that may be continued or discontinued.
This includes : **nutsc** (a native c bootstrapper) and **nuts-installer** (a **nuts** installer tool)

## 6. Honorable mentions
Although not included in this Git repository some other tools are based on **```nuts```** and hence are installable using ```nuts install the-app``` command. Those tools are published in other repositories.

6.1 **netbeans-launcher** : this tool supports installation and launch of multiple netbeans instances in parallel. See [Netbeans Launcher GitHub Repository](https://github.com/thevpc/netbeans-launcher)

6.2 **upa-box** : this tool supports creation of UPA aware projects. UPA is a non structured ORM for the Java Language. See [Netbeans Launcher GitHub Repository](https://github.com/thevpc/upa)

6.3 **vr-box** : this tool supports creation of VR aware projects. VR is a web portal framework. See [Netbeans Launcher GitHub Repository](https://github.com/thevpc/vr)

