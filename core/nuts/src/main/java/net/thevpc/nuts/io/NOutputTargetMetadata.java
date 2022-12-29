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
public interface NOutputTargetMetadata {
    NOptional<String> getName();
    NOutputTargetMetadata setName(String name);


    NOptional<String> getKind();

    NOutputTargetMetadata setKind(String userKind);

    NOutputTargetMetadata setMessage(NMsg message);
    NOptional<NMsg> getMessage();

}
