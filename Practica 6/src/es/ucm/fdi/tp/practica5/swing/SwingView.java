package es.ucm.fdi.tp.practica5.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import es.ucm.fdi.tp.basecode.bgame.Utils;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.practica5.Main.PlayerMode;


public abstract class SwingView extends JFrame implements GameObserver {
	
	private Controller ctrl;
	/**
	 * Used for the multiviews, piece with is the view
	 */
	private Piece localPiece;
	private Piece turn;
	private Board board;
	private List<Piece> pieces;
	private Map<Piece, Color> pieceColors;
	private Map<Piece, PlayerMode> playerTypes;
	private String gameDesc;
	private JPanel ctrlPanel;
	private JPanel mainPanel;
	/**
	 * Used for the combo box affected by the set button
	 */
	private JComboBox<Piece> listPieces1;
	/**
	 * Used for the combo box affected by the choose colors button
	 */
	private JComboBox<Piece> listPieces2;
	private JTextArea storyArea;
	private MyTableModel myTable;
	private Player randPlayer;
	private Player aiPlayer;
	/**
	 * Used to disable or enable panel components whether is a movement or not
	 */
	private boolean buttonsDisabled;
	private Iterator<Color> itColor = Utils.colorsGenerator();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final long TIMEOUT = 5000;
	
	public SwingView(){
		
	}
	
	public SwingView(Observable<GameObserver> g, Controller c, Piece localPiece, Player randPlayer, Player aiPlayer) {
		super("Board Games: ");
		this.ctrl = c;
		this.localPiece = localPiece;
		this.playerTypes = new HashMap<>();
		this.pieceColors = new HashMap<>();
		this.randPlayer = randPlayer;
		this.aiPlayer = aiPlayer == null ? null : new TimeOutPlayer(aiPlayer, TIMEOUT);
		initGUI();
		g.addObserver(this);
	}
	
	class MyTableModel extends AbstractTableModel {

		/**
		* 
		*/
		private static final long serialVersionUID = 1L;

		private String[] colNames;

		public MyTableModel() {
			this.colNames = new String[] { "Player", "Mode", "#Pieces" };
		}

		@Override
		public String getColumnName(int col) {
			return colNames[col];
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			return pieces == null ? 0 : pieces.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return pieces.get(rowIndex);
			}
			else if (columnIndex == 1){
				//Shows only the mode to the correct view
				if (localPiece == null || localPiece.equals(pieces.get(rowIndex))){
					return playerTypes.get(pieces.get(rowIndex));					
				}
				else{
					return null;
				}
			}
			else {
				return board.getPieceCount(pieces.get(rowIndex));
			}
		}
		
