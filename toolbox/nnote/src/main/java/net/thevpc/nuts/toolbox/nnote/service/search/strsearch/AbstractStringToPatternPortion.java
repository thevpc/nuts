/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search.strsearch;

/**
 *
 * @author vpc
 */
public abstract class AbstractStringToPatternPortion<T> implements DocumentTextPart<T> {
    final String stringValue;
    public AbstractStringToPatternPortion(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String getString() {
        return stringValue;
    }

}
