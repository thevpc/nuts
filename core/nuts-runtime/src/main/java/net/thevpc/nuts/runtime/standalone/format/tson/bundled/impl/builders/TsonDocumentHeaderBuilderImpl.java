package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonDocumentHeaderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TsonDocumentHeaderBuilderImpl implements TsonDocumentHeaderBuilder {
    private String version = null;
    private String encoding = null;
    private List<TsonElement> params = new ArrayList<>();

    public TsonDocumentHeaderBuilderImpl() {
    }

    @Override
    public TsonDocumentHeaderBuilderImpl reset() {
        version = null;
        encoding = null;
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilderImpl parse(TsonAnnotation a) {
        if (a.name().equals("tson")) {
            List<TsonElement> params = a.params();
            boolean acceptStr = true;
            if (params != null) {
                for (TsonElement param : params) {
                    switch (param.type()) {
                        case PAIR: {
                            acceptStr = false;
                            TsonPair kv = param.toPair();
                            switch (kv.key().stringValue()) {
                                case "version": {
                                    version = kv.value().stringValue();
                                    break;
                                }
                                case "encoding": {
                                    encoding = kv.value().stringValue();
                                    break;
                                }
                                default: {
                                    this.params.add(kv);
                                }
                            }
                            break;
                        }
                        case DOUBLE_QUOTED_STRING:
                        case SINGLE_QUOTED_STRING:
                        case ANTI_QUOTED_STRING:
                        case TRIPLE_DOUBLE_QUOTED_STRING:
                        case TRIPLE_SINGLE_QUOTED_STRING:
                        case TRIPLE_ANTI_QUOTED_STRING:
                        case LINE_STRING:
                        case CHAR:
                        case NAME: {
                            if (acceptStr) {
                                String v = param.stringValue();
                                if (version == null && v.startsWith("v")) {
                                    version = v.substring(1);
                                } else if (encoding == null) {
                                    String y = param.stringValue();
                                    if (Charset.availableCharsets().containsKey(y)) {
                                        encoding = y;
                                    }
                                    acceptStr = false;
                                } else {
                                    acceptStr = false;
                                    this.params.add(param);
                                }
                            } else {
                                this.params.add(param);
                            }
                            break;
                        }
                        default: {
                            acceptStr = false;
                            this.params.add(param);
                        }
                    }
                }
            }
        }
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilderImpl setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilderImpl setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public TsonElement[] getParams() {
        return params.toArray(TsonUtils.TSON_ELEMENTS_EMPTY_ARRAY);
    }

    @Override
    public TsonDocumentHeaderBuilder with(TsonElementBase... elements) {
        return addParams(elements);
    }

    @Override
    public TsonDocumentHeaderBuilder addParam(TsonElementBase element) {
        if(element!=null) {
            params.add(Tson.of(element));
        }
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilder removeParam(TsonElementBase element) {
        params.remove(Tson.of(element));
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilder addParam(TsonElementBase element, int index) {
        params.add(index, Tson.of(element));
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilder removeParamAt(int index) {
        params.remove(index);
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilder addParams(TsonElementBase... element) {
        for (TsonElementBase tsonElement : element) {
            addParam(tsonElement);
        }
        return this;
    }

    @Override
    public TsonDocumentHeaderBuilder addParams(Iterable<? extends TsonElementBase> elements) {
        if(elements!=null) {
            for (TsonElementBase tsonElement : elements) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonAnnotation toAnnotation() {
        TsonAnnotationBuilder b = Tson.ofAnnotationBuilder().name("tson");
        if (version == null && encoding == null && params.isEmpty()) {
            b.add(Tson.ofString("v"+Tson.getVersion()));
        } else {
            b.add(Tson.ofPair("version", Tson.ofString(TsonUtils.isBlank(version) ? Tson.getVersion() : version.trim())));
            if(encoding!=null){
                b.add(Tson.ofPair("encoding", Tson.ofString(TsonUtils.isBlank(version) ? Tson.getVersion() : version.trim())));
            }
            for (TsonElement e : params) {
                b.add(e);
            }
        }
        return b.build();
    }

    @Override
    public TsonDocumentHeader build() {
        return new TsonDocumentHeaderImpl(
                TsonUtils.isBlank(version) ? Tson.getVersion() : version.trim(),
                encoding,
                params.toArray(TsonUtils.TSON_ELEMENTS_EMPTY_ARRAY)
        );
    }
}
