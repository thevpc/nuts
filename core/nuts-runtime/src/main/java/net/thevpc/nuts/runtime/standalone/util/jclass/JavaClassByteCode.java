/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author thevpc
 */
public class JavaClassByteCode {

    private static final int FLAG_UTF = 1;
    private static final int FLAG_FLOAT = 4;
    private static final int FLAG_INT = 3;
    private static final int FLAG_LONG = 5;
    private static final int FLAG_DOUBLE = 6;
    private static final int FLAG_CLASS = 7;
    private static final int FLAG_FIELD_REF = 9;
    private static final int FLAG_STRING = 8;
    private static final int FLAG_METHOD_REF = 10;
    private static final int FLAG_INTERFACE_METHOD_REF = 11;
    private static final int FLAG_NAME_AND_TYPE = 12;
    private static final int FLAG_METHOD_HANDLE = 15;
    private static final int FLAG_METHOD_TYPE = 16;
    private static final int FLAG_INVOKE_DYNAMIC = 18;
    private static final int FLAG_MODULE = 19;
    private static final int FLAG_PACKAGE = 20;
    private final DataInputStream stream;
    private final NutsSession session;
    private final Visitor visitor;
    private Constant[] constants = new Constant[32];

    public JavaClassByteCode(InputStream stream, NutsSession session) {
        this(stream, null, session);
    }

    public JavaClassByteCode(DataInputStream stream, NutsSession session) {
        this(stream, null, session);
    }

    public JavaClassByteCode(InputStream stream, Visitor visitor, NutsSession session) {
        this((stream instanceof DataInputStream) ? ((DataInputStream) stream) : new DataInputStream(stream), visitor, session);
    }

