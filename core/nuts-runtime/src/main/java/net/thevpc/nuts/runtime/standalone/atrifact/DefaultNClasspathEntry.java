package net.thevpc.nuts.runtime.standalone.atrifact;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.io.NPath;

import java.util.Objects;

public class DefaultNClasspathEntry implements NClasspathEntry {
    NId id;
    NDefinition definition;
    NPath path;
    NClasspathEntryType type;
    boolean optional;

    public DefaultNClasspathEntry(NDefinition definition) {
        this.id = definition.id();
        this.definition = definition;
        this.optional = false;
        this.type = NClasspathEntryType.DEFINITION;
    }

    public DefaultNClasspathEntry(NId id) {
        this.id = id;
        this.definition = null;
        this.optional = id.toDependency().isOptional();
        this.type = NClasspathEntryType.DEPENDENCY;
    }

    public DefaultNClasspathEntry(NDependency id) {
        this.id = id.toId();
        this.definition = null;
        this.optional = id.isOptional();
        this.type = NClasspathEntryType.DEPENDENCY;
    }

    public DefaultNClasspathEntry(NPath path) {
        this.path = path;
        this.optional = false;
        this.type = NClasspathEntryType.PATH;
    }

    public DefaultNClasspathEntry(NPath path,boolean optional) {
        this.path = path;
        this.optional = optional;
        this.type = NClasspathEntryType.PATH;
    }

    public DefaultNClasspathEntry(NId id, NPath path) {
        this.id = id;
        this.path = path;
    }

    @Override
    public NId id() {
        return id;
    }

    public boolean optional() {
        return optional;
    }

    @Override
    public NDefinition definition() {
        return definition;
    }

    @Override
    public NDependency dependency() {
        return id==null?null:id.toDependency();
    }

    @Override
    public NClasspathEntryType type() {
        return type;
    }

    @Override
    public String toString() {
        if (id != null) {
            return id.toString();
        }
        if (path != null) {
            return path.toString();
        }
        if (definition != null) {
            return definition.id().toString();
        }
        return "DefOfUrl{" +
                "id=" + id +
                ", definition=" + definition +
                ", url=" + path +
                '}';
    }

    public NPath path() {
        if (path != null) {
            return path;
        }
        if (definition != null) {
            return definition.content().orNull();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNClasspathEntry that = (DefaultNClasspathEntry) o;
        return optional == that.optional && Objects.equals(id, that.id) && Objects.equals(definition, that.definition)
                && Objects.equals(path, that.path)
                && Objects.equals(type, that.type)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, definition, path, optional, type);
    }
}
