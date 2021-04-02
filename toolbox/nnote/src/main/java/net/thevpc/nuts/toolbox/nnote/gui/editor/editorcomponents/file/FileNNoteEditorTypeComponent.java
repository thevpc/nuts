/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.file;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.gui.util.FileComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.URLViewer;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class FileNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private JLabel error;
    private FileComponent comp;
    private URLViewer fileViewer;
    private VNNote currentNote;
    private boolean editable=true;

    public FileNNoteEditorTypeComponent() {
        super(new BorderLayout());
        error = new JLabel();
        comp = new FileComponent();
        comp.getTextField().getDocument().addDocumentListener(new AnyDocumentListener() {
            @Override
            public void anyChange(DocumentEvent e) {
                if (currentNote != null) {
                    currentNote.setContent(comp.getTextField().getText());
                }
            }
        });
        fileViewer = new URLViewer();
        add(comp, BorderLayout.NORTH);
        add(fileViewer, BorderLayout.CENTER);
        add(error, BorderLayout.SOUTH);
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
        String c = note.getContent();
        if (c == null || c.isEmpty()) {
            fileViewer.resetContent();
            error.setText("");
        } else {
            try {
                fileViewer.load(new File(c).toURI().toURL());
                error.setText("");
            } catch (MalformedURLException ex) {
                error.setText(ex.toString());
            }
        }
    }

    public void setEditable(boolean b) {
        if (currentNote != null && currentNote.isReadOnly()) {
            b = false;
        }
        this.editable=b;
        comp.setEditable(b);
    }

    public boolean isEditable() {
        return editable && comp.isEditable();
    }

}
