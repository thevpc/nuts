package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonDocumentBuilderImpl;

public class TsonDocumentImpl implements TsonDocument {
    private TsonDocumentHeader header;
    private TsonElement content;

    public TsonDocumentImpl(TsonDocumentHeader header, TsonElement content) {
        this.header = header;
        this.content = content;
    }

    @Override
    public TsonDocumentHeader getHeader() {
        return header;
    }

    @Override
    public TsonElement getContent() {
        return content;
    }

    public TsonDocumentBuilder builder() {
        return new TsonDocumentBuilderImpl().setHeader(header).setValue(content);
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (!visitor.visit(this)) {
            return false;
        }
        if (!visitor.visit(header)) {
            return false;
        }
        if (!visitor.visit(content)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(boolean compact) {
        return compact ? Tson.COMPACT_FORMAT.format(this) : Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(TsonFormat format) {
        return format == null ? Tson.DEFAULT_FORMAT.format(this) : format.format(this);
    }

    @Override
    public int compareTo(TsonDocument o) {
        int i = header.compareTo(o.getHeader());
        if (i != 0) {
            return i;
        }
        i = content.compareTo(o.getContent());
        if (i != 0) {
            return i;
        }
        return 0;
    }
}
