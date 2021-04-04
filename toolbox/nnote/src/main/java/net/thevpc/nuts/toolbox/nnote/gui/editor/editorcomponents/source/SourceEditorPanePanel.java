/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source;

import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.richeditor.SourceEditorPanePanelHtmlExtension;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.jeep.editor.JSyntaxStyleManager;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.GuiHelper;

/**
 *
 * @author vpc
 */
public class SourceEditorPanePanel extends JPanel implements NNoteEditorTypeComponent {

    private JEditorPaneBuilder editorBuilder;
    private VNNote currentNote;
    private boolean source;
    private boolean compactMode;
    private boolean editable = true;
    private NNoteGuiApp sapp;
    private SourceEditorPaneExtension textExtension = new SourceEditorPanePanelTextExtension();
    private SourceEditorPaneExtension htmlExtension = new SourceEditorPanePanelHtmlExtension();
    DocumentListener documentListener = new AnyDocumentListener() {
        public void anyChange(DocumentEvent e) {
            if (currentNote != null) {
//                System.out.println("update note:" + editorBuilder.editor().getText());
                currentNote.setContent(editorBuilder.editor().getText());
            }
        }
    };

    public SourceEditorPanePanel(boolean source, boolean compactMode, NNoteGuiApp sapp) {
        super(new BorderLayout());
        this.compactMode = compactMode;
        boolean lineNumbers = source;
        this.editorBuilder = new JEditorPaneBuilder();
        if (lineNumbers) {
            editorBuilder.addLineNumbers();
        }
        if (!compactMode) {
            editorBuilder.footer()
                    //                .add(new JLabel("example..."))
                    //                .add(new JSyntaxPosLabel(e, completion))
                    .addGlue()
                    .addCaret()
                    .end();
        } else {
            setBorder(BorderFactory.createEmptyBorder());
        }
//        editorBuilder.footer()
//                //                .add(new JLabel("example..."))
//                //                .add(new JSyntaxPosLabel(e, completion))
//                .addGlue()
//                .addCaret()
//                .end() //                .setEditorKit(HadraLanguage.MIME_TYPE, new HLJSyntaxKit(jContext))
//                //                    .component()
//                .header();
        //.header().add(new JLabel(title))

//        this.setWheelScrollingEnabled(true);
        this.sapp = sapp;
        this.source = source;
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
        GuiHelper.installUndoRedoManager(editorBuilder.editor());
        if (source) {
            this.editorBuilder.editor().setFont(JSyntaxStyleManager.getDefaultFont());
        }
//        editorBuilder.editor().addPropertyChangeListener("editorKit", new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                EditorKit ek = (EditorKit) evt.getNewValue();
//                String ct = ek == null ? "" : ek.getContentType();
//                if (ct == null) {
//                    ct = "";
//                }
//                for (EditorKitHeader toolbar : headers) {
//                    toolbar.component().setVisible(toolbar.acceptContentType(ct));
//                }
//            }
//        }
//        );
        add(editorBuilder.component());
    }

//    public boolean isSupportedType(String contentType) {
//        return supported != null && supported.contains(contentType);
//    }
    @Override
    public void uninstall() {
        textExtension.uninstall(editorBuilder, sapp);
        htmlExtension.uninstall(editorBuilder, sapp);
    }

    @Override
    public void setNote(VNNote note, NNoteGuiApp sapp) {
        this.currentNote = note;
        String c = note.getContent();
        String type = note.getContentType();
        if (type == null) {
            type = "";
        }
        if (source && "text/html".equals(type)) {
            type = "text/plain";//should change this
        }
        editorBuilder.editor().setContentType(type.isEmpty() ? "text/plain" : type);
        editorBuilder.editor().setText(c == null ? "" : c);
        setEditable(!note.isReadOnly());
    }

    private Action prepareAction(AbstractAction a) {
        //align-justify.png
        String s = (String) a.getValue(AbstractAction.NAME);
        SwingApplicationsHelper.registerAction(a, null, s, sapp.app());
        return a;
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void setEditable(boolean b) {
        if (currentNote != null && currentNote.isReadOnly()) {
            b = false;
        }
        editorBuilder.editor().setEditable(b);
    }

    @Override
    public boolean isEditable() {
        return editorBuilder.editor().isEditable();
    }
}
