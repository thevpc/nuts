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
import net.thevpc.nuts.runtime.standalone.elem.NElementAnnotationImpl;
import net.thevpc.nuts.runtime.standalone.elem.NElementCommentImpl;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.tson.*;
import net.thevpc.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
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
        return new NElementAnnotationImpl(
                elem.name(),
                elem.params() == null ? null : elem.params().toList().stream().map(x -> toNElem(x)).toArray(NElement[]::new)
        );
    }


    private TsonAnnotation toTsonAnn(NElementAnnotation elem) {
        return Tson.ofAnnotation(
                elem.name(),
                elem.params() == null ? null : elem.params().stream().map(x -> toTson(x)).toArray(TsonElementBase[]::new)
        );
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        TsonDocument tsonDocument = Tson.reader().readDocument(reader);
        return toNElem(tsonDocument.getContent());
    }

    public TsonElement toTson(NElement elem) {
        if (elem == null) {
            return Tson.ofNull();
        }
        switch (elem.type()) {
            case NULL: {
                return decorateTsonElement(Tson.ofNull(), elem);
            }
            case INTEGER: {
                return decorateTsonElement(Tson.ofInt(elem.asInt().get()), elem);
            }
            case LONG: {
                return decorateTsonElement(Tson.ofLong(elem.asLong().get()), elem);
            }
            case FLOAT: {
                return decorateTsonElement(Tson.ofFloat(elem.asFloat().get()), elem);
            }
            case DOUBLE: {
                return decorateTsonElement(Tson.ofDouble(elem.asDouble().get()), elem);
            }
            case BYTE: {
                return decorateTsonElement(Tson.ofByte(elem.asByte().get()), elem);
            }
            case LOCAL_DATE: {
                return decorateTsonElement(Tson.ofLocalDate(elem.asPrimitive().get().asLocalDate().get()), elem);
            }
            case LOCAL_DATETIME: {
                return decorateTsonElement(Tson.ofLocalDatetime(elem.asPrimitive().get().asLocalDateTime().get()), elem);
            }
            case LOCAL_TIME: {
                return decorateTsonElement(Tson.ofLocalTime(elem.asPrimitive().get().asLocalTime().get()), elem);
            }
            case REGEX: {
                return decorateTsonElement(Tson.ofRegex(elem.asString().get()), elem);
            }
            case BIG_INTEGER: {
                return decorateTsonElement(Tson.ofBigInt(elem.asBigInt().get()), elem);
            }
            case BIG_DECIMAL: {
                return decorateTsonElement(Tson.ofBigDecimal(elem.asBigDecimal().get()), elem);
            }
            case ARRAY: {
                return decorateTsonElement(Tson.ofNull(), elem);
            }
        }
        throw new IllegalArgumentException("not implemented");
    }

    private TsonElement decorateTsonElement(TsonElement t, NElement fromElem) {
        if (fromElem instanceof NNumberElement) {
            NNumberElement en = (NNumberElement) fromElem;
            TsonNumberLayout nf = TsonNumberLayout.DECIMAL;
            String nSuffix = null;

            if (en.numberLayout() != null && en.numberLayout() != NNumberLayout.DECIMAL) {
                switch (en.numberLayout()) {
                    case DECIMAL:
                        nf = (TsonNumberLayout.DECIMAL);
                        break;
                    case HEXADECIMAL:
                        nf = (TsonNumberLayout.HEXADECIMAL);
                        break;
                    case BINARY:
                        nf = (TsonNumberLayout.BINARY);
                        break;
                    case OCTAL:
                        nf = (TsonNumberLayout.OCTAL);
                        break;
                }
            }
            if (!NBlankable.isBlank(en.numberSuffix())) {
                nSuffix = (en.numberSuffix());
            }
            if ((nf != null && nf != TsonNumberLayout.DECIMAL) || nSuffix != null) {
                t = Tson.ofNumber(en.numberValue(), nf, nSuffix);
            }
        }

        List<NElementAnnotation> na = fromElem.annotations();
        NElementComments nc = fromElem.comments();
        if (na.isEmpty() && nc.isEmpty()) {
            return t;
        }
        TsonElementBuilder b = t.builder();
        b.addAnnotations(
                na.stream().map(x -> toTsonAnn(x)).collect(Collectors.toList())
        );
        TsonComments tc = TsonComments.BLANK;
        tc.addLeading(nc.leadingComments().stream().map(x -> new TsonComment(
                x.type() == NElementCommentType.SINGLE_LINE ? TsonCommentType.SINGLE_LINE
                        : x.type() == NElementCommentType.MULTI_LINE ? TsonCommentType.MULTI_LINE
                        : TsonCommentType.SINGLE_LINE,
                x.text()
        )).toArray(TsonComment[]::new));
        tc.addTrailing(nc.trailingComments().stream().map(x -> new TsonComment(
                x.type() == NElementCommentType.SINGLE_LINE ? TsonCommentType.SINGLE_LINE
                        : x.type() == NElementCommentType.MULTI_LINE ? TsonCommentType.MULTI_LINE
                        : TsonCommentType.SINGLE_LINE,
                x.text()
        )).toArray(TsonComment[]::new));
        b.setComments(tc);


        return b.build();
    }

    private NElement decorateNElement(NElement elem, TsonElement fromTson) {
        List<TsonAnnotation> annotations = fromTson.annotations();
        TsonComments comments = fromTson.comments();
        if (annotations.isEmpty() && comments.isEmpty()) {
            return elem;
        }
        NElementBuilder builder = elem.builder();
        builder.addAnnotations(annotations.stream().map(this::toNElemAnn).collect(Collectors.toList())).build();
        for (TsonComment tc : fromTson.comments().leadingComments()) {
            builder.addLeadingComment(new NElementCommentImpl(
                    tc.type() == TsonCommentType.SINGLE_LINE ? NElementCommentType.SINGLE_LINE
                            : tc.type() == TsonCommentType.MULTI_LINE ? NElementCommentType.MULTI_LINE
                            : NElementCommentType.SINGLE_LINE,
                    tc.text()
            ));
        }
        for (TsonComment tc : fromTson.comments().trailingComments()) {
            builder.addLeadingComment(new NElementCommentImpl(
                    tc.type() == TsonCommentType.SINGLE_LINE ? NElementCommentType.SINGLE_LINE
                            : tc.type() == TsonCommentType.MULTI_LINE ? NElementCommentType.MULTI_LINE
                            : NElementCommentType.SINGLE_LINE,
                    tc.text()
            ));
        }
        if (elem instanceof NNumberElement) {
            TsonNumber tn = (TsonNumber) fromTson;
            NPrimitiveElementBuilder nnb = (NPrimitiveElementBuilder) builder;
            if (tn.numberLayout() != null && tn.numberLayout() != TsonNumberLayout.DECIMAL) {
                switch (tn.numberLayout()) {
                    case DECIMAL:
                        nnb.numberLayout(NNumberLayout.DECIMAL);
                        break;
                    case HEXADECIMAL:
                        nnb.numberLayout(NNumberLayout.HEXADECIMAL);
                        break;
                    case BINARY:
                        nnb.numberLayout(NNumberLayout.BINARY);
                        break;
                    case OCTAL:
                        nnb.numberLayout(NNumberLayout.OCTAL);
                        break;
                }
            }
            if (!NBlankable.isBlank(tn.numberSuffix())) {
                nnb.numberSuffix(tn.numberSuffix());
            }
        }
        return builder.build();
    }

    private NElement toNElem(TsonElement tsonElem) {
        NElements elems = NElements.of();
        switch (tsonElem.type()) {
            case NULL: {
                return decorateNElement(elems.ofNull(), tsonElem);
            }
            case BYTE: {
                return decorateNElement(elems.ofByte(tsonElem.byteValue()), tsonElem);
            }
            case SHORT: {
                return decorateNElement(elems.ofShort(tsonElem.shortValue()), tsonElem);
            }
            case CHAR: {
                return decorateNElement(elems.ofChar(tsonElem.charValue()), tsonElem);
            }
            case INTEGER: {
                return decorateNElement(elems.ofInt(tsonElem.intValue()), tsonElem);
            }
            case LONG: {
                return decorateNElement(elems.ofLong(tsonElem.longValue()), tsonElem);
            }
            case FLOAT: {
                return decorateNElement(elems.ofFloat(tsonElem.floatValue()), tsonElem);
            }
            case DOUBLE: {
                return decorateNElement(elems.ofDouble(tsonElem.doubleValue()), tsonElem);
            }
            case BIG_INTEGER: {
                return decorateNElement(elems.ofBigInteger(tsonElem.bigIntegerValue()), tsonElem);
            }
            case BIG_DECIMAL: {
                return decorateNElement(elems.ofBigDecimal(tsonElem.bigDecimalValue()), tsonElem);
            }
            case STRING: {
                return decorateNElement(elems.ofString(tsonElem.stringValue()), tsonElem);
            }
            case BOOLEAN: {
                return decorateNElement(elems.ofBoolean(tsonElem.booleanValue()), tsonElem);
            }
            case REGEX: {
                return decorateNElement(elems.ofRegex(tsonElem.stringValue()), tsonElem);
            }
            case NAME: {
                return decorateNElement(elems.ofName(tsonElem.stringValue()), tsonElem);
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                TsonArray array = tsonElem.toArray();
                NArrayElementBuilder u = elems.ofArrayBuilder();
                for (TsonElement item : array) {
                    u.add(toNElem(item));
                }
                if (array.isNamed()) {
                    u.name(array.name());
                }
                if (array.isParametrized()) {
                    u.addParams(array.params().toList().stream().map(x -> toNElem(x)).collect(Collectors.toList()));
                }
                return decorateNElement(u.build(), tsonElem);
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                TsonObject obj = tsonElem.toObject();
                NObjectElementBuilder u = elems.ofObjectBuilder();
                for (TsonElement item : obj) {
                    u.add(toNElem(item));
                }
                if (obj.isNamed()) {
                    u.name(obj.name());
                }
                if (obj.isParametrized()) {
                    u.addParams(obj.params().toList().stream().map(x -> toNElem(x)).collect(Collectors.toList()));
                }
                return decorateNElement(u.build(), tsonElem);
            }
            case PAIR: {
                TsonPair pair = tsonElem.toPair();
                NPairElementBuilder b = elems.ofPairBuilder(toNElem(pair.key()), toNElem(pair.value()));
                return decorateNElement(b.build(), tsonElem);
            }
        }
        throw new IllegalArgumentException("not implemented Tson Type " + tsonElem.type());
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }

}
