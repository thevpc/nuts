module net.thevpc.nuts.toolbox.nstore {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires net.thevpc.nuts;
    exports net.thevpc.nuts.toolbox.nstore;
    exports net.thevpc.nuts.toolbox.nstore.ctrl;
    opens net.thevpc.nuts.toolbox.nstore to javafx.fxml;
    opens net.thevpc.nuts.toolbox.nstore.ctrl to javafx.fxml;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome;
    requires java.logging;
}