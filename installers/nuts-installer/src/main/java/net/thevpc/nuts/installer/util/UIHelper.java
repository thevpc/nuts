package net.thevpc.nuts.installer.util;

import net.thevpc.nuts.installer.NutsInstaller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class UIHelper {
    public static JComponent titleLabel(String str) {
        JLabel jLabel = new JLabel(str);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD, 1.1f * jLabel.getFont().getSize()));
        return margins(jLabel, 10);
    }

    public static JComponent margins(JComponent c, int margin) {
        return margins(c, margin, margin, margin, margin);
    }

    public static JComponent margins(JComponent c, int top, int left, int bottom, int right) {
        Border border = c.getBorder();
        Border margin = new EmptyBorder(top, left, bottom, right);
        c.setBorder(new CompoundBorder(border, margin));
        return c;
    }

    public static Image emptyImage(int w, int h) {
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.dispose();
        return newImage;
    }

    public static Image concatImagesH(Image a, Image b, int margin) {
        waitForImages(a, b);
        int w1 = a.getWidth(null);
        int h1 = a.getHeight(null);
        int w2 = b.getWidth(null);
        int h2 = b.getHeight(null);
        BufferedImage newImage = new BufferedImage(w1 + margin + w2, Math.max(h1, h2), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(a, 0, 0, w1, h1, null);
        g2.drawImage(b, w1, 0, margin + w2, h2, null);
        g2.dispose();
        return newImage;
    }

    public static Image getCheckedImage(boolean checked) {
        int w=32;
        int h=32;
        if (checked) {
            URL r = NutsInstaller.class.getResource("checked.png");
            return UIHelper.getFixedSizeImage(Toolkit.getDefaultToolkit().getImage(r), w, h, false);
        } else {
            return UIHelper.emptyImage(w,h);
        }
    }

    public static Image getCheckedImage(Image imageOk, boolean checked,int margin) {
        waitForImages(imageOk);
        int w = imageOk.getWidth(null);
        int h = imageOk.getHeight(null);
        if (checked) {
            URL r = NutsInstaller.class.getResource("checked.png");
            Image checkedImage = UIHelper.getFixedSizeImage(Toolkit.getDefaultToolkit().getImage(r), w, h, false);
            return UIHelper.concatImagesH(checkedImage, imageOk, margin);
        } else {
            return UIHelper.concatImagesH(UIHelper.emptyImage(w, h), imageOk, margin);
        }
    }

    public static Image getFixedSizeImage(Image srcImg, int w, int h, boolean preserveRatio) {
        if (w <= 0 && h <= 0) {
            return srcImg;
        }
        waitForImages(srcImg);
        int width = srcImg.getWidth(null);
        int height = srcImg.getHeight(null);
        if (preserveRatio) {
            if (w <= 0) {
                w = h * width / height;
            } else if (h <= 0) {
                h = w * height / width;
            }
        } else {
            if (w >= 0 && h < 0) {
                h = w;
            } else if (h >= 0 && w < 0) {
                w = h;
            }
        }

        if (w <= 0) {
            w = width;
        }
        if (h <= 0) {
            h = height;
        }
        if (width == w && height == h) {
            return srcImg;
        }
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

    public static void waitForImages(Image... images) {
        MediaTracker mt = new MediaTracker(new JLabel());
        for (Image image : images) {
            mt.addImage(image, 1);
        }
        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
