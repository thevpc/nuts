/**
 * Command-line parsing, matching, completion, formatting, and history contracts.
 *
 * <p>{@code NCmdLine} represents parsed command-line state. Arguments and their
 * values are described by {@code NArg} and {@code NArgValue}; completion resolvers
 * provide candidates and positions; and processors and matchers dispatch the result.
 * This package is used by command implementations but is also suitable for hosted
 * command-line applications.</p>
 */
package net.thevpc.nuts.cmdline;
