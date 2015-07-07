package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This Class is working as a SERVER for receiving & sending response to client.
 * 
 * @author Tanmoy
 *
 */
public class ServerDealer {

	private int[] deck; // An array of 52 Cards, representing the deck.
	private int currentPosition; // Current position in the deck
	private ServerSocket server; // server socket
	private SockServer[] sockServer; // Array of objects to be threaded
	private int counter = 1; // counter of number of connections
	private ExecutorService executor; // will run players
	private String message = "";

	/**
	 * Set up and run the server to receive connections & process connections
	 */
	public void runDeal() {
		try {
			// allocate array for up to 10 server threads
			sockServer = new SockServer[100];
			executor = Executors.newFixedThreadPool(100); // create thread pool
			server = new ServerSocket(23555, 100); // create ServerSocket

			while (true) {
				try {
					// create a new runnable object to serve the next client to
					// call in
					sockServer[counter] = new SockServer(counter);
					// make that new object wait for a connection on that new
					// server object
					sockServer[counter].waitForConnection();
					// launch that server object into its own new thread
					executor.execute(sockServer[counter]);
					// then, continue to create another object and wait (loop)

				} // end try
				catch (EOFException eofException) {
					System.out.println("\nServer terminated connection");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					++counter;
				} // end finally
			} // end while
		} catch (IOException ioException) {
			System.out.println("\nServer terminated connection");
		}
	} // end method runServer

	/**
	 * This new Inner Class implements Runnable and objects instantiated from
	 * this class will become server threads each serving a different client
	 */
	private class SockServer implements Runnable {
		private ObjectOutputStream output; // output stream to client
		private ObjectInputStream input; // input stream from client
		private Socket connection; // connection to client
		private int myConID;

		public SockServer(int counterIn) {
			myConID = counterIn;
		}

		public void run() {
			try {
				try {
					getStreams(); // get input & output streams
					playBlackjack();
					// processConnection(); // process connection
				} // end try
				catch (EOFException eofException) {
					System.out.println("\nServer" + myConID
							+ " terminated connection");
				} catch (ClassNotFoundException e) {
					System.out.println("\nServer" + myConID
							+ " terminated connection");
				} finally {
					closeConnection(); // close connection
				}// end catch
			} // end try
			catch (IOException ioException) {
			} // end catch
		} // end try

		// wait for connection to arrive, then display connection info
		private void waitForConnection() throws IOException {

			System.out.println("Waiting for connection" + myConID + "\n");
			connection = server.accept(); // allow server to accept connection
			System.out.println("Connection " + myConID + " received from: "
					+ connection.getInetAddress().getHostName());
		} // end method waitForConnection

		private void getStreams() throws IOException {
			// set up output stream for objects
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush(); // flush output buffer to send header information

			// set up input stream for objects
			input = new ObjectInputStream(connection.getInputStream());

			System.out.println("\nGot I/O streams\n");
		} // end method getStreams

		/**
		 * Close streams and socket
		 */
		private void closeConnection() {
			System.out.println("\nTerminating connection " + myConID + "\n");
			try {
				output.close(); // close output stream
				input.close(); // close input stream
				connection.close(); // close socket
			} catch (IOException ioException) {
				System.out.println("Exception occured in closing connection"
						+ ioException);
			}
		} // end method closeConnection

		/**
		 * Send object to client
		 * 
		 * @param String
		 */
		private void sendData(String message) {
			try {
				output.writeObject(message);
				output.flush(); // flush output to client
			} catch (IOException ioException) {
				System.out.println("\nError writing object");
			}
		} // end method sendData

