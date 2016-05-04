package es.ucm.fdi.tp.practica6.control;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.control.commands.Command;
import es.ucm.fdi.tp.basecode.bgame.control.commands.PlayCommand;
import es.ucm.fdi.tp.basecode.bgame.control.commands.QuitCommand;
import es.ucm.fdi.tp.basecode.bgame.control.commands.RestartCommand;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.practica6.response.Response;

public class GameClient extends Controller implements Observable<GameObserver>{
	private String host;
	private int port;
	private List<GameObserver> observers;
	private Piece localPiece;
	private GameFactory gameFactory;
	private Connection connectionToServer;
	private boolean gameOver;
	//TODO
	
	public GameClient(String host, int port) throws Exception {
		super(null, null);
		this.host = host;
		this.port = port;
		this.observers = new ArrayList<GameObserver>();
		connect();
	}
	
	private void connect() throws Exception {
		connectionToServer = new Connection(new Socket(host, port));
		connectionToServer.sendObject("Connect");
		//TODO
		Object response = connectionToServer.getObject();
		if (response instanceof Exception) {
		throw (Exception) response;
		}
		try {
			gameFactory = (GameFactory) connectionToServer.getObject();
			localPiece = (Piece) connectionToServer.getObject();
			
		} catch (Exception e) {
			throw new GameError("Unknown server response: "+e.getMessage());
		}
	}
	//A quien consulto estos valores
	public GameFactory getGameFactory() { return gameFactory; }
	public Piece getPlayerPiece() { return localPiece; }
	public void addObserver(GameObserver o) { observers.add(o); }
	public void removeObserver(GameObserver o) { observers.remove(o); }
	
	public void start() {
		//Como hacer esto
		this.observers.add( new GameObserver() {
			
			@Override
			public void onMoveStart(Board board, Piece turn) {
			}
			
			@Override
			public void onMoveEnd(Board board, Piece turn, boolean success) {
			}
			
			@Override
			public void onGameStart(Board board, String gameDesc, List<Piece> pieces,
					Piece turn) {
			}
			
			@Override
			public void onGameOver(Board board, State state, Piece winner) {
				gameOver = true;
			}
			
			@Override
			public void onError(String msg) {
			}
			
			@Override
			public void onChangeTurn(Board board, Piece turn) {
			}
		});
		gameOver = false;
		while (!gameOver) {
				try {
				Response res = (Response) connectionToServer.getObject(); // read a response
				for (GameObserver o : observers) {
					// execute the response on the observer o
					res.run(o);
				}
			} catch (ClassNotFoundException | IOException e) {
			}
		}
		//TODO
	}
	
	@Override
	public void makeMove(Player p) {
		forwardCommand(new PlayCommand(p));
	}
	@Override
	public void stop() {
		forwardCommand(new QuitCommand());
	}
	@Override
	public void restart() {
		forwardCommand(new RestartCommand());
	}
	
	//Como hacer esto
	private void forwardCommand(Command cmd) {
		if (!gameOver){
			try {
				connectionToServer.sendObject(cmd);
			} catch (IOException e) {

			}
		}
		// if the game is over do nothing, otherwise
		// send the object cmd to the server
	}
	

}
