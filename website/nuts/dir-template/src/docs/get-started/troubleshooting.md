---
id: troubleshouting
title: Troubleshouting
sidebar_label: Troubleshouting
---

${include($"${resources}/header.md")}

# Troubleshooting nuts
Network Updatable Things Services

# Recover and Reset modes
Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options 
to circumvent this which are two levels or workspace reinitialization. 

## recover mode
**recover mode** will apply best efforts to correct configuration without losing them. It will delete all cached data and 
libraries for them to be downloaded later and searches for a valid nuts installation binaries to run (it will actually 
do a forced update). To run nuts in recover mode type :

```
nuts --recover
```

## reset mode
**reset mode** will apply all efforts to correct configuration by, actually, **deleting** them 
(and all of workspace files!!) to create a new fresh workspace. This is quite a radical action to run. Do not ever
invoke this unless your are really knowing what you are doing. 
To run nuts in reset mode type :

```
nuts --reset
```

## Kill mode
**Kill mode** is a special variant of reset mode where workspace will not be recreated after deletion. 
This can be achieved by using a combination of reset mode and --skip-boot (-Q)option. Do not ever
invoke it unless your are really knowing what you are doing. To run nuts in reset mode type :

To run nuts in prune mode type :
```
nuts --reset -Q
```

## After invoking reset mode
After invoking reset mode, nuts commands (installed by ndi) will not be available anymore. 
you should use the jar base invocation at least once to reinstall these commands.

```
java -jar nuts-0.5.7.jar
```