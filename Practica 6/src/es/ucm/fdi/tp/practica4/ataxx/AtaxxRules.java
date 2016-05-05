package es.ucm.fdi.tp.practica4.ataxx;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.Utils;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.FiniteRectBoard;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Pair;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;

/**
 * Rules for Ataxx game.
 * <ul>
 * <li>The game is played on an NxN board (with N>=5).</li>
 * <li>The number of players is between 2 and 4.</li>
 * <li>The player turn in the given order, each placing a piece on an empty
 * cell. The winner is the one who construct a line (horizontal, vertical or
 * diagonal) with N consecutive pieces of the same type.</li>
 * </ul>
 * 
 * <p>
 * Reglas del juego Ataxx.
 * <ul>
 * <li>El juego se juega en un tablero NxN (con N>=5).</li>
 * <li>El numero de jugadores esta entre 2 y 4.</li>
 * <li>Los jugadores juegan en el orden proporcionado, cada uno colocando una
 * ficha en una casilla vacia. El ganador es el que consigua construir una linea
 * (horizontal, vertical o diagonal) de N fichas consecutivas del mismo tipo.
 * </li>
 * </ul>
 *
 */
public class AtaxxRules implements GameRules {

	// This object is returned by gameOver to indicate that the game is not
	// over. Just to avoid creating it multiple times, etc.
	//
	protected final Pair<State, Piece> gameInPlayResult = new Pair<State, Piece>(State.InPlay, null);

	private int dim;
	private int obstacles;

	public AtaxxRules(int dim, int obs) {
		if (dim < 5) {
			throw new GameError("Dimension must be at least 5: " + dim);
		}
		else if (dim % 2 == 0){
			throw new GameError("Dimension must be odd: " + dim);
		}
		//The number of obstacles must be less than the 20% 
		else if (obs > ((dim * dim) / 5)){
			throw new GameError("The number of obstacules must be less than " + ((dim * dim) / 5));
		}
		else {
			this.dim = dim;
			this.obstacles = obs;
		}
	}

	@Override
	public String gameDesc() {
		if (obstacles == 0){
			return "Ataxx " + dim + "x" + dim;
		}
		else return "Ataxx " + dim + "x" + dim + " with " + obstacles + " obstacles";
	}

	@Override
	public Board createBoard(List<Piece> pieces) {
		return initializeBoard(pieces, new FiniteRectBoard(dim, dim));
	}
	
	/**
	 * Inicializa el tablero, poniendo fichas en las posiciones nuevas
	 * @param pieces The list of players
	 * @param board The board in which the game is played
	 */
	private Board initializeBoard(List<Piece> pieces, Board board){
		board.setPosition(0, 0, pieces.get(0));
		board.setPosition(dim - 1, dim - 1, pieces.get(0));
		board.setPosition(0, dim - 1, pieces.get(1));
		board.setPosition(dim - 1, 0, pieces.get(1));
		//Players == 3
		if (pieces.size() == 3){
			board.setPosition(dim / 2, 0, pieces.get(2));
			board.setPosition(dim / 2, dim - 1, pieces.get(2));
		}
		//Players == 4
		else if (pieces.size() == 4){
			board.setPosition(dim / 2, 0, pieces.get(2));
			board.setPosition(dim / 2, dim - 1, pieces.get(2));
			board.setPosition(0, dim / 2, pieces.get(3));
			board.setPosition(dim - 1, dim / 2, pieces.get(3));
		}
		else if (pieces.size() != 2){
			throw new GameError("The number of players isnt 2, 3 or 4, so i create the default board");  
		}
		//Añadir Obstaculos (en que posiciones los pongo) obstaculos
		int i = 0;
		
		Piece obstacle = getObstPiece(pieces);
		
		while(i < obstacles){
			int a = Utils.randomInt(dim);
			int b = Utils.randomInt(dim);
			//Si la posicion no esta ocupada, creo el obstaculo
			if (board.getPosition(a, b) == null){
				board.setPosition(a, b, obstacle);
				i++;
			}
		}
		
		
		return board;
	}

	@Override
	public Piece initialPlayer(Board board, List<Piece> playersPieces) {
		//Protection against situations where the player 1 (index 0) cant do a first move, for example, if you create a lot of obstacles
		//(for example, 9 (the cap of obstacles in a 7x7 board) with only 2 players)
		//I use playersPieces.size() - 1 as the player so that the next is the player 1 (index 0)
		return nextPlayer(board, playersPieces, playersPieces.get(playersPieces.size() - 1));
	}

	@Override
	public int minPlayers() {
		return 2;
	}

	@Override
	public int maxPlayers() {
		return 4;
	}

