# Nuts Community Project Overview

## Project Structure

This is a Maven-based multi-module Java project organized into several key components:

### Core Modules
- **core/** - Main nuts implementation
  - `nut-boot` (lib): Workspace bootstrapper library, responsible for downloading and creating/running nuts workspaces
  - `nut-api` (lib): Core API for usage as a library/framework; depends on nut-boot
  - `nut-lib` (lib): Full-featured nuts library for usage as a framework
  - `nut-runtime` (lib): Implementation library loaded dynamically by nuts-boot
  - `nut` (app): Minimal dependency nuts application that loads required classes on the fly
  - `nut-full` (app): Fully static application with all dependencies linked

### Supporting Modules
- **libraries/** - Libraries built on top of nuts
- **extensions/** - Nuts extensions
- **test/nuts-runtime-test/** - Runtime tests
- **installers/** - Installation packages

## Key Features

This project appears to be a comprehensive framework for building and managing workspaces, with:
- Modular architecture supporting different usage patterns
- Multiple deployment options (dynamic loading vs static linking)
- Extensible design through plugins/extensions
- Maven-based build system with proper dependency management

## Technical Details

- Java 8 compatible (maven.compiler.source/target = 1.8)
- Uses Maven for build management
- Follows standard Maven directory structure
- Multi-module project structure for clear separation of concerns

## Getting Started

1. Build the project using Maven: `mvn clean install`
2. The core functionality is in the `core` module
3. Libraries and extensions are in their respective directories
4. Tests are in the `test/nuts-runtime-test` module

## Documentation

The project uses standard Maven documentation practices with README.md files in each module directory to explain their purpose and usage.


## File Location & Discovery Rules
When asked to check, read, edit, or document a specific class, interface, or file, do not guess the directory structure, module names, or package paths. You must accurately locate the target first.

Follow this strict discovery workflow:
1. **Locate by Filename First:** Use `folder_find` or `list_files` from the project root using a glob pattern for the filename (e.g., name="*NApp.java*") to discover its exact path.
2. **Loosen Content Search Patterns:** If you cannot find the file by name and must use `folder_grep` to find a definition, search for the identifier name alone (e.g., pattern="NApp") rather than a rigid string like "public class NApp" which fails if the target is an interface, abstract class, or annotated definition.
3. **Verify Before Action:** Only attempt to read (`file_read`, `file_grep`, etc.) or modify a file once you have verified its real path through a discovery tool.
