package es.ucm.fdi.tp.practica5.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public abstract class BoardComponent extends JComponent {
	private int _CELL_HEIGHT = 50;
	private int _CELL_WIDTH = 50;
	private int rows;
	private int cols;
	private Board board;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BoardComponent() {
		initGUI();
	}

	public void redraw(Board b) {
		this.board = b;
		this.cols = b.getCols();
		this.rows = b.getRows();
		repaint();
	}

	private void initGUI() {
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("Mouse Released");
			}

			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("Mouse Pressed");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				System.out.println("Mouse Exited Component");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				System.out.println("Mouse Entered Component");
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				//Debug messages
				System.out.println("Button" + e.getButton() + " Clicked at"
						+ "(" + e.getX() + "," + e.getY() + ")");
				System.out.println(e.getY()/  _CELL_HEIGHT + " " + e.getX() /  _CELL_WIDTH);
				//The important call of the function
				BoardComponent.this.mouseClicked(e.getY() / _CELL_HEIGHT, e.getX() / _CELL_WIDTH, e.getButton());
			}
		});
		this.setPreferredSize(new Dimension(400,400));
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if ( board == null ) return;
		_CELL_WIDTH = this.getWidth() / cols;
		_CELL_HEIGHT = this.getHeight() / rows;
		//Used to make the border of the oval more appealing to the eyes, using the antialising
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				drawCell(i, j, g);
	}

	private void drawCell(int row, int col, Graphics g) {
		int x = col * _CELL_WIDTH;
		int y = row * _CELL_HEIGHT;
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x + 2, y + 2, _CELL_WIDTH - 4, _CELL_HEIGHT - 4);
		Piece p = board.getPosition(row, col);
		if (board != null && p != null) {
			//Draw Pieces
			if (isPlayerPiece(p)){
				Color c = getPieceColor(p);
				g.setColor(c);
				g.fillOval(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				g.setColor(Color.black);
				g.drawOval(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
			}
			//Draw Obstacles
			else {
				Color c = getPieceColor(p);
				g.setColor(c);
				g.fillRect(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				g.setColor(Color.black);
				g.drawRect(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
			}
		}
	}

	protected abstract Color getPieceColor(Piece p);

	protected abstract boolean isPlayerPiece(Piece p);

	protected abstract void mouseClicked(int row, int col, int mouseButton);
}
