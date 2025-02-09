---
id: installation-preview
title: Preview Env
---


Installation of `nuts` for preview/test/evaluation or simply for personal use is based on a rolling `nuts` binaries version that is released a faster pace than the production version (generally a semi-monthly schedule). Versions are not rock solid but still they are "very" usable and more importantly they include all latest features. Preview releases are deployed to a development repository and hence are made accessible using a repository swith option.

## System Requirements

Here are all **```nuts```** requirements :

- **Java** : **```nuts```** requires a valid Java Runtime Environment (JRE) or Java Development Kit (JDK) version **8** or above to execute. Please note that you need to update your 1.8 version to the latest update (update 150+)
- **System Memory**: **```nuts```** memory footprint is very little and has no minimum RAM requirements.
- **Disk**: 5Mo on the disk are required for the **```nuts```** installation itself. In addition to that, additional disk space will be used for your local Nuts workspace. The size of your local workspace will vary depending on usage but expect at least 500MB.
- **Operating System**: **```nuts```** is able to run on any java enabled Operating System including all recent versions of Windows, Linux and MacOS.

To check if you have a valid java installation type

```bash
java -version
```

The result would be equivalent to the following. Just be sure the version is 1.8 or over. In this example, 
the java version is 1.8.0_211

```bash
$ java -version
java version "1.8.0_211"
Java(TM) SE Runtime Environment (build 1.8.0_211-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)
```


## Installation


<Tabs
  defaultValue="linux"
  values={[
    { label: 'Linux', value: 'linux', },
    { label: 'MacOS', value: 'macos', },
    { label: 'Windows', value: 'windows', },
    { label: '*NIX wget', value: 'wget', },
    { label: '*NIX curl', value: 'curl', }
  ]
}>
<TabItem value="windows">

download [nuts-preview.jar](https://thevpc.net/nuts/nuts-preview.jar)
```
java -jar nuts-preview.jar -Zy
```

On Windows systems, first launch will create a new **```nuts```** Menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal.
- **nuts-cmd-0.8.5** : this shortcut will open a configured command terminal. **```nuts```** command will be available as well as several nuts companion tools installed by **nadmin** by default
- **nuts-cmd**       : this shortcut will point to the last installed **nuts** version, here 0.8.5  

Any of these shortcuts will launch a nuts-aware terminal.

Supported Windows systems include Window 7 and later.

:::tip

Any of the created shortcuts for windows is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="linux">

__for testing or development (using wget):__
This will reset/delete any previous nuts installation before installing the latest version.
Removing the `Z` modifier (replace `-Zy` by `-y`) flag if you do not want to reset the workspace.

```
$ wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
$ java -jar nuts.jar -r=+preview -Zy
$ exit
```

Linux Systems installation is based on bash shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using **```nuts```** on unix-like system should be seamless. A simple bash terminal (Gnome Terminal, KDE Konsole,...) is already a nuts-aware terminal.

All Linux versions and distributions should work with or without X Window (or equivalent). Graphical system is required only if you plan to run a gui application using **nuts**.
All tests where performed on OpenSuse Tumbleweed.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="macos">

```
$ curl -sL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar
$ java -jar nuts.jar -Zy
$ exit
```

MacOS Systems installation is based on **zsh** shell. First launch will configure "~/.zshrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using **```nuts```** on MacOS system should be seamless. A simple bash terminal (MacOs Terminal App) is already a nuts-aware terminal.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="wget">

```
$ wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
$ java -jar nuts.jar -r=+preview -Zy
$ exit
```

:::tip

Any bash terminal application is a nuts-aware terminal.

:::


</TabItem>
<TabItem value="curl">

```
$ curl -sL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar
$ java -jar nuts.jar -r=+preview -Zy
$ exit
```

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>


</Tabs>


Yous should then see some log like the following :

![install-log-example](assets/images/console/install-log-example.png)

As you can see, installation upon first launch, will also trigger installation of other optional programs called "companion tools".
The main recommended helpful is **nsh**  (stands for __Nuts Shell__), is a bash compatible shell implementation application that will run equally on linux and windows systems.

:::important

After installation, you need to restart the terminal application for the configuration to take effect.

:::


## Test Installation
To test installation the simplest way is to open a nuts-aware terminal and type : 

```
nuts --version
```

It should show a result in the format : nuts-api-version/nuts-impl-version

```
0.8.5/0.8.5.0
```

## Run a command

To run a command using **nuts** just type

```
nuts <command>
```

Several commands are available, and you can always manually run any java and non java application.
