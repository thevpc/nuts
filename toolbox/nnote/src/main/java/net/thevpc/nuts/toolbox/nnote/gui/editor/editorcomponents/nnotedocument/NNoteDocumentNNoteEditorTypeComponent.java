/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.nnotedocument;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class NNoteDocumentNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private JLabel file;
    private JLabel error;

    public NNoteDocumentNNoteEditorTypeComponent() {
        super(new BorderLayout());
        add(new JLabel("n-node-document"), BorderLayout.NORTH);
        add(file = new JLabel(""), BorderLayout.CENTER);
        add(error = new JLabel(""), BorderLayout.SOUTH);
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void uninstall() {
    }

    @Override
    public void setNode(VNNote node, NNoteGuiApp sapp) {
        try {
            if (node.getContent() == null || node.getContent().length() == 0) {
                error.setText("missing file");
            } else {
                NNote o = sapp.service().loadDocument(new File(node.getContent()),sapp::askForPassword);
                node.removeAllChildren();//TODO FIX ME
                for (NNote c : o.getChildren()) {
                    node.addChild(VNNote.of(c));
                }
                error.setText(o.error == null ? "" : o.error.getEx().toString());
            }
        } catch (Exception ex) {
            error.setText(ex.toString());
        }
    }

}
