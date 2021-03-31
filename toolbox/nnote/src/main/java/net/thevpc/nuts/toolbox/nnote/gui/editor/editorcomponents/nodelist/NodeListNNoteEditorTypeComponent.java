/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.nodelist;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditor;
import net.thevpc.common.swing.list.JComponentList;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.common.swing.list.JComponentListItem;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class NodeListNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private JComponentList<VNNote> componentList;

    public NodeListNNoteEditorTypeComponent(NNoteGuiApp sapp) {
        super(new BorderLayout());
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
    public void setNode(VNNote node, NNoteGuiApp sapp) {
        componentList.setAllObjects(node.getChildren());
    }

    private static class Item extends JPanel {

        private NNoteEditor e;

        public Item(NNoteGuiApp sapp) {
            super(new BorderLayout());
            e = new NNoteEditor(sapp,true);
            add(e);
        }

        public void setValue(VNNote value, int pos, int size) {
            String s = value.getName();
            if (s == null || s.length() == 0) {
                s = "no-name";
            }
            setBorder(BorderFactory.createTitledBorder(s));
            e.setNode(value);
        }

        public VNNote getValue(int pos) {
            return ((NNoteEditor) e).getNode();
        }

        public void onUninstall() {
            e.uninstall();
        }
    }

}
