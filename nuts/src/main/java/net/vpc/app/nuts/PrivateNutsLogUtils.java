/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * Log util helper
 *
 * @author thevpc creation-date 9/16/12 10:00 PM
 * @since 0.5.4
 */
final class PrivateNutsLogUtils {

    private static boolean verboseLog = PrivateNutsUtils.getSysBoolNutsProperty("log.verbose", false);
    public static final Formatter LOG_FORMATTER = new LogFormatter();
    public static final Filter NUTS_LOG_FILTER = new Filter() {
        @Override
        public boolean isLoggable(LogRecord record) {
            String loggerName = record == null ? "" : PrivateNutsUtils.trim(record.getLoggerName());
            return loggerName.startsWith("net.vpc.app.nuts");
        }
    };

    private PrivateNutsLogUtils() {
    }

    public static void bootstrap(NutsLogConfig config) {
        if (config == null) {
            return;
        }
        prepare(config, System.getProperty("user.home"), true);
    }

    public static void prepare(NutsLogConfig config, String defaultLogFolder) {
        prepare(config, defaultLogFolder, false);
    }

    public static void prepare(NutsLogConfig config, String defaultLogFolder, boolean consoleOnly) {
        if (config == null) {
            return;
        }
        Level level = config.getLogLevel();
        String folder = config.getLogFolder();
        String name = config.getLogName();
        int maxSize = config.getLogSize();
        int count = config.getLogCount();
        boolean inheritLog = config.isLogInherited();

        Logger olderLog = Logger.getLogger(PrivateNutsLogUtils.class.getName());
        boolean acceptConsole=false;
        boolean loggedToFile = false;
        boolean loggedToConsole = false;
        String rootPackage = "net.vpc.app.nuts";
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (name == null || PrivateNutsUtils.isBlank(name)) {
            name = Instant.now().toString().replace(":", "") + "-nuts-%g.log";
        }
        if (folder == null || PrivateNutsUtils.isBlank(folder)) {
            folder = defaultLogFolder;
        }
        String pattern = (folder + "/" + name).replace('/', File.separatorChar);
        if (maxSize <= 0) {
            maxSize = 5;
        }
        if (count <= 0) {
            count = 3;
        }
        boolean updatedHandler = false;
        boolean updatedLoglevel = false;
        String[] pckArray = rootPackage.split("[.]");
        Logger[] rootLoggers = new Logger[pckArray.length + 1];
        rootLoggers[0] = Logger.getLogger("");
        for (int i = 0; i < pckArray.length; i++) {
            StringBuilder sb = new StringBuilder(rootPackage.length());
            for (int j = 0; j <= i; j++) {
                if (j > 0) {
                    sb.append(".");
                }
                sb.append(pckArray[j]);
            }
            rootLoggers[i + 1] = Logger.getLogger(sb.toString());
        }
        Logger rootLogger = rootLoggers[rootLoggers.length - 1];
        boolean found = false;
        for (int i = 0; i < rootLoggers.length - 1; i++) {
            Logger logger = rootLoggers[i];
            Level oldLevel = logger.getLevel();
            if (oldLevel == null || oldLevel.intValue() < level.intValue()) {
                logger.setLevel(level);
            }
        }
        if (!inheritLog) {
            rootLogger.setUseParentHandlers(false);
        }
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof NutsLogFileHandler) {
                NutsLogFileHandler mh = (NutsLogFileHandler) handler;
                if (mh.pattern.equals(pattern) && mh.count == count && mh.limit == maxSize * MEGA) {
                    found = true;
                } else {
                    if (!loggedToFile) {
                        loggedToFile = true;
                        olderLog.log(Level.CONFIG, "[SUCCESS] Switching log to file {0}", new Object[]{pattern});
                    }
                    rootLogger.removeHandler(mh);
                    mh.close();
                }
            } else if (handler instanceof NutsLogConsoleHandler) {
                loggedToConsole = true;
            }
        }
        if (!found) {
            if (!loggedToFile) {
                loggedToFile = true;
                olderLog.log(Level.CONFIG, "[SUCCESS] Switching log to file {0}", new Object[]{pattern});
            }
            File parentFile = new File(pattern).getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            updatedHandler = true;
            Handler handler;
            boolean consoleAdded = false;
            try {
                if (!consoleOnly && acceptConsole) {
                    handler = new NutsLogFileHandler(pattern, maxSize * MEGA, count, true);
                    rootLogger.addHandler(handler);
                }
            } catch (Exception ex) {
                if (!loggedToConsole) {
                    handler = new NutsLogConsoleHandler();
                    handler.setFormatter(LOG_FORMATTER);
                    rootLogger.setUseParentHandlers(false);
                    rootLogger.addHandler(handler);
                    rootLogger.log(Level.SEVERE, "Unable to set log File. Fallback to console log : {0}", ex.toString());
                    consoleAdded = true;
                }
            }
            if (!loggedToConsole && !consoleAdded && (Level.FINEST.equals(level) || Level.ALL.equals(level))) {
                handler = new NutsLogConsoleHandler();
                handler.setFormatter(LOG_FORMATTER);
                rootLogger.setUseParentHandlers(false);
                rootLogger.addHandler(handler);
            }
        }

        if (updatedHandler) {
            Level oldLevel = rootLogger.getLevel();
            if (oldLevel == null || !oldLevel.equals(level)) {
                updatedLoglevel = true;
                rootLogger.setLevel(level);
            }
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof NutsLogFileHandler || handler instanceof NutsLogConsoleHandler) {
                    oldLevel = handler.getLevel();
                    if (oldLevel == null || !oldLevel.equals(level)) {
                        updatedLoglevel = true;
                        handler.setLevel(level);
                    }
                    setFormatter(handler);
                    Filter oldFilter = handler.getFilter();
                    if (oldFilter == null || oldFilter != NUTS_LOG_FILTER) {
                        updatedLoglevel = true;
                        handler.setFilter(NUTS_LOG_FILTER);
                    }
                }
            }
        }