		public void refresh() {
			fireTableStructureChanged();
			fireTableDataChanged();
		}

	};
	
	
	/**
	 * Construct the view
	 */
	private void initGUI(){ 
		this.setSize(400, 200);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//Set the panel to be the one of the swingView
		mainPanel = new JPanel( new BorderLayout(5,5) );
		this.setContentPane(mainPanel);
		
		ctrlPanel = new JPanel();
		//Alignment to ensure all the components are in the Y axis
		ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));
		
		addStatusMessages();
		addPlayerInformation();
		addPieceColors();
		addPlayerModes();
		//If there is no random or ai player, there is no need to create an automatic moves panel
		if (randPlayer != null || aiPlayer != null){
			addAutomaticMoves();
		}
		addQuitRestartButtons();
		mainPanel.add(ctrlPanel, BorderLayout.LINE_END);
		initBoardGui();
		this.setVisible(true);
	}
	
	/**
	 * Used to call ctrl.makeMove(player) with a swingWorker to avoid interruption on the main thread
	 * @param player The player you want to make the move
	 */
	private void makeMove(Player player){
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				ctrl.makeMove(player);
				return null;
			}
			
		};
		worker.execute();
	}
	
	/**
	 * Add the random and intelligent button into ctrlPanel
	 */
	private void addAutomaticMoves() {
		JPanel movesPanel = new JPanel();
		//Random Button
		
		if (randPlayer != null){
			JButton random = new JButton("Random");
			random.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//If the mode is not manual or is a movement or the player is not in his turn, the button does nothing
					if (turn != null && (localPiece == null || localPiece.equals(turn) && playerTypes.get(turn).equals(PlayerMode.MANUAL) && !buttonsDisabled)){
						makeMove(randPlayer);
					}
				}
			});
			movesPanel.add(random);	
		}
		if (aiPlayer != null){
			//Intelligent Button
			JButton intelligent = new JButton("Intelligent");
			intelligent.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//If the mode is not manual or is a movement or the player is not in his turn, the button does nothing
					if (turn != null && (localPiece == null || localPiece.equals(turn)) && playerTypes.get(turn).equals(PlayerMode.MANUAL) && !buttonsDisabled){
						
						makeMove(aiPlayer);
					}
				}
			});
			movesPanel.add(intelligent);
		}
		
		//Border
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		movesPanel.setBorder(BorderFactory.createTitledBorder(b, "Automatic Modes"));
		ctrlPanel.add(movesPanel);
	}
	
	/**
	 * Name modes, based of player modes (manual, random or intelligent.)
	 * <p>
	 * Modos de juego, badado en player modes.
	 */
	
	 enum nameModes {
		MANUAL(PlayerMode.MANUAL, "Manual"), INTELLIGENT(PlayerMode.AI, "Intelligent"), RANDOM(PlayerMode.RANDOM, "Random");

		private PlayerMode mode;
		private String desc;

		nameModes(PlayerMode mode, String desc) {
			this.mode = mode;
			this.desc = desc;
		}
		
		public PlayerMode getPlayerMode(){
			return this.mode;
		}

		public String getDesc() {
			return this.desc;
		}
		
		@Override
		public String toString() {
			return this.desc;
		}
	}
	
	/**
	 * Add a JPanel which contains the players pieces, the player modes and a set button to change between modes into ctrlPanel
	 */
	private void addPlayerModes() {
		//Players ComboBox
		JPanel optionsPanel = new JPanel();
		listPieces1 = new JComboBox<Piece>();
		optionsPanel.add(listPieces1);
		
		JComboBox<nameModes> listModes = new JComboBox<>();
		listModes.addItem(nameModes.MANUAL);
		if (randPlayer != null){
			listModes.addItem(nameModes.RANDOM);
		}
		if (aiPlayer != null){
			listModes.addItem(nameModes.INTELLIGENT);
		}
		optionsPanel.add(listModes);
		//Set Button
		JButton set = new JButton("Set");
		set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//All the items in the listPieces are of the Piece type, so the casting wont fail
				Piece p = (Piece) listPieces1.getSelectedItem();
				nameModes mode = (nameModes) listModes.getSelectedItem();
				playerTypes.put(p, mode.getPlayerMode());
				myTable.refresh();
				//If the mode is not manual and the button is not disabled
				if (!mode.equals(nameModes.MANUAL) && !buttonsDisabled){
					deActivateBoard();
					decideMakeAutomaticMove();
				}
			}
		});
		optionsPanel.add(set);
		//Border
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		optionsPanel.setBorder(BorderFactory.createTitledBorder(b, "Player Modes"));
		ctrlPanel.add(optionsPanel);
	}
	
	/**
	 * Add a JPanel which contains the players pieces and the chooseColor button into ctrlPanel
	 */
	private void addPieceColors() {
		//Players ComboBox
		JPanel optionsPanel = new JPanel();
		listPieces2 = new JComboBox<Piece>();
		optionsPanel.add(listPieces2);
		
		JButton chooseColor = new JButton("Choose Color");
		chooseColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//All the items in the listPieces are of the Piece type, so the casting wont fail
				Piece p = (Piece)listPieces2.getSelectedItem();
				ColorChooser c = new ColorChooser(new JFrame(), "Choose Line Color", pieceColors.get(p));
				if (c.getColor() != null) {
					pieceColors.put(p, c.getColor());
					repaint();
				}
			}
		});
		optionsPanel.add(chooseColor);
		//Border
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		optionsPanel.setBorder(BorderFactory.createTitledBorder(b, "Piece Colors"));
		ctrlPanel.add(optionsPanel);
	}

	/**
	 * Add a ScrollPane which contains a table with the players, mode and number of pieces they have into ctrlPanel
	 */
	private void addPlayerInformation() {
		
		myTable = new MyTableModel();
		JTable table = new JTable(myTable) {
			private static final long serialVersionUID = 1L;

			// THIS IS HOW WE CHANGE THE COLOR OF EACH ROW
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				// the color of row 'row' is taken from the colors table, if
				// 'null' setBackground will use the parent component color.
				comp.setBackground(pieceColors.get(pieces.get(row)));
				return comp;
			}
		};
		
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		JScrollPane scrollPane = new JScrollPane(table);
		//Used to resize the scrollPane because its too big by default
		scrollPane.setPreferredSize(new Dimension(300, 80));
		
		scrollPane.setBorder(BorderFactory.createTitledBorder(b, "Player Information"));
		ctrlPanel.add(scrollPane);
		
	}
	
	/**
	 * Adds the TextArea (usar JTextArea) where the messages are gonna be displayed
	 */
	private void addStatusMessages() {
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		storyArea = new JTextArea();
		storyArea.setEditable(false);
		storyArea.setLineWrap(true);
		storyArea.setWrapStyleWord(true);
		JScrollPane area = new JScrollPane(storyArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		area.setPreferredSize(new Dimension(300, 100));
		area.setMinimumSize(new Dimension(200, 100));
		area.setBorder(BorderFactory.createTitledBorder(b, "Status Messages"));
		ctrlPanel.add(area);
	}
	

	/**
	 * Add a JPanel which contains the quit and restart button into ctrlPanel
	 */
	private void addQuitRestartButtons() {
		JPanel p = new JPanel();
		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { quit(); }
		});
		
		this.addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) { 
				
				if (playerTypes.get(turn).equals(PlayerMode.MANUAL)){
					quit(); 
				}
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
				
		p.add(quit);
		//Restart button is only allowed in single view, not in multiviews
		if (localPiece == null){
			JButton restart = new JButton("Restart");
			restart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//If the turn is manual and the button is not disabled or if the game is over 
					//(turn = null in OnGameOver) we can restart
					if (turn == null || (playerTypes.get(turn).equals(PlayerMode.MANUAL) && !buttonsDisabled)){
						activateBoard();
						addMsg("Restarting the game\n");
						ctrl.restart();
					}
				}
			});
			p.add(restart);
		}
		ctrlPanel.add(p);
	}

	final protected Piece getTurn() { return turn; }
	final protected Board getBoard() { return board; }
	final protected List<Piece> getPieces() { return pieces; }
	final protected Color getPieceColor(Piece p) { return pieceColors.get(p); }
	final protected Color setPieceColor(Piece p, Color c) { return pieceColors.put(p,c); }
	final protected void setBoardArea(JComponent c) { 
		mainPanel.add(c,BorderLayout.CENTER);
		this.pack();
	}
	final protected void addMsg(String msg){ 
		storyArea.append(msg);
	}
	
	final protected void decideMakeManualMove(Player manualPlayer) { 
		//If the move is null it throws an exception, we just catch it and do nothing casue there isnt a move 
		try{
			makeMove(manualPlayer);			
		}
		catch (Exception e){
		}
	}
	
	/**
	 * It will make the move only if the player is not manual and the , else it wont do anything
	 */
	private void decideMakeAutomaticMove() { 
		//If the player belong to this view 
		if (turn != null && (localPiece == null || localPiece.equals(turn))){
			//and the mode is random or AI, we make the move
			if (playerTypes.get(turn).equals(PlayerMode.RANDOM) && !buttonsDisabled){
				deActivateBoard();
				buttonsDisabled = true;
				//If the move is null it throws an exception, we just catch it and do nothing casue there isnt a move 
				try{			
					makeMove(randPlayer);
				}
				catch (Exception e){
				}
				activateBoard();
			}
			else if (playerTypes.get(turn).equals(PlayerMode.AI) && !buttonsDisabled){
				deActivateBoard();
				buttonsDisabled = true;
				//If the move is null it throws an exception, we just catch it and do nothing casue there isnt a move 
				try{			
					makeMove(aiPlayer);

				}
				catch (Exception e){
				}
				activateBoard();
			}
		}
		
	}
	
	protected abstract void initBoardGui();
	protected abstract void activateBoard();
	protected abstract void deActivateBoard();
	protected abstract void redrawBoard();

	private void quit() {
		int n = JOptionPane.showOptionDialog(new JFrame(), "Are sure you want to quit?", "Quit",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (n == 0) {
			//finalize your app
			System.exit(0);
		}
	}
	
	// GameObserver Methods
	public void onGameStart(final Board board, final String gameDesc, final List<Piece> pieces, final Piece turn) {
		this.pieces = pieces;
		this.board = board;
		this.turn = turn;
		this.gameDesc = gameDesc;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				handleGameStart(); 
			}
			
		});
	}
	
	/**
	 * Adds the main boxes and elements in the panel previously made by initGUI
	 */
	private void handleGameStart() {
		//HashMap used for playerTypes		
		listPieces1.removeAllItems();
		listPieces2.removeAllItems();
		for(Piece p : pieces) {
			listPieces2.addItem(p);
			if (localPiece == null || p.equals(localPiece)){
					listPieces1.addItem(p);				
			}
			SwingView.this.playerTypes.put(p, PlayerMode.MANUAL);
			//Put random colors at the start
			SwingView.this.pieceColors.put(p, itColor.next());
		}
		//Only the player who has the turn can move a piece, in this case the first player
		if (localPiece != null && !localPiece.equals(pieces.get(0))){
			deActivateBoard();
		}
		listPieces1.setSelectedIndex(0);
		listPieces2.setSelectedIndex(0);
		storyArea.setText("");
		String story = "Turn for ";
		if (pieces.get(0).equals(localPiece)){
			story += "You! ";
		}
		story += pieces.get(0).toString() + "\n";
		storyArea.append(story);
		if (localPiece == null){
			SwingView.super.setTitle("Board Games: " + gameDesc);
		}
		else{
			SwingView.super.setTitle("Board Games: " + gameDesc + " (" + localPiece + ")");					
		}
		buttonsDisabled = false;
		myTable.refresh();
		redrawBoard();
	}
	
	
	public void onGameOver(final Board board, final State state, final Piece winner) {
		this.board = board;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				handleOnGameOver(state, winner); 
			}
		});
	}
	
	private void handleOnGameOver(final State state, final Piece winner) {
		//The game is over, so there is no turn for anyone
		this.turn = null;
		deActivateBoard();
		redrawBoard();
		storyArea.append("Game Over!!\n");
		storyArea.append("Game Status: " + state + "\n");
		//Shows the winner in the view
		if (localPiece == null){
			if (winner != null)
				storyArea.append(winner + " have won, congratulations!\n");
			else {
				storyArea.append("You have draw, try it again!\n");
			}
		}
		//In case of multiviews, shows different messages
		else {
			if (state.equals(State.Draw)){
				storyArea.append("You have draw, try it again!\n");
			}
			//If someone has won and the piece is part of the player, show him the victory message
			else if (state.equals(State.Won) && localPiece.equals(winner)){
				storyArea.append("(You!) " + winner +  " have won, congratulations!\n");
			}
			else {
				storyArea.append("You have lost, try it again!\n");
			}
		}


	}

	public void onMoveStart(Board board, Piece turn) {
		this.board = board;
		this.turn = turn;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				handleOnMoveStart(); 
			}
		});
	}
	
	
	private void handleOnMoveStart() {
		deActivateBoard();
		buttonsDisabled = true;
	}

	public void onMoveEnd(Board board, Piece turn, boolean success) {
		this.board = board;
		this.turn = turn;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				handleOnMoveEnd(); 
			}
		});
	}
	
	private void handleOnMoveEnd() {
		buttonsDisabled = false;
	}

	public void onChangeTurn(Board board, final Piece turn) {
		this.board = board;
		this.turn = turn;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				handleOnChangeTurn(); 
			}
		});
	}
	
	private void handleOnChangeTurn() {
		String story = "Turn for ";
		if (localPiece == null || turn.equals(localPiece)){
			story += "You! ";
			activateBoard();
		}
		story += turn + "\n";
		addMsg(story);
		redrawBoard();
		decideMakeAutomaticMove();
	}

	public void onError(String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				handleOnError(msg);
			}
		});
	}
	
	private void handleOnError(String msg) {
		if (localPiece == null || turn.equals(localPiece)){
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
			addMsg(turn + " ");
			activateBoard();
			//If an error happens, the mode of the player is returned to manual
			playerTypes.put(turn, PlayerMode.MANUAL);
		}					
	}

}
