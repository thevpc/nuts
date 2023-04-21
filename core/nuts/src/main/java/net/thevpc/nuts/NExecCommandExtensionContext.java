package net.thevpc.nuts;

public interface NExecCommandExtensionContext extends NSessionProvider {
    String getHost();

    String[] getCommand();

    NExecInput getIn();

    NExecOutput getOut();

    NExecOutput getErr();
}
