package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regression tests for the generated PowerShell scripts.
 * <p>
 * {@code template-nuts-env.ps1} defines the workspace variables as <b>environment</b> variables
 * ({@code $env:NUTS_WORKSPACE_BINDIR}). {@code template-nuts-init.ps1} used to read
 * {@code $NUTS_WORKSPACE_BINDIR} (a PowerShell variable that does not exist), so
 * {@code $env:Path=$NUTS_WORKSPACE_BINDIR;$env:Path} assigned an empty value to {@code PATH}
 * and every PowerShell session started with an empty PATH ({@code git}, {@code java}, ... not found).
 */
public class NdiPowerShellTemplatesTest {

    private static final String BASE = "/net/thevpc/nuts/runtime/settings/";

    private static final List<String> TEMPLATES = Arrays.asList(
            "template-body/template-body.ps1",
            "template-nuts/template-nuts.ps1",
            "template-nuts-complete/template-nuts-complete.ps1",
            "template-nuts-env/template-nuts-env.ps1",
            "template-nuts-init/template-nuts-init.ps1",
            "template-nuts-term/template-nuts-term.ps1",
            "template-nuts-term-init/template-nuts-term-init.ps1"
    );

    // $NUTS_WORKSPACE_xxx that is not $env:NUTS_WORKSPACE_xxx and not a $$PLACEHOLDER$$
    private static final Pattern BARE_WORKSPACE_VARIABLE = Pattern.compile("(?<![\\w:$])\\$NUTS_WORKSPACE_\\w+");

    private static String read(String name) throws IOException {
        try (InputStream in = NdiPowerShellTemplatesTest.class.getResourceAsStream(BASE + name)) {
            Assertions.assertNotNull(in, "missing template resource " + name);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void workspaceVariablesAreAlwaysReadAsEnvironmentVariables() throws IOException {
        for (String template : TEMPLATES) {
            Matcher m = BARE_WORKSPACE_VARIABLE.matcher(read(template));
            boolean found = m.find();
            // the message is lazy: m.group() is only valid (and only needed) when a match was found
            Assertions.assertFalse(found, () -> template + " reads '" + m.group()
                    + "' which is not defined: the env script defines $env:NUTS_WORKSPACE_*");
        }
    }

    @Test
    public void initScriptKeepsTheExistingPathAndPrependsTheNutsBinFolder() throws IOException {
        String init = read("template-nuts-init/template-nuts-init.ps1");
        Assertions.assertTrue(init.contains("$env:NUTS_WORKSPACE_BINDIR"),
                "init script must prepend $env:NUTS_WORKSPACE_BINDIR");
        // the previous PATH must be part of the new value, and the assignment must not be
        // the bare statement `$env:Path=$X;$env:Path` which assigns $X (empty) and then evaluates $env:Path
        Assertions.assertTrue(Pattern.compile("\\$env:Path\\s*=\\s*\"[^\"]*\\$env:Path\"").matcher(init).find(),
                "$env:Path must be assigned a quoted string that ends with the previous $env:Path");
        Assertions.assertFalse(Pattern.compile("\\$env:Path\\s*=\\s*\\$NUTS_").matcher(init).find());
    }

    @Test
    public void everyEnvVariableUsedByInitScriptIsDefinedByTheEnvScript() throws IOException {
        String env = read("template-nuts-env/template-nuts-env.ps1");
        Matcher m = Pattern.compile("\\$env:(NUTS_[A-Z_]+)").matcher(read("template-nuts-init/template-nuts-init.ps1"));
        while (m.find()) {
            Assertions.assertTrue(env.contains("$env:" + m.group(1) + "="),
                    "init script uses $env:" + m.group(1) + " but template-nuts-env.ps1 does not define it");
        }
    }
}
