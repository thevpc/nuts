---
id: javadoc_Elements
title: Elements
sidebar_label: Elements
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsArrayElement
```java
public interface net.vpc.app.nuts.NutsArrayElement
```
 Array implementation of Nuts Element type.
 Nuts Element types are generic JSON like parsable objects.
 \@author vpc
 \@since 0.5.6
 \@category Elements

### ⚙ Instance Methods
#### ⚙ children()
array items

```java
Collection children()
```
**return**:Collection

#### ⚙ get(index)
element at index

```java
NutsElement get(int index)
```
**return**:NutsElement
- **int index** : index

#### ⚙ size()
element count

```java
int size()
```
**return**:int

## ☕ NutsArrayElementBuilder
```java
public interface net.vpc.app.nuts.NutsArrayElementBuilder
```
 Array element Builder is a mutable NutsArrayElement that helps 
 manipulating arrays.
 \@author vpc
 \@category Elements

### ⚙ Instance Methods
#### ⚙ add(element)
add new element to the end of the array.

```java
NutsArrayElementBuilder add(NutsElement element)
```
**return**:NutsArrayElementBuilder
- **NutsElement element** : element to add, should no be null

#### ⚙ addAll(value)
all all elements in the given array

```java
NutsArrayElementBuilder addAll(NutsArrayElement value)
```
**return**:NutsArrayElementBuilder
- **NutsArrayElement value** : value

#### ⚙ addAll(value)
all all elements in the given array builder

```java
NutsArrayElementBuilder addAll(NutsArrayElementBuilder value)
```
**return**:NutsArrayElementBuilder
- **NutsArrayElementBuilder value** : value

#### ⚙ build()
create array with this instance elements

```java
NutsArrayElement build()
```
**return**:NutsArrayElement

#### ⚙ children()
array items

```java
List children()
```
**return**:List

#### ⚙ clear()
remove all elements from this array.

```java
NutsArrayElementBuilder clear()
```
**return**:NutsArrayElementBuilder

#### ⚙ get(index)
element at index

```java
NutsElement get(int index)
```
**return**:NutsElement
- **int index** : index

#### ⚙ insert(index, element)
insert new element at the given index.

```java
NutsArrayElementBuilder insert(int index, NutsElement element)
```
**return**:NutsArrayElementBuilder
- **int index** : index to insert into
- **NutsElement element** : element to add, should no be null

#### ⚙ remove(index)
add new element to the end of the array.

```java
NutsArrayElementBuilder remove(int index)
```
**return**:NutsArrayElementBuilder
- **int index** : index to remove

#### ⚙ set(other)
reset this instance with the given array

```java
NutsArrayElementBuilder set(NutsArrayElementBuilder other)
```
**return**:NutsArrayElementBuilder
- **NutsArrayElementBuilder other** : array

#### ⚙ set(other)
reset this instance with the given array

```java
NutsArrayElementBuilder set(NutsArrayElement other)
```
**return**:NutsArrayElementBuilder
- **NutsArrayElement other** : array builder

#### ⚙ set(index, element)
update element at the given index.

```java
NutsArrayElementBuilder set(int index, NutsElement element)
```
**return**:NutsArrayElementBuilder
- **int index** : index to update
- **NutsElement element** : element to add, should no be null

#### ⚙ size()
element count

```java
int size()
```
**return**:int

## ☕ NutsElement
```java
public interface net.vpc.app.nuts.NutsElement
```
 Nuts Element types are generic JSON like parsable objects.
 \@author vpc
 \@since 0.5.6
 \@category Elements

### ⚙ Instance Methods
#### ⚙ array()
convert this element to \{\@link NutsArrayElement\} or throw ClassCastException

```java
NutsArrayElement array()
```
**return**:NutsArrayElement

#### ⚙ object()
convert this element to \{\@link NutsObjectElement\} or throw ClassCastException

```java
NutsObjectElement object()
```
**return**:NutsObjectElement

#### ⚙ primitive()
convert this element to \{\@link NutsPrimitiveElement\} or throw ClassCastException

```java
NutsPrimitiveElement primitive()
```
**return**:NutsPrimitiveElement

#### ⚙ type()
element type

```java
NutsElementType type()
```
**return**:NutsElementType

## ☕ NutsElementBuilder
```java
public interface net.vpc.app.nuts.NutsElementBuilder
```
 Nuts Element builder that helps creating element instances.
 \@author vpc
 \@category Elements

### ⚙ Instance Methods
#### ⚙ forArray()
create array element builder (mutable)

```java
NutsArrayElementBuilder forArray()
```
**return**:NutsArrayElementBuilder

#### ⚙ forBoolean(value)
create primitive boolean element

```java
NutsPrimitiveElement forBoolean(String value)
```
**return**:NutsPrimitiveElement
- **String value** : value

#### ⚙ forBoolean(value)
create primitive boolean element

```java
NutsPrimitiveElement forBoolean(boolean value)
```
**return**:NutsPrimitiveElement
- **boolean value** : value

#### ⚙ forDate(value)
create primitive date element

```java
NutsPrimitiveElement forDate(Date value)
```
**return**:NutsPrimitiveElement
- **Date value** : value

