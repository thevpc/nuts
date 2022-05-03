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
public interface NutsOutputTargetMetadata {
    NutsOptional<String> getName();
    NutsOutputTargetMetadata setName(String name);


    NutsOptional<String> getKind();

    NutsOutputTargetMetadata setKind(String userKind);

    NutsOutputTargetMetadata setMessage(NutsMessage message);
    NutsOptional<NutsMessage> getMessage();

}
