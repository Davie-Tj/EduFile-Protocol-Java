package tj.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ClientPane extends GridPane{
	private Socket cSocket = null;
	private ClientHandler cHandler= null;
	private String[] docList;
	private ArrayList<Integer> ids;
	
	
	//GUI components
	private Label studentNumber;
	private TextField studentNumberField;
	private Label lblPassword;
	private TextField passwordField;
	private Button btnLogin;
	private Label lblFileIDToDownload;
	private TextField fileIDToDownload;
	private Button btnDownLoad;
	private Label lblFileIDToDisplay;
	private TextField fileIDToDisplay;
	private Button btnDisplay;
	private Label lblFileList;
	private TextArea fileListArea;
	private Button btnPull;
	private Label lblResposeArea;
	private TextArea responseArea;
	private Button btnUppload;
	private ImageView imgView;
	private Button btnLogOut;
	
	public ClientPane(Stage stage) {
		setUI();
		
		btnLogin.setOnAction(e -> {
			new Thread(() -> {
				try {
					cSocket = new Socket("localhost",3030);
					cHandler = new ClientHandler(cSocket);
					String userName = studentNumberField.getText();
					String passWd = passwordField.getText();
					if (!userName.isEmpty() && !passWd.isEmpty()) {
						String respose = cHandler.login(userName, passWd);
						Platform.runLater(() -> {
							responseArea.appendText(respose + "\n");
						});
						if (respose.equals("SUCCESSFULLY_LOGED_IN")) {
							//enable buttons
							btnPull.setDisable(false);
							btnDownLoad.setDisable(false);
							btnDisplay.setDisable(false);
							btnUppload.setDisable(false);
							btnLogOut.setDisable(false);
							ids = new ArrayList<>();
						}
					}else {
						responseArea.appendText("User name or password cannot be empty\n");
					}
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}).start();
		});
		
		btnPull.setOnAction(e -> {
			btnPull.setDisable(true);
			responseArea.appendText("Requesting document list...\n");
			new Thread(() -> {
				String response = ClientHandler.pullStatic();
				if (response.contains("FAILED")) {
					Platform.runLater(() -> {
						responseArea.appendText(response + "\n");
					});
				}else {
					fileListArea.appendText(response);
					//save the list to memory for future use
					docList = response.split("\n");
					for (String line : docList) {
						String[] parts = line.split(" ");
						int id = Integer.parseInt(parts[0]);
						ids.add(id);
					}
				}	
			}).start();
			btnPull.setDisable(false);
		});
		
		btnDownLoad.setOnAction(e -> {
			btnDownLoad.setDisable(true);
			String id =fileIDToDownload.getText();
			responseArea.appendText("Downloading document with ID " + id + "...\n");
			new Thread(() -> {
				File docFile = ClientHandler.getFileStatic(id);
				if (docFile != null) {
					String docName = docFile.getName();
					Platform.runLater(() -> {
						responseArea.appendText(docName + " downloaded\n");
					});
				}else {
					Platform.runLater(() -> {
						responseArea.appendText("No document with ID " + id + "\n");
					});
				}
			}).start();
			btnDownLoad.setDisable(false);
		});
		
		btnDisplay.setOnAction(e -> {
			String id = fileIDToDisplay.getText();
			String fileName = findFileName(id);
			if (fileName != null && (fileName.endsWith(".png") || fileName.endsWith(".jpeg"))) {
				File imgFile = new File("data/client/" + fileName);
				if(imgFile.exists()) {
					displayImage(imgFile);
				}else {
					responseArea.appendText("Image not found\n");
				}
			}else {
				responseArea.appendText("ERR can only display images\n");
			}
		});
		
		btnUppload.setOnAction(e -> {
			btnUppload.setDisable(true);
			responseArea.appendText("Uploading file to server\n");
			
			FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File("data/client/"));
			File selectedFile = fc.showOpenDialog(stage);
			if (selectedFile == null) {
				responseArea.appendText("No file selected.\n");
				btnUppload.setDisable(false);
				return;
			}
			//get file details
			String fileName = selectedFile.getName();
			long fileSize = selectedFile.length();
			int id = generateID();
			new Thread(() -> {
				if (selectedFile != null) {
					//send the file
					String response = ClientHandler.uploadStatic(id, fileName, fileSize, selectedFile);
					Platform.runLater(() -> {
						responseArea.appendText(response + '\n');
						btnUppload.setDisable(false);
					});
					
				}
			}).start();
			
		});
		
		btnLogOut.setOnAction(e -> {
			//close connections
			logout();
			responseArea.appendText("You logged out\n");
		});
	}
	
	private void setUI() {
		this.setHgap(20);
		this.setVgap(10);
		this.setAlignment(Pos.CENTER);
		
		// GUI Components
		studentNumber = new Label("Enter UserName");
		this.add(studentNumber, 0, 0);
		studentNumberField = new TextField();
		this.add(studentNumberField, 1, 0);
		lblPassword = new Label("Enter Password");
		this.add(lblPassword, 0, 1);
		passwordField = new TextField();
		this.add(passwordField, 1, 1);
		btnLogin = new Button("Login");
		this.add(btnLogin, 2, 1);
		lblFileIDToDownload = new Label("Enter file ID ");
		this.add(lblFileIDToDownload, 0, 2);
		fileIDToDownload = new TextField();
		this.add(fileIDToDownload, 1, 2);
		btnDownLoad = new Button("DownLoad");
		this.add(btnDownLoad, 2, 2);
		btnDownLoad.setDisable(true);
		lblFileIDToDisplay = new Label("Enter file ID");
		this.add(lblFileIDToDisplay, 0, 3);
		fileIDToDisplay = new TextField();
		this.add(fileIDToDisplay, 1,3);
		btnDisplay = new Button("Display");
		this.add(btnDisplay, 2, 3);
		btnDisplay.setDisable(true);
		lblFileList = new Label("File list");
		this.add(lblFileList, 0, 4);
		fileListArea = new TextArea();
		this.add(fileListArea, 1, 4);
		btnPull = new Button("Pull");
		this.add(btnPull, 2, 4);
		btnPull.setDisable(true);
		lblResposeArea = new Label("Response Area");
		this.add(lblResposeArea, 0, 5);
		responseArea = new TextArea();
		this.add(responseArea, 1, 5);
		btnUppload = new Button("Uppload");
		this.add(btnUppload, 2, 6);
		btnUppload.setDisable(true);
		btnLogOut = new Button("Logout");
		this.add(btnLogOut,0,6);
		btnLogOut.setDisable(true);
	}
	
	//utility function to display the image
	private void displayImage(File imgFile) {
		//create image object
		Image image = new Image(imgFile.toURI().toString());
		//Image view to display the image
		imgView = new ImageView(image);
		imgView.setFitWidth(500);
		imgView.setFitHeight(400);
		imgView.setPreserveRatio(true);
		//new window
		Stage imgStage = new Stage();
		imgStage.setTitle("Retrieved Imagae");
		//layout container
		StackPane root = new StackPane(imgView);
		Scene scene = new Scene(root,550,450);
		imgStage.setScene(scene);
		imgStage.show();
	}
	
	//utility function to  find the image
	private String findFileName(String ID) {
		for(String line : docList) {
			String[] parts = line.split(" ");
			if (parts[0].equals(ID)) {
				return parts[1];
			}
		}
		return null;
	}
	
	//utility function to generate random id
	private int generateID() {
		Random rand = new Random();
		int id;
		do {
			id = rand.nextInt(10000000);
		}while(idExisits(id));
		return id;
	}
	
	//utility function to check if id exist
	private boolean idExisits(int id) {
		if(ids.contains(id)) {
			return true;
		}else {
			return false;
		}
	}
	public void logout() {
		if (cHandler != null && cSocket.isConnected()) {
			cHandler.logout();
			btnUppload.setDisable(true);
			btnPull.setDisable(true);
			btnDisplay.setDisable(true);
			btnDownLoad.setDisable(true);
			btnLogOut.setDisable(true);
		}
	}
	
}
