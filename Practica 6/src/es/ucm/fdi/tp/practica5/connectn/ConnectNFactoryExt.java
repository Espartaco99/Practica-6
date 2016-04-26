package es.ucm.fdi.tp.practica5.connectn;



import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.connectn.ConnectNFactory;

public class ConnectNFactoryExt extends ConnectNFactory {
	
	
	public ConnectNFactoryExt() {
		super();
	}

	public ConnectNFactoryExt(int dim) {
		super(dim);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void createSwingView(final Observable<GameObserver> g, final Controller c, final Piece viewPiece,
			Player random, Player ai) {
		SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new ConnectNSwingView(g,c,viewPiece, random, ai);
				}
			});
	}
}
