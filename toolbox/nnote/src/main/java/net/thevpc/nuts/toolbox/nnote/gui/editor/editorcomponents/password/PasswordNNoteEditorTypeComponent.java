/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.password;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.gui.util.PasswordComponent;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class PasswordNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private PasswordComponent text = new PasswordComponent();
    private VNNote currentNote;

    public PasswordNNoteEditorTypeComponent() {
        setLayout(new BorderLayout());
        add(text, BorderLayout.NORTH);
        add(new JLabel(), BorderLayout.CENTER);
        text.getPasswordField().getDocument().addDocumentListener(new AnyDocumentListener() {
            @Override
            public void anyChange(DocumentEvent e) {
                if (currentNote != null) {
                    currentNote.setContent(text.getContentString());
                }
            }
        });
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
        this.currentNote = note;
        if (note == null) {
            text.setContentString("");
        } else {
            text.setContentString(note.getContent());
        }
        text.setEditable(!note.isReadOnly());
    }

    @Override
    public void setEditable(boolean b) {
        if (currentNote != null && currentNote.isReadOnly()) {
            b = false;
        }
        text.setEditable(b);
    }

    @Override
    public boolean isEditable() {
        return text.isEditable();
    }

}
