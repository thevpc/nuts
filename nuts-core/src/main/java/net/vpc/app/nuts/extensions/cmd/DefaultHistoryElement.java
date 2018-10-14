package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.HistoryElement;

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
