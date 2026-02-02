package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.util.NHex;

public class CoreSecurityUtils {
    public static final String ENV_KEY_PASSPHRASE = "passphrase";
    public static final String DEFAULT_PASSPHRASE = NHex.fromBytes("It's completely nuts!!".getBytes());
}
