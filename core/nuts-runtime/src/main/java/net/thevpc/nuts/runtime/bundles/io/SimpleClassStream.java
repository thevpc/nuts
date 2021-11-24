/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author thevpc
 */
public class SimpleClassStream {

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

    public SimpleClassStream(InputStream stream, NutsSession session) {
        this(stream, null, session);
    }

    public SimpleClassStream(DataInputStream stream, NutsSession session) {
        this(stream, null, session);
    }

    public SimpleClassStream(InputStream stream, Visitor visitor, NutsSession session) {
        this((stream instanceof DataInputStream) ? ((DataInputStream) stream) : new DataInputStream(stream), visitor, session);
    }

    public SimpleClassStream(DataInputStream stream, Visitor visitor, NutsSession session) {
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
            visitVersion(majorVersion, minorVersion);
            readConstantPool();
            int accessFlags = stream.readUnsignedShort();

            int thisClassIndex = stream.readUnsignedShort();
            String thisClass = SimpleClassStream.this.getConstant(thisClassIndex).asString();
            int superClassIndex = stream.readUnsignedShort();
            String superClass = superClassIndex == 0 ? null : SimpleClassStream.this.getConstant(superClassIndex).asString();

            int interfacesCount = stream.readUnsignedShort();
            String[] interfaces = new String[interfacesCount];
            for (int i = 0; i < interfacesCount; i++) {
                int index = stream.readUnsignedShort();
                interfaces[i] = SimpleClassStream.this.getConstant(index).asString();
            }
            visitClassDeclaration(accessFlags, thisClass, superClass, interfaces);

            int fieldsCount = stream.readUnsignedShort();
            for (int i = 0; i < fieldsCount; i++) {
                readField();
            }
            int methodsCount = stream.readUnsignedShort();
            for (int i = 0; i < methodsCount; i++) {
                readMethod();
            }
            int attributesCount = stream.readUnsignedShort();
            for (int i = 0; i < attributesCount; i++) {
                new ClassAttribute();
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }

    }

    protected void readConstantPool() {
        try {


            int count = stream.readUnsignedShort();
            ensureConstants(count*2);
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

    protected void readField() {
        try {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            String name = SimpleClassStream.this.getConstant(nameIndex).asString();
            int descriptorIndex = stream.readUnsignedShort();
            String descriptor = SimpleClassStream.this.getConstant(descriptorIndex).asString();
            int attributeCount = stream.readUnsignedShort();
            FieldAttribute[] attributes = new FieldAttribute[attributeCount];
            for (int i = 0; i < attributeCount; i++) {
                attributes[i] = new FieldAttribute();
            }
            visitField(accessFlags, name, descriptor, attributes);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }

    }

    protected void readMethod() {
        try {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            String name = SimpleClassStream.this.getConstant(nameIndex).asString();
            int descriptorIndex = stream.readUnsignedShort();
            String descriptor = SimpleClassStream.this.getConstant(descriptorIndex).asString();
            int attributeCount = stream.readUnsignedShort();
            MethodAttribute[] attributes = new MethodAttribute[attributeCount];
            for (int i = 0; i < attributeCount; i++) {
                attributes[i] = new MethodAttribute();
            }
            visitMethod(accessFlags, name, descriptor, attributes);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public Constant getConstant(int index) throws IOException {
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
    public void visitVersion(int major, int minor) {
        if (visitor != null) {
            visitor.visitVersion(major, minor);
        }
    }

    public void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
        if (visitor != null) {
            visitor.visitClassDeclaration(accessFlags, thisClass, superClass, interfaces);
        }

    }

    public void visitField(int accessFlags, String name, String descriptor, FieldAttribute[] attributes) {
        if (visitor != null) {
            visitor.visitField(accessFlags, name, descriptor);
        }

    }

    public void visitMethod(int accessFlags, String name, String descriptor, MethodAttribute[] attributes) {
        if (visitor != null) {
            visitor.visitMethod(accessFlags, name, descriptor);
        }
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

        default void visitVersion(int major, int minor) {
        }

        default void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {

        }

        default void visitField(int accessFlags, String name, String descriptor) {

        }

        default void visitMethod(int accessFlags, String name, String descriptor) {

        }
    }

    public class MethodAttribute {

        Constant entry;
        Constant signature;
        Constant entry2;
        Constant[] exceptions;

        private MethodAttribute() throws IOException {
            int nameIndex = stream.readUnsignedShort();
            entry = SimpleClassStream.this.getConstant(nameIndex);
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

        public ClassAttribute() {
            try {
                int nameIndex = stream.readUnsignedShort();
                Constant entry = getConstant(nameIndex);
                if (entry.tag != 1) {
                    throw new IOException("unexpected");
                }
                stream.skipBytes(stream.readInt());
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
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

        void read(DataInputStream stream,SimpleClassStream s) {
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
}
