package net.thevpc.nuts.toolbox.nlog.model;

public class LineFormat {
    private boolean lineNumber;
    public LineFormat(NLogFilterConfig config) {
        this.lineNumber=config.isLineNumber();
    }

    public boolean isLineNumber() {
        return lineNumber;
    }
}
