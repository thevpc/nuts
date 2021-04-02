/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs;

import net.thevpc.nuts.toolbox.nnote.service.templates.UrlCardNNoteTemplate;
import net.thevpc.common.swing.NamedValue;
import net.thevpc.nuts.toolbox.nnote.gui.util.ComboboxHelper;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import net.thevpc.common.swing.ExtensionFileChooserFilter;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTemplate;
import net.thevpc.nuts.toolbox.nnote.gui.util.OkCancelAppDialog;
import net.thevpc.nuts.toolbox.nnote.service.templates.EthernetConnectionTemplate;
import net.thevpc.nuts.toolbox.nnote.service.templates.WifiConnectionTemplate;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;
import net.thevpc.nuts.toolbox.nnote.gui.util.FileComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.PasswordComponent;
import net.thevpc.nuts.toolbox.nnote.gui.util.URLComponent;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;

/**
 *
 * @author vpc
 */
public class NewNoteDialog extends OkCancelAppDialog {

    private JTextField nameText;
    private JComboBox iconList;
    private JComboBox typeList;
    private JLabel valueLabel;
//    private JTextField typeSourceValue;

    private PasswordComponent typePasswordValue;
    private FileComponent typeFileValue;
    private URLComponent typeURLValue;
    private JTextField typeShortStringValue;

    private JScrollPane typeDescriptionContent;
    private JEditorPane typeDescription;

    private boolean ok = false;

