package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.model.App;
import net.thevpc.nuts.installer.model.ButtonInfo;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.NutsInstaller;
import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.UIHelper;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PackagesPanel extends AbstractInstallPanel {
    private final ItemListener buttonInfoItemListener;
    //    private final MouseAdapter buttonInfoMouseListener;
    private final JScrollPane jsp;
    JPanel panel;
    JComponent panelScroll;
    JEditorPane jep;
    int w = 160;
    java.util.List<JToggleButton> buttons = new ArrayList<>();

    public PackagesPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please select the packages you want to install along with nuts"), BorderLayout.PAGE_START);
        panel = new JPanel(new GridBagLayout());
//        panel.setPreferredSize(new Dimension(2*w,w));
//        panel.setMaximumSize(new Dimension(2*w,w));
        add(panelScroll = UIHelper.margins(new JScrollPane(panel), 10), BorderLayout.CENTER);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);

        jsp = new JScrollPane(jep);
        jsp.setMinimumSize(new Dimension(400, 200));
        jsp.setMaximumSize(new Dimension(1000, 100));
        jsp.setPreferredSize(new Dimension(100, 100));
        add(jsp, BorderLayout.PAGE_END);
        jep.setText("<html><body>selection the <strong>stable</strong> version for production</body></html>");
        buttonInfoItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateJep();
            }
        };
