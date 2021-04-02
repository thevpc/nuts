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
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.notelist.NoteListNNoteEditorTypeComponent;
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
    private VNNote currentNote;
    private JPanel container;
    private String editorTypeI18nPrefix = "EditorType";
    private JBreadCrumb breadCrumb;
    private JTabbedButtons noteEditorsSelector /*{
        @Override
        protected void updateButton(JButton b, int pos, int size) {
            b.setOpaque(false);
            b.setBackground(null);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setBorder(new RoundedBorder(5));
        }
    }*/;
    private Component editorsSelectorSuffix;
    private List<NNoteEditorListener> listeners = new ArrayList<>();
    private EditorOnLocalChangePropertyListenerImpl editorOnLocalChangePropertyListenerImpl = new EditorOnLocalChangePropertyListenerImpl();
    private NNoteGuiApp sapp;
    private Application app;
    private boolean compactMode;
    private boolean editable;

    public NNoteEditor(NNoteGuiApp sapp, boolean compactMode) {
        super(new CardLayout());
        this.compactMode = compactMode;
        this.sapp = sapp;
        this.app = sapp.app();
        this.setBorder(null);
        container = this;
//        add(container, BorderLayout.CENTER);

        if (!compactMode) {
            breadCrumb = new JBreadCrumb();
            Box hb = Box.createHorizontalBox();
            noteEditorsSelector = new JTabbedButtons();
            editorsSelectorSuffix = Box.createHorizontalStrut(5);
            hb.add(noteEditorsSelector);
            hb.add(editorsSelectorSuffix);
            hb.add(Box.createHorizontalGlue());
            if (breadCrumb != null) {
                hb.add(breadCrumb);
            }
            add(hb, BorderLayout.NORTH);
            breadCrumb.addListener(new ObjectListModelListener() {
                @Override
                public void onSelected(Object component, int index) {
                    if (component != currentNote) {
                        for (NNoteEditorListener listener : listeners) {
                            listener.onNavigateTo((VNNote) component);
                        }
                    }
                }
            });
            noteEditorsSelector.addListener(new ObjectListModelListener() {
                @Override
                public void onSelected(Object component, int index) {
                    String editorType = (String) component;
                    if (currentNote != null) {
                        currentNote.setEditorType(editorType);
                        setNote(currentNote);
                    }
                }
            });

        }

        components.put("empty", new EmpyNNodtEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_UNSUPPORTED, new UnsupportedNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_WYSIWYG, new SourceEditorPanePanel(false, compactMode,app));//"HTML"
        components.put(NNoteTypes.EDITOR_SOURCE, new SourceEditorPanePanel(true, compactMode,app));//"Source Code"
        components.put(NNoteTypes.EDITOR_FILE, new FileNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_URL, new URLNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_PASSWORD, new PasswordNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_STRING, new StringNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_N_NOTE_DOCUMENT, new NNoteDocumentNNoteEditorTypeComponent());
        components.put(NNoteTypes.EDITOR_OBJECT_LIST, new NNoteObjectDocumentComponent(sapp));
        components.put(NNoteTypes.EDITOR_NOTE_LIST, new NoteListNNoteEditorTypeComponent(sapp));

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

    public VNNote getNote() {
        return currentNote;
    }

    public void setNote(VNNote note) {
        this.currentNote = note;
        if (note == null) {
            showEditor("empty");
            if (breadCrumb != null) {
                breadCrumb.setModel(null);
            }
        } else {
            if (breadCrumb != null) {
                breadCrumb.setModel(createBreadCrumModel(note));
            }
            String contentType = NNoteTypes.normalizeContentType(note.getContentType());
            String editorType = NNoteTypes.normalizeEditorType(contentType, note.getEditorType());
            String[] all = NNoteTypes.getEditorTypes(contentType);
            if (!compactMode) {
                if (all.length == 0 || all.length == 1) {
                    noteEditorsSelector.setVisible(false);
                    editorsSelectorSuffix.setVisible(false);
                } else {
                    noteEditorsSelector.setVisible(true);
                    editorsSelectorSuffix.setVisible(true);
                    noteEditorsSelector.setModel(createEditorTypeModel(contentType));
                }
            }
            getEditor(editorType).setNote(note, sapp);//TODO FIX ME
            showEditor(editorType);
        }
    }

    private ObjectListModel createBreadCrumModel(VNNote note) {
        List<VNNote> bm = new ArrayList<>();
        VNNote n = note;
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
                noteEditorsSelector.setModel(createEditorTypeModel(
                        currentNote == null ? null : currentNote.getContentType()
                ));
            }
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        for (NNoteEditorTypeComponent component : components.values()) {
            component.setEditable(editable);
        }
    }
    

}
