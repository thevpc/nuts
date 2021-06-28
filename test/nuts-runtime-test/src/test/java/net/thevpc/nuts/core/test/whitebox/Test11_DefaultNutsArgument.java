/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test11_DefaultNutsArgument {

    @Test
    public void test1() {
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
        Assertions.assertEquals("StringKey:"+a.getString(),key,a.getStringKey());
        Assertions.assertEquals("StringValue:"+a.getString(),value,a.getStringValue());
        Assertions.assertEquals("StringOptionName:"+a.getString(),optionName,a.getStringOptionName());
        Assertions.assertEquals("StringOptionPrefix:"+a.getString(),optionPrefix,a.getStringOptionPrefix());
        Assertions.assertEquals("KeyValueSeparator:"+a.getString(),eq,a.getKeyValueSeparator());
        TestUtils.println("OK : "+a.getString());
    }

}
