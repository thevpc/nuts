package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;

/**
 * @since 1.0.0
 */
public interface NClasspathEntry {
    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @return of result
     */
    static NClasspathEntry of(NId id){
        NAssert.requireNamedNonBlank(id,"id");
        return NReflectRPI.of().createClasspathEntry(id);
    }
    /**
     * Creates a new instance of of.
     *
     * @param dependency dependency
     * @return of result
     */
    static NClasspathEntry of(NDependency dependency){
        NAssert.requireNamedNonBlank(dependency,"dependency");
        return NReflectRPI.of().createClasspathEntry(dependency);
    }
    /**
     * Creates a new instance of of.
     *
     * @param definition definition
     * @return of result
     */
    static NClasspathEntry of(NDefinition definition){
        NAssert.requireNamedNonBlank(definition,"definition");
        return NReflectRPI.of().createClasspathEntry(definition);
    }
    /**
     * Creates a new instance of of.
     *
     * @param path path
     * @return of result
     */
    static NClasspathEntry of(NPath path){
        NAssert.requireNamedNonBlank(path,"path");
        return NReflectRPI.of().createClasspathEntry(path);
    }
    /**
     * Id.
     *
     * @return id result
     */
    NId id();          // null for raw paths
    /**
     * Dependency.
     *
     * @return dependency result
     */
    NDependency dependency();          // null for raw paths
    /**
     * Definition.
     *
     * @return definition result
     */
    NDefinition definition();  // null for raw paths
    /**
     * Path.
     *
     * @return path result
     */
    NPath path();      // resolved location either way
    /**
     * Type.
     *
     * @return type result
     */
    NClasspathEntryType type();
}
