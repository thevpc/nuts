---
id: downloadPreview
title: Preview Binaries
---

Download Nuts Preview version {{runtimeVersion}}.
Download preview version, this is not production ready version.

Download Nuts jar file to perform installation using your favourite shell. After downloading the installer, follow the documentation to install the package manager. Use 'Portable' version for production and 'Preview' for all other cases. A valid java 1.8+ runtime is required.

<Tabs
defaultValue="linux"
values={[
{ label: 'Linux', value: 'linux', },
{ label: 'MacOS', value: 'macos', },
{ label: 'Windows', value: 'windows', },
]
}>
<TabItem value="windows">

download manually [https://thevpc.net/nuts/nuts-preview.jar](https://thevpc.net/nuts/nuts-preview.jar)

```
java -jar nuts.jar  -Zy
```

On Windows systems, first launch will create a new **```nuts```** Menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal.
- **nuts-cmd-{{apiVersion}}** : this shortcut will open a configured command terminal. **```nuts```** command will be available as well as several nuts companion tools installed by **nadmin** by default
- **nuts-cmd**       : this shortcut will point to the last installed **nuts** version, here {{apiVersion}}

Any of these shortcuts will launch a nuts-aware terminal.

Supported Windows systems include Window 7 and later.

:::tip

Any of the created shortcuts for windows is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="linux">

__(using wget):__
This will reuse any previous nuts installation before installing the latest version.

```
$ wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
$ java -jar nuts.jar -y
$ exit
```

__(using curl):__
This will reuse any previous nuts installation before installing the latest version.

```
$ curl -sOL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar
$ java -jar nuts.jar -Zy
$ exit
```

Linux Systems installation is based on bash shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances. Note that shells are also supported and the relative `rcfiles` are also updated (including `zsh`, `fish` etc)
Using **```nuts```** on unix-like system should be seamless. A simple bash terminal (Gnome Terminal, KDE Konsole,...) is already a nuts-aware terminal.

All Linux versions and distributions should work with or without X Window (or equivalent). Graphical system is required only if you plan to run a gui application using **nuts**.
All tests where performed on OpenSuse Tumbleweed.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>

<TabItem value="macos">

```
$ curl -sOL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar
$ java -jar nuts.jar -Zy
$ exit
```

MacOS Systems installation is based on **bash** shell. First launch will configure "~/.bashrc" so that **nuts** and other companion tool commands will be available in any future terminal instances.
Using **```nuts```** on MacOS system should be seamless. A simple bash terminal (MacOs Terminal App) is already a nuts-aware terminal.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>

</Tabs>
