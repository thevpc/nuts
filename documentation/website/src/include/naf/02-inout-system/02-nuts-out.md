---
title: NOut Standard output
---


The `NOut` class is a simple and powerful utility for writing to the standard output in Nuts. It provides a consistent and extensible way to print text, formatted messages, and structured data.

By default, `NOut` delegates to the session's configured output stream, defined as an `NPrintStream` in the current `NSession`. This stream is **customizable**, **structured**, and **NTF-aware**, making it suitable for both human-readable and machine-readable outputs (JSON, XML, etc.).

Unlike `System.out`, `NOut` provides enhanced capabilities:
- Intelligent rendering of objects (beyond basic `toString()`),
- Colorized and formatted output via **NTF (Nuts Text Format)**,
- Support for various structured formats (e.g., JSON, YAML, XML, TSON),
- Support for formatted messages with placeholders,
- Table and tree rendering.

## Basic Usage

The simplest way to print a message to the console:

```java
    Nuts.require();
    NOut.println("Hello");
```

## Using NTF
NTF enables you to add rich formatting and colorization:

```java
    NOut.println("##Hello colored## ##:_:Hello underlined## ");
    NOut.println("##:yellow:Hello in yellow##");
    NOut.println("##:warn:this is a warning##");
    NOut.println("##:fxFF000:this is a red message##");
```

## Rendering structured output
`NOut` can render structured output based on the active format in the `NSession`.

```java
    class Customer{String id;String name;}
    Customer customer1,customer2,customer3; ...
    // configure le current output to render objects as json
    // to display the curstomer list as a json array
    NSession.of().json();
    NOut.println(Arrays.asList(customer1,customer2,customer3));

    // you can do the same for yaml,tson,xml,table and tree (as formats)
    NSession.of().tree();
    NOut.println(Arrays.asList(customer1,customer2,customer3));
```

## Formatted Messages
You can build formatted messages using NMsg, with placeholder support and type-aware formatting:

```java
    NOut.println(NMsg.of("this is a %s message that is %s %% beautiful",true,100));
```

Values such as booleans and numbers are rendered with distinct styles (e.g., colors) for better readability.

Or you can build your own styled arguments :

```java
    NOut.println(NMsg.of("this is a %s ",NMsg.ofStyledPrimary1("message")));
```


## Working with Tables
To have full control over tabular output, use `NMutableTableModel`:

```java
    NSession session=...;
    Object a,b,c,d; ...
    NMutableTableModel m = NMutableTableModel.of();
    m.newRow().addCells(a,b,c,d);
    NOut.println(m);
```

## Working with Trees
To render hierarchical structures, you can implement a custom `NTreeModel`:

```java
    NOut.println(
        new NTreeModel() {
            @Override
            public Object getRoot () {
                return "/";
            }
        
            @Override
            public List<NDependencyTreeNodeAndFormat> getChildren (Object node){
                if ("/".equals(node)) {
                    return Arrays.asList(1,2,3);
                }
                return Arrays.asList();
            }
        }
        );
```

## Summary
The `NOut` class provides a robust and extensible mechanism for console output in the Nuts ecosystem. Whether you're logging simple messages, displaying structured data, or building CLI tools, NOut ensures consistent and powerful rendering—fully aligned with Nuts' NTF and output formatting infrastructure.
