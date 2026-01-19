# 📄 TSON Specification (v2.0)

**TSON** (Type Safe Object Notation) is a human-readable, whitespace-aware configuration and DSL format that combines the simplicity of outlines with the expressiveness of structured data. Its primary rationale is to provide a **type-safe** alternative to JSON/YAML, ensuring that data structures are strictly typed and easily validated. It supports **primitives**, **strings**, **structured literals**, **streams**, **annotations**, and **lists** — all while preserving every token for round-tripping, tooling, and diagnostics.

> [!TIP]
> **Key Principles**
> - **Fail-never**: Invalid input is preserved, not rejected.
> - **Token-preserving**: Every space, newline, and comment is retained.
> - **Depth-driven hierarchy**: Structure comes from `.` / `#` depth, not indentation.
> - **Interchangeable whitespace**: Spaces and newlines are equivalent (except in literals).

---

## TSON Data Model
A TSON document is represented as a tree of TsonElement nodes. Every element is one of the following:

    TsonPrimitive: A literal value (String, Number, Boolean, Null) optionally tagged with a Unit Suffix (e.g., 5%P) for numbers.
    TsonName: A "naked" identifier that carries semantic meaning without a value (e.g., bold).
    TsonPair: A Key-Value association where the key is a String and the value is any TsonElement.
    TsonContainer: An ordered collection of TsonElement nodes. Containers can be:
        Braced {}: Typically used for object-like mapping.
        Bracketed []: Typically used for list-like sequences.
        Parenthesized (): Typically used for tuples or function-like arguments.
        Ordered Lists # : primarily used for readability in documentation or simple configurations.
        Unordered Lists '.' (dot) : primarily used for readability in documentation or simple configurations.
    TsonAnnotation: A special node (prefixed with @) that provides metadata to the element following it.


## 1. Primitive Literals

TSON supports a wide range of primitive types, including booleans, null, and sophisticated number formats.

### 1.1 Booleans and Null
```tson
true
false
null
```

### 1.2 Number Literals
TSON supports real, complex, typed, and annotated numbers in a unified syntax. All forms support optional type annotations (`_s32`, `_u64`, etc.) and **suffixes** (e.g., `%`, `ms`).

**General Form:**
`[sign][digits][type][suffix]`

#### Decimal Integers and Floats
```tson
42       // Integer
-17      // Negative integer
3.14     // Float
-0.5     // Negative float
1.       // Trailing dot allowed
.25      // Leading dot allowed
```

#### Scientific Notation
```tson
1e6      // 1,000,000
-2.5e-3  // -0.0025
1E+10    // Case-insensitive
```

#### Hexadecimal, Binary, and Octal
```tson
0xFF     // Hexadecimal (255)
0b1010   // Binary (10)
0o755    // Octal (493)
-0x10    // Negative hex
```

#### Number Constants
TSON supports special constants for minimum, maximum, infinity, and NaN values of various types. These are prefixed with `0` followed by the constant name (`max`, `min`, `pinf` for positive infinity, `ninf` for negative infinity, `nan` for Not-a-Number) and the type name.
- The constant name (`max`, `min`, `pinf`, `ninf`, `nan`) is **case-insensitive**.
- The type part (`u`, `s`, `f`) must be **lowercased**.
- Constants can also take suffixes.

```tson
0max_s8    // Maximum signed 8-bit integer (127)
0min_s16   // Minimum signed 16-bit integer (-32768)
0pinf_f32  // Positive infinity for 32-bit float
0NINF_f64  // Negative infinity for 64-bit float (case-insensitive name)
0max_s8%   // Constant with suffix
```

#### Complex Numbers
The imaginary unit is `i` or `î` (lowercase). It must appear after the imaginary part.
```tson
3+4i     // Real + Imaginary
-1.5i    // Pure imaginary
-6-1.2î  // Negative real and imaginary
```

#### Type Suffixes
Numbers can be explicitly typed or include custom suffixes (which can represent units). TSON uses `s` (signed) or `u` (unsigned) followed by bit width for types. The underscore `_` before the type or suffix is **optional**.
- For integers, `s` and `u` specify signedness and bit width (e.g., `s32`, `u64`).
- For floating point numbers, both `s` and `u` are used to specify bit width/precision (e.g., `s64`, `u64` for doubles).
- **Constraint**: Suffixes cannot include numbers. They can only contain letters, `%`, or `_`.

