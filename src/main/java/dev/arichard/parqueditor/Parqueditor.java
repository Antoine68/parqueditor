package dev.arichard.parqueditor;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import dev.arichard.parqueditor.service.FxmlService;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class Parqueditor extends Application {
	
	private ConfigurableApplicationContext context;
	
	public static void callLaunch(String[] args) {
		launch(args);
	}
	
    @Override
    public void init() throws Exception {
    	SpringApplication application = new SpringApplication(Parqueditor.class);
    	application.setWebApplicationType(WebApplicationType.NONE);
        this.context = application.run();
    }
	
	@Override
    public void start(Stage primaryStage) throws IOException {
		FxmlService fxmlService = this.context.getBean(FxmlService.class);
		Parent root = fxmlService.load("/fxml/main.fxml");
        Scene scene = new Scene(root);
        primaryStage.setTitle("Parqueditor");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

}
