package net.thevpc.nuts.toolbox.ndiff.jar;

import java.io.File;

public final class Diff {
    public static DiffBuilder of(File source,File target){
        return new DiffBuilder(source,target);
    }
}
