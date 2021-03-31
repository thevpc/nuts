/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor;

import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg.SourceEditorPanePanel;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.empty.EmpyNNodtEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.unsupported.UnsupportedNNoteEditorTypeComponent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JPanel;
import net.thevpc.common.props.PropertyEvent;
import net.thevpc.common.props.PropertyListener;
import net.thevpc.echo.Application;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.file.FileNNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.nnotedocument.NNoteDocumentNNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.nodelist.NodeListNNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.objectlist.NNoteObjectDocumentComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.password.PasswordNNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.string.StringNNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.url.URLNNoteEditorTypeComponent;
import net.thevpc.common.swing.JBreadCrumb;
import net.thevpc.nuts.toolbox.nnote.gui.util.DefaultObjectListModel;
import net.thevpc.common.swing.JTabbedButtons;
import net.thevpc.common.swing.ObjectListModel;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.common.swing.ObjectListModelListener;

/**
 *
 * @author vpc
 */
public class NNoteEditor extends JPanel {

    private Map<String, NNoteEditorTypeComponent> components = new LinkedHashMap<String, NNoteEditorTypeComponent>();
    private NNoteEditorTypeComponent currentEditor;
    private VNNote currentNode;
    private JPanel container;
    private String editorTypeI18nPrefix = "EditorType";
    private JBreadCrumb breadCrumb;
    private JTabbedButtons nodeEditorsSelector /*{
        @Override
        protected void updateButton(JButton b, int pos, int size) {
            b.setOpaque(false);
            b.setBackground(null);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setBorder(new RoundedBorder(5));
        }
    }*/;
    private Component nodenodeEditorsSelectorSuffix;
    private List<NNoteEditorListener> listeners = new ArrayList<>();
    private EditorOnLocalChangePropertyListenerImpl editorOnLocalChangePropertyListenerImpl = new EditorOnLocalChangePropertyListenerImpl();
    private NNoteGuiApp sapp;
    private Application app;
    private boolean compactMode;

    public NNoteEditor(NNoteGuiApp sapp, boolean compactMode) {
        super(new BorderLayout());
        this.compactMode = compactMode;
        this.sapp = sapp;
        this.app = sapp.app();
        container = new JPanel(new CardLayout());

        if (!compactMode) {
            breadCrumb = new JBreadCrumb();
            Box hb = Box.createHorizontalBox();
            nodeEditorsSelector = new JTabbedButtons();
            nodenodeEditorsSelectorSuffix = Box.createHorizontalStrut(5);
            hb.add(nodeEditorsSelector);
            hb.add(nodenodeEditorsSelectorSuffix);
            hb.add(Box.createHorizontalGlue());
            if (breadCrumb != null) {
                hb.add(breadCrumb);
            }
            add(hb, BorderLayout.NORTH);
            breadCrumb.addListener(new ObjectListModelListener() {
                @Override
                public void onSelected(Object component, int index) {
                    if (component != currentNode) {
                        for (NNoteEditorListener listener : listeners) {
                            listener.onNavigateTo((VNNote) component);
                        }
                    }
                }
            });
            nodeEditorsSelector.addListener(new ObjectListModelListener() {
                @Override
                public void onSelected(Object component, int index) {
                    String editorType = (String) component;
                    if (currentNode != null) {
                        currentNode.setEditorType(editorType);
                        setNode(currentNode);
                    }
                }
            });

        }
        add(container, BorderLayout.CENTER);

        components.put("empty", new EmpyNNodtEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_UNSUPPORTED, new UnsupportedNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_WYSIWYG, SourceEditorPanePanel.create("HTML", false, app));
        components.put(NNoteTypes.EDITOR_SOURCE, SourceEditorPanePanel.create("Sources", true, app));
        components.put(NNoteTypes.EDITOR_FILE, new FileNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_URL, new URLNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_PASSWORD, new PasswordNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_STRING, new StringNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_N_NOTE_DOCUMENT, new NNoteDocumentNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_OBJECT_LIST, new NNoteObjectDocumentComponent(sapp));
        components.put(NNoteTypes.EDITOR_NODE_LIST, new NodeListNNoteEditorTypeComponent(sapp));

        for (Map.Entry<String, NNoteEditorTypeComponent> entry : components.entrySet()) {
            container.add(entry.getValue().component(), entry.getKey());
        }

        showEditor("empty");

        app.i18n().locale().listeners().add(editorOnLocalChangePropertyListenerImpl);
    }

    public void uninstall() {
        app.i18n().locale().listeners().remove(editorOnLocalChangePropertyListenerImpl);
        for (Map.Entry<String, NNoteEditorTypeComponent> entry : components.entrySet()) {
            entry.getValue().uninstall();
        }
    }

    public void addListener(NNoteEditorListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public String getEditorName(String name) {
        NNoteEditorTypeComponent n = components.get(name);
        if (n != null) {
            return name;
        }
        return NNoteTypes.EDITOR_UNSUPPORTED;
    }

    public NNoteEditorTypeComponent getEditor(String name) {
        return components.get(getEditorName(name));
    }

    public void showEditor(String name) {
        String okName = getEditorName(name);
        this.currentEditor = getEditor(okName);
        ((CardLayout) container.getLayout()).show(container, okName);
    }

    public ObjectListModel createEditorTypeModel(String contentType) {
        contentType = NNoteTypes.normalizeContentType(contentType);
        String[] all = NNoteTypes.getEditorTypes(contentType);
        return new DefaultObjectListModel(
                Arrays.asList(all),
                x -> app.i18n().getString(editorTypeI18nPrefix + "." + x)
        );
    }

    public VNNote getNode() {
        return currentNode;
    }

    public void setNode(VNNote node) {
        this.currentNode = node;
        if (node == null) {
            showEditor("empty");
            if (breadCrumb != null) {
                breadCrumb.setModel(null);
            }
        } else {
            if (breadCrumb != null) {
                breadCrumb.setModel(createBreadCrumModel(node));
            }
            String contentType = NNoteTypes.normalizeContentType(node.getContentType());
            String editorType = NNoteTypes.normalizeEditorType(contentType, node.getEditorType());
            String[] all = NNoteTypes.getEditorTypes(contentType);
            if (!compactMode) {
                if (all.length == 0 || all.length == 1) {
                    nodeEditorsSelector.setVisible(false);
                    nodenodeEditorsSelectorSuffix.setVisible(false);
                } else {
                    nodeEditorsSelector.setVisible(true);
                    nodenodeEditorsSelectorSuffix.setVisible(true);
                    nodeEditorsSelector.setModel(createEditorTypeModel(contentType));
                }
            }
            getEditor(editorType).setNode(node, sapp);//TODO FIX ME
            showEditor(editorType);
        }
    }

    private ObjectListModel createBreadCrumModel(VNNote node) {
        List<VNNote> bm = new ArrayList<>();
        VNNote n = node;
        while (n != null) {
            bm.add(0, n);
            n = n.getParent();
        }
        bm.remove(0);//remove root!
        DefaultObjectListModel model = new DefaultObjectListModel(bm);
        return model;
    }

    private class EditorOnLocalChangePropertyListenerImpl implements PropertyListener {

        public EditorOnLocalChangePropertyListenerImpl() {
        }

        @Override
        public void propertyUpdated(PropertyEvent e) {
            if (!compactMode) {
                nodeEditorsSelector.setModel(createEditorTypeModel(
                        currentNode == null ? null : currentNode.getContentType()
                ));
            }
        }
    }

}
