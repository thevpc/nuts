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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

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
            String loggerName = record == null ? "" : NutsStringUtils.trim(record.getLoggerName());
            return loggerName.startsWith("net.vpc.app.nuts");
        }
    };

    private NutsLogUtils() {
    }

    public static void prepare(Level level, String pattern, int maxSize, int count) {
        Logger rootLogger = Logger.getLogger("");//"net.vpc.app.nuts"
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (pattern == null || NutsStringUtils.isEmpty(pattern)) {
            pattern = System.getProperty("user.home") +"/.nuts/default-workspace/log/net/vpc/app/nuts/nuts/LATEST/nuts-%g.log";
        }
        if (maxSize <= 0) {
            maxSize = 5;
        }
        if (count <= 0) {
            count = 3;
        }
        try {
            boolean found=false;
            for (Handler handler : rootLogger.getHandlers()) {
                if(handler instanceof MyFileHandler){
                    found=true;
                }
            }
            if(!found) {
                if (pattern.contains("/")) {
                    NutsIOUtils.createFile(pattern.substring(0, pattern.lastIndexOf('/'))).mkdirs();
                }
                rootLogger.addHandler(new MyFileHandler(pattern, maxSize * MEGA, count, true));
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
            handler.setFormatter(LOG_FORMATTER);
            handler.setFilter(LOG_FILTER);
        }
    }

    private static final class MyFileHandler extends FileHandler {

        public MyFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
            super(pattern, limit, count, append);
        }
    }
    private static final class LogFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        Map<Level,String> logLevelCache=new HashMap<>();
        Map<String,String> classNameCache=new HashMap<>();
        private String logLevel(Level l){
            String v = logLevelCache.get(l);
            if(v==null) {
                v=ensureSize(l.getLocalizedName(),6);
                logLevelCache.put(l,v);
            }
            return v;
        }

        public String ensureSize(String className,int size) {
            StringBuilder sb = new StringBuilder(size);
            sb.append(className);
            while (sb.length()<6){
                sb.append(' ');
            }
            return sb.toString();
        }

        public String formatClassName(String className) {
            if (className == null) {
                return "";
            }
            String v = classNameCache.get(className);
            if(v==null) {
                StringBuilder sb=new StringBuilder();
                String[] split = className.split("\\.");
                for (int i = 0; i < split.length - 1; i++) {
                    sb.append(split[i].charAt(0));
                    sb.append('.');
                }
                if(split.length>0){
                    sb.append(split[split.length-1]);
                }
                while (sb.length()<45){
                    sb.append(' ');
                }
                v=sb.toString();
                classNameCache.put(className,v);
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
