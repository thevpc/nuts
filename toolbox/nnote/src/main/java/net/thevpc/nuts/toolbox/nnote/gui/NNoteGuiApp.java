/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui;

import net.thevpc.nuts.toolbox.nnote.gui.util.NNoteError;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import net.thevpc.nuts.toolbox.nnote.gui.tree.NNoteDocumentTree;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.thevpc.common.iconset.ResourcesIconSet;
import net.thevpc.common.swing.DateTimeLabel;
import net.thevpc.common.swing.DefaultRecentFilesModel;
import net.thevpc.common.swing.RecentFileEvent;
import net.thevpc.common.swing.FileSelectedListener;
import net.thevpc.common.swing.JSplashScreen;
import net.thevpc.common.swing.MemoryUseIconTray;
import net.thevpc.common.swing.RecentFilesMenu;
import net.thevpc.echo.AppDockingWorkspace;
import net.thevpc.echo.AppToolAction;
import net.thevpc.echo.AppToolWindow;
import net.thevpc.echo.AppToolWindowAnchor;
import net.thevpc.echo.AppTools;
import net.thevpc.echo.AppWindowDisplayMode;
import net.thevpc.echo.Application;
import net.thevpc.echo.swing.SwingApplications;
import net.thevpc.echo.swing.mydoggy.MyDoggyAppDockingWorkspace;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.toolbox.nnote.NNoteSplashScreen;
import net.thevpc.nuts.toolbox.nnote.gui.actions.NNoteAction;
import net.thevpc.nuts.toolbox.nnote.gui.dialogs.EnterPasswordDialog;
import net.thevpc.nuts.toolbox.nnote.gui.search.SearchResultPanel;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.nuts.toolbox.nnote.model.NNoteConfig;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;
import net.thevpc.swing.plaf.UIPlaf;
import net.thevpc.swing.plaf.UIPlafManager;

/**
 *
 * @author vpc
 */
public class NNoteGuiApp {

    private NutsApplicationContext appContext;
    private NNoteService service;
    private NNoteConfig config;
    private Application app;
    private RecentFilesMenu recentFilesMenu;
    private NNoteDocumentTree tree;
    private JSplashScreen jSplashScreen;
    private SearchResultPanel searchResultsTool;
    private AppToolWindow documentTool;
    private List<String> recentSearchQueries = new ArrayList<>();

