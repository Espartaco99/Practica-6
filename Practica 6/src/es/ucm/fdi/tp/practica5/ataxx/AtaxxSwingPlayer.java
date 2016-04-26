package es.ucm.fdi.tp.practica5.ataxx;
import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.practica4.ataxx.AtaxxMove;

public class AtaxxSwingPlayer extends Player {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int rowOrigin;
	private int colOrigin;
	private int rowDest;
	private int colDest;

	public void setMove(int rowOrigin, int colOrigin, int rowDest, int colDest) {
		this.rowOrigin = rowOrigin;
		this.colOrigin = colOrigin;
		this.rowDest = rowDest;
		this.colDest = colDest;
	}

	@Override
	public GameMove requestMove(Piece p, Board board, List<Piece> pieces,
			GameRules rules) {
		return new AtaxxMove(rowOrigin,colOrigin,rowDest, colDest, p);
	}
}
