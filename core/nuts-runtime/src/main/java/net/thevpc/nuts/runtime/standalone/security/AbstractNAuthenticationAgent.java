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
    public boolean removeCredentials(NCredentialId credentialsId, Function<String, String> envProvider) {
        checkValidCredentialId(credentialsId);
        //not really stored elsewhere so just return true....
        return true;
    }

    @Override
    public <T> T withSecret(NCredentialId id, NSecretCaller<T> consumer, Function<String, String> env) {
        //credentials are already encrypted with default passphrase!
        checkValidCredentialId(id);
        String p = id.getPayload();
        T result = null;
        if (p != null && !p.isEmpty()) {
            if (p.charAt(0) == 'B') {
                char[] buffer = new char[p.length() - 1];
                char[] chars = new char[p.length() - 1];
                try {
                    p.getChars(1, p.length(), buffer, 0);
                    chars = decryptChars(buffer, env);
                    result = consumer.call(id, chars, env);
                } finally {
                    Arrays.fill(chars, '\0');
                    Arrays.fill(buffer, '\0');
                }
                return result;
            }
        }
        throw new NSecurityException(NMsg.ofC("invalid secret %s", id));
    }

    protected boolean verifyOneWayImpl(char[] candidate, char[] storedHash, Function<String, String> env) {
        try {
            char[] rehashed = oneWayChars(candidate, env);
            if (constantTimeEquals(storedHash, rehashed)) {
                return true;
            }
        } finally {
            Arrays.fill(storedHash, '\0');
        }
        return false;
    }

    @Override
    public boolean verify(NCredentialId id, char[] candidate, Function<String, String> env) {
        if (candidate == null) {
            candidate = new char[0];
        }
        try {
            //credentials are already encrypted with default passphrase!
            checkValidCredentialId(id);
            String p = id.getPayload();
            if (p != null && !p.isEmpty()) {
                if (p.charAt(0) == 'H') {
                    char[] storeHashed = new char[p.length() - 1];
                    try {
                        p.getChars(1, p.length(), storeHashed, 0);
                        return verifyOneWayImpl(candidate, storeHashed, env);
                    } finally {
                        Arrays.fill(storeHashed, '\0');
                    }
                } else if (p.charAt(0) == 'B') {
                    char[] storedSecret = new char[p.length() - 1];
                    char[] decryptedChars=null;
                    try {
                        p.getChars(1, p.length(), storedSecret, 0);
                        decryptedChars = decryptChars(storedSecret, env);
                        if (constantTimeEquals(candidate, decryptedChars)) {
                            return true;
                        }
                    } finally {
                        Arrays.fill(storedSecret, '\0');
                        if(decryptedChars!=null){
                            Arrays.fill(decryptedChars, '\0');
                        }
                    }
                    return false;
                }
            }
            throw new NSecurityException(NMsg.ofC("invalid credential %s", id));
        } finally {
            Arrays.fill(candidate, '\0');
        }
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

    private void checkValidCredentialId(NCredentialId id) {
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
    public NCredentialId addSecret(char[] credentials, Function<String, String> env) {
        if (credentials == null) {
            credentials = new char[0];
        }
        char[] val = null;
        char[] result = null;
        NCredentialId r;
        try {
            val = encryptChars(credentials, env);
            result = new char[val.length + 1];
            result[0] = 'B';
            System.arraycopy(val, 0, result, 1, val.length);
            r = new NCredentialId(getId(), new String(result));
        } finally {
            if (val != null) {
                Arrays.fill(val, '\0');
            }
            if (result != null) {
                Arrays.fill(result, '\0');
            }
        }
        return r;
    }

    @Override
    public NCredentialId updateSecret(NCredentialId old, char[] credentials, Function<String, String> envProvider) {
        removeCredentials(old, envProvider);
        return addSecret(credentials, envProvider);
    }

    @Override
    public NCredentialId addOneWayCredential(char[] credentials, Function<String, String> env) {
        if (credentials == null) {
            credentials = new char[0];
        }
        char[] val = null;
        char[] result = null;
        NCredentialId r;
        try {
            val = oneWayChars(credentials, env);
            result = new char[val.length + 1];
            result[0] = 'H';
            System.arraycopy(val, 0, result, 1, val.length);
            r = new NCredentialId(getId(), new String(result));
        } finally {
            if (val != null) {
                Arrays.fill(val, '\0');
            }
            if (result != null) {
                Arrays.fill(result, '\0');
            }
        }
        return r;
    }

    @Override
    public NCredentialId updateOneWay(NCredentialId old, char[] credentials, Function<String, String> envProvider) {
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

    private static boolean constantTimeEquals(char[] a, char[] b) {
        if (a == null || b == null) {
            return false;
        }
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    protected abstract char[] decryptChars(char[] data, Function<String, String> env);

    protected abstract char[] encryptChars(char[] data, Function<String, String> env);

    protected abstract char[] oneWayChars(char[] data, Function<String, String> env);

}
