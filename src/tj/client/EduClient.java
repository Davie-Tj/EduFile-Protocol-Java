package tj.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EduClient extends Application{
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ClientPane pane = new ClientPane(primaryStage);
		Scene scene = new Scene(pane,800,600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("File Vault");
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(e -> {
			if (pane != null) pane.logout();
		});
	}

}
