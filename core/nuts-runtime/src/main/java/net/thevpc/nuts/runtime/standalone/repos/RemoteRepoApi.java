/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.NutsEnum;

/**
 *
 * @author thevpc
 */
public enum RemoteRepoApi  implements NutsEnum {

    DEFAULT,
    MAVEN,
    GITHUB,
    DIR_TEXT,
    DIR_LIST,
    UNSUPPORTED;
    private String id;

    RemoteRepoApi() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

}
