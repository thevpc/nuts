package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;

/**
 * @since 1.0.0
 */
public interface NClasspathEntry {
    static NClasspathEntry of(NId id){
        NAssert.requireNamedNonBlank(id,"id");
        return NReflectRPI.of().createClasspathEntry(id);
    }
    static NClasspathEntry of(NDependency dependency){
        NAssert.requireNamedNonBlank(dependency,"dependency");
        return NReflectRPI.of().createClasspathEntry(dependency);
    }
    static NClasspathEntry of(NDefinition definition){
        NAssert.requireNamedNonBlank(definition,"definition");
        return NReflectRPI.of().createClasspathEntry(definition);
    }
    static NClasspathEntry of(NPath path){
        NAssert.requireNamedNonBlank(path,"path");
        return NReflectRPI.of().createClasspathEntry(path);
    }
    NId id();          // null for raw paths
    NDependency dependency();          // null for raw paths
    NDefinition definition();  // null for raw paths
    NPath path();      // resolved location either way
    NClasspathEntryType type();
}
