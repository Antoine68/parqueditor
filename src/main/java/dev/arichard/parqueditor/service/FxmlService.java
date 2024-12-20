package dev.arichard.parqueditor.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@Service
public class FxmlService {

    private static final List<String> SUPPORTED_LOCALES = List.of("en", "fr");

    @Autowired
    private ConfigurableApplicationContext context;

    private final ResourceBundle bundle;

    public FxmlService() {
        String userLocale = System.getProperty("user.language");
        if (!SUPPORTED_LOCALES.contains(userLocale)) {
            userLocale = "en";
        }
        bundle = ResourceBundle.getBundle("bundles/Parqueditor", new Locale(userLocale));
    }
    
    public <T> T load(String path, Object control) throws IOException {
        FXMLLoader fxmlLoader = createLoader(path);
        fxmlLoader.setRoot(control);
        fxmlLoader.setController(control);
        return fxmlLoader.load();
    }
    
    public <T> T safeLoad(String path, Object control) {
        try {
            return load(path, control);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error during " + path + " control loading", e.getMessage());
        }
        return null;
    }

    public <T> T load(String path) throws IOException {
        FXMLLoader fxmlLoader = createLoader(path);
        fxmlLoader.setControllerFactory(this.context::getBean);
        return fxmlLoader.load();
    }
    
    public void showAlert(AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
    
    private FXMLLoader createLoader(String path) {
        return new FXMLLoader(getClass().getResource(path), bundle);
    }

}
