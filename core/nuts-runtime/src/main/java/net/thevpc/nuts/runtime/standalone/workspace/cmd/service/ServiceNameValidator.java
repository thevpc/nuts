package net.thevpc.nuts.runtime.standalone.workspace.cmd.service;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.regex.Pattern;

/**
 * Validates service names before they are interpolated into root-owned shell
 * scripts and service unit files. Internal to the runtime: no public API change.
 */
public final class ServiceNameValidator {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,127}");

    private ServiceNameValidator() {
    }

    /**
     * @return the validated (unchanged) service name
     * @throws NIllegalArgumentException when the name is not a safe service identifier
     */
    public static String validate(String serviceName) {
        if (serviceName == null || !VALID_NAME.matcher(serviceName).matches()) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid service name : %s", serviceName));
        }
        return serviceName;
    }
}
