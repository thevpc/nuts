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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * @app.category Format
 */
public enum NTextStyleType implements NEnum {
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

    NTextStyleType(boolean basic) {
        this.basic = basic;
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NTextStyleType> parse(String value) {
        return NEnumUtils.parseEnum(value, NTextStyleType.class, s->{
            switch (s.getNormalizedValue()) {
                case "F":
                case "FOREGROUND":
                case "FOREGROUNDCOLOR": {
                    return NOptional.of(FORE_COLOR);
                }
                case "PLAIN": {
                    return NOptional.of(PLAIN);
                }
                case "FX":
                case "FOREGROUNDX":
                case "FOREGROUNDTRUECOLOR": {
                    return NOptional.of(FORE_TRUE_COLOR);
                }
                case "B":
                case "BACKGROUND":
                case "BACK_COLOR":
                case "BACKCOLOR":
                case "BACKGROUNDCOLOR": {
                    return NOptional.of(BACK_COLOR);
                }
                case "BX":
                case "BACKTRUECOLOR":
                case "BACKGROUNDX":
                case "BACKGROUNDTRUECOLOR": {
                    return NOptional.of(BACK_TRUE_COLOR);

                }

                case "P":
                case "PRIMARY": {
                    return NOptional.of(PRIMARY);
                }
                case "S":
                case "SECONDARY": {
                    return NOptional.of(SECONDARY);
                }
                case "UNDERLINED": {
                    return NOptional.of(UNDERLINED);
                }
                case "BOLD": {
                    return NOptional.of(BOLD);
                }
                case "BOOLEAN":
                case "BOOL": {
                    return NOptional.of(BOOLEAN);
                }
                case "BLINK": {
                    return NOptional.of(BLINK);
                }
                case "COMMENT":
                case "COMMENTS": {
                    return NOptional.of(COMMENTS);
                }
                case "CONFIG": {
                    return NOptional.of(CONFIG);
                }
                case "DANGER": {
                    return NOptional.of(DANGER);
                }
                case "DATE": {
                    return NOptional.of(DATE);
                }
                case "NUMBER": {
                    return NOptional.of(NUMBER);
                }
                case "ERROR": {
                    return NOptional.of(ERROR);
                }
                case "WARNING":
                case "WARN": {
                    return NOptional.of(WARN);
                }
                case "VERSION": {
                    return NOptional.of(VERSION);
                }
                case "VAR":
                case "VARIABLE": {
                    return NOptional.of(VAR);
                }
                case "INPUT": {
                    return NOptional.of(INPUT);
                }
                case "TITLE": {
                    return NOptional.of(TITLE);
                }
                case "SUCCESS": {
                    return NOptional.of(SUCCESS);
                }
                case "STRING": {
                    return NOptional.of(STRING);
                }
                case "STRIKE":
                case "STRIKED": {
                    return NOptional.of(STRIKED);
                }
                case "SEP":
                case "SEPARATOR": {
                    return NOptional.of(SEPARATOR);
                }
                case "REVERSED": {
                    return NOptional.of(REVERSED);
                }
                case "PATH": {
                    return NOptional.of(PATH);
                }
                case "OPTION": {
                    return NOptional.of(OPTION);
                }
                case "PALE": {
                    return NOptional.of(PALE);
                }
                case "OPERATOR": {
                    return NOptional.of(OPERATOR);
                }
                case "KW":
                case "KEYWORD": {
                    return NOptional.of(KEYWORD);
                }
                case "ITALIC": {
                    return NOptional.of(ITALIC);
                }
                case "INFORMATION":
                case "INFO": {
                    return NOptional.of(INFO);
                }
                case "FAIL": {
                    return NOptional.of(FAIL);
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
