---
title: Remote Execution
---

## Run on a Remote Host

**nuts** provides seamless, transparent remote execution capabilities. You can execute **nuts** commands and launch applications on remote servers via SSH, completely bypassing the need to manually install **nuts**, configure workspaces, or manage deployments on the target machine.

### Basic Usage

To run an application remotely, use the `exec` command with the `--target` flag specifying the SSH connection string:

```bash
nuts exec --target=ssh://user@host myapp
```

### How It Works Under the Hood

When you trigger a remote execution, **nuts** orchestrates a complex deployment sequence entirely automatically:

- 1. **Local Bundling**: It analyzes the requested application and its dependencies, creating an air-gapped bundle on your local machine.
- 2. **Secure Transfer**: It securely SCPs the self-contained bundle to a temporary location on the remote host.
- 3. **Self-Installation**: It bootstraps a temporary, isolated **nuts** workspace on the remote host using the transferred bundle (no internet connection required on the remote server).
- 4. **Execution**: It launches the application within the remote environment.
- 5. **Stream Redirection**: It connects the remote standard output, standard error, and standard input streams directly back to your local terminal, making it feel like a local execution.

### Prerequisites

To utilize remote execution, ensure the following conditions are met:
* **SSH Key-based Authentication**: You must have SSH access to the remote host configured with key-based authentication. Password prompts during the automated transfer phase are not supported.
* **Remote Java**: The remote host must have a compatible Java Runtime Environment installed and available on the system PATH. Alternatively, you can use a bundle that includes a JRE (though this significantly increases transfer times).

### Passing Arguments

You can pass arguments to the remote application exactly as you would locally. Everything following the application name is securely forwarded:

```bash
nuts exec --target=ssh://user@host myapp --config prod.json --verbose
```

### Security Considerations

Remote execution is designed with security in mind:
* **No Additional Daemons**: It uses your existing SSH daemon (`sshd`). No custom **nuts** ports or agents need to be exposed.
* **Native Authentication**: It inherits your standard SSH authentication, respecting authorized keys, jump hosts, and network policies.
* **Ephemeral Footprint**: Temporary files and workspaces generated during execution can be configured to be ephemeral, leaving no trace after the process terminates.

### Troubleshooting

If remote execution fails, check the following common issues:

* **SSH Connectivity**: Verify you can manually connect to the server without a password using `ssh user@host`.
* **Missing Remote Java**: Ensure Java is installed remotely. Run `ssh user@host java -version` to verify.
* **Path Issues**: If Java is installed but not on the default path, you may need to update the remote `.bashrc` or specify the Java path explicitly.
* **Transfer Timeouts**: For very large applications on slow connections, the SCP phase might time out. Consider pre-bundling or installing **nuts** natively on the remote host for heavy workloads.
