/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import net.thevpc.common.swing.ObjectListModel;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author vpc
 */
public class DefaultObjectListModel implements ObjectListModel {

    List any;
    Function<Object, String> stringifier;

    public DefaultObjectListModel(List any) {
        this(any, null);
    }

    public DefaultObjectListModel(List any, Function<Object, String> stringifier) {
        this.any = any;
        this.stringifier = stringifier;
    }

    @Override
    public int size() {
        return any.size();
    }

    @Override
    public String getName(Object obj) {
        if (stringifier != null) {
            String s = stringifier.apply(obj);
            if (s == null) {
                return "<null>";
            }
            return s;
        }
        if (obj == null) {
            return "<null>";
        }
        return String.valueOf(obj);
    }

    @Override
    public Object getObjectAt(int i) {
        return any.get(i);
    }

}
