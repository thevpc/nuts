---
title: Security Model
---

# Security Model

**nuts** incorporates a robust security framework designed to safely execute third-party applications, manage credentials, and control access to environments in both single-user systems and shared enterprise architectures.

## Default Mode (Unsecured)

Out of the box, **nuts** operates in an unsecured mode. It runs with the privileges of the underlying OS user and does not enforce internal authentication. This provides a frictionless developer experience, structurally identical to running standard package managers like `pip`, `npm`, or `brew`. 

In this mode, any local user with filesystem access to the workspace can install packages, modify settings, or execute applications.

## Enabling Secure Mode

For production environments, CI/CD agents, or multi-tenant systems, you can activate the internal security manager:

```bash
nuts settings secure true
```

Once secure mode is enabled, **nuts** enforces strict internal access controls. The system transitions to require:
- **Authentication**: Users must log in via a local credential database or external provider.
- **Authorization**: Actions require specific, granular permissions.
- **Auditability**: Critical actions are logged for compliance.

## Permission Model

The authorization system is built on granular permissions. When secure mode is active, executing a command or altering configuration requires the active session to hold the relevant permission node.

| Permission | Description |
|---|---|
| `admin` | Grants full administrative access to all workspace operations. |
| `install` | Allows the user to install new artifacts into the workspace. |
| `uninstall` | Allows the user to remove installed artifacts. |
| `deploy` | Allows pushing artifacts to local or remote repositories. |
| `update` | Permits updating existing packages to newer versions. |
| `settings` | Grants access to modify workspace configuration (repos, security, paths). |
| `execute` | Allows running installed applications. |

## User Management

**nuts** maintains an internal user directory within the workspace to handle authentication and permission mapping.

```bash
# Create a new user
nuts settings add user developer --password=secret

# Grant install and execute permissions to the user
nuts settings add permission developer install
nuts settings add permission developer execute

# Remove a user
nuts settings remove user developer
```

## Workspaces as Security Boundaries

It is critical to understand that **security settings are scoped to the workspace**. A user with `admin` privileges in `workspace-dev` has zero implied privileges in `workspace-prod`. 

This strict boundary allows operators to create high-security, locked-down workspaces for production workloads while simultaneously allowing open, unauthenticated workspaces for local development on the same host machine.

## Audit Logging

When operating in secure environments, visibility is paramount. **nuts** maintains comprehensive audit logs for lifecycle events.

Audit logs track:
- **Who**: The authenticated identity making the request.
- **What**: The command and arguments executed.
- **When**: High-precision timestamp.
- **Result**: Success, failure, or security denial.

These logs are safely stored in the workspace's designated `log` location and are formatted in parseable JSON or plain text for easy ingestion by centralized logging systems like ELK or Splunk.

## System-Wide vs Per-User Isolation

Security is also enforced by the underlying OS file system.
- **Per-User (Default)**: Workspaces are located in the user's home directory (e.g., `~/.config/nuts`). The OS inherently prevents other local users from modifying or accessing these files.
- **System-Wide (`--global` | `-g`)**: When **nuts** is executed with the `-g` flag (usually requiring OS-level `sudo` privileges), the workspace is initialized in protected system directories (`/etc`, `/var/lib`). The OS prevents non-root users from modifying the binaries, while **nuts** internal security governs execution rights.

## Best Practices for Production

1. **Always use Secure Mode** in shared environments or production servers.
2. **Apply the Principle of Least Privilege**: Create a specific user account with only `execute` permissions for running daemonized applications.
3. **Use Ephemeral Workspaces** in CI/CD pipelines to guarantee clean, unpoisoned execution environments.
4. **Isolate Credentials**: Never hardcode API keys for private repositories in scripts; use the **nuts** secure credential vault.