package net.thevpc.nuts.toolbox.nstore.service;

import javafx.scene.image.Image;
import net.thevpc.nuts.toolbox.nstore.model.PackageInfo;

import java.util.Arrays;
import java.util.stream.Stream;

public class NutsService {
    public Stream<PackageInfo> findAll(){
        return Arrays.stream(
                new PackageInfo[]{
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                        new PackageInfo("name","genericName","description",5,10,"here",new Image(
                                "https://raw.githubusercontent.com/thevpc/nuts-public/master/org/jedit/jedit/5.6.0/jedit-icon48.png"
                        ), false, false, false, false, false),
                }
        );
    }
}
