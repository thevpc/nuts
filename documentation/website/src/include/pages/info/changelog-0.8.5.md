---
id: changelog085
title: Change Log 0.8.5
sidebar_label: Change Log
order: 50
---

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.5.0 (DEVELOPMENT VERSION)
- ```2024/09/06 	nuts 0.8.5.0``` not released yet nuts-runtime-0.8.5.0
- ADDED: added new tool `ndoc` and `nlib-doc` (the related library) to generate statically html websites...
- FIXED: enhanced NExprs toolkil to support complex expressions, and is now default for ndoc
- DEPRECATED: deprecated for removal nclown (was really never used)
- DEPRECATED: deprecated for removal old docusaurus website and replace with one new based on ndoc (a tool by nuts!!)
- CHANGED: When generating preview jar, it will support natively thevpc.net
- ADDED: added option --preview-repo | -U to help simplifying preview mode installation. 
- CHANGED: nuts-app now depends only on nuts-boot 
- CHANGED: extracted nuts boot that does only load runtime all within its API. This will make it simpler api upgrades 
- CHANGED: nuts is exploded into nuts (api library ) two applications nuts-app (minimal) and nuts-app-full (including runtime) 
- WARNING: The OSS License has changed from APACHE2 to LGPL3
- ```2024/09/06``` started new version
