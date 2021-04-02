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
    private VNNote currentNote;
    private boolean editable = true;

    public NNoteDocumentNNoteEditorTypeComponent() {
        super(new BorderLayout());
        add(new JLabel("nnote-document"), BorderLayout.NORTH);
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
    public void setNote(VNNote note, NNoteGuiApp sapp) {
        try {
            this.currentNote = note;
            if (note.getContent() == null || note.getContent().length() == 0) {
                error.setText("missing file");
            } else {
                NNote o = sapp.service().loadDocument(new File(note.getContent()), sapp::askForPassword);
                note.removeAllChildren();//TODO FIX ME
                for (NNote c : o.getChildren()) {
                    note.addChild(VNNote.of(c));
                }
                error.setText(o.error == null ? "" : o.error.getEx().toString());
            }
            setEditable(!note.isReadOnly());
        } catch (Exception ex) {
            error.setText(ex.toString());
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean b) {
        if (currentNote != null && currentNote.isReadOnly()) {
            b = false;
        }
        this.editable = b;
    }

}
