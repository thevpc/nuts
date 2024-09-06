/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 */
public class DefaultNContentMetadata implements NContentMetadata {

    private Long contentLength;
    private NMsg message;
    private String contentType;
    private String name;
    private String kind;
    private String charset;

    public DefaultNContentMetadata(NContentMetadata other) {
        if (other != null) {
            this.contentLength = other.getContentLength().orNull();
            this.name = other.getName().orNull();
            this.message = other.getMessage().orNull();
            this.kind = other.getKind().orNull();
            this.contentType = other.getContentType().orNull();
            this.charset = other.getCharset().orNull();
        } else {
            this.contentLength = null;
            this.name = null;
            this.message = null;
            this.kind = null;
            this.contentType = null;
            this.charset = null;
        }
    }

    public DefaultNContentMetadata() {
    }

    @Override
    public boolean isBlank() {
        if (contentLength != null && contentLength >= 0) {
            return false;
        }
        if (message != null) {
            return false;
        }
        if (contentType != null) {
            return false;
        }
        if (name != null) {
            return false;
        }
        if (kind != null) {
            return false;
        }
        if (charset != null) {
            return false;
        }
        return true;
    }

    //    public DefaultNContentMetadata(NMsg message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(),
//                session -> message == null ? null : message.toNutsString(session), contentLength, contentType, kind);
//    }
//
//    public DefaultNContentMetadata(NutsString message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(), s -> message, contentLength, contentType, kind);
//    }
//
//
    public DefaultNContentMetadata(NMsg message, Long contentLength, String contentType, String charset, String kind) {
        this.contentLength = contentLength;
        this.name = message == null ? null : message.toString();
        this.message = message;
        this.kind = kind;
        this.contentType = contentType;
    }

    public DefaultNContentMetadata(String name, NMsg message, Long contentLength, String contentType, String charset, String kind) {
        this.contentLength = contentLength;
        this.name = name;
        this.message = message;
        this.kind = kind;
        this.contentType = contentType;
    }

    @Override
    public NOptional<Long> getContentLength() {
        return NOptional.ofNamed(contentLength, "contentLength");
    }

    @Override
    public NOptional<String> getContentType() {
        return NOptional.ofNamed(contentType, "contentType");
    }

    @Override
    public NOptional<String> getName() {
        return NOptional.ofNamed(name, "name");
    }

    public NOptional<String> getKind() {
        return NOptional.ofNamed(kind, "kind");
    }

    public NOptional<NMsg> getMessage() {
        return NOptional.ofNamed(message, "message");
    }

    @Override
    public NOptional<String> getCharset() {
        return NOptional.ofNamed(charset, "encoding");
    }

    public NContentMetadata setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public NContentMetadata setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public NContentMetadata setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    public DefaultNContentMetadata setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public DefaultNContentMetadata setName(String name) {
        this.name = name;
        return this;
    }

    public DefaultNContentMetadata setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public String toString() {
        if (message != null) {
            return message.toString();
        }
        if (name != null) {
            return name;
        }
        return "InputSourceMetadata";
    }
}
