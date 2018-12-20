package net.vpc.app.nuts;

public class NutsUserCancelException extends NutsException {
    public NutsUserCancelException() {
        super("User cancelled operation");
    }
}
