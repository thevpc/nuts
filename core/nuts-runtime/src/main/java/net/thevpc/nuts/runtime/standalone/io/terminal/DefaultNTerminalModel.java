package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.util.NUnused;

@NUnused
public class DefaultNTerminalModel {

    public DefaultNTerminalModel() {
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNTerminalModel.class);
    }

}