```tson
12_s32    // Signed 32-bit integer
12s32     // Same as above (underscore is optional)
12_u64    // Unsigned 64-bit integer
12u64     // Same as above
12_sN     // BigInteger (Arbitrary precision)
1.2_s64   // 64-bit float (double)
12.0u64   // 64-bit float (double) using 'u' suffix
100ms     // With suffix (interpreted as unit)
50%       // With suffix
1.2i_s32% // Complex with type and suffix
```

TSON is semantically neutral. While the syntax supports suffixes (e.g., 10ms, 50%) and unquoted identifiers (e.g., Blue), the TSON parser does not validate or convert these. It is the responsibility of the consuming application (e.g., a layout engine or scientific tool) to map these tokens to their respective domain-specific logic.

---

## 2. Identifiers

Identifiers are unquoted symbolic names used for keys, variables, or labels. They are not strings, but can be converted to strings when needed.
Syntax Rules
- Start: Must begin with a Unicode letter, _, or $.
- Continue: May contain Unicode letters, digits, _, $, - (hyphen), and . (dot).
- Constraints:
  - Cannot start or end with - or ..
  - Cannot contain consecutive separators like --, .., or -..

```tson

name
user-id
api.endpoint
Ω
π
$var
café.menu

```

## 3. Strings

TSON provides multiple ways to represent text, from simple quoted strings to multi-line blocks.

