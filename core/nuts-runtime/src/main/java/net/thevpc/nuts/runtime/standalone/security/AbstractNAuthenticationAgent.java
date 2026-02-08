package net.thevpc.nuts.runtime.standalone.security;

import java.util.Arrays;
import java.util.function.Function;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;

/**
 * abstract base implementation with no external storage, the secret is bundled with NCredentialId
 */
public abstract class AbstractNAuthenticationAgent implements NAuthenticationAgent {

    private final String name;
    private final NVersion version;

    public AbstractNAuthenticationAgent(String name, NVersion version) {
        this.name = NAssert.requireNamedNonBlank(name, "name");
        this.version = NAssert.requireNamedNonBlank(version, "version");
    }

    @Override
    public String getId() {
        return name + "#" + version;
    }

    @Override
    public boolean removeCredentials(NSecureToken credentialsId, Function<String, String> envProvider) {
        checkValidCredentialId(credentialsId);
        //not really stored elsewhere so just return true....
        return true;
    }

    @Override
    public <T> T withSecret(NSecureToken id, NSecretCaller<T> consumer, Function<String, String> env) {
        //credentials are already encrypted with default passphrase!
        checkValidCredentialId(id);
        String p = id.getPayload();
        T result = null;
        if (p != null && !p.isEmpty()) {
            if (p.charAt(0) == 'B') {
                char[] buffer = new char[p.length() - 1];
                try {
                    p.getChars(1, p.length(), buffer, 0);
                    try(NSecureString secureBuffer = NSecureString.ofSecure(buffer)) {
                        try(NSecureString chars = decryptChars(secureBuffer, env)){
                            result = consumer.call(id, chars, env);
                        }
                    }
                } finally {
                    Arrays.fill(buffer, '\0');
                }
                return result;
            }
        }
        throw new NSecurityException(NMsg.ofC("invalid secret %s", id));
    }

    protected boolean verifyOneWayImpl(NSecureString candidate, NSecureString storedHash, Function<String, String> env) {
        try (NSecureString rehashed= oneWayChars(candidate, env)){
            if (constantTimeEquals(storedHash, rehashed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean verify(NSecureToken id, NSecureString candidate, Function<String, String> env) {
        if (candidate == null) {
            candidate = NSecureString.ofEmpty();
        }
        //credentials are already encrypted with default passphrase!
        checkValidCredentialId(id);
        String p = id.getPayload();
        if (p != null && !p.isEmpty()) {
            if (p.charAt(0) == 'H') {
                char[] storeHashed = new char[p.length() - 1];
                p.getChars(1, p.length(), storeHashed, 0);
                return verifyOneWayImpl(candidate, NSecureString.ofSecure(storeHashed), env);
            } else if (p.charAt(0) == 'B') {
                char[] storedSecret = new char[p.length() - 1];
//                    char[] decryptedChars = null;
                try {
                    p.getChars(1, p.length(), storedSecret, 0);
                    try(NSecureString secureBuffer = NSecureString.ofSecure(storedSecret)) {
                        try(NSecureString decryptedChars = decryptChars(secureBuffer, env)) {
                            if (constantTimeEquals(candidate, decryptedChars)) {
                                return true;
                            }
                        }
                    }
                } finally {
                    Arrays.fill(storedSecret, '\0');
                }
                return false;
            }
        }
        throw new NSecurityException(NMsg.ofC("invalid credential %s", id));
    }

    protected boolean isSupportedVersion(NVersion version) {
        if (version == null) {
            return false;
        }
        if (version.compareTo(this.version) > 0) {
            return false;
        }
        //suppose backward compatibility by default!
        return true;
    }

    private void checkValidCredentialId(NSecureToken id) {
        NAssert.requireNamedNonBlank(id, "id");
        String a = id.getAgentId();
        if (a != null && !a.isEmpty()) {
            int h = a.indexOf("#");
            if (h >= 0) {
                String b = a.substring(0, h).trim();
                if (name.equals(b)) {
                    NVersion v = NVersion.of(a.substring(h + 1).trim());
                    if (version.equals(v)) {
                        return;
                    }
                    if (isSupportedVersion(version)) {
                        return;
                    } else {
                        throw new NSecurityException(NMsg.ofC("unsupported credential version : %s", id));
                    }
                }
            }
        }
        throw new NSecurityException(NMsg.ofC("invalid credential id %s", id));
    }

    @Override
    public NSecureToken addSecret(NSecureString credentials, Function<String, String> env) {
        if (credentials == null) {
            credentials = NSecureString.ofEmpty();
        }
        try (NSecureString val = oneWayChars(credentials, env)){
            return val.callWithContent(valChars -> {
                char[] result = null;
                try {
                    result = new char[valChars.length + 1];
                    result[0] = 'B';
                    System.arraycopy(valChars, 0, result, 1, valChars.length);
                    return new NSecureToken(getId(), new String(result));
                } finally {
                    if (result != null) {
                        Arrays.fill(result, '\0');
                    }
                }
            });
        }
    }

    @Override
    public NSecureToken updateSecret(NSecureToken old, NSecureString credentials, Function<String, String> envProvider) {
        removeCredentials(old, envProvider);
        return addSecret(credentials, envProvider);
    }

    @Override
    public NSecureToken addOneWayCredential(NSecureString credentials, Function<String, String> env) {
        if (credentials == null) {
            credentials = NSecureString.ofEmpty();
        }
        NSecureToken r;
        try (NSecureString val = oneWayChars(credentials, env)){
            String sresult = val.callWithContent(valChars -> {
                char[] result = null;
                try {
                    result = new char[valChars.length + 1];
                    result[0] = 'H';
                    System.arraycopy(valChars, 0, result, 1, valChars.length);
                    return new String(result);
                } finally {
                    if (result != null) {
                        Arrays.fill(result, '\0');
                    }
                }
            });
            r = new NSecureToken(getId(), sresult);
        }
        return r;
    }

    @Override
    public NSecureToken updateOneWay(NSecureToken old, NSecureString credentials, Function<String, String> envProvider) {
        removeCredentials(old, envProvider);
        return addOneWayCredential(credentials, envProvider);
    }

    public String getPassphrase(Function<String, String> envProvider) {
        String defVal = CoreSecurityUtils.DEFAULT_PASSPHRASE;
        if (envProvider != null) {
            String r = envProvider.apply("nuts.authentication-agent.simple.passphrase");
            if (r == null) {
                r = defVal;
            }
            if (r == null || r.isEmpty()) {
                r = defVal;
            }
            return r;
        }
        return defVal;
    }

    private static boolean constantTimeEquals(NSecureString a, NSecureString b) {
        if (a == null || b == null) {
            if (a == null && b == null) {
                return true;
            }
            return false;
        }
        return a.constantTimeEquals(b);
    }

    protected abstract NSecureString decryptChars(NSecureString data, Function<String, String> env);

    protected abstract NSecureString encryptChars(NSecureString data, Function<String, String> env);

    protected abstract NSecureString oneWayChars(NSecureString data, Function<String, String> env);

}
