/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.tree;

import com.formdev.flatlaf.util.StringUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import net.thevpc.common.swing.ExtensionFileChooserFilter;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.common.swing.tree.TreeTransferHandler;
import net.thevpc.echo.Application;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs.EditNoteDialog;
import net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs.NewNoteDialog;
import net.thevpc.nuts.toolbox.nnote.gui.search.SearchDialog;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.ReturnType;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.model.VNNoteTreeModel;
import net.thevpc.nuts.toolbox.nnote.model.CypherInfo;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.swing.plaf.UIPlafManager;

/**
 *
 * @author vpc
 */
public class NNoteDocumentTree extends JPanel {

    JTree tree;
    private JPopupMenu treePopupMenu;
    private List<VNNoteSelectionListener> listeners = new ArrayList<>();
    private NNoteGuiApp sapp;
    private NNote lastSavedDocument;
    Application app;
    private VNNoteTreeModel model;
    List<TreeAction> actions = new ArrayList<>();

    public NNoteDocumentTree(NNoteGuiApp sapp) {
        super(new BorderLayout());
        this.sapp = sapp;
        this.app = sapp.app();
        model = new VNNoteTreeModel(new VNNote().newDocument());
        tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler(VNNote.class, model));
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath p = e.getNewLeadSelectionPath();
                if (p == null) {
                    fireOnSelectedNote(null);
                } else {
                    fireOnSelectedNote((VNNote) p.getLastPathComponent());
                }

            }
        });
        UIPlafManager.getCurrentManager().addListener((p) -> tree.setCellRenderer(new SimpleDefaultTreeCellRendererImpl(app)));
        tree.setCellRenderer(new SimpleDefaultTreeCellRendererImpl(app));
        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//                    System.out.println("right click " + selRow + "  " + selPath);
                    if (selRow != -1) {
                        VNNote selectedNote = ((VNNote) selPath.getLastPathComponent());
                        tree.setSelectionPath(selPath);
                    } else {
                        tree.clearSelection();
//                        tree.setSelectionPath(
//                                null
////                                new TreePath(tree.getModel().getRoot())
//                        );
                    }
                    if (tree.isShowing()) {
                        treePopupMenu.show(tree, e.getX(), e.getY());
                    }
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//                    System.out.println("left click " + selRow + "  " + selPath);
                    if (selRow != -1) {
                        VNNote selectedNote = ((VNNote) selPath.getLastPathComponent());
                        tree.setSelectionPath(selPath);
                    } else {
                        tree.clearSelection();
//                        tree.setSelectionPath(
//                                null
////                                new TreePath(tree.getModel().getRoot())
//                        );
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        treePopupMenu = new JPopupMenu();
        tree.addPropertyChangeListener("UI", (p) -> SwingUtilities.updateComponentTreeUI(treePopupMenu));
//        tree.setComponentPopupMenu(treePopupMenu);
        treePopupMenu.add(new TreeAction("AddChildNote", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddChild();
                //
            }
        });
        treePopupMenu.add(new TreeAction("AddNoteBefore", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddChildBefore();
            }

            @Override
            protected void onSelectedNote(VNNote note) {
                requireSelectedNote(note);
            }
        });
        treePopupMenu.add(new TreeAction("AddNoteAfter", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddChildAfter();
            }

            @Override
            protected void onSelectedNote(VNNote note) {
                requireSelectedNote(note);
            }
        });
