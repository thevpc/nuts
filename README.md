# nuts
Network Updatable Things Services
<pre>
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   dev version 0.8.6.0 
\_\ \/\__,_/\__/____/    production version 0.8.5.0 
</pre>

Website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)
Docs: [Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

## What is Nuts?

```nuts``` is a Java™ Package Manager that that makes running Java applications as simple as typing:

```bash
nuts <package>
```

Unlike Maven, which resolves dependencies at compile time, ```nuts``` resolves them at install or runtime, saving disk space and bandwidth by caching only what’s needed for the current environment and sharing it across apps.

```nuts``` is fully compatible with existing Maven-built JARs—you don’t need to repackage your app or add custom metadata. If it’s in Maven Central (or any repo), you can run it with Nuts.

No more ugly lib folders or fat-jars. Just install and run in a single command.

## Why Nuts?

- Zero Fat-Jars – Get rid of bloated single JARs.
- Shared Dependencies – Save disk space and bandwidth.
- Works with Existing Maven JARs – No special packaging required.
- Cross-Platform – Runs anywhere Java runs.
- Dynamic Updates – Upgrade tools and apps easily.

## Quick Start
(coming soon)

## Installation

You can install Latest or Stable versions of Nuts:
- Latest (Recommended) → Best for most users and developers. Safe, tested, and up-to-date.
- Stable → A conservative release for production environments where consistency matters most.

### ✅ Install Latest Version (Recommended)

```bash
curl -s https://thevpc.net/nuts/install-latest.sh | bash
```

Restart your terminal, then verify:

```
nuts --version
```

### ✅ Install Stable Version (For Production)

```bash
curl -s https://thevpc.net/nuts/install-stable.sh | bash
```

Restart your terminal, then verify:

```
nuts --version
```

### Update Nuts
Already installed? Just run:

```
nuts update
```

## Basic Usage

Install a package:

```
nuts install <package>
```

Run a package:

```
nuts <package>
```

Search a package:

```
nuts search <package>
```

Explore all commands:

```
nuts <package>
```

More details at [https://thevpc.github.io/nuts/doc-nuts.html](https://thevpc.github.io/nuts/doc-nuts.html)


## Try Nuts (Cool Examples)


| Command                                   | Short Description                        |
|-------------------------------------------|------------------------------------------|
| nuts io.github.jiashunx:masker-flappybird | run game flappybird                      |
| nuts org.jmeld:jmeld                      | run a folder diff tool                   |
| nuts org.jd:jd-gui                        | run a java decompiler tool               |
| nuts jpass:jpass                          | jpass a Password Manager                 |
| nuts org.jedit:jedit                      | jedit text editor tool                   |
| nuts netbeans-launcher                    | run a multi instance runner for netbeans |
| nuts kifkif                               | run a file duplicates tool               |
| nuts pnote                                | run a pnote taking tool                  |
| nuts nops  --gui                          | run a nops DevOps tool                   |
| nuts ntexup --view                        | run a Latex like Presentation Tool       |
|                                           |                                          |

## Contribute
```nuts``` is open-source and evolving. Join us!

[Contribute on GitHub](https://thevpc.github.io/nuts/doc-nuts.html)

## License
Licensed under the GNU LESSER GENERAL PUBLIC LICENSE v3.
