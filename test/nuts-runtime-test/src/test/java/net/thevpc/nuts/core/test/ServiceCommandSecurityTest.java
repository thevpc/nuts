package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.service.ScriptBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.service.ServiceNameValidator;
import net.thevpc.nuts.util.NIllegalArgumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for safe service-name handling in root scripts.
 */
public class ServiceCommandSecurityTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void validServiceNamesAreAccepted() {
        Assertions.assertEquals("nuts", ServiceNameValidator.validate("nuts"));
        Assertions.assertEquals("my-service_1", ServiceNameValidator.validate("my-service_1"));
    }

    @Test
    public void injectionAndTraversalNamesAreRejected() {
        String[] invalid = {
                null, "", "../evil", "/etc/evil", "foo bar",
                "foo;rm -rf /", "foo$(id)", "foo`id`", "foo\nsystemctl reboot",
                "foo/bar", "foo\\bar", "-foo", "foo'bar"
        };
        for (String name : invalid) {
            Assertions.assertThrows(NIllegalArgumentException.class,
                    () -> ServiceNameValidator.validate(name), String.valueOf(name));
        }
    }

    @Test
    public void scriptArgumentsAreShellQuoted() {
        Assertions.assertEquals("'simple'", ScriptBuilder.quote("simple"));
        Assertions.assertEquals("'a b'", ScriptBuilder.quote("a b"));
        Assertions.assertEquals("'a'\\''b'", ScriptBuilder.quote("a'b"));
        Assertions.assertEquals("''", ScriptBuilder.quote(null));
    }
}