//        JMenu addCustomMenu = new JMenu();
//        SwingApplicationsHelper.registerButton(addCustomMenu, "Action.AddCustom", "$Action.AddCustom", app);
//        treePopupMenu.add(addCustomMenu);
//        addCustomMenu.add(new TreeAction("AddTodayNote") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                NewNoteDialog a = new NewNoteDialog(sapp);
//                NNote n = a.showDialog(NNoteDocumentTree::showError);
//                if (n != null) {
//                    //
//                }
//                //
//            }
//        });
        treePopupMenu.add(new TreeAction("DuplicateNote", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNote();
                if (current != null) {
                    setSelectedNote(current.addDuplicate());
                    updateTree();
                }
            }

            @Override
            protected void onSelectedNote(VNNote note) {
                requireSelectedNote(note);
            }
        });
        treePopupMenu.addSeparator();
        JMenu importCustomMenu = new JMenu();
        SwingApplicationsHelper.registerButton(importCustomMenu, "Action.Import", "$Action.Import.icon", app);
        treePopupMenu.add(importCustomMenu);
        importCustomMenu.add(new TreeAction("ImportAny", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNote();
                if (current == null) {
                    current = (VNNote) tree.getModel().getRoot();
                }
                importFileInto(current);
                updateTree();
            }
        });
        importCustomMenu.add(new TreeAction("ImportNNote", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNote();
                if (current == null) {
                    current = (VNNote) tree.getModel().getRoot();
                }
                importFileInto(current, "nnote");
                updateTree();
            }
        });
        importCustomMenu.add(new TreeAction("ImportCherryTree", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNote();
                if (current == null) {
                    current = (VNNote) tree.getModel().getRoot();
                }
                importFileInto(current, "ctd");
                updateTree();
            }

        });
//        JMenu exportMenu = new JMenu();
//        SwingApplicationsHelper.registerButton(exportMenu, "Action.Export", "$Action.Export.icon", app);
//        treePopupMenu.add(exportMenu);
//        exportMenu.add(new TreeAction("ExportNNote", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
//        exportMenu.add(new TreeAction("ExportCherryTree", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("DeleteNote", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote n = getSelectedNote();
                if (n != null) {
                    n.delete();
                    updateTree();
                    setSelectedNote(null);
                }
            }

            @Override
            protected void onSelectedNote(VNNote note) {
                requireSelectedNote(note);
            }

        });
//        treePopupMenu.addSeparator();
//        JMenu moveMenu = new JMenu();
//        SwingApplicationsHelper.registerButton(moveMenu, "Action.Move", "$Action.Move.icon", app);
//        treePopupMenu.add(moveMenu);
//        moveMenu.add(new TreeAction("MoveUp", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
//        moveMenu.add(new TreeAction("MoveDown", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
//        moveMenu.add(new TreeAction("MoveLeft", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
//        moveMenu.add(new TreeAction("MoveRight", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//
//        });
//        moveMenu.add(new TreeAction("SortNoteAsc", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//        });
//        moveMenu.add(new TreeAction("SortNoteDesc", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//        });

        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("SearchNote", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSearch();
            }

            @Override
            protected void onSelectedNote(VNNote note) {

            }
        });
