/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.string;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class StringNNoteEditorTypeComponentClear extends JTextField implements NNoteEditorTypeComponent {

    private VNNote currentNote;

    public StringNNoteEditorTypeComponentClear() {
        this.getDocument().addDocumentListener(new AnyDocumentListener() {
            @Override
            public void anyChange(DocumentEvent e) {
                if (currentNote != null) {
                    currentNote.setContent(StringNNoteEditorTypeComponentClear.this.getText());
                }
            }
        });
//        setBorder(BorderFactory.createLineBorder(Color.red));
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void uninstall() {
    }

    @Override
    public void setNote(VNNote note,NNoteGuiApp sapp) {
        this.currentNote = note;
        if (note == null) {
            this.setText("");
        } else {
            this.setText(note.getContent());
        }
    }

}
