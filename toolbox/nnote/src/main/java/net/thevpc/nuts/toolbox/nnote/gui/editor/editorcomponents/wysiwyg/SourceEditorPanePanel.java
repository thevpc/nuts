/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import net.thevpc.echo.Application;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.jeep.editor.JSyntaxStyleManager;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class SourceEditorPanePanel extends JScrollPane implements NNoteEditorTypeComponent{

    private JEditorPaneBuilder editorBuilder;
    private VNNote node;
    private boolean source;
    private Application app;
    private SourceEditorPaneExtension textExtension = new SourceEditorPanePanelTextExtension();
    private SourceEditorPaneExtension htmlExtension = new SourceEditorPanePanelHtmlExtension();
    DocumentListener documentListener = new AnyDocumentListener() {
        public void anyChange(DocumentEvent e) {
            if (node != null) {
//                System.out.println("update node:" + editorBuilder.editor().getText());
                node.setContent(editorBuilder.editor().getText());
            }
        }
    };

    public static SourceEditorPanePanel create(String title, boolean source, Application app) {
        boolean lineNumbers = source;
        JEditorPaneBuilder editorBuilder = new JEditorPaneBuilder();
        if (lineNumbers) {
            editorBuilder.addLineNumbers();
        }
        editorBuilder.footer()
                //                .add(new JLabel("example..."))
                //                .add(new JSyntaxPosLabel(e, completion))
                .addGlue()
                .addCaret()
                .end() //                .setEditorKit(HadraLanguage.MIME_TYPE, new HLJSyntaxKit(jContext))
                //                    .component()
                .header();
        //.header().add(new JLabel(title))
        return new SourceEditorPanePanel(editorBuilder, source, app);
    }

    public SourceEditorPanePanel(JEditorPaneBuilder builder, boolean source, Application app) {
        super(builder.component());
        this.setWheelScrollingEnabled(true); 
        this.app = app;
        this.source = source;
        this.editorBuilder = builder;
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
        textExtension.prepareEditor(editorBuilder, app);
        htmlExtension.prepareEditor(editorBuilder, app);
        this.editorBuilder.header().addGlue();
        if(source){
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
    }

//    public boolean isSupportedType(String contentType) {
//        return supported != null && supported.contains(contentType);
//    }
    @Override
    public void uninstall() {
        textExtension.uninstall(editorBuilder, app);
        htmlExtension.uninstall(editorBuilder, app);
    }
    
    public void setNode(VNNote node,NNoteGuiApp sapp) {
        this.node = node;
        String c = node.getContent();
        String type = node.getContentType();
        if (type == null) {
            type = "";
        }
        if (source && "text/html".equals(type)) {
            type = "text/plain";//should change this
        }
        editorBuilder.editor().setContentType(type.isEmpty() ? "text/plain" : type);
        editorBuilder.editor().setText(c == null ? "" : c);
    }

    private Action prepareAction(AbstractAction a) {
        //align-justify.png
        String s = (String) a.getValue(AbstractAction.NAME);
        SwingApplicationsHelper.registerAction(a, null, s, app);
        return a;
    }
    
    public JComponent component(){
        return this;
    }

}
