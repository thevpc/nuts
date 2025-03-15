//package net.thevpc.nuts.runtime.standalone.elem;
//
//import net.thevpc.nuts.elem.NArrayElement;
//import net.thevpc.nuts.elem.NElement;
//import net.thevpc.nuts.elem.NElementHeader;
//import net.thevpc.nuts.elem.NObjectElement;
//import net.thevpc.nuts.util.*;
//
//import java.time.Instant;
//import java.util.*;
//import java.util.stream.Stream;
//
//public class DefaultNElementHeader implements NElementHeader {
//    private boolean hasArgs;
//    private String name = null;
//    private List<NElement> args;
//    public static final DefaultNElementHeader EMPTY = new DefaultNElementHeader(null, false, new NElement[0]);
//    public static final DefaultNElementHeader EMPTY_ARGS = new DefaultNElementHeader(null, true, new NElement[0]);
//
//    public static DefaultNElementHeader of(String name, boolean hasArgs, NElement[] args) {
//        name = NStringUtils.trimToNull(name);
////        if(name!=null){
////            NAssert.requireTrue(name.matches("[a-zA-Z][a-zA-Z0-9-_]*"), "args");
////        }
//        if (args == null) {
//            args = new NElement[0];
//        }
//        if (args.length == 0 && name == null) {
//            if (hasArgs) {
//                return EMPTY_ARGS;
//            } else {
//                return EMPTY;
//            }
//        }
//        return new DefaultNElementHeader(name, hasArgs, args);
//    }
//
//    public static DefaultNElementHeader of(String name, boolean hasArgs, List<NElement> args) {
//        name = NStringUtils.trimToNull(name);
////        if(name!=null){
////            NAssert.requireTrue(name.matches("[a-zA-Z][a-zA-Z0-9-_]*"), "args");
////        }
//        if (args == null) {
//            args = new ArrayList<>();
//        }
//        return new DefaultNElementHeader(name, hasArgs, args.toArray(new NElement[0]));
//    }
//
//    public DefaultNElementHeader(String name, boolean hasArgs, NElement[] args) {
//        this.name = NStringUtils.trimToNull(name);
//        this.hasArgs = hasArgs || (args != null && args.length > 0);
//        this.args = args == null ? Collections.emptyList() : Arrays.asList(args);
//    }
//
//    @Override
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public boolean isWithArgs() {
//        return hasArgs;
//    }
//
//    @Override
//    public Collection<NElement> args() {
//        return args;
//    }
//
//    @Override
//    public boolean isBlank() {
//        if (!NBlankable.isBlank(name)) {
//            return false;
//        }
//        if (hasArgs) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public Iterator<NElement> iterator() {
//        return args().iterator();
//    }
//
//    @Override
//    public Stream<NElement> stream() {
//        return args.stream();
//    }
//
//    @Override
//    public NOptional<NElement> getArg(int index) {
//        if (index >= 0 && index < args.size()) {
//            return NOptional.of(args.get(index));
//        }
//        return NOptional.ofNamedEmpty(NMsg.ofC("element at index %s", index));
//    }
//
//    @Override
//    public NOptional<String> getStringArg(int index) {
//        return getArg(index).flatMap(x -> x.asString());
//    }
//
//    @Override
//    public NOptional<Boolean> getBooleanArg(int index) {
//        return getArg(index).flatMap(x -> x.asBoolean());
//    }
//
//    @Override
//    public NOptional<Byte> getByteArg(int index) {
//        return getArg(index).flatMap(x -> x.asByte());
//    }
//
//    @Override
//    public NOptional<Short> getShortArg(int index) {
//        return getArg(index).flatMap(x -> x.asShort());
//    }
//
//    @Override
//    public NOptional<Integer> getIntArg(int index) {
//        return getArg(index).flatMap(x -> x.asInt());
//    }
//
//    @Override
//    public NOptional<Long> getLongArg(int index) {
//        return getArg(index).flatMap(x -> x.asLong());
//    }
//
//    @Override
//    public NOptional<Float> getFloatArg(int index) {
//        return getArg(index).flatMap(x -> x.asFloat());
//    }
//
//    @Override
//    public NOptional<Double> getDoubleArg(int index) {
//        return getArg(index).flatMap(x -> x.asDouble());
//    }
//
//    @Override
//    public NOptional<Instant> getInstantArg(int index) {
//        return getArg(index).flatMap(x -> x.asInstant());
//    }
//
//    @Override
//    public NOptional<NArrayElement> getArrayArg(int index) {
//        return getArg(index).flatMap(x -> x.asArray());
//    }
//
//    @Override
//    public NOptional<NObjectElement> getObjectArg(int index) {
//        return getArg(index).flatMap(x -> x.asObject());
//    }
//}