    public NNoteGuiApp(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    public NNoteDocumentTree tree() {
        return tree;
    }

    public SearchResultPanel searchResultsTool() {
        return searchResultsTool;
    }

    public NNoteService service() {
        return service;
    }

    public void onChangePath(String newPath) {
        if (newPath == null || newPath.length() == 0) {
            newPath = "<" + app.i18n().getString("Message.noName") + ">";
        } else {
            recentFilesMenu.addFile(newPath);
            config.addRecentFile(newPath);
        }
        app.mainWindow().get().title().set("N-Note: " + newPath);
    }

    public String getValidLastOpenPath() {
        String p = config.getLastOpenPath();
        if (!OtherUtils.isBlank(p)) {
            File f = new File(p);
            if (f.isDirectory()) {
                return f.getPath();
            }
            if (f.isFile()) {
                File parentFile = f.getParentFile();
                if (parentFile != null) {
                    return parentFile.getPath();
                }
            }
        }
        return service().getDefaultDocumentsFolder().getPath();
    }

    public void setLastOpenPath(String path) {
        if (path != null) {
            File f = new File(path);
            if (f.isDirectory()) {
                config.setLastOpenPath(f.getPath());
                saveConfig();
            } else {
                File p = f.getParentFile();
                if (p != null) {
                    config.setLastOpenPath(p.getPath());
                    saveConfig();
                }
            }
        } else {
            config.setLastOpenPath(null);
            saveConfig();
        }
    }

    public void bindConfig() {
        app.iconSet().id().listeners().add(x -> {
            config.setIconSet(((String) x.getNewValue()));
            saveConfig();
        });
        app.i18n().locale().listeners().add(x -> {
            config.setLocale(((Locale) x.getNewValue()).toString());
            saveConfig();
        });
        app.mainWindow().get().displayMode().listeners().add(x -> {
            config.setDisplayMode(((AppWindowDisplayMode) x.getNewValue()));
            saveConfig();
        });
        app.mainWindow().get().toolBar().get().visible().listeners().add(x -> {
            config.setDisplayToolBar(((Boolean) x.getNewValue()));
            saveConfig();
        });
        app.mainWindow().get().statusBar().get().visible().listeners().add(x -> {
            config.setDisplayStatusBar(((Boolean) x.getNewValue()));
            saveConfig();
        });
        UIPlafManager.getCurrentManager().addListener(x -> {
            config.setPlaf(((UIPlaf) x).getId());
            saveConfig();
        });
        recentFilesMenu.addFileSelectedListener(new FileSelectedListener() {
            @Override
            public void fileSelected(RecentFileEvent event) {
                tree.openDocument(new File(event.getFile()));
            }
        });
    }

    public void saveConfig() {
        service.saveConfig(config);
    }

    public void loadConfig() {
        config = service.loadConfig(() -> {
            //default config...
            NNoteConfig c = new NNoteConfig();
            c.setIconSet("feather-black-16");
            c.setPlaf("FlatLight");
            return c;
        });
    }

    public void applyConfigToUI() {
        if (app.iconSets().containsKey(config.getIconSet())) {
            app.iconSet().id().set(config.getIconSet());
        }
        if (config.getLocale() != null && config.getLocale().length() > 0) {
            app.i18n().locale().set(new Locale(config.getLocale()));
        }
        if (config.getPlaf() != null && config.getPlaf().length() > 0) {
            SwingUtilities.invokeLater(() -> UIPlafManager.INSTANCE.apply(config.getPlaf()));
        }
        if (config.getDisplayMode() != null) {
            app.mainWindow().get().displayMode().set(config.getDisplayMode());
        }
        if (config.getDisplayToolBar() != null) {
            app.mainWindow().get().toolBar().get().visible().set(config.getDisplayToolBar());
        }
        if (config.getDisplayStatusBar() != null) {
            app.mainWindow().get().statusBar().get().visible().set(config.getDisplayStatusBar());
        }
        recentFilesMenu.getRecentFilesModel().setFiles(
                (config.getRecentFiles() == null ? Collections.EMPTY_LIST : config.getRecentFiles())
        );
    }

    public void run() {
//        AppEditorThemes editorThemes = new AppEditorThemes();
        app = SwingApplications.Apps.Default();
        AppTools tools = app.tools();
        tools.config().configurableLargeIcon().set(false);
        tools.config().configurableTooltip().set(false);
        NNoteSplashScreen.get().tic();
        app.builder().mainWindowBuilder().get().workspaceFactory().set(MyDoggyAppDockingWorkspace.factory());
        app.i18n().bundles().add("net.thevpc.nuts.toolbox.nnote.messages.locale-independent");
        app.i18n().bundles().add("net.thevpc.nuts.toolbox.nnote.messages.messages");
        for (String iconSetId : new String[]{"feather-black", "feather-white"}) {
            for (int iconSize : new int[]{16, 24, 32}) {
                app.iconSets().add(new ResourcesIconSet(
                        iconSetId + "-" + iconSize, iconSize,
                        "/net/thevpc/nuts/toolbox/nnote/iconsets/" + iconSetId,
                        getClass().getClassLoader()));
            }
        }
        NNoteSplashScreen.get().tic();
        service = new NNoteService(appContext, app.i18n());
        System.out.println("loading config: " + service.getConfigFilePath());

        loadConfig();
        //initialize UI from config before loading the window...
        if (app.iconSets().containsKey(config.getIconSet())) {
            app.iconSet().id().set(config.getIconSet());
        } else {
            app.iconSet().id().set(app.iconSets().values().get(0).getId());
        }
        if (config.getLocale() != null && config.getLocale().length() > 0) {
            app.i18n().locale().set(new Locale(config.getLocale()));
        }
        if (config.getPlaf() != null && config.getPlaf().length() > 0) {
            UIPlafManager.getCurrentManager().apply(config.getPlaf());
        }

        NNoteSplashScreen.get().tic();
        app.start();
        app.mainWindow().get().centerOnDefaultMonitor();
        NNoteSplashScreen.get().tic();
        app.mainWindow().get().title().set("N-Note");
        app.mainWindow().get().icon().set(new ImageIcon(getClass().getResource("/net/thevpc/nuts/toolbox/nnote/nnote.png")));

        AppDockingWorkspace ws = (AppDockingWorkspace) app.mainWindow().get().workspace().get();
        NNoteSplashScreen.get().tic();

        tree = new NNoteDocumentTree(this);
        NNoteEditor content = new NNoteEditor(this, false);
        NNoteSplashScreen.get().tic();
        tree.addNoteSelectionListener(n -> content.setNote(n));
        content.addListener(n -> tree.setSelectedNote(n));
        searchResultsTool = new SearchResultPanel(this);

//        NNoteDocumentTree favourites = new NNoteDocumentTree(this);
//        NNoteDocumentTree openFiles = new NNoteDocumentTree(app);
        NNoteSplashScreen.get().tic();
        documentTool = ws.addTool("Tools.Document", tree, AppToolWindowAnchor.LEFT);
//        ws.addTool("Tools.Favorites", favourites, AppToolWindowAnchor.LEFT);
//        ws.addTool("Open Documents", "", null, favourites, AppToolWindowAnchor.LEFT);
//        ws.addTool("Recent Documents", "", null, favourites, AppToolWindowAnchor.LEFT);
        AppToolWindow resultPanelTool = ws.addTool("Tools.SearchResults", searchResultsTool, AppToolWindowAnchor.BOTTOM);
        searchResultsTool.setResultPanelTool(resultPanelTool);

        NNoteSplashScreen.get().tic();
        ws.addContent("Content", content);

        NNoteSplashScreen.get().tic();

        NNoteSplashScreen.get().tic();
        tools.addHorizontalGlue("/mainWindow/statusBar/Default/glue");
        NNoteSplashScreen.get().tic();
        tools.addCustomTool("/mainWindow/statusBar/Default/calendar", context -> new DateTimeLabel().setDateTimeFormatter("yyy-MM-dd HH:mm:ss"));
        NNoteSplashScreen.get().tic();
        tools.addHorizontalSeparator("/mainWindow/statusBar/Default/glue");
        tools.addCustomTool("/mainWindow/statusBar/Default/memory", context -> new MemoryUseIconTray(true));
        NNoteSplashScreen.get().tic();

        tools.addFolder("/mainWindow/menuBar/File");

        tools.addAction(new NNoteAction("NewFile", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.openNewDocument(true);
            }
        }, "/mainWindow/menuBar/File/NewFile", "/mainWindow/toolBar/Default/NewFile");
        NNoteSplashScreen.get().tic();

        tools.addAction(new NNoteAction("OpenFile", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.openDocument(true);
            }
        }, "/mainWindow/menuBar/File/Open", "/mainWindow/toolBar/Default/Open");

