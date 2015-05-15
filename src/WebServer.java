/* ==============================================================================
 *
 * Filename: WebServer.java
 *
 * Synopsis: A fairly straightforward class which exists to initialise the
 * 			web server used by the main server process. It creates a fixed thread
 * 			pool which allows for concurrent requests to be processed.
 *
 * GitHub Repository: https://github.com/nightpaws/ACE4
 *
 * Author: Craig Morrison, Reg no: 201247913
 *
 *
 * Promise: I confirm that this submission is all my own work.
 *
 * (Craig Morrison) __________________________________________
 *
 * Version: Full version history can be found on GitHub.
 *
 * =============================================================================*/
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WebServer {
	/**
	 * Web Server Initialiser, which creates a fixed Size thread pool. A fixed
	 * number of threads are created for use by the specified web server.
	 * 
	 * @param argv
	 *            unused
	 * @throws Exception
	 */
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
