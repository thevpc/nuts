//package net.thevpc.nuts.runtime.standalone.format.tson.format;
//
//
//public class TsonFormatImplBuilder implements TsonFormatBuilder {
//
//    private DefaultTsonFormatConfig config = new DefaultTsonFormatConfig();
//
//    public TsonFormatImplBuilder() {
//    }
//
//    public TsonFormatImplBuilder setConfig(DefaultTsonFormatConfig config) {
//        this.config = config.copy();
//        return this;
//    }
//
//    public TsonFormatBuilder setOption(String optionName, Object configValue) {
//        config.set(optionName, configValue);
//        return this;
//    }
//
//    public TsonFormatBuilder compact(boolean compact) {
//        config.setCompact(compact);
//        return this;
//    }
//
//    public boolean isCompact() {
//        return config.isCompact();
//    }
//
//    public TsonFormatBuilder option(String optionName, Object configValue) {
//        return setOption(optionName,configValue);
//    }
//
//    public Object getOption(String optionName) {
//        return config.get(optionName);
//    }
//
//    public TsonFormat build() {
//        return new TsonFormatImpl(config);
//    }
//}
