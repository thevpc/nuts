package net.thevpc.nuts.toolbox.nstore;


import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.thevpc.nuts.*;

import java.io.IOException;

/**
 * JavaFX App
 */
public class Main extends Application implements NutsApplication {
    private static String[] appArgs;

    public void init() throws Exception {
        NutsApplicationContext ac=NutsApplications.createApplicationContext(this,appArgs,null);
        NutsApplications.runApplication(this,ac);
        NutsSession session=ac.getSession();
        switch (ac.getMode()){
            case INSTALL:
            case UNINSTALL:
            case UPDATE:
            case AUTO_COMPLETE:
            {
                throw new NutsExecutionException(session,NMsg.ofCstyle("exit"),0);
            }
        }
    }

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try {
            root = javafx.fxml.FXMLLoader.load(getClass().getResource("main.fxml"));
            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Nuts Store - 0.8.3.1");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        appArgs=args;
        launch();
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {

    }
}
