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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspace;
import net.thevpc.nuts.boot.NBootLogConfig;

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
public class NBootLog {

    /**
     * Universal Data and time format "yyyy-MM-dd HH:mm:ss.SSS"
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private NBootOptionsInfo options;
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    private Scanner inScanner;
    private PrintStream fileLogPrintStream;
    private int cachedTermLogLevel;
    private int cachedFileLogLevel;

    public NBootLog() {
        this(null);
    }

    public NBootLog(NBootOptionsInfo bootTerminal) {
        in = (bootTerminal == null || bootTerminal.getStdin() == null) ? System.in : bootTerminal.getStdin();
        out = (bootTerminal == null || bootTerminal.getStdout() == null) ? System.out : bootTerminal.getStdout();
        err = (bootTerminal == null || bootTerminal.getStderr() == null) ? System.out : bootTerminal.getStderr();
        cachedTermLogLevel = Level.OFF.intValue();
        cachedFileLogLevel = Level.OFF.intValue();
    }

    public void error(NBootMsg message, Throwable e) {
        log(Level.SEVERE, "FAIL",message,e);
    }
    public void warn(NBootMsg message) {
        log(Level.WARNING, "WARNING",message);
    }

    public void error(NBootMsg message) {
        log(Level.SEVERE, "FAIL",message);
    }
    public void log(Level lvl, String logVerb, NBootMsg message) {
        if (isLoggableTerm(lvl)) {
            doLogTerm(lvl, logVerb, message == null ? "" : message.toString());
        }
        if (isLoggableFile(lvl)) {
            doLogFile(lvl, logVerb, message == null ? "" : message.toString());
        }
    }

    public void log(Level level, String verb, Supplier<NBootMsg> msgSupplier, Supplier<Throwable> errorSupplier) {
        if (isLoggableTerm(level)) {
            log(level, verb, msgSupplier.get(), errorSupplier.get());
        }
    }

    public void log(LogRecord record) {
        String verb = null;
//        if (record instanceof NLogRecord) {
//            verb = ((NLogRecord) record).getVerb();
//        }
        Level level = record.getLevel();
        if (verb == null) {
            if (level.intValue() >= Level.SEVERE.intValue()) {
                verb = "FAIL";
            } else if (level.intValue() >= Level.WARNING.intValue()) {
                verb = "WARNING";
            } else if (level.intValue() >= Level.INFO.intValue()) {
                verb = "INFO";
            } else {
                verb = "INFO";
            }
        }
        log(level, verb, NBootMsg.ofPlain(record.getMessage()), record.getThrown());
    }

    public LogOp with() {
        return new LogOp(this);
    }

    public void log(Level lvl, String logVerb, NBootMsg message, Throwable err) {
        if (isLoggableTerm(lvl)) {
            doLogTerm(lvl, logVerb, message == null ? "" : message.toString());
            if (err != null) {
                err.printStackTrace(this.err);
            }
        }
        if (isLoggableFile(lvl)) {
            doLogFile(lvl, logVerb, message == null ? "" : message.toString());
            if (err != null && fileLogPrintStream != null) {
                err.printStackTrace(fileLogPrintStream);
            }
        }
    }

    private void doLogTerm(Level lvl, String logVerb, String s) {
        errln("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    private void doLogFile(Level lvl, String logVerb, String s) {
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
        if (options == null || options.getLogConfig() == null) {
            if (lvl.intValue() == Level.SEVERE.intValue()) {
                //this is a special case where we do log in all cases!
                return true;
            }
        }
        if (options.getBot() != null) {
            if (options.getBot()) {
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
        if (options == null || options.getLogConfig() == null) {
            return false;
        }
        return lvl.intValue() >= cachedFileLogLevel;

    }

    public boolean isLoggable(Level lvl) {
        return isLoggableTerm(lvl) || isLoggableFile(lvl);
    }

    public void setOptions(NBootOptionsInfo options) {
        this.options = options;
        NBootLogConfig nLogConfig = options.getLogConfig();
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
                if (name == null || NBootStringUtils.isBlank(name)) {
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
                                    String tempFolder = Paths.get(NBootPlatformHome.of(options.getStoreLayout()).getWorkspaceStore("TEMP", options.getWorkspace())).toString();
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
                if (folder == null || NBootStringUtils.isBlank(folder)) {
                    String logFolder = Paths.get(NBootPlatformHome.of(options.getStoreLayout()).getWorkspaceStore("LOG", options.getWorkspace())).toString();
                    folder = logFolder + "/" + NBootConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + NBootWorkspace.getVersion();
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
        this.err.printf(msg, p);
        this.err.printf("%n");
        this.err.flush();
    }

    public PrintStream err() {
        return this.err;
    }

    public void err(String msg, Object... p) {
        this.err.printf(msg, p);
        this.err.flush();
    }

    public void outln(String msg, Object... p) {
        this.out.println(NBootMsg.ofC(msg, p));
        this.out.flush();
    }

    public void errln(Throwable exception) {
        exception.printStackTrace(this.err);
    }

    public String readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }

    public static class LogOp {
        private NBootLog logger;
        private Level level = Level.FINE;
        private String verb;
        private NBootMsg msg;
        private long time;
        private Supplier<NBootMsg> msgSupplier;
        private Throwable error;

        public LogOp(NBootLog logger) {
            this.logger = logger;
        }

        public LogOp verbFail() {
            this.verb = "FAIL";
            return this;
        }
        public LogOp verbCache() {
            this.verb = "CACHE";
            return this;
        }
        public LogOp verbInfo() {
            this.verb = "INFO";
            return this;
        }
        public LogOp verbRead() {
            this.verb = "READ";
            return this;
        }
        public LogOp verbWarning() {
            this.verb = "WARNING";
            return this;
        }
        public LogOp verbSuccess() {
            this.verb = "SUCCESS";
            return this;
        }
        public LogOp verbStart() {
            this.verb = "START";
            return this;
        }

        public LogOp level(Level level) {
            this.level = level == null ? Level.FINE : level;
            return this;
        }


        public LogOp error(Throwable error) {
            this.error = error;
            return this;
        }


        public void log(NBootMsg message) {
            this.msg = message;
            run();
        }


        public void log(Supplier<NBootMsg> msgSupplier) {
            this.msgSupplier = msgSupplier;
            run();
        }


        public LogOp time(long time) {
            this.time = time;
            return this;
        }

        private void run() {
            if (logger.isLoggable(level)) {
                NBootMsg m = msg;
                if (msgSupplier != null) {
                    m = msgSupplier.get();
                }
                logger.log(level, verb, m, error);
            }
        }

        public boolean isLoggable(Level level) {
            return logger.isLoggable(level);
        }
    }
}
