package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NQuoteType;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.web.NWebRequest;
import net.thevpc.nuts.web.NWebRequestBody;

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
    public NInputSource getBody() {
        return body;
    }

    @Override
    public NWebRequestBody setBody(NInputSource body) {
        this.body = body;
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public NWebRequestBody setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public NWebRequestBody setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NWebRequestBody setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public NWebRequestBody setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public NWebRequestBody setStringValue(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }

    @Override
    public String getContentDisposition() {
        StringBuilder sb = new StringBuilder("form-data");
        if (!NBlankable.isBlank(getName())) {
            sb.append("; name=");
            sb.append(NStringUtils.formatStringLiteral(getName(), NQuoteType.DOUBLE));
        }
        if (!NBlankable.isBlank(getFileName())) {
            sb.append("; filename=");
            sb.append(NStringUtils.formatStringLiteral(getFileName(), NQuoteType.DOUBLE));
        }
        return sb.toString();
    }


    @Override
    public NWebRequest end() {
        return request;
    }
}
