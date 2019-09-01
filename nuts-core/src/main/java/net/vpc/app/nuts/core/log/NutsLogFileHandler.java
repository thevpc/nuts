package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class NutsLogFileHandler extends FileHandler {

    String pattern;
    int limit;
    int count;

    public static NutsLogFileHandler create(NutsWorkspace ws, NutsLogConfig config, boolean append) throws IOException, SecurityException {
        Level level = config.getLogLevel();
        String folder = config.getLogFolder();
        String name = config.getLogName();
        int maxSize = config.getLogSize();
        int count = config.getLogCount();
//        boolean inheritLog = config.isLogInherited();
        String rootPackage = "net.vpc.app.nuts";
        if (level == null) {
            level = Level.INFO;
        }
        int MEGA = 1024 * 1024;
        if (name == null || CoreStringUtils.isBlank(name)) {
            name = Instant.now().toString().replace(":", "") + "-nuts-%g.log";
        }
        if (folder == null || CoreStringUtils.isBlank(folder)) {
            folder = ws.config().getStoreLocation(NutsStoreLocation.LOG) + "/net/vpc/app/nuts/nuts/" + ws.config().getApiVersion();
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
        if (maxSize <= 0) {
            maxSize = 5;
        }
        return new NutsLogFileHandler(pattern, maxSize * MEGA, count, append);
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

}
