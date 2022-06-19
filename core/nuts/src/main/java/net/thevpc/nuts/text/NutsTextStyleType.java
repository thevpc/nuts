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

import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

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
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsTextStyleType> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsTextStyleType.class, s->{
            switch (s.getNormalizedValue()) {
                case "F":
                case "FOREGROUND":
                case "FOREGROUNDCOLOR": {
                    return NutsOptional.of(FORE_COLOR);
                }
                case "PLAIN": {
                    return NutsOptional.of(PLAIN);
                }
                case "FOREGROUNDX":
                case "FOREGROUNDTRUECOLOR": {
                    return NutsOptional.of(FORE_TRUE_COLOR);
                }
                case "B":
                case "BACKGROUND":
                case "BACK_COLOR":
                case "BACKCOLOR":
                case "BACKGROUNDCOLOR": {
                    return NutsOptional.of(BACK_COLOR);
                }
                case "BACKTRUECOLOR":
                case "BACKGROUNDX":
                case "BACKGROUNDTRUECOLOR": {
                    return NutsOptional.of(BACK_TRUE_COLOR);

                }

                case "P":
                case "PRIMARY": {
                    return NutsOptional.of(PRIMARY);
                }
                case "S":
                case "SECONDARY": {
                    return NutsOptional.of(SECONDARY);
                }
                case "UNDERLINED": {
                    return NutsOptional.of(UNDERLINED);
                }
                case "BOLD": {
                    return NutsOptional.of(BOLD);
                }
                case "BOOLEAN":
                case "BOOL": {
                    return NutsOptional.of(BOOLEAN);
                }
                case "BLINK": {
                    return NutsOptional.of(BLINK);
                }
                case "COMMENT":
                case "COMMENTS": {
                    return NutsOptional.of(COMMENTS);
                }
                case "CONFIG": {
                    return NutsOptional.of(CONFIG);
                }
                case "DANGER": {
                    return NutsOptional.of(DANGER);
                }
                case "DATE": {
                    return NutsOptional.of(DATE);
                }
                case "NUMBER": {
                    return NutsOptional.of(NUMBER);
                }
                case "ERROR": {
                    return NutsOptional.of(ERROR);
                }
                case "WARNING":
                case "WARN": {
                    return NutsOptional.of(WARN);
                }
                case "VERSION": {
                    return NutsOptional.of(VERSION);
                }
                case "VAR":
                case "VARIABLE": {
                    return NutsOptional.of(VAR);
                }
                case "INPUT": {
                    return NutsOptional.of(INPUT);
                }
                case "TITLE": {
                    return NutsOptional.of(TITLE);
                }
                case "SUCCESS": {
                    return NutsOptional.of(SUCCESS);
                }
                case "STRING": {
                    return NutsOptional.of(STRING);
                }
                case "STRIKE":
                case "STRIKED": {
                    return NutsOptional.of(STRIKED);
                }
                case "SEP":
                case "SEPARATOR": {
                    return NutsOptional.of(SEPARATOR);
                }
                case "REVERSED": {
                    return NutsOptional.of(REVERSED);
                }
                case "PATH": {
                    return NutsOptional.of(PATH);
                }
                case "OPTION": {
                    return NutsOptional.of(OPTION);
                }
                case "PALE": {
                    return NutsOptional.of(PALE);
                }
                case "OPERATOR": {
                    return NutsOptional.of(OPERATOR);
                }
                case "KW":
                case "KEYWORD": {
                    return NutsOptional.of(KEYWORD);
                }
                case "ITALIC": {
                    return NutsOptional.of(ITALIC);
                }
                case "INFORMATION":
                case "INFO": {
                    return NutsOptional.of(INFO);
                }
                case "FAIL": {
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
