package net.thevpc.nuts.installer.util;

import net.thevpc.nuts.installer.NutsInstaller;
import net.thevpc.nuts.nswing.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class UiHelper2 {
    public static Image getStopImage(boolean enabled) {
        int w=32;
        int h=32;
        if (enabled) {
            URL r = NutsInstaller.class.getResource("stop.png");
            return UIHelper.getFixedSizeImage(Toolkit.getDefaultToolkit().getImage(r), w, h, false);
        } else {
            return UIHelper.emptyImage(w,h);
        }
    }

    public static Image getCheckedImage(Image imageOk, boolean checked,int margin) {
        UIHelper.waitForImages(imageOk);
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

    public static ImageIcon getCheckedImageIcon(boolean checked) {
        return new ImageIcon(getCheckedImage(checked));
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
    public static ImageIcon getStopImageIcon(boolean enabled) {
        return new ImageIcon(getStopImage(enabled));
    }


}
