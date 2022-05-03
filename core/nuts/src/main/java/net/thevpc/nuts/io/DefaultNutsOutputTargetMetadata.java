/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;


import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

/**
 * @author thevpc
 */
public class DefaultNutsOutputTargetMetadata implements NutsOutputTargetMetadata {

    private NutsMessage message;
    private String name;
    private String kind;

    public DefaultNutsOutputTargetMetadata(NutsOutputTargetMetadata other) {
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

    public DefaultNutsOutputTargetMetadata(NutsMessage message, String name) {
        this.message = message;
        this.name = name;
    }

    public DefaultNutsOutputTargetMetadata() {
        this.name = null;
        this.message = null;
        this.kind = null;
    }

    @Override
    public NutsOptional<String> getName() {
        if (!NutsBlankable.isBlank(name)) {
            return NutsOptional.of(name);
        }
        NutsMessage m = getMessage().orNull();
        if (m != null) {
            return NutsOptional.of(m.toString());
        }
        return NutsOptional.ofNamedEmpty("name");
    }

    public NutsOptional<String> getKind() {
        return NutsOptional.ofNamed(kind, "kind");
    }

    @Override
    public NutsOptional<NutsMessage> getMessage() {
        return NutsOptional.of(message);
    }

    public NutsOutputTargetMetadata setKind(String userKind) {
        this.kind = userKind;
        return this;
    }

    public NutsOutputTargetMetadata setMessage(NutsMessage message) {
        this.message = message;
        return this;
    }

    @Override
    public NutsOutputTargetMetadata setName(String name) {
        this.name = name;
        return this;
    }
}
