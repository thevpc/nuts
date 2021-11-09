/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class Test14_CommandLineTest {


    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--log-info",
                "--skip-companions",
                "--skip-welcome"
        );

        NutsArgument[] cmd = NutsCommandLine.of("-ad+ +ad--",session).toArgumentArray();
        Set<String> set = Arrays.stream(cmd).map(x -> x.toString()).collect(Collectors.toSet());
        Set<String> expectedSet = new HashSet<>(Arrays.asList(
                "-a", "-d+", "+a","+d--"
        ));
        Assertions.assertEquals(set,expectedSet);
    }


    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

    @Test
    public void testArgument() {
        checkDefaultNutsArgument(
                new DefaultNutsArgument(null),
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument(""),
                true,
                false,
                false,
                false,
                "",
                null,
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("hello"),
                true,
                false,
                false,
                false,
                "hello",
                null,
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("!hello"),
                true,
                false,
                false,
                true,
                "hello",
                null,
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("//!hello"),
                false,
                false,
                false,
                true,
                "hello",
                null,
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello"),
                true,
                false,
                false,
                false,
                "/!hello",
                null,
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello=me"),
                true,
                false,
                true,
                false,
                "/!hello",
                "me",
                "",
                "",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--!hello=me"),
                true,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//!hello=me"),
                false,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//="),
                false,
                true,
                true,
                false,
                "--",
                "",
                "",
                "--",
                "="
        );
    }

    private static void checkDefaultNutsArgument(NutsArgument a, boolean enabled, boolean option, boolean keyValue, boolean negated
            , String key
            , String value
            , String optionName
            , String optionPrefix
            , String eq
    ){
        Assertions.assertEquals(option,a.isOption(),"Option:"+a.getString());
        Assertions.assertEquals(enabled,a.isEnabled(),"Enabled:"+a.getString());
        Assertions.assertEquals(keyValue,a.isKeyValue(),"KeyValue:"+a.getString());
        Assertions.assertEquals(negated,a.isNegated(),"Negated:"+a.getString());
        Assertions.assertEquals(key,a.getKey().getString(),"StringKey:"+a.getString());
        Assertions.assertEquals(value,a.getValue().getString(),"StringValue:"+a.getString());
        Assertions.assertEquals(optionName,a.getOptionName().getString(),"StringOptionName:"+a.getString());
        Assertions.assertEquals(optionPrefix,a.getOptionPrefix(),"StringOptionPrefix:"+a.getString());
        Assertions.assertEquals(eq,a.getSeparator(),"KeyValueSeparator:"+a.getString());
        TestUtils.println("OK : "+a.getString());
    }
}
