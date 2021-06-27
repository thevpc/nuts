/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.bundles.strings;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface StringComparator {

    public boolean matches(String other);

    public StringTransform getTransform();

    public StringComparator resetTransform();

    public StringComparator apply(StringTransform tranform);
}
