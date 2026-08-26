package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.net.NWebRequest;
import net.thevpc.nuts.net.NWebRequestBody;

public class NWebRequestBodyImpl implements NWebRequestBody {
    private NInputSource body;

    private String contentType;

    private String encoding;

    private String name;

    private String fileName;

    private String stringValue;

    private NWebRequest request;

    public NWebRequestBodyImpl(NWebRequest request) {
        this.request = request;
    }

    @Override
    public NInputSource body() {
        return body;
    }

    @Override
    public NWebRequestBody body(NInputSource body) {
        this.body = body;
        return this;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public NWebRequestBody contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String encoding() {
        return encoding;
    }

    @Override
    public NWebRequestBody encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NWebRequestBody name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public NWebRequestBody fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String stringValue() {
        return stringValue;
    }

    @Override
    public NWebRequestBody stringValue(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }

    @Override
    public String contentDisposition() {
        StringBuilder sb = new StringBuilder("form-data");
        if (!NBlankable.isBlank(name())) {
            sb.append("; name=");
            sb.append(NStringUtils.formatStringLiteral(name(), NElementType.DOUBLE_QUOTED_STRING));
        }
        if (!NBlankable.isBlank(fileName())) {
            sb.append("; filename=");
            sb.append(NStringUtils.formatStringLiteral(fileName(), NElementType.DOUBLE_QUOTED_STRING));
        }
        return sb.toString();
    }


    @Override
    public NWebRequest end() {
        return request;
    }
}
