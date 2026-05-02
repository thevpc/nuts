package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.model.App;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.NutsInstaller;
import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.swing.*;
import net.thevpc.nuts.installer.util.UiHelper2;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PackagesPanel extends WizardPageBase {
    private final ItemListener buttonInfoItemListener;
    //    private final MouseAdapter buttonInfoMouseListener;
    private final JScrollPane jsp;
    JPanel panel;
    JComponent panelScroll;
    JEditorPane jep;
    int w = 160;
    java.util.List<ButtonComponent> buttons = new ArrayList<>();
    volatile boolean buttonsLoaded = false;
    int maxCols=3;
    private static class ButtonComponent {
        JToggleButton button;
        PackageButtonInfo bi;
        String title;
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
        int col=0;
        int row=0;
        for (ButtonComponent a : buttons) {
            PackageButtonInfo button = a.bi;

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(6, 6, 6, 6);
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = col;
            c.gridy = row;
            panel.add(a.button, c);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
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
        try {
            PackageButtonInfo[] onlineButtons = getOnlineButtons2();
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

    @Override
    public void sendAction(String[] action) {
        switch (action[0]) {
            case "companions": {
                SwingUtilities.invokeLater(() -> {
                    for (ButtonComponent a : buttons) {
                        PackageButtonInfo button = a.bi;
                        if (button.app.getId().equals("<companions>")) {
                            a.button.setSelected(true);
                        }
                    }
                });
                break;
            }
            case "select": {
                SwingUtilities.invokeLater(() -> {
                    if(buttons.isEmpty()){
                        return;
                    }
                    String a="";
                    if(action.length>1){
                        a=action[1];
                    }
                    if(a.equals("rand") || a.equals("random")) {
                        a = ""+new Random().nextInt(buttons.size());
                    }
                    if(a.matches("[0-9]+")){
                        int x=Integer.parseInt(a);
                        buttons.get(x%buttons.size()).button.setSelected(true);
                    }
                });
                break;
            }
            case "unselect":
            case "deselect":
            {
                SwingUtilities.invokeLater(() -> {
                    if(buttons.isEmpty()){
                        return;
                    }
                    String a="";
                    if(action.length>1){
                        a=action[1];
                    }
                    if(a.equals("rand") || a.equals("random")) {
                        a = ""+new Random().nextInt(buttons.size());
                    }
                    if(a.matches("[0-9]+")){
                        int x=Integer.parseInt(a);
                        buttons.get(x%buttons.size()).button.setSelected(false);
                    }
                });
                break;
            }
            case "change":
            {
                SwingUtilities.invokeLater(() -> {
                    if(buttons.isEmpty()){
                        return;
                    }
                    String a="";
                    if(action.length>1){
                        a=action[1];
                    }
                    if(a.equals("rand") || a.equals("random")) {
                        a = ""+new Random().nextInt(buttons.size());
                    }
                    if(a.matches("[0-9]+")){
                        int x=Integer.parseInt(a);
                        ButtonComponent b = buttons.get(x % buttons.size());
                        b.button.setSelected(!b.button.isSelected());
                    }
                });

                break;
            }
            case "wait-loading":{
                getInstallerContext().waitLoading();
                while(!buttonsLoaded){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                break;
            }
        }
    }

    protected PackageButtonInfo[] getOnlineButtons2() {
        List<PackageButtonInfo> all = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL("https://thevpc.github.io/nuts/RECOMMENDATIONS").openStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (
                            line.startsWith(";")
                                    || line.startsWith("-")
                                    || line.startsWith("#")
                    ) {
                        //just ignore
                    } else {
                        String[] split = line.split(",");
                        if (split.length >= 5) {
                            all.add(new PackageButtonInfo(
                                    new App(split[0]),
                                    split[1],
                                    split[2],
                                    "gui".equalsIgnoreCase(split[3]) || "true".equals(split[3]),
                                    split[4],
                                    false
                            ));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (!all.isEmpty()) {
                return all.toArray(new PackageButtonInfo[0]);
            }
            throw new IllegalArgumentException(ex);
        }
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
                        JToggleButton a = createPackageCard(b);
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
//            if(buttons.size()>4){
//                maxCols = 3;
//            }
            GridBagConstraints c = new GridBagConstraints();
            for (ButtonComponent a : buttons) {
                PackageButtonInfo button = a.bi;
                a.button.setIcon(button.imageIcon);
                a.button.setSelectedIcon(button.selectedImageIcon);
                a.button.setToolTipText(button.desc);
                //a.button.setSelected(button.selected);
                a.button.addItemListener(buttonInfoItemListener);
//                a.addMouseListener(buttonInfoMouseListener);
                a.button.setMinimumSize(new Dimension(20, 20));
//                a.setPreferredSize(new Dimension(40,40));
                c.fill = GridBagConstraints.BOTH;
//                c.insets = new Insets(5, 10, 5, 10);
                c.insets = new Insets(2, 2, 2, 2);
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = col;
                c.gridy = row;
                panel.add(a.button, c);
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
            updateJep();
            panel.invalidate();
            panel.revalidate();
            this.buttonsLoaded=true;
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
        return new ImageIcon(UiHelper2.getCheckedImage(imageOk, checked, 12));
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


    private JToggleButton createPackageCard(PackageButtonInfo b) {
        JToggleButton card = new JToggleButton() {
            @Override
            public void updateUI() {
            }


            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 110);
            }
        };
        card.setRolloverEnabled(true);
        card.setUI(new BasicToggleButtonUI() {

            @Override
            public void update(Graphics g, JComponent c) {
                paint(g, c);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                boolean darkMode = InstallData.of(getInstallerContext()).isDarkMode();
                JToggleButton btn = (JToggleButton) c;
                boolean selected = btn.isSelected();
                boolean hover = btn.getModel().isRollover();
//
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();

                // background
                if (selected) {
                    g2.setColor(darkMode ? new Color(0x1A, 0x4A, 0x80) : new Color(0xA8, 0xCB, 0xF0));
                } else if (hover) {
                    g2.setColor(darkMode ? new Color(0x2E, 0x4A, 0x68) : new Color(0xD0, 0xDC, 0xF0));
                } else {
                    g2.setColor(darkMode ? new Color(0x55, 0x5A, 0x5D) : new Color(0xE8, 0xE8, 0xE8));
                }
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                // icon background pill
                int iconSize = 48;
                int iconX = (w - iconSize) / 2;
                int iconY = 24; // was 16, now lower

                if (selected) {
                    g2.setColor(new Color(255, 255, 255, 80));
                } else {
                    g2.setColor(darkMode ? new Color(0x42, 0x45, 0x47) : new Color(0xD0, 0xD0, 0xD0));
                }
                g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);

                // icon
                Icon icon = btn.getIcon();
                if (icon != null) {
                    int iw = icon.getIconWidth();
                    int ih = icon.getIconHeight();
                    icon.paintIcon(c, g2, iconX + (iconSize - iw) / 2, iconY + (iconSize - ih) / 2);
                }

                // label
                g2.setFont(btn.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN).deriveFont(11f));
                g2.setColor(selected
                        ? (darkMode ? new Color(0xC0, 0xD8, 0xF0) : new Color(0x0D, 0x3D, 0x7A))
                        : (darkMode ? new Color(0xCC, 0xCC, 0xCC) : new Color(0x44, 0x44, 0x44)));
                FontMetrics fm = g2.getFontMetrics();
                String name = b.name;
                // wrap long names
                if (fm.stringWidth(name) > w - 12) {
                    name = name.substring(0, name.length() - 3);
                    while (fm.stringWidth(name + "...") > w - 12 && name.length() > 0) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name = name + "...";
                }
                int tx = (w - fm.stringWidth(name)) / 2;
                int ty = iconY + iconSize + 10 + fm.getAscent();
                g2.drawString(name, tx, ty);

                // checkmark badge (top-right) when selected
                if (selected) {
                    int r = 8; // was 0, fixed
                    int cx = w - r - 8;
                    int cy = r + 8;
                    g2.setColor(darkMode ? new Color(0x1E, 0x6F, 0xB5) : new Color(0x15, 0x65, 0xC0));
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int[] px = {cx - 4, cx - 1, cx + 4};
                    int[] py = {cy, cy + 3, cy - 4};
                    g2.drawPolyline(px, py, 3);
                }

                g2.dispose();
            }
        });
        card.setSelected(b.selected);
        card.putClientProperty("buttonInfo", b);
        card.addItemListener(buttonInfoItemListener);
        return card;
    }
}
