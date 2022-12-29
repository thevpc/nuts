package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.io.NPath;

public class FileInfo {
    private NPath file;
    private String highlighter;

    public FileInfo(NPath file, String highlighter) {
        this.file = file;
        this.highlighter = highlighter;
    }

    public NPath getFile() {
        return file;
    }

    public String getHighlighter() {
        return highlighter;
    }

    public FileInfo setHighlighter(String highlighter) {
        this.highlighter = highlighter;
        return this;
    }
}
