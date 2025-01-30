---
id: troubleshooting
title: Troubleshooting
sidebar_label: Troubleshooting
---


Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options
to circumvent this which are two levels or workspace reinitialization.

## recover mode
**recover mode** will apply best efforts to correct configuration without losing them. It will delete all cached data and 
libraries for them to be downloaded later and searches for a valid nuts installation binaries to run (it will actually 
do a forced update). To run nuts in recover mode type :

```
nuts -z
```

## newer mode
**newer mode** will apply best efforts to reload cached files and libraries. to run nuts in 'newer mode' type:

```
nuts -N
```

## reset mode
**reset mode** will apply all efforts to correct configuration by, actually, **deleting** them 
(and all of workspace files!!) to create a new fresh workspace. This is quite a radical action to run. Do not ever
invoke this unless your are really knowing what you are doing. 
To run nuts in reset mode type :

```
nuts -Z
```

## hard-reset mode
**hard-reset mode** will delete all nuts configuration files including all workspaces. 
This is quite a radical action to run. Do not ever invoke this unless your are really knowing what you are doing. 
To run nuts in reset mode type :

```
nuts --hard-reset
```

## kill mode
**kill mode** is a special variant of reset mode where workspace will not be recreated after deletion. 
This can be achieved by using a combination of reset mode and --skip-boot (-Q) option. Do not ever
invoke it unless you are really knowing what you are doing. To run nuts in kill mode type :

To run nuts in kill mode type :
```
nuts -ZQ
```

To can run hard-reset in kill mode too :
```
nuts -Q --hard-reset
```

## After invoking reset mode
After invoking reset mode, nuts shell launchers (installed by nuts settings) will not be available anymore. 
PATH environment will point (temporarily) to a non existing folder. You should use the jar based invocation at least once to reinstall these commands.

```
java -jar nuts.jar
```