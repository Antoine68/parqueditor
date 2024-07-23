package dev.arichard.parqueditor.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javafx.fxml.FXMLLoader;

@Service
public class FxmlService {
	
	@Autowired
	private ConfigurableApplicationContext context;
	
	
	public <T> T load(String path) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
        fxmlLoader.setControllerFactory(this.context::getBean);
        return fxmlLoader.load();
	}
	
	
	
}
