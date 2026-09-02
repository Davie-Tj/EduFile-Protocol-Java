package tj.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientHandler{
	
	private Socket s = null;
	private InputStream is  = null;
	private OutputStream os = null;
	private BufferedReader br = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private PrintWriter pw = null;
	
	public ClientHandler(Socket connection) {
		s = connection;
		try {
			is = s.getInputStream();
			os = s.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			dos = new DataOutputStream(os);
			dis = new DataInputStream(is);
			pw = new PrintWriter(os,true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to send the LOGIN Command
	 * @param studentNum student number
	 * @param passWd password
	 * @return response message from the server
	 */
	public String login(String studentNum, String passWd) {
		
		try {
			pw.println("LOGIN" + " " + studentNum + " " + passWd);
			String response = br.readLine();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return "FAILED TO LOGIN";
		}
	}
	
	/**
	 * Method to request documents list from the server
	 * @return String of documents available on the server
	 */
	private String pull() {
		String list = "";
		pw.println("LIST");
		try {
			String line;
			while((line = br.readLine()) != null && !line.equals("END")) {
				list = list.concat(line + "\n");
			}
			System.out.println("List recieved: \n" + list);
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return "FAILED TO GET LIST";
		}
	}
	
	public static String pullStatic() {
		try(Socket s = new Socket("localhost",3030)){
			ClientHandler tempHandler = new ClientHandler(s);
			return tempHandler.pull();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "FAILED TO GET LIST";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "FAILED TO GET LIST";
		}
	}
	
	private File get(String ID) {
		pw.println("GET" + " " + ID);
		String fileName;
		try {
			fileName = br.readLine();
			if(fileName.equals("INVALID_ID")) {
				return null;
			}
			//read file size
			long fileSize = dis.readLong();
			//prepare for the file
			File outputFile = new File("data/client/" + fileName);
			FileOutputStream fos = new FileOutputStream(outputFile);
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			long totalBytesRead = 0;
			while (totalBytesRead < fileSize) {
				bytesRead = dis.read(buffer);
				if(bytesRead < 0) break; //end of stream
				fos.write(buffer,0,bytesRead);
				totalBytesRead += bytesRead;
			}
			fos.close();
			return outputFile;
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method to download the requested document by creating a new connection to avoid corrupting stream
	 * @param id document ID to be downloaded
	 * @return document matching the provided id
	 */
	public static File getFileStatic(String id) {
		try(Socket s = new Socket("localhost",3030)){
			ClientHandler tempHandler = new ClientHandler(s);
			return tempHandler.get(id);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String uploadFile(int id,String fileName, long fileSize,File file) {
		try{
			pw.println("UPLOAD" + " " + id + " " + fileName + " " + fileSize);
			dos.writeLong(fileSize);
			dos.flush();
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while((bytesRead = fis.read(buffer)) > 0) {
				dos.write(buffer,0,bytesRead);
				dos.flush();
			}
			fis.close();
			return "SUCCESS";
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "FAILURE";
		} catch (IOException e) {
			e.printStackTrace();
			return "FAILURE";
		}
	}
	
	/**
	 * Method to upload a file to the server
	 * @param id file ID
	 * @param fileName file name
	 * @param fileSize file size
	 * @param file the actual file to be uploaded
	 * @return SUCCESS if the file is uploaded successfully FAILURE otherwise
	 */
	public static String uploadStatic(int id,String fileName, long fileSize,File file) {
		try (Socket s = new Socket("localhost",3030)){
			ClientHandler tempHandler = new ClientHandler(s);
			return tempHandler.uploadFile(id, fileName, fileSize, file);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "FAILURE";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "FAILURE";
		}
	}
	
	
	private void close() {
		try {
			if(s != null) s.close();
			if(is  != null) is.close();
			if (os != null) os.close();
			if (br != null) br.close();
			if (dos != null) dos.close();
			if (dis != null) dis.close();
			if (pw != null) pw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		 
	}
	
	/**
	 * Method to sent the logout command when the client wishes to log out
	 */
	public void logout() {
		pw.println("LOGOUT");
		close();
	}
	
	
}
