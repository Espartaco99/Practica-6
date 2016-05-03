package es.ucm.fdi.tp.practica6;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.control.commands.Command;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.Game;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.practica6.response.ChangeTurnResponse;
import es.ucm.fdi.tp.practica6.response.ErrorResponse;
import es.ucm.fdi.tp.practica6.response.GameOverResponse;
import es.ucm.fdi.tp.practica6.response.GameStartResponse;
import es.ucm.fdi.tp.practica6.response.MoveEndResponse;
import es.ucm.fdi.tp.practica6.response.MoveStartResponse;
import es.ucm.fdi.tp.practica6.response.Response;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class GameServer extends Controller implements GameObserver{
	
	//TODO AÑADIR COSAS
	private int port;
	private int numPlayers;
	private int numOfConnectedPlayers;
	private GameFactory gameFactory;
	private List<Connection> clients;
	volatile private ServerSocket server;
	volatile private boolean stopped;
	volatile private boolean gameOver;
	private String infoArea;
	
	public GameServer(GameFactory gameFactory, List<Piece> pieces, int port) {
		super(new Game(gameFactory.gameRules()), pieces);
		this.port = port;
		this.gameFactory = gameFactory;
		this.numPlayers = 0;
		this.numOfConnectedPlayers = 0;
		//TODO initialise the fields with corresponding values
		game.addObserver(this);
		}
	
	@Override
	public synchronized void makeMove(Player player) {
		try { super.makeMove(player); } catch (GameError e) { }
	}
	@Override
	public synchronized void stop() {
		//super.stop(player);
		try { super.stop(); } catch (GameError e) { }
	}
	@Override
	public synchronized void restart() {
		try { super.restart(); } catch (GameError e) { }
	}
	@Override
	public void start() {
		controlGUI();
		startServer();
	}
	
	private void startServer() {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopped = false;
		while (!stopped) {
			try {
				Socket s = server.accept();
				// 1. accept a connection into a socket s
				log("Connection Successful\n");
				// 2. log a corresponding message
				handleRequest(s);
				// 3. call handleRequest(s) to handle the request
			} catch (IOException e) {
				if (!stopped) {
					log("error while waiting for a connection: " + e.getMessage());
				}
			}
		}
		//TODO TENGO QUE PARAR EL SERVIDOR O NO?
		//server.close();
	}
	
	private void handleRequest(Socket s) {
		try {
			Connection c = new Connection(s);
			Object clientRequest = c.getObject();
			if ( !(clientRequest instanceof String) && !((String) clientRequest).equalsIgnoreCase("Connect")){
				c.sendObject(new GameError("Invalid Request"));
				c.stop();
				return;
			}
			if (numOfConnectedPlayers >= MAXIMO){
				throw new GameError("Max number of players reached");
			}
			//TODO
			numOfConnectedPlayers++;
			clients.add(c);
			// 3. …
			//TODO
			// 4. …
			//TODO
			startClientListener(c);
		} catch (IOException | ClassNotFoundException _e) { }
	}
	
	private void startClientListener(Connection c) {
		gameOver = false;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stopped && !gameOver) {
					try {
						//How to read a command
						Command cmd = null;
						
						// 1. read a Command
						cmd.execute(GameServer.this);
						// 2. execute the command
					} catch (ClassNotFoundException | IOException e) {
						if (!stopped && !gameOver) {
							stopGame();
						// stop the game (not the server)
						}
					}
				}
				
			}
		});
		//TODO
		t.start();
		//TODO
	}
	
	private void controlGUI() {
		try {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() { constructGUI(); }
		});
		} catch (InvocationTargetException | InterruptedException e) {
			throw new GameError("Something went wrong when constructing the GUI");
		}
	}
	
	private void constructGUI() {
		JFrame window = new JFrame("Game Server");
		//TODO
		// create text area for printing messages
		//Mirar si es un string o un JTextArea
		infoArea = "";
		//TODO
		// quit button
		JButton quitButton = new JButton("Stop Server");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopServer();
			}
		});
		//TODO
		window.add(quitButton);
		//Mirar la dimension
		window.setPreferredSize(new Dimension(200, 200));
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
	/**
	 * Stops the server, marking stopped to true, stopping the game and turning off the server
	 */
	private void stopServer() {
		// TODO Auto-generated method stub
		this.stopped = true;
		stopGame();
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Stop the game, marking gameOver to true and stopping each connection of the players
	 */
	private void stopGame() {
		stop();
		gameOver = true;
		for (Connection c : clients){
			try {
				c.stop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void log(String msg) {
		// show the message in infoArea, use invokeLater!!
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infoArea += msg;
			}
		});
	}
	
	@Override
	public void onGameStart(Board board, String gameDesc, List<Piece> pieces, Piece turn) {
		forwardNotification(new GameStartResponse(board, gameDesc, pieces, turn));
	}

	@Override
	public void onGameOver(Board board, State state, Piece winner) {
		forwardNotification(new GameOverResponse(board, state, winner));
		//Stop the game
	}

	@Override
	public void onMoveStart(Board board, Piece turn) {
		forwardNotification(new MoveStartResponse(board, turn));
	}

	@Override
	public void onMoveEnd(Board board, Piece turn, boolean success) {
		forwardNotification(new MoveEndResponse(board, turn, success));
	}

	@Override
	public void onChangeTurn(Board board, Piece turn) {
		forwardNotification(new ChangeTurnResponse(board, turn));
	}

	@Override
	public void onError(String msg) {
		forwardNotification(new ErrorResponse(msg));
	}
	
	void forwardNotification(Response r) {
		for (Connection c : clients){
			try {
				c.sendObject(r);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// call c.sendObject(r) for each client connection ‘c’
	}

}
