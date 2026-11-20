package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.io.util.NSecretRedactor;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Regression tests for credential redaction and HTTPS-only inline auth.
 */
public class CredentialSecurityTest {

    @Test
    public void secretsAreRedactedFromUrlsAndHeaders() {
        Assertions.assertEquals("https://***:***@example.com/repo",
                NSecretRedactor.redact("https://alice:s3cret@example.com/repo"));
        Assertions.assertEquals("https://example.com/f?ul=***&up=***&x=1",
                NSecretRedactor.redact("https://example.com/f?ul=alice&up=s3cret&x=1"));
        Assertions.assertEquals("Authorization: ***",
                NSecretRedactor.redact("Authorization: Basic YWxpY2U6czNjcmV0"));
        Assertions.assertEquals("no secrets here", NSecretRedactor.redact("no secrets here"));
    }

    @Test
    public void basicAuthIsHttpsOnlyAndUtf8Safe() throws Exception {
        Assertions.assertEquals(basic("user:pass"),
                DefaultNHttpClient.resolveAuthorizationHeader(new URL("https://user:pass@example.com/")));
        Assertions.assertNull(
                DefaultNHttpClient.resolveAuthorizationHeader(new URL("http://user:pass@example.com/")));
        Assertions.assertEquals(basic("u@x:p+ss"),
                DefaultNHttpClient.resolveAuthorizationHeader(new URL("https://u%40x:p%2Bss@example.com/")));
        Assertions.assertEquals(basic("user:"),
                DefaultNHttpClient.resolveAuthorizationHeader(new URL("https://user@example.com/")));
    }

    @Test
    public void cleartextInlineCredentialsAreStripped() {
        Assertions.assertEquals("http://example.com/repo",
                DefaultNRepositoryConfigModel.stripCleartextCredentials(
                        "http://alice:s3cret@example.com/repo"));
        Assertions.assertEquals("https://alice:s3cret@example.com/repo",
                DefaultNRepositoryConfigModel.stripCleartextCredentials(
                        "https://alice:s3cret@example.com/repo"));
    }

    private static String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
