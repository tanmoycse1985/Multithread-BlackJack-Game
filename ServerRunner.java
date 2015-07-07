package server;

/**
 * This class calls the server class.
 * 
 * @author Tanmoy
 *
 */
public class ServerRunner {

	public static void main(String[] args) {
		ServerDealer serverDealer = new ServerDealer();
		serverDealer.runDeal();
	}
}
