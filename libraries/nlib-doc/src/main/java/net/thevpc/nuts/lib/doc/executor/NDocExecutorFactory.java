/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.executor;

/**
 *
 * @author thevpc
 */
public interface NDocExecutorFactory {
    NDocExecutor getExecutor(String mimeType);
}
