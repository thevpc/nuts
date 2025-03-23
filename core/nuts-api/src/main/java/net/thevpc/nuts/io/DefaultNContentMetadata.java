/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class DefaultNContentMetadata implements NContentMetadata {

    private Supplier<Long> contentLength;
    private Supplier<NMsg> message;
    private Supplier<String> contentType;
    private Supplier<String> name;
    private Supplier<String> kind;
    private Supplier<String> charset;

    public DefaultNContentMetadata(NContentMetadata other) {
        if (other != null) {
            this.contentLength = () -> other.getContentLength().orNull();
            this.name = () -> other.getName().orNull();
            this.message = () -> other.getMessage().orNull();
            this.kind = () -> other.getKind().orNull();
            this.contentType = () -> other.getContentType().orNull();
            this.charset = () -> other.getCharset().orNull();
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
        if (contentLength != null && contentLength.get() != null && contentLength.get() >= 0) {
            return false;
        }
        if (message != null && message.get() != null) {
            return false;
        }
        if (contentType != null && contentType.get() != null) {
            return false;
        }
        if (name != null && name.get() != null) {
            return false;
        }
        if (kind != null && kind.get() != null) {
            return false;
        }
        if (charset != null && charset.get() != null) {
            return false;
        }
        return true;
    }

    //    public DefaultNContentMetadata(NMsg message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(),
//                () -> message == null ? null : message.toNutsString(session), contentLength, contentType, kind);
//    }
//
//    public DefaultNContentMetadata(NutsString message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(), s -> message, contentLength, contentType, kind);
//    }
//
//
    public DefaultNContentMetadata(NMsg message, Long contentLength, String contentType, String charset, String kind) {
        this.contentLength = contentLength == null ? null : () -> contentLength;
        this.name = message == null ? null : () -> NText.of(message).filteredText();
        this.message = message == null ? null : () -> message;
        this.kind = kind == null ? null : () -> kind;
        this.contentType = contentType == null ? null : () -> contentType;
    }

    public DefaultNContentMetadata(String name, NMsg message, Long contentLength, String contentType, String charset, String kind) {
        this.contentLength = contentLength == null ? null : () -> contentLength;
        this.name = name == null ? null : () -> name;
        this.message = message == null ? null : () -> message;
        this.kind = kind == null ? null : () -> kind;
        this.contentType = contentType == null ? null : () -> contentType;
    }

    @Override
    public NOptional<Long> getContentLength() {
        return NOptional.ofNamed(contentLength == null ? null : contentLength.get(), "contentLength");
    }

    @Override
    public NOptional<String> getContentType() {
        return NOptional.ofNamed(contentType == null ? null : contentType.get(), "contentType");
    }

    @Override
    public NOptional<String> getName() {
        return NOptional.ofNamed(name == null ? null : name.get(), "name");
    }

    public NOptional<String> getKind() {
        return NOptional.ofNamed(kind == null ? null : kind.get(), "kind");
    }

    public NOptional<NMsg> getMessage() {
        return NOptional.ofNamed(message == null ? null : message.get(), "message");
    }

    @Override
    public NOptional<String> getCharset() {
        return NOptional.ofNamed(charset == null ? null : charset.get(), "encoding");
    }

    public NContentMetadata setKind(String kind) {
        this.kind = () -> kind;
        return this;
    }

    public NContentMetadata setContentLength(Long contentLength) {
        this.contentLength = () -> contentLength;
        return this;
    }

    public NContentMetadata setMessage(NMsg message) {
        this.message = () -> message;
        return this;
    }

    public DefaultNContentMetadata setContentType(String contentType) {
        this.contentType = () -> contentType;
        return this;
    }

    public DefaultNContentMetadata setName(String name) {
        this.name = () -> name;
        return this;
    }

    public DefaultNContentMetadata setCharset(String charset) {
        this.charset = () -> charset;
        return this;
    }

    @Override
    public String toString() {
        if (message != null) {
            NMsg obj = message.get();
            if (obj != null) {
                return NText.of((NMsg) obj).filteredText();
            }
        }
        if (name != null) {
            String n = name.get();
            if (n != null) {
                return n;
            }
        }
        return "InputSourceMetadata";
    }
}
