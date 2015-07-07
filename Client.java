package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class works as a client for taking user input, connecting with server
 * and displaying the result to user.
 * 
 * @author Tanmoy
 *
 */
public class Client {

	private static Scanner scanner = new Scanner(System.in);
	private ObjectOutputStream output; // output stream to server
	private ObjectInputStream input; // input stream from server
	private String message = ""; // message from server
	private String chatServer; // host server for this application
	private Socket clientSocket; // socket to communicate with server

	public Client(String host) {
		this.chatServer = host; // set server to which this client connects
	}

	/**
	 * Connect to server and process messages from server
	 */
	public void runClient() {

		try {
			connectToServer();
			getStreams();
			run();
		} catch (EOFException eofException) {
			System.out.println("");
		} catch (IOException ioException) {
			System.out.println("\nClient terminated connection ");
		} catch (ClassNotFoundException e) {
			System.out.println("\nClient terminated connection ");
		} finally {
			try {
				closeConnection();
			} catch (IOException e) {
				System.out.println("Exception in closing connection ...." + e);
				e.printStackTrace();
			}
		}
	} // end method runClient

	/**
	 * Sending message to server
	 */
	private void sendData(String message) {
		try {
			output.writeObject(message);
			output.flush(); // flush data to output

		} catch (IOException ioException) {
			System.out.println("\nError writing object");
		}
	}

	/**
	 * This program lets the user play Blackjack. The computer acts as the
	 * dealer. The user can leave at any time, or will be kicked out when he
	 * loses the money. House rules:: The dealer hits on a total of 16 or less
	 * and stands on a total of 17 or more. Dealer wins ties. A new deck of
	 * cards is used for each game.
	 * 
	 */

	public void run() throws IOException, ClassNotFoundException {

		int bet = 0; // Amount user bets on a game.

		System.out.println("Welcome to the game of blackjack.");
		System.out.println();
		do {
			System.out.println("How many dollars do you want to bet?");
			System.out.print("? ");
			// bet = scanner.nextInt();
			String betString = scanner.next();
			if (!betString.isEmpty()) {
				try {
					bet = Integer.parseInt(betString);
					if (bet <= 0) {
						System.out.println("Your bet amount is " + bet
								+ " it must be greater than Zero.");
						// return;
					}
				} catch (NumberFormatException nfe) {
					System.out
							.println("Enter an positive integer number greater than 0 ....");
					bet = 0;
				}
			}

		} while (bet <= 0);

		if (bet > 0) {
			sendData("StartGame"); // flushing the output
		}
		while (true) {
			// set up input stream for objects
			message = (String) input.readObject(); // read new message
			System.out.println("\n" + message); // display message

			if (message
					.equalsIgnoreCase("Hit (H) or Stand (S) or Double (D)? ")) {
				char userAction; // User's response, 'H' or 'S' or 'D'.
				do {
					userAction = Character
							.toUpperCase(scanner.next().charAt(0));
					if (userAction != 'H' && userAction != 'S'
							&& userAction != 'D') {
						System.out.print("Please respond H or S or D :  ");
					}

				} while (userAction != 'H' && userAction != 'S'
						&& userAction != 'D');

				if (userAction == 'D') {
					System.out
							.println("Your bet amount is double now, Amount is : "
									+ bet * 2 + "$");
				}
				sendData(String.valueOf(userAction));
			}
			if (message.equalsIgnoreCase("You busted by going over 21")) {
				System.out.println("You busted by going over 21.  You lose.");
				break;
			}
			if (message.equalsIgnoreCase("You have Blackjack")) {
				System.out.println("You have Blackjack.  You win.");
				break;
			}
			if (message.equalsIgnoreCase("Dealer has Blackjack")) {
				System.out.println("Dealer has Blackjack.  Dealer wins.");
				break;
			}
			if (message.contains("You win")) {
				System.out.println("You are the winner.");
				break;
			}
			if (message.contains("Dealer wins")) {
				System.out.println("Dealer is the winner");
				break;
			}
			if (message.equalsIgnoreCase("Dealer wins on a tie.  You lose.")) {
				System.out.println("Dealer wins on a tie.  You lose.");
				break;
			}
		} // End of while loop
	}

	/**
	 * Connecting to server
	 * 
	 * @throws IOException
	 */
	private void connectToServer() throws IOException {
		System.out.println("Attempting connection\n");

		// create Socket to make connection to server
		clientSocket = new Socket(InetAddress.getByName(chatServer), 23555);

		// display connection information
		System.out.println("Connected to: "
				+ clientSocket.getInetAddress().getHostName());
	} // end method connectToServer

	/**
	 * Get streams to send and receive data
	 * 
	 * @throws IOException
	 */
	private void getStreams() throws IOException {
		// set up output stream for objects
		output = new ObjectOutputStream(clientSocket.getOutputStream());
		output.flush(); // flush output buffer to send header information

		// set up input stream for objects
		input = new ObjectInputStream(clientSocket.getInputStream());

		System.out.println("\nGot I/O streams\n");
	} // end method getStreams

	/**
	 * Close streams and socket
	 * 
	 * @throws IOException
	 */
	private void closeConnection() throws IOException {
		System.out.println("\nClosing connection");
		try {
			output.close(); // close output stream
			input.close(); // close input stream
			clientSocket.close(); // close socket
		} // end try
		catch (IOException ioException) {
			System.out.println("Issue is closing connection");
			throw ioException;
		} // end catch
	} // end method closeConnection

}
