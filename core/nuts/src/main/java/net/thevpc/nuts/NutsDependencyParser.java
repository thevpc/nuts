package net.thevpc.nuts;

public interface NutsDependencyParser {

    NutsDependencyParser setLenient(boolean lenient);

    boolean isLenient();


    /**
     * parse dependency in the form
     * namespace://group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     * If the string cannot be evaluated, return null (when not required).
     * @param dependency dependency
     * @return new instance of parsed dependency
     */
    NutsDependency parseDependency(String dependency);


    NutsDependencyScope parseScope(String scope);

    boolean parseOptional(String optional);
}