        NNoteSplashScreen.get().tic();
        tools.addAction(new NNoteAction("Save", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.saveDocument();
            }
        }, "/mainWindow/menuBar/File/Save", "/mainWindow/toolBar/Default/Save");
        NNoteSplashScreen.get().tic();

//        tools.addAction(
//                new NNoteAction("SaveAll", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        },"/mainWindow/menuBar/File/SaveAll", "/mainWindow/toolBar/Default/SaveAll");
        tools.addAction(new NNoteAction("SaveAs", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.saveAsDocument();
            }
        }, "/mainWindow/menuBar/File/SaveAs"/*, "/mainWindow/toolBar/Default/SaveAs"*/);
        NNoteSplashScreen.get().tic();
        recentFilesMenu = new RecentFilesMenu(app.i18n().getString("Action.recentFiles"), new DefaultRecentFilesModel());

        NNoteSplashScreen.get().tic();
        tools.addCustomTool("/mainWindow/menuBar/File/LoadRecent", x -> recentFilesMenu);
//
//        tools.addSeparator("/mainWindow/menuBar/File/LoadRecent/Separator1");
//        tools.addAction(new NNoteAction("ClearRecentFiles", this) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        }, "/mainWindow/menuBar/File/LoadRecent/Clear");
        NNoteSplashScreen.get().tic();
        tools.addSeparator("/mainWindow/menuBar/File/Separator2");
