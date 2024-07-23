package dev.arichard.parqueditor.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javafx.fxml.FXMLLoader;

@Service
public class FxmlService {
	
	private final static List<String> SUPPORTED_LOCALES = List.of("en", "fr");
	
	@Autowired
	private ConfigurableApplicationContext context;
	
	private ResourceBundle bundle;
	
	public FxmlService() {
		String userLocale = System.getProperty("user.language");
		if (!SUPPORTED_LOCALES.contains(userLocale)) {
			userLocale = "en";
		}
		bundle = ResourceBundle.getBundle("bundles/Parqueditor", new Locale(userLocale));
	}
	
	public <T> T load(String path) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path), bundle);
        fxmlLoader.setControllerFactory(this.context::getBean);
        return fxmlLoader.load();
	}
	
	
	
}
