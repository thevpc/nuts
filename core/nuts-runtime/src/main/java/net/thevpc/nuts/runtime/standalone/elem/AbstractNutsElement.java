/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.time.Instant;

/**
 * @author thevpc
 */
public abstract class AbstractNutsElement implements NutsElement {

    protected transient NutsSession session;
    private NutsElementType type;

    public AbstractNutsElement(NutsElementType type, NutsSession session) {
        this.type = type;
        this.session = session;
    }

    @Override
    public NutsElementType type() {
        return type;
    }

    @Override
    public NutsPrimitiveElement asPrimitive() {
        if (this instanceof NutsPrimitiveElement) {
            return (NutsPrimitiveElement) this;
        }
        throw new IllegalStateException("unable to cast " + type().id() + " to primitive" + this);
    }

    @Override
    public NutsObjectElement asObject() {
        if (this instanceof NutsObjectElement) {
            return (NutsObjectElement) this;
        }
        throw new IllegalStateException("unable to cast " + type().id() + " to object: " + this);
    }

    public NutsCustomElement asCustom() {
        if (this instanceof NutsCustomElement) {
            return (NutsCustomElement) this;
        }
        throw new IllegalStateException("unable to cast " + type().id() + " to custom: " + this);
    }

    @Override
    public boolean isCustom() {
        return this instanceof NutsCustomElement;
    }

    @Override
    public NutsObjectElement asSafeObject() {
        return asSafeObject(false);
    }

    @Override
    public NutsArrayElement asSafeArray() {
        return asSafeArray(false);
    }

    @Override
    public NutsArrayElement asArray() {
        if (this instanceof NutsArrayElement) {
            return (NutsArrayElement) this;
        }
        throw new IllegalStateException("unable to cast " + type().id() + " to array" + this);
    }

    @Override
    public NutsObjectElement asSafeObject(boolean embed) {
        if (this instanceof NutsObjectElement) {
            return (NutsObjectElement) this;
        }
        if(embed && type()!=NutsElementType.NULL){
            return NutsElements.of(session)
                    .ofObject()
                    .set("value", this)
                    .build();
        }
        return NutsElements.of(session)
                .ofObject()
                .build();
    }

    @Override
    public NutsArrayElement asSafeArray(boolean embed) {
        if (this instanceof NutsArrayElement) {
            return (NutsArrayElement) this;
        }
        if(embed && type()!=NutsElementType.NULL){
            return NutsElements.of(session)
                    .ofArray()
                    .add(this)
                    .build();
        }
        return NutsElements.of(session)
                .ofArray()
                .build();
    }

    @Override
    public boolean isPrimitive() {
        NutsElementType t = type();
        return t != NutsElementType.ARRAY
                && t != NutsElementType.OBJECT
                && t != NutsElementType.CUSTOM
                ;
    }

    @Override
    public boolean isNumber() {
        NutsElementType t = type();
        switch (t) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
        }
        return false;
    }

//    @Override
//    public NutsString asNutsString() {
//        return asPrimitive().getNutsString();
//    }

    @Override
    public boolean isNull() {
        NutsElementType t = type();
        return t == NutsElementType.NULL;
    }

    @Override
    public boolean isString() {
        NutsElementType t = type();
        return t == NutsElementType.STRING;
    }

    @Override
    public boolean isByte() {
        return type() == NutsElementType.BYTE;
    }

    @Override
    public boolean isInt() {
        NutsElementType t = type();
        return t == NutsElementType.INTEGER;
    }

    @Override
    public boolean isLong() {
        return type() == NutsElementType.LONG;
    }

    @Override
    public boolean isShort() {
        return type() == NutsElementType.SHORT;
    }

    @Override
    public boolean isFloat() {
        return type() == NutsElementType.FLOAT;
    }

    @Override
    public boolean isDouble() {
        return type() == NutsElementType.DOUBLE;
    }

    @Override
    public boolean isObject() {
        NutsElementType t = type();
        return t == NutsElementType.OBJECT;
    }

    @Override
    public boolean isArray() {
        NutsElementType t = type();
        return t == NutsElementType.ARRAY;
    }

    @Override
    public boolean isInstant() {
        return type() == NutsElementType.INSTANT;
    }

    @Override
    public String asString() {
        return asPrimitive().getString();
    }

    @Override
    public boolean asBoolean() {
        return asPrimitive().getBoolean();
    }

    @Override
    public byte asByte() {
        return asPrimitive().getByte();
    }

    @Override
    public double asDouble() {
        return asPrimitive().getDouble();
    }

    @Override
    public float asFloat() {
        return asPrimitive().getFloat();
    }

    @Override
    public Instant asInstant() {
        return asPrimitive().getInstant();
    }



    @Override
    public Integer asSafeInt(Integer defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getInt(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Instant asSafeInstant(Instant defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getInstant(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Long asSafeLong(Long defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getLong(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Short asSafeShort(Short defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getShort(defaultValue);
        }
        return defaultValue;
    }



    @Override
    public Byte asSafeByte(Byte defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getByte(defaultValue);
        }
        return defaultValue;
    }


    @Override
    public Double asSafeDouble(Double defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getDouble(defaultValue);
        }
        return defaultValue;
    }


    @Override
    public Float asSafeFloat(Float defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getFloat(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public String asSafeString(String defaultValue) {
        if(isPrimitive()){
            return asPrimitive().getString(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public int asInt() {
        return asPrimitive().getInt();
    }

    @Override
    public long asLong() {
        return asPrimitive().getLong();
    }

    @Override
    public short asShort() {
        return asPrimitive().getShort();
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return this;
    }
}
