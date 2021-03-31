/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search.strsearch;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author vpc
 */
public class TextStringToPatternHandler<T> implements DocumentTextNavigator<T> {

    private String key;
    private String text;
    private T object;
    private Object part;

    public TextStringToPatternHandler(String key, T object, Object part, String text) {
        this.key = key;
        this.object = object;
        this.part = part;
        this.text = text == null ? "" : String.valueOf(text);
    }

    @Override
    public Iterator<DocumentTextPart<T>> iterator() {
        return Arrays.asList(text)
                .stream().map(x -> (DocumentTextPart<T>) new StringToPatternPortionImpl(key, text, object, part, x)).iterator();
    }

}