	@Override
	public Pair<State, Piece> updateState(Board board, List<Piece> pieces, Piece lastPlayer){
		int[] numPieces = countPieces(board, pieces);
		//If the board is full, the game is over, else it could be continue or over if only one player has pieces 
		if (board.isFull()){
			Piece winner = pieces.get(0);
			int playersPiecesMax = numPieces[0];
			boolean draw = false;
			for(int i = 1; i < pieces.size(); i++){
				if(playersPiecesMax == numPieces[i]){
					draw = true;
				}
				else if(playersPiecesMax < numPieces[i]){
					playersPiecesMax = numPieces[i];
					winner = pieces.get(i);
					draw = false;
					}
			}
			if (draw){
				return new Pair<State, Piece>(State.Draw, null);
			}
			else {
				return new Pair <State, Piece>(State.Won, winner);
			}
		}
		else {
			//Boolean to control that only one player has pieces left, if 2 or more players has pieces, the game continues
			boolean hasNoPieces = true;
			//Check that all players has pieces in the board
			for (int i = 0; i < pieces.size(); i++){
				//i is the player and j is a counter to see if all the other players hasnt got pieces, so the game is over
				for (int j = i + 1; j < i + pieces.size() && hasNoPieces; j++){
					if (numPieces[j % pieces.size()] > 0){
						hasNoPieces = false;
					}
				}
				//If only one player has pieces, he is the winner
				if (hasNoPieces){
					return new Pair <State, Piece>(State.Won, pieces.get(i));
				}
				hasNoPieces = true;
			}
			return gameInPlayResult;
		}
		
	}

	/**
	 * Count the number of pieces each player has in the board
	 * @param board The board in which the game is played
	 * @param playersPieces The list of players
	 * @return An array of int with all the pieces of each player, the index of List<Piece> is used for the array
	 */
	private int[] countPieces(Board board, List<Piece> playersPieces) {
		int[] numPieces = {0,0,0,0};
		for (int row = 0; row < dim; row++){
			for (int col = 0; col < dim; col++){
				Piece p = board.getPosition(row, col);
				int numPlayer = playersPieces.indexOf(p);
				//Obstacles dont count
				if (numPlayer != -1){
					numPieces[numPlayer]++;
				}
			}
		}
		return numPieces;
	}
	
	/**
	 * //Creates different obstacles, avoiding using the same name in a piece that a player or other obstacle uses 
	 * @param l The list of pieces
	 * @return A new obstacle (Piece)
	 */
	private Piece getObstPiece(List<Piece> l) {
		int i=0;
		while ( true ) {
			Piece o = new Piece("*#"+i);
			if ( !l.contains(o) ) return o;
			i++;
		}
	}

	@Override
	public Piece nextPlayer(Board board, List<Piece> playersPieces, Piece lastPlayer) {
		int nextPlayer = (playersPieces.indexOf(lastPlayer) + 1) % playersPieces.size();
		Piece p;
		boolean hasMoves = false;
		//The way is implemented updateState, the board won`t be full, so there won`t be an infinite loop
		while (!hasMoves){
			for (int row = 0; row < dim && !hasMoves; row++){
				for (int col = 0; col < dim && !hasMoves; col++){
					p = board.getPosition(row, col);
					//Looking for the moves the nextPlayer has, if there aren`t posible moves, the player next has the turn 
					if (p.equals(playersPieces.get(nextPlayer))){
						List<GameMove> moves = adyacentMoves(board, p, new ArrayList<GameMove>(), row, col);
						if (!moves.isEmpty()){
							hasMoves = true;
						}
					}
				}
			}
			//if the player has no moves, the turn goes to the next player
			if (!hasMoves){
				nextPlayer = (nextPlayer + 1) % playersPieces.size();
			}
		}
		return playersPieces.get(nextPlayer);
	}



	@Override
	//Hacer solo la lista de movimientos de la pieza pasada por parametro
	public List<GameMove> validMoves(Board board, List<Piece> playersPieces, Piece turn) {
		List<GameMove> moves = new ArrayList<GameMove>();
		for (int i = 0; i < board.getRows(); i++){
			for (int j = 0; j < board.getCols(); j++){
				//Si la pieza que pasamos está en esa posicion, miramos todos los casos
				if (board.getPosition(i, j).equals(turn)){
					moves = adyacentMoves(board, turn, moves, i, j);
				}
			}
		}
		return moves;
	}
	
	/**
	 * Check the posible moves of the piece given in a 2 square in any direction area 
	 * @param board The board in which the game is being played
	 * @param turn The piece which we are checking
	 * @param moves The list of moves we create
	 * @param row The row of the piece (turn) in the board 
	 * @param col The column of the piece (turn) in the board 
	 * @return The list of moves with all the posibilities
	 */
	private List<GameMove> adyacentMoves(Board board, Piece turn, List<GameMove> moves, int row, int col) {
		for (int k = 0; k < 5; k++){
			for (int l = 0; l < 5; l++){
				int newRow = row + k - 2, newCol = col + l - 2;
				//If the new position is in the board and has no piece, the position is added to the board
				if (newRow >= 0 && newRow < dim && newCol >= 0 && newCol < dim && board.getPosition(newRow, newCol) == null){
					moves.add(new AtaxxMove(row, col, newRow, newCol, turn));
				}
			}
		}
		return moves;
	}

	@Override
	public double evaluate(Board board, List<Piece> pieces, Piece turn, Piece p) {
		return 0;
	}
	
	
	
}
