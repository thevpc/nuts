package net.thevpc.nuts;

public interface NutsMemoryPrintStream extends NutsPrintStream {
    static NutsMemoryPrintStream of(NutsSession session) {
        return NutsPrintStreams.of(session).createInMemory();
    }

    byte[] getBytes();
}
