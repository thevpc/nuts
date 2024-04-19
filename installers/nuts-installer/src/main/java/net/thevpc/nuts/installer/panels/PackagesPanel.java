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
    java.util.List<ButtonComponent> buttons = new ArrayList<>();

    private static class ButtonComponent {
        JToggleButton button;
        PackageButtonInfo bi;
        String title;
        JLabel label;
    }

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
        for (ButtonComponent a : buttons) {
            PackageButtonInfo button = a.bi;
            if (a.button.isSelected()) {
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
        getInstallerContext().getNextButton().setEnabled(false);
        getInstallerContext().getPreviousButton().setEnabled(false);
    }

    private void endWaiting() {
        getInstallerContext().stopLoading(getPageIndex());
        getInstallerContext().getNextButton().setEnabled(true);
        getInstallerContext().getPreviousButton().setEnabled(true);
        panel.invalidate();
        panel.revalidate();
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
                q.setId("net.thevpc.nuts:nuts#" + InstallData.of(getInstallerContext()).getInstallVersion().api);
                Map d = connector.getRecommendations(ri);
                if (d != null) {
                    recommendedIds = (java.util.List<Map>) d.get("recommendedIds");
                }
            } catch (Exception ex) {
                //ignore
            }
            if (recommendedIds == null) {
                q.setId("net.thevpc.nuts:nuts#RELEASE");
                Map d = connector.getRecommendations(ri);
                if (d != null) {
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
                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                , false));
        all.add(new PackageButtonInfo(new App("org.jd:jd-gui"), "Java Decompiler", "Java Decompiler", true,
                "http://java-decompiler.github.io/img/Icon_java_64.png"
                , false));
        all.add(new PackageButtonInfo(new App("com.mucommander:mucommander"), "Mu-Commander", "File Explorer", true,
                "https://raw.githubusercontent.com/thevpc/nuts-public/master/com/mucommander/mucommander/1.1.0-1/mucommander-icon.png"
                , false));
        all.add(new PackageButtonInfo(new App("net.thevpc.pnote:pnote"), "Pangaea Note", "Note Taking Application", true,
                "https://raw.githubusercontent.com/thevpc/pangaea-note/master/docs/img/icon.png"
                , false));
        all.add(new PackageButtonInfo(new App("net.thevpc.netbeans-launcher:netbeans-launcher"), "Netbeans Launcher", "Netbeans IDE Launcher", true,
                "https://raw.githubusercontent.com/thevpc/netbeans-launcher/master/docs/img/icon.png"
                , false));
        all.add(new PackageButtonInfo(new App("net.thevpc.kifkif:kifkif"), "Kifkif", "Files and Folders Duplicate Finder", true,
                "https://raw.githubusercontent.com/thevpc/kifkif/master/docs/img/icon.png"
                , false));
        all.add(new PackageButtonInfo(new App("io.github.jiashunx:masker-flappybird"), "Flappy Bird", "Flappy Bird Game", true,
                "https://upload.wikimedia.org/wikipedia/en/0/0a/Flappy_Bird_icon.png"
                , false));
        all.add(new PackageButtonInfo(new App("org.jmeld:jmeld"), "JMeld", "A visual diff and merge tool", true,
                "https://raw.githubusercontent.com/albfan/jmeld/master/res/jmeld-component.png"
                , false));
        all.add(new PackageButtonInfo(new App("com.jgoodies:jdiskreport"), "JDisk Report", "A visual Disk Analyzer", true,
                "https://www.jgoodies.com/wp-content/uploads/2012/04/o1960.jpg"
                , false));
        all.add(new PackageButtonInfo(new App("org.jd:jd-gui"), "Java Decompiler", "A visual Java Decompiler", true,
                "https://raw.githubusercontent.com/java-decompiler/jd-gui/master/app/src/main/resources/org/jd/gui/images/jd_icon_128.png"
                , false));
        all.add(new PackageButtonInfo(new App("jpass:jpass"), "JPass", "Password Manager", true,
                "https://raw.githubusercontent.com/gaborbata/jpass/master/resources/bannerReadMe.png"
                , false));
        all.add(new PackageButtonInfo(new App("org.omegat:omegat"), "OmegaT", "The free translation memory tool", true,
                "https://lingenio.de/wp-content/uploads/2016/08/2016-CAT-tool-CAT-tools-OmegaT.jpg"
                , false));
        all.add(new PackageButtonInfo(new App("eu.binjr:binjr-core"), "Bonjour", "Time Series Dashboard", true,
                "https://binjr.eu/assets/images/binjr_title.png"
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
                        JToggleButton a = new JToggleButton();
                        b.imageIcon = gelAppUi(b.icon, b.gui, false);
                        b.selectedImageIcon = gelAppUi(b.icon, b.gui, true);
                        a.putClientProperty("buttonInfo", b);
                        ButtonComponent bc = new ButtonComponent();
                        bc.button = a;
                        bc.bi = b;
                        bc.title = b.name;
                        buttons.add(bc);
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
            int maxCols = 3;
//            if(buttons.size()>4){
//                maxCols = 3;
//            }
            GridBagConstraints c = new GridBagConstraints();
            for (ButtonComponent a : buttons) {
                PackageButtonInfo button = a.bi;
                a.button.setIcon(button.imageIcon);
                a.button.setSelectedIcon(button.selectedImageIcon);
                a.button.setToolTipText(button.desc);
                a.button.setSelected(button.selected);
                a.button.addItemListener(buttonInfoItemListener);
//                a.addMouseListener(buttonInfoMouseListener);
                a.button.setMinimumSize(new Dimension(20, 20));
//                a.setPreferredSize(new Dimension(40,40));
                c.fill = GridBagConstraints.BOTH;
//                c.insets = new Insets(5, 10, 5, 10);
                c.insets = new Insets(2, 2, 2, 2);
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 0;
                c.weighty = 1;
                c.gridx = col;
                c.gridy = row;
                panel.add(a.button, c);
                col++;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = col;
                c.gridy = row;
                a.label=new JLabel(a.title);
                a.label.setFont(new Font("arial", Font.PLAIN, 10));
                a.label.setToolTipText(button.desc);
                panel.add(a.label, c);


                col++;
                if (col >= maxCols * 2) {
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

    ImageIcon gelAppUi(String s, boolean gui, boolean checked) {
        Image imageOk = null;
        if (s != null && s.length() > 0) {
            try {
                Image image = Toolkit.getDefaultToolkit().getImage(new URL(s));
                UIHelper.waitForImages(image);
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                if (width > 0 && height > 0) {
                    imageOk = image;
                }
            } catch (Exception ex) {
                //
            }
        }
        if (imageOk == null) {
            URL r = NutsInstaller.class.getResource(gui ? "mouse.png" : "keyboard.png");
            imageOk = Toolkit.getDefaultToolkit().getImage(r);
        }
        imageOk = UIHelper.getFixedSizeImage(imageOk, 32, 32, false);
        return new ImageIcon(UIHelper.getCheckedImage(imageOk, checked, 12));
    }

    @Override
    public void onNext() {
        super.onNext();
        InstallData u = InstallData.of(getInstallerContext());
        for (ButtonComponent button : buttons) {
            PackageButtonInfo bi = button.bi;
            if ("<companions>".equals(bi.app.getId())) {
                u.optionk = !button.button.isSelected();
            } else {
                if (button.button.isSelected()) {
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
        ImageIcon imageIcon;
        ImageIcon selectedImageIcon;

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
