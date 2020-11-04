package net.thevpc.nuts;

public interface NutsIdParser {
    NutsIdParser setLenient(boolean lenient);

    boolean isLenient();

    /**
     * parse id or null if not valid.
     * id is parsed in the form
     * namespace://group:name#version?key=&lt;value&gt;{@code &}key=&lt;value&gt; ...
     * @param id to parse
     * @return parsed id
     * @throws NutsParseException if the string cannot be evaluated
     */
    NutsId parse(String id);


}
