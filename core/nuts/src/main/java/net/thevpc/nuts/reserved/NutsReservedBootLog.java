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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLogConfig;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author thevpc
 * @app.category Internal
 */
public class NutsReservedBootLog implements NutsLogger {

    /**
     * Universal Data and time format "yyyy-MM-dd HH:mm:ss.SSS"
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private NutsWorkspaceOptions options;
    private final NutsWorkspaceTerminalOptions bootTerminal;
    private Scanner inScanner;

    public NutsReservedBootLog() {
        this(null);
    }
    public NutsReservedBootLog(NutsWorkspaceTerminalOptions bootTerminal) {
        InputStream in = (bootTerminal == null || bootTerminal.getIn() == null) ? System.in : bootTerminal.getIn();
        PrintStream out = (bootTerminal == null || bootTerminal.getOut() == null) ? System.out : bootTerminal.getOut();
        PrintStream err = (bootTerminal == null || bootTerminal.getErr() == null) ? System.out : bootTerminal.getErr();
        this.bootTerminal = new NutsWorkspaceTerminalOptions(in, out, err);
    }

    public void log(Level lvl, NutsLoggerVerb logVerb, NutsMessage message) {
        if (isLoggable(lvl)) {
            doLog(lvl, logVerb, message == null ? "" : message.toString());
        }
    }

    @Override
    public void log(NutsSession session, Level level, NutsLoggerVerb verb, NutsMessage msg, Throwable thrown) {
        log(level, msg, thrown);
    }

    @Override
    public void log(NutsSession session, Level level, NutsLoggerVerb verb, Supplier<NutsMessage> msgSupplier, Supplier<Throwable> errorSupplier) {
        if (isLoggable(level)) {
            log(level, msgSupplier.get(), errorSupplier.get());
        }
    }

    @Override
    public void log(LogRecord record) {
        log(record.getLevel(),NutsMessage.ofPlain(record.getMessage()),record.getThrown());
    }

    @Override
    public NutsLoggerOp with() {
        return new LoggerOp(this);
    }

    public void log(Level lvl, NutsMessage message, Throwable err) {
        if (isLoggable(lvl)) {
            doLog(lvl, NutsLoggerVerb.FAIL, message == null ? "" : message.toString());
            if(err!=null) {
                err.printStackTrace(bootTerminal.getErr());
            }
        }
    }

    private void doLog(Level lvl, NutsLoggerVerb logVerb, String s) {
        errln("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    public boolean isLoggable(Level lvl) {
        if (lvl.intValue() == Level.OFF.intValue()) {
            //this is a special case where we do log in all cases!
            return true;
        }
        if (options == null || options.getLogConfig() == null) {
            return false;
        }
        return lvl.intValue() >= options.getLogConfig().orElseGet(NutsLogConfig::new)
                .getLogTermLevel().intValue();
    }

    public void setOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }


    void errln(String msg, Object... p) {
        this.bootTerminal.getErr().printf(msg, p);
        this.bootTerminal.getErr().printf("%n");
        this.bootTerminal.getErr().flush();
    }

    public PrintStream err() {
        return bootTerminal.getErr();
    }

    public void err(String msg, Object... p) {
        this.bootTerminal.getErr().printf(msg, p);
        this.bootTerminal.getErr().flush();
    }

    public void outln(String msg, Object... p) {
        this.bootTerminal.getOut().printf(msg, p);
        this.bootTerminal.getOut().printf("%n");
        this.bootTerminal.getOut().flush();
    }

    public void errln(Throwable exception) {
        exception.printStackTrace(this.bootTerminal.getErr());
    }

    public String readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }

    private static class LoggerOp implements NutsLoggerOp {
        private NutsSession session;
        private NutsReservedBootLog logger;
        private Level level = Level.FINE;
        private NutsLoggerVerb verb;
        private NutsMessage msg;
        private long time;
        private Supplier<NutsMessage> msgSupplier;
        private Throwable error;

        public LoggerOp(NutsReservedBootLog logger) {
            this.logger = logger;
        }

        public NutsSession getSession() {
            return session;
        }

        @Override
        public NutsLoggerOp session(NutsSession session) {
            this.session = session;
            return this;
        }

        @Override
        public NutsLoggerOp verb(NutsLoggerVerb verb) {
            this.verb = verb;
            return this;
        }

        @Override
        public NutsLoggerOp level(Level level) {
            this.level = level == null ? Level.FINE : level;
            return this;
        }

        @Override
        public NutsLoggerOp error(Throwable error) {
            this.error = error;
            return this;
        }

        @Override
        public void log(NutsMessage message) {
            this.msg = message;
            run();
        }

        @Override
        public void log(Supplier<NutsMessage> msgSupplier) {
            this.msgSupplier = msgSupplier;
            run();
        }

        @Override
        public NutsLoggerOp time(long time) {
            this.time = time;
            return this;
        }

        private void run() {
            if (logger.isLoggable(level)) {
                NutsMessage m = msg;
                if (msgSupplier != null) {
                    m = msgSupplier.get();
                }
                logger.log(level,m,error);
            }
        }

        @Override
        public boolean isLoggable(Level level) {
            return logger.isLoggable(level);
        }
    }
}
