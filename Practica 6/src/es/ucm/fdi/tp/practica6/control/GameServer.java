package es.ucm.fdi.tp.practica6.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
	
	private int port;
	private int numPlayers;
	private int numOfConnectedPlayers;
	private GameFactory gameFactory;
	private List<Connection> clients;
	volatile private ServerSocket server;
	volatile private boolean stopped;
	volatile private boolean gameOver;
	private JTextArea infoArea;
	
	public GameServer(GameFactory gameFactory, List<Piece> pieces, int port) {
		super(new Game(gameFactory.gameRules()), pieces);
		this.port = port;
		this.gameFactory = gameFactory;
		this.numPlayers = pieces.size();
		this.clients = new ArrayList<Connection>();
		this.numOfConnectedPlayers = 0;
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
			//Caso de que haya 2 servidores escuchando en el mismo puerto, se cierra el servidor
			catch (NullPointerException e){
				System.exit(0);
			}
		}
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
			if (numOfConnectedPlayers >= numPlayers){
				throw new GameError("Max number of players reached");
			}
			
			numOfConnectedPlayers++;
			clients.add(c);
			
			c.sendObject("OK");
			c.sendObject(gameFactory);
			c.sendObject(pieces.get(numOfConnectedPlayers - 1));
			
			if (numOfConnectedPlayers == numPlayers){
				if (game.getState().equals(State.Starting)){
					game.start(pieces);
				}
				else {
					game.restart();
				}
			}
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
						Command cmd =(Command) c.getObject();
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
		t.start();
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
		JPanel mainPanel = new JPanel( new BorderLayout() );
		window.setContentPane(mainPanel);
		// create text area for printing messages
		infoArea = new JTextArea();
		infoArea.setEditable(false);
		mainPanel.add(infoArea, BorderLayout.CENTER);
		// quit button
		JButton quitButton = new JButton("Stop Server and Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopAndQuit();
			}
		});
		
		mainPanel.add(quitButton, BorderLayout.PAGE_END);
		//Mirar la dimension
		window.setPreferredSize(new Dimension(200, 200));
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * Stops the server and finishes the game when the confirmation window is accepted
	 */
	private void stopAndQuit() {
		int n = JOptionPane.showOptionDialog(new JFrame(), "Are sure you want to quit?", "Quit",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (n == 0) {
			stopServer();
			//finalize your app
			System.exit(0);
		}
	}
	
	
	/**
	 * Stops the server, marking stopped to true, stopping the game and turning off the server
	 */
	private void stopServer() {
		this.stopped = true;
		stopGame();
		try {
			server.close();
		} catch (IOException e) {
		}
	}
	/**
	 * Stop the game, marking gameOver to true and stopping each connection of the players
	 */
	private void stopGame() {
		if (game.getState().equals(State.InPlay)){
			stop();
		}
		gameOver = true;
		for (Connection c : clients){
			try {
				c.stop();
			} catch (IOException e) {
			}
		}
		//The game has ended, so there arent any connected players
		numOfConnectedPlayers = 0;	
		}

	private void log(String msg) {
		// show the message in infoArea, use invokeLater!!
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infoArea.append(msg);
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
		
		stopGame();
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
			}
		}
	}

}
