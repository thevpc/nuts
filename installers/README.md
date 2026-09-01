# Nuts Installers & Tooling

This directory contains installer applications and release management tools.

## Included Modules

| Module | Description |
| :--- | :--- |
| **`nuts-installer`** | Desktop GUI installer (Swing) for installing Nuts and configuring desktop launchers. |
| **`nuts-release-tool`** | Build and release automation tool. Pre-processes markdown templates via `nsite`, compiles version artifacts, generates the GitHub Pages website in `docs/`, and updates root `README.md` and `CONTRIBUTING.md`. |

---

## Running `nuts-release-tool`

⚠️ Must be executed directly from the **Nuts repository root**:

```bash
# Execute at root directory:
./nuts-release-tool
```

Configured via `nuts-release-tool.tson` at root.