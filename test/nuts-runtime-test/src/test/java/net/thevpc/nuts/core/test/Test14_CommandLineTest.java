/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNutsArgument;
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



    @Test
    public void testArgument() {
        NutsSession session = TestUtils.openNewMinTestWorkspace();
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument(null,elems),
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
                new DefaultNutsArgument("",elems),
                true,
                false,
                false,
                false,
                "",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("hello",elems),
                true,
                false,
                false,
                false,
                "hello",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("!hello",elems),
                true,
                false,
                false,
                false,
                "!hello",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("//!hello",elems),
                true,
                false,
                false,
                false,
                "//!hello",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello",elems),
                true,
                false,
                false,
                false,
                "/!hello",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello=me",elems),
                true,
                false,
                false,
                false,
                "/!hello=me",
                null,
                null,
                null,
                "="
        );
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--!hello=me",elems),
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
                new DefaultNutsArgument("--//!hello=me",elems),
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
                new DefaultNutsArgument("--//=",elems),
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

    private static void checkDefaultNutsArgument(NutsArgument a, boolean active, boolean option, boolean keyValue, boolean negated
            , String key
            , String value
            , String optionName
            , String optionPrefix
            , String eq
    ){
        Assertions.assertEquals(option,a.isOption(),"Option:"+a.getString());
        Assertions.assertEquals(active,a.isActive(),"Enabled:"+a.getString());
        Assertions.assertEquals(keyValue,a.isKeyValue(),"KeyValue:"+a.getString());
        Assertions.assertEquals(negated,a.isNegated(),"Negated:"+a.getString());
        Assertions.assertEquals(key,a.getKey().getString(),"StringKey:"+a.getString());
        Assertions.assertEquals(value,a.getValue().getString(),"StringValue:"+a.getString());
        Assertions.assertEquals(optionName,a.getOptionName().getString(),"StringOptionName:"+a.getString());
        Assertions.assertEquals(optionPrefix,a.getOptionPrefix().getString(),"StringOptionPrefix:"+a.getString());
        Assertions.assertEquals(eq,a.getSeparator(),"KeyValueSeparator:"+a.getString());
        TestUtils.println("OK : "+a.getString());
    }
}
