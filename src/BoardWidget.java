/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Kyung-Wan Woo
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);

        g.setColor(LINE_COLOR);
        for (int i = 0; i <= SIDE; i++) {
            g.setStroke(LINE_STROKE);
            g.drawLine(SQDIM * i, 0, SQDIM * i, SQDIM * SIDE);
        }
        for (int j = 0; j <= SIDE; j++) {
            g.drawLine(0, SQDIM * j, SQDIM * SIDE, SQDIM * j);
        }
        if (_selectedCol != 0) {
            g.setColor(SELECTED_COLOR);
            int selC = SQDIM  * (_selectedCol - 'a');
            int selR = SQDIM * 6 - SQDIM  * (_selectedRow - '1');
            g.fillRect(selC, selR, SQDIM, SQDIM);
        }
        for (char col = 'a'; col < 'h'; col++) {
            for (char row = '1'; row < '8'; row++) {
                int c = SQDIM  * (col - 'a');
                int r = SQDIM  * (row - '1');
                int ro = (SQDIM * 7) - r - (4 * SQDIM / 5);
                int t = SQDIM / 5;
                int tw = SQDIM * 2 / 5;
                int f = SQDIM * 4 / 5;
                int h = SQDIM / 2;
                int th = SQDIM * 6;
                if (_model.get(col, row) == RED) {
                    g.setColor(RED_COLOR);
                    g.fillOval(c + t, ro, PIECE_RADIUS * 2, PIECE_RADIUS * 2);
                } else if (_model.get(col, row) == BLUE) {
                    g.setColor(BLUE_COLOR);
                    g.fillOval(c + t, ro, PIECE_RADIUS * 2, PIECE_RADIUS * 2);
                } else if (_model.get(col, row) == BLOCKED) {
                    g.setColor(BLOCK_COLOR);
                    int coB = c + SQDIM / 10;
                    int roB = th - r + SQDIM / 10;
                    g.fillRect(coB, roB, BLOCK_WIDTH, BLOCK_WIDTH);
                    g.setColor(BLANK_COLOR);
                    int coI = c + t;
                    int roI = th - r + t;
                    g.fillRect(coI, roI, 3 * SQDIM / 5, 3 * SQDIM / 5);
                    g.setColor(BLOCK_COLOR);
                    int coO = c + tw;
                    int roO = th - r + tw;
                    g.fillOval(coO, roO, SQDIM / 5, SQDIM / 5);
                    g.setStroke(BLOCK_STROKE);
                    g.drawLine(c + t, th - r + t, c + f, th - r + f);
                    g.drawLine(c + f, th - r + t, c + t, th - r + f);
                    g.drawLine(c + h, th - r + t, c + h, th - r + f);
                    g.drawLine(c + t, th - r + h, c + f, th - r + h);
                }
            }
        }
    }

    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        g.setColor(BLOCK_COLOR);

        char[] col = {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
        char[] row = {'1', '2', '3', '4', '5', '6', '7'};

        char x = col[(cx - SQDIM / 2) / SQDIM];
        char y = row[(cy - SQDIM / 2) / SQDIM];

        _model.setBlock(x, y);
    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    _commandQueue.offer("block " + mouseCol + mouseRow);
                } else {
                    if (_selectedCol != 0) {
                        String half = "" + _selectedCol + _selectedRow;
                        _commandQueue.offer(half + "-" + mouseCol + mouseRow);
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