//        treePopupMenu.add(new TreeAction("SearchAndReplaceNote", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNote(VNNote note) {
//                requireSelectedNote(note);
//            }
//        });
        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("NoteProperties", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEditNote();
            }

            @Override
            protected void onSelectedNote(VNNote note) {
                requireSelectedNote(note);
            }

        });

        VNNote sn = getSelectedNote();
        for (TreeAction action : actions) {
            action.onSelectedNote(sn);
        }
        add(new JScrollPane(tree));
    }

    public ReturnType closeDocument() {
        if (isModifiedDocument()) {
            int s = JOptionPane.showConfirmDialog(sapp.frame(), sapp.app().i18n().getString("Message.askSaveDocument"));
            if (s == JOptionPane.YES_OPTION) {
                return saveDocument();
            } else if (s == JOptionPane.NO_OPTION) {
                return openNewDocument(false);
            } else {
                return ReturnType.CANCEL;
            }
        }
        openNewDocument(false);
        return ReturnType.SUCCESS;
    }

    public ReturnType openNewDocument(boolean closeCurrent) {
        if (!closeCurrent) {
            openDocument(NNote.newDocument());
            return ReturnType.SUCCESS;
        } else {
            ReturnType c = closeDocument();
            if (c == ReturnType.SUCCESS) {
                openDocument(new NNote().setContentType(NNoteTypes.NNOTE_DOCUMENT));
                return ReturnType.SUCCESS;
            }
            return c;
        }
    }

    public void openDocument(NNote note) {
        if (!NNoteTypes.NNOTE_DOCUMENT.equals(note.getContentType())) {
            throw new IllegalArgumentException("expected Document Note");
        }
        sapp.onChangePath(note.getContent());
        SwingUtilities3.invokeLater(() -> {
            model.setRoot(VNNote.of(note));
            snapshotDocument();
        });
    }

    public void setSelectedNote(VNNote note) {
        List<VNNote> elems = new ArrayList<>();
        while (note != null) {
            elems.add(0, note);
            note = note.getParent();
        }
        if (elems.isEmpty()) {
            elems.add(getDocument());
        }
        TreePath tPath = new TreePath(elems.toArray());
        tree.setSelectionPath(tPath);
    }

    public NNoteDocumentTree addNoteSelectionListener(VNNoteSelectionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    public void fireNoteChanged(VNNote note) {
        updateTree();
    }

    private void fireOnSelectedNote(VNNote note) {
        for (TreeAction action : actions) {
            action.onSelectedNote(note);
        }
        for (VNNoteSelectionListener listener : listeners) {
            listener.onSelectionChanged(note);
        }
    }

    public boolean isModifiedDocument() {
        NNote newDoc = getDocument().toNote();
        boolean mod = lastSavedDocument != null && !lastSavedDocument.equals(newDoc);
        if (mod) {
//            System.out.println("modified: " + newDoc + "\nexpected: " + lastSavedDocument);
        }
        return mod;
    }

    public void snapshotDocument() {
        lastSavedDocument = getDocument().toNote();
//        System.out.println("snapshotted:" + lastSavedDocument);
    }

    public ReturnType openDocument(File file) {
        NNote n = sapp.service().loadDocument(file, sapp::askForPassword);
        if (n.error == null) {
            openDocument(n);
            return ReturnType.SUCCESS;
        } else {
            sapp.showError(n.error);
            return ReturnType.FAIL;
        }
    }

    public ReturnType importFileInto(VNNote current, String... preferred) {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(sapp.getValidLastOpenPath()));
        if (preferred.length == 0) {
            jfc.addChoosableFileFilter(new FileFilter() {
                private Set<String> extensions = new HashSet<String>(Arrays.asList("nnote", "ctd"));
                private String description = sapp.app().i18n().getString("Message.nnoteSupportedFileFilters");

                public boolean accept(File f) {
                    //TODO is it correct to handle such case ??
                    if (f.isDirectory()) {
                        String[] strings = f.list();
                        return strings != null && strings.length > 0;//true;
                    }

                    String e = getExtension(f);
                    if (e == null) {
                        e = "";
                    }
                    e = e.toLowerCase();
                    return extensions.contains(e);
                }

                public boolean accept(File dir, String name) {
                    String e = getExtension(name);
                    if (e == null) {
                        e = "";
                    }
                    e = e.toLowerCase();
                    return extensions.contains(e);
                }

                public String getExtension(File f) {
                    if (f != null) {
                        String filename = f.getName();
                        int i = filename.lastIndexOf('.');
                        if (i > 0 && i < filename.length() - 1) {
                            return filename.substring(i + 1).toLowerCase();
                        }
                        ;
                    }
                    return null;
                }

                public String getExtension(String filename) {
                    if (filename != null) {
                        int i = filename.lastIndexOf('.');
                        if (i > 0 && i < filename.length() - 1) {
                            return filename.substring(i + 1).toLowerCase();
                        }
                    }
                    return null;
                }

                public String getDescription() {
                    return description;
                }
            });
        }
        Set<String> preferredSet = new HashSet<>(Arrays.asList(preferred));
        if (preferredSet.isEmpty() || preferredSet.contains("nnote")) {
            jfc.addChoosableFileFilter(new ExtensionFileChooserFilter("nnote", sapp.app().i18n().getString("Message.nnoteDocumentFileFilter")));
        }
        if (preferredSet.isEmpty() || preferredSet.contains("ctd")) {
            jfc.addChoosableFileFilter(new ExtensionFileChooserFilter("ctd", sapp.app().i18n().getString("Message.ctdDocumentFileFilter")));
        }
        jfc.setAcceptAllFileFilterUsed(!preferredSet.isEmpty());
        if (jfc.showOpenDialog(sapp.frame()) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            sapp.setLastOpenPath(file.getPath());
            if (file.getName().endsWith(".nnote")) {
                NNote n = sapp.service().loadDocument(file, sapp::askForPassword);
                for (NNote c : n.getChildren()) {
                    current.addChild(VNNote.of(c));
                }
            } else if (file.getName().endsWith(".ctd")) {
                NNote n = sapp.service().loadCherryTreeXmlFile(file);
                for (NNote c : n.getChildren()) {
                    current.addChild(VNNote.of(c));
                }
            }
            return ReturnType.CANCEL;
        } else {
            return ReturnType.CANCEL;
        }
    }

    public ReturnType openDocument(boolean closeCurrent) {
        if (closeCurrent) {
            ReturnType c = closeDocument();
            if (c != ReturnType.SUCCESS) {
                return c;
            }
        }
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(sapp.getValidLastOpenPath()));
        jfc.addChoosableFileFilter(new ExtensionFileChooserFilter("nnote", sapp.app().i18n().getString("Message.nnoteDocumentFileFilter")));
        jfc.setAcceptAllFileFilterUsed(false);
        if (jfc.showOpenDialog(sapp.frame()) == JFileChooser.APPROVE_OPTION) {
            sapp.setLastOpenPath(jfc.getSelectedFile().getPath());
            return openDocument(jfc.getSelectedFile());
        } else {
            return ReturnType.CANCEL;
        }
    }

    public VNNote getDocument() {
        return (VNNote) model.getRoot();
    }

    public ReturnType saveAsDocument() {
        SecureJFileChooserImpl jfc = new SecureJFileChooserImpl(this);
        jfc.setCurrentDirectory(new File(sapp.getValidLastOpenPath()));
        jfc.addChoosableFileFilter(new ExtensionFileChooserFilter("nnote", sapp.app().i18n().getString("Message.nnoteDocumentFileFilter")));
        jfc.setAcceptAllFileFilterUsed(false);
        boolean doSecureDocument = false;
        if (getDocument().getCypherInfo() == null) {
            jfc.getSecureCheckbox().setSelected(false);
            jfc.getSecureCheckbox().setVisible(true);
            jfc.getSecureCheckbox().setText(sapp.app().i18n().getString("Message.secureDocument"));
            doSecureDocument = true;
        } else {
            jfc.getSecureCheckbox().setSelected(isSecureAlgo(getDocument().getCypherInfo().getAlgo()));
            jfc.getSecureCheckbox().setVisible(true);
            jfc.getSecureCheckbox().setText(sapp.app().i18n().getString("Message.secureDocument"));
            doSecureDocument = false;
        }
        if (jfc.showSaveDialog(sapp.frame()) == JFileChooser.APPROVE_OPTION) {
            sapp.setLastOpenPath(jfc.getSelectedFile().getPath());
            if (doSecureDocument && jfc.getSecureCheckbox().isSelected()) {
                getDocument().setCypherInfo(new CypherInfo(NNoteService.SECURE_ALGO, ""));
            }
            try {
                String canonicalPath = jfc.getSelectedFile().getCanonicalPath();
                if (!canonicalPath.endsWith(".nnote") && !new File(canonicalPath).exists()) {
                    canonicalPath = canonicalPath + ".nnote";
                }
                getDocument().setContent(canonicalPath);
                sapp.service().saveDocument(getDocument().toNote(), sapp::askForPassword);
                sapp.onChangePath(canonicalPath);
                snapshotDocument();
                sapp.config().addRecentFile(canonicalPath);
                sapp.saveConfig();
                return ReturnType.SUCCESS;
            } catch (Exception ex) {
                sapp.showError(ex);
                return ReturnType.FAIL;
            }
        }
        return ReturnType.CANCEL;
    }

    private boolean isSecureAlgo(String s) {
        return s != null && s.length() > 0;
    }

    public ReturnType saveDocument() {
        if (StringUtils.isEmpty(getDocument().getContent())) {
            return saveAsDocument();
        } else {
            try {
                sapp.onChangePath(getDocument().getContent());
                sapp.service().saveDocument(getDocument().toNote(), sapp::askForPassword);
                return ReturnType.SUCCESS;
            } catch (Exception ex) {
                sapp.showError(ex);
                return ReturnType.FAIL;
            }
        }
    }

