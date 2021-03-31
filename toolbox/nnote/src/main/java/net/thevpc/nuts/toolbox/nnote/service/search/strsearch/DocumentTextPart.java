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
public interface DocumentTextPart<T> {

    String getString();

    StringSearchResult<T> resolvePosition(int index,String v);
}
