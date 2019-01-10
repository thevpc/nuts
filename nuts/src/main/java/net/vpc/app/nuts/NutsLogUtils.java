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
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * Log util helper
 *
 * @author Taha BEN SALAH <taha.bensalah@gmail.com>
 * @creationdate 9/16/12 10:00 PM
 */
public final class NutsLogUtils {

    public static final LogFormatter LOG_FORMATTER = new LogFormatter();
    public static final Filter LOG_FILTER = new Filter() {
        @Override
        public boolean isLoggable(LogRecord record) {
            String loggerName = record == null ? "" : NutsUtils.trim(record.getLoggerName());
            return loggerName.startsWith("net.vpc.app.nuts");
        }
    };

    private NutsLogUtils() {
    }

    public static void prepare(NutsLogConfig config, String defaultLogFolder) {
        if(config==null){
            return;
        }
        Level level=config.getLogLevel();
        String folder=config.getLogFolder();
        String name=config.getLogName();
        int maxSize=config.getLogSize();
        int count=config.getLogCount();
        boolean inheritLog=config.isLogInherited();

        Logger olderLog = Logger.getLogger(NutsLogUtils.class.getName());
        boolean logged = false;
        String rootPackage = "net.vpc.app.nuts";
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (name == null || NutsUtils.isEmpty(name)) {
            name = "nuts-%g.log";
        }
        if (folder == null || NutsUtils.isEmpty(folder)) {
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
        String[] splitted = rootPackage.split("\\.");
        Logger[] rootLoggers = new Logger[splitted.length + 1];
        rootLoggers[0] = Logger.getLogger("");
        for (int i = 0; i < splitted.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                if (j > 0) {
                    sb.append(".");
                }
                sb.append(splitted[j]);
            }
            rootLoggers[i + 1] = Logger.getLogger(sb.toString());
        }
        Logger rootLogger = rootLoggers[rootLoggers.length - 1];
        boolean found = false;
        for (int i = 0; i < rootLoggers.length - 1; i++) {
            Logger logger = rootLoggers[i];
            Level oldLevel = logger.getLevel();
            if (oldLevel == null || oldLevel.intValue()<level.intValue()) {
                logger.setLevel(level);
            }
        }
        if(!inheritLog){
            rootLogger.setUseParentHandlers(false);
        }
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof MyFileHandler) {
                MyFileHandler mh = (MyFileHandler) handler;
                if (mh.pattern.equals(pattern) && mh.count == count && mh.limit == maxSize * MEGA) {
                    found = true;
                } else {
                    if (!logged) {
                        logged = true;
                        olderLog.log(Level.CONFIG, "Switching log config to file {0}", new Object[]{pattern});
                    }
                    rootLogger.removeHandler(mh);
                    mh.close();
                }
            }else if(handler instanceof MyConsoleHandler){
                logged = true;
            }
        }
        if (!found) {
            if (!logged) {
                logged = true;
                olderLog.log(Level.CONFIG, "Switching log config to file {0}", new Object[]{pattern});
            }
            File parentFile = new File(pattern).getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            updatedHandler = true;
            Handler handler = null;
            try {
                handler = new MyFileHandler(pattern, maxSize * MEGA, count, true);
                rootLogger.addHandler(handler);
            } catch (Exception ex) {
                handler = new MyConsoleHandler();
                handler.setFormatter(LOG_FORMATTER);
                rootLogger.setUseParentHandlers(false);
                rootLogger.addHandler(handler);

                rootLogger.log(Level.SEVERE, "Unable to set File log. Fallback to console log : {0}",ex.toString());
            }
        }

        if (updatedHandler) {
            Level oldLevel = rootLogger.getLevel();
            if (oldLevel == null || !oldLevel.equals(level)) {
                updatedLoglevel = true;
                rootLogger.setLevel(level);
            }
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof MyFileHandler || handler instanceof MyConsoleHandler) {
                    oldLevel = handler.getLevel();
                    if (oldLevel == null || !oldLevel.equals(level)) {
                        updatedLoglevel = true;
                        handler.setLevel(level);
                    }
                    setFormatter(handler);
                    Filter oldFilter = handler.getFilter();
                    if (oldFilter == null || oldFilter != LOG_FILTER) {
                        updatedLoglevel = true;
                        handler.setFilter(LOG_FILTER);
                    }
                }
            }
        }
//        if (updatedHandler || updatedLoglevel) {
//            olderLog.log(Level.CONFIG, "Switching log config to file {0}", new Object[]{pattern});
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

    private static final class MyConsoleHandler extends ConsoleHandler {

    }

    private static final class MyFileHandler extends FileHandler {
        String pattern;
        int limit;
        int count;

        public MyFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
            super(pattern, limit, count, append);
            this.pattern = pattern;
            this.limit = limit;
            this.count = count;
        }
    }

    private static final class LogFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        Map<Level, String> logLevelCache = new HashMap<>();
        Map<String, String> classNameCache = new HashMap<>();

        private String logLevel(Level l) {
            String v = logLevelCache.get(l);
            if (v == null) {
                v = ensureSize(l.getLocalizedName(), 6);
                logLevelCache.put(l, v);
            }
            return v;
        }

        public String ensureSize(String className, int size) {
            StringBuilder sb = new StringBuilder(size);
            sb.append(className);
            while (sb.length() < 6) {
                sb.append(' ');
            }
            return sb.toString();
        }

        public String formatClassName(String className) {
            if (className == null) {
                return "";
            }
            String v = classNameCache.get(className);
            if (v == null) {
                StringBuilder sb = new StringBuilder(45);
                int pos = 0, end;
                while ((end = className.indexOf('.', pos)) >= 0) {
                    sb.append(className.charAt(pos)).append('.');
                    pos = end + 1;
                }
                sb.append(className.substring(pos));

                int length = 47 - sb.length();
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
                v = sb.toString();
                classNameCache.put(className, v);
            }
            return v;
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(record.getMillis())))
                    .append(" ")
                    .append(logLevel(record.getLevel()))
                    .append(" ")
                    .append(formatClassName(record.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(record))
                    .append(LINE_SEPARATOR);

            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // ignore
                }
            }

            return sb.toString();
        }
    }
}
