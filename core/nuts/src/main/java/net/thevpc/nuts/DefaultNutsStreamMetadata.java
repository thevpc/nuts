/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;


import java.util.function.Function;

/**
 * @author thevpc
 */
public class DefaultNutsStreamMetadata implements NutsStreamMetadata {

    private final long contentLength;
    private final Function<NutsSession, NutsString> formattedName;
    private final String contentType;
    private final String name;
    private String userKind;

    public DefaultNutsStreamMetadata(NutsStreamMetadata other) {
        if (other != null) {
            this.contentLength = other.getContentLength();
            this.name = other.getName();
            this.formattedName = other::getFormattedPath;
            this.userKind = other.getUserKind();
            this.contentType = other.getContentType();
        } else {
            this.contentLength = -1;
            this.name = null;
            this.formattedName = null;
            this.userKind = null;
            this.contentType = null;
        }
    }

    public DefaultNutsStreamMetadata() {
        this.contentLength = -1;
        this.name = null;
        this.formattedName = null;
        this.userKind = null;
        this.contentType = null;
    }

    public DefaultNutsStreamMetadata(NutsMessage message, long contentLength, String contentType, String userKind) {
        this(message == null ? null : message.toString(),
                session -> message == null ? null : message.toNutsString(session), contentLength, contentType, userKind);
    }

    public DefaultNutsStreamMetadata(NutsString message, long contentLength, String contentType, String userKind) {
        this(message == null ? null : message.toString(), s -> message, contentLength, contentType, userKind);
    }

    public DefaultNutsStreamMetadata(NutsPath path) {
        this(path.getName(),
                path::format,
                path.getContentLength(),
                path.getContentType(),
                path.getUserKind());
    }

    public DefaultNutsStreamMetadata(String name, Function<NutsSession, NutsString> formattedName, long contentLength, String contentType, String userKind) {
        this.contentLength = contentLength;
        this.name = name;
        this.formattedName = formattedName;
        this.userKind = userKind;
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public NutsString getFormattedPath(NutsSession session) {
        return formattedName == null ? null : formattedName.apply(session);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUserKind() {
        return userKind;
    }

    public NutsStreamMetadata setUserKind(String userKind) {
        this.userKind = userKind;
        return this;
    }
}
