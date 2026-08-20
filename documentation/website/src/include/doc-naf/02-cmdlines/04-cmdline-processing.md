---
title: Processing the commandline
---

| expr       | DEFAULT                                | ENTRY                              | FLAG                      | VALUE               | OPTIONAL_VALUE          | TERMINAL              |
|------------|----------------------------------------|------------------------------------|---------------------------|---------------------|-------------------------|-----------------------|
| `--k v t`  | `--k=<null>` 	--k=<null> (v,t remain)  | `--k='v'` (v consumed, t remains)  | `--k=true`                | error?              | `--k='v'`               | `--k=<null>`          |
| `--k= t`   | `--k=''`                               | `--k=''`                           | `--k=boolean(v)`          | `--k=''`            | `--k=''`                | `--k=''`              |
| `--k=v t`  | `--k='v'`                              | `--k='v'`                          | `--k=boolean(v)`          | `--k='v'`           | `--k='v'`               | `--k='v'`             |
| `--k`      | `--k=<null>`                           | `--k=<null>`                       | `--k=true`                | error?              | `--k=<null>`            | `--k=<null>`          |
| `--!k v t` | `--k=<null>`, negated                  | `--k='v'`,  negated                | `--k=false`, negated      | error?              | `--k='v'`,  negated     | `--k=<null>`, negated |
| `--!k=v t` | `--k='v'`, negated                     | `--k='v'`,  negated                | `--k=boolean(v)`, negated | `--k='v'`,  negated | `--k='v'`,  negated     | `--k='v'`, negated    |
| `--!k`     | `--k=<null>` negated                   | `--k=<null>`,  negated             | `--k=false` negated       | error?              | `--k=<null>`,  negated  | `--k=<null>` negated  |

## Using CommandLine, The recommended way...

NCmdLine has a versatile parsing API.
One way to use it is as follows :

```java
    NCmdLine cmdLine = NApp.of().getCmdLine(); // or from somewhere else
NRef<Boolean> boolOption = NRef.of(false);
NRef<String> stringOption = NRef.ofNull();
List<String> others = new ArrayList<>();
    while(cmdLine.

hasNext()){
        cmdLine.

matcher()
                .

with("-o","--option").

matchFlag((v) ->boolOption.

set(v.booleanValue()))
        .

with("-n","--name").

matchEntry((v) ->stringOption.

set(v.stringValue()))
        .

withNonOption().

matchAny((v) ->stringOption.

set(v.image()))
        .

requireDefaults()
        ;
    }
            // test if application is running in exec mode
            // (and not in autoComplete mode)
            if(cmdLine.

isExecMode()){
        //do the good staff here
        NOut.

println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
        }

```

## Using CommandLine, The simple and legacy way...

```java
NCmdLine cmdLine = NApp.of().getCmdLine();
boolean boolOption = false;
String stringOption = null;
List<String> others = new ArrayList<>();
NArg a;
while(cmdLine.

hasNext()){
a =cmdLine.

peek().

get();
    if(a.

isOption()){
        switch(a.

key()){
        case"-o":
        case"--option":{
a =cmdLine.

nextFlag().

get();
                if(a.

isUncommented()){
boolOption =a.

getValue().

asBoolean().

get();
                }
                        break;
                        }
                        case"-n":
                        case"--name":{
a =cmdLine.

nextEntry().

get();
                if(a.

isUncommented()){
stringOption =a.

getValue().

asString().

get();
                }
                        break;
                        }
default:{
        NSession.

of().

configureLast(cmdLine);
            }
                    }
                    }else{
                    others.

add(cmdLine.next().

get().

image());
        }
        }
// test if application is running in exec mode
// (and not in autoComplete mode)
        if(cmdLine.

isExecMode()){
        //do the good staff here
        NOut.

println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
        }
```

