package client;

/**
 * The class calls the client class.
 * 
 * @author Tanmoy
 *
 */
public class ClientRunner {

	public static void main(String[] args) {

		Client clientApp; // declare client application

		if (args.length == 0)
			clientApp = new Client("172.18.19.36"); // connect to localhost
		else
			clientApp = new Client(args[0]); // use args to connect

		clientApp.runClient(); // run client application

	} // end main

}
