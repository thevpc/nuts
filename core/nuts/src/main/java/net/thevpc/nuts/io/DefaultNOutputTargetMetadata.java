/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

/**
 * @author thevpc
 */
public class DefaultNOutputTargetMetadata implements NOutputTargetMetadata {

    private NMsg message;
    private String name;
    private String kind;

    public DefaultNOutputTargetMetadata(NOutputTargetMetadata other) {
        if (other != null) {
            this.name = other.getName().orNull();
            this.message = other.getMessage().orNull();
            this.kind = other.getKind().orNull();
        } else {
            this.name = null;
            this.message = null;
            this.kind = null;
        }
    }

    public DefaultNOutputTargetMetadata(NMsg message, String name) {
        this.message = message;
        this.name = name;
    }

    public DefaultNOutputTargetMetadata() {
        this.name = null;
        this.message = null;
        this.kind = null;
    }

    @Override
    public NOptional<String> getName() {
        if (!NBlankable.isBlank(name)) {
            return NOptional.of(name);
        }
        NMsg m = getMessage().orNull();
        if (m != null) {
            return NOptional.of(m.toString());
        }
        return NOptional.ofNamedEmpty("name");
    }

    public NOptional<String> getKind() {
        return NOptional.ofNamed(kind, "kind");
    }

    @Override
    public NOptional<NMsg> getMessage() {
        return NOptional.of(message);
    }

    public NOutputTargetMetadata setKind(String userKind) {
        this.kind = userKind;
        return this;
    }

    public NOutputTargetMetadata setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    @Override
    public NOutputTargetMetadata setName(String name) {
        this.name = name;
        return this;
    }
}
