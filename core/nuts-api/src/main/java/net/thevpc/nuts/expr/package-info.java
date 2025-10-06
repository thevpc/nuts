/**
 * The {@code net.thevpc.nuts.expr} package provides a complete framework for
 * defining, parsing, compiling, and evaluating expressions within Nuts.
 * <p>
 * Expressions are abstract syntax trees (ASTs) composed of nodes, operators,
 * functions, variables, and literals, allowing runtime evaluation and template
 * processing.
 * </p>
 *
 * <p>
 * Key features:
 * <ul>
 *     <li><b>Expression nodes:</b> Represent the building blocks of expressions,
 *         including literals, variables, operators, function calls, conditional nodes,
 *         and interpolated strings.</li>
 *     <li><b>Evaluation:</b> NExprEvaluator and related classes provide mechanisms
 *         for evaluating expression trees against variable bindings and runtime context.</li>
 *     <li><b>Operator system:</b> Supports defining custom operators, their precedence,
 *         associativity, and evaluation rules.</li>
 *     <li><b>Functions and templates:</b> Support for declaring functions,
 *         constructs, and template-based expressions that can be compiled and reused.</li>
 *     <li><b>Extensibility:</b> Users can define new operators, functions, or AST node types,
 *         allowing integration with custom data types and domain-specific logic.</li>
 *     <li><b>Error handling:</b> NExpressionException and related mechanisms provide
 *         detailed feedback when evaluation or parsing fails.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Evaluating dynamic formulas and conditions at runtime.</li>
 *     <li>Implementing domain-specific languages or scripting within Nuts applications.</li>
 *     <li>Template evaluation and variable interpolation in configurations and outputs.</li>
 *     <li>Custom operators and functions for application-specific logic.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This package is tightly integrated with other Nuts frameworks, especially
 * the {@link net.thevpc.nuts.elem} package for representing structured data
 * and supporting expression evaluation on elements and their properties.
 * </p>
 *
 * @since 0.8.5
 */
package net.thevpc.nuts.expr;
