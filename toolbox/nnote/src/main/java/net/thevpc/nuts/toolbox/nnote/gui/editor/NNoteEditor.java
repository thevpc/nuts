/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor;

import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source.SourceEditorPanePanel;
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
import net.thevpc.nuts.toolbox.nnote.gui.util.DefaultObjectListModel;
import net.thevpc.common.swing.JTabbedButtons;
import net.thevpc.common.swing.ObjectListModel;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.common.swing.ObjectListModelListener;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.richeditor.RichEditor;

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
            Box hb = Box.createHorizontalBox();
            noteEditorsSelector = new JTabbedButtons();
            editorsSelectorSuffix = Box.createHorizontalStrut(5);
            hb.add(noteEditorsSelector);
            hb.add(editorsSelectorSuffix);
            hb.add(Box.createHorizontalGlue());
            add(hb, BorderLayout.NORTH);
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

    public NNoteEditorTypeComponent createEditor(String name) {
        switch (name) {
//        components.put(NNoteTypes.EDITOR_WYSIWYG, new RichEditor(compactMode,sapp));//"HTML"
            case NNoteTypes.EDITOR_WYSIWYG:
                return new RichEditor(compactMode, sapp);
            case NNoteTypes.EDITOR_SOURCE:
                new SourceEditorPanePanel(true, compactMode, sapp);//"Source Code"
            case NNoteTypes.EDITOR_FILE:
                return new FileNNoteEditorTypeComponent();
            case NNoteTypes.EDITOR_URL:
                return new URLNNoteEditorTypeComponent();
            case NNoteTypes.EDITOR_PASSWORD:
                return new PasswordNNoteEditorTypeComponent();
            case NNoteTypes.EDITOR_STRING:
                return new StringNNoteEditorTypeComponent();
            case NNoteTypes.EDITOR_NNOTE_DOCUMENT:
                return new NNoteDocumentNNoteEditorTypeComponent();
            case NNoteTypes.EDITOR_OBJECT_LIST:
                return new NNoteObjectDocumentComponent(sapp);
            case NNoteTypes.EDITOR_NOTE_LIST:
                return new NoteListNNoteEditorTypeComponent(sapp);
        }
        return null;
    }

    public String getEditorName(String name) {
        NNoteEditorTypeComponent n = components.get(name);
        if (n != null) {
            return name;
        }
        NNoteEditorTypeComponent c = createEditor(name);
        if (c != null) {
            components.put(name, c);
            container.add(c.component(), name);
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
        contentType = sapp.service().normalizeContentType(contentType);
        String[] all = sapp.service().getEditorTypes(contentType);
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
        } else {
            String contentType = sapp.service().normalizeContentType(note.getContentType());
            String editorType = sapp.service().normalizeEditorType(contentType, note.getEditorType());
            String[] all = sapp.service().getEditorTypes(contentType);
            if (!compactMode) {
                if (all.length == 0 || all.length == 1) {
                    noteEditorsSelector.setVisible(true);
                    editorsSelectorSuffix.setVisible(true);
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
