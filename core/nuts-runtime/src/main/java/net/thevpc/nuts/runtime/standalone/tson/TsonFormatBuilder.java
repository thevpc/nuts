package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonFormatBuilder {
    TsonFormatBuilder compact(boolean compact);

    boolean isCompact();

    TsonFormatBuilder option(String optionName, Object configValue);

    TsonFormatBuilder setOption(String optionName, Object configValue);

    Object getOption(String optionName);

    TsonFormat build();
}
