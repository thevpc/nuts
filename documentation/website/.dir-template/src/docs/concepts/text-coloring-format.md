---
id: doc1
title: Nuts Text Format
sidebar_label: Nuts Text Format
---

${{include($"${resources}/header.md")}}

# Nuts Text Format
**```nuts```** comes up with a simple coloring syntax that helps writing better looking portable command line programs.
standard output is automatically configured to accept the "Nuts Text Format" (NTF) syntax. 
Though it remains possible to disable this ability using the --!color standard option (or programmatically, 
see **```nuts```** API documentation). NTF will be translated to the underlying terminal implementation using ANSI 
escape code on linux/windows terminals if available.

Here after a showcase of available NTF syntax.

![text-coloring-format](text-coloring-format-01.png)


![text-coloring-format](text-coloring-format-02.png)


![text-coloring-format](text-coloring-format-03.png)


![text-coloring-format](text-coloring-format-04.png)

# Nuts Text Format Specification

```
<TOKEN> S10: '##########'
<TOKEN> S9 : '#########'
<TOKEN> S8 : '########'
<TOKEN> S7 : '#######'
<TOKEN> S6 : '######'
<TOKEN> S5 : '#####'
<TOKEN> S4 : '####'
<TOKEN> S3 : '###'
<TOKEN> S2 : '##'
<TOKEN> S1 : '##'
<TOKEN> A3 : '```'

<RULE>  S2 ':' KEY ':' ANYTHING S2
<RULE>  S2 '{:' WORD ANYTHING S2
<RULE>  13 ANYTHING A3

```