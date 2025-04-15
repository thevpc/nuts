package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public class TsonStreamParserConfig {
    private boolean skipComments=false;
    private boolean skipHeader=false;
    private boolean rawComments=false;
    private TsonParserVisitor visitor= TsonParserVisitorEmpty.INSTANCE;

    public boolean isSkipComments() {
        return skipComments;
    }

    public TsonStreamParserConfig setSkipComments(boolean skipComments) {
        this.skipComments = skipComments;
        return this;
    }

    public boolean isSkipHeader() {
        return skipHeader;
    }

    public TsonStreamParserConfig setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
        return this;
    }

    public TsonParserVisitor getVisitor() {
        return visitor;
    }

    public TsonStreamParserConfig setVisitor(TsonParserVisitor visitor) {
        this.visitor = visitor;
        return this;
    }

    public boolean isRawComments() {
        return rawComments;
    }

    public TsonStreamParserConfig setRawComments(boolean rawComments) {
        this.rawComments = rawComments;
        return this;
    }
}
