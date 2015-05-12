import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
		System.out.println("RequestLine: " + requestLine);

		// Get and display the header lines.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println("HeaderLine: " + headerLine);
		}

		// Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String fileInput = tokens.nextToken();
		String fileName = fileInput;

		String strippedFInput = null;
		String strippedFName = null;

		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;

		strippedFInput = fileInput.substring(6, fileInput.length() - 1);
		strippedFName = fileName.substring(6, fileInput.length() - 1);

		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;

		try {
			fis = new FileInputStream(fileName);// fileName
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) { // if cached
			System.out.println("----File Found");
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			System.out.println("----200 Message Created");

		} else {
			// try to load from external
			Boolean loaded = false;
			System.out.println("----File Not Found, Attempt to load.");

			URL url = new URL(fileInput);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			// allow both GZip and Deflate (ZLib) encodings
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			String encoding = connection.getContentEncoding();
			InputStream inStr = null;
			Path out = Paths.get(strippedFInput);

			//encoding checks before we get input. Set inStr equal to the result of the inputstream
			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				inStr = new GZIPInputStream(connection.getInputStream());
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				inStr = new InflaterInputStream(connection.getInputStream(),
						new Inflater(true));
			} else {
				inStr = connection.getInputStream();
			}

			try {
			//copy the contents of the input stream to the text file specified above.
			Files.copy(inStr, out);
			//Now set return values since we have the file.
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			
			loaded = true;
			System.out.println("----Attempted to load from external source");
			}
			catch (Exception e){
			System.out.println("Exception Occurred trying to load the file");
			}
		

			// return local 404 if page doesn't load/respond
			if (!loaded) {
				System.out
						.println("----External Source not available... Output 404 instead.");
				statusLine = "HTTP/1.1 404 Not Found"; // edit
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>"
						+ "<HEAD><TITLE>ERROR - 404!</TITLE></HEAD>"
						+ "<BODY>The page you were looking for could not be retrieved.</br></br> No cached copy exists, and the origin server either does not exist or isn't responding.</BODY></HTML>";
				System.out.println("----404 Output given");
			}
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