//        buttonInfoMouseListener = new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                JToggleButton a = (JToggleButton) e.getSource();
//                PackageButtonInfo button = (PackageButtonInfo) a.getClientProperty("buttonInfo");
//                jep.setText(button.desc);
//            }
//        };
    }

    private void updateJep() {
        java.util.List<String> selection = new ArrayList<>();
        boolean companionsSelected = false;
        for (JToggleButton a : buttons) {
            PackageButtonInfo button = (PackageButtonInfo) a.getClientProperty("buttonInfo");
            if (a.isSelected()) {
                if (button.app.getId().equals("<companions>")) {
                    companionsSelected = true;
                }
                selection.add(button.name);
            }
        }
        if (!selection.isEmpty()) {
            if (companionsSelected) {
                if (selection.size() == 1) {
                    jep.setText("<html><body><p>You have selected to install 1 package : </p> <ul><li>Companions</li></ul></body></html>");
                } else {
                    jep.setText("<html><body><p>You have selected to install " + selection.size() + " packages : </p> <ul>" +
                            selection.stream().map(x -> "<li>" + x + "</li>").collect(Collectors.joining("\n"))
                            + "</ul></body></html>");
                }
            } else {
                jep.setText("<html><body><p>You have selected to install " + selection.size() + " packages : </p> <ul>" +
                        selection.stream().map(x -> "<li>" + x + "</li>").collect(Collectors.joining("\n"))
                        + "</ul>" +
                        "<p>We recommend you install also nuts companions.</p>" +
                        "</body></html>");
            }
        } else {
            jep.setText("<html><body><p>You have not selected any application to be installed with nuts</p> <p>We recommend you install at least nuts companions</p></body></html>");
        }
    }

    private void startWaiting() {
        getInstallerContext().startLoading();
    }

    private void endWaiting() {
        getInstallerContext().stopLoading(getPageIndex());
    }

    protected PackageButtonInfo[] getButtons() {
        java.util.List<PackageButtonInfo> all = new ArrayList<>();
        all.add(new PackageButtonInfo(new App("<companions>"), "Companions", Utils.loadString("package-companions.html", null), false, null, true));
        try {
            PackageButtonInfo[] onlineButtons = getOnlineButtons();
            if (onlineButtons != null) {
                all.addAll(Arrays.asList(onlineButtons));
                return all.toArray(new PackageButtonInfo[0]);
            }
        } catch (Exception e) {
            //
        }
        all.addAll(Arrays.asList(getDefaultButtons()));
        return all.toArray(new PackageButtonInfo[0]);
    }

    protected PackageButtonInfo[] getOnlineButtons() {
        SimpleRecommendationConnector connector = new SimpleRecommendationConnector();
        RequestQueryInfo ri = new RequestQueryInfo();
        RequestQuery q = new RequestQuery();
        ri.setQ(q);
        Map info = null;
        java.util.List<Map> recommendedIds = null;
        try {
            try {
                q.setId("net.thevpc.nuts:nuts#" + InstallData.of(getInstallerContext()).installVersion.api);
                Map d = connector.getRecommendations(ri);
                if(d!=null) {
                    recommendedIds = (java.util.List<Map>) d.get("recommendedIds");
                }
            } catch (Exception ex) {
                //ignore
            }
            if (recommendedIds == null) {
                q.setId("net.thevpc.nuts:nuts#RELEASE");
                Map d = connector.getRecommendations(ri);
                if(d!=null) {
                    recommendedIds = (java.util.List<Map>) d.get("recommendedIds");
                }
            }
        } finally {
            SwingUtilities.invokeLater(() -> {
                getInstallerContext().stopLoading(getPageIndex());
            });
        }
        return recommendedIds.stream().map(x -> {
            try {
                PackageButtonInfo b = new PackageButtonInfo(
                        new App(
                                (String) x.get("id"),
                                (String) x.get("repos")
                        ),
                        (String) x.get("name"),
                        (String) x.get("description"),
                        Boolean.parseBoolean(String.valueOf(x.get("gui"))),
                        (String) x.get("smallIcon"),
                        false
                );
                if (Utils.isBlank(b.name) || Utils.isBlank(b.app.getId())) {
                    b = null;
                }
                return b;
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(PackageButtonInfo[]::new);
    }

    protected PackageButtonInfo[] getDefaultButtons() {
        java.util.List<PackageButtonInfo> all = new ArrayList<>();
        all.add(new PackageButtonInfo(new App("org.jedit:jedit"), "JEdit", "Text Editor", true,
                "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                , false));
        all.add(new PackageButtonInfo(new App("org.jd:jd-gui"), "Java Decompiler", "Java Decompiler", true,
                "http://java-decompiler.github.io/img/Icon_java_64.png"
                , false));
        all.add(new PackageButtonInfo(new App("com.mucommander:mucommander"), "Mu-Commander", "File Explorer", true,
                "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master/com/mucommander/mucommander/0.9.7-1/mucommander-icon.png"
                , false));
//        all.add(new PackageButtonInfo("<companions>", "Pangaea Note", "Note Taking Application", true, null, false));
        return all.toArray(new PackageButtonInfo[0]);
    }

    protected void updateButtons() {
        SwingUtilities.invokeLater(() -> {
            startWaiting();
        });
        buttons.clear();
        try {
            PackageButtonInfo[] u = getButtons();
            if (u != null) {
                for (PackageButtonInfo b : u) {
                    if (b != null) {
                        JToggleButton a = new JToggleButton(b.name);
                        a.putClientProperty("buttonInfo", b);
                        buttons.add(a);
                    }
                }
            }
        } finally {
            SwingUtilities.invokeLater(() -> {
                endWaiting();
            });
        }
        SwingUtilities.invokeLater(() -> {
            removeAllComponents2();
            int row = 0;
            int col = 0;
            GridBagConstraints c = new GridBagConstraints();
            for (JToggleButton a : buttons) {
                PackageButtonInfo button = (PackageButtonInfo) a.getClientProperty("buttonInfo");
                a.setIcon(gelAppUi(button.icon, button.gui));
                a.setToolTipText(button.desc);
                a.setSelected(button.selected);
                a.addItemListener(buttonInfoItemListener);
//                a.addMouseListener(buttonInfoMouseListener);
                a.setMinimumSize(new Dimension(40, 40));
//                a.setPreferredSize(new Dimension(40,40));
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(5, 10, 5, 10);
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = col;
                c.gridy = row;
                panel.add(a, c);
                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
            updateJep();
            panel.invalidate();
            panel.revalidate();
        });
    }

    private void removeAllComponents2() {
        for (Component component : panel.getComponents()) {
            if (component instanceof JToggleButton) {
                ((JToggleButton) component).removeItemListener(buttonInfoItemListener);
//                component.removeMouseListener(buttonInfoMouseListener);
            }
        }
        panel.removeAll();
    }

    ImageIcon gelAppUi(String s, boolean gui) {
        if (s != null && s.length() > 0) {
            try {
                Image image = Toolkit.getDefaultToolkit().getImage(new URL(s));
                MediaTracker mt = new MediaTracker(new JLabel());
                mt.addImage(image, 1);
                try {
                    mt.waitForAll();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                if (width > 0 && height > 0) {
                    return new ImageIcon(UIHelper.getFixedSizeImage(image, 32, 32,false));
                }
            } catch (Exception ex) {
                //
            }
        }
        URL r = NutsInstaller.class.getResource(gui ? "mouse.png" : "keyboard.png");
        Image image = Toolkit.getDefaultToolkit().getImage(r);
        return new ImageIcon(UIHelper.getFixedSizeImage(image, 32, 32,false));
    }

    @Override
    public void onNext() {
        super.onNext();
        InstallData u = InstallData.of(getInstallerContext());
        for (JToggleButton button : buttons) {
            PackageButtonInfo bi = (PackageButtonInfo) button.getClientProperty("buttonInfo");
            if ("<companions>".equals(bi.app.getId())) {
                u.optionk = !button.isSelected();
            } else {
                if(button.isSelected()) {
                    u.recommendedIds.add(bi.app);
                }
            }
        }
    }

    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        new Thread(this::updateButtons).start();
    }

    public class PackageButtonInfo {
        App app;
        String name;
        String desc;
        String icon;
        boolean gui;
        boolean selected;

        public PackageButtonInfo(App app, String name, String desc, boolean gui, String icon, boolean selected) {
            this.app = app;
            this.name = name;
            this.desc = desc;
            this.gui = gui;
            this.icon = icon;
            this.selected = selected;
        }
    }
}
