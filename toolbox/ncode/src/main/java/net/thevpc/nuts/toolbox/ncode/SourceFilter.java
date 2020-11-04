/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

/**
 *
 * @author vpc
 */
public interface SourceFilter {

    boolean accept(Source source);

    boolean lookInto(Source source);
}
