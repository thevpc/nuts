/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.objectlist;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.CheckboxesComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.ComboboxComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.FormComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.PasswordComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.SimpleTextComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.TextAreaComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.URLComponent;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNoteField;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObject;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectFieldType;

/**
 *
 * @author vpc
 */
class NNoteFieldDescriptorPanel {

    private NNoteObjectDocument document;
    private NNoteObject object;
    private NNoteField field;

    private NNoteFieldDescriptor descr;
    private JLabel label;
    private JComponent component;

    private NNoteObjectTracker objectTracker;
    private NNoteGuiApp sapp;
    private SwingApplicationsHelper.Tracker tracker;
    private List<AbstractButton> buttons = new ArrayList<>();

    private JMenu changeTypeMenu;
    private boolean editable=true;

    //        ButtonGroup bg;
    public NNoteFieldDescriptorPanel(NNoteGuiApp sapp, NNoteFieldDescriptor descr, NNoteObjectTracker objectTracker) {
        this.objectTracker = objectTracker;
        this.descr = descr.copy();
        this.sapp = sapp;
        tracker = new SwingApplicationsHelper.Tracker(sapp.app());
        if (descr.getType() == null) {
            descr.setType(NNoteObjectFieldType.TEXT);
        }
        label = new JLabel(descr.getName());
        JPopupMenu jPopupMenu = new JPopupMenu();
        label.setComponentPopupMenu(jPopupMenu);
        jPopupMenu.add(createAction("changeFieldName", this::onDescriptorRename));
        changeTypeMenu = new JMenu();
        tracker.registerStandardButton(changeTypeMenu, "Action.changeFieldType");
        jPopupMenu.add(changeTypeMenu);
        changeTypeMenu.add(createAction("changeFieldTypeText", () -> onDescriptorChangeType(NNoteObjectFieldType.TEXT)));
        changeTypeMenu.add(createAction("changeFieldTypeCombobox", () -> onDescriptorChangeType(NNoteObjectFieldType.COMBOBOX)));
        changeTypeMenu.add(createAction("changeFieldTypeCheckbox", () -> onDescriptorChangeType(NNoteObjectFieldType.CHECKBOX)));
        changeTypeMenu.add(createAction("changeFieldTypeLongText", () -> onDescriptorChangeType(NNoteObjectFieldType.TEXTAREA)));
        changeTypeMenu.add(createAction("changeFieldTypeURL", () -> onDescriptorChangeType(NNoteObjectFieldType.URL)));
        changeTypeMenu.add(createAction("changeFieldTypePassword", () -> onDescriptorChangeType(NNoteObjectFieldType.PASSWORD)));
        jPopupMenu.add(createAction("changeFieldValues", () -> onDescriptorEditValues()));

        jPopupMenu.addSeparator();
        jPopupMenu.add(createAction("addField", () -> onAddField()));

        jPopupMenu.addSeparator();
        jPopupMenu.add(createAction("removeField", () -> onRemoveField()));

        jPopupMenu.addSeparator();
        jPopupMenu.add(createAction("moveUpField", () -> onMoveUpField()));
        jPopupMenu.add(createAction("moveDownField", () -> onMoveDownField()));
        jPopupMenu.addSeparator();
        jPopupMenu.add(createAction("hideField", () -> onHideField()));
        jPopupMenu.add(createAction("unhideFields", () -> onUnhideFields()));
        
        FormComponent comp = createFormComponent(descr.getType());
        comp.install(sapp.app());
        comp.setSelectValues(resolveValues(descr));
        comp.setFormChangeListener(() -> callOnValueChanged());
        component = (JComponent) comp;
    } //=new ButtonGroup()

    private FormComponent createFormComponent(NNoteObjectFieldType t) {
        switch (t) {
            case TEXT: {
                return new SimpleTextComponent();
            }
            case PASSWORD: {
                return new PasswordComponent();
            }
            case URL: {
                return new URLComponent();
            }

            case COMBOBOX: {
                return new ComboboxComponent();
            }
            case CHECKBOX: {
                return new CheckboxesComponent();
            }
            case TEXTAREA: {
                return new TextAreaComponent();
            }
        }
        return new SimpleTextComponent();
    }

    private void callOnStructureChanged() {
        if (objectTracker != null) {
            objectTracker.onStructureChanged();
        }
    }

    private void callOnValueChanged() {
        if (this.field != null) {
            this.field.setValue(getStringValue());
        }
        if (objectTracker != null) {
            objectTracker.onValueChanged();
        }
    }

    public String getStringValue() {
        return formComponent().getContentString();
    }

    public void setStringValue(String s) {
        formComponent().setContentString(s);
    }

    public void setValue(NNoteField field, NNoteObject object, NNoteObjectDocument document) {
        this.field = field;
        this.object = object;
        this.document = document;
        String s = field.getValue();
        if (s == null) {
            s = "";
        }
        setStringValue(s);
        formComponent().setEditable(isEditable());
    }

    public NNoteField getValue() {
        NNoteField df = new NNoteField();
        df.setName(this.descr.getName());
        df.setValue(getStringValue());
        return df;
    }

    private static List<String> resolveValues(NNoteFieldDescriptor descr) {
        Set<String> a = new LinkedHashSet<>();
        if (descr.getValues() != null) {
            for (String value : descr.getValues()) {
                if (value == null) {
                    value = "";
                }
                a.add(value);
            }
        }
        String dv = descr.getDefaultValue();
        if (dv == null) {
            dv = "";
        }
        if (!a.contains(dv)) {
            a.add(dv);
        }
        if (a.isEmpty()) {
            a.add("");
        }
        return new ArrayList<>(a);
    }

