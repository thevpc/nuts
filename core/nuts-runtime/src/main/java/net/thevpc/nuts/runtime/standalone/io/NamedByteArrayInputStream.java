//package net.thevpc.nuts.runtime.standalone.io;
//
//import java.io.ByteArrayInputStream;
//
//public class NamedByteArrayInputStream extends ByteArrayInputStream {
//    private String name;
//
//    public NamedByteArrayInputStream(byte[] buf, String name) {
//        super(buf);
//        this.name = name;
//    }
//
//    public NamedByteArrayInputStream(byte[] buf, int offset, int length, String name) {
//        super(buf, offset, length);
//        this.name = name;
//    }
//
//    @Override
//    public String toString() {
//        if(name==null){
//            return super.toString();
//        }
//        return name;
//    }
//}
