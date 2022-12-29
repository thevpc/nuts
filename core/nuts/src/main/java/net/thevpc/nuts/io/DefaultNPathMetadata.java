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
public class DefaultNPathMetadata {
    private NPath path;
    private NMsg message;
    private String kind;
    private Long contentLength;
    private String contentType;
    private String name;
    private boolean userCache;
    private boolean userTemporary;

    private PathOutputMetadata out = new PathOutputMetadata(this);
    private PathInputMetadata in = new PathInputMetadata(this);

    public DefaultNPathMetadata(NPath path) {
        this.path = path;
    }

    public NOutputTargetMetadata asOutput() {
        return out;
    }

    public NInputSourceMetadata asInput() {
        return in;
    }

    public boolean isUserCache() {
        return userCache;
    }

    public DefaultNPathMetadata setUserCache(boolean userCache) {
        this.userCache = userCache;
        return this;
    }

    public boolean isUserTemporary() {
        return userTemporary;
    }

    public DefaultNPathMetadata setUserTemporary(boolean userTemporary) {
        this.userTemporary = userTemporary;
        return this;
    }

    public NOptional<String> getName() {
        if (!NBlankable.isBlank(name)) {
            return NOptional.of(name);
        }
        String n = path.getName();
        if (!NBlankable.isBlank(n)) {
            return NOptional.of(n);
        }
        NMsg m = getMessage().orNull();
        if (m != null) {
            return NOptional.of(m.toString());
        }
        return NOptional.ofNamedEmpty("name");
    }

    public NOptional<NMsg> getMessage() {
        return NOptional.ofNamed(message, "message")
                .orElseOf(() -> NMsg.ofNtf(path.format(path.getSession())))
                ;
    }

    public NOptional<String> getKind() {
        return NOptional.ofNamed(kind, "kind");
    }

    public NOptional<Long> getContentLength() {
        return NOptional.ofNamed(contentLength, "contentLength")
                .orElseOf(() -> path.getContentLength())
                ;
    }

    public NOptional<String> getContentType() {
        return NOptional.ofNamed(contentType, "contentType")
                .orElseOf(() -> path.getContentType())
                ;
    }

    public void setMessage(NMsg message) {
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

    public void setAll(NOutputTargetMetadata omd) {
        this.message = omd.getMessage().orNull();
        this.kind = omd.getKind().orNull();
        this.name = omd.getName().orNull();
    }

    public void setAll(NInputSourceMetadata omd) {
        this.message = omd.getMessage().orNull();
        this.kind = omd.getKind().orNull();
        this.contentLength = omd.getContentLength().orNull();
        this.contentType = omd.getContentType().orNull();
        this.name = omd.getName().orNull();
    }

    public void setAll(DefaultNPathMetadata omd) {
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


    private static class PathInputMetadata implements NInputSourceMetadata {
        private DefaultNPathMetadata outer;

        public PathInputMetadata(DefaultNPathMetadata outer) {
            this.outer = outer;
        }

        @Override
        public NOptional<Long> getContentLength() {
            return outer.getContentLength();
        }

        @Override
        public NOptional<NMsg> getMessage() {
            return outer.getMessage();
        }

        @Override
        public NOptional<String> getContentType() {
            return outer.getContentType();
        }

        @Override
        public NOptional<String> getName() {
            return outer.getName();
        }

        @Override
        public NOptional<String> getKind() {
            return outer.getKind();
        }

        @Override
        public NInputSourceMetadata setKind(String userKind) {
            outer.setKind(userKind);
            return this;
        }

        @Override
        public NInputSourceMetadata setName(String name) {
            outer.setName(name);
            return this;
        }

        @Override
        public NInputSourceMetadata setContentType(String contentType) {
            outer.setContentType(contentType);
            return this;
        }

        @Override
        public NInputSourceMetadata setContentLength(Long contentLength) {
            outer.setContentLength(contentLength);
            return this;
        }

        @Override
        public String toString() {
            return outer.toString();
        }
    }

    private class PathOutputMetadata implements NOutputTargetMetadata {
        private DefaultNPathMetadata outer;

        public PathOutputMetadata(DefaultNPathMetadata outer) {
            this.outer = outer;
        }

        @Override
        public NOptional<String> getName() {
            return outer.getName();
        }

        @Override
        public NOutputTargetMetadata setName(String name) {
            outer.setName(name);
            return this;
        }

        @Override
        public NOptional<String> getKind() {
            return outer.getName();
        }

        @Override
        public NOutputTargetMetadata setKind(String userKind) {
            outer.setKind(userKind);
            return this;
        }

        @Override
        public NOutputTargetMetadata setMessage(NMsg message) {
            outer.setMessage(message);
            return this;
        }

        @Override
        public NOptional<NMsg> getMessage() {
            return outer.getMessage();
        }

        @Override
        public String toString() {
            return outer.toString();
        }
    }
}
