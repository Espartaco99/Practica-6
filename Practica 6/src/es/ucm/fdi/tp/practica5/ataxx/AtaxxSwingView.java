package es.ucm.fdi.tp.practica5.ataxx;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.practica5.swing.RectBoardSwingView;

public class AtaxxSwingView extends RectBoardSwingView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AtaxxSwingPlayer player;
	private boolean hasFirstClick; 
	private int rowOrigin;
	private int colOrigin;
	
	
	public AtaxxSwingView(Observable<GameObserver> g, Controller c, Piece localPiece, Player random, Player ai) {
		super(g, c, localPiece, random, ai);
		this.hasFirstClick = false;
		player = new AtaxxSwingPlayer();
	}

	@Override
	protected void handleMouseClick(int row, int col, int mouseButton) {
		//Allow the moves of the mouse get captured, used in case the state or phase of the game dont allow us make a move
		if (getMouseActive()){
			//If is the second click, i move
			if (hasFirstClick) {
				//Reset the value of the field for the next move
				hasFirstClick = false;
				//If we click right mouse, we cancel the move
				if (mouseButton != 3){
					player.setMove(rowOrigin, colOrigin, row, col);
					decideMakeManualMove(player);
				}
				else {
					addMsg("Movement canceled\n");
				}
			} else {
				//First Click
				rowOrigin = row;
				colOrigin = col;
				hasFirstClick = true;
				addMsg("Left Click on the destination position, right click to cancel\n");
			}
		}
	}
	
	@Override
	protected void activateBoard() {
	// - add corresponding message to the status messages indicating
	// what to do for making a move, etc.
		super.activateBoard();
		addMsg("Click on an origin piece\n");
	}
}
