/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 */
public class DefaultNPathMetadata {
    private NPath path;
    private NMsg message;
    private String kind;
    private Long contentLength;
    private String contentType;
    private String charset;
    private String name;
    private boolean userCache;
    private boolean userTemporary;

    private PathMetadata md = new PathMetadata(this);
    public DefaultNPathMetadata(NPath path) {
        this.path = path;
    }

    public NContentMetadata getMetaData() {
        return md;
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
                .orElseOf(() -> NMsg.ofNtf(NFormats.of(path).get().format()))
                ;
    }

    public NOptional<String> getKind() {
        return NOptional.ofNamed(kind, "kind");
    }

    public NOptional<Long> getContentLength() {
        return NOptional.ofNamed(contentLength, "contentLength")
                .orElseOf(() -> path.contentLength())
                ;
    }

    public NOptional<String> getContentType() {
        return NOptional.ofNamed(contentType, "contentType")
                .orElseOf(() -> path.getContentType())
                ;
    }

    public NOptional<String> getCharset() {
        return NOptional.ofNamed(charset, "charset")
                .orElseOf(() -> path.getCharset())
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

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void copyFrom(NContentMetadata cmd) {
        if(cmd==null){
            return;
        }
        this.message = cmd.getMessage().orNull();
        this.kind = cmd.getKind().orNull();
        this.contentLength = cmd.getContentLength().orNull();
        this.contentType = cmd.getContentType().orNull();
        this.charset = cmd.getCharset().orNull();
        this.name = cmd.getName().orNull();
    }

    public void copyFrom(DefaultNPathMetadata cmd) {
        if(cmd==null){
            return;
        }
        this.message = cmd.message;
        this.kind = cmd.kind;
        this.contentLength = cmd.contentLength;
        this.contentType = cmd.contentType;
        this.charset = cmd.charset;
        this.name = cmd.name;
        this.userCache = cmd.userCache;
        this.userTemporary = cmd.userTemporary;
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }


    private class PathMetadata implements NContentMetadata {
        private DefaultNPathMetadata outer;

        public PathMetadata(DefaultNPathMetadata outer) {
            this.outer = outer;
        }

        @Override
        public NOptional<String> getName() {
            return outer.getName();
        }

        @Override
        public NContentMetadata setName(String name) {
            outer.setName(name);
            return this;
        }

        @Override
        public NOptional<String> getKind() {
            return outer.getName();
        }

        @Override
        public NContentMetadata setKind(String userKind) {
            outer.setKind(userKind);
            return this;
        }

        @Override
        public NContentMetadata setMessage(NMsg message) {
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

        @Override
        public NOptional<Long> getContentLength() {
            return outer.getContentLength();
        }

        @Override
        public NOptional<String> getContentType() {
            return outer.getContentType();
        }

        @Override
        public NContentMetadata setContentType(String contentType) {
            outer.setContentType(contentType);
            return this;
        }

        @Override
        public NContentMetadata setContentLength(Long contentLength) {
            outer.setContentLength(contentLength);
            return this;
        }

        @Override
        public NOptional<String> getCharset() {
            return outer.getCharset();
        }

        @Override
        public NContentMetadata setCharset(String charset) {
            outer.setCharset(charset);
            return this;
        }

        @Override
        public boolean isBlank() {
            if (outer.contentLength != null && outer.contentLength >= 0) {
                return false;
            }
            if (outer.message != null) {
                return false;
            }
            if (outer.contentType != null) {
                return false;
            }
            if (outer.name != null) {
                return false;
            }
            if (outer.charset != null) {
                return false;
            }
            if (kind != null) {
                return false;
            }
            return true;
        }
    }
}
