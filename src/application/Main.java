package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {

	private static final String LAYOUT_LOCATION = "/application/EFSLayout.fxml";
	private static final String APPLICATION_TITLE = "Encryption File System";
	private static final boolean IS_RESIZABLE = false;

	@Override
	public void start(Stage primaryStage) {

		try {
			Parent root = FXMLLoader.load(getClass().getResource(LAYOUT_LOCATION));
			primaryStage.setTitle(APPLICATION_TITLE);
			primaryStage.setScene(new Scene(root));
			primaryStage.setResizable(IS_RESIZABLE);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
