/**
 * The {@code net.thevpc.nuts.elem} package provides the core abstraction for
 * structured, typed, and self-describing data elements in Nuts.
 * <p>
 * Elements are the building blocks for representing data in a uniform way,
 * allowing conversions between multiple formats (JSON, XML, YAML, TSON, etc.)
 * and supporting annotations, comments, and metadata.
 * </p>
 *
 * <p>
 * Key features:
 * <ul>
 *     <li><b>Typed elements:</b> NElement and subtypes represent numbers, strings,
 *         arrays, maps, matrices, binary streams, characters, and user-defined types.</li>
 *     <li><b>Builders:</b> Fluent builders for creating and transforming elements
 *         (e.g., NArrayElementBuilder, NObjectElementBuilder, NPrimitiveElementBuilder).</li>
 *     <li><b>Comments and metadata:</b> Support for element comments,
 *         annotations, and descriptive metadata for documentation or processing hints.</li>
 *     <li><b>Mapping and conversion:</b> Mapper infrastructure to convert
 *         elements to/from Java objects, custom types, or other element structures.</li>
 *     <li><b>Navigation and paths:</b> Utilities for selecting, filtering,
 *         and transforming nested elements (e.g., NElementPath, NNameSelectorStrategy).</li>
 *     <li><b>Extensibility:</b> Support for custom element types, operators,
 *         and transformation rules.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The package underpins many higher-level Nuts frameworks such as:
 * <ul>
 *     <li>Configuration and option parsing</li>
 *     <li>Data persistence and serialization</li>
 *     <li>Presentation layers and templates</li>
 *     <li>Computation and expression evaluation</li>
 * </ul>
 * </p>
 *
 * <p>
 * This package is designed for extensibility and interoperability, making it
 * suitable as a universal data representation for both runtime and persisted state.
 * </p>
 *
 * @since 0.8.0
 */
package net.thevpc.nuts.elem;
