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
package net.thevpc.nuts;

import java.util.Objects;

/**
 *
 * @author vpc
 */
public final class NutsTerminalCommand {

    public static final NutsTerminalCommand LATER_RESET_LINE = new NutsTerminalCommand(Ids.LATER_RESET_LINE);
    public static final NutsTerminalCommand MOVE_LINE_START = new NutsTerminalCommand(Ids.MOVE_LINE_START);
    public static final NutsTerminalCommand CLEAR_SCREEN = new NutsTerminalCommand(Ids.CLEAR_SCREEN);
    public static final NutsTerminalCommand CLEAR_SCREEN_TO_CURSOR = new NutsTerminalCommand(Ids.CLEAR_SCREEN_FROM_CURSOR);
    public static final NutsTerminalCommand CLEAR_SCREEN_FROM_CURSOR = new NutsTerminalCommand(Ids.CLEAR_SCREEN_FROM_CURSOR);
    public static final NutsTerminalCommand CLEAR_LINE = new NutsTerminalCommand(Ids.CLEAR_LINE);
    public static final NutsTerminalCommand CLEAR_LINE_TO_CURSOR = new NutsTerminalCommand(Ids.CLEAR_LINE_FROM_CURSOR);
    public static final NutsTerminalCommand CLEAR_LINE_FROM_CURSOR = new NutsTerminalCommand(Ids.CLEAR_LINE_FROM_CURSOR);
    public static final NutsTerminalCommand MOVE_UP = MOVE_UP(1);
    public static final NutsTerminalCommand MOVE_DOWN = MOVE_DOWN(1);
    public static final NutsTerminalCommand MOVE_LEFT = MOVE_LEFT(1);
    public static final NutsTerminalCommand MOVE_RIGHT = MOVE_RIGHT(1);
    private final String name;
    private final String args;

    public NutsTerminalCommand(String name) {
        this(name, "");
    }

    public NutsTerminalCommand(String name, String args) {
        this.name = name;
        this.args = args;
    }

    public static final NutsTerminalCommand MOVE_TO(int row, int col) {
        return new NutsTerminalCommand(Ids.MOVE_TO, row + "," + col);
    }

    public static final NutsTerminalCommand MOVE_RIGHT(int count) {
        return new NutsTerminalCommand(Ids.MOVE_RIGHT, count <= 0 ? "1" : String.valueOf(count));
    }

    public static final NutsTerminalCommand MOVE_LEFT(int count) {
        return new NutsTerminalCommand(Ids.MOVE_LEFT, count <= 0 ? "1" : String.valueOf(count));
    }

    public static final NutsTerminalCommand MOVE_UP(int count) {
        return new NutsTerminalCommand(Ids.MOVE_UP, count <= 0 ? "1" : String.valueOf(count));
    }

    public static final NutsTerminalCommand MOVE_DOWN(int count) {
        return new NutsTerminalCommand(Ids.MOVE_DOWN, count <= 0 ? "1" : String.valueOf(count));
    }

    public static NutsTerminalCommand of(String name) {
        return of(name, "");
    }

    public static NutsTerminalCommand of(String name, String args) {
        if (args.trim().isEmpty()) {
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
        }

        return new NutsTerminalCommand(name, args);
    }

    public String getName() {
        return name;
    }

    public String getArgs() {
        return args;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsTerminalCommand other = (NutsTerminalCommand) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.args, other.args);
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
        private Ids() {
        }

    }


}
