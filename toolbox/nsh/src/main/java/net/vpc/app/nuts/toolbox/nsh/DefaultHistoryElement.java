package net.vpc.app.nuts.toolbox.nsh;

public class DefaultHistoryElement implements HistoryElement {
    private String command;

    public DefaultHistoryElement(String command) {
        this.command = command;
    }

    @Override
    public String getCommand() {
        return command;
    }
}
