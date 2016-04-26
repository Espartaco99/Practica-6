package es.ucm.fdi.tp.practica5.ataxx;

import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.practica4.ataxx.AtaxxFactory;

public class AtaxxFactoryExt extends AtaxxFactory {
	
	public AtaxxFactoryExt() {
		super();
	}

	public AtaxxFactoryExt(int obs, boolean dummy) {
		super(obs, dummy);
	}

	public AtaxxFactoryExt(int dim, int obs) {
		super(dim, obs);
	}

	public AtaxxFactoryExt(int dim) {
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
				new AtaxxSwingView(g,c,viewPiece, random, ai);
			}
		});
	}
}