#### ⚙ forDate(value)
create primitive date element

```java
NutsPrimitiveElement forDate(Instant value)
```
**return**:NutsPrimitiveElement
- **Instant value** : value

#### ⚙ forDate(value)
create primitive date element

```java
NutsPrimitiveElement forDate(String value)
```
**return**:NutsPrimitiveElement
- **String value** : value

#### ⚙ forNull()
create primitive null element

```java
NutsPrimitiveElement forNull()
```
**return**:NutsPrimitiveElement

#### ⚙ forNumber(value)
create primitive number element

```java
NutsPrimitiveElement forNumber(Number value)
```
**return**:NutsPrimitiveElement
- **Number value** : value

#### ⚙ forNumber(value)
create primitive number element

```java
NutsPrimitiveElement forNumber(String value)
```
**return**:NutsPrimitiveElement
- **String value** : value

#### ⚙ forObject()
create object element builder (mutable)

```java
NutsObjectElementBuilder forObject()
```
**return**:NutsObjectElementBuilder

#### ⚙ forString(value)
create primitive string element

```java
NutsPrimitiveElement forString(String value)
```
**return**:NutsPrimitiveElement
- **String value** : value

## ☕ NutsElementPath
```java
public interface net.vpc.app.nuts.NutsElementPath
```
 Element XPath like filter

 \@author vpc
 \@category Elements

### ⚙ Instance Methods
#### ⚙ filter(element)
filter element to a valid children list

```java
List filter(NutsElement element)
```
**return**:List
- **NutsElement element** : element to filter

#### ⚙ filter(elements)
filter elements to a valid children list

```java
List filter(List elements)
```
**return**:List
- **List elements** : elements to filter

## ☕ NutsElementType
```java
public final net.vpc.app.nuts.NutsElementType
```
 Element type. this an extension of json element types.
 \@author vpc
 \@category Elements

### 📢❄ Constant Fields
#### 📢❄ ARRAY
```java
public static final NutsElementType ARRAY
```
#### 📢❄ BOOLEAN
```java
public static final NutsElementType BOOLEAN
```
#### 📢❄ DATE
```java
public static final NutsElementType DATE
```
#### 📢❄ FLOAT
```java
public static final NutsElementType FLOAT
```
#### 📢❄ INTEGER
```java
public static final NutsElementType INTEGER
```
#### 📢❄ NULL
```java
public static final NutsElementType NULL
```
#### 📢❄ OBJECT
```java
public static final NutsElementType OBJECT
```
#### 📢❄ STRING
```java
public static final NutsElementType STRING
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsElementType valueOf(String name)
```
**return**:NutsElementType
- **String name** : 

#### 📢⚙ values()


```java
NutsElementType[] values()
```
**return**:NutsElementType[]

### 🎛 Instance Properties
#### 📄🎛 primitive
true if private type
```java
[read-only] boolean public primitive
public boolean isPrimitive()
```
### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsObjectElement
```java
public interface net.vpc.app.nuts.NutsObjectElement
```
 Object implementation of Nuts Element type.
 Nuts Element types are generic JSON like parsable objects.
 \@author vpc
 \@since 0.5.6
 \@category Elements

### ⚙ Instance Methods
#### ⚙ children()
object (key,value) attributes

```java
Collection children()
```
**return**:Collection

#### ⚙ get(name)
return value for name or null.
 If multiple values are available return any of them.

```java
NutsElement get(String name)
```
**return**:NutsElement
- **String name** : key name

#### ⚙ size()
element count

```java
int size()
```
**return**:int

## ☕ NutsPrimitiveElement
```java
public interface net.vpc.app.nuts.NutsPrimitiveElement
```
 primitive values implementation of Nuts Element type. Nuts Element types are
 generic JSON like parsable objects.

 \@author vpc
 \@since 0.5.6
 \@category Elements

### 🎛 Instance Properties
#### 📄🎛 boolean
value as any java Boolean. Best effort is applied to convert to this
 type.
```java
[read-only] boolean public boolean
public boolean getBoolean()
```
#### 📄🎛 date
value as any java date. Best effort is applied to convert to this type.
```java
[read-only] Instant public date
public Instant getDate()
```
#### 📄🎛 double
true if the value is or can be converted to double
```java
[read-only] boolean public double
public boolean isDouble()
```
#### 📄🎛 float
true if the value is or can be converted to float
```java
[read-only] boolean public float
public boolean isFloat()
```
#### 📄🎛 int
true if the value is or can be converted to int.
```java
[read-only] boolean public int
public boolean isInt()
```
#### 📄🎛 long
true if the value is or can be converted to long.
```java
[read-only] boolean public long
public boolean isLong()
```
#### 📄🎛 null
true if the value is null (in which case, the type should be NULL)
```java
[read-only] boolean public null
public boolean isNull()
```
#### 📄🎛 number
value as any java Number. Best effort is applied to convert to this type.
```java
[read-only] Number public number
public Number getNumber()
```
#### 📄🎛 string
value as any java string. Best effort is applied to convert to this type.
```java
[read-only] String public string
public String getString()
```
#### 📄🎛 value
value as any java Object
```java
[read-only] Object public value
public Object getValue()
```
