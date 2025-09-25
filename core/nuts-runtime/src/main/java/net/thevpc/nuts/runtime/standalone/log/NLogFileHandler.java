package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NLogFileHandler implements NLogSPI {

    private String pattern;
    private int limit;
    private int count;
    private Level level;
    private FileHandler fileHandler;

    public static NLogFileHandler create(NLogConfig config, boolean append, Path logFolder) throws IOException, SecurityException {
        Level level = config.getLogFileLevel();
        String folder = config.getLogFileBase();
        String name = config.getLogFileName();
        int maxSize = config.getLogFileSize();
        int count = config.getLogFileCount();
//        String rootPackage = "net.thevpc.nuts";
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (name == null || NBlankable.isBlank(name)) {
            name = Instant.now().toString().replace(":", "") + "-nuts-%g.log";
        }
        if (folder == null || NBlankable.isBlank(folder)) {
            folder = logFolder + "/" + NConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + NWorkspace.of().getApiVersion();
        }
        String pattern = (folder + "/" + name).replace('/', File.separatorChar);
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
        NLogFileHandler handler = new NLogFileHandler(pattern, maxSize * MEGA, count, append, level);
        return handler;
    }

    private NLogFileHandler(String pattern, int limit, int count, boolean append, Level level) throws IOException, SecurityException {
        this.pattern = prepare(pattern);
        this.limit = limit;
        this.count = count;
        this.level = level;
        this.fileHandler = new FileHandler(pattern, limit, count, append);
        this.fileHandler.setLevel(level);
        this.fileHandler.setFormatter(new NLogRichFormatter(true));
    }

    private static String prepare(String pattern) {
        File parentFile = new File(pattern).getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        return pattern;
    }

    @Override
    public void log(NMsg message) {
        if (!isLoggable(message.getLevel())) {
            return;
        }
        Instant now = Instant.now();
        NMsg msg2=NMsg.ofC("%s [%-6s] [%-7s] %s%s", now, message.getLevel(), message.getIntent(), message,
                message.getDurationNanos() <= 0 ? ""
                        : NMsg.ofC(" (duration: %s)", NDuration.ofNanos(message.getDurationNanos()))
        );
        LogRecord r = new LogRecord(message.getLevel(),"{0}");
        r.setMillis(now.toEpochMilli());
        r.setThrown(message.getThrowable());
        r.setParameters(new Object[]{msg2.toString()});
        this.fileHandler.publish(r);
    }

    @Override
    public boolean isLoggable(Level level) {
        int levelValue = this.level == null ? Level.INFO.intValue() : this.level.intValue();
        if (!(level.intValue() >= levelValue && levelValue != Level.OFF.intValue())) {
            return false;
        }
        NSession session = NSession.of();
        NLogConfig logConfig = NWorkspace.of().getBootOptions().getLogConfig().orElseGet(NLogConfig::new);
        Level sessionLogLevel = session.getLogFileLevel();
        if (sessionLogLevel == null) {
            if (logConfig != null) {
                sessionLogLevel = logConfig.getLogFileLevel();
            }
            if (sessionLogLevel == null) {
                sessionLogLevel = Level.OFF;
            }
        }
        final int sessionLogLevelValue = sessionLogLevel.intValue();
        if (!(level.intValue() >= sessionLogLevelValue && sessionLogLevelValue != Level.OFF.intValue())) {
            return false;
        }
        return true;
    }

    public void setLevel(Level level) {
        this.level = level;
        fileHandler.setLevel(level);
    }
}
