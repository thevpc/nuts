# 📚 Nuts Documentation & Public Website Hub

Welcome to the **Nuts Documentation Hub**. This directory contains the complete source materials for the **Nuts** ecosystem's public documentation, technical specifications, framework tutorials, slide presentations, brand assets, syntax highlighting packages, and static website generators.

---

## 🌐 Public Resources & Links

* **Official Website**: [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)
* **Nuts Core Documentation**: [https://thevpc.github.io/nuts/doc-nuts.html](https://thevpc.github.io/nuts/doc-nuts.html)
* **NAF (Nuts Application Framework) Documentation**: [https://thevpc.github.io/nuts/doc-naf.html](https://thevpc.github.io/nuts/doc-naf.html)
* **Application Catalog**: [https://thevpc.github.io/nuts/apps.html](https://thevpc.github.io/nuts/apps.html)
* **Downloads & Releases**: [https://thevpc.github.io/nuts/download.html](https://thevpc.github.io/nuts/download.html)
* **FAQ**: [https://thevpc.github.io/nuts/faq.html](https://thevpc.github.io/nuts/faq.html)
* **Blog & Release Notes**: [https://thevpc.github.io/nuts/blog.html](https://thevpc.github.io/nuts/blog.html)

---

## 📁 Directory Structure Overview

```text
documentation/
├── integration/         # Syntax highlighting packages & install rules for popular editors & IDEs
│   ├── ntf-support/     # Nuts Text Format (NTF) syntax support (VSCode, IntelliJ, Vim, etc.)
│   └── tson-support/    # Type Safe Object Notation (TSON) syntax support
├── media/               # Brand identity assets: vector logos, icons, fonts, and design history
├── presentations/       # Technical slide decks (AsciiDoc, PDF, NTexUp terminal presentations)
├── repo/                # Repository root template files and dev scripts processed during release
├── specifications/      # Formal language and format specifications (NTF, TSON, TSON-Schema)
├── term-cast/           # Terminal session recording scripts (VHS) and animated demos (WebP)
├── tutorials/           # Comprehensive NAF developer tutorials and practical CLI showcases
└── website/             # Source code, templates, scripts, and assets for the static documentation website
```

---

## 📂 Subdirectories in Detail

### 1. `integration/` — Editor & IDE Syntax Highlighting
Contains grammar definitions, syntax highlighters, and auto-install configuration files (`sys-editor-support.tson`) for:
* **NTF (Nuts Text Format)**: Rich console formatting, semantic tags, and ANSI styling.
* **TSON (Type Safe Object Notation)**: Strongly-typed structured configuration and DSL format.

#### Supported Editors & IDEs:
* **VS Code** (`vscode/`): Language configurations and TextMate grammar (`language.tmLanguage.json`).
* **IntelliJ IDEA** (`intellij/`): XML custom language definitions.
* **Vim** (`vim/`): Syntax and file type detection scripts (`language.syntax.vim`, `language.ftdetect.vim`).
* **Kate** (`kate/`): KSyntaxHighlighting XML definitions.
* **Gedit** (`gedit/`): GtkSourceView `.lang` definitions.
* **JEdit** (`jedit/`): Mode catalog definitions.
* **Notepad++** (`notepad-plus-plus/`): User-defined language XML files.

#### Installation Commands:
Syntax highlighting can be installed automatically via Nuts CLI:
```bash
# Install NTF syntax highlighting for an editor
nuts settings install-ntf-editor-syntax <vscode|intellij|vim|kate|gedit|jedit|notepad-plus-plus>

# Install TSON syntax highlighting for an editor
nuts settings install-tson-editor-syntax <vscode|intellij|vim|kate|gedit|jedit|notepad-plus-plus>
```

---

### 2. `media/` — Brand Identity & Graphic Assets
Contains official brand artwork and vector graphics:
* **Logos**: `nuts-logo.svg`, `nuts-logo.png`, high-res `nuts-logo-1280.png`, compact `nuts-logo-200.png`.
* **Icons**: `nuts-icon.svg`, `nuts-icon.png`, Windows icon `nuts-icon.ico`, macOS icon `nuts-icon.icns`.
* **Typography**: `fonts/ZeroesTwo.ttf` — the official font used in the Nuts identity.
* **Archive**: `old/` contains previous iterations, 2025/2026 drafts, and asset zip archives.

---

### 3. `presentations/` — Slide Decks & Presentations
Contains architectural and conceptual slide decks in multiple formats:
* **`001-Rationale/`** (`nuts-presentation-001-rationale.adoc` / `.pdf`):
  * Explains the core motivations behind Nuts: runtime dependency management, classpath generation, eliminating the need for fat JARs, and automatic transparent JDK provisioning.
* **`002-Components/`** (`nuts-presentation-002-components.adoc` / `.pdf`):
  * Architectural breakdown: Nuts core, workspaces, repositories, package lifecycle, and runtime sandboxing. Includes GraphML architecture diagrams.
* **`003-API/`** (`nuts-presentation-003-API.adoc` / `.pdf`):
  * Nuts Application Framework (NAF) API, workspace/session context model, execution flowcharts, and command-line lifecycle.
* **`004-presentation/`** (`main.ntx`, `pages/`):
  * Interactive terminal-based presentation powered by `ntexup` (`nuts-dev ntexup show`).
* **`shared/asciidoctor/`**: Shared theme definitions (`resources/themes`) and font packages (`resources/fonts`).
* **`deprecated/`**: Legacy OpenDocument presentation (`2021-06-25-nuts.odp`).

#### Building Presentations:
Presentations can be compiled to PDF using Asciidoctor PDF:
```bash
# Build all PDF presentations
cd documentation/presentations && ./make-all

# Or build individual presentation decks
cd documentation/presentations/001-Rationale && ./make-this
```
> *Prerequisites*: `asciidoctor-pdf`, `asciidoctor-diagram`, and a running PlantUML server (e.g. `docker run -d --name plantuml -p 8081:8080 plantuml/plantuml-server:jetty`).

---

### 4. `repo/` — Repository Templates & Dynamic Sync
Contains dynamic templates used by `nuts-release-tool` to maintain consistent versioning across repository files:
* **`src/main/README.md`**: Template for the root `$NUTS_REPO_ROOT/README.md` using NExpr dynamic version tokens (e.g., `{{runtimeVersion}}`, `{{stableRuntimeVersion}}`).
* **`src/main/CONTRIBUTING.md`**: Template for root contribution guidelines.
* **`src/main/scripts/`**: Development shell helpers (`nuts-dev`, `nsh-dev`, `nuts-example-debug`).

When `nuts-release-tool` runs, these templates are evaluated and written to the repository root.

---

### 5. `specifications/` — Formal Specifications
Contains formal specifications for Nuts data formats and markup languages:
* **[`ntf.md`](specifications/ntf.md)** — **Nuts Text Format (NTF v1.0)**:
  * Markdown-like format for rich, portable terminal formatting and console logging.
  * Hierarchical titles (`#) Title`), inline styles (`##:bold: text##`, `##:underlined: text##`), 4-bit/8-bit/24-bit RGB colors, semantic tags (`success`, `warn`, `error`, `keyword`, `option`), verbatim blocks, code syntax blocks, and `!include` directives.
* **[`tson.md`](specifications/tson.md)** — **Typed Structured Object Notation (TSON v2.0)**:
  * Strongly-typed, human-centric configuration and DSL format.
  * Native literal intelligence (dates, timestamps, arbitrary-precision numbers, unit suffixes `u16`, `ms`, `%`), literal-first string semantics (death of backslash escaping), multi-mode quotes, documentary paragraphs (`¶`), and exhaustive symbol catalog.
* **[`tson-schema.md`](specifications/tson-schema.md)** — **TSON Schema**:
  * Structural definition and validation rules for TSON documents.

---

### 6. `term-cast/` — Terminal Casts & Demos
Contains terminal recording scripts and high-definition animated demonstrations:
* **`nuts-install-demo.tape`**: Terminal recording script for [VHS](https://github.com/charmbracelet/vhs).
* **`nuts-install-demo.webp`**: The generated animated demo displayed on the project README and landing page.
* **`build-nuts-install-demo`**: Shell script automating clean environment setup, running `vhs`, and converting output to animated WebP via `ffmpeg`.

#### Re-recording the Terminal Demo:
```bash
cd documentation/term-cast && ./build-nuts-install-demo
```
> *Prerequisites*: `vhs` and `ffmpeg`.

---

### 7. `tutorials/` — Developer Tutorials & Showcases
Contains comprehensive guides for users and developers:
* **[`FWK-TUTORIAL.md`](tutorials/FWK-TUTORIAL.md)** — **Building NutsAdminCLI (10-Module NAF Tutorial)**:
  * **Module 1**: Standalone Bootstrapping & Lifecycle (`NWorkspace`, `NSession`, `@App`, `@NAppRun`, `@NAppInstall`).
  * **Module 2**: Command-Line Parsing (`NCmdLine`, `NArg`, non-destructive peeking/consuming, auto-complete).
  * **Module 3**: Structured Messaging & Terminal Styling (`NMsg`, `NOut`, `NErr`, `NLog`, `NLogIntent`, NTF markup).
  * **Module 4**: Filesystem Abstraction & Integrity (`NPath`, `NCp`, `NDigest`, XDG directories, HTTP/remote streams).
  * **Module 5**: Structured Data Processing (`NElement`, `NElementReader`, `NElementWriter`, JSON/XML/TSON).
  * **Module 6**: Concurrency & Asynchronous Tasks (`NThreadPool`, `NPromise`, progress monitors).
  * **Module 7**: Cross-Platform Process Execution (`NExec`, `NProcessExec`, stream redirection).
  * **Module 8**: Dynamic Expression Evaluation (`NExpr`, custom functions, variable scopes).
  * **Module 9**: Dynamic Loading & Plugin Architecture (`NExtensions`, service discovery).
  * **Module 10**: Spring Boot Bridge & Logging (`nuts-spring`, bridging native NAF logging to SLF4J).
* **[`SHOWCASE.md`](tutorials/SHOWCASE.md)** — **Nuts CLI Showcase**:
  * Practical, zero-configuration copy-paste commands to run IDEs (NetBeans, JEdit), Web & Application Servers (Tomcat, Jetty running WAR files), portable local database instances without `sudo` (PostgreSQL, Derby, H2, HSQLDB), and utilities.

---

### 8. `website/` — Documentation Website Sources
The source directory for generating the static website hosted on GitHub Pages:
* **`src/main/`**: Top-level web pages processed by `nsite`:
  * `index.html` (Landing page)
  * `doc-nuts.html` (Core Nuts user & CLI manual)
  * `doc-naf.html` (NAF developer manual)
  * `apps.html` (Nuts application catalog)
  * `download.html` (Installation & binaries page)
  * `blog.html` & `faq.html` (Announcements & frequently asked questions)
  * `contrib.html` (Contributor guide)
  * `compat_reports/` (API & binary compatibility reports)
  * `versions/` (Release version manifests)
* **`src/include/`**: Reusable HTML fragments and card templates (`apps/`, `blog/`, `contrib/`, `doc-naf/`, `doc-nuts/`, `download/`, `faq/`, `template/`).
* **`src/resources/`**: Static assets copied verbatim to the generated site:
  * Stylesheets & SASS (`assets/css`, `assets/sass`)
  * Vendor libraries (`bootstrap`, `font-awesome`, `highlight.js`, `jquery`, `magnific-popup`)
  * Downloadable runtime JARs (`nuts-latest.jar`, `nuts-standard.jar`, versioned JARs)
  * Theme examples (`example.ntf-theme`, `horizon.ntf-theme`, `min.ntf-theme`)
* **`src/script/project.nexpr`**: NExpr configuration script defining version numbers (`latestApiVersion`, `stableRuntimeVersion`, etc.), download URLs, and metadata variables.
* **`archive/`**: Archived historical versions (e.g. `v2026/`).
* **`other-src/`**: AsciiDoc source documentation (`nuts-documentation.adoc`) and extra graphics.

---

## 🛠️ Build & Release Engine (`nuts-release-tool`)

The documentation website, repository root metadata, fat JARs, native executables, repository indexes, and API compatibility reports are orchestrated by `nuts-release-tool` (`net.thevpc.nuts.build`).

### ⚙️ Build Pipeline & Runner Architecture

When `nuts-release-tool` runs, it executes a sequential chain of specialized runners:

```mermaid
flowchart LR
    A["BaseConfRunner<br/>(Load TSON & Vars)"] --> B["JarsRunner<br/>(Publish Maven)"]
    B --> C["ReposRunner<br/>(Update Indexes)"]
    C --> D["InstallerRunner<br/>(Fat JARs & Native)"]
    D --> E["CompatRunner<br/>(JAPI Checker Matrix)"]
    E --> F["SiteRunner<br/>(nsite Docs & Repo)"]
```

1. **`BaseConfRunner`**:
   - Resolves `nuts-release-tool.tson` (and merges optional local overrides from `nuts-release-tool.local.tson`).
   - Initializes `NutsBuildRunnerContext`, path mappings, and environment variables (`vars`).
2. **`JarsRunner`**:
   - Resolves LTS and latest version numbers.
   - When `publish` is enabled, publishes Maven artifacts (`net.thevpc:nuts`) to the remote repository.
3. **`ReposRunner`**:
   - Updates repository metadata and package indexes for `nuts-preview` and `nuts-public`.
4. **`InstallerRunner`**:
   - Builds portable fat JARs, GraalVM native binaries, OS packages via `jpackage`, and standalone bundles with embedded JRE 8.
5. **`CompatRunner`**:
   - Runs `japi-compliance-checker` to compare API changes across release versions and builds the interactive compatibility matrix.
6. **`SiteRunner`**:
   - Uses `net.thevpc.nuts:nsite` to compile website templates into `$NUTS_REPO_ROOT/docs/` and interpolate root repository files (`README.md`, `CONTRIBUTING.md`).

---

### 🚩 Configuration Flags Reference (`nuts-release-tool.tson`)

The release configuration file (`nuts-release-tool.tson` / `nuts-release-tool.local.tson`) controls the build process through the following flags:

| Flag | Type | Description |
| :--- | :--- | :--- |
| **`build-jars`** | `boolean` | **Build Portable Fat JARs (`PackageType.PORTABLE`)**.<br>When `true`, packages self-contained JARs for `nuts-installer` (`net.thevpc.nuts.installer.NutsInstaller`) and `nuts-app-full` (`net.thevpc.nuts.NutsApp`), copying them into `installers/dist/<version>/` along with SHA-256 checksums. |
| **`build-native`** | `boolean` | **Build Native Platform Executables & Bundles**.<br>When `true`, generates three distribution formats:<br>• `PackageType.BIN`: Ahead-of-Time (AOT) compiled native executables using GraalVM `native-image` (with automated reflection config profiling).<br>• `PackageType.NATIVE`: OS-native platform installers generated via `jpackage`.<br>• `PackageType.JRE_BUNDLE`: Standalone archive bundles embedding a dedicated JRE 8 for Linux (x64/x32), Windows (x64/x32), and macOS (x64). |
| **`build-site`** | `boolean` | **Build Documentation Website & Sync Repository**.<br>When `true`, runs `nsite` to evaluate `project.nexpr`, compile pages (`website/src/main/*.html`), embed partials (`website/src/include/`), copy static assets verbatim (`website/src/resources/`), emit static files to `$NUTS_REPO_ROOT/docs/`, and sync interpolated files to repository root (`repo/src/main/`). |
| **`build-repos`** | `boolean` | **Update Repository Statistics & Catalogs**.<br>When `true`, executes `nuts settings update stats` on both `../nuts-repos/nuts-preview` and `../nuts-repos/nuts-public` to refresh artifact catalog indexes. |
| **`build-repo-nuts-preview`** | `boolean` | Rebuilds catalog indexes specifically for `../nuts-repos/nuts-preview`. |
| **`build-repo-nuts-public`** | `boolean` | Rebuilds catalog indexes specifically for `../nuts-repos/nuts-public`. |
| **`build-compat`** | `boolean` | **Generate API Backward Compatibility Matrix**.<br>When `true`, invokes `japi-compliance-checker` on pairwise combinations of versions in `all-versions` (e.g. `0.8.0` through `1.0.0`), generates HTML change reports under `website/src/main/compat_reports/`, and builds the matrix table in `120-versions.html.md`. |
| **`publish`** | `boolean` | **Push / Deploy Artifacts to Production Server**.<br>When `true`, securely uploads all compiled JARs, native packages, checksums, Maven artifacts, and server scripts to the production server (`thevpc.net`) via SSH/`rsync`. |
| **`all-versions`** | `list` / `string` | List of version identifiers to include in API compatibility checks (e.g. `["0.8.0", ..., "0.8.9", "1.0.0"]`). |
| **`stable-api-version`** | `string` | Version identifier for the LTS API (e.g. `"0.8.9"`). |
| **`stable-app-version`** | `string` | Version identifier for the LTS App (e.g. `"0.8.9"`). |
| **`stable-runtime-version`**| `string` | Version identifier for the LTS Runtime (e.g. `"0.8.9.0"`). |
| **`trace`** | `boolean` | Enables trace-level logging (`NSession.of().trace(true)`). |
| **`verbose`** | `boolean` | Enables verbose logging output (`Level.FINEST`). |
| **`debug`** | `boolean` | Appends JVM remote socket debugging flags (`-agentlib:jdwp=...`) to sub-commands. |
| **`vars`** | `object` | Environment paths and deployment settings: JDK homes (`JAVA8_HOME`, `JAVA17_HOME`), GraalVM directory (`NUTS_GRAALVM_DIR`), JRE 8 archive paths (`INSTALLER_JRE8_*`), and remote deployment targets (`REMOTE_NUTS_THEVPC_DEPLOY_*`). |

---

### 🚀 Running the Build

From the root of the repository:
```bash
# Run the release tool script directly
./nuts-release-tool

# Or run via nuts with verbose logging and stacktraces
nuts -ZySb --stacktrace --color=formatted nuts-release-tool
```

---

## 📊 Summary Quick Reference

| Directory | Content Type | Key Formats / Technologies | Build / Generator Tool | Primary Output |
| :--- | :--- | :--- | :--- | :--- |
| **`integration/`** | Syntax Highlighters | JSON, XML, VimScript, Lang | `nuts settings install-*-editor-syntax` | Editor configuration files |
| **`media/`** | Brand Identity | SVG, PNG, ICO, ICNS, TTF | N/A (Source assets) | Web and desktop icons/logos |
| **`presentations/`**| Technical Presentations | AsciiDoc, PDF, NTexUp | `asciidoctor-pdf`, `ntexup` | PDF files & interactive TUI slides |
| **`repo/`** | Root Templates | Markdown, Bash, NExpr | `nuts-release-tool` | `$NUTS_REPO_ROOT/README.md`, etc. |
| **`specifications/`**| Specifications | Markdown | N/A (Standard specs) | Reference documents (NTF, TSON) |
| **`term-cast/`** | Motion Demos | VHS Tape, WebP, GIF | `vhs`, `ffmpeg` | `nuts-install-demo.webp` |
| **`tutorials/`** | Guides & Showcases | Markdown | N/A (Developer docs) | `FWK-TUTORIAL.md`, `SHOWCASE.md` |
| **`website/`** | Public Documentation | HTML, SCSS, JS, NExpr | `net.thevpc.nuts:nsite` | `$NUTS_REPO_ROOT/docs/` (GitHub Pages) |


