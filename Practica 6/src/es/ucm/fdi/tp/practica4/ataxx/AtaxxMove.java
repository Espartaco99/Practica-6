package es.ucm.fdi.tp.practica4.ataxx;

import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

/**
 * A Class representing a move for Attax.
 * 
 * <p>
 * Clase para representar un movimiento del juego Attax.
 * 
 */
public class AtaxxMove extends GameMove {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int rowOrigin;
	protected int colOrigin;
	/**
	 * The rowOrigin where to place the piece return by {@link GameMove#getPiece()}.
	 * <p>
	 * Fila en la que se coloca la ficha devuelta por
	 * {@link GameMove#getPiece()}.
	 */
	protected int rowDest;

	/**
	 * The column where to place the piece return by {@link GameMove#getPiece()}
	 * .
	 * <p>
	 * columna en la que se coloca la ficha devuelta por
	 * {@link GameMove#getPiece()}.
	 */
	protected int colDest;

	/**
	 * This constructor should be used ONLY to get an instance of
	 * {@link AtaxxMove} to generate game moves from strings by calling
	 * {@link #fromString(String)}
	 * 
	 * <p>
	 * Solo se debe usar este constructor para obtener objetos de
	 * {@link AtaxxMove} para generar movimientos a partir de strings usando
	 * el metodo {@link #fromString(String)}
	 * 
	 */

	public AtaxxMove() {
	}

	/**
	 * Constructs a move for placing a piece of the type referenced by {@code p}
	 * at position ({@code rowOrigin},{@code colOrigin}).
	 * 
	 * <p>
	 * Construye un movimiento para colocar una ficha del tipo referenciado por
	 * {@code p} en la posicion ({@code rowOrigin},{@code colOrigin}).
	 * 
	 * @param rowOrigin
	 *            Number of row.
	 *            <p>
	 *            Numero de fila.
	 * @param colOrigin
	 *            Number of column.
	 *            <p>
	 *            Numero de columna.
	 * @param p
	 *            A piece to be place at ({@code rowOrigin},{@code colOrigin}).
	 *            <p>
	 *            Ficha a colocar en ({@code rowOrigin},{@code colOrigin}).
	 */
	public AtaxxMove(int rowOrigin, int colOrigin, int rowDest,int colDest, Piece p) {
		super(p);
		this.rowOrigin = rowOrigin;
		this.colOrigin = colOrigin;
		this.rowDest = rowDest;
		this.colDest = colDest;
	}
	
	
	@Override
	public void execute(Board board, List<Piece> pieces) {
		Piece p = board.getPosition(rowOrigin, colOrigin);
		//La pieza que mueves tiene que ser una de tu turno
		if (getPiece().equals(p)){
			if (p != null && !pieces.equals(p)){
				if (rowDest >= 0 && rowDest < board.getRows() && colDest >= 0 && colDest < board.getCols() 
						&& board.getPosition(rowDest, colDest) == null){
					int dist = Math.max(Math.abs(rowDest - rowOrigin), Math.abs(colDest - colOrigin));
					//Muevo 1 casilla la ficha
					if (dist <= 2){
						board.setPosition(rowDest, colDest, getPiece());
						//Muevo 2 casillas la pieza y la elimino de su posicion original
						if (dist == 2){
							board.setPosition(rowOrigin, colOrigin, null);
						}
					
					changePieces(board, pieces);
					}
					else {
						throw new GameError("Position (" + rowDest + "," + colDest + ") is 3 or more squares away from its original position!");
					}
				}
				else {
					throw new GameError("Position (" + rowDest + "," + colDest + ") is occupied by other piece or it´s out of range of the board!");
				}
			} 
			else {
				throw new GameError("Position (" + rowOrigin + "," + colOrigin + ") has no pieces or it´s an obstacle!");
			}
		}
		else {
			throw new GameError("Position (" + rowOrigin + "," + colOrigin + ") has a piece which is not yours!");
		}
	}
	
	/**
	 * Executes the change of pieces caused by the movement and flow of the game
	 * @param board A board on which the move operates. 
	 * @param pieces A list of pieces that are involved in the game. 
	 */
	
	private void changePieces(Board board, List<Piece> pieces) {
		Piece p;
		//Mira todas las posiciones adyacentes
		for (int rowAux = rowDest - 1; rowAux < rowDest + 2; rowAux++){
			for (int colAux = colDest - 1; colAux < colDest + 2; colAux++){
				if (rowAux >= 0 && rowAux < board.getRows() && colAux >= 0 && colAux < board.getCols()){
					p = board.getPosition(rowAux, colAux);
					//If the piece in the square is diferent from the one is playing, not null and is on the list of players
					//this piece is captured and changed to be one of the player pieces
					if (!getPiece().equals(p) && p != null && pieces.indexOf(p) != -1){
						board.setPosition(rowAux, colAux, getPiece());
					}
				}
			}
		}
	}
	
	
	/**
	 * This move can be constructed from a string of the form "rowOrigin SPACE colOrigin"
	 * where rowOrigin and colOrigin are integers representing a position.
	 * 
	 * <p>
	 * Se puede construir un movimiento desde un string de la forma
	 * "rowOrigin SPACE colOrigin" donde rowOrigin y colOrigin son enteros que representan una casilla.
	 */
	@Override
	public GameMove fromString(Piece p, String str) {
		String[] words = str.split(" ");
		if (words.length != 4) {
			return null;
		}

		try {
			int rowOrigin, colOrigin, rowDest, colDest;
			rowOrigin = Integer.parseInt(words[0]);
			colOrigin = Integer.parseInt(words[1]);
			rowDest = Integer.parseInt(words[2]);
			colDest = Integer.parseInt(words[3]);
			return createMove(rowOrigin, colOrigin, rowDest, colDest, p);
		} catch (NumberFormatException e) {
			return null;
		}

	}

	/**
	 * Creates a move that is called from {@link #fromString(Piece, String)}.
	 * Separating it from that method allows us to use this class for other
	 * similar games by overriding this method.
	 * 
	 * <p>
	 * Crea un nuevo movimiento con la misma ficha utilizada en el movimiento
	 * actual. Llamado desde {@link #fromString(Piece, String)}; se separa este
	 * metodo del anterior para permitir utilizar esta clase para otros juegos
	 * similares sobrescribiendo este metodo.
	 * 
	 * @param rowOrigin
	 *            rowOrigin of the move being created.
	 *            <p>
	 *            Fila del nuevo movimiento.
	 * 
	 * @param colOrigin
	 *            colOriginumn of the move being created.
	 *            <p>
	 *            colOriginumna del nuevo movimiento.
	 */
	protected GameMove createMove(int rowOrigin, int colOrigin,int rowDest,int colDest, Piece p) {
		return new AtaxxMove(rowOrigin, colOrigin, rowDest, colDest, p);
	}

	@Override
	public String help() {
		return "'rowOrigin columnOrigin rowDestiny colDestiny', to place a piece at the corresponding position.";
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtaxxMove other = (AtaxxMove) obj;
		if (colDest != other.colDest)
			return false;
		if (colOrigin != other.colOrigin)
			return false;
		if (rowDest != other.rowDest)
			return false;
		if (rowOrigin != other.rowOrigin)
			return false;
		return getPiece().equals(other.getPiece());
	}

	@Override
	public String toString() {
		if (getPiece() == null) {
			return help();
		} else {
			return "Place a piece '" + getPiece() + "' at (" + rowDest + "," + colDest + ")";
		}
	}
}
