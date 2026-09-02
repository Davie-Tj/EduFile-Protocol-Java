package tj.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class EduServerHandler implements Runnable{
	
	private Socket connection = null;
	private InputStream is = null;
	private OutputStream os = null;
	private PrintWriter pw = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private BufferedReader br = null;
	
	
	public EduServerHandler(Socket s) {
		connection = s;
		try {
			is = connection.getInputStream();
			os = connection.getOutputStream();
			pw = new PrintWriter(os,true);
			dos = new DataOutputStream(os);
			dis = new DataInputStream(is);
			br = new BufferedReader(new InputStreamReader(is));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		System.out.println("handling Client Requests...");
		
		//receive and handle incoming requests
		try {
			String requestLine = br.readLine();
			System.out.println(requestLine);
			//break the request into tokens
			StringTokenizer tokens = new StringTokenizer(requestLine);
			//extract the request command
			String command = tokens.nextToken();
			
			switch (command){
			case "LOGIN":
			{
				//validate the user
				String userName = tokens.nextToken();
				String passWord = tokens.nextToken();
				if(!authonticate(userName, passWord)) {
					System.out.println("Invalid login details");
					pw.println("INVALID_CREDENTIALS");
					return;
				}else {
					System.out.println("Login succeful");
					pw.println("SUCCESSFULLY_LOGED_IN");
				}
				break;
			}
			case "LIST":
			{
				ArrayList<String> docList = getDocList();
				String list = returnList(docList);
				pw.println(list);
				break;
				
			}
			case "GET":
			{	
				//returning requested file to the client
				String id = tokens.nextToken();
				String fileName = idToFileNmae(id);
				if(fileName != null) {
					pw.println(fileName);
					sendFile(fileName);
					//send success message
					pw.println("SUCCESS");
					
				}else {
					System.out.println("Invalid ID: no document with ID " + id);
					pw.println("INVALID_ID");
				}
				break;
			}
			case "UPLOAD":
			{
				//Receiving a file from client
				String id = tokens.nextToken();
				String fileName = tokens.nextToken();
				long fileSize = Long.parseLong(tokens.nextToken());
				//receive file 
				receiveFile(fileName);
				//update the document list
				updateDocFile(id,fileName);
				break;
			}
			case "LOGOUT":
			{
				logOut();
				break;
			}
			default:
			{
				System.out.println("Invalid request Command: " + command);
				break;
			}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//utility function to authenticate the user
	private boolean authonticate(String userName, String pass) {
		boolean found = false;
		File usersFile = new File("data/server/Users.txt");
		try {
			Scanner sc = new Scanner(usersFile);
			while(sc.hasNext() && !found) {
				String line = sc.nextLine();
				String[] parts = line.split("\\s");
				String UName = parts[0];
				String Upass = parts[1];
				if(UName.equals(userName) && Upass.equals(Upass)) {
					found = true;
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return found;
	}
	
	//utility function to get file list
	private ArrayList<String> getDocList(){
		ArrayList<String> list = new ArrayList<>();
		File docFile = new File("data/server/docs.txt");
		try {
			Scanner sc = new Scanner(docFile);
			while(sc.hasNext()) {
				String doc = sc.nextLine();
				list.add(doc);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	//utility function to convert array list to string 
	private String returnList(ArrayList<String> arrList) {
		String list = "";
		for(String line : arrList) {
			//String[] parts = line.split("\\s");
			//String docName = parts[1];
			list = list.concat(line + "\n");
		}
		list = list.concat("END");
		return list;
	}
	
	//utility function to get image Name from ID
	private String idToFileNmae(String ID) {
		String fileName = null;
		File docsFile = new File("data/server/docs.txt");
		try {
			Scanner sc = new Scanner(docsFile);
			while(sc.hasNext() && fileName == null) {
				String line = sc.nextLine();
				String[] parts = line.split("\\s");
				String strID = parts[0];
				String strFileName = parts[1];
				if(strID.equals(ID)) {
					fileName = strFileName;
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fileName;
		
	}
	
	//utility function to send the requested file
	private void sendFile(String fileName) {
		File doc = new File("data/server/" + fileName);
		
		long fileSize = doc.length();
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		
		try {
			//send file size
			dos.writeLong(fileSize);
			dos.flush();
			FileInputStream fis = new FileInputStream(doc);
			//send the actual file bytes
			while((bytesRead = fis.read(buffer)) > 0) {
				dos.write(buffer,0,bytesRead);
				dos.flush();
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//utility function to receive file from client
	private void receiveFile(String fileName) {
		//prepare for file to be received
		File outputFile = new File("data/server/" + fileName);
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		long totalBytesRead = 0;
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			//receive file size
			long fileSize = dis.readLong();
			//Receive actual file bytes
			while(totalBytesRead < fileSize) {
				bytesRead = dis.read(buffer);
				if(bytesRead < 0) break; //end of stream
				fos.write(buffer,0,bytesRead);
				totalBytesRead += bytesRead;
			}
			fos.close();
			pw.println("SUCCESS");
		} catch (IOException e) {
			pw.println("FAILURE");
			e.printStackTrace();
		}
	}
	
	//utility function to update document list
	private void updateDocFile(String ID, String fileName) {
		try(PrintWriter pw = new PrintWriter(new FileWriter("data/server/docs.txt",true))){ //use file writer for append mode
			pw.println(ID + " " + fileName);
		} catch (IOException e) {
			System.err.println("Failed to append to file");
			e.printStackTrace();
		}
	}
	
	//utility function to close connections
	private void logOut() {
		try {
			if (connection != null) connection.close();
			if (is != null) is.close();
			if (os != null) os.close();
			if (pw != null) pw.close();
			if (dos != null) dos.close();
			if (dis != null) dis.close();
			if (br != null) br.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