		/**
		 * Let the user play one game of Blackjack.
		 * 
		 * @throws IOException
		 * @throws ClassNotFoundException
		 */
		private void playBlackjack() throws IOException, ClassNotFoundException {

			List<Integer> dealerHand; // The dealer's hand.
			List<Integer> userHand; // The user's hand.

			// Create an unshuffled deck of cards.
			deck = new int[52];
			int cardCt = 0; // How many cards have been created so far.
			for (int suit = 0; suit <= 3; suit++) {
				for (int value = 1; value <= 13; value++) {
					deck[cardCt] = value;
					cardCt++;
				}
			}
			currentPosition = 0;

			dealerHand = new ArrayList<Integer>();
			userHand = new ArrayList<Integer>();

			/* Shuffle the deck, then deal two cards to each player. */

			shuffle();

			dealerHand.add(dealCard());
			dealerHand.add(dealCard());
			userHand.add(dealCard());
			userHand.add(dealCard());

			/**
			 * Check if one of the players has Blackjack (two cards totaling to
			 * 21). The player with Blackjack wins the game. Dealer wins ties.
			 */

			if (value(dealerHand) == 21) {

				message = "Dealer has the " + showCard(getCard(dealerHand, 0))
						+ " and the " + showCard(getCard(dealerHand, 1)) + ".";
				sendData(message);

				message = ("User has the " + showCard(getCard(userHand, 0))
						+ " and the " + showCard(getCard(userHand, 1)) + ".");
				sendData(message);

				message = ("Dealer has Blackjack");
				sendData(message);
			}

			if (value(userHand) == 21) {
				message = ("Dealer has the " + showCard(getCard(dealerHand, 0))
						+ " and the " + showCard(getCard(dealerHand, 1)) + ".");
				sendData(message);

				message = ("User has the " + showCard(getCard(userHand, 0))
						+ " and the " + showCard(getCard(userHand, 1)) + ".");
				sendData(message);

				message = ("You have Blackjack");
				sendData(message);
			}

			/**
			 * If neither player has Blackjack, play the game. The user gets a
			 * chance to draw cards (i.e., to "Hit"). The while loop ends when
			 * the user chooses to "Stand" or when the user goes over 21.
			 */
			boolean entryCheck = false;
			String startCheckval = "";
			startCheckval = (String) input.readObject();
			if (startCheckval.equalsIgnoreCase("StartGame")) {
				entryCheck = true;
			}

			sendData("Your cards are:");
			for (int i = 0; i < userHand.size(); i++) {
				sendData("    " + showCard(getCard(userHand, i)));
			}
			sendData("Your total is " + value(userHand));
			sendData("Dealer is showing the "
					+ showCard(getCard(dealerHand, 0)));
			sendData("Hit (H) or Stand (S) or Double (D)? ");

			while (entryCheck) {
				if (!startCheckval.equals("StartGame")) {
					message = (String) input.readObject();
					startCheckval = "";

					/**
					 * If the user Hits, the user gets a card. If the user
					 * Stands, the dealer gets a chance to draw and the game
					 * ends.
					 */

					if ("S".equalsIgnoreCase(message)) {
						entryCheck = false;
						sendData("User stands.");
						break;
					}
					if ("H".equalsIgnoreCase(message)) {
						int newCard = dealCard();
						userHand.add(newCard);
						sendData("User hits. \n Your card are ");
						for (int i = 0; i < userHand.size(); i++) {
							sendData("    " + showCard(getCard(userHand, i)));
						}
						sendData(" \n Your total is now " + value(userHand));
						entryCheck = true;
						if (value(userHand) > 21) {
							sendData("You busted by going over 21.  You lose.");
							entryCheck = false;
							return;
						}
					}
					if ("D".equalsIgnoreCase(message)) {
						int newCard = dealCard();
						userHand.add(newCard);
						sendData("User want Double \n Your card are");
						for (int i = 0; i < userHand.size(); i++) {
							sendData("    " + showCard(getCard(userHand, i)));
						}
						sendData(" \n Your total is now " + value(userHand));
						entryCheck = false;
						if (value(userHand) > 21) {
							sendData("You busted by going over 21.  You lose.");
							entryCheck = false;
							return;
						}
						break;
					}

					sendData("Your cards are:");
					for (int i = 0; i < userHand.size(); i++) {
						sendData("    " + showCard(getCard(userHand, i)));
					}
					sendData("Your total is " + value(userHand));
					sendData("Dealer is showing the "
							+ showCard(getCard(dealerHand, 0)));
					sendData("Hit (H) or Stand (S) or Double (D)? ");

					// }
				}
				startCheckval = "";

			} // end while loop

			/**
			 * If we get to this point, the user has Stood with 21 or less. Now,
			 * it's the dealer's chance to draw. Dealer draws cards until the
			 * dealer's total is > 16.
			 */

			sendData("Dealer's cards are");
			sendData("    " + showCard(getCard(dealerHand, 0)));
			sendData("    " + showCard(getCard(dealerHand, 1)));
			while (value(dealerHand) <= 16) {
				int newCard = dealCard();
				sendData("Dealer hits and gets the " + showCard(newCard));
				dealerHand.add(newCard);
			}
			sendData("Dealer's total is " + value(dealerHand));

			/** Now, the winner can be declared. */

			if (value(dealerHand) > 21) {
				sendData("Dealer busted by going over 21.  You win.");
			} else {
				if (value(dealerHand) == value(userHand)) {
					sendData("Dealer wins on a tie.  You lose.");
				} else {
					if (value(dealerHand) > value(userHand)) {
						sendData("Dealer wins, " + value(dealerHand)
								+ " points to " + value(userHand) + ".");
					} else {
						sendData("You win, " + value(userHand) + " points to "
								+ value(dealerHand) + ".");
					}
				}
			}

		} // end playBlackjack()

	} // end class SockServer

