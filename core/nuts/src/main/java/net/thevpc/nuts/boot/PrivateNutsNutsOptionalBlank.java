//package net.thevpc.nuts.boot;
//
//import net.thevpc.nuts.NutsMessage;
//import net.thevpc.nuts.NutsOptional;
//import net.thevpc.nuts.NutsSession;
//
//import java.util.function.Function;
//
//public class PrivateNutsNutsOptionalBlank<T> extends PrivateNutsOptionalImpl<T> {
//    private T value;
//    private Function<NutsSession, NutsMessage> blankMessage;
//
//    public PrivateNutsNutsOptionalBlank(T value, Function<NutsSession, NutsMessage> blankMessage) {
//        this.value = value;
//        this.blankMessage = blankMessage != null ? blankMessage : (s) -> NutsMessage.cstyle("blank value");
//    }
//
//    public T get(NutsSession session) {
//        return value;
//    }
//
//    @Override
//    public T get(NutsMessage message, NutsSession session) {
//        return value;
//    }
//
//    @Override
//    public boolean isPresent() {
//        return false;
//    }
//
//    public boolean isBlank() {
//        return true;
//    }
//
//    @Override
//    public boolean isError() {
//        return false;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return false;
//    }
//
//    @Override
//    public boolean isNotPresent() {
//        return true;
//    }
//
//    @Override
//    public Function<NutsSession, NutsMessage> getMessage() {
//        return blankMessage;
//    }
//}