//    protected Icon resolveIcon(String name) {
//        if (name == null || name.length() == 0) {
//            return null;
//        }
//        return app.iconSet().icon(name).get();
//    }
    
    
    public void updateTree() {
//        model = new VNNoteTreeModel((VNNote) model.getRoot());
//        tree = new JTree(model);
        TreePath o = tree.getSelectionPath();
        model.treeStructureChanged();
        tree.invalidate();
        tree.revalidate();
        tree.setSelectionPath(o);
    }

    public VNNote getSelectedNoteOrDocument() {
        TreePath p = tree.getSelectionPath();
        if (p != null) {
            VNNote c = (VNNote) p.getLastPathComponent();
            if (c != null) {
                return c;
            }
        }
        return (VNNote) tree.getModel().getRoot();
    }

    public VNNote getSelectedNote() {
        TreePath p = tree.getSelectionPath();
        if (p != null) {
            VNNote c = (VNNote) p.getLastPathComponent();
            if (c != null && c != tree.getModel().getRoot()) {
                return c;
            }
        }
        return null;
    }

    public void onAddChildAfter() {
        NewNoteDialog a = new NewNoteDialog(sapp);
        NNote n = a.showDialog(sapp::showError);
        if (n != null) {
            VNNote current = getSelectedNote();
            if (current != null) {
                VNNote cc = new VNNote().copyFrom(n);
                sapp.service().prepareChildForInsertion(current, cc);
                current.addAfterThis(cc);
                updateTree();
                setSelectedNote(cc);
            }
        }
    }

    public void onAddChildBefore() {
        NewNoteDialog a = new NewNoteDialog(sapp);
        NNote n = a.showDialog(sapp::showError);
        if (n != null) {
            VNNote current = getSelectedNote();
            if (current != null) {
                VNNote cc = new VNNote().copyFrom(n);
                sapp.service().prepareChildForInsertion(current, cc);
                current.addBeforeThis(cc);
                updateTree();
                setSelectedNote(cc);
            }
        }
    }

    public void onAddChild() {
        NewNoteDialog a = new NewNoteDialog(sapp);
        NNote n = a.showDialog(sapp::showError);
        if (n != null) {
            VNNote current = getSelectedNoteOrDocument();
            VNNote cc = new VNNote().copyFrom(n);
            sapp.service().prepareChildForInsertion(current, cc);
            current.addChild(cc);
            updateTree();
            setSelectedNote(cc);
        }
    }

    public void onEditNote() {
        NNote n = new EditNoteDialog(sapp, getSelectedNote()).showDialog();
        if (n != null) {
            tree.invalidate();
            tree.repaint();
            fireOnSelectedNote(getSelectedNote());
        }
    }

    public void onSearch() {
        SearchDialog dialog = new SearchDialog(sapp);
        dialog.showDialogAndSearch(sapp, getSelectedNoteOrDocument());
    }

//    public void expandTree(JTree tree) {
//        VNNoteTreeModel root
//                = (VNNoteTreeModel) tree.getModel().getRoot();
//        Enumeration e = root.breadthFirstEnumeration();
//        while (e.hasMoreElements()) {
//            Object note = e.nextElement();
////            if(note.isLeaf()) continue;
////            int row = tree.getRowForPath(new TreePath(note.getPath()));
////            tree.expandRow(row);
//        }
//    }
//
}
