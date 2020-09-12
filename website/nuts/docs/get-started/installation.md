---
id: installation
title: Installation
sidebar_label: Installation
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

```
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )
\_\ \/\__,_/\__/____/    version v0.7.0
```

## System Requirements

Here are all **```nuts```** requirements :

- **Java** : **```nuts```** requires a valid Java Runtime Environment (JRE) or Java Development Kit (JDK) version **8** or above to execute.
- **System Memory**: **```nuts```** memory footprint is very little and has no minimum RAM requirements.
- **Disk**: 2.5Mo on the disk are required for the **```nuts```** installation itself. In addition to that, additional disk space will be used for your local Nuts workspace. The size of your local workspace will vary depending on usage but expect at least 500MB.
- **Operating System**: **```nuts```** is able to run on any java enabled Operating System including all recent versions of Windows, Linux and MacOS.

To check if you have a valid java installation type

```bash
java -version
```

The result would be equivalent to the following. Just be sure the version is 1.8 or over. In this example, 
the java version is 13.0.1

```bash
$> java -version
java version "13.0.1" 2019-10-15
Java(TM) SE Runtime Environment (build 13.0.1+9)
Java HotSpot(TM) 64-Bit Server VM (build 13.0.1+9, mixed mode, sharing)
```


## Installation


<Tabs
  defaultValue="linux"
  values={[
    { label: 'Linux', value: 'linux', },
    { label: 'MacOS', value: 'macos', },
    { label: 'Windows', value: 'windows', },
    { label: '*NIX wget', value: 'wget', },
    { label: '*NIX curl', value: 'curl', },
    { label: 'Any Java enabled OS', value: 'java', },
  ]
}>
<TabItem value="windows">

download [nuts-0.7.0.jar](https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.0/nuts-0.7.0.jar)
```
java -jar -y nuts-0.7.0.jar
```

On Windows systems, first launch will create a new **```nuts```** Menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal.
  + **nuts-cmd-0.7.0** : this shortcut will open a configured command terminal. **```nuts```** command will be available as well 
                         as several nuts companion tools installed by **ndi** by default
  + **nuts-cmd**       : this shortcut will point to the last installed **nuts** version, here 0.7.0  

Any of these shortcuts will launch a nuts-aware terminal.

Supported Windows systems include Window 7 and later.

:::tip

Any of the created shortcuts for windows is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="linux">

```
NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\
/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \
      nuts-$NDVER.jar -zy
```

Linux Systems installation is based on bash shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using **```nuts```** on unix-like system should be seamless. A simple bash terminal (Gnome Terminal, KDE Konsole,...) is already a nuts-aware terminal.

All Linux versions and distributions should work with or without XWindow (or equivalent). Graphical system is required only if you plan to run a gui application using **nuts**.
All tests where performed on OpenSuse Tumbleweed.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="macos">

```
NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\
/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \
      nuts-$NDVER.jar -y
```

MacOS Systems installation is based on **bash** shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using **```nuts```** on MacOS system should be seamless. A simple bash terminal (MacOs Terminal App) is already a nuts-aware terminal.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="wget">

```
  NDVER=0.7.0 && rm -f nuts-$NDVER.jar && wget https://github.com/thevpc/\
vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar &&\
    java -jar nuts-$NDVER.jar -y
```

:::tip

Any bash terminal application is a nuts-aware terminal.

:::


</TabItem>
<TabItem value="curl">

```
  NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\
/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \
      nuts-$NDVER.jar -y
```
:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>

<TabItem value="java">

```
  NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\
/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \
      nuts-$NDVER.jar -y
```

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
</Tabs>


Yous should then see some log like the following :

![install-log-example](../../static/img/install-log-example.png)

As you can see, installation upon first launch, will also trigger installation of other optional programs called "companion tools".
Actually they are recommended helpful tools :
  + **ndi** which stands for __Nuts Desktop Integration__ that helps configuring the desktop to better 
    interact with **```nuts```** by for instance creating shortcuts.
  + **nsh** which stands for __Nuts Shell__ , a bash compatible shell implementation program that will run equally on linux an windows systems.
  + **nadmin** an administration tool for **```nuts```** 

:::important

After installation, you need to restart the terminal application for the configuration to take effet.

:::


## Test Installation
To test installation the simplest way is to open a nuts-aware terminal and type : 

```
nuts --version
```

It should show a result in the format : nuts-api-version/nuts-impl-version

```
00.7.0/0.7.0.0
```

## Run a command

To run a command using **nuts** just type

```
nuts <command>
```

Several commands are available, and you still be able to run any java and non java application. More info is available in the **```nuts```** official [wiki](https://github.com/thevpc/nuts/wiki) .
