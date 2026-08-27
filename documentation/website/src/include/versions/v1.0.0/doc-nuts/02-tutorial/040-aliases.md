---
id: aliases
title: Aliases, Imports & Launchers
sidebar_label: Aliases, Imports & Launchers
---

:::tip What You'll Learn
In this section, you will learn how to create shortcuts and simplify command execution in **nuts**. We will cover:
* **Imports**: How to omit group IDs when running artifacts.
* **Aliases**: How to create custom commands with preset arguments.
* **Launchers**: How to generate desktop icons and system menu entries for applications.
* **Command Precedence**: How **nuts** resolves what to execute when you type a command.
:::

By default, Maven artifacts are uniquely identified by their full Long ID, consisting of a `groupId`, `artifactId`, `version`, and sometimes a `classifier`. For instance, to invoke the `nsh` shell, you would theoretically need to type:

```bash
nuts net.thevpc.nsh:nsh#{{runtimeVersion}}
```

This is cumbersome for daily use. **nuts** provides three features—Imports, Aliases, and Launchers—that allow you to call artifacts with simple words or clicks instead of typing full coordinates.

## Imports

Imports allow you to discard the `groupId` and call or install artifacts using only their `artifactId`. When you add an import for `com.my-company`, any artifact belonging to that group ID will be resolved automatically.

For example, without imports, running `nsh` looks like this:

```bash
nuts net.thevpc.nsh:nsh#{{runtimeVersion}}
```

If we import the group ID, we can omit it entirely:

```bash
nuts settings add import net.thevpc.nsh
# Now call it simply with the artifactId:
nuts nsh#{{runtimeVersion}}
# Or, if you want the default/latest version, even simpler:
nuts nsh
```

> **Note:** By default, **nuts** already imports a few core group IDs, including `net.thevpc`, which is why many built-in companion tools work immediately without requiring you to configure imports manually.

## Aliases

Aliases allow you to define your own custom commands by wrapping existing artifacts and pre-defining specific arguments. This is very similar to standard shell aliases (e.g., where `ll` is defined as an alias for `/bin/ls -l`).

Let's say you frequently run a tool with a specific set of verbose flags and custom parameters:

```bash
nuts settings add alias ll='net.thevpc.nsh:nsh#{{runtimeVersion}} -c ls -l'
```

Now, instead of typing the full artifact ID and its arguments, you can simply call your alias:

```bash
nuts ll
```

Aliases are powerful because they are stored securely in your workspace configuration and persist across sessions, unlike standard shell aliases which are often bound to a specific terminal profile (like `.bashrc`).

## Launchers

Launchers take the concept of an alias one step further by integrating directly with your Operating System's desktop environment. Launchers allow you to create Desktop Environment shortcuts, system menu entries, and icons (whenever a GUI is applicable).

To create a launcher that adds a shortcut to your OS applications menu and your desktop:

```bash
nuts settings add launcher --menu --desktop 'net.thevpc.nsh:nsh#{{runtimeVersion}}'
```

After running this command, you no longer need the terminal to start the application. You can simply click on the new desktop icon or open it via your OS application menu.

## Command Precedence

When you type a short command like `nuts build`, how does **nuts** know exactly what you mean? **nuts** resolves commands using a strict order of precedence:

1. **Built-in Commands**: First, **nuts** checks if the word is a core command (e.g., `install`, `search`, `settings`).
2. **Aliases**: If it's not a built-in command, it checks your configured workspace aliases.
3. **Imported Artifacts**: Next, it appends your configured Imports (group IDs) to see if an installed artifact matches the name.
4. **Full Artifact ID**: It checks if the string itself is a valid, resolvable Long ID in your configured repositories.
5. **System Commands**: Finally, if all else fails, it may attempt to delegate to the underlying operating system path, depending on your execution mode.

## Managing Configurations

You can easily manage your aliases, imports, and launchers using the `settings` command.

### Listing
To view what is currently configured in your workspace:
```bash
nuts settings list imports
nuts settings list aliases
nuts settings list launchers
```

### Removing
To remove a configuration, use the `remove` subcommand:
```bash
nuts settings remove import net.thevpc.nsh
nuts settings remove alias ll
```

## Summary Comparison

| Feature | Purpose | Scope | Example |
|---------|---------|-------|---------|
| **Import** | Omits the `groupId` so you can use the `artifactId` directly. | Workspace-wide resolution | `nuts settings add import com.myorg` |
| **Alias** | Creates a custom shorthand command for an artifact + arguments. | Workspace CLI command | `nuts settings add alias my-task='my-app -v run'` |
| **Launcher**| Integrates an application into the OS UI (desktop/menus). | OS Desktop Environment | `nuts settings add launcher --desktop 'my-gui-app'` |
