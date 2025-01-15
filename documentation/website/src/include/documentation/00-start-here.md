---
id: start_here
sidebar_position: 1
title: Nuts Documentation
---

# Tutorial Intro

Let's discover **Nuts Package Manager** in less than **5 minutes**.

## Yet Another Package Manager for Java

Actually you will be surprised that Nuts is another 'Package Manager', but perhaps it is the first fully functional, and production ready, Java Package Manager that can be used to install and run java applications in a very seamless manner.

Nuts is not a build tool (like `maven` and `gradle`). Nuts is more likely to be compared with nodejs `npm` or python's `pip`


## Getting Started

Get started by **running your first application**.


### What you'll need

- [java](https://www.java.com) version 1.8 or above:
- [java]  compatible operating system including [Linux],[Windows] and [MacOS]

## Install Nuts

you can instakk nuts in various ways. See download page for more details.



```bash
curl -sOL https://thevpc.net/nuts/nuts-stable.jar -o nuts.jar && java -jar nuts.jar -Zy
```

You can type this command into Command Prompt, Powershell, Terminal, or any other integrated terminal of your code editor.

The command also installs all necessary dependencies you need to run Docusaurus.

## Start using nuts

Run the development server:

```bash
nuts install jedit
nuts jedit
```

The `nuts install jedit` command changes searches for **jedit** application and all its dependencies, and installs it the the local repository

The `nuts jedit` command runs `jedit` application.

