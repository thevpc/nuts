/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.platform.NRuntimeDistributionManager;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests resolution of java version specs that are not plain versions : the {@code current}
 * keyword and the name of a registered java installation.
 *
 * @author thevpc
 */
public class JavaVersionSpecTest {

    private static final String REGISTERED_NAME = "jdk-registered-for-tests";

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        //register the current jdk under a custom name, as 'settings add java <folder>' would do
        NRuntimeDistribution registered = NRuntimeDistributionManager.of()
                .resolveRuntimeDistribution(
                        NRuntimeDistributionFamily.JAVA,
                        NJavaSdkUtils.currentJavaHome(),
                        REGISTERED_NAME
                )
                .orElseThrow(() -> new IllegalStateException("unable to register " + REGISTERED_NAME));
        NAssert.requireTrue(NRuntimeDistributionManager.of().addRuntimeDistribution(registered),
                () -> NMsg.ofC("unable to add %s", REGISTERED_NAME));
    }

    @Test
    public void testIsJavaVersionSpec() {
        Assertions.assertTrue(NJavaSdkUtils.isJavaVersionSpec("11"));
        Assertions.assertTrue(NJavaSdkUtils.isJavaVersionSpec("1.8.0_452"));
        Assertions.assertTrue(NJavaSdkUtils.isJavaVersionSpec("21+35"));
        Assertions.assertTrue(NJavaSdkUtils.isJavaVersionSpec(""));
        Assertions.assertTrue(NJavaSdkUtils.isJavaVersionSpec(null));
        Assertions.assertFalse(NJavaSdkUtils.isJavaVersionSpec(NRuntimeDistribution.JAVA_VERSION_CURRENT));
        Assertions.assertFalse(NJavaSdkUtils.isJavaVersionSpec(REGISTERED_NAME));
    }

    @Test
    public void testIsCurrentJavaSpec() {
        Assertions.assertTrue(NJavaSdkUtils.isCurrentJavaSpec("current"));
        Assertions.assertTrue(NJavaSdkUtils.isCurrentJavaSpec("Current"));
        Assertions.assertFalse(NJavaSdkUtils.isCurrentJavaSpec(null));
        Assertions.assertFalse(NJavaSdkUtils.isCurrentJavaSpec(""));
        Assertions.assertFalse(NJavaSdkUtils.isCurrentJavaSpec("11"));
        Assertions.assertFalse(NJavaSdkUtils.isCurrentJavaSpec(REGISTERED_NAME));
    }

    @Test
    public void testCurrentJavaHome() {
        NPath home = NJavaSdkUtils.currentJavaHome();
        Assertions.assertNotNull(home);
        String appSuffix = NEnv.of().osFamily() == NOsFamily.WINDOWS ? ".exe" : "";
        Assertions.assertTrue(home.resolve("bin").resolve("java" + appSuffix).isRegularFile(),
                "no java executable in " + home);
    }

    @Test
    public void testResolveCurrent() {
        NRuntimeDistribution current = NJavaSdkUtils.of()
                .resolveJavaDistribution("current")
                .orElseThrow(() -> new IllegalStateException("current not resolved"));
        Assertions.assertEquals(current.path(), NJavaSdkUtils.currentJavaHome().toString());
        Assertions.assertEquals(current.version(), System.getProperty("java.version"));
    }

    @Test
    public void testResolveCurrentWithoutProvisioning() {
        //remote lookup is disabled : 'current' must be honored as is and never trigger any installation
        NRuntimeDistribution current = NJavaSdkUtils.of()
                .resolveJdkLocation("current", true, false, false, null)
                .orElseThrow(() -> new IllegalStateException("current not resolved"));
        Assertions.assertEquals(current.path(), NJavaSdkUtils.currentJavaHome().toString());
    }

    @Test
    public void testResolveRegisteredName() {
        NRuntimeDistribution d = NJavaSdkUtils.of()
                .resolveJavaDistribution(REGISTERED_NAME)
                .orElseThrow(() -> new IllegalStateException(REGISTERED_NAME + " not resolved"));
        Assertions.assertEquals(d.name(), REGISTERED_NAME);
        Assertions.assertEquals(d.family(), NRuntimeDistributionFamily.JAVA);
    }

    @Test
    public void testResolveRegisteredNameWithoutProvisioning() {
        NRuntimeDistribution d = NJavaSdkUtils.of()
                .resolveJdkLocation(REGISTERED_NAME, true, false, false, null)
                .orElseThrow(() -> new IllegalStateException(REGISTERED_NAME + " not resolved"));
        Assertions.assertEquals(d.name(), REGISTERED_NAME);
    }

    @Test
    public void testFindRegisteredNameIgnoringFormat() {
        NRuntimeDistribution d = NJavaSdkUtils.of().findJavaDistributionByName(REGISTERED_NAME.toUpperCase())
                .orElseThrow(() -> new IllegalStateException(REGISTERED_NAME + " not found"));
        Assertions.assertEquals(d.name(), REGISTERED_NAME);
    }

    @Test
    public void testResolvePlainVersionIsNotAnAlias() {
        //plain versions are left to the standard resolution : empty and not an error
        Assertions.assertTrue(NJavaSdkUtils.of().resolveJavaDistribution("11").isEmpty());
        Assertions.assertTrue(NJavaSdkUtils.of().resolveJavaDistribution("1.8.0_452").isEmpty());
    }

    @Test
    public void testResolveUnknownNameIsAnError() {
        NOptional<NRuntimeDistribution> r = NJavaSdkUtils.of().resolveJavaDistribution("nosuchjavahere");
        Assertions.assertTrue(r.isError());
        //the reason must be explicit : the value is neither a version nor a known java name
        String message = r.message().get().toString();
        Assertions.assertTrue(message.contains("nosuchjavahere"), message);
        Assertions.assertTrue(message.contains(NRuntimeDistribution.JAVA_VERSION_CURRENT), message);
        Assertions.assertTrue(NJavaSdkUtils.of().resolveJdkLocation("nosuchjavahere", true, false, false, null).isError());
    }
}
