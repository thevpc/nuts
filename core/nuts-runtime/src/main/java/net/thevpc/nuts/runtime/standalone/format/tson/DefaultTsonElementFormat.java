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

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
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
                return decorateTsonElement(Tson.ofInt(elem.asIntValue().get()), elem);
            }
            case LONG: {
                return decorateTsonElement(Tson.ofLong(elem.asLongValue().get()), elem);
            }
            case FLOAT: {
                return decorateTsonElement(Tson.ofFloat(elem.asFloatValue().get()), elem);
            }
            case DOUBLE: {
                return decorateTsonElement(Tson.ofDouble(elem.asDoubleValue().get()), elem);
            }
            case BYTE: {
                return decorateTsonElement(Tson.ofByte(elem.asByteValue().get()), elem);
            }
            case LOCAL_DATE: {
                return decorateTsonElement(Tson.ofLocalDate(elem.asPrimitive().get().asLocalDateValue().get()), elem);
            }
            case LOCAL_DATETIME: {
                return decorateTsonElement(Tson.ofLocalDatetime(elem.asPrimitive().get().asLocalDateTimeValue().get()), elem);
            }
            case LOCAL_TIME: {
                return decorateTsonElement(Tson.ofLocalTime(elem.asPrimitive().get().asLocalTimeValue().get()), elem);
            }
            case REGEX: {
                return decorateTsonElement(Tson.ofRegex(elem.asStringValue().get()), elem);
            }
            case BIG_INTEGER: {
                return decorateTsonElement(Tson.ofBigInt(elem.asBigIntValue().get()), elem);
            }
            case BIG_DECIMAL: {
                return decorateTsonElement(Tson.ofBigDecimal(elem.asBigDecimalValue().get()), elem);
            }
            case SHORT: {
                return decorateTsonElement(Tson.ofShort(elem.asShortValue().get()), elem);
            }
            case BOOLEAN: {
                return decorateTsonElement(Tson.ofBoolean(elem.asBooleanValue().get()), elem);
            }
            case CHAR: {
                return decorateTsonElement(Tson.ofChar(elem.asCharValue().get()), elem);
            }
            case INSTANT: {
                return decorateTsonElement(Tson.ofInstant(elem.asInstantValue().get()), elem);
            }
            case BIG_COMPLEX: {
                NBigComplex v = elem.asBigComplexValue().get();
                return decorateTsonElement(Tson.ofBigComplex(v.real(), v.imag()), elem);
            }
            case DOUBLE_COMPLEX: {
                NDoubleComplex v = elem.asDoubleComplexValue().get();
                return decorateTsonElement(Tson.ofDoubleComplex(v.real(), v.imag()), elem);
            }
            case FLOAT_COMPLEX: {
                NFloatComplex v = elem.asFloatComplexValue().get();
                return decorateTsonElement(Tson.ofFloatComplex(v.real(), v.imag()), elem);
            }

            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                NArrayElement ee = elem.asArray().get();
                return decorateTsonElement(
                        Tson.ofArrayBuilder()
                                .name(ee.name())
                                .addParams(
                                        ee.params() == null ? null :
                                                ee.params().stream().map(x -> toTson(x)).toArray(TsonElement[]::new)
                                ).addAll(ee.children().stream().map(x -> toTson(x)).toArray(TsonElement[]::new))
                                .build()
                        , elem);
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement ee = elem.asObject().get();
                return decorateTsonElement(
                        Tson.ofArrayBuilder()
                                .name(ee.name())
                                .addParams(
                                        ee.params() == null ? null :
                                                ee.params().stream().map(x -> toTson(x)).toArray(TsonElement[]::new)
                                ).addAll(ee.children().stream().map(x -> toTson(x)).toArray(TsonElement[]::new))
                                .build()
                        , elem);
            }
            case UPLET:
            case NAMED_UPLET: {
                NUpletElement ee = elem.asUplet().get();
                return decorateTsonElement(
                        Tson.ofArrayBuilder()
                                .name(ee.name())
                                .addAll(ee.children().stream().map(x -> toTson(x)).toArray(TsonElement[]::new))
                                .build()
                        , elem);
            }
            case PAIR: {
                NPairElement ee = elem.asPair().get();
                return decorateTsonElement(
                        Tson.ofPair(
                                toTson(ee.key()),
                                toTson(ee.value())
                        )
                        , elem);
            }
            case NAME: {
                return decorateTsonElement(Tson.ofName(elem.asNamed().get().name()), elem);
            }
            case STRING: {
                return decorateTsonElement(Tson.ofString(
                        elem.asStr().get().stringValue(),
                        toTsonStringLayout(elem.asStr().get().stringLayout())
                ), elem);
            }
        }
        throw new IllegalArgumentException("not implemented");
    }

    private TsonStringLayout toTsonStringLayout(NStringLayout layout) {
        if (layout == null) {
            return null;
        }
        switch (layout) {
            case DOUBLE_QUOTE:
                return TsonStringLayout.DOUBLE_QUOTE;
            case ANTI_QUOTE:
                return TsonStringLayout.ANTI_QUOTE;
            case TRIPLE_DOUBLE_QUOTE:
                return TsonStringLayout.TRIPLE_DOUBLE_QUOTE;
            case TRIPLE_SINGLE_QUOTE:
                return TsonStringLayout.TRIPLE_SINGLE_QUOTE;
            case TRIPLE_ANTI_QUOTE:
                return TsonStringLayout.TRIPLE_ANTI_QUOTE;
            case SINGLE_QUOTE:
                return TsonStringLayout.SINGLE_QUOTE;
            case SINGLE_LINE:
                return TsonStringLayout.SINGLE_LINE;
        }
        throw new IllegalArgumentException("not implemented Tson Type " + layout);
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
        for (TsonComment tc : comments.leadingComments()) {
            builder.addLeadingComment(new NElementCommentImpl(
                    tc.type() == TsonCommentType.SINGLE_LINE ? NElementCommentType.SINGLE_LINE
                            : tc.type() == TsonCommentType.MULTI_LINE ? NElementCommentType.MULTI_LINE
                            : NElementCommentType.SINGLE_LINE,
                    tc.text()
            ));
        }
        for (TsonComment tc : comments.trailingComments()) {
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
                return decorateNElement(elems.ofString(tsonElem.toStr().stringValue(), toNStringLayout(tsonElem.toStr().layout())), tsonElem);
            }
            case BOOLEAN: {
                return decorateNElement(elems.ofBoolean(tsonElem.booleanValue()), tsonElem);
            }
            case INSTANT:{
                return decorateNElement(elems.ofInstant(tsonElem.instantValue()), tsonElem);
            }
            case LOCAL_DATE:{
                return decorateNElement(elems.ofLocalDate(tsonElem.localDateValue()), tsonElem);
            }
            case LOCAL_TIME:{
                return decorateNElement(elems.ofLocalTime(tsonElem.localTimeValue()), tsonElem);
            }
            case LOCAL_DATETIME:{
                return decorateNElement(elems.ofLocalDateTime(tsonElem.localDateTimeValue()), tsonElem);
            }
            case BIG_COMPLEX:{
                TsonBigComplex v = tsonElem.toBigComplex();
                return decorateNElement(elems.ofBigComplex(v.real(),v.imag()), tsonElem);
            }
            case FLOAT_COMPLEX:{
                TsonFloatComplex v = tsonElem.toFloatComplex();
                return decorateNElement(elems.ofFloatComplex(v.real(),v.imag()), tsonElem);
            }
            case DOUBLE_COMPLEX:{
                TsonDoubleComplex v = tsonElem.toDoubleComplex();
                return decorateNElement(elems.ofDoubleComplex(v.real(),v.imag()), tsonElem);
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
            case UPLET:
            case NAMED_UPLET: {
                TsonUplet obj = tsonElem.toUplet();
                NUpletElementBuilder u = elems.ofUpletBuilder();
                for (TsonElement item : obj) {
                    u.add(toNElem(item));
                }
                if (obj.isNamed()) {
                    u.name(obj.name());
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

    private NStringLayout toNStringLayout(TsonStringLayout layout) {
        if (layout == null) {
            return null;
        }
        switch (layout) {
            case DOUBLE_QUOTE:
                return NStringLayout.DOUBLE_QUOTE;
            case ANTI_QUOTE:
                return NStringLayout.ANTI_QUOTE;
            case TRIPLE_DOUBLE_QUOTE:
                return NStringLayout.TRIPLE_DOUBLE_QUOTE;
            case TRIPLE_SINGLE_QUOTE:
                return NStringLayout.TRIPLE_SINGLE_QUOTE;
            case TRIPLE_ANTI_QUOTE:
                return NStringLayout.TRIPLE_ANTI_QUOTE;
            case SINGLE_QUOTE:
                return NStringLayout.SINGLE_QUOTE;
            case SINGLE_LINE:
                return NStringLayout.SINGLE_LINE;
        }
        throw new IllegalArgumentException("not implemented Tson Type " + layout);
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }

}
