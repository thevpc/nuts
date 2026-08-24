---
title: Security Model
---


## Security Model

By default, Nuts runs as the current OS user with no authentication.
This is equivalent to `pip install` or `npm install`.

### Enabling Secure Mode

```sh
nuts settings secure
```


This activates:
- User authentication (local user database)
- Permission-based access control (install, uninstall, deploy, etc.)
- Audit logging (binary log at `~/.config/nuts/audit.log`)

```sh
nuts settings unsecure
```

```sh
nuts --user=<username> ...
```


```sh
nuts settings add user ...
```


### Permission Levels

| Permission | Effect |
|------------|--------|
| `install`  | Install new artifacts |
| `uninstall`| Remove artifacts |
| `deploy`   | Push to local repositories |
| `admin`    | Modify users and repositories |

### Workspaces as Sandboxes

Each workspace is an isolated universe:
- Separate config, apps, lib, cache, log folders
- No sharing of dependencies or JDKs unless explicitly configured
- Temporary workspaces (`--workspace=temp-$$`) for CI pipelines

### System-Wide vs. Per-User

- Per-user (default): `~/.config/nuts`
- System-wide: `nuts --root ...` (requires privileged OS access)
- 