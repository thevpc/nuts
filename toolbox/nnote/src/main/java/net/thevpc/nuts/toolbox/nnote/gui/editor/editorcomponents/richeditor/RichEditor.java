/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.richeditor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.more.shef.ShefHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source.SourceEditorPaneExtension;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source.SourceEditorPanePanelTextExtension;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.gui.util.GuiHelper;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;

/**
 *
 * @author vpc
 */
public class RichEditor extends JPanel implements NNoteEditorTypeComponent {

    private NNoteGuiApp sapp;
    private JEditorPaneBuilder editorBuilder;
    private VNNote currentNote;
    private boolean compactMode;
    DocumentListener documentListener = new AnyDocumentListener() {
        public void anyChange(DocumentEvent e) {
            if (currentNote != null) {
//                System.out.println("update note:" + editorBuilder.editor().getText());
                currentNote.setContent(editorBuilder.editor().getText());
            }
        }
    };

    private SourceEditorPaneExtension textExtension = new SourceEditorPanePanelTextExtension();
    private SourceEditorPaneExtension htmlExtension = new SourceEditorPanePanelHtmlExtension();

    public RichEditor(boolean compactMode, NNoteGuiApp sapp) {
        super(new BorderLayout());
        this.sapp = sapp;
        this.compactMode = compactMode;
        editorBuilder = new JEditorPaneBuilder().setEditor(ShefHelper.installMin(new JEditorPane("text/html", "")));
        if (!compactMode) {
//            editorBuilder.footer()
//                    //                .add(new JLabel("example..."))
//                    //                .add(new JSyntaxPosLabel(e, completion))
//                    .addGlue()
//                    .addCaret()
//                    .end();
        } else {
            setBorder(BorderFactory.createEmptyBorder());
        }
        this.editorBuilder.editor().getDocument().addDocumentListener(documentListener);
        this.editorBuilder.editor().addPropertyChangeListener("document", e -> {
            Document o = (Document) e.getOldValue();
            Document n = (Document) e.getNewValue();
            if (o != null) {
                o.removeDocumentListener(documentListener);
            }
            if (n != null) {
                n.addDocumentListener(documentListener);
            }
        });
        JPopupMenu popup = editorBuilder.editor().getComponentPopupMenu();
        if (popup == null) {
            popup = new JPopupMenu();
            editorBuilder.editor().setComponentPopupMenu(popup);
        }
        textExtension.prepareEditor(editorBuilder, compactMode, sapp);
        htmlExtension.prepareEditor(editorBuilder, compactMode, sapp);

        if (!compactMode) {
            this.editorBuilder.header().addGlue();
        }
        this.editorBuilder.editor().addPropertyChangeListener("document", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                GuiHelper.installUndoRedoManager(editorBuilder.editor());
            }
        });
//        GuiHelper.installUndoRedoManager(editorBuilder.editor());

        if (compactMode) {
            add(editorBuilder.component());
        } else {
            add(editorBuilder.component());
        }
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void uninstall() {
        //
    }

    @Override
    public void setNote(VNNote note, NNoteGuiApp sapp) {
        editorBuilder.editor().setText(note.getContent());
    }

    @Override
    public void setEditable(boolean b) {
        editorBuilder.editor().setEditable(b);
    }

    @Override
    public boolean isEditable() {
        return editorBuilder.editor().isEditable();
    }

}
