package tj.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EduServer {
	/**
	 * Server Socket
	 */
	private ServerSocket ss = null;
	/**
	 * Client connection
	 */
	private Socket cs = null;
	
	private boolean running = false;
	
	public EduServer(int port) {
		try {
			ss = new ServerSocket(port);
			running = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to start the server
	 */
	public void startServer() {
		System.out.println("Starting server...");
		try {
			System.out.println("Server listening on port " + ss.getLocalPort());
			while(running) {
				//accept incoming connections
				cs = ss.accept();
				//handle each client on a new thread
				new Thread(new EduServerHandler(cs)).start();
			}
		}catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to accept incomming connection");
		}
	}
	
	/**
	 * Method to stop the server
	 */
	public void stopServer() {
		if (ss != null && !ss.isClosed()) {
			try {
				ss.close();
				running = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Failed  to close server");
			}
		}
	}
	
	public static void main(String[] args) {
		EduServer server = new EduServer(3030);
		server.startServer();
	}
}