    public NewNoteDialog(NNoteGuiApp sapp) {
        super(sapp, "Message.addNewNote");

        this.valueLabel = new JLabel(sapp.app().i18n().getString("Message.valueLabel"));
        nameText = new JTextField("");
        iconList = createIconListComponent(sapp);
        typeDescriptionContent = new JScrollPane(typeDescription = new JEditorPane("text/html", ""));
        typeDescriptionContent.setPreferredSize(new Dimension(400, 100));
        typeDescription.setEditable(false);
        typePasswordValue = new PasswordComponent();
        typeShortStringValue = new JTextField();//sapp.getApp()
        typeFileValue = new FileComponent();
        typeURLValue = new URLComponent();

        typeList = createTypeListComponent(sapp);

        GridBagLayoutSupport gbs = GridBagLayoutSupport.load(NewNoteDialog.class.getResource(
                "/net/thevpc/nuts/toolbox/nnote/forms/NewNoteDialog.gbl-form"
        ));
        gbs.bind("valueLabel", new JLabel("valueLabel"));
        gbs.bind("nameLabel", new JLabel(sapp.app().i18n().getString("Message.name")));
        gbs.bind("nameText", nameText);
        gbs.bind("iconLabel", new JLabel(sapp.app().i18n().getString("Message.icon")));
        gbs.bind("iconList", iconList);
        gbs.bind("typeLabel", new JLabel(sapp.app().i18n().getString("Message.noteType")));
        gbs.bind("typeList", typeList);
//        gbs.bind("typeSourceValue", typeSourceValue);
        gbs.bind("valueLabel", valueLabel);
        gbs.bind("typePasswordValue", typePasswordValue);
        gbs.bind("typeURLValue", typeURLValue);
        gbs.bind("typeFileValue", typeFileValue);
        gbs.bind("typeShortStringValue", typeShortStringValue);
        gbs.bind("description", typeDescriptionContent);

        onNoteTypeChange(null);
        typeList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                NamedValue v = (NamedValue) e.getItem();
                onNoteTypeChange(v.isGroup() ? null : v.getId());
            }

        });
        typeList.setSelectedIndex(1); // 0 is a group!
        onNoteTypeChange(((NamedValue) typeList.getSelectedItem()).getId());

        build(gbs.apply(new JPanel()), this::ok, this::cancel);
    }

    private JComboBox createIconListComponent(NNoteGuiApp sapp1) {
        List<NamedValue> list = new ArrayList<>();
        list.add(new NamedValue(false, "", sapp1.app().i18n().getString("Icon.none"), null));
        for (String icon : NNoteTypes.ALL_USER_ICONS) {
            list.add(createIconValue(icon));
        }
        return ComboboxHelper.createCombobox(sapp1.app(), list.toArray(new NamedValue[0]));
    }

    private List<NamedValue> createTypeListNamedValue() {
        List<NamedValue> availableTypes = new ArrayList<>();
//        availableTypes.add(createNNoteTypeFamilyNameGroup("quick-strings"));
//        for (String s : new String[]{NNoteTypes.STRING, NNoteTypes.PASSWORD}) {
//            availableTypes.add(createNNoteTypeFamilyNameValue(s));
//        }
        availableTypes.add(createNNoteTypeFamilyNameGroup("simple-documents"));
        for (String s : new String[]{NNoteTypes.PLAIN, NNoteTypes.RICH_HTML}) {
            availableTypes.add(createNNoteTypeFamilyNameValue(s));
        }
//        availableTypes.add(createNNoteTypeFamilyNameGroup("lists"));
        availableTypes.add(createNNoteTypeFamilyNameValue(NNoteTypes.NOTE_LIST));
        availableTypes.add(createNNoteTypeFamilyNameValue(NNoteTypes.OBJECT_LIST));

        for (NNoteTemplate value : sapp.service().getTemplates()) {
            String s = value.getLabel(sapp.service());
            if (s == null) {
                s = sapp.app().i18n().getString("NNoteTypeFamily." + value.getId());
            }
            NamedValue n = new NamedValue(false, value.getId(), s, sapp.service().getContentTypeIcon(value.getId()));
            availableTypes.add(n);
        }

        availableTypes.add(createNNoteTypeFamilyNameGroup("external"));
        for (String s : new String[]{NNoteTypes.NNOTE_DOCUMENT, NNoteTypes.FILE, NNoteTypes.URL}) {
            availableTypes.add(createNNoteTypeFamilyNameValue(s));
        }


//        if (extra.size() > 0) {
//            availableTypes.add(createNNoteTypeFamilyNameGroup("custom"));
//            for (NNoteTemplate value : extra.values()) {
//                String s = value.getLabel(sapp);
//                if (s == null) {
//                    s = sapp.app().i18n().getString("NNoteTypeFamily." + value.getId());
//                }
//                NamedValue n = new NamedValue(false, value.getId(), s, null);
//                availableTypes.add(n);
//            }
//        }
        availableTypes.add(createNNoteTypeFamilyNameGroup("sources"));
        for (String s : new String[]{
            NNoteTypes.SOURCE_HTML,
            NNoteTypes.SOURCE_MARKDOWN,
            NNoteTypes.SOURCE_NUTS_TEXT_FORMAT,
            NNoteTypes.JAVA,
            NNoteTypes.JAVASCRIPT,
            NNoteTypes.C,
            NNoteTypes.CPP,}) {
            availableTypes.add(createNNoteTypeFamilyNameValue(s));
        }
        return availableTypes;
    }

    private JComboBox createTypeListComponent(NNoteGuiApp sapp1) {
        List<NamedValue> availableTypes = new ArrayList<>();
        List<String> rct = sapp.config().getRecentContentTypes();
        if (rct != null) {
            List<NamedValue> recent = new ArrayList<>();
            for (String id : rct) {
                recent.add(
                        new NamedValue(false, "recent:" + id, sapp.app().i18n().getString("NNoteTypeFamily." + id),
                                sapp.service().getContentTypeIcon(id)
                        )
                );
            }
            if (recent.size() > 0) {
                availableTypes.add(createNNoteTypeFamilyNameGroup("recent-documents"));
                availableTypes.addAll(recent);
            }
        }
        availableTypes.addAll(createTypeListNamedValue());
        return ComboboxHelper.createCombobox(sapp1.app(), availableTypes.toArray(new NamedValue[0]));
    }

    protected NamedValue createNNoteTypeFamilyNameValue(String id) {
        return new NamedValue(false, id,
                sapp.app().i18n().getString("NNoteTypeFamily." + id),
                sapp.service().getContentTypeIcon(id)
        );
    }

    protected NamedValue createNNoteTypeFamilyNameGroup(String id) {
        return new NamedValue(true, id, sapp.app().i18n().getString("NNoteTypeFamily." + id), null);
    }

    protected NamedValue createIconValue(String id) {
        return new NamedValue(false, id,
                sapp.app().i18n().getString("Icon." + id),
                id);
    }

    protected NNote getNote() {
        NNote n = new NNote();
        n.setName(nameText.getText());
        if (nameText.getText() == null || nameText.getText().trim().length() == 0) {
            String betterName = typeList.getSelectedItem().toString();
            nameText.setText(betterName);
            n.setName(betterName);
            //throw new IllegalArgumentException("missing note name");
        }
        NamedValue selectedIcon = (NamedValue) iconList.getSelectedItem();
        n.setIcon(selectedIcon != null ? selectedIcon.getId() : null);
        NamedValue selectedContentType = (NamedValue) typeList.getSelectedItem();
        if (selectedContentType == null) {
            throw new IllegalArgumentException("missing content type");
        }
        String selectedContentTypeId = selectedContentType.getId();
        if (selectedContentTypeId.startsWith("recent:")) {
            selectedContentTypeId = selectedContentTypeId.substring("recent:".length());
        }

        String[] contentTypeId = selectedContentTypeId.split(":");
        n.setContentType(contentTypeId[0]);
        if (contentTypeId.length > 1) {
            n.setEditorType(contentTypeId[1]);
        }
        switch (selectedContentTypeId) {
            case NNoteTypes.STRING: {
                n.setContent(typeShortStringValue.getText());
                break;
            }
            case NNoteTypes.PASSWORD: {
                n.setContent(typePasswordValue.getContentString());
                break;
            }
            case NNoteTypes.FILE:
            case NNoteTypes.NNOTE_DOCUMENT: {
                n.setContent(typeFileValue.getContentString());
                break;
            }
            case NNoteTypes.URL: {
                n.setContent(typeURLValue.getContentString());
                break;
            }
            case NNoteTypes.NOTE_LIST: {
                n.setContent("");
                break;
            }
            case NNoteTypes.OBJECT_LIST: {
                n.setContent(sapp.service().stringifyDescriptor(new NNoteObjectDocument()
                        .setDescriptor(new NNoteObjectDescriptor())
                        .setValues(new ArrayList<>())
                )
                );
                break;
            }
            default: {
                NNoteTemplate z = sapp.service().getTemplate(selectedContentTypeId);
                if (z != null) {
                    z.prepare(n, sapp.service());
                } else {
                    n.setContentType(selectedContentTypeId = NNoteTypes.PLAIN);
                }
            }

        }
        List<String> recentContentTypes = sapp.config().getRecentContentTypes();
        if (recentContentTypes == null) {
            recentContentTypes = new ArrayList<>();
        }
        recentContentTypes.add(0, selectedContentTypeId);
        recentContentTypes = new ArrayList<>(new LinkedHashSet<String>(recentContentTypes));
        int maxRecentContentTypes = 12;
        while (recentContentTypes.size() > maxRecentContentTypes) {
            recentContentTypes.remove(recentContentTypes.size() - 1);
        }
        sapp.config().setRecentContentTypes(recentContentTypes);
        sapp.saveConfig();
        return n;
    }

    protected void install() {
        typeFileValue.install(sapp.app());
        typeURLValue.install(sapp.app());
        typePasswordValue.install(sapp.app());
    }

    protected void uninstall() {
        typeFileValue.uninstall();
        typeURLValue.uninstall();
        typePasswordValue.uninstall();
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
            return getNote();
        }
        return null;
    }

    public String resolveNoteTypeDescription(String id) {
        if (id == null || id.isEmpty() || id.equals("id")) {
            id = "none";
        }
        if (id.startsWith("recent:")) {
            id = id.substring("recent:".length());
        }
        String s = sapp.app().i18n().getString("NNoteTypeFamily." + id + ".help");
        if (s.startsWith("resource://")) {
            URL i = getClass().getClassLoader().getResource(s.substring("resource://".length()));
            if (i == null) {
                throw new IllegalArgumentException("not found resource " + s);
            }
            try (InputStream is = i.openStream()) {
                s = new String(OtherUtils.toByteArray(is));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        String toLowerCase = s.trim().toLowerCase();
        if (!toLowerCase.startsWith("<html>")
                && !toLowerCase.startsWith("<!doctype html>")) {
            s = "<html><body>" + s + "</body></html>";
        }
        return s;
    }

    private void onNoteTypeChange(String id) {
        if (id != null) {
            boolean objList = id.equals(NNoteTypes.OBJECT_LIST);
            boolean fileType = id.equals(NNoteTypes.FILE) || id.equals(NNoteTypes.NNOTE_DOCUMENT);
            boolean urlType = id.equals(NNoteTypes.URL);
            boolean passwordType = id.equals(NNoteTypes.PASSWORD);
            boolean stringType = id.equals(NNoteTypes.STRING);

            typeFileValue.setVisible(fileType);

            typeFileValue.setAcceptAllFileFilterUsed(id.equals(NNoteTypes.FILE));
            typeFileValue.getFileFilters().clear();
            if (id.equals(NNoteTypes.FILE)) {
                typeFileValue.setAcceptAllFileFilterUsed(true);
            } else if (id.equals(NNoteTypes.NNOTE_DOCUMENT)) {
                typeFileValue.setAcceptAllFileFilterUsed(true);
                typeFileValue.getFileFilters().add(new ExtensionFileChooserFilter("nnote",
                        sapp.app().i18n().getString("Message.nnoteDocumentFileFilter")
                ));
            }
            typeURLValue.setVisible(urlType);
            typePasswordValue.setVisible(passwordType);
            typeShortStringValue.setVisible(stringType);
            valueLabel.setVisible(objList || urlType || fileType || passwordType || stringType);
            typeDescription.setText(resolveNoteTypeDescription(id));
            valueLabel.setText(
                    objList ? sapp.app().i18n().getString("Message.valueForObjList")
                            : urlType ? sapp.app().i18n().getString("Message.valueForUrl")
                                    : fileType ? sapp.app().i18n().getString("Message.valueForFile")
                                            : passwordType ? sapp.app().i18n().getString("Message.valueForPassword")
                                                    : stringType ? sapp.app().i18n().getString("Message.valueForString")
                                                            : ""
            );

        } else {
            typeShortStringValue.setVisible(false);
            valueLabel.setVisible(false);
            typeDescription.setText(resolveNoteTypeDescription(null));
            typeFileValue.setVisible(false);
            typePasswordValue.setVisible(false);
            typeURLValue.setVisible(false);
        }
        pack();
    }

}
