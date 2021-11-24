/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * Command Line Argument
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public interface NutsArgument {

    /**
     * create instance for the given value and with the given session
     *
     * @param value   value
     * @param session session (must not be null)
     * @return new instance
     */
    static NutsArgument of(String value, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return NutsCommandLines.of(session).createArgument(value);
    }

    /**
     * true if the argument starts with '-' or '+'
     *
     * @return true if the argument starts with '-'
     */
    boolean isOption();

    /**
     * true if the argument do not start with '-' or '+' or is blank. this is
     * equivalent to {@code !isOption()}.
     *
     * @return true if the argument do not start with '-' or '+'
     */
    boolean isNonOption();

    /**
     * string representation of the argument or null
     *
     * @return string representation of the argument or null
     */
    String getString();

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-!name[=...]</li>
     * <li>--!name[=...]</li>
     * <li>!name[=...]</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is negated
     */
    boolean isNegated();

    /**
     * false if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if it is commented
     */
    boolean isEnabled();

    /**
     * Throw an exception if the argument is null
     *
     * @return {@code this} instance
     */
    NutsArgument required();

    /**
     * true if the argument is in the form key=value
     *
     * @return true if the argument is in the form key=value
     */
    boolean isKeyValue();

    /**
     * return option prefix part  ('-' and '--')
     *
     * @return option prefix part  ('-' and '--')
     * @since 0.5.7
     */
    NutsVal getOptionPrefix();

    /**
     * return query value separator
     *
     * @return query value separator
     * @since 0.5.7
     */
    String getSeparator();

    /**
     * return option key part excluding prefix ('-' and '--')
     *
     * @return option key part excluding prefix ('-' and '--')
     * @since 0.5.7
     */
    NutsVal getOptionName();

    /**
     * return new instance (never null) of the value part of the argument (after
     * =). However Argument's value may be null (
     * {@code getArgumentValue().getString() == null}). Here are some examples of
     * getArgumentValue() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentValue() ==&gt; Argument(null) </li>
     * <li>Argument("key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("key=").getArgumentValue() ==&gt; Argument("") </li>
     * <li>Argument("--key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!//key=value").getArgumentValue() ==&gt;
     * Argument("value") </li>
     * </ul>
     *
     * @return new instance (never null) of the value part of the argument
     * (after =)
     */
    NutsVal getValue();


    /**
     * return key part (never null) of the argument. The key does not include
     * neither ! nor // or = argument parts as they are parsed separately. Here
     * are some examples of getStringKey() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentKey() ==&gt; "key" </li>
     * <li>Argument("key=value").getArgumentKey() ==&gt; "key" </li>
     * <li>Argument("--key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!//key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--//!key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * </ul>
     * equivalent to {@code getArgumentKey().getString()}
     *
     * @return string key
     */
    NutsVal getKey();

    NutsVal getAll();


}
