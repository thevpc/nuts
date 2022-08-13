package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Objects;

public class NutsPomProperty {
    private String name;
    private String value;

    public NutsPomProperty() {
    }

    public NutsPomProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public NutsPomProperty setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public NutsPomProperty setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPomProperty that = (NutsPomProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
