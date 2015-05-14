import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

final class HttpRequest implements Runnable {

	final static String CRLF = "\r\n";
	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void processRequest() throws Exception {
		// Get a reference to the socket's input and output streams.
		InputStream is = this.socket.getInputStream();
		DataOutputStream os = new DataOutputStream(
				this.socket.getOutputStream());
		// Set up input stream filters.
		// ?

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();
		// Display the request line.
		System.out.println();
		System.out.println(requestLine);

		// Get and display the header lines.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		// Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();
		String workingDir = System.getProperty("user.dir");
		URL url = new URL(fileName);
		boolean local = false;

		// Required for presence check=============================

		String test = fileName.replace("http://", "");
		test = workingDir + "\\" + test;
		if (test.endsWith("/")) {
			test = test.substring(0, test.length() - 1);
		}
		// System.out.println("TESTURL: " + test);
		File file = new File(test);
		// System.out.println("FILE IS SET TO ==" + file.toString());

		// create directory structure needed for cache
		// file.mkdirs();
		// file.mkdir();

		System.out.println("File Name is: " + file.getName());
		// ========================================================

		// Presence check==========================================
		if (url.getHost().equals("localhost")) {
			// File is stored on the local server. Just use the path
			System.out.println("FILE IS LOCAL.");
			local = true;
			fileName = url.getFile();
		} else if (ClassLoader.getSystemResource(file.getName()) != null) {
			// file is cached
			System.out.println("FILE IS CACHED!");
		} else {
			// not cached, file is external

			// file is external
			System.out.println("FILE NOT CACHED. RETRIEVING FROM SERVER.");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			BufferedWriter out = new BufferedWriter(new FileWriter(
					file.getName()));
			char[] cbuf = new char[255];
			while ((in.read(cbuf)) != -1) {
				out.write(cbuf);
			}
			in.close();
			out.close();
			System.out.println("RETRIEVAL COMPLETED.");

		}
		// ========================================================

		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;
		System.out.println("filename2 is: " + fileName);

		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;

		try {
			fis = new FileInputStream(file.getName());
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		if (fileExists) {
			System.out.println("----File Found");
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			System.out.println("----200 Message Created");

		} else {
			System.out
					.println("----External Source not available... Output 404 instead.");
			statusLine = "HTTP/1.1 404 Not Found"; // edit
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>"
					+ "<HEAD><TITLE>ERROR - 404!</TITLE></HEAD>"
					+ "<BODY>The page you were looking for could not be retrieved.</br></br> No cached copy exists, and the origin server either does not exist or isn't responding.</BODY></HTML>";
			System.out.println("----404 Output given");
		}

		// Send the status line.
		os.writeBytes(statusLine);
		// Send the content type line.
		os.writeBytes(contentTypeLine);
		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);

		// Send the entity body.
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}

		// Close streams and socket.
		os.close();
		br.close();
		socket.close();

	}

	// public void saveUrlToFile(File saveFile, String location) {
	// URL url;
	// try {
	// url = new URL(location);
	// BufferedReader in = new BufferedReader(new InputStreamReader(
	// url.openStream()));
	// BufferedWriter out = new BufferedWriter(new FileWriter(saveFile));
	// char[] cbuf = new char[255];
	// while ((in.read(cbuf)) != -1) {
	// out.write(cbuf);
	// }
	// in.close();
	// out.close();
	//
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	private static String contentType(String fileName) {
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if (fileName.endsWith(".gif")) {
			return "image/gif";
		}
		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (fileName.endsWith("png")) {
			return "image/png";
		}
		if (fileName.endsWith(".js")) {
			return "application/javascript";
		}
		if (fileName.endsWith(".css")) {
			return "text/css";
		}

		return "application/octet-stream";
	}

	private static void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception {
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;
		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}
}