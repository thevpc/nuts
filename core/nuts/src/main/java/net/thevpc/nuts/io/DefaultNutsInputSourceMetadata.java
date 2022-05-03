/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

/**
 * @author thevpc
 */
public class DefaultNutsInputSourceMetadata implements NutsInputSourceMetadata {

    private Long contentLength;
    private NutsMessage message;
    private String contentType;
    private String name;
    private String kind;

    public DefaultNutsInputSourceMetadata(NutsInputSourceMetadata other) {
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

    public DefaultNutsInputSourceMetadata() {
    }

//    public DefaultNutsInputSourceMetadata(NutsMessage message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(),
//                session -> message == null ? null : message.toNutsString(session), contentLength, contentType, kind);
//    }
//
//    public DefaultNutsInputSourceMetadata(NutsString message, long contentLength, String contentType, String kind) {
//        this(message == null ? null : message.toString(), s -> message, contentLength, contentType, kind);
//    }
//
//
    public DefaultNutsInputSourceMetadata(NutsMessage message, long contentLength, String contentType, String kind) {
        this.contentLength = contentLength;
        this.name = message ==null?null: message.toString();
        this.message = message;
        this.kind = kind;
        this.contentType = contentType;
    }
    public DefaultNutsInputSourceMetadata(String name, NutsMessage message, long contentLength, String contentType, String kind) {
        this.contentLength = contentLength;
        this.name = name;
        this.message = message;
        this.kind = kind;
        this.contentType = contentType;
    }

    @Override
    public NutsOptional<Long> getContentLength() {
        return NutsOptional.ofNamed(contentLength, "contentLength");
    }

    @Override
    public NutsOptional<String> getContentType() {
        return NutsOptional.ofNamed(contentType, "contentType");
    }

    @Override
    public NutsOptional<String> getName() {
        return NutsOptional.ofNamed(name, "name");
    }

    public NutsOptional<String> getKind() {
        return NutsOptional.ofNamed(kind, "kind");
    }

    public NutsOptional<NutsMessage> getMessage() {
        return NutsOptional.ofNamed(message, "message");
    }

    public NutsInputSourceMetadata setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public NutsInputSourceMetadata setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public NutsInputSourceMetadata setMessage(NutsMessage message) {
        this.message = message;
        return this;
    }

    public DefaultNutsInputSourceMetadata setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public DefaultNutsInputSourceMetadata setName(String name) {
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
