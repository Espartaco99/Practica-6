package es.ucm.fdi.tp.practica5.swing;


import java.awt.Color;
import java.util.List;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public abstract class RectBoardSwingView extends SwingView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Color OBSTACLES_COLOR = Color.ORANGE;
	private BoardComponent boardComp;
	private boolean mouseActive = true;
	
	public RectBoardSwingView(Observable<GameObserver> g, Controller c, Piece localPiece, Player random, Player ai) {
		super(g, c, localPiece, random, ai);
	}
	@Override
	protected void initBoardGui() {
		boardComp = new BoardComponent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			protected void mouseClicked(int row, int col, int mouseButton) {
				//MouseButton: 1 = left click, 2 = middle click, 3 = right click
				handleMouseClick(row,col,mouseButton);
			}
			@Override
			protected Color getPieceColor(Piece p) {
				//Color for obstacles
				if (RectBoardSwingView.this.getPieceColor(p) == null){
					return OBSTACLES_COLOR;
				}
				else{
					return RectBoardSwingView.this.getPieceColor(p);
				}
			};
			
			@Override
			protected boolean isPlayerPiece(Piece p) {
				List<Piece> pieces = RectBoardSwingView.this.getPieces();
				//If the piece is not on the list is an obstacle
				for (int i =0; i < pieces.size(); i++){
					//If the piece is on the list, we stop the iteration and return true
					if (p.equals(pieces.get(i))){
						return true;
					}
				}
				return false;
			}

		};
		
		setBoardArea(boardComp); // install the board in the view
	}
	@Override
	protected void redrawBoard() {
		boardComp.redraw(getBoard()); 
	}
	
	protected abstract void handleMouseClick(int row, int col, int mouseButton);
	

	@Override
	protected void activateBoard() {
		mouseActive = true;
	}

	@Override
	protected void deActivateBoard(){
		mouseActive = false;
	}
	
	protected boolean getMouseActive(){
		return mouseActive;
	}

}
