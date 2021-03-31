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
import net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs.EditNodeDialog;
import net.thevpc.nuts.toolbox.nnote.gui.tree.dialogs.NewNodeDialog;
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
        model = new VNNoteTreeModel(new VNNote().copyFrom(new NNote().setName("root")));
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
                    fireOnSelectedNode(null);
                } else {
                    fireOnSelectedNode((VNNote) p.getLastPathComponent());
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
                        VNNote selectedNode = ((VNNote) selPath.getLastPathComponent());
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
                        VNNote selectedNode = ((VNNote) selPath.getLastPathComponent());
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
        treePopupMenu.add(new TreeAction("AddChildNode", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewNodeDialog a = new NewNodeDialog(sapp);
                NNote n = a.showDialog(sapp::showError);
                if (n != null) {
                    VNNote current = getSelectedNode();
                    if (current == null) {
                        current = (VNNote) tree.getModel().getRoot();
                    }
                    VNNote cc = new VNNote().copyFrom(n);
                    current.addChild(cc);
                    updateTree();
                    setSelectedNode(cc);
                }
                //
            }
        });
        treePopupMenu.add(new TreeAction("AddNodeBefore", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewNodeDialog a = new NewNodeDialog(sapp);
                NNote n = a.showDialog(sapp::showError);
                if (n != null) {
                    VNNote current = getSelectedNode();
                    if (current != null) {
                        VNNote cc = new VNNote().copyFrom(n);
                        current.addBeforeThis(cc);
                        updateTree();
                        setSelectedNode(cc);
                    }
                }
            }

            @Override
            protected void onSelectedNode(VNNote node) {
                requireSelectedNode(node);
            }
        });
        treePopupMenu.add(new TreeAction("AddNodeAfter", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewNodeDialog a = new NewNodeDialog(sapp);
                NNote n = a.showDialog(sapp::showError);
                if (n != null) {
                    VNNote current = getSelectedNode();
                    if (current != null) {
                        VNNote cc = new VNNote().copyFrom(n);
                        current.addAfterThis(cc);
                        updateTree();
                        setSelectedNode(cc);
                    }
                }
            }

            @Override
            protected void onSelectedNode(VNNote node) {
                requireSelectedNode(node);
            }
        });
