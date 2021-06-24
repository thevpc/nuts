/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.template;

import java.util.Map;
import java.util.function.Function;

/**
 * @author taha.bensalah@gmail.com
 */
public class StringToObjectMap implements Function<String,Object> {

    private final Map<String, Object> values;

    public StringToObjectMap(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public Object apply(String str) {
        return values == null ? null : values.get(str);
    }

}
