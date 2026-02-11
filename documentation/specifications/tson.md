# 📄 TSON Specification (v2.0)
**Author:** [thevpc](https://github.com/thevpc)  
**Last Updated:** 2026-02-11

**TSON** (Type Safe Object Notation) is an open, human-readable, whitespace-flexible configuration and DSL format that combines the simplicity of outlines with the expressiveness of structured data. Its primary rationale is to provide a **type-safe** alternative to JSON/YAML, ensuring that data structures are strictly typed and easily validated. It supports **primitives**, **strings**, **structured literals**, **streams**, **annotations**, and **lists** — all while preserving every token for round-tripping, tooling, and diagnostics.
**TSON** is a Strongly Typed configuration format. Unlike "Stringly-Typed" formats (like JSON or YAML) that require the application to guess or cast data types, **TSON** guarantees Type Fidelity at the parser level.
- Literal Intelligence: A value like 2025-12-01 isn't a string; it is natively resolved as a LocalDate. A value like 12sN is natively resolved as a BigDecimal.
- Intent Preservation: Suffixes allow the configuration author to communicate hardware or domain-specific intent (e.g., u16, ms, %) directly to the application.
- Note on Type Safety & Schemas: While **TSON** is currently schema-agnostic (allowing for maximum flexibility and "semantically neutral" data structures), the internal engine is designed for Type Safety. Future versions will introduce optional Schema validation to enforce structural constraints, building upon **TSON**'s existing strong-typing foundation.


> [!TIP]
> **Key Principles**
> - **Fail-never**: Invalid input is preserved, not rejected.
> - **Token-preserving**: Every space, newline, and comment is retained.
> - **Depth-driven hierarchy**: Structure comes from `.` / `#` depth, not indentation.
> - **Interchangeable whitespace**: Spaces and newlines are equivalent (except in literals).

Designed as a frictionless superset of JSON, **TSON** offers a trivial learning curve for existing developers while providing the rigorous type safety and metadata support required for modern, large-scale systems.

##  Open Source & Pivot Architecture
**TSON** is an **Open Specification** designed for maximum interoperability. 
The reference implementation (**Nuts TSON**) is Open Source, ensuring transparency and community-driven evolution.
As a Pivot Format, **TSON** is designed to be the **"common denominator"** between various data representations (including JSON,YAML, XML, PROPERTIES, ...).

##  Syntactic Philosophy

**TSON** provides a broad syntactic surface area designed for Human-Centric Data Modeling. Rather than forcing all data into a single quote or list style, **TSON** offers specialized markers for different data densities:
- Paragraphs (¶): Optimized for human-readable notes and documentation.
- Multi-mode Quotes: Allows embedding of code, SQL, or JSON without the 'escaping hell' of standard formats.
- Depth-based Nesting: Reduces visual noise in deeply hierarchical configurations.

### Contextual Ergonomics
**TSON** is designed with the principle of Contextual Ergonomics. Rather than forcing all data into a single, restrictive syntax (the 'one-size-fits-all' approach of JSON), **TSON** provides a rich vocabulary of markers tailored to specific data densities.
- Orthogonal Design: While the syntax is broad, it is non-overlapping. Each marker (e.g., ¶ for comments vs. """ for blocks) occupies a distinct lexical space, ensuring the parser remains deterministic and high-performance.
- Human-Centric Modeling: We provide multiple quote types and paragraph markers so that the data looks like what it represents. Configuration should be as readable to a human as it is to a machine.
- Intent over Escaping: By offering various delimiters (like ^id{...^id}), we eliminate 'escaping hell,' allowing complex data to be embedded in its raw, natural state."


| Lane            | Syntax           | Examples Purpose                                     |            
|:----------------|:-----------------|:-----------------------------------------------------|
| **Pithy**       | '...', [...], 12 | High-density data key-value pairs.                   |   
| **Documentary** | ¶, ¶¶, """       | Human-readable notes and text blocks                 |
| **System**      | 0x, 0b, u16, sN  | Low-level hardware and units and financial precision |
| **Raw/Opaque**  | ^id{...^id}      | Large-scale binary or code embedding |

**TSON**'s operator system is Exhaustive, not Arbitrary. We provide a formal catalog of over 500 symbols encompassing nearly all mathematical, logical, and relational Unicode characters. This ensures that even complex scientific or financial expressions remain deterministic and portable across all **TSON**-compliant parsers.

### The "Literal-First" Principle (The Death of the Backslash)
Most data formats treat the backslash (\) as a "magic" character that triggers escape sequences (like \n for a newline). This often results in "escaping hell," especially when dealing with Windows paths or Regular Expressions.
**TSON** takes a different approach:
- Absolute Fidelity: All characters between delimiters are treated as literal text.
- No Magic: The sequence \n in **TSON** is always two characters: a backslash and an 'n'.
- WYSIWYG: To include a newline, simply insert a literal newline. To include a backslash, simply type a backslash.

This ensures that what you see in your editor is exactly what the application receives, making the format "copy-paste safe" for system paths and shell commands.


Because **TSON** prioritizes explicit intent over implicit guessing, it solves several long-standing pain points found in traditional formats. The following section illustrates how **TSON**’s rich syntax provides a safer, more expressive alternative for users coming from JSON or YAML.

---

## TSON for JSON/YAML Users
If you know JSON, you already know 90% of **TSON**. 
**TSON** is a strict superset of JSON, meaning any valid JSON file is also a valid **TSON** file.

| Feature             | JSON                 | TSON                                | Why it matters                         |
|:--------------------|:---------------------|:------------------------------------|:---------------------------------------|
| **Syntax**          | Strict `{}` and `[]` | ✅ Identical                         | Zero learning curve for basics.        |
| **Quotes**          | Double ("") only     | ✅ Double, Single, Backtick, or None | No more ""quote-escaped"" nightmares.  |
| **Comments**        | ❌ None               | ✅ Inline, Block, and Doc-blocks     | Configuration needs documentation.     |
| **Trailing Commas** | ❌ Forbidden          | ✅ Allowed & Optional                | Faster editing; cleaner git diffs.     |
| **Types**           | ❌ Forbidden          | ✅ Strict bit-width (u32, f64)       | Catch data-type errors at the source.  |

## Whitespace: Preserved, Not Structural
One of the most common sources of bugs in YAML is the "Invisible Error"—a single misplaced space that changes the entire meaning of the document.
Unlike indentation-sensitive formats (such as YAML), **TSON** is indentation-agnostic. 
While **TSON** is "whitespace-aware" in the sense that it preserves every space, tab, and newline for perfect round-tripping and formatting fidelity, whitespace never determines the hierarchy of the data model.
- **Explicit Anchors**: Structural depth is exclusively governed by explicit markers (`{,` `[`, `(`, `•`, `▪`).
- **Interchangeable Separators**: Outside of string literals, a single space, a tab, and a newline are functionally equivalent. They serve as delimiters between elements but do not carry nesting logic.
- **Copy-Paste Safety**: Because the tree structure is baked into the tokens themselves (e.g., a `•child` is a child because of the `•` prefix, not its horizontal offset), **TSON** code can be reformatted or shifted without changing its semantic meaning.

### Handling System Paths

One of the most common sources of bugs in JSON is the requirement to double-escape backslashes.


| Feature                       | JSON                 | TSON                                |
|:------------------------------|:---------------------|:------------------------------------|
| **Windows Path**              | "C:\\Users\\Config"  | "C:\Users\Config"                   |
| **RegEx**                     | "\\d+\\s+"           | "\d+\s+"                            |
| **Visual Clarity**            | Cluttered by \\      | Clean and Literal                   |

## TSON Data Model
A **TSON** document is represented as a tree of `Element` nodes. Every element is one of the following:

- `Primitive`: A literal value (String, Number, Boolean, Null) optionally tagged with a Unit Suffix (e.g., 5%P) for numbers.
- `Name`: A "naked" identifier that carries semantic meaning without a value (e.g., bold).
- `Pair`: A Key-Value Pair association where the key is a String and the value is any `Element`.
- `Container`: An ordered collection of `Element` nodes. Containers can be:
  - `Braced` `{}`: Typically used for object-like mapping.
  - `Bracketed` `[]`: Typically used for list-like sequences.
  - `Parenthesized` `()`: Typically used for tuples or function-like arguments.
  - `Ordered Lists` `[#]` : primarily used for readability in documentation or simple configurations.
  - `Unordered Lists` `[.]` (dot) : primarily used for readability in documentation or simple configurations.
-  `Annotation`: A special node (prefixed with @) that provides metadata to the element following it.


## 1. Primitive Literals

**TSON** supports a wide range of primitive types, including booleans, null, and sophisticated number formats.

### 1.1 Booleans and Null
```tson
true
false
null
```

### 1.2 Number Literals
**TSON** supports real, complex, typed, and annotated numbers in a unified syntax. All forms support optional type annotations (`_s32`, `_u64`, etc.) and **suffixes** (e.g., `%`, `ms`).

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
**TSON** supports special constants for minimum, maximum, infinity, and NaN values of various types. These are prefixed with `0` followed by the constant name (`max`, `min`, `pinf` for positive infinity, `ninf` for negative infinity, `nan` for Not-a-Number) and the type name.
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
**TSON** treats complex numbers as Composite Literals. They are not handled by the lexer as a single string, but are resolved by the parser as an expression of real and imaginary components.

The imaginary unit is `i` or `î` (lowercase). It must appear after the imaginary part.
```tson
3+4i     // Real + Imaginary
-1.5i    // Pure imaginary
-6-1.2î  // Negative real and imaginary
```

##### Lexing the Components
- Real Part: A standard `NUMBER` (e.g., 3).
- Operator: A standard `OPERATOR_SYMBOL` (e.g., +).
- Imaginary Part: A `NUMBER` with the `i` suffix (e.g., `4i`). This is a Typed Literal.

##### Lexical Bonding (The Whitespace Rule)

To resolve the "Complex Number" ambiguity, **TSON** implements a Lexical Bonding rule. This rule determines whether a sequence of characters is treated as a single literal value or a mathematical expression.

**The Bonding Principle**

  - Bonded (No Spaces): If a real number, an operator (+ or -), and an imaginary number are written without intervening whitespace (e.g., 3+4i), the lexer consumes them as a single complex literal.
  - Floating (With Spaces): If whitespace is present (e.g., 3 + 4i), the lexer produces three distinct tokens. The parser then treats this as a FlatExpression.

Formal Rule: Literal Bonding:
**TSON** distinguishes between Data (Literals) and Logic (Expressions) via whitespace proximity.
- Complex Literals must be contiguous. The presence of any whitespace (including newlines) within the real-operator-imaginary sequence breaks the bond, downgrading the sequence to a FlatExpression.
- This ensures that a configuration value like frequency: 3+4i is stored as a constant numeric type, while frequency: 3 + 4i is stored as a calculation to be evaluated.
- Validation: A bonded literal like 4i3 is lexically invalid because the i suffix acts as a terminal for the complex-literal state."

**Implementation Logic**
This is handled at the Lexer level using a "Lookahead" or "No-Space" constraint:
    Complex Token: `(DIGITS)? ('+'|'-') + DIGITS + 'i'` (with no whitespace allowed between components).


| Input       | Token Type                | Resulting Element                 |
|:------------|:--------------------------|:----------------------------------|
| **3+4i_sN** | COMPLEX_LITERAL           | BIG_COMPLEX (Value: 3 + 4i)       |
| **3 + 4i**  | INT, OP, COMPLEX_LITERAL  | FLAT_EXPR (A math operation)      |
| **-1.5i**   | COMPLEX_LITERAL           | DOUBLE_COMPLEX (Value: 0 - 1.5i)  |


#### Type Suffixes
Numbers can be explicitly typed or include custom suffixes (which can represent units). **TSON** uses `s` (signed) or `u` (unsigned) followed by bit width for types. The underscore `_` before the type or suffix is **optional**.
- For integers, `s` and `u` specify signedness and bit width (e.g., `s32`, `u64`).
- For floating point numbers, both `s` and `u` are used to specify bit width/precision (e.g., `s64`, `u64` for doubles).
- **Constraint**: Suffixes cannot include numbers. They can only contain letters, `%`, or `_`.

```tson
12_s32    // Signed 32-bit integer
12s32     // Same as above (underscore is optional)
12_u64    // Unsigned 64-bit integer
12u64     // Same as above
12_sN     // BigInteger (Arbitrary precision)
12.0_sN   // BigDecimal (Arbitrary precision)
1.2_f64_GHz  // 64-bit float (double) with suffix
100ms     // With suffix (interpreted as unit)
100Ω      // With unicode suffix (interpreted as unit)
50%       // With percent suffix
1.2i_s32% // Complex with type and suffix
```

**TSON** is semantically neutral. While the syntax supports suffixes (e.g., 10ms, 50%) and unquoted identifiers (e.g., Blue), the **TSON** parser does not validate or convert these. It is the responsibility of the consuming application (e.g., a layout engine or scientific tool) to map these tokens to their respective domain-specific logic.

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
apiEndpoint
Ω
π
$var
café.menu

```

## 3. Strings

**TSON** provides multiple ways to represent text, from simple quoted strings to multi-line blocks.

**Guideline**: Use the simplest form that doesn't require escaping.

| Syntax      | Best For                                        | Escaping                                            |
|:------------|:------------------------------------------------|:----------------------------------------------------|
| "..."       | Standard strings and JSON-like properties       | double Terminal quote only "", can include newlines |
| '...'       | Short identifiers or strings containing "       | Terminal quote only '', can include newlines        |
| `...`       | Command-line snippets or Shell scripts          | Terminal quote only ``, can include newlines        |
| """..."""   | SQL queries or formatted text blocks            | Terminal quote only """""", can include newlines    |
| '''...'''   | Multi-line regex or nested single-quote strings | Terminal quote only '''''', can include newlines    |
| ```...```   | Embedded Markdown or code blocks                | Terminal quote only `````` , can include newlines   |
| ¶ text      | Inline annotations and quick metadata           | None (ends at newline)                              |
| ¶¶ text     | Header-level documentation or changelogs        | None (consecutive lines)                            |
| ^id{...^id} | Serialized data (XML/JSON) or complex macros    | None (custom delimiter)                             |

### 3.1 Quoted Strings & The "Literal-First" Rule
All quoted strings are multi-line by design and follow the Literal-First principle.
- No Magic Backslashes: The backslash \ is treated as a literal character. Standard sequences like \n, \t, or \r are not converted into control characters by the parser.
- Escaping: Only the terminal quote can be escaped (e.g., \" inside a "" string).
- Result: What you see is exactly what the application receives.

````tson
path: "C:\new\temp"      // Result: C:\new\temp (No newline/tab injection)
regex: "\d+\s+"          // Result: \d+\s+ (No double-backslash required)
multiline: "Line 1
Line 2"                  // Result: Actual newline preserved
````

### 3.2 Quoted Strings
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

### 3.3 Single-line Strings (`¶`)
A single `¶` starts a string that continues until the end of the line. **No escaping sequences** are supported.
```tson
¶ This is a single-line string.
```
A single ¶ starts a string that continues until the end of the line.
- Behavior: Everything after the ¶ (including leading spaces) is part of the string.
- Escaping: No escaping sequences are supported.


### 3.4 Multi-line Strings (`¶¶`)
Multi-line strings use the `¶¶` prefix for consecutive lines. Each element
stores **two representations**:
1. **Raw Value**: The literal text as written (preserves all whitespace)
2. **Clean Value**: With maximum common indentation stripped

```tson
¶¶ Line 1 of a long text.
¶¶ Line 2 of the same text.

   ¶¶ This is line 1.
       ¶¶ This is line 2.
// ^ Indentation here is ignored; result starts at "This is..."
```

#### Parsing Rules
1. **Line Detection**: A line is part of a multi-line string if `¶¶` is the
   first non-whitespace sequence on that line.
2. **Prefix Stripping**: The `¶¶` marker and any whitespace **before** it
   are stripped from each line.
3. **Raw Storage**: The remaining text (after ¶¶) from all lines is joined
   with newlines and stored as the **raw value**.
4. **Common Indent Detection**: Find the maximum amount of leading whitespace
   that is **common to all non-empty lines**.
5. **Clean Storage**: Strip the common indent from each line to produce the
   **clean value**.
6. **Termination**: The string terminates at the first line that does not
   start with `¶¶` (after stripping leading whitespace).


Rule: the ¶¶ can start at amy level of a line, and consumes till the line ends and there is no more ¶¶

---

## 4. Temporal Literals
**TSON** provides native support for date and time types. To prevent ambiguity with mathematical expressions (like 2025-01-01 being parsed as 2025 minus 1 minus 1), **TSON** employs the Temporal Bonding Rule.

### 4.1 Temporal Types
**TSON** categorizes temporal data into four distinct `ElementType` groups:

### 4.2 The Bonding Rule (Resolution of - Ambiguity)

A sequence of digits separated by hyphens (-) or colons (:) is bonded into a Temporal Literal only if:
- No Internal Whitespace: There are no spaces between the digits and separators (e.g., 2025-01-01 is a Date; 2025 - 01 - 01 is a Math Expression).
- Valid Pattern Match: The sequence matches a valid date or time structure.

### 4.3 Handling Edge Cases
Following the "Fail-Never" philosophy, **TSON** handles invalid dates gracefully:
- Invalid Calendar Dates: 2025-02-30 (February 30th) will be lexically captured as a LocalDate. However, isErrorTree() will return true, and the diagnostics() will report an "Invalid Calendar Date."
- Mixed Precision: **TSON** supports optional milliseconds or nanoseconds (e.g., 12:30:00.500).
- Timezone Suffixes: Instants support Z (UTC) or offset notation +HH:mm.


## 4. Expressions

**TSON** is expression-oriented. Almost everything is an expression, and expressions can be combined using arbitrary operators.

**TSON** preserves expressions as **ordered token sequences**:
```tson
result: 1 + 2 * 3
```

**Parsed as**: `[result, :, 1, +, 2, *, 3]`

**Applications choose interpretation**:
- Math context: `7` (multiplication first)
- Left-to-right: `9`
- Custom DSL: Application-defined

**TSON never evaluates** these expressions. Use the Nuts API's
`NFlatExpression::reshape(...)` to construct ASTs with your precedence rules or use default implementations (including java's precedence table)

### 4.1 Operators
**TSON** supports a wide range of predefined **symbolic** operators (prefix, suffix, and infix).
- **No Parse-time Precedence**: The **TSON** parser does **not** process operator priority or precedence.
- **Order Preservation**: Expressions are parsed exactly in the order they appear. For example, `1+2*3` is parsed as a sequence of terms and operators. Precedence and refactoring (e.g., building an AST based on standard math rules) are handled at "use-time" by the application.

```tson
1 + 2 * 3           // Parsed as literal 1, op +, literal 2, op *, literal 3
-x                  // Prefix operator
x++                 // Suffix operator
a && b || c         // Symbolic infix operators

// Exotic Operators
x +++ y             // Triple plus
a ==> b             // Double arrow
a.b(3)              // dot is an operator actually
a ⇒ b               // unicode operator
assemtion : ∀ x ∈ 𝒩 // complex unicode operators
value ??? default   // Triple interrogation
@deprecated !!! x   // Exclamation with annotation
```

#### 4.1.1 Rationale for Deferred Precedence
Unlike a programming language (like C or Java) which has a fixed execution model, **TSON** is a semantic transport format. It is designed to allow domain-specific tools to define their own mathematical or logical rules.
- Domain Sovereignty: In a standard math context, 1 + 2 * 3 is 7. However, in a CSS-like layout engine using **TSON**, or a custom Logic DSL, operators might have entirely different priorities (e.g., a "pipe" operator or a "unit conversion" operator).
- The "Structural-Only" Guarantee: The **TSON** parser's only job is to guarantee the order of tokens. By delivering a flat, ordered sequence of [1, +, 2, *, 3], **TSON** ensures that the raw intent of the author is preserved without the parser "hallucinating" a structure that the target domain might not support.
- Security & Consistency: To prevent inconsistent interpretation, consuming applications are encouraged to use a Standard Evaluation Library (like the NFlatExpression evaluator in Nuts) which provides multiple default, industry-standard precedence tables (for Java, logical, left associative) and provides means to build one's own.
[!TIP] Best Practice for Ambiguity For mission-critical configurations where cross-tool consistency is paramount, **TSON** recommends the use of explicit parentheses: result: 1 + (2 * 3) This ensures that even the most basic consumer interprets the hierarchy correctly.

---

## 5. Structural Fragments and Splicing
**TSON** v2.0 introduces the concept of Structural Fragments. 
A Fragment is a transparent, transient container used during the construction of **TSON** elements. 
It allows for flexible data streaming and "Fail-Never" builder patterns.

### 5.1 The Document as a Fragment
Conceptually, a **TSON** Document is itself a Fragment. Unlike JSON, which requires a single root object or array, a **TSON** document is a stream of elements. 
When parsing a file, the top-level structure is captured as a Fragment, which is then spliced into the host environment's root. 
This allows for multi-root documents and seamless concatenation of **TSON** files.

### 5.2 The Nature of Fragments
Unlike Arrays or Uplets, a Fragment is not a "solid" structural node. It has two fundamental behaviors based on context: Splicing and Defragmentation.
#### A. Splicing (Contextual Dilution)
When a Fragment is added to a collection (such as an Array, the Document Root, or another Fragment), it "dilutes" or "melts." Its children are extracted and added directly to the parent collection as siblings.
- Logic: `array.add(fragment(a, b))` results in `[a, b]`.
- Purpose: Enables seamless programmatic streaming and macro expansion.

#### B. Defragmentation (Structural Healing)
When a context requires exactly one logical node (such as a Pair Key, a Pair Value, or a Flat Expression Operand) but receives a Fragment, the Builder performs an automatic normalization called Defragmentation.


| Fragment Size       | Resulting Node | Logic                                                                           |
|:--------------------|:---------------|:--------------------------------------------------------------------------------|
| **Empty (s=0)**     | `NULL`         | Simple anonymous object.                                                        |
| **Single (s=1)**    | `Element`      | Unwraps the fragment to avoid "junk" nesting.                                   |
| **Multiple (s>1)**  | `Uplet`        | Solidification: Groups elements into an Uplet () to maintain grammar validity.  |

### 5.3 Fragments in Flat Expressions
To preserve Round-Trip Fidelity, the `FlatExprElementBuilder` ensures that the linear stream of tokens adheres to the Operand-Operator-Operand requirement.
If multiple operands are detected consecutively (either via direct addition or fragment splicing), they are automatically grouped into a Solid Uplet.
Example:
- Developer Input: `builder.add("a").add(fragment("b", "c")).add("+").add("d")`
- Normalized AST: `NFlatExpr( NUplet("a", "b", "c"), "+", "d" )`
- Serialized **TSON**: `(a b c) + d`

### 5.4 Comparison: Fragment vs. Uplet

| Feature            | Fragment                            | Uplet                        |
|:-------------------|:------------------------------------|:-----------------------------|
| **Persistence**    | Transient (Transient Builder state) | Persistent (Final AST Node)  |
| **Serialization**  | Invisible (Splices children)        | Explicit (Wrapped in ())     |
| **Arity**          | Zero, One, or Many                  | Always One (Atomic Unit)     |
| **Usage**          | Programmatic Streaming / Macros     | Structural Grouping / Logic  |


### 5.5 Representation of Ambiguity
Fragments allow the Parser and Builder to handle structural ambiguity without throwing errors. 
By allowing a "loose" grouping to exist temporarily, **TSON** ensures that data is never lost during the transition from a raw token stream to a structured tree.

## 6. Structured Literals

Structured literals in **TSON** include pairs, objects, arrays, and tuples. **Keys, values, and elements** in these structures can be complex expressions (including other objects, arrays, or functions).

### 6.1 Pairs
Pairs represent key-value Pair associations using a colon `:` as a separator.

```tson
key: value
age: 30
(a + b): (c * d)    // Complex key and value
```

### 6.2 Separators
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

### 6.3 Objects (`{}`)
Objects contain **pairs** or standalone elements. They can have an optional **header** (name) and **parameters**.

| Type             | Syntax         | Description                           |
|:-----------------|:---------------|:--------------------------------------|
| **Object**       | `{}`           | Simple anonymous object.              |
| **Named Object** | `name{}`       | Object with a name/tag.               |
| **Param Object** | `(args){}`     | Object with parameters.               |
| **Full Object**  | `name(args){}` | Object with both name and parameters. |

**Examples:**
```tson
{}                                  // Simple Object
user{ name: "John", age: 30 }       // Named Object
(id: 1, active: true){ data: "..." } // Param Object
rgba(255, 0, 0, 0.5){ label: "red" } // Full Object
```

> [!NOTE]
> The parameters `(args)` and the body `{}` can contain the same types of elements. An **element** in **TSON** is the fundamental building block and can be **anything**: a value (primitive, string, object, etc.), a pair, a list, or an annotated expression.

### 6.4 Arrays (`[]`)
Arrays are ordered collections. Like objects, they support names and parameters. **Elements** within an array can be any valid expression.

| Type            | Syntax         | Description                          |
|:----------------|:---------------|:-------------------------------------|
| **Array**       | `[]`           | Simple anonymous array.              |
| **Named Array** | `name[]`       | Array with a name/tag.               |
| **Param Array** | `(args)[]`     | Array with parameters.               |
| **Full Array**  | `name(args)[]` | Array with both name and parameters. |

**Examples:**
```tson
[ 1, 2, 3 ]                         // Simple Array
points[ 1, 2, 3 ]                   // Named Array
(type: "int")[ 1, 2, 3 ]            // Param Array
matrix(rows: 2, cols: 2)[ 1, 0, 0, 1 ] // Full Array
```

### 6.5 Tuples (`()`)
Tuples (or uplets) are fixed-size ordered collections. Like objects and arrays, they can be anonymous or named. **Elements** within a tuple can be any valid expression.

| Type            | Syntax   | Description                                     |
|:----------------|:---------|:------------------------------------------------|
| **Tuple**       | `()`     | Simple anonymous tuple.                         |
| **Named Tuple** | `name()` | Tuple with a name (similar to a function call). |

**Examples:**
```tson
(1, 2)                              // Simple Tuple
color(255, 128, 0)                  // Named Tuple
point(x: 10, y: 20)                 // Named Tuple with pairs
```

---

## 7. Streams

Streams are used for large or binary data.

### 7.1 Binary Streams
Binary data is enclosed in `^[]`. An optional **encoding** can be specified before the brackets.
- **Default Encoding**: `^[]` is equivalent to `^b64[]` (Base64).
- **Supported Encodings**: Common encodings include `b64` (Base64), `hex` (Hexadecimal), and `b85` (Base85).

```tson
^[YmFzZTY0ZGF0YQ==]      // Default Base64
^b64[YmFzZTY0ZGF0YQ==]   // Explicit Base64
^hex[68656c6c6f]         // Hexadecimal encoding
^b85[He7W%DIdAh]         // Base85 encoding
```

**TSON** separates Lexical Capture from Content Validation. The parser will extract invalid!! as the payload of a Base64 stream. However, because ! is not a valid Base64 character, the `Element`.isErrorTree() check will return true, and the diagnostics() list will contain an 'Invalid Base64 Encoding' warning. This allows the application to decide whether to crash, ignore the field, or attempt to log the raw corrupted data for debugging.

### 7.2 Character Streams
Character streams use a custom delimiter `^id{...^id}`.
```tson
^html{<div>Hello</div>^html}
```

---

## 8. Annotations

Annotations add metadata to elements using the `@` symbol.

```tson
@required
@range(1, 100)
port: 80

@validate(regex="^[a-z]+$")
username: "admin"
```

---

## 9. Lists (Depth-Driven Hierarchy)
Lists in **TSON** encode hierarchy using marker repetition, not indentation.
The number of leading marker characters defines the depth level of the element.
Whitespace, indentation, and line breaks have no structural meaning.
A node at depth `n` is always attached to the most recent node at depth `< n`.
Two semantic kinds of lists exist:
- #Unordered# (Cardinality) — order is not significant
- #Ordered# (Ordinality) — position is significant

Only the marker shape determines the list type. Only the marker count determines the depth.

### 9.1 Marker Characters (Normative)
The following Unicode characters are reserved exclusively for list structure and **cannot appear inside identifiers**.

Unordered markers:
* `•` — U+2022 BULLET
* `●` — U+25CF BLACK CIRCLE

Ordered markers:
- `▪` — U+25AA BLACK SMALL SQUARE
- `■` — U+25A0 BLACK SQUARE

ASCII fallbacks:
- `[.]` — unordered
- `[#]` — ordered

All variants are **lexically equivalent** within their category.

Example equivalence:

```tson
• Item
● Item
[.] Item
```
All represent the same unordered depth-1 node.

--- 

### 9.2 Depth Rule
Depth is determined only by the number of repeated markers.
Examples:

```tson
•   depth 1
••  depth 2
••• depth 3
```

```tson
▪   depth 1
▪▪  depth 2
▪▪▪ depth 3
```

Bracketed ASCII uses repeated internal characters:

```tson
[.]   depth 1
[..]  depth 2
[##]  depth 2
```

### 9.3 Whitespace Neutrality
Whitespace is purely cosmetic. Spaces, tabs, and newlines are treated identically as element separators.
Therefore:

```tson
• Fruit •• Apple •• Banana • Vegetable
```
and

```tson
• Fruit
  •• Apple
  •• Banana
• Vegetable
```
produce identical ASTs. Indentation is ignored.

### 9.4 Marker-First Principle
List markers are lexically distinct tokens and are never valid identifier characters.
Because of this:
```tson
•Fruit
• Fruit
```
are parsed identically.
No separator is required between marker and value.

### 9.5 Cross-Prefix Nesting
Ordered and unordered markers may freely nest.

Depth alone determines parentage.

```tson
▪ Step One
•• Subtask
▪ Step Two
```

`Subtask` is a child of `Step One` despite the marker type change.

### 9.6 Sparse Depth Jumps
Depth may increase by any amount.
A node attaches to the most recent shallower depth.
```tson
• Top
••••• Deep Child       // depth 5 → child of "Top"
•• Sibling             // depth 2 → also child of "Top"
```

Both children belong to Top.


### 9.7 Lexical Disambiguation

Structural markers take lexical priority over arrays.

```tson
[.]   → list marker
[ . ] → array containing "."
```

Spacing breaks the marker token.
This rule ensures deterministic LL(k) parsing.

---

## 10. Comments and Whitespace
**TSON** supports both line-oriented and block-oriented comments. While whitespace is generally ignored as a separator, comments serve to document logic without affecting the evaluation of the expression.

### 10.1 Comments
Comments in **TSON** are categorized into three formats based on their delimiter and intended use:

Comments are non-structural and are attached to AST nodes based on proximity:
- Leading Decoration: By default, a comment is attached to the next subsequent non-whitespace node.
- Trailing Decoration: If no subsequent node exists within the current scope (e.g., at the end of a file or a block), the comment is attached to the previous node as a trailing comment.

#### 10.1.1 Single-Line Comments

Single-line comments begin with the sequence //. Unlike standard implementations, **TSON** treats consecutive single-line comments as a single atomic token.
- Aggregation Rule: The lexer captures all text following // up to the Line Terminator. If the following line (ignoring horizontal whitespace) also begins with //, the lexer continues the capture into the same token.
- Preservation: The internal Line Terminators between contiguous comment lines are preserved within the token value to maintain the user's formatting.
- Termination: The comment block is terminated by the first line that does not begin with the // sequence.

Example of a single atomic token:


```tson
// This entire block is processed
// as a single `ElementTokenType.COMMENT`
// despite spanning three lines.
```
#### 10.1.2 Block Comments
Block comments are enclosed between /* and */.
- Universal Handling: No distinction is made between /* and /**. Both are processed as a single block.
- Content Trimming: If the block comment follows the "Doc-style" convention—where every line starts with a consistent number of spaces followed by one or more asterisks (*)—the lexer trims these decorative characters from the internal string.
- Original Preservation: Despite the trimming logic, the original raw content (including the asterisks) is preserved in a separate "raw" field of the token for round-trip fidelity.

Example of Block Trimming:

```tson
/*
 * This is a comment
 * with decorative stars
 */   
```

- Raw Value: "\n * This is a comment\n * with decorative stars\n"
- Trimmed Value: "\n This is a comment\n with decorative stars\n"

### 10.2 Whitespace
Spaces and newlines are interchangeable outside literals.
```tson
. A .. B . C   // Equivalent to multi-line
```

---

### 11. Lexical Specification: The Tokenization Engine
To ensure absolute consistency across implementations, **TSON** uses a Predefined Lexical Catalog for operators, rather than allowing truly "arbitrary" character sequences.

#### 11.1 The Operator Catalog
**TSON** supports over 500+ specialized operators, covering standard arithmetic, set theory, calculus (integrals), and logic (arrows/quantifiers).
- Lexeme-Based Tokenization: The lexer uses a Greedy Multi-Character Match (Maximum Munch). It compares the character stream against the `NOperatorSymbol` catalog.
- Aliases and Unicode Normalization: Many operators support Unicode aliases (e.g., `*` and `∗` are lexically identical). The parser treats these as the same internal `OPERATOR_SYMBOL`.
- Token Boundaries: Because the operator catalog is predefined, the lexer can unambiguously split `x+y` into `[ID:x], [OP:+], and [ID:y]` without whitespace, because `+` is a known terminal symbol in the catalog.

##### 11.2 Tokenization Priority (The "Longest Match" Rule)
When the lexer encounters sequences of operator-class characters, it always prioritizes the longest string present in the NOperatorSymbol table.



| Input    | Tokenization Result | Reasoning                                       |
|:---------|:--------------------|:------------------------------------------------|
| `+++`    | PLUS3               | Found exact match for `+++`                     |
| `+ + +`  | PLUS, PLUS, PLUS    | Separated by whitespace,treated as three tokens |
| `+==` | PLUS_EQ2           | Found exact match for `+==`                     |


#### 11.3 Identifier vs. Operator Interaction
The `NAME` (Identifier) and `OPERATOR` groups are disjoint. An identifier terminates the moment a character from the NOperatorSymbol catalog or a structural delimiter (like :) is encountered.

Example: `key:value`
- `key` matches `NAME`.
- `:` is a structural separator.
- `value` matches `NAME`.

Example: `price<=100usd`
- `price` matches `NAME`.
- `<=` matches `OPERATOR_SYMBOL` (LTE).
- `100usd` matches `NUMBER` (with suffix).

#### 11.4 Implementation: The Temporal Lexer

To satisfy the requirement for a strict Lexer Specification, the parser identifies Temporal Literals using the following priority:
- Greedy Temporal Match: The lexer looks ahead for the pattern \d{4}-\d{2}-\d{2}.
- Separator Check: If a T or whitespace follows the date, it continues to look for the time component.
- Fallback: If the pattern is broken by an illegal character or unexpected whitespace, the lexer reverts to parsing individual INT and OPERATOR tokens.
Example of Tokenization Priority: 

| Raw Input      | Tokenized As          | Logic                               | 
|:---------------|:----------------------|:------------------------------------| 
| 2026-02-03     | LOCAL_DATE            | Bonded sequence; no spaces.         | 
| 2026 - 02 - 03 | INT, OP, INT, OP, INT | Spaces break the temporal bond.     | 
| 03:15:00Z      | INSTANT               | Recognized as Time with UTC marker. |

---

## 12. Character Encoding & Byte Streams
To ensure universal compatibility across systems, **TSON** defines strict rules for text encoding and binary data integrity.
### 12.1 The UTF-8 Standard
- Primary Encoding: **TSON** is strictly a UTF-8 format. All parsers must support the full Unicode range.
- BOM (Byte Order Mark): **TSON** parsers should detect and ignore the `UTF-8` `BOM` (`EF BB BF`). If present, it is treated as leading whitespace and discarded.
- Invalid Sequences: If a file contains invalid `UTF-8` byte sequences, the parser must treat that segment as an Error Node. The `isErrorTree()` method will return true, and the diagnostic will report "Encoding Violation."

### 12.2 Stream and Binary Validation

**TSON** supports embedded binary data via suffixes (e.g., ^b64).
- Validation Policy: The **TSON** parser is a Structural Parser, not a Data Validator.
  - It will identify the content of `^b64[...]` as a `BINARY_STREAM` element.
  - Lazy Decoding: To maximize performance, the parser may defer the actual Base64 decoding until the application explicitly accesses the value.
- Invalid Encoding Handling:
  - If `^b64[invalid!!]` is encountered, the Lexer will successfully capture the string.
  - The Decoder (upon access) will flag the error.
  - If `isErrorTree()` is called, it will trigger a validation check on encoded streams and return `true` if the content is not a valid Base64/Hex string.



## 13. Fault Tolerance & Security (Fail-Never)

**TSON** is designed with a Resilient Grammar. This means the parser will always attempt to construct a valid Abstract Syntax Tree (AST), even when the input is syntactically malformed.


```tson
. { unclosed object
.. valid child
```
The parser attaches a diagnostic to the unclosed object and continues parsing.
Ambiguous expressions are not errors; they are simply structural sequences.


### 13.1 The "Best-Effort" AST Principle

When **TSON** encounters a structural error (like a missing comma or a mismatched bracket), it does not halt. Instead, it uses Contextual Repair to close the current container and continue.
Example: The "Admin" Risk Input: admin_users `[ "alice", "bob" { "charlie" ]`
In this case, the **TSON** parser detects a conflict (a `{` inside an array without a separator).
- The parser treats `{ "charlie" ]` as a malformed fragment.
- To preserve the structure, it may resolve "charlie" as a string but wrap it in an `EMPTY` or `ERROR` node type, or simply terminate the array.
- The Result: The AST will exist, but it will contain an anomaly.

### 13.2 Security Best Practices: "Strict Mode" vs. "Resilient Mode"
**TSON** guarantees a Tree, not a Truth." Just because **TSON** successfully parsed a file doesn't mean the file is valid for your application. **TSON** moves the "Failure Point" from the Lexer to the Validator
Because **TSON** is "Fail-Never," developers must use a Validator (or a future **TSON** Schema) to check the health of the resulting AST.
- Check for Error Nodes: **TSON** parsers flag malformed segments as `ElementType.EMPTY` or `ElementType.CUSTOM` with error metadata.
- Schema Enforcement: Applications should verify that admin_users is a clean ARRAY containing only STRING types. If the parser had to "guess" due to a typo, the validator should reject the config before it reaches the logic layer.
- every `Element` has a `isErrorTree()` method that recursively checks for the validity of the tree and `List<ElementDiagnostic>` diagnostics() that collect all errors within the tree


In the case of `admin_users [ "alice", "bob" { "charlie" ]`, the **TSON** parser ensures the application doesn't crash. 
However, the resulting `OBJECT` for `admin_users` will contain a structural anomaly. 
A security-conscious implementation should check if the admin_users element is a 'valid' array. 
If it contains unexpected objects or error-fragments, the application should log a Critical Configuration Error and refuse to start.



---

## Appendix: Grammar Sketch

1. Structural Rules
The root of a **TSON** document is a sequence of elements.

```antlr
Document    -> Element*
Element     -> Annotation* (ListContainer | Entry)
Entry       -> Expr (Separator Expr)* Separator   -> ':' | '=' | ' '
```

2. Lists (Hierarchical)
This section now correctly uses the Unicode markers from your NOperatorSymbol logic.

```antlr
ListContainer -> (UnorderedMarker | OrderedMarker) Element
UnorderedMarker -> '•'+ | '●'+ | '[.' '.'* ']'
OrderedMarker   -> '▪'+ | '■'+ | '[#' '#'* ']'
```

3. Expressions & Operators
This is where we address the "Bonding" and "Predefined Operator" critiques.

```antlr
Expr        -> Term (Operator Term)*
Term        -> Literal | Identifier | Container | FlatExpr

// Bonding Rule: A Complex Literal is a Lexer-level Terminal 
// that looks like a math expression but has no whitespace.
Literal     -> Number | ComplexLiteral | String | Boolean | Temporal | Null
ComplexLiteral -> Number ('+'|'-') Number ('i'|'j'|'k') // No whitespace allowed

// Operator comes from the NOperatorSymbol catalog (500+ symbols)
Operator    -> [See NOperatorSymbol Catalog]
```

4. Containers
   **TSON** uses a consistent Header + Body pattern for Objects, Arrays, and Tuples.

```antlr
Container   -> Header? (Body | ArrayBody | TupleBody)
Header      -> Identifier Params?
Params      -> '(' (Element (',' Element)*)? ')'

Body        -> '{' (Element (','? Element)*)? '}'
ArrayBody   -> '[' (Element (','? Element)*)? ']'
TupleBody   -> '(' (Element (','? Element)*)? ')'
```

5. Streams (Binary/Encoded)
Revised to include the unique ID matching requirement.

```antlr
Stream      -> '^' Suffix? '[' Content ']'
| '^' Suffix '{' Content '^' Suffix '}'
Suffix      -> Identifier
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
.. ttl: 300ms
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
> 🧮 Evaluation engine applies precedence later — **TSON** stays neutral.

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

• Introduction
• API Reference
•• GET /users
••• Returns list of users
••• @response(200): [User]
•• POST /users
••• Creates a new user
• Examples
•• Basic Auth
•• OAuth2 Flow
```

> Depth-driven hierarchy mirrors NTF section structure.
> Can be rendered as collapsible TOC in terminal (via NTF).

### 13. REST API Contract

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

**TSON** is natively supported in [Nuts](https://github.com/thevpc/nuts)
— a modular, dependency-free Java platform for CLI tools, package management, and structured I/O.

Nuts provides a full-featured, token-preserving **TSON** parser and writer that implements all **TSON** v2.0 features, including:

- Round-trip safe parsing (comments, whitespace, and errors preserved)
- Depth-driven list hierarchy
- Typed numbers, streams, annotations, and expressions
- Full DOM manipulation via the NElement API

## 🔧 Basic Usage

```java
// Parse a **TSON** file
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
- Invalid Tokens: If a sequence cannot be parsed, it should be captured as a `ElementDiagnostic` and the parser should resume at the next separator (,, ;) or closing brace (}, ], )).
- Partial AST: The resulting tree should contain as much valid data as possible, with error nodes marking the gaps.

