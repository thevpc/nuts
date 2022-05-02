package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.io.NutsPath;

public class FileInfo {
    private NutsPath file;
    private String highlighter;

    public FileInfo(NutsPath file, String highlighter) {
        this.file = file;
        this.highlighter = highlighter;
    }

    public NutsPath getFile() {
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
