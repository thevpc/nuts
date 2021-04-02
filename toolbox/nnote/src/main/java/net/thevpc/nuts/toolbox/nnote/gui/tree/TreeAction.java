/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.tree;

import javax.swing.AbstractAction;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;

/**
 *
 * @author vpc
 */
public abstract class TreeAction extends AbstractAction {
    
    private String id;
    private final NNoteDocumentTree outer;

    public TreeAction(String id, final NNoteDocumentTree outer) {
        super(id);
        this.outer = outer;
        this.id = id;
        outer.actions.add(this);
        SwingApplicationsHelper.registerAction(this, "Action." + id, "$Action." + id + ".icon", outer.app);
        //            onLocaleChanged();
    }

    protected boolean isNonRootNote(VNNote note) {
        return note != null && note != outer.tree.getModel().getRoot();
    }

    protected void requireSelectedNote(VNNote note) {
        boolean nonRootSelected = isNonRootNote(note);
        //            System.out.println("requireSelectedNote " + note);
        setEnabled(nonRootSelected);
        putValue("visible", nonRootSelected);
    }

    protected void onSelectedNote(VNNote note) {
    }

    protected void onLocaleChanged() {
        //            putValue(NAME, app.i18n().getString("Action." + id + ".name"));
    }
    
}
