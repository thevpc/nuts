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
package net.thevpc.nuts.runtime.standalone.format.text.parser;

import net.thevpc.nuts.NutsTextNodeCommand;
import net.thevpc.nuts.NutsTextNodeType;
import net.thevpc.nuts.runtime.standalone.format.text.FPrintCommands;
import net.thevpc.nuts.runtime.standalone.format.text.TextFormat;
import net.thevpc.nuts.runtime.standalone.format.text.TextFormats;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeCommand extends NutsTextNodeSpecialBase implements NutsTextNodeCommand {

    private final String text;
    private final TextFormat style;

    public DefaultNutsTextNodeCommand(String start, String command, String separator, String end, String text, TextFormat style) {
        super(start, command, separator, end);
        this.text = text;
        this.style = style==null?parseTextFormat(text):style;
    }
    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.COMMAND;
    }

    @Override
    public String getText() {
        return text;
    }

    public TextFormat getStyle() {
        return style;
    }

    @Override
    public String toString() {
        return (getStart() + getKind() + getSeparator() + getText() + "(" + style + ")" + getEnd());
    }

    public static TextFormat parseTextFormat(String v){
        //this might be a command !!
        v = v.trim();
        switch (v) {
            case FPrintCommands.MOVE_LINE_START: {
                return TextFormats.MOVE_LINE_START;
            }
            case FPrintCommands.LATER_RESET_LINE: {
                return TextFormats.LATER_RESET_LINE;
            }
            case FPrintCommands.MOVE_UP: {
                return TextFormats.MOVE_UP;
            }
            default: {
                return null;
            }
        }
    }
}
