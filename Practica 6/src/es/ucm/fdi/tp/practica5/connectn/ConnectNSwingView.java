package es.ucm.fdi.tp.practica5.connectn;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.practica5.swing.RectBoardSwingView;

public class ConnectNSwingView extends RectBoardSwingView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectNSwingPlayer player;
	
	public ConnectNSwingView(Observable<GameObserver> g, Controller c, Piece localPiece, Player random, Player ai) {
		super(g, c, localPiece, random, ai);
		player = new ConnectNSwingPlayer();
		
	}

	@Override
	protected void handleMouseClick(int row, int col, int mouseButton) {
		//Allow the moves of the mouse get captured, used in case the state or phase of the game dont allow us make a move
		if (getMouseActive()){
			player.setMove(row, col);
			decideMakeManualMove(player);
		}
		
		
	}
	//I dont override deActivateBoard cause both do the same
	@Override
	protected void activateBoard() {
	// - add corresponding message to the status messages indicating
	// what to do for making a move, etc.
		super.activateBoard();
		addMsg("Click on an empty cell\n");
	}
	
}
