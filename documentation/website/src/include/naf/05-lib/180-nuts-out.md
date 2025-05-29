---
title: NOut/NErr: Standard In/Out/Err API
---


**nuts** Library suppors colorful and structured output


```java
    Nuts.openWorkspace("-Z","-S","y","--json").share();
NSession session=NSession.of();
    NOut.println("Hello");
    NOut.printlnf("Hello");

    NOut.println(Arrays.asList("Hello"));
        NOut.printlnf("Hello");

    session.err()....;
    session.in()....;
```


```java
    NOut.printlnf("#Hello1# ##Hello2## ##:_:Hello3## ");
    NOut.printlnf("```java public static class MyClass {}```");
    NOut.printlnf("```js public static class MyClass {}```");
    NOut.printlnf("```xml <a>hello</a>```");
    NOut.printlnf("```json {a:'hello'}```");
```

```java
    NErr.printlnf("#Hello1# ##Hello2## ##:_:Hello3## ");
    NErr.printlnf("```java public static class MyClass {}```");
    NErr.printlnf("```js public static class MyClass {}```");
    NErr.printlnf("```xml <a>hello</a>```");
    NErr.printlnf("```json {a:'hello'}```");
```

```java
    NSession session=...;
    class Customer{String id;String name;}
    Customer customer1,customer2,customer3; ...
    //
    session.setOutputFormat(NContentType.JSON).out().printlnf(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.TREE).out().printlnf(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.PLAIN).out().printlnf(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.XML).out().printlnf(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.PROPS).out().printlnf(Arrays.asList(customer1,customer2,customer3))
    NOut.printlnf(Arrays.asList(customer1,customer2,customer3))
```



## Working with Tables

```java
    NSession session=...;
    Object a,b,c,d; ...
    NMutableTableModel m = NMutableTableModel.of();
    m.newRow().addCells(a,b,c,d);
    NOut.printlnf(m);
```

## Working with Trees

```java
    NSession session=...;
    Object a,b,c,d; ...
    NMutableTableModel m = NMutableTableModel.of();
    m.newRow().addCells(a,b,c,d);
    NOut.printlnf(m);
```
