package mpe.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mpe.config.FileParser;

public class ClientLauncher {

	static String path = "/Users/daniel/Desktop/bigscreens/";
	static int listenPort = 9005;

	static String background = "background.app";

	static String lastExecuted = "";
	static int textDelay = 5000;

	public static void main(String[] args) throws IOException {

		init();
		while (!server()) {
			System.out.println("Restarting Server!");
		}

		System.out.println("Quitting");

	}

	public static boolean server() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(listenPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + listenPort + ".");
			System.exit(1);
		}

		System.out.println("Waiting for controller.");
		Socket clientSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("Controller connected.");

			String inputLine, outputLine;
			//outputLine = "Success.";
			//out.println(outputLine);
			while ((inputLine = in.readLine()) != null) {
				System.out.println("Controller says: "  + inputLine);

				if (inputLine.indexOf("kbackground") > -1) {
					killBackground();
				} else if (inputLine.indexOf("background") > -1) {
					runBackground();
				} else if (inputLine.indexOf("kill") > -1) {
					killProgram();
				} else if (inputLine.indexOf("ktitles") > -1) {
					killText();
				} else if (inputLine.charAt(0) == '*') {
					runTitlesProgramAuto(inputLine.substring(1,inputLine.length()),false);
				} else if (inputLine.charAt(0) == '!') {
					runTitlesProgramAuto(inputLine.substring(1,inputLine.length()),true);
				} else if (inputLine.charAt(0) == '$'){
					runTitles(inputLine.substring(1,inputLine.length()));
				} else if (inputLine.charAt(0) == '#') {
					runProgram(inputLine.substring(1,inputLine.length()));
				}
			}

			out.close();
			in.close();
			clientSocket.close();
			serverSocket.close();
			return false;
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		return true;
	}


	public static void runTitles(String toExecute) {
		try {
			Runtime rt = Runtime.getRuntime();
			String s = toExecute.substring(0,toExecute.indexOf("/",1));
			String titles = "open "+ path + s + "/titles/titles.app";
			System.out.println("Executing: " + titles);
			rt.exec(titles);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public static void runProgram(String toExecute) {
		try {
			Runtime rt = Runtime.getRuntime();
			killText();
			Thread.sleep(50);  // This needs to to be longer in order to not have a conflict
			String fullCommand = "open " + path + toExecute;
			System.out.println("Launching: " + fullCommand);
			rt.exec(fullCommand);
			lastExecuted = toExecute;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void runTitlesProgramAuto(String toExecute, boolean text) {
		try {
			Runtime rt = Runtime.getRuntime();
			if (text) {
				String s = toExecute.substring(0,toExecute.indexOf("/",1));
				String fullCommand = "open "+ path + s + "/titles/titles.app";
				System.out.println("Titles: " + fullCommand);
				rt.exec(fullCommand);
				Thread.sleep(textDelay);
				killText();
			}
			Thread.sleep(50);
			System.out.println("Launching " + toExecute);
			String fullCommand = "open " + path + toExecute;
			System.out.println("Launching: " + fullCommand);
			rt.exec(fullCommand);
			lastExecuted = toExecute;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void runBackground() {
		System.out.println("Launching " + background);
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("open " + path + background);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void killBackground() {
		System.out.println("Killing background");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			while ((str = in.readLine()) != null)  {
				if (str.indexOf(background) > 0) {
					System.out.println(str);
					break;
				}
			}
			//String id = str.substring(0,str.indexOf("  "));
			//Pattern pat = Pattern.compile("\\s+(\\d++)\\s??");
			Pattern pat = Pattern.compile("(\\d++)\\s??");
			Matcher m = pat.matcher(str);
			m.find();
			String id = m.group(1);//str.substring(0,str.indexOf("  "));
			System.out.println(id.trim());
			Runtime rt = Runtime.getRuntime();
			rt.exec("kill " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void killProgram() {
		System.out.println("Killing app");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			while ((str = in.readLine()) != null)  {
				//if (str.indexOf("bigscreens") > 0) {
				if (str.indexOf(lastExecuted) > 0) {
					System.out.println(str);
					break;
				}
			}
			//String id = str.substring(0,str.indexOf("  "));
			//Pattern pat = Pattern.compile("\\s+(\\d++)\\s??");
			Pattern pat = Pattern.compile("(\\d++)\\s??");
			Matcher m = pat.matcher(str);
			m.find();
			String id = m.group(1);//str.substring(0,str.indexOf("  "));
			System.out.println(id.trim());
			Runtime rt = Runtime.getRuntime();
			rt.exec("kill " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void killText() {
		System.out.println("Killing text intro");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			while ((str = in.readLine()) != null)  {
				if (str.indexOf("titles") > 0) {
					System.out.println(str);
					break;
				}
			}
			Pattern pat = Pattern.compile("(\\d++)\\s??");
			Matcher m = pat.matcher(str);
			m.find();
			String id = m.group(1);//str.substring(0,str.indexOf("  "));
			System.out.println(id.trim());
			Runtime rt = Runtime.getRuntime();
			rt.exec("kill " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkProgram() {
		System.out.println("Checking if running");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			boolean found = false;
			while ((str = in.readLine()) != null)  {
				if (str.indexOf(lastExecuted) > 0) {
					System.out.println("Still running: " +  str);
					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("Not found! Relaunch!");
				runTitlesProgramAuto(lastExecuted,false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void init() {
		FileParser fp = new FileParser("client.ini");
		if (fp.fileExists()) {
			path = fp.getStringValue("path");
			System.out.println("Path is: " + path);
			background = fp.getStringValue("background");
			System.out.println("Background app is: " + background);
			listenPort = fp.getIntValue("port");
			System.out.println("Port is: " + listenPort);
			textDelay = fp.getIntValue("text")*1000;
			System.out.println("Text Delay: " + textDelay);

		} else {
			System.out.println("Couldn't find ini file.");
		}
	}
}