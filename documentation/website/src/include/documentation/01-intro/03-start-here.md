---
id: start_here
sidebar_position: 1
title: First example
---


## Let's start the journey

Get started by **running your first application**.
Nuts is not a build tool (like `maven` and `gradle`). Nuts is more likely to be compared with nodejs `npm` or python's `pip`
It consists mainly of a commandline application that is used to install, uninstall and run other java applications in a smooth elegant way.

### What you'll need

- `java`  compatible operating system including [Linux],[Windows] and [MacOS]
- [java](https://www.java.com) version 1.8 or above. JRE is sufficient, JDK allows you to do more with `nuts`

Let's check that java is installed :
```
java --version
```

But in the following example, I am assuming you are using a Linux distribution et MacOS. 
If you are using Windows please refer to the [installation](#Installation) section.

### Installing and Running Nuts

We start by opening a new terminal (term, konsole or whatever you prefer) then download **```nuts```** using this command :
On linux/MacOS system we issue :

```
curl -sL https://thevpc.net/nuts/nuts-stable.jar -o nuts.jar && java -jar nuts.jar -Zy
```

You can type this command into Command Prompt, Powershell, Terminal, or any other integrated terminal of your code editor.

The command also installs all necessary dependencies you need to run Docusaurus.


We used the flags ```-y``` to auto-confirm and ```-z``` to ignore cached binaries (combined here as ```-zy```).
These flags are optional and are used here to demonstrate some of Nuts' available options.
The installation process may take several minutes, as it involves downloading all required dependencies, companions, and tools.

You should then see this message

```
Welcome to nuts. Yeah, it is working...
```

**```nuts```** is well installed, just restart your terminal.

Now we will install `jedit`, a tremendous underrated text editor. So in your terminal type:

```bash
nuts install org.jedit:jedit
```

Let's run jedit now
```
nuts jedit
```

As you can see, simple commands are all you need to download, install, configure and run `jedit` or any java application that is deployed in the maven repository.

So please visit ```nuts``` [website](https://thevpc.github.com/nuts) or [github repository](https://github.com/thevpc/nuts) for more information.


