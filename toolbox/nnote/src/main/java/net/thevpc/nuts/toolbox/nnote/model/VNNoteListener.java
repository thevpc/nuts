/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

/**
 *
 * @author vpc
 */
public interface VNNoteListener {

    void onAdded(VNNote child,VNNote parent);

    void onRemoved(VNNote child,VNNote parent);

    void onChanged(VNNote node,String prop, Object oval, Object nval);
}
