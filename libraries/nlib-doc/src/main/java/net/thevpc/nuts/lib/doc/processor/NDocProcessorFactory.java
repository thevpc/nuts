/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.processor;

/**
 *
 * @author thevpc
 */
public interface NDocProcessorFactory {
    NDocProcessor getProcessor(String mimeType);
}
