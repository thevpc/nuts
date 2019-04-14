/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vpc
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
    private DataInputStream stream;
    private Visitor visitor;
    private Map<Integer, Constant> poolEntries = new HashMap<Integer, Constant>();

    public SimpleClassStream(InputStream stream) throws IOException {
        this(stream, null);
    }

    public SimpleClassStream(DataInputStream stream) throws IOException {
        this(stream, null);
    }

    public SimpleClassStream(InputStream stream, Visitor visitor) throws IOException {
        this((stream instanceof DataInputStream) ? ((DataInputStream) stream) : new DataInputStream(stream), visitor);
    }

    public SimpleClassStream(DataInputStream stream, Visitor visitor) throws IOException {
        this.stream = stream;
        this.visitor = visitor;
        int signature = stream.readInt();
        if (signature != 0xcafebabe) {
            throw new IllegalArgumentException("Invalid Java signature");
        }
        int minorVersion = stream.readUnsignedShort();
        int majorVersion = stream.readUnsignedShort();
        visitVersion(majorVersion, minorVersion);
        readConstantPool();
        int accessFlags = stream.readUnsignedShort();

        int thisClassIndex = stream.readUnsignedShort();
        String thisClass = SimpleClassStream.this.getConstant(thisClassIndex).asString();
        int superClassIndex = stream.readUnsignedShort();
        String superClass = SimpleClassStream.this.getConstant(superClassIndex).asString();

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
    }

    protected void readConstantPool() throws IOException {
        int count = stream.readUnsignedShort();
        for (int i = 1; i < count; i++) {
            Constant cst = getConstant(i, true);
            cst.read();
            if ((cst.tag == FLAG_DOUBLE) || (cst.tag == FLAG_LONG)) {
                i++;
            }
        }
    }

    protected void readField() throws IOException {
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
    }

    protected void readMethod() throws IOException {
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

    public class VerificationTypeInfo {

        int type;
        int offset;
        Constant classConstant;

        public VerificationTypeInfo() throws IOException {
            type = stream.readUnsignedByte();
            switch (type) {
                //
                case 0:
                case 1:
                case 2:
                case 4:
                case 3:
                case 5:
                case 6:
                    break;
                case 7: {
                    int index = stream.readUnsignedShort();
                    this.classConstant = getConstant(index);
                    break;
                }
                case 8: {
                    this.offset = stream.readUnsignedShort();
                    break;
                }
                default: {
                    throw new IOException("Unexpect : " + type);
                }
            }
        }

    }

    public class ClassAttribute {

        public ClassAttribute() throws IOException {
            int nameIndex = stream.readUnsignedShort();
            Constant entry = getConstant(nameIndex);
            if (entry.tag != 1) {
                throw new IOException("unexpected");
            }
            stream.skipBytes(stream.readInt());
        }
    }

    public class Constant {

        int index;
        int tag;
        int valKind;
        Constant valName;
//        Constant valNameAndType;
//        Constant valText;
        Constant valRef;
        int valInt;
        float valFloat;
        long valLong;
        double valDouble;
        String valString;

        public Constant(int entryId) {
            this.index = entryId;
        }

        void read() throws IOException {
            tag = stream.readUnsignedByte();
            switch (tag) {
                case FLAG_UTF: {
                    int length = stream.readUnsignedShort();
                    byte[] bytes = new byte[length];
                    stream.read(bytes, 0, length);
                    this.valString = new String(bytes, "UTF-8");
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
                    valRef = getConstant(index, true);
                    break;
                }
                case FLAG_STRING: {
                    int index = stream.readUnsignedShort();
                    this.valRef = getConstant(index, true);
                    break;
                }
                case FLAG_FIELD_REF:
                case FLAG_METHOD_REF:
                case FLAG_INTERFACE_METHOD_REF: {
                    int classIndex = stream.readUnsignedShort();
                    int nameAndTypeIndex = stream.readUnsignedShort();
                    this.valName = getConstant(classIndex, true);
                    this.valRef = getConstant(nameAndTypeIndex, true);
                    break;
                }
                case FLAG_NAME_AND_TYPE: {
                    int nameIndex = stream.readUnsignedShort();
                    int descriptorIndex = stream.readUnsignedShort();
                    this.valName = getConstant(nameIndex, true);
                    this.valRef = getConstant(descriptorIndex, true);
                    break;
                }
                case FLAG_METHOD_HANDLE: {
                    this.valKind = stream.readByte();
                    if(this.valKind<1 || this.valKind>9){
                        throw new IllegalArgumentException("Unsupported");
                    }
                    this.valRef = getConstant(stream.readUnsignedShort(), true);
                    break;
                }
                case FLAG_METHOD_TYPE: {
                    this.valRef = getConstant(stream.readUnsignedShort(), true);
                    break;
                }
                case FLAG_INVOKE_DYNAMIC: {
                    this.valKind = stream.readUnsignedShort();
                    this.valRef = getConstant(stream.readUnsignedShort(), true);
                    break;
                }
                default:
                    throw new IOException("Unknown constant tag: " + tag);
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
            }
            sb.append('}');
            return sb.toString();
        }

    }

    public Constant getConstant(int index) throws IOException {
        Constant e = getConstant(index, false);
        if (e == null) {
            throw new IllegalArgumentException("Not Found");
        }
        return e;
    }

    public Constant getConstant(int index, boolean createNew) throws IOException {
        Constant entry = this.poolEntries.get(index);
        if (entry == null) {
            if (createNew) {
                entry = new Constant(index);
                this.poolEntries.put(index, entry);
            }
        }
        return entry;
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
            visitor.visitField(accessFlags, name, descriptor, attributes);
        }

    }

    public void visitMethod(int accessFlags, String name, String descriptor, MethodAttribute[] attributes) {
        if (visitor != null) {
            visitor.visitMethod(accessFlags, name, descriptor, attributes);
        }
    }

    public static interface Visitor {

        public default void visitVersion(int major, int minor) {
        }

        public default void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {

        }

        public default void visitField(int accessFlags, String name, String descriptor, FieldAttribute[] attributes) {

        }

        public default void visitMethod(int accessFlags, String name, String descriptor, MethodAttribute[] attributes) {

        }
    }
}
