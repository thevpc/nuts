# 📄 TSON Schema Specification (v2.0)
**Author:** [thevpc](https://github.com/thevpc)  
**Last Updated:** 2026-02-26

`TODO` :: THIS is a draft, not final, not implemented, just brainstormed...

This example illustrates how a TSON schema can be used to describe the structure and constraints of a simple digital twin model. 
It uses schema constructs such as parametric patterns, interleaved children, semantic predicates, and uniqueness constraints.

```tson
// Parametric pattern for optional positive values
@define optionalPositive(type) {
    element(type:$type|string, value:>0)?
}

(
    // Optional numeric or string elements at top level
    element(type:number|string, value:>0)*

    // Safety limits with semantic constraint (high must be > low)
    element(type:nobject, name:"safety_limits") {
        children: {
            element(type:pair) {
                key:   element(type:string, value:"low")
                value: element(type:int, suffix:"pascal")
            } &
            element(type:pair) {
                key:   element(type:string, value:"high")
                value: element(type:int, suffix:"pascal", value:>low)
            }
        }
    }

    // System components with unique names among siblings
    element(type:object|nobject|fobject, name:matches("[a-z]*")) {
        children: {
            (
            element(type:pair, uniqueValue:(scope:parent)) {
                key:   element(type:string, value:"name")
                value: element(type:string)
            } &
            element(type:pair) {
                key:   element(type:string, value:"age")
                value: element(type:int)
            }
            )*
            element(type:pair) {
                key:   element(type:string, value:"label")
                value: element(type:tsqstring)
            }
        }

        // Parameters: numeric or optional positive with a required label
        params: {
            (
                element(type:int|uint, value:[0..30],commentsLength:>0) |
                $optionalPositive(number)
            ){1..5} &
            
        }
    }
){1..5}

```

This schema includes the following features:

- Parametric patterns (`@define optionalPositive(type)`) for reusable parameter definitions.
- Interleaved child elements (`&`) allowing order‑insensitive fields.
- Semantic predicates (`value:>low`) enforcing relational constraints between values.
- Uniqueness constraints (`uniqueValue:(scope:parent)`) to ensure component names are distinct among siblings.
- Repetition and ranges (`{1..5}`, `*`) for multiplicity of elements and parameters.


## Example TSON Document (Digital Twin)

The following is a TSON document that would validate against the schema above:

```tson
// Top-level status indicators
temperature: 23
mode: "auto"

// Safety limits: high must be greater than low, suffix "pascal"
safety_limits{
    high: 250pascal
    low: 100pascal
}

// Motor component with unique name
motor{
    name: "MainMotor"
    age: 5
    rpm: 1500  // commentsLength:>0 satisfied
    voltage: 230  // commentsLength:>0 satisfied
    status: "on"
    label: '''fast'''
    params{
        10  // normal numeric param
        7   // normal numeric param
        12  // normal numeric param
        4   // normal numeric param
        15  // normal numeric param
        8   // optional positive number, exercises $optionalPositive(number)
    }
}

// Pump component with unique name
pump{
    name: "CoolantPump"
    age: 2
    flow: 120  // commentsLength:>0 satisfied
    pressure: 80  // commentsLength:>0 satisfied
    status: "ok"
    label: '''safe'''
    params{
        5
        12
        1
        6
        3
        9  // optional positive number
    }
}

// Valve component with unique name
valve{
    name: "InletValve"
    age: 1
    position: 75  // commentsLength:>0 satisfied
    leak_rate: 2  // commentsLength:>0 satisfied
    status: "open"
    label: '''open'''
    params{
        2
        4
        9
        1
        7
        3  // optional positive number
    }
}


```

In this document:

- Top‑level elements are optional numeric/string values.
- Safety limits are provided with a high and low value that satisfy the semantic constraint.
- Components (e.g., `motor`, `pump`, `valve`) have name and age children and contain parameter values and a short label.
- Each component’s name value is unique among siblings.