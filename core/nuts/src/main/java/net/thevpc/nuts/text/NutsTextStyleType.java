/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * @app.category Format
 */
public enum NutsTextStyleType implements NutsEnum {
    PLAIN(true),//f
    UNDERLINED(true),//_
    ITALIC(true),// /
    STRIKED(true),// -
    REVERSED(true),//v
    BOLD(true),//d
    BLINK(true),//k
    FORE_COLOR(true),//f
    BACK_COLOR(true),//b
    FORE_TRUE_COLOR(true),//f
    BACK_TRUE_COLOR(true),//b

    PRIMARY(false), //p
    SECONDARY(false),//s
    ERROR(false),
    WARN(false),
    INFO(false),
    CONFIG(false),
    COMMENTS(false),
    STRING(false),
    NUMBER(false),
    DATE(false),
    BOOLEAN(false),
    KEYWORD(false),
    OPTION(false),
    INPUT(false),
    SEPARATOR(false),
    OPERATOR(false),
    SUCCESS(false),
    FAIL(false),
    DANGER(false),
    VAR(false),
    PALE(false),
    PATH(false),
    VERSION(false),
    TITLE(false);
    private final boolean basic;
    private final String id;

    NutsTextStyleType(boolean basic) {
        this.basic = basic;
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOptional<NutsTextStyleType> parse(String value) {
        return NutsApiUtils.parse(value, NutsTextStyleType.class,s->{
            switch (s.toLowerCase()) {
                case "f":
                case "foreground":
                case "foregroundcolor": {
                    return NutsOptional.of(FORE_COLOR);
                }
                case "plain": {
                    return NutsOptional.of(PLAIN);
                }
                case "foregroundx":
                case "foregroundtruecolor": {
                    return NutsOptional.of(FORE_TRUE_COLOR);
                }
                case "b":
                case "background":
                case "back_color":
                case "backcolor":
                case "backgroundcolor": {
                    return NutsOptional.of(BACK_COLOR);
                }
                case "backtruecolor":
                case "backgroundx":
                case "backgroundtruecolor": {
                    return NutsOptional.of(BACK_TRUE_COLOR);

                }

                case "p":
                case "primary": {
                    return NutsOptional.of(PRIMARY);
                }
                case "s":
                case "secondary": {
                    return NutsOptional.of(SECONDARY);
                }
                case "underlined": {
                    return NutsOptional.of(UNDERLINED);
                }
                case "bold": {
                    return NutsOptional.of(BOLD);
                }
                case "boolean":
                case "bool": {
                    return NutsOptional.of(BOOLEAN);
                }
                case "blink": {
                    return NutsOptional.of(BLINK);
                }
                case "comment":
                case "comments": {
                    return NutsOptional.of(COMMENTS);
                }
                case "config": {
                    return NutsOptional.of(CONFIG);
                }
                case "danger": {
                    return NutsOptional.of(DANGER);
                }
                case "date": {
                    return NutsOptional.of(DATE);
                }
                case "number": {
                    return NutsOptional.of(NUMBER);
                }
                case "error": {
                    return NutsOptional.of(ERROR);
                }
                case "warning":
                case "warn": {
                    return NutsOptional.of(WARN);
                }
                case "version": {
                    return NutsOptional.of(VERSION);
                }
                case "var":
                case "variable": {
                    return NutsOptional.of(VAR);
                }
                case "input": {
                    return NutsOptional.of(INPUT);
                }
                case "title": {
                    return NutsOptional.of(TITLE);
                }
                case "success": {
                    return NutsOptional.of(SUCCESS);
                }
                case "string": {
                    return NutsOptional.of(STRING);
                }
                case "strike":
                case "striked": {
                    return NutsOptional.of(STRIKED);
                }
                case "sep":
                case "separator": {
                    return NutsOptional.of(SEPARATOR);
                }
                case "reversed": {
                    return NutsOptional.of(REVERSED);
                }
                case "path": {
                    return NutsOptional.of(PATH);
                }
                case "option": {
                    return NutsOptional.of(OPTION);
                }
                case "pale": {
                    return NutsOptional.of(PALE);
                }
                case "operator": {
                    return NutsOptional.of(OPERATOR);
                }
                case "kw":
                case "keyword": {
                    return NutsOptional.of(KEYWORD);
                }
                case "italic": {
                    return NutsOptional.of(ITALIC);
                }
                case "information":
                case "info": {
                    return NutsOptional.of(INFO);
                }
                case "fail": {
                    return NutsOptional.of(FAIL);
                }
            }
            return null;
        });
    }


    @Override
    public String id() {
        return id;
    }

    public boolean basic() {
        return basic;
    }
}
