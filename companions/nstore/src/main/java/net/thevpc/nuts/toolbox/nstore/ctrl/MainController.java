package net.thevpc.nuts.toolbox.nstore.ctrl;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.*;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController {
    @FXML
    private AnchorPane availablePackagesAnchorPane;

    @FXML
    private Button availablePackagesClearSelection;

    @FXML
    private Button availablePackagesInstall;

    @FXML
    private Button availablePackagesRemove;

    @FXML
    private Button availablePackagesUpdate;

    @FXML
    private TextField searchTextField;

    @FXML
    private GridPane selectedPackagesGridPane;

    @FXML
    private FontIcon showHomeButton;

    @FXML
    private Button showInstalledButton;

    @FXML
    private FontIcon showMarketplaceButton;

    @FXML
    private FontIcon showUpdatableButton;

    @FXML
    protected void initialize(URL location, ResourceBundle resources){

    }
}
