---
title: Versions
---

:::tip What You'll Learn
In this section, you will learn how **nuts** handles artifact versioning. We will cover:
* Installing and running multiple side-by-side versions of the same application.
* Understanding version syntax (exact versions, version ranges, and latest).
* Managing the default version for an artifact.
* Listing all installed versions of an application.
* Uninstalling specific versions to clean up your workspace.
:::

## Multiple Artifact Version Installation

One of the key features of **nuts** is its ability to install multiple versions of the same application side-by-side without conflicts. This is particularly useful when testing updates or when different projects require different versions of a tool.

We can, for instance, install two separate versions of `netbeans-launcher`:

```bash
nuts install netbeans-launcher#1.2.2
# then
nuts install netbeans-launcher#1.2.0
```

Now we have two versions installed simultaneously. You can run either one by explicitly specifying its version in the command:

```bash
nuts netbeans-launcher#1.2.2 &
# or
nuts netbeans-launcher#1.2.0 &
```

## Default Versions

When you have multiple versions installed for the same artifact and you try to run it without specifying the version, **nuts** needs to know which one to pick. Every artifact has a **default version** assigned to it. 

By default, the *last version you installed* becomes the default version.

In our earlier example, since `#1.2.0` was installed after `#1.2.2`, it became the default. If you type:

```bash
nuts netbeans-launcher &
```

The `1.2.0` version will be invoked.

### Switching the Default Version

If you want to switch the default back to version `1.2.2`, you simply re-install it:

```bash
nuts install netbeans-launcher#1.2.2
```

Don't worry—no files will be downloaded again. **nuts** will detect that the version is already cached but is not currently marked as default, and it will immediately update the default pointer.

## Version Syntax

When specifying versions in **nuts**, you have several options beyond just exact version numbers. **nuts** supports standard Maven versioning syntax:

* **Exact Versions**: Specify the exact version number after the `#` symbol. 
  Example: `nuts install my-app#1.2.3`
* **Version Ranges**: Use mathematical interval notation to specify acceptable ranges. Brackets `[]` are inclusive, while parentheses `()` or reversed brackets `][` are exclusive.
  Example: `nuts install my-app#[1.0,2.0[` (Installs the highest available version that is >= 1.0 and < 2.0).
* **Latest Version**: Simply omit the version entirely. **nuts** will query the repositories to find the highest stable release.
  Example: `nuts install my-app`

## Listing Installed Versions

If you want to see all the versions of a specific artifact that you currently have installed, you can use the `search` command combined with the `--installed` flag:

```bash
nuts search --installed netbeans-launcher
```

To see which version is marked as default, add the `-l` (long format) flag. The default version will be marked with a capital `I` in the first column, while non-default installed versions will be marked with a lowercase `i`.

```bash
nuts search --installed -l netbeans-launcher
```

## Uninstalling a Specific Version

To keep your workspace clean, you may eventually want to remove older versions of an application. You can use the `uninstall` command and specify the exact version you want to remove:

```bash
nuts uninstall netbeans-launcher#1.2.0
```

If you omit the version (`nuts uninstall netbeans-launcher`), **nuts** will prompt you to select which version(s) you wish to remove or confirm if you want to remove all of them.

## Summary of Version Commands

| Goal | Command |
|------|---------|
| Install a specific version | `nuts install <app>#<version>` |
| Run a specific version | `nuts <app>#<version>` |
| Run the default version | `nuts <app>` |
| Change the default version | `nuts install <app>#<version>` |
| List all installed versions | `nuts search --installed <app>` |
| Uninstall a specific version | `nuts uninstall <app>#<version>` |
