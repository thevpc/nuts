# Nuts Integration Libraries

This directory contains reusable Java libraries that integrate Nuts capabilities into popular frameworks and UI toolkits.

## Included Libraries

| Library | Description |
| :--- | :--- |
| **`nuts-spring-boot`** | Spring Boot starter & auto-configurations for running and managing Nuts packages inside Spring Boot applications. |
| **`nuts-slf4j`** | SLF4J binding and logger bridge for Nuts log trace output. |
| **`nuts-swing`** | Swing UI components and helpers for Nuts desktop applications. |
| **`nuts-tomcat-classloader`** | Custom Tomcat ClassLoader integration for dynamically loading Nuts packages in Web Applications. |

---

## Building Libraries

Build all libraries from the repository root:
```bash
mvn clean install -pl libraries
```