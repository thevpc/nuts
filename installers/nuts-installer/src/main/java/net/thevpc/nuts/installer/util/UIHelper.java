package net.thevpc.nuts.installer.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UIHelper {
    public static JComponent titleLabel(String str){
        JLabel jLabel = new JLabel(str);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD,1.1f*jLabel.getFont().getSize()));
        return margins(jLabel,10);
    }
    public static JComponent margins(JComponent c,int margin){
        return margins(c,margin,margin,margin,margin);
    }

    public static JComponent margins(JComponent c,int top, int left, int bottom, int right){
        Border border = c.getBorder();
        Border margin = new EmptyBorder(top,left,bottom,right);
        c.setBorder(new CompoundBorder(border, margin));
        return c;
    }

    public static Image getFixedSizeImage(Image srcImg, int w, int h,boolean preserveRatio) {
        if (w <= 0 && h <= 0) {
            return srcImg;
        }
        MediaTracker mt=new MediaTracker(new JLabel());
        mt.addImage(srcImg,1);
        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int width = srcImg.getWidth(null);
        int height = srcImg.getHeight(null);
        if(preserveRatio){
            if (w <= 0) {
                w = h*width/height;
            }else if(h<=0){
                h = w*height/width;
            }
        }else {
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
}
