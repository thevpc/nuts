package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsFile;

class NutsIdVertex {
    NutsFile from;
    NutsFile to;

    public NutsIdVertex(NutsFile from, NutsFile to) {
        this.from = from;
        this.to = to;
    }
}
