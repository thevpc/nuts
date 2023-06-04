package net.thevpc.nuts.build.util;

public class JpsResult {
    private long pid;
    private String className;
    private String type;
    private String discriminator;
    private String[] cmd;

    public JpsResult(long pid, String className, String[] cmd) {
        this.pid = pid;
        this.className = className;
        this.cmd = cmd;
    }

    public String getType() {
        return type;
    }

    public JpsResult setType(String type) {
        this.type = type;
        return this;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public JpsResult setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
        return this;
    }

    public long getPid() {
        return pid;
    }

    public String getClassName() {
        return className;
    }

    public String[] getCmd() {
        return cmd;
    }
}