//        JMenu addCustomMenu = new JMenu();
//        SwingApplicationsHelper.registerButton(addCustomMenu, "Action.AddCustom", "$Action.AddCustom", app);
//        treePopupMenu.add(addCustomMenu);
//        addCustomMenu.add(new TreeAction("AddTodayNode") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                NewNodeDialog a = new NewNodeDialog(sapp);
//                NNote n = a.showDialog(NNoteDocumentTree::showError);
//                if (n != null) {
//                    //
//                }
//                //
//            }
//        });
        treePopupMenu.add(new TreeAction("DuplicateNode", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNode();
                if (current != null) {
                    setSelectedNode(current.addDuplicate());
                    updateTree();
                }
            }

            @Override
            protected void onSelectedNode(VNNote node) {
                requireSelectedNode(node);
            }
        });
        treePopupMenu.addSeparator();
        JMenu importCustomMenu = new JMenu();
        SwingApplicationsHelper.registerButton(importCustomMenu, "Action.Import", "$Action.Import.icon", app);
        treePopupMenu.add(importCustomMenu);
        importCustomMenu.add(new TreeAction("ImportAny", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote current = getSelectedNode();
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
                VNNote current = getSelectedNode();
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
                VNNote current = getSelectedNode();
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
//            }
//
//        });
        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("DeleteNode", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote n = getSelectedNode();
                if (n != null) {
                    n.delete();
                    updateTree();
                    setSelectedNode(null);
                }
            }

            @Override
            protected void onSelectedNode(VNNote node) {
                requireSelectedNode(node);
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
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
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
//            }
//
//        });
//        moveMenu.add(new TreeAction("SortNodeAsc", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//        });
//        moveMenu.add(new TreeAction("SortNodeDesc", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//        });

        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("SearchNode", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSearch();
            }

            @Override
            protected void onSelectedNode(VNNote node) {

            }
        });
//        treePopupMenu.add(new TreeAction("SearchAndReplaceNode", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //
//            }
//
//            @Override
//            protected void onSelectedNode(VNNote node) {
//                requireSelectedNode(node);
//            }
//        });
        treePopupMenu.addSeparator();
        treePopupMenu.add(new TreeAction("NodeProperties", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VNNote vn = getSelectedNode();
                EditNodeDialog d = new EditNodeDialog(sapp, vn.toNode());
                NNote n = d.showDialog(sapp::showError);
                if (n != null) {
                    vn.setName(n.getName());
                    vn.setIcon(n.getIcon());
                    vn.setReadOnly(n.isReadOnly());
                    vn.setTitleForeground(n.getTitleForeground());
                    vn.setTitleBackground(n.getTitleBackground());
                    vn.setTitleBold(n.isTitleBold());
                    vn.setTitleItalic(n.isTitleItalic());
                    vn.setTitleUnderlined(n.isTitleUnderlined());
                    tree.invalidate();
                    tree.repaint();
                }
            }

            @Override
            protected void onSelectedNode(VNNote node) {
                requireSelectedNode(node);
            }

        });

        VNNote sn = getSelectedNode();
        for (TreeAction action : actions) {
            action.onSelectedNode(sn);
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
            openDocument(NNote.newDocument(null));
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

    public void openDocument(NNote node) {
        if (!NNoteTypes.NNOTE_DOCUMENT.equals(node.getContentType())) {
            throw new IllegalArgumentException("expected Document Node");
        }
        sapp.onChangePath(node.getContent());
        SwingUtilities3.invokeLater(() -> {
            model.setRoot(VNNote.of(node));
            snapshotDocument();
        });
    }

    public void setSelectedNode(VNNote node) {
        List<VNNote> elems = new ArrayList<>();
        while (node != null) {
            elems.add(0, node);
            node = node.getParent();
        }
        if (elems.isEmpty()) {
            elems.add(getDocument());
        }
        TreePath tPath = new TreePath(elems.toArray());
        tree.setSelectionPath(tPath);
    }

    public NNoteDocumentTree addNodeSelectionListener(VNNoteSelectionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    private void fireOnSelectedNode(VNNote node) {
        for (TreeAction action : actions) {
            action.onSelectedNode(node);
        }
        for (VNNoteSelectionListener listener : listeners) {
            listener.onSelectionChanged(node);
        }
    }

    public boolean isModifiedDocument() {
        NNote newDoc = getDocument().toNode();
        boolean mod = lastSavedDocument != null && !lastSavedDocument.equals(newDoc);
        if (mod) {
//            System.out.println("modified: " + newDoc + "\nexpected: " + lastSavedDocument);
        }
        return mod;
    }

    public void snapshotDocument() {
        lastSavedDocument = getDocument().toNode();
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
                sapp.service().saveDocument(getDocument().toNode(), sapp::askForPassword);
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
                sapp.service().saveDocument(getDocument().toNode(), sapp::askForPassword);
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

    public VNNote getSelectedNodeOrDocument() {
        TreePath p = tree.getSelectionPath();
        if (p != null) {
            VNNote c = (VNNote) p.getLastPathComponent();
            if (c != null) {
                return c;
            }
        }
        return (VNNote) tree.getModel().getRoot();
    }

    public VNNote getSelectedNode() {
        TreePath p = tree.getSelectionPath();
        if (p != null) {
            VNNote c = (VNNote) p.getLastPathComponent();
            if (c != null && c != tree.getModel().getRoot()) {
                return c;
            }
        }
        return null;
    }

    public void onSearch() {
        SearchDialog dialog = new SearchDialog(sapp);
        dialog.showDialogAndSearch(sapp, getSelectedNodeOrDocument());
    }

//    public void expandTree(JTree tree) {
//        VNNoteTreeModel root
//                = (VNNoteTreeModel) tree.getModel().getRoot();
//        Enumeration e = root.breadthFirstEnumeration();
//        while (e.hasMoreElements()) {
//            Object node = e.nextElement();
////            if(node.isLeaf()) continue;
////            int row = tree.getRowForPath(new TreePath(node.getPath()));
////            tree.expandRow(row);
//        }
//    }
//
}
