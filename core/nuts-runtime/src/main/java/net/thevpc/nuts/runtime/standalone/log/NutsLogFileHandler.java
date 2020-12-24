package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NutsLogConfig;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogFileHandler extends FileHandler {

    String pattern;
    int limit;
    int count;

    public static NutsLogFileHandler create(NutsWorkspace ws, NutsLogConfig config, boolean append, Path logFolder) throws IOException, SecurityException {
        Level level = config.getLogFileLevel();
        String folder = config.getLogFileBase();
        String name = config.getLogFileName();
        int maxSize = config.getLogFileSize();
        int count = config.getLogFileCount();
//        boolean inheritLog = config.isLogInherited();
//        String rootPackage = "net.thevpc.nuts";
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (name == null || CoreStringUtils.isBlank(name)) {
            name = Instant.now().toString().replace(":", "") + "-nuts-%g.log";
        }
        if (folder == null || CoreStringUtils.isBlank(folder)) {
            folder = logFolder + "/net/thevpc/nuts/nuts/" + ws.getApiVersion();
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
        NutsLogFileHandler handler = new NutsLogFileHandler(pattern, maxSize * MEGA, count, append);
        handler.setLevel(level);
        return handler;
    }

    private NutsLogFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
        super(prepare(pattern), limit, count, append);
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        setFormatter(NutsLogPlainFormatter.PLAIN);
    }

    private static String prepare(String pattern) {
        File parentFile = new File(pattern).getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        return pattern;
    }

    public boolean isLoggable(LogRecord record) {
        if (!super.isLoggable(record)) {
            return false;
        }
        if (record instanceof NutsLogRecord) {
            NutsSession session = ((NutsLogRecord) record).getSession();
            if (session != null) {
                NutsLogConfig logConfig = session.getWorkspace().config().options().getLogConfig();
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
                Level recLogLevel = record.getLevel();
                if (recLogLevel.intValue() < sessionLogLevelValue || sessionLogLevelValue == Level.OFF.intValue()) {
                    return false;
                }
                Filter sessionLogFilter = session.getLogFileFilter();
                if (sessionLogFilter == null && logConfig != null) {
                    sessionLogFilter = logConfig.getLogFileFilter();
                }
                if (sessionLogFilter != null) {
                    if (!sessionLogFilter.isLoggable(record)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
