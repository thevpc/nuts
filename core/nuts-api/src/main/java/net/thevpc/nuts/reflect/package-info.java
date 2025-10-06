/**
 * The {@code net.thevpc.nuts.reflect} package provides reflection and introspection
 * utilities for the Nuts ecosystem.
 * <p>
 * It allows examining, accessing, and manipulating classes, methods, properties,
 * and parameters in a structured and extensible way.
 * </p>
 *
 * <p>
 * Key features:
 * <ul>
 *     <li><b>Type inspection:</b> NReflect and NReflectType provide detailed
 *         information about classes, interfaces, generics, and annotations.</li>
 *     <li><b>Method and constructor metadata:</b> NReflectMethod, NSignature,
 *         and NReflectParameter support full introspection of callable members.</li>
 *     <li><b>Property access:</b> NReflectProperty, NReflectPropertyAccessStrategy,
 *         and NReflectPropertyDefaultValueStrategy simplify reading/writing
 *         object properties in a uniform way.</li>
 *     <li><b>Bean referencing:</b> NBeanContainer and NBeanRef allow referencing and
 *         serializing existing beans by ID and variant, supporting runtime resolution
 *         without depending on a specific DI framework.</li>
 *     <li><b>Mapping support:</b> NReflectMapper and NReflectTypeMapper allow
 *         converting between object types or representations.</li>
 *     <li><b>Configuration:</b> NReflectConfiguration and NReflectConfigurationBuilder
 *         allow customizing reflection behavior globally.</li>
 *     <li><b>Repository support:</b> NReflectRepository enables managing and
 *         caching type metadata for performance.</li>
 *     <li><b>Utility helpers:</b> NReflectUtils provides helper methods for common
 *         reflection operations.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Generic object serialization and deserialization.</li>
 *     <li>Dynamic method invocation or property manipulation.</li>
 *     <li>Building frameworks that rely on type introspection (DI containers, mappers, etc.).</li>
 *     <li>Runtime analysis of class structure, annotations, and metadata.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The package is designed to be lightweight, extensible, and fully integrated with
 * the Nuts runtime environment, ensuring consistent reflection handling across modules.
 * </p>
 *
 * @since 0.8.5
 */
package net.thevpc.nuts.reflect;
