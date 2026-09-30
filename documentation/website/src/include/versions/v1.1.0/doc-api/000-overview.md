---
id: api-overview
title: Overview
order: 0
---

# Nuts Core API (Javadoc)

Comprehensive API reference for the Nuts Core API (nuts-api).

## Packages Summary

| Package | Description |
| :--- | :--- |
| [net.thevpc.nuts](#pkg-net-thevpc-nuts) | Public API for Nuts, a runtime and package-management platform for discovering, resolving, installing, and executing software artifacts. |
| [net.thevpc.nuts.app](#pkg-net-thevpc-nuts-app) | Application lifecycle contracts for applications hosted by a Nuts session. |
| [net.thevpc.nuts.artifact](#pkg-net-thevpc-nuts-artifact) | Artifact identity, versioning, descriptors, dependencies, and selection. |
| [net.thevpc.nuts.boot](#pkg-net-thevpc-nuts-boot) | Nuts : Network Updatable Things Service (universal package manager) is a new Open Source Package Manager to help install packages and libraries for... |
| [net.thevpc.nuts.boot.core](#pkg-net-thevpc-nuts-boot-core) | Types and interfaces for `net.thevpc.nuts.boot.core` |
| [net.thevpc.nuts.boot.internal](#pkg-net-thevpc-nuts-boot-internal) | Types and interfaces for `net.thevpc.nuts.boot.internal` |
| [net.thevpc.nuts.boot.internal.cmdline](#pkg-net-thevpc-nuts-boot-internal-cmdline) | Types and interfaces for `net.thevpc.nuts.boot.internal.cmdline` |
| [net.thevpc.nuts.boot.internal.compat](#pkg-net-thevpc-nuts-boot-internal-compat) | Types and interfaces for `net.thevpc.nuts.boot.internal.compat` |
| [net.thevpc.nuts.boot.internal.maven](#pkg-net-thevpc-nuts-boot-internal-maven) | Types and interfaces for `net.thevpc.nuts.boot.internal.maven` |
| [net.thevpc.nuts.boot.internal.util](#pkg-net-thevpc-nuts-boot-internal-util) | Types and interfaces for `net.thevpc.nuts.boot.internal.util` |
| [net.thevpc.nuts.cmdline](#pkg-net-thevpc-nuts-cmdline) | Command-line parsing, matching, completion, formatting, and history contracts. |
| [net.thevpc.nuts.collections](#pkg-net-thevpc-nuts-collections) | Collection abstractions and utilities not provided directly by the JDK. |
| [net.thevpc.nuts.command](#pkg-net-thevpc-nuts-command) | High-level workspace commands and their execution contracts. |
| [net.thevpc.nuts.concurrent](#pkg-net-thevpc-nuts-concurrent) | Resilience, coordination, and execution utilities. |
| [net.thevpc.nuts.core](#pkg-net-thevpc-nuts-core) | Nuts : Network Updatable Things Service (universal package manager) is a new Open Source Package Manager to help install packages and libraries for... |
| [net.thevpc.nuts.elem](#pkg-net-thevpc-nuts-elem) | Structured, typed data elements and their serialization infrastructure. |
| [net.thevpc.nuts.expr](#pkg-net-thevpc-nuts-expr) | Expression syntax trees, evaluation contexts, operators, and templates. |
| [net.thevpc.nuts.ext](#pkg-net-thevpc-nuts-ext) | Extension discovery, registration, and lifecycle contracts. |
| [net.thevpc.nuts.internal](#pkg-net-thevpc-nuts-internal) | Internal Nuts implementation support; not a public extension or application API. |
| [net.thevpc.nuts.internal.artifact](#pkg-net-thevpc-nuts-internal-artifact) | Internal implementations of artifact identity and version value objects. |
| [net.thevpc.nuts.internal.optional](#pkg-net-thevpc-nuts-internal-optional) | Internal implementations of net.thevpc.nuts.util.NOptional states. |
| [net.thevpc.nuts.internal.rpi](#pkg-net-thevpc-nuts-internal-rpi) | Reserved Programming Interfaces (RPIs) used to wire public API facilities to the default Nuts runtime. |
| [net.thevpc.nuts.internal.util](#pkg-net-thevpc-nuts-internal-util) | Internal utility adapters and described functional-object implementations. |
| [net.thevpc.nuts.io](#pkg-net-thevpc-nuts-io) | Input/output, paths, content metadata, compression, and terminal abstractions. |
| [net.thevpc.nuts.log](#pkg-net-thevpc-nuts-log) | Logging contracts, records, configuration, scopes, and message intent. |
| [net.thevpc.nuts.math](#pkg-net-thevpc-nuts-math) | Numeric value types, ranges, formatting, and mathematical function contracts. |
| [net.thevpc.nuts.mon](#pkg-net-thevpc-nuts-mon) | Monitoring primitives for elapsed time, memory use, and task progress. |
| [net.thevpc.nuts.net](#pkg-net-thevpc-nuts-net) | Networking and HTTP request/response abstractions. |
| [net.thevpc.nuts.pipeline](#pkg-net-thevpc-nuts-pipeline) | Lazy iteration and stream-like pipeline abstractions. |
| [net.thevpc.nuts.platform](#pkg-net-thevpc-nuts-platform) | Host-platform detection and runtime-environment models. |
| [net.thevpc.nuts.reflect](#pkg-net-thevpc-nuts-reflect) | Reflection, type descriptions, member/property access, and object mapping. |
| [net.thevpc.nuts.security](#pkg-net-thevpc-nuts-security) | Authentication, credentials, repository access, users, and secret handling. |
| [net.thevpc.nuts.spi](#pkg-net-thevpc-nuts-spi) | Supported Service Provider Interfaces for extending a Nuts runtime. |
| [net.thevpc.nuts.spi.base](#pkg-net-thevpc-nuts-spi-base) | Reusable base classes and delegates for implementing Nuts SPIs. |
| [net.thevpc.nuts.text](#pkg-net-thevpc-nuts-text) | Structured text, messages, formatting, rendering, and object writers. |
| [net.thevpc.nuts.time](#pkg-net-thevpc-nuts-time) | Clock, duration, and elapsed-time utilities. |
| [net.thevpc.nuts.util](#pkg-net-thevpc-nuts-util) | General-purpose Nuts value types, functional contracts, validation, and utilities. |

