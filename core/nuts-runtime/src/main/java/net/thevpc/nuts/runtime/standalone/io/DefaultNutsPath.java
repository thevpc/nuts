//package net.thevpc.nuts.runtime.standalone.io;
//
//import net.thevpc.nuts.NutsPath;
//import net.thevpc.nuts.NutsSession;
//import net.thevpc.nuts.NutsString;
//import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
//
//import java.util.Objects;
//
//public class DefaultNutsPath extends NutsPathBase {
//    private String value;
//
//    public DefaultNutsPath(String value, NutsSession session) {
//        super(session);
//        if (value == null) {
//            throw new IllegalArgumentException("invalid path");
//        }
//        this.value = value;
//    }
//
//    @Override
//    public String name() {
//        return CoreIOUtils.getURLName(value);
//    }
//
//    @Override
//    public String location() {
//        return value;
//    }
//
//    @Override
//    public NutsPath compressedForm() {
//        return new NutsCompressedPath(this);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(value);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        DefaultNutsPath that = (DefaultNutsPath) o;
//        return Objects.equals(value, that.value);
//    }
//
//    @Override
//    public String toString() {
//        return String.valueOf(value);
//    }
//}
