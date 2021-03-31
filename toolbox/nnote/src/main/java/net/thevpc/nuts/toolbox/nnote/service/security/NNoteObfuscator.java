/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.security;

import net.thevpc.nuts.toolbox.nnote.model.CypherInfo;
import net.thevpc.nuts.toolbox.nnote.model.NNote;

/**
 *
 * @author vpc
 */
public interface NNoteObfuscator {

    CypherInfo encrypt(NNote a, PasswordHandler handler);

    NNote decrypt(CypherInfo cypherInfo, NNote original,PasswordHandler handler);
}
