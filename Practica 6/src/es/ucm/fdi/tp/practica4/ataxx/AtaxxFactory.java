package es.ucm.fdi.tp.practica4.ataxx;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.ucm.fdi.tp.basecode.bgame.control.ConsolePlayer;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.DummyAIPlayer;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.AIAlgorithm;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.views.GenericConsoleView;

/**
 * A factory for creating ataxx games. See {@link AtaxxRules} for the
 * description of the game.
 * 
 * 
 * <p>
 * Factoria para la creacion de juegos ataxx. Vease {@link AtaxxRules}
 * para la descripcion del juego.
 */
public class AtaxxFactory implements GameFactory {
	
	/**
	 * Required to avoid a warning
	 */
	private static final long serialVersionUID = 1L;
	private static final int DEF_DIM = 7;
	private static final int DEF_OBS = 0;
	private int dim;
	private int obstacles;

	public AtaxxFactory() {
		this(DEF_DIM);
		obstacles = DEF_OBS;
	}

	public AtaxxFactory(int dim) {
		if (dim < 5) {
			throw new GameError("Dimension must be at least 5: " + dim);
		} else {
			this.dim = dim;
			obstacles = DEF_OBS;
		}
	}
	//El dummy esta para evitar lios entre constructoras
	public AtaxxFactory(int obs, boolean dummy) {
		this(DEF_DIM);
		if (obs > ((dim * dim) / 5)){
			throw new GameError("The number of obstacules must be equal or less than " + ((dim * dim) / 5));
		} else {
			this.obstacles = obs;
		}
	}
	
	public AtaxxFactory(int dim, int obs) {
		if (dim < 5) {
			throw new GameError("Dimension must be at least 5: " + dim);
		}
		else if (obs > ((dim * dim) / 5)){
			throw new GameError("The number of obstacules must be equal or less than " + ((dim * dim) / 5));
		}
		else {
			this.dim = dim;
			this.obstacles = obs;
		}
	}

	@Override
	public GameRules gameRules() {
		return new AtaxxRules(dim, obstacles);
	}

	@Override
	public Player createConsolePlayer() {
		ArrayList<GameMove> possibleMoves = new ArrayList<GameMove>();
		possibleMoves.add(new AtaxxMove());
		return new ConsolePlayer(new Scanner(System.in), possibleMoves);
	}

	@Override
	public Player createRandomPlayer() {
		return new AtaxxRandomPlayer();
	}

	@Override
	public Player createAIPlayer(AIAlgorithm alg) {
		return new DummyAIPlayer(createRandomPlayer(), 1000);
	}

	/**
	 * By default, we have two players, X and O, with 2 pieces .
	 * <p>
	 * Por defecto, hay dos jugadores, X y O.
	 */
	@Override
	public List<Piece> createDefaultPieces() {
		List<Piece> pieces = new ArrayList<Piece>();
		pieces.add(new Piece("X"));
		pieces.add(new Piece("O"));
		return pieces;
	}
	
	

	@Override
	public void createConsoleView(Observable<GameObserver> g, Controller c) {
		new GenericConsoleView(g, c);
	}

	@Override
	public void createSwingView(final Observable<GameObserver> g, final Controller c, final Piece viewPiece,
			Player random, Player ai) {
		throw new UnsupportedOperationException("There is no swing view");
	}

}
