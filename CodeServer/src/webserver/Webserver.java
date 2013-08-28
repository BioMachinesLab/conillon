package webserver;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;

import result.ClassRequest;

import code.ClassCodeServer;

public class Webserver extends Thread {
	ClassCodeServer ccs;
	private String httpdir = "";

	public Webserver(int listen_port, ClassCodeServer ccs) {
		this.ccs = ccs;
		port = listen_port;
		this.start();
	}

	private int port;

	public void run() {

		ServerSocket serversocket = null;
		try {

			serversocket = new ServerSocket(port);
			System.out.println("Web server running on port " + port);
		} catch (Exception e) {
			System.out.println(e);
			return;
		}
		while (true) {
			try {
				Socket connectionsocket = serversocket.accept();
				new DealWithClient(connectionsocket).start();
			} catch (Exception e) {
				System.out.println(e);

			}

		}
	}

	private class DealWithClient extends Thread {

		private Socket connectionsocket;

		public DealWithClient(Socket connectionsocket) {
			super();
			this.connectionsocket = connectionsocket;
		}

		public void run() {
			try {

				InetAddress client = connectionsocket.getInetAddress();
				// System.out.println("New web server client: " + client);
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connectionsocket.getInputStream()));

				DataOutputStream output = new DataOutputStream(
						connectionsocket.getOutputStream());

				http_handler(input, output);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	private void http_handler(BufferedReader input, DataOutputStream output) {
		int method = 0;
		String path = new String();
		try {

			// GET /index.html HTTP/1.0
			// HEAD /index.html HTTP/1.0

			String tmp = input.readLine();
			if (tmp != null) {
				String tmp2 = tmp;
				System.out.println("TMP2:" + tmp2);
				tmp = tmp.toUpperCase();
				if (tmp.startsWith("GET")) {
					method = 1;
				}
				if (tmp.startsWith("HEAD")) {
					method = 2;
				}

				if (method == 0) { // not supported
					output.writeBytes(construct_http_header(501, 0));
					output.close();
					return;
				}

				// tmp contains "GET /index.html HTTP/1.0 ......."
				int start = 0;
				int end = 0;
				for (int a = 0; a < tmp2.length(); a++) {
					if (tmp2.charAt(a) == ' ' && start != 0) {
						end = a;
						break;
					}
					if (tmp2.charAt(a) == ' ' && start == 0) {
						start = a;
					}
				}

				path = tmp2.substring(start + 2, end); // fill in the path

				// System.out.println("PATH+" + path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (path.equals("")) {
			path = "index.html";
		}
		int id = 0;
		int type_is = 0;

		try {
			if (path.endsWith(".class")) {
				type_is = 4;
				// System.out.println("its .class");

				// path = "bin/"+path;
				String originalPath = path;
				try {
					// int pos1 = path.indexOf("/");
					// problemNumber = Integer.parseInt(path.substring(0,
					// pos1));
					// int pos2 = path.indexOf("/", pos1 + 1);
					// versionNumber = Integer.parseInt(path.substring(pos1 + 1,
					// pos2));
					//
					// path = path.substring(pos2 + 1, path.length());

					int pos1 = path.indexOf("/");
					// String s= path.substring(2, pos1);
					id = Integer.parseInt(path.substring(2, pos1));
					// int pos2 = path.indexOf("/", pos1 + 1);
					// versionNumber = Integer.parseInt(path.substring(pos1 + 2,
					// pos2));
					//
					// path = path.substring(pos2 + 1, path.length());
				} catch (NumberFormatException e) {
					path = originalPath;
				}
				path = path.substring(0, path.length() - 6);
				// System.out.println("Trying code server ...");
				path = path.replaceAll("/", ".");
				output.writeBytes(construct_http_header(200, 4));
				ClassRequest neededClass = new ClassRequest(id, path);
				System.out.println("needClass: " + neededClass);
				byte[] classToSend = ccs.getClassBy(neededClass);
				if (classToSend != null) {
					System.out.println("Encontrou a class");

					int[] x = new int[classToSend.length];
					for (int j = 0; j < classToSend.length; j++) {
						output.write(classToSend[j]);
					}
					output.close();
					System.out.println("Class sent " + neededClass);
					return;

				}
				// problemNumber = 7;
				// versionNumber = 7;

				// System.out.println(path + " - (" + id + ")");
			}
			// System.out.println("PATH: '" + path + "'");
			FileInputStream requestedfile = null;

			try {
				requestedfile = new FileInputStream(path);

				// find out what the filename ends with,
				// so you can construct a the right content type
				if (path.endsWith(".zip") || path.endsWith(".exe")
						|| path.endsWith(".tar")) {
					type_is = 3;
				} else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
					type_is = 1;
				} else if (path.endsWith(".gif")) {
					type_is = 2;
					// write out the header, 200 ->everything is ok we are all
					// happy.
				} else if (path.endsWith(".jar")) {
					type_is = 4;
				}
				output.writeBytes(construct_http_header(200, type_is));
				System.out.println("sent: " + path);
				if (method == 1) { // 1 is GET 2 is head and skips the body
					// System.out.println("Sending: " + path);
					while (true) {
						// read the file from filestream, and print out through
						// the
						// client-outputstream on a byte per byte base.
						int b = requestedfile.read();
						if (b == -1) {
							break;
						}
						output.write(b);
					}
				}
				output.close();
				requestedfile.close();

			} catch (IOException e) {
				try {
					// if you could not open the file send a 404
					System.out.println("File doesn't exists " + path);
					output.writeBytes(construct_http_header(404, 0));
					output.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	// this method makes the HTTP header for the response
	// the headers job is to tell the browser the result of the request
	// among if it was successful or not.
	private String construct_http_header(int return_code, int file_type) {
		String s = "HTTP/1.0 ";
		switch (return_code) {
		case 200:
			s = s + "200 OK";
			break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 403:
			s = s + "403 Forbidden";
			break;
		case 404:
			s = s + "404 Not Found";
			break;
		case 500:
			s = s + "500 Internal Server Error";
			break;
		case 501:
			s = s + "501 Not Implemented";
			break;
		}

		s = s + "\r\n"; // other header fields,
		s = s + "Connection: close\r\n"; // we can't handle persistent
											// connections
		s = s + "Server: Conillon web server v1\r\n"; // server name

		switch (file_type) {
		// plenty of types for you to fill in
		case 0:
			break;
		case 1:
			s = s + "Content-Type: image/jpeg\r\n";
			break;
		case 2:
			s = s + "Content-Type: image/gif\r\n";
		case 3:
			s = s + "Content-Type: application/x-zip-compressed\r\n";
			break;
		case 4:
			s = s + "Content-Type: application/java-archive\r\n";
		default:
			s = s + "Content-Type: text/html\r\n";
			break;
		}

		s = s + "\r\n"; // this marks the end of the httpheader
		return s;
	}

}
