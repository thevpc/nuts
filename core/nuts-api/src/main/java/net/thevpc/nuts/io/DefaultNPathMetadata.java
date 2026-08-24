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
    /**
     * Default n path metadata.
     *
     * @param path path
     * @return default n path metadata result
     */
    public DefaultNPathMetadata(NPath path) {
        this.path = path;
    }

    /**
     * Meta data.
     *
     * @return meta data result
     */
    public NContentMetadata metaData() {
        return md;
    }

    /**
     * Checks if is user cache.
     *
     * @return is user cache result
     */
    public boolean isUserCache() {
        return userCache;
    }

    /**
     * User cache.
     *
     * @param userCache user cache
     * @return user cache result
     */
    public DefaultNPathMetadata userCache(boolean userCache) {
        this.userCache = userCache;
        return this;
    }

    /**
     * Checks if is user temporary.
     *
     * @return is user temporary result
     */
    public boolean isUserTemporary() {
        return userTemporary;
    }

    /**
     * User temporary.
     *
     * @param userTemporary user temporary
     * @return user temporary result
     */
    public DefaultNPathMetadata userTemporary(boolean userTemporary) {
        this.userTemporary = userTemporary;
        return this;
    }

    /**
     * Name.
     *
     * @return name result
     */
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

    /**
     * Message.
     *
     * @return message result
     */
    public NOptional<NMsg> message() {
        return NOptional.ofNamed(message, "message")
                .orElseGetOptionalOf(() -> NMsg.ofNtf(NObjectWriter.of(path).format(path)))
                ;
    }

    /**
     * Kind.
     *
     * @return kind result
     */
    public NOptional<String> kind() {
        return NOptional.ofNamed(kind, "kind");
    }

    /**
     * Content length.
     *
     * @return content length result
     */
    public NOptional<Long> contentLength() {
        return NOptional.ofNamed(contentLength, "contentLength")
                .orElseGetOptionalOf(() -> path.contentLength())
                ;
    }

    /**
     * Content type.
     *
     * @return content type result
     */
    public NOptional<String> contentType() {
        return NOptional.ofNamed(contentType, "contentType")
                .orElseGetOptionalOf(() -> path.contentType())
                ;
    }

    /**
     * Charset.
     *
     * @return charset result
     */
    public NOptional<String> charset() {
        return NOptional.ofNamed(charset, "charset")
                .orElseGetOptionalOf(() -> path.charset())
                ;
    }


    /**
     * Message.
     *
     * @param message message
     */
    public void message(NMsg message) {
        this.message = message;
    }

    /**
     * Kind.
     *
     * @param kind kind
     */
    public void kind(String kind) {
        this.kind = kind;
    }

    /**
     * Name.
     *
     * @param name name
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * Content length.
     *
     * @param contentLength content length
     */
    public void contentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Content type.
     *
     * @param contentType content type
     */
    public void contentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Charset.
     *
     * @param charset charset
     */
    public void charset(String charset) {
        this.charset = charset;
    }

    /**
     * Copy from.
     *
     * @param cmd cmd
     */
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

    /**
     * Copy from.
     *
     * @param cmd cmd
     */
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

        /**
         * Path metadata.
         *
         * @param outer outer
         * @return path metadata result
         */
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
