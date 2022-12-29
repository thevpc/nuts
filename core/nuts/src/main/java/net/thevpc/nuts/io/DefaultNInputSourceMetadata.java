/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

/**
 * @author thevpc
 */
public class DefaultNInputSourceMetadata implements NInputSourceMetadata {

    private Long contentLength;
    private NMsg message;
    private String contentType;
    private String name;
    private String kind;

    public DefaultNInputSourceMetadata(NInputSourceMetadata other) {
        if (other != null) {
            this.contentLength = other.getContentLength().orNull();
            this.name = other.getName().orNull();
            this.message = other.getMessage().orNull();
            this.kind = other.getKind().orNull();
            this.contentType = other.getContentType().orNull();
        } else {
            this.contentLength = null;
            this.name = null;
            this.message = null;
            this.kind = null;
            this.contentType = null;
        }
    }

    public DefaultNInputSourceMetadata() {
    }

//    public DefaultNInputSourceMetadata(NMsg message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(),
//                session -> message == null ? null : message.toNutsString(session), contentLength, contentType, kind);
//    }
//
//    public DefaultNInputSourceMetadata(NutsString message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(), s -> message, contentLength, contentType, kind);
//    }
//
//
    public DefaultNInputSourceMetadata(NMsg message, long contentLength, String contentType, String kind) {
        this.contentLength = contentLength;
        this.name = message ==null?null: message.toString();
        this.message = message;
        this.kind = kind;
        this.contentType = contentType;
    }
    public DefaultNInputSourceMetadata(String name, NMsg message, long contentLength, String contentType, String kind) {
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

    public NInputSourceMetadata setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public NInputSourceMetadata setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public NInputSourceMetadata setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    public DefaultNInputSourceMetadata setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public DefaultNInputSourceMetadata setName(String name) {
        this.name = name;
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