//        if (updatedHandler || updatedLoglevel) {
//            olderLog.log(Level.CONFIG, "[SUCCESS] Switching log to file {0}", new Object[]{pattern});
//        }
    }

    private static void setFormatter(Handler handler) {
        boolean updatedLoglevel;
        Formatter oldLogFormatter = handler.getFormatter();
        if (oldLogFormatter == null || oldLogFormatter != LOG_FORMATTER) {
            updatedLoglevel = true;
            handler.setFormatter(LOG_FORMATTER);
        }
    }

    private static final class NutsLogConsoleHandler extends ConsoleHandler {

    }

    private static final class NutsLogFileHandler extends FileHandler {

        String pattern;
        int limit;
        int count;

        public NutsLogFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
            super(pattern, limit, count, append);
            this.pattern = pattern;
            this.limit = limit;
            this.count = count;
        }
    }

    private static final class LogFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        private final Map<Level, String> logLevelCache = new HashMap<>();
        private final Map<String, String> classNameCache = new HashMap<>();
        private long lastMillis = -1;

        private String logLevel(Level l) {
            String v = logLevelCache.get(l);
            if (v == null) {
                StringBuilder sb = new StringBuilder(l.getName());
                ensureSize(sb, 6);
                v = sb.toString();
                logLevelCache.put(l, v);
            }
            return v;
        }

        private void ensureSize(StringBuilder sb, int size) {
            sb.ensureCapacity(size);
            int length = size - sb.length();
            while (length > 0) {
                if (length >= 16) {
                    sb.append("                ");
                    length -= 16;
                } else if (length >= 8) {
                    sb.append("        ");
                    length -= 8;
                } else if (length >= 4) {
                    sb.append("    ");
                    length -= 4;
                } else if (length >= 2) {
                    sb.append("  ");
                    length -= 2;
                } else {
                    sb.append(' ');
                    length--;
                }
            }
        }

        private String formatClassName(String className) {
            if (className == null) {
                return "";
            }
            String v = classNameCache.get(className);
            if (v == null) {
                int maxSize = 47;
                StringBuilder sb = new StringBuilder(maxSize);
                int pos = 0, end;
                while ((end = className.indexOf('.', pos)) >= 0) {
                    sb.append(className.charAt(pos)).append('.');
                    pos = end + 1;
                }
                sb.append(className.substring(pos));
                ensureSize(sb, maxSize);
                v = sb.toString();
                classNameCache.put(className, v);
            }
            return v;
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            String date = Instant.ofEpochMilli(record.getMillis()).toString().replace(":", "");

            sb.append(date);
            for (int i = 22 - date.length()-1; i >= 0; i--) {
                sb.append(' ');
            }
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(record.getMillis() - lastMillis);
                }
                ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(logLevel(record.getLevel()))
                    .append(" ")
                    .append(formatClassName(record.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(record))
                    .append(LINE_SEPARATOR);
            lastMillis = record.getMillis();
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    try (PrintWriter pw = new PrintWriter(sw)) {
                        record.getThrown().printStackTrace(pw);
                    }
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // ignore
                }
            }

            return sb.toString();
        }
    }
}
