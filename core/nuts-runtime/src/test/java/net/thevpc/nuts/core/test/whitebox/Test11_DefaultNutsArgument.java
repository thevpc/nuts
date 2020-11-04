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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.app.DefaultNutsArgument;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
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
        Assert.assertEquals("Option:"+a.getString(),option,a.isOption());
        Assert.assertEquals("Enabled:"+a.getString(),enabled,a.isEnabled());
        Assert.assertEquals("KeyValue:"+a.getString(),keyValue,a.isKeyValue());
        Assert.assertEquals("Negated:"+a.getString(),negated,a.isNegated());
        Assert.assertEquals("StringKey:"+a.getString(),key,a.getStringKey());
        Assert.assertEquals("StringValue:"+a.getString(),value,a.getStringValue());
        Assert.assertEquals("StringOptionName:"+a.getString(),optionName,a.getStringOptionName());
        Assert.assertEquals("StringOptionPrefix:"+a.getString(),optionPrefix,a.getStringOptionPrefix());
        Assert.assertEquals("KeyValueSeparator:"+a.getString(),eq,a.getKeyValueSeparator());
        TestUtils.println("OK : "+a.getString());
    }

}
