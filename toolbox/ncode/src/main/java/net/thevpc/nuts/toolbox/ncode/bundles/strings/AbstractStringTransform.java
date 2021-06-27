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
public abstract class AbstractStringTransform implements StringTransform {

    public StringTransform apply(final StringTransform base) {
        if (base == null) {
            return this;
        }
        return new AbstractStringTransform() {

            @Override
            public String transform(String s) {
                s = base.transform(s);
                s = AbstractStringTransform.this.transform(s);
                return s;
            }

            @Override
            public String toString() {
                return AbstractStringTransform.this.toString() + "->" + base.toString();
            }

        };
    }
}
