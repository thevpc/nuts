---
id: NOptional
title: NOptional
sidebar_label: NOptional
---

${{include($"${resources}/header.md")}}

**nuts** introduces a concept very similar to java's Optional but with better extension builtin mechanisms and helper methods : ```NOptional```

```NOptional``` is extensively used in Nuts Package Manager itself.

## Non Null Assertion

Java has a builtin null Check mechanism but it does not enable customized messages or exceptions.
Optional are described as per Java's (c) Documentation "A container object which may or may not contain a non-null value".
NOptional is more of an Object Wrapper than addes several useful null related operators like '??' '?.' and '!' in typescript. 

<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'java', },
{ label: 'Typescript', value: 'typescript', }
]
}>
<TabItem value="windows">

```typescript 
    if(stringWord==null){
        throw new Exception("missing user name");
    }
    stringWord!.toUpperCase()
```

</TabItem>

<TabItem value="java">

```java
    if(stringWord==null){
        throw new IllegalArgumentException("missing user name");
    }
    stringWord.toUpperCase()
```

</TabItem>

<TabItem value="NAF">

```java 
    NOptional.ofNamed(stringWord,"user name").get().toUpperCase();
    // will throw an IllegalArgumentException|NIllegalArgumentException with "missing user name" message;
```

</TabItem>

<Tabs>


## Nullish Coalescing


<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'java', },
{ label: 'Typescript', value: 'typescript', }
]
}>
<TabItem value="windows">

```typescript 
    var roadNumber=road.number??10;
```

</TabItem>

<TabItem value="java">

```java
    Number roadNumber=road.number!=null?road.number:10;
```

</TabItem>

<TabItem value="NAF">

```java 
    Number roadNumber=NOptional.of(road.number).orElse(10);
```

</TabItem>

<Tabs>


## Optional Chaining

<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'java', },
{ label: 'Typescript', value: 'typescript', }
]
}>
<TabItem value="windows">

```typescript 
    var roadNumber=app?.person?.address?.road?.number;
```

</TabItem>

<TabItem value="java">

```java
    Number roadNumber=(app!=null && app.person!=null && app.person.road!=null)? app.person.address.road.number:null;
```

</TabItem>

<TabItem value="NAF">

```java 
    Number roadNumber=NOptional.of(app).then(v->v.person).then(v->v.road).then(v->v.number).orNull();
```

</TabItem>

<Tabs>


## Combining Optional Chaining

<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'java', },
{ label: 'Typescript', value: 'typescript', }
]
}>
<TabItem value="windows">

```typescript 
    var roadNumber=app?.person?.address!.road?.number??0;
```

</TabItem>

<TabItem value="java">

```java
    Address address=(app!=null && app.person!=null)?app.person.address:null;
    if(address==null){
       throw new IllegalArgumentException("missing address"); 
    }
    Number roadNumber=(address!=null 
        && address.road!=null)
        ? address.road.number:0;
```

</TabItem>

<TabItem value="NAF">

```java 
    Number roadNumber=NOptional.of(app).then(v->v.person).then(v->v.address).get().then(v->v.road).then(v->v.number).orElse(0);
```

</TabItem>

<Tabs>