//        tools.addAction("/mainWindow/menuBar/File/Settings", "/mainWindow/toolBar/Default/Settings");
//        tools.addSeparator("/mainWindow/menuBar/File/Separator3");
        NNoteSplashScreen.get().tic();
        tools.addAction(new NNoteAction("Exit", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tree.isModifiedDocument()) {
                    tree.saveDocument();
                }
                app.shutdown();
            }
        },
                "/mainWindow/menuBar/File/Exit");

        tools.addFolder("/mainWindow/menuBar/Edit");
        AppToolAction a = tools.addAction(new NNoteAction("Search", this) {
            {
//                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift f"));
//                putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke("control shift f"));
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                tree.onSearch();
            }
        }, "/mainWindow/menuBar/Edit/Search"/*, "/mainWindow/toolBar/Default/SaveAs"*/);
        a.mnemonic().set(KeyEvent.VK_F);
        a.accelerator().set("control shift F");

//        tools.addSeparator("/mainWindow/toolBar/Default/Separator1");
//        tools.addAction("/mainWindow/menuBar/Edit/Copy", "/mainWindow/toolBar/Default/Copy");
//        tools.addAction("/mainWindow/menuBar/Edit/Cut", "/mainWindow/toolBar/Default/Cut");
//        tools.addAction("/mainWindow/menuBar/Edit/Paste", "/mainWindow/toolBar/Default/Paste");
//        tools.addAction("/mainWindow/menuBar/Edit/Undo", "/mainWindow/toolBar/Default/Undo");
//        tools.addAction("/mainWindow/menuBar/Edit/Redo", "/mainWindow/toolBar/Default/Redo");
        NNoteSplashScreen.get().tic();
        SwingApplications.Helper.addViewToolActions(app);
        NNoteSplashScreen.get().tic();
        SwingApplications.Helper.addViewPlafActions(app);
        NNoteSplashScreen.get().tic();
        SwingApplications.Helper.addViewLocaleActions(app, new Locale[]{Locale.ENGLISH, Locale.FRENCH});
        NNoteSplashScreen.get().tic();

        SwingApplications.Helper.addViewIconActions(app);
        NNoteSplashScreen.get().tic();
        SwingApplications.Helper.addViewAppearanceActions(app);
        NNoteSplashScreen.get().tic();
        tools.addFolder("/mainWindow/menuBar/Help");
        tools.addAction(new NNoteAction("About", this) {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAbout();
            }
        },
                "/mainWindow/menuBar/Help/About");
        NNoteSplashScreen.get().tic();
        documentTool.active().set(true);
        applyConfigToUI();
        NNoteSplashScreen.get().tic();
        bindConfig();
        NNoteSplashScreen.get().tic();
        tree.openNewDocument(false);
//        folders.openDocument(service().createSampleDocumentNote());
        NNoteSplashScreen.get().tic();
        NNoteSplashScreen.get().closeSplash();
        app.waitFor();
    }

    public AppToolWindow documentTool() {
        return documentTool;
    }

    public Application app() {
        return app;
    }

    public JFrame frame() {
        return (JFrame) app.mainWindow().get().component();
    }

    public void showAbout() {
        JOptionPane.showMessageDialog(frame(), "N-Note Application");
    }

    public void showError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
                frame(), e.getMessage()
        );
    }

    public void showError(NNoteError e) {
        if (e != null) {
            if (e.getEx() != null) {
                showError(e.getEx());
            }
        }
    }

    public String askForPassword(String path) {
        EnterPasswordDialog d = new EnterPasswordDialog(this);
        return d.showDialog(this::showError);
    }

    public NNoteConfig config() {
        return config;
    }

    public List<String> getRecentSearchQueries() {
        return recentSearchQueries;
    }

    public void addRecentSearchQuery(String query) {
        if (query != null && query.trim().length() > 0) {
            recentSearchQueries.add(0, query.trim());
            recentSearchQueries = new ArrayList<>(new LinkedHashSet<String>(recentSearchQueries));
        }
    }
}