    public void uninstall() {
        this.sapp = null;
        this.objectTracker = null;
        formComponent().uninstall();
        tracker.unregisterAll();
    }

    public NNoteFieldDescriptor getDescr() {
        return descr;
    }

    public JLabel getLabel() {
        return label;
    }

    public JComponent getComponent() {
        return component;
    }

    public boolean supportsUpdateDescriptor(NNoteFieldDescriptor field) {
        if (field.getType() != descr.getType()) {
            return false;
        }
        return true;
    }

    public void updateDescriptor(NNoteFieldDescriptor field) {
        if (!supportsUpdateDescriptor(field)) {
            throw new IllegalArgumentException("Cannot morph this field panel");
        }
        this.descr = field;
        label.setText(this.descr.getName());
        formComponent().setSelectValues(resolveValues(descr));
    }

    public void onDescriptorMoveDownField() {
        if (document != null) {
            document.moveFieldDown(descr.getName());
            callOnStructureChanged();
        }
    }

    public void onDescriptorMoveUpField() {
        if (document != null) {
            document.moveFieldUp(descr.getName());
            callOnStructureChanged();
        }
    }

    public void onHideField() {
        if (document != null) {
            if(field!=null){
                field.setHidden(true);
            }
            callOnStructureChanged();
        }
    }

    public void onUnhideFields() {
        if (document != null) {
            if(object!=null){
                for (NNoteField f : object.getFields()) {
                    f.setHidden(false);
                }
            }
            callOnStructureChanged();
        }
    }

    public void onDescriptorEditValues() {
        if (document != null) {
            String title = sapp.app().i18n().getString("Message.changeFieldValues");
            String label = sapp.app().i18n().getString("Message.changeFieldValues.label");
            JTextArea a = new JTextArea();
            JScrollPane p = new JScrollPane(a);
            p.setPreferredSize(new java.awt.Dimension(400, 300));
            JPanel panel = new javax.swing.JPanel(new BorderLayout());
            panel.add(new JLabel(label), BorderLayout.NORTH);
            panel.add(p, BorderLayout.CENTER);
            if (descr.getValues() != null) {
                TreeSet<String> all = new TreeSet<>();
                for (String s : descr.getValues()) {
                    if (s == null) {
                        s = "";
                    }
                    s = s.trim();
                    all.add(s);
                }
                a.setText(String.join("\n", all));
            }
            if (JOptionPane.showConfirmDialog(
                    resolveAncestor(), panel, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null
            ) == JOptionPane.OK_OPTION) {
                document.updateFieldValues(descr.getName(), a.getText().split("\n"));
                callOnStructureChanged();
            }
        }
    }

    public Container resolveAncestor() {
        return SwingUtilities3.getAncestorOfClass(new Class[]{Window.class}, component);
    }

    public void onDescriptorRename() {
        if (document != null) {
            String title = sapp.app().i18n().getString("Message.renameField");
            String label = sapp.app().i18n().getString("Message.renameField.label");
            String n = (String) JOptionPane.showInputDialog(resolveAncestor(), title, label, JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    descr.getName()
            );
            if (n != null) {
                n = n.trim();
                if (n.length() > 0) {
                    if (document != null) {
                        document.renameField(descr.getName(), n);
                    }
                    callOnStructureChanged();
                }
            }
        }
    }

    public void onDescriptorChangeType(NNoteObjectFieldType type) {
        if (document != null) {
            document.changeType(descr.getName(), type);
            callOnStructureChanged();
        }
    }

    public void onRemoveField() {
        if (document != null) {
            document.removeField(descr.getName());
            callOnStructureChanged();
        }
    }

    public void onAddField() {
        if (document != null) {
             String title = sapp.app().i18n().getString("Message.addField");
            String label = sapp.app().i18n().getString("Message.addField.label");
            String n = (String) JOptionPane.showInputDialog(resolveAncestor(), title, label, JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    descr.getName()
            );
            if (n != null) {
                n = n.trim();
                if (n.length() > 0) {
                    document.addField(new NNoteFieldDescriptor().setName(n).setType(NNoteObjectFieldType.TEXT));
                    callOnStructureChanged();
                }
            }
        }
    }

    public void onMoveDownField() {
        if (document != null) {
            document.moveFieldDown(descr.getName());
            callOnStructureChanged();
        }
    }

    public void onMoveUpField() {
        if (document != null) {
            document.moveFieldUp(descr.getName());
            callOnStructureChanged();
        }
    }

    public void onDuplicateField() {
        if (document != null) {
            String n = JOptionPane.showInputDialog(null, descr.getName());
            if (n != null) {
                document.addField(new NNoteFieldDescriptor().setName(n).setType(NNoteObjectFieldType.TEXT));
                callOnStructureChanged();
            }
        }
    }

    private Action createAction(String id, Runnable r) {
        AbstractAction a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
        tracker.registerStandardAction(a, id);
        return a;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        formComponent().setEditable(editable);
        for (AbstractButton button : buttons) {
            button.setEnabled(editable);
        }
        for (Action action : tracker.getActions()) {
            action.setEnabled(editable);
        }
    }

    private FormComponent formComponent() {
        return (FormComponent) component;
    }
    
}