    public JavaClassByteCode(DataInputStream stream, Visitor visitor, NutsSession session) {
        this.session = session;
        this.stream = stream;
        this.visitor = visitor;
        try {


            int signature = stream.readInt();
            if (signature != 0xcafebabe) {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid Java signature"));
            }
            int minorVersion = stream.readUnsignedShort();
            int majorVersion = stream.readUnsignedShort();
            if (!visitVersion(majorVersion, minorVersion)) {
                return;
            }
            readConstantPool();
            int accessFlags = stream.readUnsignedShort();

            int thisClassIndex = stream.readUnsignedShort();
            String thisClass = JavaClassByteCode.this.getConstant(thisClassIndex).asString();
            int superClassIndex = stream.readUnsignedShort();
            String superClass = superClassIndex == 0 ? null : JavaClassByteCode.this.getConstant(superClassIndex).asString();

            int interfacesCount = stream.readUnsignedShort();
            String[] interfaces = new String[interfacesCount];
            for (int i = 0; i < interfacesCount; i++) {
                int index = stream.readUnsignedShort();
                interfaces[i] = JavaClassByteCode.this.getConstant(index).asString();
            }
            if (!visitClassDeclaration(accessFlags, thisClass, superClass, interfaces)) {
                return;
            }

            int fieldsCount = stream.readUnsignedShort();
            for (int i = 0; i < fieldsCount; i++) {
                if (!readField()) {
                    return;
                }
            }
            int methodsCount = stream.readUnsignedShort();
            for (int i = 0; i < methodsCount; i++) {
                if (!readMethod()) {
                    return;
                }
            }
            int attributesCount = stream.readUnsignedShort();
            for (int i = 0; i < attributesCount; i++) {
                ClassAttribute a = new ClassAttribute();
                if (a.entry.valString.equals("Module")) {
                    DataInputStream q = new DataInputStream(new ByteArrayInputStream(a.raw));
                    ModuleInfo mi = new ModuleInfo();
                    mi.module_name = getConstantModule(q.readUnsignedShort());
                    mi.module_flags = q.readUnsignedShort();
                    mi.module_version = getConstantUTF(q.readUnsignedShort());
                    int requires_count = q.readUnsignedShort();
                    mi.required = new ModuleInfoRequired[requires_count];
                    for (int j = 0; j < requires_count; j++) {
                        ModuleInfoRequired rr = new ModuleInfoRequired();
                        rr.req_name = getConstantModule(q.readUnsignedShort());
                        rr.req_flags = q.readUnsignedShort();
                        rr.req_version = getConstantUTF(q.readUnsignedShort());
                        mi.required[j] = rr;
                    }
                    if (!visitClassAttributeModule(mi)) {
                        return;
                    }
                }
                if (!visitClassAttribute(thisClass, a)) {
                    return;
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }

    }

    private boolean visitClassAttribute(String thisClass, ClassAttribute a) {
        return true;
    }

    private boolean visitClassAttributeModule(ModuleInfo mi) {
        if (visitor != null) {
            return visitor.visitClassAttributeModule(mi);
        }
        return true;
    }

    protected void readConstantPool() {
        try {
            int count = stream.readUnsignedShort();
            ensureConstants(count * 2);
            for (int i = 1; i < count; i++) {
                Constant cst = getConstant(i, true);
                cst.read(stream, this);
                if ((cst.tag == FLAG_DOUBLE) || (cst.tag == FLAG_LONG)) {
                    i++;
                }
            }

        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }

    }

    protected boolean readField() {
        try {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            String name = JavaClassByteCode.this.getConstant(nameIndex).asString();
            int descriptorIndex = stream.readUnsignedShort();
            String descriptor = JavaClassByteCode.this.getConstant(descriptorIndex).asString();
            int attributeCount = stream.readUnsignedShort();
            FieldAttribute[] attributes = new FieldAttribute[attributeCount];
            for (int i = 0; i < attributeCount; i++) {
                attributes[i] = new FieldAttribute();
            }
            return visitField(accessFlags, name, descriptor, attributes);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    protected boolean readMethod() {
        try {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            String name = JavaClassByteCode.this.getConstant(nameIndex).asString();
            int descriptorIndex = stream.readUnsignedShort();
            String descriptor = JavaClassByteCode.this.getConstant(descriptorIndex).asString();
            int attributeCount = stream.readUnsignedShort();
            MethodAttribute[] attributes = new MethodAttribute[attributeCount];
            for (int i = 0; i < attributeCount; i++) {
                attributes[i] = new MethodAttribute();
            }
            return visitMethod(accessFlags, name, descriptor, attributes);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public String getConstantUTF(int index) throws IOException {
        Constant a = getConstant(index);
        if(a==null){
            return null;
        }
        if(a.tag!=FLAG_UTF){
            throw new IllegalArgumentException("expected UTF");
        }
        return a.valString;
    }
    public String getConstantModule(int index) throws IOException {
        Constant a = getConstant(index);
        if(a==null){
            return null;
        }
        if(a.tag!=FLAG_MODULE){
            throw new IllegalArgumentException("expected MODULE");
        }
        if(a.valName.tag!=FLAG_UTF){
            throw new IllegalArgumentException("expected UTF");
        }
        return a.valName.valString;
    }
    public Constant getConstant(int index) throws IOException {
        if(index==0){
            return null;
        }
        Constant e = getConstant(index, false);
        if (e == null) {
            throw new IllegalArgumentException("jvm constant not found at index " + index);
        }
        return e;
    }

    public Constant getConstant(int index, boolean createNew) throws IOException {
        boolean tooBig = index >= constants.length;
        Constant cst = tooBig ? null : this.constants[index];
        if (cst == null) {
            if (createNew) {
                cst = new Constant(index);
                if (tooBig) {
                    ensureConstants(index + 1);
                }
                this.constants[index] = cst;
            }
        }
        return cst;
    }

    ////////////////////////////
    public boolean visitVersion(int major, int minor) {
        if (visitor != null) {
            return visitor.visitVersion(major, minor);
        }
        return true;
    }

    public boolean visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
        if (visitor != null) {
            return visitor.visitClassDeclaration(accessFlags, thisClass, superClass, interfaces);
        }
        return true;
    }

    public boolean visitField(int accessFlags, String name, String descriptor, FieldAttribute[] attributes) {
        if (visitor != null) {
            return visitor.visitField(accessFlags, name, descriptor);
        }
        return true;
    }

    public boolean visitMethod(int accessFlags, String name, String descriptor, MethodAttribute[] attributes) {
        if (visitor != null) {
            return visitor.visitMethod(accessFlags, name, descriptor);
        }
        return true;
    }

    private void ensureConstants(int size) {
        int len = constants.length;
        if (len < size) {
            int len2 = size + 32;
            Constant[] n = new Constant[len2];
            System.arraycopy(constants, 0, n, 0, len);
            constants = n;
        }
    }

    public interface Visitor {

        default boolean visitVersion(int major, int minor) {
            return true;
        }

        default boolean visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
            return true;
        }

        default boolean visitField(int accessFlags, String name, String descriptor) {
            return true;
        }

        default boolean visitMethod(int accessFlags, String name, String descriptor) {
            return true;
        }

        default boolean visitClassAttributeModule(ModuleInfo mi){return true;}
    }

    public static class Constant {

        int index;
        int tag;
        int valKind;
        Constant valName;
        Constant valRef;
        int valInt;
        float valFloat;
        long valLong;
        double valDouble;
        String valString;

        public Constant(int entryId) {
            this.index = entryId;
        }

        void read(DataInputStream stream, JavaClassByteCode s) {
            try {
                tag = stream.readUnsignedByte();
                switch (tag) {
                    case FLAG_UTF: {
                        int length = stream.readUnsignedShort();
                        byte[] bytes = new byte[length];
                        stream.readFully(bytes);
                        this.valString = new String(bytes, StandardCharsets.UTF_8);
                        break;
                    }
                    case FLAG_INT: {
                        valInt = stream.readInt();
                        break;
                    }
                    case FLAG_FLOAT: {
                        valFloat = stream.readFloat();
                        break;
                    }
                    case FLAG_LONG: {
                        valLong = stream.readLong();
                        break;
                    }
                    case FLAG_DOUBLE: {
                        valDouble = stream.readDouble();
                        break;
                    }
                    case FLAG_CLASS: {
                        int index = stream.readUnsignedShort();
                        valRef = s.getConstant(index, true);
                        break;
                    }
                    case FLAG_STRING: {
                        int index = stream.readUnsignedShort();
                        this.valRef = s.getConstant(index, true);
                        break;
                    }
                    case FLAG_FIELD_REF:
                    case FLAG_METHOD_REF:
                    case FLAG_INTERFACE_METHOD_REF:
                    case FLAG_NAME_AND_TYPE: {
                        int classIndex = stream.readUnsignedShort();
                        int nameAndTypeIndex = stream.readUnsignedShort();
                        this.valName = s.getConstant(classIndex, true);
                        this.valRef = s.getConstant(nameAndTypeIndex, true);
                        break;
                    }
                    case FLAG_METHOD_HANDLE: {
                        this.valKind = stream.readUnsignedByte();
                        if (this.valKind < 1 || this.valKind > 9) {
                            throw new IllegalArgumentException("Unsupported");
                        }
                        this.valRef = s.getConstant(stream.readUnsignedShort(), true);
                        break;
                    }
                    case FLAG_METHOD_TYPE: {
                        this.valRef = s.getConstant(stream.readUnsignedShort(), true);
                        break;
                    }
                    case FLAG_INVOKE_DYNAMIC: {
                        this.valKind = stream.readUnsignedShort();
                        this.valRef = s.getConstant(stream.readUnsignedShort(), true);
                        break;
                    }
                    case FLAG_MODULE:
                    case FLAG_PACKAGE: {
                        this.valName = s.getConstant(stream.readUnsignedShort(), true);
                        break;
                    }
                    default:
                        throw new IOException("Unknown constant tag: " + tag);
                }
            } catch (IOException ex) {
                throw new NutsIOException(s.session, ex);
            }
        }

        public String asString() {
            switch (tag) {
                case FLAG_UTF: {
                    if (valString == null) {
                        throw new IllegalArgumentException("Expected String");
                    }
                    return valString;
                }
                case FLAG_CLASS: {
                    return valRef.asString();
                }
                case FLAG_METHOD_REF: {
                    return valName.asString();
                }
            }
            throw new IllegalArgumentException("Unsupported");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Constant[" + index + "]{");
            sb.append("tag=").append(tag).append(", ");
            switch (tag) {
                case FLAG_UTF:
                    sb.append("UTF, ");
                    sb.append(valString);
                    break;
                case FLAG_INT:
                    sb.append("INT, ");
                    sb.append(valInt);
                    break;
                case FLAG_FLOAT:
                    sb.append("FLOAT, ");
                    sb.append(valFloat);
                    break;
                case FLAG_LONG:
                    sb.append("LONG, ");
                    sb.append(valLong);
                    break;
                case FLAG_DOUBLE:
                    sb.append("DOUBLE, ");
                    sb.append(valDouble);
                    break;
                case FLAG_CLASS:
                    sb.append("CLASS, ");
                    sb.append(valRef);
                    break;
                case FLAG_STRING:
                    sb.append("STRING, ");
                    sb.append(valRef);
                    break;
                case FLAG_METHOD_TYPE:
                    sb.append("METHOD_TYPE, ");
                    sb.append(valRef);
                    break;
                case FLAG_FIELD_REF:
                    sb.append("FIELD_REF, ");
                    sb.append(valName).append(" ").append(valRef);
                    break;
                case FLAG_INTERFACE_METHOD_REF:
                    sb.append("INTERFACE_METHOD_REF, ");
                    sb.append(valName).append(" ").append(valRef);
                    break;
                case FLAG_INVOKE_DYNAMIC:
                    sb.append("INVOKE_DYNAMIC, ");
                    sb.append(valKind).append(" ").append(valRef);
                    break;
                case FLAG_METHOD_HANDLE:
                    sb.append("METHOD_HANDLE, ");
                    sb.append(valName).append(" ").append(valRef);
                    break;
                case FLAG_METHOD_REF:
                    sb.append("METHOD_REF, ");
                    sb.append(valName).append(" ").append(valRef);
                    break;
                case FLAG_NAME_AND_TYPE:
                    sb.append("NAME_AND_TYPE, ");
                    sb.append(valName).append(" ").append(valRef);
                    break;
                case FLAG_MODULE:
                    sb.append("MODULE, ");
                    sb.append(valName);
                    break;
                case FLAG_PACKAGE:
                    sb.append("PACKAGE, ");
                    sb.append(valName);
                    break;
            }
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ModuleInfoRequired {
        public String req_name;
        public int req_flags;
        public String req_version;
    }

    public static class ModuleInfo {
        public String module_name;
        public int module_flags;
        public String module_version;
        ModuleInfoRequired[] required;
    }

    public class MethodAttribute {

        Constant entry;
        Constant signature;
        Constant entry2;
        Constant[] exceptions;

        private MethodAttribute() throws IOException {
            int nameIndex = stream.readUnsignedShort();
            entry = JavaClassByteCode.this.getConstant(nameIndex);
            if (entry.tag != 1) {
                throw new IOException("unexpected");
            }
            stream.skipBytes(stream.readInt());
        }

    }

    public class FieldAttribute {

        Constant entry;
        Constant signature;
        Constant entry2;

        public FieldAttribute() throws IOException {
            int nameIndex = stream.readUnsignedShort();
            entry = getConstant(nameIndex);
            if (entry.tag != 1) {
                throw new IOException("unexpected");
            }
            stream.skipBytes(stream.readInt());
        }

    }

    public class CodeAttribute {

        public CodeAttribute() throws IOException {
            int nameIndex = stream.readUnsignedShort();
            Constant entry = getConstant(nameIndex);
            if (entry.tag != 1) {
                throw new IOException("unexpected");
            }
            stream.skipBytes(stream.readInt());
        }

    }

    public class ClassAttribute {
        int nameIndex;
        Constant entry;
        byte[] raw;

        public ClassAttribute() {
            try {
                nameIndex = stream.readUnsignedShort();
                entry = getConstant(nameIndex);
                if (entry.tag != 1) {
                    throw new IOException("unexpected");
                }
                int n = stream.readInt();
                raw = new byte[n];
                stream.readFully(raw);
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
    }
}
