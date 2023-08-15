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
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author thevpc
 * @app.category Internal
 */
public class NReservedBootLog implements NLog {

    /**
     * Universal Data and time format "yyyy-MM-dd HH:mm:ss.SSS"
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private NWorkspaceOptions options;
    private final NWorkspaceTerminalOptions bootTerminal;
    private Scanner inScanner;
    private PrintStream fileLogPrintStream;
    private int cachedTermLogLevel;
    private int cachedFileLogLevel;

    public NReservedBootLog() {
        this(null);
    }

    public NReservedBootLog(NWorkspaceTerminalOptions bootTerminal) {
        InputStream in = (bootTerminal == null || bootTerminal.getIn() == null) ? System.in : bootTerminal.getIn();
        PrintStream out = (bootTerminal == null || bootTerminal.getOut() == null) ? System.out : bootTerminal.getOut();
        PrintStream err = (bootTerminal == null || bootTerminal.getErr() == null) ? System.out : bootTerminal.getErr();
        this.bootTerminal = new NWorkspaceTerminalOptions(in, out, err);
        cachedTermLogLevel = Level.OFF.intValue();
        cachedFileLogLevel = Level.OFF.intValue();
    }

    public void log(Level lvl, NLogVerb logVerb, NMsg message) {
        if (isLoggableTerm(lvl)) {
            doLogTerm(lvl, logVerb, message == null ? "" : message.toString());
        }
        if (isLoggableFile(lvl)) {
            doLogFile(lvl, logVerb, message == null ? "" : message.toString());
        }
    }

    @Override
    public void log(NSession session, Level level, NLogVerb verb, NMsg msg, Throwable thrown) {
        log(level, verb, msg, thrown);
    }

    @Override
    public void log(NSession session, Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier) {
        if (isLoggableTerm(level)) {
            log(level, verb, msgSupplier.get(), errorSupplier.get());
        }
    }

    @Override
    public void log(LogRecord record) {
        NLogVerb verb = null;
        if (record instanceof NLogRecord) {
            verb = ((NLogRecord) record).getVerb();
        }
        Level level = record.getLevel();
        if (verb == null) {
            if (level.intValue() >= Level.SEVERE.intValue()) {
                verb = NLogVerb.FAIL;
            } else if (level.intValue() >= Level.WARNING.intValue()) {
                verb = NLogVerb.WARNING;
            } else if (level.intValue() >= Level.INFO.intValue()) {
                verb = NLogVerb.INFO;
            } else {
                verb = NLogVerb.INFO;
            }
        }
        log(level, verb, NMsg.ofPlain(record.getMessage()), record.getThrown());
    }

    @Override
    public NLogOp with() {
        return new LogOp(this);
    }

    public void log(Level lvl, NLogVerb logVerb, NMsg message, Throwable err) {
        if (isLoggableTerm(lvl)) {
            doLogTerm(lvl, logVerb, message == null ? "" : message.toString());
            if (err != null) {
                err.printStackTrace(bootTerminal.getErr());
            }
        }
        if (isLoggableFile(lvl)) {
            doLogFile(lvl, logVerb, message == null ? "" : message.toString());
            if (err != null && fileLogPrintStream != null) {
                err.printStackTrace(fileLogPrintStream);
            }
        }
    }

    private void doLogTerm(Level lvl, NLogVerb logVerb, String s) {
        errln("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    private void doLogFile(Level lvl, NLogVerb logVerb, String s) {
        if (fileLogPrintStream != null) {
            fileLogPrintStream.printf("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
            fileLogPrintStream.println();
            fileLogPrintStream.flush();
        }
    }

    public boolean isLoggableTerm(Level lvl) {
        if (lvl.intValue() == Level.OFF.intValue()) {
            //this is a special case where we do log in all cases!
            return true;
        }
        if (options == null || options.getLogConfig().orNull() == null) {
            return false;
        }
        if (options.getBot().orNull() != null) {
            if (options.getBot().get()) {
                return false;
            }
        }
        return lvl.intValue() >= cachedTermLogLevel;
    }

    public boolean isLoggableFile(Level lvl) {
        if (fileLogPrintStream == null) {
            return false;
        }
        if (lvl.intValue() == Level.OFF.intValue()) {
            //this is a special case where we do log in all cases!
            return true;
        }
        if (options == null || options.getLogConfig().orNull() == null) {
            return false;
        }
        return lvl.intValue() >= cachedFileLogLevel;

    }

    public boolean isLoggable(Level lvl) {
        return isLoggableTerm(lvl) || isLoggableFile(lvl);
    }

    public void setOptions(NWorkspaceOptions options) {
        this.options = options;
        NLogConfig nLogConfig = options.getLogConfig().orNull();
        if (nLogConfig != null) {
            if (nLogConfig.getLogTermLevel() != null) {
                cachedTermLogLevel = nLogConfig.getLogTermLevel().intValue();
            }
            if (nLogConfig.getLogFileLevel() != null) {
                cachedFileLogLevel = nLogConfig.getLogFileLevel().intValue();
            }
            {
                Level level = nLogConfig.getLogFileLevel();
                String folder = nLogConfig.getLogFileBase();
                String name = nLogConfig.getLogFileName();
                int maxSize = nLogConfig.getLogFileSize();
                int count = nLogConfig.getLogFileCount();
//        String rootPackage = "net.thevpc.nuts";
                if (level == null) {
                    level = Level.INFO;
                }
                int MEGA = 1024 * 1024;
                if (name == null || NBlankable.isBlank(name)) {
                    name = Instant.now().toString().replace(":", "") + "-nuts-%g.log";
                }
                StringBuilder realName = new StringBuilder();
                char[] charArray = name.toCharArray();
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    switch (c) {
                        case '%': {
                            if (i + 1 < charArray.length) {
                                i++;
                            }
                            switch (charArray[i]) {
                                case '/': {
                                    realName.append(File.separatorChar);
                                    break;
                                }
                                case '%': {
                                    realName.append('%');
                                    break;
                                }
                                case 'g': {
                                    //rotation is not supported!!
                                    realName.append("boot");
                                    break;
                                }
                                case 't': {
                                    String tempFolder = Paths.get(NPlatformHome.of(options.getStoreLayout().orNull()).getWorkspaceStore(NStoreType.TEMP, options.getWorkspace().orNull())).toString();
                                    realName.append(tempFolder);
                                    break;
                                }
                                case 'u': {
                                    realName.append(UUID.randomUUID());
                                    break;
                                }
                                default: {
                                    realName.append('%');
                                    realName.append(charArray[i]);
                                }
                            }
                            break;
                        }
                        default: {
                            realName.append(c);
                        }
                    }
                }
                if (folder == null || NBlankable.isBlank(folder)) {
                    String logFolder = Paths.get(NPlatformHome.of(options.getStoreLayout().orNull()).getWorkspaceStore(NStoreType.LOG, options.getWorkspace().orNull())).toString();
                    folder = logFolder + "/" + NConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + Nuts.getVersion();
                }
                String pattern = (folder + "/" + realName).replace('/', File.separatorChar);
                if (maxSize <= 0) {
                    maxSize = 5;
                }
                if (count <= 0) {
                    count = 3;
                }
                File parentFile = new File(pattern).getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                boolean append = true;
                String realPath = pattern;
                long realMaxSizeInBytes = maxSize * MEGA;
                //always create a new file
                try {
                    fileLogPrintStream = new PrintStream(new FileOutputStream(realPath, append));
                } catch (FileNotFoundException e) {
                    //just ignore, not file log will be handled...
                }
            }
        }
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
        this.bootTerminal.getOut().println(NMsg.ofC(msg, p));
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

    private static class LogOp implements NLogOp {
        private NSession session;
        private NReservedBootLog logger;
        private Level level = Level.FINE;
        private NLogVerb verb;
        private NMsg msg;
        private long time;
        private Supplier<NMsg> msgSupplier;
        private Throwable error;

        public LogOp(NReservedBootLog logger) {
            this.logger = logger;
        }

        public NSession getSession() {
            return session;
        }

        @Override
        public NLogOp session(NSession session) {
            this.session = session;
            return this;
        }

        @Override
        public NLogOp verb(NLogVerb verb) {
            this.verb = verb;
            return this;
        }

        @Override
        public NLogOp level(Level level) {
            this.level = level == null ? Level.FINE : level;
            return this;
        }

        @Override
        public NLogOp error(Throwable error) {
            this.error = error;
            return this;
        }

        @Override
        public void log(NMsg message) {
            this.msg = message;
            run();
        }

        @Override
        public void log(Supplier<NMsg> msgSupplier) {
            this.msgSupplier = msgSupplier;
            run();
        }

        @Override
        public NLogOp time(long time) {
            this.time = time;
            return this;
        }

        private void run() {
            if (logger.isLoggable(level)) {
                NMsg m = msg;
                if (msgSupplier != null) {
                    m = msgSupplier.get();
                }
                logger.log(level, verb, m, error);
            }
        }

        @Override
        public boolean isLoggable(Level level) {
            return logger.isLoggable(level);
        }
    }
}
