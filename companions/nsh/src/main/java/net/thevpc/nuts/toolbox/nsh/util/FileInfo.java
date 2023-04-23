package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.io.NPath;

public class FileInfo {
    private NPath path;
    private String highlighter;

    public FileInfo(NPath path, String highlighter) {
        this.path = path;
        this.highlighter = highlighter;
    }

    public NPath getPath() {
        return path;
    }

    public String getHighlighter() {
        return highlighter;
    }

    public FileInfo setHighlighter(String highlighter) {
        this.highlighter = highlighter;
        return this;
    }
}