	/**
	 * Deals one card from the deck and returns it.
	 * 
	 * @return Integer
	 */
	public int dealCard() {

		if (currentPosition == 52) {
			shuffle();
		}
		currentPosition++;
		return deck[currentPosition - 1];
	}

	/**
	 * Put all the used cards back into the deck, and shuffle it into a random
	 * order.
	 */
	public void shuffle() {
		for (int i = 51; i > 0; i--) {
			int rand = (int) (Math.random() * (i + 1));
			int temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		currentPosition = 0;
	}

	/**
	 * Get the card from the hand in given position, where positions are
	 * numbered starting from 0. If the specified position is not the position
	 * number of a card in the hand, then null is returned.
	 * 
	 * @param List
	 *            <Integer>
	 * @param position
	 * @return Integer
	 */
	public int getCard(List<Integer> hand2, int position) {
		if (position >= 0 && position < hand2.size()) {
			return ((Integer) hand2.get(position)).intValue();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the value of this hand for the game of Blackjack.
	 * 
	 * @param List
	 *            <Integer>
	 * @return Integer
	 */
	public int value(List<Integer> hand) {
		int val; // The value computed for the hand.
		boolean ace; // This will be set to true if the
		// hand contains an ace.
		int cards; // Number of cards in the hand.

		val = 0;
		ace = false;
		cards = hand.size();

		for (int i = 0; i < cards; i++) {
			// Add the value of the i-th card in the hand.
			int card; // The i-th card;
			int cardVal; // The blackjack value of the i-th card.
			card = getCard(hand, i);
			cardVal = getCardValue(card); // The normal value, 1 to 13.
			if (cardVal > 10) {
				cardVal = 10; // For a Jack, Queen, or King.
			}
			if (cardVal == 1) {
				ace = true; // There is at least one ace.
			}
			val = val + cardVal;
		}

		// Now, val is the value of the hand, counting any ace as 1.
		// If there is an ace, and if changing its value from 1 to
		// 11 would leave the score less than or equal to 21,
		// then do so by adding the extra 10 points to val.

		if (ace == true && val + 10 <= 21) {
			val = val + 10;
		}

		return val;

	}

	/**
	 * Get the value of a card
	 * 
	 * @param card
	 * @return Integer
	 */
	public int getCardValue(int card) {
		int result = card;
		switch (card) {
		case 11:
		case 12:
		case 13:
			result = 10;
		}
		return result;
	}

	/**
	 * Returns the Card name
	 * 
	 * @param card
	 * @return String
	 */
	public String showCard(int card) {
		switch (card) {
		case 1:
			return "Ace";
		case 2:
			return "2";
		case 3:
			return "3";
		case 4:
			return "4";
		case 5:
			return "5";
		case 6:
			return "6";
		case 7:
			return "7";
		case 8:
			return "8";
		case 9:
			return "9";
		case 10:
			return "10";
		case 11:
			return "Jack";
		case 12:
			return "Queen";
		case 13:
			return "King";
		default:
			return "??";
		}
	}

}
