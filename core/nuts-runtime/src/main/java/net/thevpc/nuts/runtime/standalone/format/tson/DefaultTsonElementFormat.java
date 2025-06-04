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

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.elem.item.NElementAnnotationImpl;
import net.thevpc.nuts.runtime.standalone.format.elem.item.NElementCommentImpl;
import net.thevpc.nuts.runtime.standalone.format.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.TsonElementsFactoryImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonAnnotationBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonObjectBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonPairImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.format.DefaultTsonFormatConfig;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.format.TsonFormatImplBuilder;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.ElementBuilderTsonParserVisitor;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc.JavaccHelper;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc.ParseException;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc.TsonStreamParserImpl;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NElementUtils;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultTsonElementFormat implements NElementStreamFormat {

    private static TsonElementsFactory factory = new TsonElementsFactoryImpl();

    public DefaultTsonElementFormat() {
    }

    public NElement parseElement(String string, NElementFactoryContext context) {
        if (string == null) {
            string="";
        }
        return parseElement(new StringReader(string), context);
    }

    public void write(NPrintStream out, NElement data, boolean compact) {
        TsonFormatImplBuilder ts = new TsonFormatImplBuilder();
        DefaultTsonFormatConfig c = new DefaultTsonFormatConfig();
        c.setCompact(compact);
        ts.setConfig(c);
        out.print(ts.build().format(toTson(data)));
    }

    @Override
    public NElement normalize(NElement e) {
        return e;
    }


    private NElementAnnotation toNElemAnn(TsonAnnotation elem) {
        List<TsonElement> params = elem.params();
        return new NElementAnnotationImpl(
                elem.name().orNull(),
                params == null ? null : params.stream().map(x -> toNElem(x)).toArray(NElement[]::new)
        );
    }


    private TsonAnnotation toTsonAnn(NElementAnnotation elem) {
        List<NElement> params = elem.params();
        TsonAnnotationBuilder b = new TsonAnnotationBuilderImpl().name(elem.name()).addAll(
                params == null ? null : params.stream().map(x -> toTson(x)).toArray(TsonElementBase[]::new)
        );
        return b.build();
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        TsonStreamParserConfig config = new TsonStreamParserConfig();
        ElementBuilderTsonParserVisitor r = new ElementBuilderTsonParserVisitor();
        TsonStreamParser source = fromReader(reader, null);
        config.setVisitor(r);
        source.setConfig(config);
        try {
            source.parseDocument();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
        TsonDocument document = r.getDocument();
        TsonElement e = document==null?null:document.getContent();
        return e == null ? NElement.ofNull() : toNElem(e);
    }

    public TsonElement[] toTsonElemArray(List<NElement> elems) {
        if (elems == null) {
            return null;
        }
        return elems.stream().map(x -> toTson(x)).toArray(TsonElement[]::new);
    }

    public TsonElement toTson(NElement elem) {
        if (elem == null) {
            return factory.ofNull();
        }
        switch (elem.type()) {
            case NULL: {
                return decorateTsonElement(factory.ofNull(), elem);
            }
            case INTEGER: {
                return decorateTsonElement(factory.ofInt(elem.asIntValue().get()), elem);
            }
            case LONG: {
                return decorateTsonElement(factory.ofLong(elem.asLongValue().get()), elem);
            }
            case FLOAT: {
                return decorateTsonElement(factory.ofFloat(elem.asFloatValue().get()), elem);
            }
            case DOUBLE: {
                return decorateTsonElement(factory.ofDouble(elem.asDoubleValue().get()), elem);
            }
            case BYTE: {
                return decorateTsonElement(factory.ofByte(elem.asByteValue().get()), elem);
            }
            case LOCAL_DATE: {
                return decorateTsonElement(factory.ofLocalDate(elem.asPrimitive().get().asLocalDateValue().get()), elem);
            }
            case LOCAL_DATETIME: {
                return decorateTsonElement(factory.ofLocalDatetime(elem.asPrimitive().get().asLocalDateTimeValue().get()), elem);
            }
            case LOCAL_TIME: {
                return decorateTsonElement(factory.ofLocalTime(elem.asPrimitive().get().asLocalTimeValue().get()), elem);
            }
            case REGEX: {
                return decorateTsonElement(factory.ofRegex(elem.asStringValue().get()), elem);
            }
            case BIG_INTEGER: {
                return decorateTsonElement(factory.ofBigInt(elem.asBigIntValue().get()), elem);
            }
            case BIG_DECIMAL: {
                return decorateTsonElement(factory.ofBigDecimal(elem.asBigDecimalValue().get()), elem);
            }
            case SHORT: {
                return decorateTsonElement(factory.ofShort(elem.asShortValue().get()), elem);
            }
            case BOOLEAN: {
                return decorateTsonElement(factory.ofBoolean(elem.asBooleanValue().get()), elem);
            }
            case CHAR: {
                return decorateTsonElement(factory.ofChar(elem.asCharValue().get()), elem);
            }
            case INSTANT: {
                return decorateTsonElement(factory.ofInstant(elem.asInstantValue().get()), elem);
            }
            case BIG_COMPLEX: {
                NBigComplex v = elem.asBigComplexValue().get();
                return decorateTsonElement(factory.ofBigComplex(v.real(), v.imag()), elem);
            }
            case DOUBLE_COMPLEX: {
                NDoubleComplex v = elem.asDoubleComplexValue().get();
                return decorateTsonElement(factory.ofDoubleComplex(v.real(), v.imag()), elem);
            }
            case FLOAT_COMPLEX: {
                NFloatComplex v = elem.asFloatComplexValue().get();
                return decorateTsonElement(factory.ofFloatComplex(v.real(), v.imag()), elem);
            }

            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                NArrayElement ee = elem.asArray().get();
                return decorateTsonElement(
                        factory.ofArrayBuilder()
                                .name(ee.name().orNull())
                                .addParams(toTsonElemArray(ee.params().orNull()))
                                .addAll(toTsonElemArray(ee.children()))
                                .build()
                        , elem);
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement ee = elem.asObject().get();
                return decorateTsonElement(
                        new TsonObjectBuilderImpl()
                                .name(ee.name().orNull())
                                .addParams(toTsonElemArray(ee.params().orNull()))
                                .addAll(toTsonElemArray(ee.children()))
                                .build()
                        , elem);
            }
            case UPLET:
            case NAMED_UPLET: {
                NUpletElement ee = elem.asUplet().get();
                return decorateTsonElement(
                        factory.ofUpletBuilder()
                                .name(ee.name().orNull())
                                .addAll(toTsonElemArray(ee.children()))
                                .build()
                        , elem);
            }
            case PAIR: {
                NPairElement ee = elem.asPair().get();
                return decorateTsonElement(
                        new TsonPairImpl(
                                toTson(ee.key()),
                                toTson(ee.value())
                        )
                        , elem);
            }
            case NAME: {
                String s = elem.asStringValue().get();
                if(NElementUtils.isValidElementName(s, NContentType.TSON)) {
                    return decorateTsonElement(factory.ofName(s), elem);
                }else{
                    //some names include ':' and they are not supported in tson
                    return decorateTsonElement(factory.ofDoubleQuotedString(s), elem);
                }
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING: {
                return decorateTsonElement(factory.ofString(
                        toTsonStringLayout(elem.asStr().get().type()),
                        elem.asStr().get().stringValue()
                ), elem);
            }
        }
        throw new IllegalArgumentException("not implemented");
    }

    private TsonElementType toTsonStringLayout(NElementType layout) {
        if (layout == null) {
            return null;
        }
        switch (layout) {
            case DOUBLE_QUOTED_STRING:
                return TsonElementType.DOUBLE_QUOTED_STRING;
            case ANTI_QUOTED_STRING:
                return TsonElementType.ANTI_QUOTED_STRING;
            case TRIPLE_DOUBLE_QUOTED_STRING:
                return TsonElementType.TRIPLE_DOUBLE_QUOTED_STRING;
            case TRIPLE_SINGLE_QUOTED_STRING:
                return TsonElementType.TRIPLE_SINGLE_QUOTED_STRING;
            case TRIPLE_ANTI_QUOTED_STRING:
                return TsonElementType.TRIPLE_ANTI_QUOTED_STRING;
            case SINGLE_QUOTED_STRING:
                return TsonElementType.SINGLE_QUOTED_STRING;
            case LINE_STRING:
                return TsonElementType.LINE_STRING;
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
                t = factory.ofNumber(en.numberValue(), nf, nSuffix);
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
        tc = tc.addLeading(nc.leadingComments().stream().map(x -> new TsonComment(
                x.type() == NElementCommentType.SINGLE_LINE ? TsonCommentType.SINGLE_LINE
                        : x.type() == NElementCommentType.MULTI_LINE ? TsonCommentType.MULTI_LINE
                        : TsonCommentType.SINGLE_LINE,
                x.text()
        )).toArray(TsonComment[]::new));
        tc = tc.addTrailing(nc.trailingComments().stream().map(x -> new TsonComment(
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
                return decorateNElement(NElement.ofNull(), tsonElem);
            }
            case BYTE: {
                return decorateNElement(NElement.ofByte(tsonElem.byteValue(), toTsonNumberLayout(((TsonNumber) tsonElem).numberLayout()),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case SHORT: {
                return decorateNElement(NElement.ofShort(tsonElem.shortValue(), toTsonNumberLayout(((TsonNumber) tsonElem).numberLayout()),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case CHAR: {
                return decorateNElement(NElement.ofChar(tsonElem.charValue()), tsonElem);
            }
            case INTEGER: {
                return decorateNElement(NElement.ofInt(tsonElem.intValue(), toTsonNumberLayout(((TsonNumber) tsonElem).numberLayout()),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case LONG: {
                return decorateNElement(NElement.ofLong(tsonElem.longValue(), toTsonNumberLayout(((TsonNumber) tsonElem).numberLayout()),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case FLOAT: {
                return decorateNElement(NElement.ofFloat(tsonElem.floatValue(),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case DOUBLE: {
                return decorateNElement(NElement.ofDouble(tsonElem.doubleValue(),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case BIG_INTEGER: {
                return decorateNElement(NElement.ofBigInteger(tsonElem.bigIntegerValue(), toTsonNumberLayout(((TsonNumber) tsonElem).numberLayout()),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case BIG_DECIMAL: {
                return decorateNElement(NElement.ofBigDecimal(tsonElem.bigDecimalValue(),((TsonNumber)tsonElem).numberSuffix()), tsonElem);
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING: {
                return decorateNElement(NElement.ofString(tsonElem.toStr().stringValue(), toNStringLayout(tsonElem.toStr().type())), tsonElem);
            }
            case BOOLEAN: {
                return decorateNElement(NElement.ofBoolean(tsonElem.booleanValue()), tsonElem);
            }
            case INSTANT: {
                return decorateNElement(NElement.ofInstant(tsonElem.instantValue()), tsonElem);
            }
            case LOCAL_DATE: {
                return decorateNElement(NElement.ofLocalDate(tsonElem.localDateValue()), tsonElem);
            }
            case LOCAL_TIME: {
                return decorateNElement(NElement.ofLocalTime(tsonElem.localTimeValue()), tsonElem);
            }
            case LOCAL_DATETIME: {
                return decorateNElement(NElement.ofLocalDateTime(tsonElem.localDateTimeValue()), tsonElem);
            }
            case BIG_COMPLEX: {
                TsonBigComplex v = tsonElem.toBigComplex();
                return decorateNElement(NElement.ofBigComplex(v.real(), v.imag()), tsonElem);
            }
            case FLOAT_COMPLEX: {
                TsonFloatComplex v = tsonElem.toFloatComplex();
                return decorateNElement(NElement.ofFloatComplex(v.real(), v.imag()), tsonElem);
            }
            case DOUBLE_COMPLEX: {
                TsonDoubleComplex v = tsonElem.toDoubleComplex();
                return decorateNElement(NElement.ofDoubleComplex(v.real(), v.imag()), tsonElem);
            }
            case REGEX: {
                return decorateNElement(NElement.ofRegex(tsonElem.stringValue()), tsonElem);
            }
            case NAME: {
                return decorateNElement(NElement.ofName(tsonElem.stringValue()), tsonElem);
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                TsonArray array = tsonElem.toArray();
                NArrayElementBuilder u = NElement.ofArrayBuilder();
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
                NObjectElementBuilder u = NElement.ofObjectBuilder();
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
                NUpletElementBuilder u = NElement.ofUpletBuilder();
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
                NPairElementBuilder b = NElement.ofPairBuilder(toNElem(pair.key()), toNElem(pair.value()));
                return decorateNElement(b.build(), tsonElem);
            }
        }
        throw new IllegalArgumentException("not implemented Tson Type " + tsonElem.type());
    }

    private static NNumberLayout toTsonNumberLayout(TsonNumberLayout tsonElem) {
        if(tsonElem==null){
            return null;
        }
        switch (tsonElem) {
            case OCTAL:return NNumberLayout.OCTAL;
            case DECIMAL:return NNumberLayout.DECIMAL;
            case HEXADECIMAL:return NNumberLayout.HEXADECIMAL;
            case BINARY:return NNumberLayout.BINARY;
        }
        throw new IllegalArgumentException("not implemented TsonNumberLayout " + tsonElem);
    }

    private NElementType toNStringLayout(TsonElementType layout) {
        if (layout == null) {
            return null;
        }
        switch (layout) {
            case DOUBLE_QUOTED_STRING:
                return NElementType.DOUBLE_QUOTED_STRING;
            case ANTI_QUOTED_STRING:
                return NElementType.ANTI_QUOTED_STRING;
            case TRIPLE_DOUBLE_QUOTED_STRING:
                return NElementType.TRIPLE_DOUBLE_QUOTED_STRING;
            case TRIPLE_SINGLE_QUOTED_STRING:
                return NElementType.TRIPLE_SINGLE_QUOTED_STRING;
            case TRIPLE_ANTI_QUOTED_STRING:
                return NElementType.TRIPLE_ANTI_QUOTED_STRING;
            case SINGLE_QUOTED_STRING:
                return NElementType.SINGLE_QUOTED_STRING;
            case LINE_STRING:
                return NElementType.LINE_STRING;
        }
        throw new IllegalArgumentException("not implemented Tson Type " + layout);
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }


    public TsonStreamParser fromReader(Reader reader, Object source) {
        TsonStreamParserImpl p = new TsonStreamParserImpl(reader);
        p.source(source);
        return new TsonStreamParser() {
            @Override
            public Object source() {
                return source;
            }

            @Override
            public void setConfig(TsonStreamParserConfig config) {
                p.setConfig(config);
            }

            @Override
            public void parseElement() {
                try {
                    p.parseElement();
                } catch (ParseException e) {
                    throw JavaccHelper.createTsonParseException(e, source);
                }
            }

            @Override
            public void parseDocument() {
                try {
                    p.parseDocument();
                } catch (ParseException e) {
                    throw JavaccHelper.createTsonParseException(e, source);
                }
            }
        };
    }
}
