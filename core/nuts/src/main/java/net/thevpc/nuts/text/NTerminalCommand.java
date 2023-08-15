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

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.reserved.NReservedCollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
public final class NTerminalCommand {

    public static final NTerminalCommand LATER_RESET_LINE = new NTerminalCommand(Ids.LATER_RESET_LINE);

    public static final NTerminalCommand MOVE_LINE_START = new NTerminalCommand(Ids.MOVE_LINE_START);
    public static final NTerminalCommand CLEAR_SCREEN = new NTerminalCommand(Ids.CLEAR_SCREEN);
    public static final NTerminalCommand CLEAR_SCREEN_TO_CURSOR = new NTerminalCommand(Ids.CLEAR_SCREEN_FROM_CURSOR);
    public static final NTerminalCommand CLEAR_SCREEN_FROM_CURSOR = new NTerminalCommand(Ids.CLEAR_SCREEN_FROM_CURSOR);
    public static final NTerminalCommand CLEAR_LINE = new NTerminalCommand(Ids.CLEAR_LINE);
    public static final NTerminalCommand CLEAR_LINE_TO_CURSOR = new NTerminalCommand(Ids.CLEAR_LINE_FROM_CURSOR);
    public static final NTerminalCommand CLEAR_LINE_FROM_CURSOR = new NTerminalCommand(Ids.CLEAR_LINE_FROM_CURSOR);
    public static final NTerminalCommand GET_SIZE = new NTerminalCommand(Ids.GET_SIZE);
    public static final NTerminalCommand GET_CURSOR = new NTerminalCommand(Ids.GET_CURSOR);
    public static final NTerminalCommand MOVE_UP = MOVE_UP(1);
    public static final NTerminalCommand MOVE_DOWN = MOVE_DOWN(1);
    public static final NTerminalCommand MOVE_LEFT = MOVE_LEFT(1);
    public static final NTerminalCommand MOVE_RIGHT = MOVE_RIGHT(1);
    private final String name;
    private final List<String> args;

    public NTerminalCommand(String name) {
        this(name, Collections.emptyList());
    }

    public NTerminalCommand(String name, List<String> args) {
        this.name = name;
        this.args = NReservedCollectionUtils.unmodifiableList(args);
    }

    public static NTerminalCommand MOVE_TO(int row, int col) {
        return new NTerminalCommand(Ids.MOVE_TO, Arrays.asList(String.valueOf(row), String.valueOf(col)));
    }

    public static NTerminalCommand MOVE_RIGHT(int count) {
        return new NTerminalCommand(Ids.MOVE_RIGHT, Arrays.asList(count <= 0 ? "1" : String.valueOf(count)));
    }

    public static NTerminalCommand MOVE_LEFT(int count) {
        return new NTerminalCommand(Ids.MOVE_LEFT, Arrays.asList(count <= 0 ? "1" : String.valueOf(count)));
    }

    public static NTerminalCommand MOVE_UP(int count) {
        return new NTerminalCommand(Ids.MOVE_UP, Arrays.asList(count <= 0 ? "1" : String.valueOf(count)));
    }

    public static NTerminalCommand MOVE_DOWN(int count) {
        return new NTerminalCommand(Ids.MOVE_DOWN, Arrays.asList(count <= 0 ? "1" : String.valueOf(count)));
    }

    public static NTerminalCommand of(String name) {
        return of(name, "");
    }

    public static NTerminalCommand of(String name, String... args) {
        if (args == null || args.length == 0) {
            switch (name) {
                case Ids.LATER_RESET_LINE:
                    return LATER_RESET_LINE;
                case Ids.MOVE_LINE_START:
                    return MOVE_LINE_START;
                case Ids.CLEAR_SCREEN:
                    return CLEAR_SCREEN;
                case Ids.CLEAR_SCREEN_TO_CURSOR:
                    return CLEAR_SCREEN_TO_CURSOR;
                case Ids.CLEAR_SCREEN_FROM_CURSOR:
                    return CLEAR_SCREEN_FROM_CURSOR;
                case Ids.CLEAR_LINE:
                    return CLEAR_LINE;
                case Ids.CLEAR_LINE_TO_CURSOR:
                    return CLEAR_LINE_TO_CURSOR;
                case Ids.CLEAR_LINE_FROM_CURSOR:
                    return CLEAR_LINE_FROM_CURSOR;
                case Ids.MOVE_UP:
                    return MOVE_UP;
                case Ids.MOVE_DOWN:
                    return MOVE_DOWN;
                case Ids.MOVE_LEFT:
                    return MOVE_LEFT;
                case Ids.MOVE_RIGHT:
                    return MOVE_RIGHT;
            }
        }
        // if no arg command still return default!
        switch (name) {
            case Ids.LATER_RESET_LINE:
                return LATER_RESET_LINE;
            case Ids.MOVE_LINE_START:
                return MOVE_LINE_START;
            case Ids.CLEAR_SCREEN:
                return CLEAR_SCREEN;
            case Ids.CLEAR_SCREEN_TO_CURSOR:
                return CLEAR_SCREEN_TO_CURSOR;
            case Ids.CLEAR_SCREEN_FROM_CURSOR:
                return CLEAR_SCREEN_FROM_CURSOR;
            case Ids.CLEAR_LINE:
                return CLEAR_LINE;
            case Ids.CLEAR_LINE_TO_CURSOR:
                return CLEAR_LINE_TO_CURSOR;
            case Ids.CLEAR_LINE_FROM_CURSOR:
                return CLEAR_LINE_FROM_CURSOR;
            case Ids.MOVE_UP:
                return MOVE_UP(NLiteral.of(args[0]).asInt().orElse(1));
            case Ids.MOVE_DOWN:
                return MOVE_DOWN(NLiteral.of(args[0]).asInt().orElse(1));
            case Ids.MOVE_LEFT:
                return MOVE_LEFT(NLiteral.of(args[0]).asInt().orElse(1));
            case Ids.MOVE_RIGHT:
                return MOVE_RIGHT(NLiteral.of(args[0]).asInt().orElse(1));
        }

        return new NTerminalCommand(name, args == null ? null : Arrays.asList(args));
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NTerminalCommand that = (NTerminalCommand) o;
        return Objects.equals(name, that.name) && Objects.equals(args, that.args);
    }

    @Override
    public String toString() {
        return "NutsTerminalCommand{" + "name=" + name + ", args=" + args + '}';
    }

    public static final class Ids {

        public static final String LATER_RESET_LINE = ("later-reset-line");

        public static final String MOVE_LINE_START = ("move-line-start");
        public static final String MOVE_TO = ("move-to");
        public static final String MOVE_UP = ("move-up");
        public static final String MOVE_DOWN = ("move-down");
        public static final String MOVE_LEFT = ("move-left");
        public static final String MOVE_RIGHT = ("move-right");
        public static final String CLEAR_SCREEN = ("clear-screen");
        public static final String CLEAR_SCREEN_TO_CURSOR = ("clear-screen-to-cursor");
        public static final String CLEAR_SCREEN_FROM_CURSOR = ("clear-screen-from-cursor");
        public static final String CLEAR_LINE = ("clear-line");
        public static final String CLEAR_LINE_TO_CURSOR = ("clear-line-to-cursor");
        public static final String CLEAR_LINE_FROM_CURSOR = ("clear-line-from-cursor");
        public static final String GET_SIZE = ("get-size");
        public static final String GET_CURSOR = ("get-cursor");

        private Ids() {
        }

    }


}
