/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.tson;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.tson.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultTsonElementFormat implements NElementStreamFormat {

    private NWorkspace ws;

    public DefaultTsonElementFormat(NWorkspace ws) {
        this.ws = ws;
    }

    public NElement parseElement(String string, NElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(NPrintStream out, NElement data, boolean compact) {
        TsonWriter w = Tson.writer();
        w.setOptionCompact(compact);
        w.write(out.asPrintStream(), toTson(data));
    }


    private NElementAnnotation toNElemAnn(TsonAnnotation elem) {
        throw new IllegalArgumentException("not implemented");
    }


    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        TsonDocument tsonDocument = Tson.reader().readDocument(reader);
        return toNElem(tsonDocument.getContent());
    }

    public TsonElement toTson(NElement elem) {
        throw new IllegalArgumentException("not implemented");
    }

    private NElement toNElem(TsonElement elem) {
        NElements elems = NElements.of();
        switch (elem.type()) {
            case NULL: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofNull();
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BYTE: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofByte(elem.byteValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case SHORT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofShort(elem.shortValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case CHAR: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofChar(elem.charValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case INTEGER: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofInt(elem.intValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case LONG: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofLong(elem.longValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case FLOAT: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofFloat(elem.floatValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case DOUBLE: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofDouble(elem.doubleValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BIG_INTEGER: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofBigInteger(elem.bigIntegerValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BIG_DECIMAL: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofBigDecimal(elem.bigDecimalValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case STRING: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofString(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case BOOLEAN: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofBoolean(elem.booleanValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case REGEX: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofRegex(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case NAME: {
                TsonAnnotation[] annotations = elem.annotations();
                NPrimitiveElement u = elems.ofName(elem.stringValue());
                if (annotations.length > 0) {
                    return u.builder().addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u;
            }
            case ARRAY: {
                TsonAnnotation[] annotations = elem.annotations();
                TsonArray array = elem.toArray();
                NArrayElementBuilder u = elems.ofArrayBuilder();
                for (TsonElement item : array) {
                    u.add(toNElem(item));
                }
                if(array.isNamed()) {
                    u.setName(array.name());
                }
                if(array.isParametrized()) {
                    u.addArgs(array.params().toList().stream().map(x -> toNElem(x)).collect(Collectors.toList()));
                }
                if (annotations.length > 0) {
                    return u.addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u.build();
            }
            case OBJECT: {
                TsonAnnotation[] annotations = elem.annotations();
                TsonObject obj = elem.toObject();
                NObjectElementBuilder u = elems.ofObjectBuilder();
                for (TsonElement item : obj) {
                    u.add(toNElem(item));
                }
                if(obj.isNamed()) {
                    u.setName(obj.name());
                }
                if(obj.isParametrized()) {
                    u.addArgs(obj.params().toList().stream().map(x -> toNElem(x)).collect(Collectors.toList()));
                }
                if (annotations.length > 0) {
                    return u.addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return u.build();
            }
            case PAIR:{
                TsonAnnotation[] annotations = elem.annotations();
                TsonPair pair = elem.toPair();
                NPairElementBuilder b = elems.ofPairBuilder(toNElem(pair.key()), toNElem(pair.key()));
                if (annotations.length > 0) {
                    return b.addAnnotations(Arrays.stream(annotations).map(this::toNElemAnn).collect(Collectors.toList())).build();
                }
                return b.build();
            }
        }
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }

}
