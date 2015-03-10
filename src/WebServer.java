import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WebServer {
	public static void main(String argv[]) throws Exception {
		// Set the port number.
		int port = 7000;
		// Establish the listen socket.
		ServerSocket sock = new ServerSocket(port);
		System.out.println("Server is running!");
		ExecutorService executorService = Executors.newCachedThreadPool();

		
		
		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			Socket client = sock.accept();
			executorService.execute(new Server(client));
			// Code below here will not execute
		}
		
	}
}
