/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
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

    public NContentMetadata metaData() {
        return md;
    }

    public boolean isUserCache() {
        return userCache;
    }

    public DefaultNPathMetadata userCache(boolean userCache) {
        this.userCache = userCache;
        return this;
    }

    public boolean isUserTemporary() {
        return userTemporary;
    }

    public DefaultNPathMetadata userTemporary(boolean userTemporary) {
        this.userTemporary = userTemporary;
        return this;
    }

    public NOptional<String> name() {
        if (!NBlankable.isBlank(name)) {
            return NOptional.of(name);
        }
        String n = path.name();
        if (!NBlankable.isBlank(n)) {
            return NOptional.of(n);
        }
        NMsg m = message().orNull();
        if (m != null) {
            return NOptional.of(m.toString());
        }
        return NOptional.ofNamedEmpty("name");
    }

    public NOptional<NMsg> message() {
        return NOptional.ofNamed(message, "message")
                .orElseGetOptionalOf(() -> NMsg.ofNtf(NObjectWriter.of(path).format(path)))
                ;
    }

    public NOptional<String> kind() {
        return NOptional.ofNamed(kind, "kind");
    }

    public NOptional<Long> contentLength() {
        return NOptional.ofNamed(contentLength, "contentLength")
                .orElseGetOptionalOf(() -> path.contentLength())
                ;
    }

    public NOptional<String> contentType() {
        return NOptional.ofNamed(contentType, "contentType")
                .orElseGetOptionalOf(() -> path.contentType())
                ;
    }

    public NOptional<String> charset() {
        return NOptional.ofNamed(charset, "charset")
                .orElseGetOptionalOf(() -> path.charset())
                ;
    }


    public void message(NMsg message) {
        this.message = message;
    }

    public void kind(String kind) {
        this.kind = kind;
    }

    public void name(String name) {
        this.name = name;
    }

    public void contentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public void contentType(String contentType) {
        this.contentType = contentType;
    }

    public void charset(String charset) {
        this.charset = charset;
    }

    public void copyFrom(NContentMetadata cmd) {
        if(cmd==null){
            return;
        }
        this.message = cmd.message().orNull();
        this.kind = cmd.kind().orNull();
        this.contentLength = cmd.contentLength().orNull();
        this.contentType = cmd.contentType().orNull();
        this.charset = cmd.charset().orNull();
        this.name = cmd.name().orNull();
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
        public NOptional<String> name() {
            return outer.name();
        }

        @Override
        public NContentMetadata name(String name) {
            outer.name(name);
            return this;
        }

        @Override
        public NOptional<String> kind() {
            return outer.name();
        }

        @Override
        public NContentMetadata kind(String userKind) {
            outer.kind(userKind);
            return this;
        }

        @Override
        public NContentMetadata message(NMsg message) {
            outer.message(message);
            return this;
        }

        @Override
        public NOptional<NMsg> message() {
            return outer.message();
        }

        @Override
        public String toString() {
            return outer.toString();
        }

        @Override
        public NOptional<Long> contentLength() {
            return outer.contentLength();
        }

        @Override
        public NOptional<String> contentType() {
            return outer.contentType();
        }

        @Override
        public NContentMetadata contentType(String contentType) {
            outer.contentType(contentType);
            return this;
        }

        @Override
        public NContentMetadata contentLength(Long contentLength) {
            outer.contentLength(contentLength);
            return this;
        }

        @Override
        public NOptional<String> charset() {
            return outer.charset();
        }

        @Override
        public NContentMetadata charset(String charset) {
            outer.charset(charset);
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
