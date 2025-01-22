---
id: app-ndoc
title: ndoc
sidebar_label: ndoc
---


## T0003- Nuts Document Templater

`ndoc` is a simple templating tool. It is used to generate statically Nuts website from 
markdown documents. 
`ndoc` can also be embedded as a library (as `net.thevpc.nuts.lib:nlib-doc#0.8.5.0`)

```
nuts install ndoc
# Example of usage
nuts ndoc --source your-folder  --target the-generated-folder
```

### Synopsis

```
nuts ndoc (--source=<path>)+ (--resource=<path>)* --target=<path> (<other-options>)*
```

Options are :

- `-s=<...>` ou `--source=<...>` :  source file or folder to process.
- `--resource=<...>` :         source file or folder that are copied as is and are not processed by the template engine.
- `-t=<...>` ou `--target=<...>` : target folder where the generated files will be located

### Templating format ()
```
    \{{: statement}}
    \{{expression}}
    \{{:for varName(,index):<expression}} ... \{{:end}}
    \{{:if expression}} ... \{{:else if expression}} ... \{{:else if expression}} \{{:end}}
```
