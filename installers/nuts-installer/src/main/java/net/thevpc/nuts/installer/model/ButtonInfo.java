package net.thevpc.nuts.installer.model;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ButtonInfo {
    public String text;
    public String html;
    public VerInfo verInfo;
    public StatusButtonColorSet lightColors;
    public StatusButtonColorSet darkColors;
    public ImageIcon iconNonSelected;
    public ImageIcon iconSelected;
    public boolean darkMode;

    public static ButtonInfo of(JComponent component) {
        return (ButtonInfo) component.getClientProperty("ButtonInfo");
    }

    public ButtonInfo(String text, String html, StatusButtonColorSet lightColors, StatusButtonColorSet darkColors, ImageIcon iconNonSelected, ImageIcon iconSelected) {
        this.text = text;
        this.html = html;
        this.lightColors = lightColors;
        this.darkColors = darkColors;
        this.iconNonSelected = iconNonSelected;
        this.iconSelected = iconSelected;
    }

    public JToggleButton createAndBind() {
        JToggleButton button = new JToggleButton(text) {
            @Override
            public void updateUI() {
                // do nothing — keep our custom UI
            }
        };
        button.setRolloverEnabled(true);
        button.setUI(new BasicToggleButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JToggleButton btn = (JToggleButton) c;
                ButtonColorSet colors = resolveColors(btn);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // background
                g2.setColor(colors.background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                g2.setColor(colors.background.darker());
                // icon
                Icon icon = btn.getIcon();
                int textX = 0;
                if (icon != null) {
                    int ix = 16;
                    int iy = (c.getHeight() - icon.getIconHeight()) / 2;
                    icon.paintIcon(c, g2, ix, iy);
                    textX = ix + icon.getIconWidth() + 8;
                }

                // text
                g2.setColor(colors.foreground);
                g2.setFont(btn.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int ty = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (icon == null) {
                    textX = (c.getWidth() - fm.stringWidth(btn.getText())) / 2;
                }
                g2.drawString(btn.getText(), textX, ty);
//                if(btn.isSelected()) {
//                    Stroke stroke = g2.getStroke();
//                    g2.setStroke(new BasicStroke(2.0f));
//                    g2.setColor(colors.background.darker());
//                    g2.drawRoundRect(4, 4, c.getWidth() - 8, c.getHeight() - 8, 10, 10);
//                    g2.setStroke(stroke);
//                }

                g2.dispose();
            }

            @Override
            public void update(Graphics g, JComponent c) {
                paint(g, c); // skip all BasicButtonUI background logic
            }
        });
        bind(button);
        return button;
    }


    public ButtonColorSet resolveColors(JToggleButton src) {
        StatusButtonColorSet theme = darkMode ? darkColors : lightColors;
        boolean hover = src.getModel().isRollover();
        return hover ? theme.hover
                : src.isSelected() ? theme.selected
                  : theme.normal;
    }

    public void bind(JToggleButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.putClientProperty("ButtonInfo", this);
        applyButtonInfo(button);
//        attachHoverListener(button);
    }

//    private void attachHoverListener(JToggleButton button) {
//        button.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                applyButtonInfo(button, true);
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                applyButtonInfo(button, false);
//            }
//        });
//    }

    public void applyButtonInfo(JToggleButton src) {
        // Force a repaint of the current hover state (if mouse is over)
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseLoc, src);
        boolean hover = src.contains(mouseLoc);
        applyButtonInfo(src,hover);
    }

    public void applyButtonInfo(JToggleButton src, boolean hover) {
        boolean darkMode = isDarkMode();
        boolean selected = src.isSelected();
        StatusButtonColorSet theme = darkMode ? darkColors : lightColors;
        ButtonColorSet colors =
                hover ? theme.hover :
                        selected ? theme.selected : theme.normal;

        src.setBackground(colors.background);
        src.setForeground(colors.foreground);
        src.setText(this.text);

        // Icon and font based on selected state
        if (selected) {
            src.setIcon(iconSelected);
            src.setFont(src.getFont().deriveFont(Font.BOLD));
        } else {
            src.setIcon(null);
            src.setFont(src.getFont().deriveFont(Font.PLAIN));
        }
    }


    public boolean isDarkMode() {
        return darkMode;
    }

    public ButtonInfo setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        return this;
    }
}
