package es.ucm.fdi.tp.practica5.swing;

import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.Utils;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class TimeOutPlayer extends Player {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Player player;
	private long timeOut;
	private volatile GameMove m;
	private Thread callerThread;
	private volatile Thread workerThread;
	
	public TimeOutPlayer(Player player, long timeout) {
		this.player = player;
		this.timeOut = timeout;
	}
	
	public void setTimeOut(long timeOut){
		this.timeOut = timeOut;
	}
	
	@Override
	public GameMove requestMove(Piece p, Board board, List<Piece> pieces,
			GameRules rules) {
		
		callerThread = Thread.currentThread();
		Utils.worker.execute(new Runnable() {
			public void run() {
				workerThread = Thread.currentThread();
				m = player.requestMove(p, board, pieces, rules);
				if (m != null){
					callerThread.interrupt();
				}
			};
		});
		
		
		try {
			Thread.sleep(timeOut);
			workerThread.interrupt();
			throw new GameError("Automatic player time out");
		} catch (InterruptedException e) {
			return m;
		}
		
	}

}
