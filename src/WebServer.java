import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WebServer {
	public static void main(String argv[]) throws Exception {
		// Set the port number.
		int port = 7000;
		int poolSize = 10;
		// Establish the listen socket.
		ServerSocket sock = new ServerSocket(port);
		// ExecutorService executorService = Executors.newCachedThreadPool();
		ExecutorService serverThreadPool = Executors
				.newFixedThreadPool(poolSize);
		System.out.println("Server is running!");

		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			Socket client = sock.accept();
			// executorService.execute(new Server(client));
			Thread thread = new Thread(new HttpRequest(client));
			serverThreadPool.execute(thread);
			// Code below here will not execute
		}

	}
}
