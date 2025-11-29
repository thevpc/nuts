package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;

/**
 * Represents a snapshot of the execution environment of a target host.
 * <p>
 * This information is typically obtained via a probe and may include
 * the operating system, shell, and user details. The snapshot is intended
 * to be immutable and can be cached for reuse across multiple executions.
 *
 * <p>Example usage:
 * <pre>{@code
 * NExecTargetInfo info = cmd.probeTarget();
 * System.out.println(info.getOsFamily());
 * System.out.println(info.getUserName());
 * }</pre>
 *
 * @since 0.8.9
 */
public interface NExecTargetInfo {
    /** Returns the operating system family of the target host. */
    NOsFamily getOsFamily();

    /** Returns the OS identifier or version of the target host. */
    NId getOsId();

    /** Returns the shell identifier used on the target host. */
    NId getShellId();

    /** Returns the shell family of the target host. */
    NShellFamily getShellFamily();

    /** Returns the effective username used for execution on the target. */
    String getUserName();

    /** Returns the root username on the target host, if different from user. */
    String getRootUserName();

    /** Returns the home directory of the execution user on the target host. */
    String getUserHome();
}