### 3.1 Quoted Strings
- **Quoted Strings**: Support single (`'`), double (`"`), and backtick (`` ` ``) quotes. All quoted strings are **multi-line by design**.
- **Triple Quotes**: Support `'''`, `"""`, and ` ``` `.
- **Escaping**: Quoted strings only support escaping the **terminal quote** character with a backslash (e.g., `\"` in a double-quoted string, `\'''` in a triple-single-quoted string).
    - A backslash `\` by itself is treated as a literal character.
    - Standard escape sequences like `\n`, `\r`, or `\t` are **not interpreted** by the parser; they are preserved as literal text to be interpreted at use-time.

````tson
"hello world"
'single quotes'
`backticks`
"multi-line
quoted string"
"escaped \"quote\""
"literal \n (not a newline)"

// Triple Quotes
"""Triple double quotes"""
'''Triple single quotes'''
```Triple backticks```

"""
Multi-line
triple double quotes
"""
````

### 3.2 Single-line Strings (`¶`)
A single `¶` starts a string that continues until the end of the line. **No escaping sequences** are supported.
```tson
¶ This is a single-line string.
```

### 3.3 Multi-line Strings (`¶¶`)
Use `¶¶` for multi-line text. The string continues as long as subsequent lines also start with `¶¶`. **No escaping sequences** are supported.

Multiline strings are defined by a repeating prefix at the start of each line.
- Aggregation: Consecutive lines starting with ¶¶ are aggregated into a single TsonString element.
- Newline Preservation: The newline character between lines is preserved in the resulting string.
- Termination: The element terminates at the first line that does not start with the ¶¶ prefix.
- Strip Rule: The leading ¶¶ prefix is stripped from each line before being added to the value.


```tson
¶¶ Line 1 of a long text.
¶¶ Line 2 of the same text.
```

Rule: Does the ¶¶ have to be the very first character on the line, or can it be preceded by indentation?

---

## 4. Expressions

TSON is expression-oriented. Almost everything is an expression, and expressions can be combined using arbitrary operators.

### 4.1 Operators
TSON supports a wide range of predefined **symbolic** operators (prefix, suffix, and infix).
- **No Parse-time Precedence**: The TSON parser does **not** process operator priority or precedence.
- **Order Preservation**: Expressions are parsed exactly in the order they appear. For example, `1+2*3` is parsed as a sequence of terms and operators. Precedence and refactoring (e.g., building an AST based on standard math rules) are handled at "use-time" by the application.

```tson
1 + 2 * 3           // Parsed as literal 1, op +, literal 2, op *, literal 3
-x                  // Prefix operator
x++                 // Suffix operator
a && b || c         // Symbolic infix operators

// Exotic Operators
x +++ y             // Triple plus
a ==> b             // Double arrow
value ??? default   // Triple interrogation
@deprecated !!! x   // Exclamation with annotation
```

---

## 5. Structured Literals

Structured literals in TSON include pairs, objects, arrays, and tuples. **Keys, values, and elements** in these structures can be complex expressions (including other objects, arrays, or functions).

### 5.1 Pairs
Pairs represent key-value associations using a colon `:` as a separator.

```tson
key: value
age: 30
(a + b): (c * d)    // Complex key and value
```

### 5.2 Separators
In structured literals (objects, arrays, tuples) and lists, elements are separated by **whitespace**, **commas** `,`, or **semicolons** `;`.
- **Optional**: Commas and semicolons are entirely optional.
- **Interchangeable**: Commas and semicolons are treated identically.
- **Whitespace**: One or more spaces, tabs, or newlines act as a separator.

```tson
[1 2 3]             // Whitespace separation
[1, 2, 3]           // Comma separation
[1; 2; 3]           // Semicolon separation
{ a:1, b:2; c:3 }   // Mixed separation
```

### 5.3 Objects (`{}`)
Objects contain **pairs** or standalone elements. They can have an optional **header** (name) and **parameters**.

| Type | Syntax | Description |
| :--- | :--- | :--- |
| **Object** | `{}` | Simple anonymous object. |
| **Named Object** | `name{}` | Object with a name/tag. |
| **Param Object** | `(args){}` | Object with parameters. |
| **Full Object** | `name(args){}` | Object with both name and parameters. |

**Examples:**
```tson
{}                                  // Simple Object
user{ name: "John", age: 30 }       // Named Object
(id: 1, active: true){ data: "..." } // Param Object
rgba(255, 0, 0, 0.5){ label: "red" } // Full Object
```

> [!NOTE]
> The parameters `(args)` and the body `{}` can contain the same types of elements. An **element** in TSON is the fundamental building block and can be **anything**: a value (primitive, string, object, etc.), a pair, a list, or an annotated expression.

### 5.4 Arrays (`[]`)
Arrays are ordered collections. Like objects, they support names and parameters. **Elements** within an array can be any valid expression.

| Type | Syntax | Description |
| :--- | :--- | :--- |
| **Array** | `[]` | Simple anonymous array. |
| **Named Array** | `name[]` | Array with a name/tag. |
| **Param Array** | `(args)[]` | Array with parameters. |
| **Full Array** | `name(args)[]` | Array with both name and parameters. |

**Examples:**
```tson
[ 1, 2, 3 ]                         // Simple Array
points[ 1, 2, 3 ]                   // Named Array
(type: "int")[ 1, 2, 3 ]            // Param Array
matrix(rows: 2, cols: 2)[ 1, 0, 0, 1 ] // Full Array
```

### 5.5 Tuples (`()`)
Tuples (or uplets) are fixed-size ordered collections. Like objects and arrays, they can be anonymous or named. **Elements** within a tuple can be any valid expression.

| Type | Syntax | Description |
| :--- | :--- | :--- |
| **Tuple** | `()` | Simple anonymous tuple. |
| **Named Tuple** | `name()` | Tuple with a name (similar to a function call). |

**Examples:**
```tson
(1, 2)                              // Simple Tuple
color(255, 128, 0)                  // Named Tuple
point(x: 10, y: 20)                 // Named Tuple with pairs
```

---

## 6. Streams

Streams are used for large or binary data.

### 6.1 Binary Streams
Binary data is enclosed in `^[]`. An optional **encoding** can be specified before the brackets.
- **Default Encoding**: `^[]` is equivalent to `^b64[]` (Base64).
- **Supported Encodings**: Common encodings include `b64` (Base64), `hex` (Hexadecimal), and `b85` (Base85).

```tson
^[YmFzZTY0ZGF0YQ==]      // Default Base64
^b64[YmFzZTY0ZGF0YQ==]   // Explicit Base64
^hex[68656c6c6f]         // Hexadecimal encoding
^b85[He7W%DIdAh]         // Base85 encoding
```

### 6.2 Character Streams
Character streams use a custom delimiter `^id{...^id}`.
```tson
^html{<div>Hello</div>^html}
```

---

## 7. Annotations

Annotations add metadata to elements using the `@` symbol.

```tson
@required
@range(1, 100)
port: 80

@validate(regex="^[a-z]+$")
username: "admin"
```

---

## 8. Lists (Depth-Driven Hierarchy)

Lists are a core feature of TSON, using depth instead of indentation for hierarchy.
TSON supports two types of implicit hierarchical lists:
- Unordered Lists (.): Represent a collection of elements where the order may be secondary to the identity (Cardinality).
- Ordered Lists (#): Represent a sequence where the position is semantically significant (Ordinality).
Nesting Rules: Hierarchies are created by repeating the prefix. However, TSON allows Cross-Prefix Nesting. An ordered item (#) can contain unordered sub-items (..), and vice-versa.
The depth of a node is determined by the total count of the prefix characters (. or #). A node at Level 3 (... or ###) is always a child of the most recent node at Level 2, regardless of whether the Level 2 node was ordered or unordered.

### 8.1 Unordered Lists (`.`)
Each item in a dotted list is a full TsonElement. This allows list items to be primitives, complex objects, or even nested containers
TSON supports a shorthand notation for hierarchical lists, primarily used for readability in documentation or simple configurations.
Syntax: A line starting with one or more dots followed by a space.
Nesting: The number of dots represents the nesting level.
Mapping: A dotted list is parsed into a standard TsonContainer.

The number of dots determines the depth.
```tson
. Fruit
.. Apple
.. Banana
. Vegetable
.. Carrot
```

### 8.2 Ordered Lists (`#`)
```tson
# Step 1
## Substep A
## Substep B
# Step 2
```

### 8.3 Mixed Content
Items can have a value and a sublist.
```tson
. Server Config
.. port: 8080
.. features
... auth
... logging
```

### Cross-Prefix Nesting
Nesting Rules: Hierarchies are created by repeating the prefix. However, TSON allows Cross-Prefix Nesting. An ordered item (#) can contain unordered sub-items (..), and vice-versa.

```tson
# Step One
.. Sub-task A
.. Sub-task B
# Step Two
```

### 8.4 Sparse Depth Jumps
Depth can jump arbitrarily. TSON attaches to the most recent shallower item.
```tson
. Top
..... Deep Child       // depth 5 → child of "Top"
.. Sibling             // depth 2 → also child of "Top"
```

---

## 9. Comments and Whitespace

### 9.1 Comments
```tson
// Inline comment
/* Block 
   comment */
```

### 9.2 Whitespace
Spaces and newlines are interchangeable outside literals.
```tson
. A .. B . C   // Equivalent to multi-line
```

---

## 10. Error Recovery (Fail-Never)

TSON never fails to parse. Malformed constructs are preserved as "error elements".
```tson
. { unclosed object
.. valid child
```
The parser attaches a diagnostic to the unclosed object and continues parsing.

---

## Appendix: Grammar Sketch

```antlr
Document       -> Element*
Element        -> Annotation* (List | Expr)
List           -> (ListItem | OrderItem) (Value? (SubList?)?)
ListItem       -> '.'+
OrderItem      -> '#'+
Expr           -> Term (Op Term)*
Term           -> Value | Pair
Op             -> Operator
Value          -> Primitive | String | Object | Array | Tuple | Stream
Pair           -> Expr (':' | ' ') Expr
Object         -> Header? Body
Header         -> Name? Params | Name
Params         -> '(' (Element (','? Element)*)? ')'
Body           -> '{' (Element (','? Element)*)? '}'
Array          -> Header? ArrayBody
ArrayBody      -> '[' (Element (','? Element)*)? ']'
Tuple          -> Header? TupleBody
TupleBody      -> '(' (Element (','? Element)*)? ')'
Stream         -> '^' Name? '{' Content '^' Name? '}' | '^' Name? '[' Content ']'
```

## 🧩 TSON Common Patterns / Examples

### 1. Application Configuration

```tson
app {
  name: "file-server"
  version: "1.2.0"
  port: 8080_u16
  debug: false
  log {
    level: "info"
    file: "/var/log/app.log"
    rotate: true
  }
  features [
    "auth",
    "cors",
    "metrics"
  ]
}
```

> ✅ Uses named object, typed number (_u16), nested config, and array.
> 🔧 Tooling can validate port is ≤ 65535 at runtime.

### 2. CLI Command Definition (DSL Style)

```tson
@command("deploy")
deploy(image: string, env: string) {
  @option("-f", "--force") force: boolean = false
  @option("-r", "--region") region: string = "us-east-1"
  @arg("image") image
  @arg("env") env
  description: "Deploy container to cloud environment"
}
```

> ✅ Leverages parametrized named object + annotations for CLI metadata.
> 💡 Parsers can generate --help or ZSH completions from this.


### 3. i18n / Localization Bundle

```tson
en {
  greeting: "Hello, {name}!"
  error.timeout: "Request took too long."
  units {
    ms: "milliseconds"
    %: "percent"
  }
}

fr {
  greeting: "Bonjour, {name} !"
  error.timeout: "La requête a pris trop de temps."
}
```

> ✅ Flat key hierarchy via . in keys (error.timeout).
> 🔁 Round-trip safe: translators edit without breaking structure.

### 4. Time-Series Data with Units

```tson
cpu_usage [
  (timestamp: 1705489200, value: 42.5_f32%),
  (timestamp: 1705489260, value: 67.2_f32%),
  (timestamp: 1705489320, value: 0max_f32%)  // 100%
]
```

> ✅ Typed floats + suffix (%) + constants (0max_f32).
> 📊 Plotting tools interpret % as unit; validation ensures value ≤ 100.

### 5. Embedded SQL Query (Safe & Preserved)

```tson
query: ^sql{
    SELECT u.name, o.total
    FROM users u
    JOIN orders o ON u.id = o.user_id
    WHERE u.active = true
^sql}
```

> ✅ Character stream with custom delimiter (^sql{...^sql}).
> 🔒 No escaping needed; parser treats content as opaque blob.

### 6. Error-Resilient Partial Config (Fail-Never in Action)

```tson
. database
.. host: "localhost"
.. port: 5432
.. { unclosed_brace   // ← malformed, but preserved!
.. ssl: true
. cache
.. ttl: 300_s32
```

> ✅ Sparse depth + malformed object.
> 🛠️ Editor shows red squiggle on { unclosed_brace but still parses ssl: true and ttl.

### 7. Complex Math Expression (Deferred Evaluation)

```tson
formula: radius * π * 2
π: 3.1415926535_f64
radius: 5.0_f64
```

> ✅ Expression radius * π * 2 is parsed as [radius, *, π, *, 2].
> 🧮 Evaluation engine applies precedence later — TSON stays neutral.

### 8. Binary Asset Reference
```tson
logo: ^b64[iVBORw0KGgoAAAANSUhEUgAAASwAAACCCAMAAADQNkiAAAAA1BMVEW10NBjBBbqAAAAH0lEQVRo3u3BAQ0AAADCoPdPbQ43oAAAAAAAAIBLcQ8AAa0jZQAAAABJRU5ErkJggg==]
```

> ✅ Base64 stream embedded inline.
> 🖼️ UI loads it as image; config remains text-only.

### 9. Feature Flags with Validation

```tson
@range(1, 10)
@required
max_retries: 3

@validate(regex="^[a-z0-9-]+$")
service_name: "auth-service"

@deprecated("Use 'tls_enabled' instead")
ssl: true
```

> ✅ Annotations drive external validation.
> ⚠️ Linter warns on @deprecated; CI fails if max_retries < 1.

### 10. Mixed List for Documentation Outline

```tson

. Introduction
. API Reference
.. GET /users
... Returns list of users
... @response(200): [User]
.. POST /users
... Creates a new user
. Examples
.. Basic Auth
.. OAuth2 Flow
```

> ✅ Depth-driven hierarchy mirrors NTF section structure.
> 📚 Can be rendered as collapsible TOC in terminal (via NTF).

### 11. REST API Contract

```tson
@title("User Management API")
@api(version: "v1", base_url: "https://api.example.com/v1")

endpoints {
  get_users: endpoint(
    method: "GET",
    path: "/users"
  ) {
    @summary("List all users")
    @security(ApiKeyAuth)
    query {
      page: integer = 1
      @range(1, 100) limit: integer = 20_u16
    }
    responses {
      200: [User]
      401: Error
    }
  }

  create_user: endpoint(
    method: "POST",
    path: "/users"
  ) {
    @summary("Create a new user")
    @security(ApiKeyAuth)
    request { body: CreateUserRequest }
    responses {
      201: User
      400: ValidationError
      409: ConflictError
    }
  }

  get_user_by_id: endpoint(
    method: "GET",
    path: "/users/{id}"
  ) {
    @summary("Get user by ID")
    path_params {
      @pattern("^[a-z0-9]{8,}$") id: string
    }
    responses {
      200: User
      404: NotFoundError
    }
  }
}

// Schemas (unchanged — already object-based)
schemas {
  User { ... }
  CreateUserRequest { ... }
  Error { ... }
}
```

### 12. Protocol Buffer–Style Message Definition

```tson
@package("com.example.models")
@version("proto3")

// Enum
Status {
  UNKNOWN: 0
  ACTIVE: 1
  SUSPENDED: 2
  DELETED: 3
}

// Message
UserMessage(id: 1) {
  @required(string) name = 1
  @optional(string) email = 2
  @repeated(string) tags = 3
  @required(Status) status = 4
  @optional(int64) created_at = 5
  @map(string, string) metadata = 6
}

// Nested message
UserMessage.Profile(id: 2) {
  @optional(string) bio = 1
  @optional(string) avatar_url = 2
}

// Service (gRPC-like)
UserService {
  @rpc GetUser(GetUserRequest) : (UserMessage)
  @rpc ListUsers(ListUsersRequest) : (stream UserMessage)
}

// Request messages
GetUserRequest {
  @required(string) user_id = 1
}

ListUsersRequest {
  @optional(int32) page_size = 1
  @optional(string) page_token = 2
}
```


## Reference Implementation: Nuts

TSON is natively supported in [Nuts](https://github.com/thevpc/nuts)
— a modular, dependency-free Java platform for CLI tools, package management, and structured I/O.

Nuts provides a full-featured, token-preserving TSON parser and writer that implements all TSON v2.0 features, including:

- Round-trip safe parsing (comments, whitespace, and errors preserved)
- Depth-driven list hierarchy
- Typed numbers, streams, annotations, and expressions
- Full DOM manipulation via the NElement API

## 🔧 Basic Usage

```java
// Parse a TSON file
NElement doc = NElementReader.ofTson()
    .read(NPath.of("config.tson"));

// Navigate and modify
NObjectElement server = doc.asObject()
    .flatMap(o -> o.getObject("server"))
    .get();

// Create a modified version using a builder
NObjectElement updatedServer = server.builder()
        .set("port", 9090)          // update or add field
        .build();

// Rebuild the root document with the updated server
NObjectElement updatedDoc = doc.asObject()
        .map(root -> root.builder()
                .set("server", updatedServer)
                .build())
        .get();

// Write back to file
NElementWriter.ofTson()
    .write(NPath.of("config.tson"), updatedDoc);
```


## Implementation Requirements

1. Concrete Syntax Tree (CST) Preservation

To support refactoring tools and authoring environments, parsers should ideally be lossless. This means:
- Whitespace, newlines, and comments are stored as "Trivia" nodes within the tree.
- The sequence of elements must be preserved exactly as written (Ordered Maps).

2. Error Recovery & Synchronization

Parsers must not fail-fast on syntax errors. They should implement a "Synchronization Strategy":
- Invalid Tokens: If a sequence cannot be parsed, it should be captured as a TsonErrorNode and the parser should resume at the next separator (,, ;) or closing brace (}, ], )).
- Partial AST: The resulting tree should contain as much valid data as possible, with error nodes marking the gaps.
- 