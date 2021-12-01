///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// *
// * <br>
// * <p>
// * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
// * or agreed to in writing, software distributed under the License is
// * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
//package net.thevpc.nuts.runtime.standalone.text;
//
//import java.util.Objects;
//
//import net.thevpc.nuts.NutsSession;
//import net.thevpc.nuts.NutsTerminalCommand;
//import net.thevpc.nuts.runtime.standalone.text.renderer.AnsiStyleStyleApplierResolver;
//import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyle;
//import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyleStyleApplier;
//import net.thevpc.nuts.spi.NutsAnsiTermHelper;
//
///**
// * @author thevpc
// */
//public class DefaultAnsiEscapeCommand extends AnsiEscapeCommand implements AnsiStyleStyleApplier {
//
//    private final NutsTerminalCommand command;
//
//    public DefaultAnsiEscapeCommand(NutsTerminalCommand command) {
//        this.command = command;
//    }
//
//    public String getName() {
//        return command.getName();
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 89 * hash + Objects.hashCode(this.command);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final DefaultAnsiEscapeCommand other = (DefaultAnsiEscapeCommand) obj;
//        if (!Objects.equals(this.command, other.command)) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return getName() + "(" + command.getArgs() + ')';
//    }
//
//    @Override
//    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsSession session, AnsiStyleStyleApplierResolver applierResolver) {
//        switch (command.getName()) {
//            case NutsTerminalCommand.Ids.MOVE_LINE_START:
//            case NutsTerminalCommand.Ids.MOVE_TO:
//            case NutsTerminalCommand.Ids.MOVE_UP:
//            case NutsTerminalCommand.Ids.MOVE_DOWN:
//            case NutsTerminalCommand.Ids.MOVE_RIGHT:
//            case NutsTerminalCommand.Ids.MOVE_LEFT:
//            case NutsTerminalCommand.Ids.CLEAR_SCREEN:
//            case NutsTerminalCommand.Ids.CLEAR_SCREEN_FROM_CURSOR:
//            case NutsTerminalCommand.Ids.CLEAR_SCREEN_TO_CURSOR:
//            case NutsTerminalCommand.Ids.CLEAR_LINE:
//            case NutsTerminalCommand.Ids.CLEAR_LINE_FROM_CURSOR:
//            case NutsTerminalCommand.Ids.CLEAR_LINE_TO_CURSOR: {
//                String t = NutsAnsiTermHelper.of(session).command(command, session);
//                if (t != null) {
//                    return old.addCommand(t);
//                }
//                return old;
//            }
////            case NutsTerminalCommand.Ids.LATER_RESET_LINE: {
////                int tputCallTimeout = session.boot().getBootCustomArgument("---nuts.term.tput.call.timeout").getValue().getInt(60);
////                Integer w = session.boot().getBootCustomArgument("---nuts.term.width").getValue().getInt(null);
////                if (w == null) {
////                    NutsElement e = session.env().getProperty("nuts.term.tput.call.instance");
////                    NutsCachedValue<Integer> tput_cols = (NutsCachedValue) (e.isCustom() ? e.asCustom().getValue() : null);
////                    if (tput_cols == null) {
////                        tput_cols = new NutsCachedValue<>(new TputEvaluator(session), tputCallTimeout);
////                        session.env().setProperty("nuts.term.tput.call.instance", tput_cols);
////                    }
////                    w = tput_cols.getValue();
////                }
////                if (w == null) {
////                    w = 120;
////                }
////                if (w > 0) {
//////                return old.addLaterCommand("\u001b[1000D"
//////                        + CoreStringUtils.fillString(' ', w)
//////                        + "\u001b[1000D"
//////                );
////                    return old.addLaterCommand("\r"
////                            + CoreStringUtils.fillString(' ', w)
////                            + "\r"
////                    );
////                }
////                return old;
////            }
//        }
//        return old;
//    }
//
////    public static class TputEvaluator implements Supplier<Integer> {
////
////        private final NutsSession session;
////        boolean wasError = false;
////
////        public TputEvaluator(NutsSession session) {
////            this.session = session;
////        }
////
////        @Override
////        public Integer get() {
////            switch (session.env().getOsFamily()) {
////                case LINUX:
////                case UNIX:
////                case MACOS: {
////                    try {
////                        String d = session.exec()
////                                .setExecutionType(NutsExecutionType.SYSTEM)
////                                .grabOutputString()
////                                .setSession(session)
////                                .setFailFast(true)
////                                .addCommand("tput", "cols")
////                                .getOutputString();
////                        String s = d.trim();
////                        if (d.isEmpty()) {
////                            return null;
////                        }
////                        ;
////                        return Integer.parseInt(s);
////                    } catch (Exception ex) {
////                        wasError = true;
////                        return null;
////                    }
////                }
////            }
////            return null;
////        }
////    }
//
//}
