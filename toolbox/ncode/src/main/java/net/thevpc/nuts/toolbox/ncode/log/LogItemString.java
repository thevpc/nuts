package net.thevpc.nuts.toolbox.ncode.log;

public class LogItemString implements LogItem {
    private String line;

    public LogItemString(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "LogItemString{" +
                "line='" + line + '\'' +
                '}';
    }
}
