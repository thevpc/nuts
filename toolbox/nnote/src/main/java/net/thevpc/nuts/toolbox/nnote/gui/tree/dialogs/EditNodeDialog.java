/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs;

import net.thevpc.common.swing.NamedValue;
import net.thevpc.nuts.toolbox.nnote.gui.util.ComboboxHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.thevpc.common.swing.ColorChooserButton;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.common.swing.JDialog2;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.OkCancelAppDialog;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class EditNodeDialog extends OkCancelAppDialog {

    private JTextField nameEditor;
    private JComboBox iconEditor;
    private ColorChooserButton foregroundEditor;
    private ColorChooserButton backgroundEditor;
    private JCheckBox readOnlyEditor;
    private JCheckBox boldEditor;
    private JCheckBox italicEditor;
    private JCheckBox underlinedEditor;

    private boolean ok = false;
    private NNote node;

    public EditNodeDialog(NNoteGuiApp sapp, NNote node){
        super(sapp,"Message.editNode");

        this.node = node;
        nameEditor = new JTextField("");
        iconEditor = createIconListComponent(sapp);
        foregroundEditor = new ColorChooserButton();
        backgroundEditor = new ColorChooserButton();
        Box modifiersEditor = Box.createHorizontalBox();

        boldEditor = new JCheckBox(sapp.app().i18n().getString("Message.titleBold"));
        modifiersEditor.add(boldEditor);
        italicEditor = new JCheckBox(sapp.app().i18n().getString("Message.titleItalic"));
        modifiersEditor.add(italicEditor);
        underlinedEditor = new JCheckBox(sapp.app().i18n().getString("Message.titleUnderlined"));
        readOnlyEditor = new JCheckBox(sapp.app().i18n().getString("Message.readOnly"));
        modifiersEditor.add(underlinedEditor);

        GridBagLayoutSupport gbs = GridBagLayoutSupport.load(EditNodeDialog.class.getResource(
                "/net/thevpc/nuts/toolbox/nnote/forms/EditNodeDialog.gbl-form"
        ));
        gbs.bind("nameLabel", new JLabel(sapp.app().i18n().getString("Message.name")));
        gbs.bind("nameEditor", nameEditor);
        gbs.bind("iconLabel", new JLabel(sapp.app().i18n().getString("Message.icon")));
        gbs.bind("iconEditor", iconEditor);
        gbs.bind("forgroundLabel", new JLabel(sapp.app().i18n().getString("Message.titleForegroundColor")));
        gbs.bind("forgroundEditor", foregroundEditor);
        gbs.bind("backgroundLabel", new JLabel(sapp.app().i18n().getString("Message.titleBackgroundColor")));
        gbs.bind("backgroundEditor", backgroundEditor);
        gbs.bind("readOnlyEditor", readOnlyEditor);
        gbs.bind("modifiersEditor", modifiersEditor);

        nameEditor.setText(node.getName());
        for (int i = 0; i < iconEditor.getModel().getSize(); i++) {
            NamedValue v=(NamedValue)iconEditor.getModel().getElementAt(i);
            if(v!=null && Objects.equals(OtherUtils.trim(v.getId()), OtherUtils.trim(node.getIcon()))){
                iconEditor.setSelectedItem(v);
                break;
            }
        }
        iconEditor.setSelectedItem(node.getIcon());
        foregroundEditor.setColorValue(OtherUtils.parseColor(node.getTitleForeground()));
        backgroundEditor.setColorValue(OtherUtils.parseColor(node.getTitleBackground()));
        foregroundEditor.setPreferredSize(new Dimension(20,20));
        backgroundEditor.setPreferredSize(new Dimension(20,20));
        boldEditor.setSelected(node.isTitleBold());
        italicEditor.setSelected(node.isTitleItalic());
        underlinedEditor.setSelected(node.isTitleUnderlined());

        build(gbs.apply(new JPanel()), this::ok,this::cancel);
    }


    

    private JComboBox createIconListComponent(NNoteGuiApp sapp1) {
        List<NamedValue> list = new ArrayList<>();
        list.add(new NamedValue(false, "", sapp1.app().i18n().getString("Icon.none"), null));
        for (String icon : NNoteTypes.ALL_USER_ICONS) {
            list.add(createIconValue(icon));
        }
        return ComboboxHelper.createCombobox(sapp1.app(), list.toArray(new NamedValue[0]));
    }

    protected NamedValue createNodeTypeFamilyNameValue(String id) {
        return new NamedValue(false, id, sapp.app().i18n().getString("NodeTypeFamily." + id), null);
    }

    protected NamedValue createIconValue(String id) {
        return new NamedValue(false, id,
                sapp.app().i18n().getString("Icon." + id),
                id);
    }

    protected NNote getNode() {
        node.setName(nameEditor.getText());
        if (nameEditor.getText() == null || nameEditor.getText().trim().length() == 0) {
            String ct = node.getContentType();
            nameEditor.setText(ct);
            node.setName(sapp.app().i18n().getString("NodeTypeFamily." + ct));
            //throw new IllegalArgumentException("missing node name");
        }
        NamedValue selectedIcon = (NamedValue) iconEditor.getSelectedItem();
        node.setIcon(selectedIcon != null ? selectedIcon.getId() : null);
        node.setReadOnly(readOnlyEditor.isSelected());
        node.setTitleBackground(OtherUtils.formatColor(backgroundEditor.getColorValue()));
        node.setTitleForeground(OtherUtils.formatColor(foregroundEditor.getColorValue()));
        node.setTitleBold(boldEditor.isSelected());
        node.setTitleItalic(italicEditor.isSelected());
        node.setTitleItalic(underlinedEditor.isSelected());
        return node;
    }

    protected void install() {
    }

    protected void uninstall() {
    }

    protected void ok() {
        uninstall();
        this.ok = true;
        setVisible(false);
    }

    protected void cancel() {
        uninstall();
        this.ok = false;
        setVisible(false);
    }

    public NNote showDialog(Consumer<Exception> exHandler) {
        while (true) {
            install();
            this.ok = false;
            pack();
            setLocationRelativeTo((JFrame) sapp.app().mainWindow().get().component());
            setVisible(true);
            try {
                return get();
            } catch (Exception ex) {
                exHandler.accept(ex);
            }
        }
    }

    public NNote get() {
        if (ok) {
            return getNode();
        }
        return null;
    }

}
