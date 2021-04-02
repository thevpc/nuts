/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.notelist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import net.thevpc.common.swing.ComponentBasedBorder;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditor;
import net.thevpc.common.swing.list.JComponentList;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.common.swing.list.JComponentListItem;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;
import net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs.EditNoteDialog;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.NNoteListModel;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class NoteListNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private JComponentList<VNNote> componentList;
    private VNNote currentNote;
    private NNoteListModel noteListModel;
    private NNoteGuiApp sapp;
    private boolean editable = true;

    public NoteListNNoteEditorTypeComponent(NNoteGuiApp sapp) {
        super(new BorderLayout());
        this.sapp = sapp;
        componentList = new JComponentList<VNNote>(new JComponentListItem<VNNote>() {
            @Override
            public JComponent createComponent(int pos, int size) {
                return new Item(sapp);
            }

            @Override
            public void setComponentValue(JComponent comp, VNNote value, int pos, int size) {
                Item b = (Item) comp;
                b.setValue(value, pos, size);
            }

            @Override
            public VNNote getComponentValue(JComponent comp, int pos) {
                return ((Item) comp).getValue(pos);
            }

            @Override
            public void uninstallComponent(JComponent comp) {
                ((Item) comp).onUninstall();
            }

            @Override
            public void setEditable(JComponent component, boolean editable, int pos, int size) {
                ((Item) component).setEditable(editable);
            }

        });
        JScrollPane scrollPane = new JScrollPane(componentList);
        scrollPane.setWheelScrollingEnabled(true);
        add(scrollPane);
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
        this.noteListModel = sapp.service().parseNoteListModel(note.getContent());
        if (this.noteListModel == null) {
            this.noteListModel = new NNoteListModel();
        }
        componentList.setAllObjects(note.getChildren());
        setEditable(!note.isReadOnly());
    }

    public boolean setSelectedName(String name, boolean sel) {
        if (this.currentNote != null && this.noteListModel != null) {
            Set<String> n = this.noteListModel.getSelectedNames();
            if (sel) {
                n.add(name);
            } else {
                n.remove(name);
            }
            this.currentNote.setContent(sapp.service().stringifyNoteListInfo(this.noteListModel));
        }
        return false;
    }

    public boolean isSelectedIndex(String name) {
        if (this.currentNote != null && this.noteListModel != null) {
            return this.noteListModel.getSelectedNames().contains(name);
        }
        return false;
    }

    public void onAddObjectAt(int pos) {
        if (currentNote != null) {
            if (currentNote.getChildren().size() > 0) {
                VNNote p = currentNote.getChildren().get(pos);
                currentNote.addChild(pos, VNNote.of(new NNote().setContentType(p.getContentType())));
            } else {
                currentNote.addChild(pos, VNNote.of(new NNote().setContentType(NNoteTypes.PLAIN)));
            }
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onDuplicateObjectAt(int pos) {
        if (currentNote != null) {
            VNNote p = currentNote.getChildren().get(pos);
            currentNote.addChild(pos, p.duplicate());
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onRemoveObjectAt(int pos) {
        if (currentNote != null) {
            currentNote.removeChild(pos);
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onMoveUpAt(int pos) {
        if (currentNote != null) {
            currentNote.moveUp(pos);
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onMoveDownAt(int pos) {
        if (currentNote != null) {
            currentNote.moveDown(pos);
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onMoveFirstAt(int pos) {
        if (currentNote != null) {
            currentNote.moveFirst(pos);
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    public void onEditAt(int pos) {
        VNNote cc = currentNote.getChildren().get(pos);
        NNote n = new EditNoteDialog(sapp, cc).showDialog();
        if (n != null) {
            sapp.tree().fireNoteChanged(cc);
            this.invalidate();
            this.repaint();
        }
    }

    public void onMoveLastAt(int pos) {
        if (currentNote != null) {
            currentNote.moveLast(pos);
            componentList.setAllObjects(currentNote.getChildren());
            sapp.tree().fireNoteChanged(currentNote);
        }
    }

    @Override
    public void setEditable(boolean b) {
        if (currentNote != null && currentNote.isReadOnly()) {
            b = false;
        }
        this.editable = b;
        componentList.setEditable(b);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    private class Item extends NNoteEditor {

        private JCheckBox check;
        private SwingApplicationsHelper.Tracker stracker;
        private int pos;
        private Font _font;
        private Color _foreground;
        private Color _background;
        private ComponentBasedBorder border;

        public Item(NNoteGuiApp sapp) {
            super(sapp, true);
            stracker = new SwingApplicationsHelper.Tracker(sapp.app());
            ComponentBasedBorder.ComponentBasedBorderBuilder b = ComponentBasedBorder.of(this).withCheckbox();
            border = b.install();
            check = (JCheckBox) border.getBorderComponent();
            check.addActionListener(e -> {
                setSelectedName(getNote().getName(), check.isSelected());
            });
            JPopupMenu bar = new JPopupMenu();
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onAddObjectAt(pos), "addToObjectList"))));
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onDuplicateObjectAt(pos), "duplicateInObjectList"))));
            bar.addSeparator();
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onRemoveObjectAt(pos), "removeInObjectList"))));
            bar.addSeparator();
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onMoveUpAt(pos), "moveUpInObjectList"))));
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onMoveDownAt(pos), "moveDownInObjectList"))));
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onMoveFirstAt(pos), "moveFirstInObjectList"))));
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onMoveLastAt(pos), "moveLastInObjectList"))));
            bar.addSeparator();
            bar.add(prepareButton(new JMenuItem(stracker.registerStandardAction(() -> onEditAt(pos), "NoteProperties"))));
            this.setComponentPopupMenu(bar);
        }

        public JMenuItem prepareButton(JMenuItem b) {
//            b.setHideActionText(true);
            return b;
        }

        public void setValue(VNNote value, int pos, int size) {
            this.pos = pos;
            String s = value.getName();
            if (s == null || s.length() == 0) {
                s = "no-name";
            }
            setNote(value);
            check.setText((pos + 1) + " - " + value.getName());
            check.setSelected(isSelectedIndex(value.getName()));
            if (_font == null) {
                _font = check.getFont();
            }
            if (_foreground == null) {
                _foreground = check.getForeground();
            }
            if (_background == null) {
                _background = check.getBackground();
            }
            check.setFont(OtherUtils.deriveFont(_font, value.isTitleBold(), value.isTitleItalic(), value.isTitleUnderlined(), value.isTitleStriked()));
            Color b = OtherUtils.parseColor(value.getTitleBackground());
            check.setBackground(b != null ? b : _background);
            b = OtherUtils.parseColor(value.getTitleForeground());
            check.setForeground(b != null ? b : _foreground);
            String iconName = sapp.service().getNoteIcon(value.toNote(), value.getChildren().size() > 0, false);
            Icon icon = sapp.app().iconSet().icon(iconName).get();
            border.setIcon(icon);
            repaint();
        }

        public void setEditable(boolean editable) {
            super.setEditable(editable);
            check.setEnabled(editable);
            for (Action action : stracker.getActions()) {
                action.setEnabled(editable);
            }
        }

        public VNNote getValue(int pos) {
            return this.getNote();
        }

        public void onUninstall() {
            this.uninstall();
        }

    }

}
