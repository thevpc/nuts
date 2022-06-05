/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.*;

/**
 * @author thevpc
 */
public class DefaultNutsPathMetadata {
    private NutsPath path;
    private NutsMessage message;
    private String kind;
    private Long contentLength;
    private String contentType;
    private String name;
    private boolean userCache;
    private boolean userTemporary;

    private PathOutputMetadata out = new PathOutputMetadata(this);
    private PathInputMetadata in = new PathInputMetadata(this);

    public DefaultNutsPathMetadata(NutsPath path) {
        this.path = path;
    }

    public NutsOutputTargetMetadata asOutput() {
        return out;
    }

    public NutsInputSourceMetadata asInput() {
        return in;
    }

    public boolean isUserCache() {
        return userCache;
    }

    public DefaultNutsPathMetadata setUserCache(boolean userCache) {
        this.userCache = userCache;
        return this;
    }

    public boolean isUserTemporary() {
        return userTemporary;
    }

    public DefaultNutsPathMetadata setUserTemporary(boolean userTemporary) {
        this.userTemporary = userTemporary;
        return this;
    }

    public NutsOptional<String> getName() {
        if (!NutsBlankable.isBlank(name)) {
            return NutsOptional.of(name);
        }
        String n = path.getName();
        if (!NutsBlankable.isBlank(n)) {
            return NutsOptional.of(n);
        }
        NutsMessage m = getMessage().orNull();
        if (m != null) {
            return NutsOptional.of(m.toString());
        }
        return NutsOptional.ofNamedEmpty("name");
    }

    public NutsOptional<NutsMessage> getMessage() {
        return NutsOptional.ofNamed(message, "message")
                .orElseOf(() -> NutsMessage.ofNtf(path.format(path.getSession())))
                ;
    }

    public NutsOptional<String> getKind() {
        return NutsOptional.ofNamed(kind, "kind");
    }

    public NutsOptional<Long> getContentLength() {
        return NutsOptional.ofNamed(contentLength, "contentLength")
                .orElseOf(() -> path.getContentLength())
                ;
    }

    public NutsOptional<String> getContentType() {
        return NutsOptional.ofNamed(contentType, "contentType")
                .orElseOf(() -> path.getContentType())
                ;
    }

    public void setMessage(NutsMessage message) {
        this.message = message;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setAll(NutsOutputTargetMetadata omd) {
        this.message = omd.getMessage().orNull();
        this.kind = omd.getKind().orNull();
        this.name = omd.getName().orNull();
    }

    public void setAll(NutsInputSourceMetadata omd) {
        this.message = omd.getMessage().orNull();
        this.kind = omd.getKind().orNull();
        this.contentLength = omd.getContentLength().orNull();
        this.contentType = omd.getContentType().orNull();
        this.name = omd.getName().orNull();
    }

    public void setAll(DefaultNutsPathMetadata omd) {
        this.message = omd.message;
        this.kind = omd.kind;
        this.contentLength = omd.contentLength;
        this.contentType = omd.contentType;
        this.name = omd.name;
        this.userCache = omd.userCache;
        this.userTemporary = omd.userTemporary;
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }


    private static class PathInputMetadata implements NutsInputSourceMetadata {
        private DefaultNutsPathMetadata outer;

        public PathInputMetadata(DefaultNutsPathMetadata outer) {
            this.outer = outer;
        }

        @Override
        public NutsOptional<Long> getContentLength() {
            return outer.getContentLength();
        }

        @Override
        public NutsOptional<NutsMessage> getMessage() {
            return outer.getMessage();
        }

        @Override
        public NutsOptional<String> getContentType() {
            return outer.getContentType();
        }

        @Override
        public NutsOptional<String> getName() {
            return outer.getName();
        }

        @Override
        public NutsOptional<String> getKind() {
            return outer.getKind();
        }

        @Override
        public NutsInputSourceMetadata setKind(String userKind) {
            outer.setKind(userKind);
            return this;
        }

        @Override
        public NutsInputSourceMetadata setName(String name) {
            outer.setName(name);
            return this;
        }

        @Override
        public NutsInputSourceMetadata setContentType(String contentType) {
            outer.setContentType(contentType);
            return this;
        }

        @Override
        public NutsInputSourceMetadata setContentLength(Long contentLength) {
            outer.setContentLength(contentLength);
            return this;
        }

        @Override
        public String toString() {
            return outer.toString();
        }
    }

    private class PathOutputMetadata implements NutsOutputTargetMetadata {
        private DefaultNutsPathMetadata outer;

        public PathOutputMetadata(DefaultNutsPathMetadata outer) {
            this.outer = outer;
        }

        @Override
        public NutsOptional<String> getName() {
            return outer.getName();
        }

        @Override
        public NutsOutputTargetMetadata setName(String name) {
            outer.setName(name);
            return this;
        }

        @Override
        public NutsOptional<String> getKind() {
            return outer.getName();
        }

        @Override
        public NutsOutputTargetMetadata setKind(String userKind) {
            outer.setKind(userKind);
            return this;
        }

        @Override
        public NutsOutputTargetMetadata setMessage(NutsMessage message) {
            outer.setMessage(message);
            return this;
        }

        @Override
        public NutsOptional<NutsMessage> getMessage() {
            return outer.getMessage();
        }

        @Override
        public String toString() {
            return outer.toString();
        }
    }
}
