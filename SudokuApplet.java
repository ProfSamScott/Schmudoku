import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import SudokuEngine.Board_type;
import SudokuEngine.HCBReturnValues;
import SudokuEngine.SFBReturnValues;
import SudokuEngine.SolverReturnValues;
import SudokuEngine.Sudoku2;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class SudokuApplet extends JApplet {

	public static void main(String[] args) {
		JFrame f = new JFrame("Schmudoku");
		SudokuApplet s = new SudokuApplet();
		s.init();
		s.setPreferredSize(new Dimension(470,570));
		f.setContentPane(s);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.pack();
		f.setVisible(true);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 2378699901799242154L;
	
	public static final boolean debug = true;
	
	/**
	 * The board (internal rep)
	 */
	Board_type board = new Board_type();
	/**
	 * The Cell array
	 */
	Cell[][] cell = new Cell[9][9];
	private final String[] difficulties = { "Very Easy", "Easy", "Moderate",
			"Hard", "Very Hard" };
	private boolean solving = false;
	private JApplet me = this;
	/**
	 * Button
	 */
	JButton newBoardButton, solveButton, clearButton, undoButton, redoButton;
	/**
	 * Check Box
	 */
	JCheckBox lockButton, errorButton;
	/**
	 * Combo Box
	 */
	JComboBox newBoardSelector;
	/**
	 * Label
	 */
	JLabel announce1, announce2;

	private final int historySize = 201; // actual history size will be 1
	// less
	private int nextHistoryPointer; // next location to capture board to
	private int startHistoryPointer; // first stored board
	private int endHistoryPointer; // last stored board
	private int currentHistoryPointer; // currently displayed board

	private class BoardHistory {
		Cell[][] cell = new Cell[9][9];
		boolean lockButton, errorButton;
		String announce1, announce2;

		public BoardHistory() {
			for (int x = 0; x <= 8; x++)
				for (int y = 0; y <= 8; y++)
					cell[x][y] = new Cell();
		}
	}

	private BoardHistory[] boardHistory = new BoardHistory[historySize];
	private final Dimension buttonSize = new Dimension(70, 20);
	private final Dimension buttonHalfSize = new Dimension(50, 20);

	/**
	 * Create gui
	 */
	public void init() {
		JPanel mainPanel = makeGUI();
		setContentPane(mainPanel);
		for (int h = 0; h < historySize; h++)
			boardHistory[h] = new BoardHistory();
		new NewBoardButtonListener(3);
	}

	public void initBoardHistory() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].copyOut(boardHistory[0].cell[x][y]);
		nextHistoryPointer = 1;
		startHistoryPointer = 0;
		endHistoryPointer = 0;
		currentHistoryPointer = 0;
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
	}

	public int incrementHistoryPointer(int historyPointer) {
		return (historyPointer + 1) % historySize;
	}

	public int decrementHistoryPointer(int historyPointer) {
		int newPointer = historyPointer - 1;
		if (newPointer < 0)
			newPointer += historySize;
		return newPointer;
	}

	public void captureBoardHistory() {
		// capture current to next location
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].copyOut(boardHistory[nextHistoryPointer].cell[x][y]);
		boardHistory[nextHistoryPointer].lockButton = lockButton.isSelected();
		boardHistory[nextHistoryPointer].errorButton = errorButton.isSelected();
		boardHistory[nextHistoryPointer].announce1 = announce1.getText();
		boardHistory[nextHistoryPointer].announce2 = announce2.getText();
		// change end, current, and next pointers
		endHistoryPointer = nextHistoryPointer;
		currentHistoryPointer = nextHistoryPointer;
		nextHistoryPointer = incrementHistoryPointer(nextHistoryPointer);
		// change start pointer if we wrap
		if (startHistoryPointer == endHistoryPointer)
			startHistoryPointer = incrementHistoryPointer(startHistoryPointer);
		// set undo/redo buttons
		undoButton.setEnabled(true);
		redoButton.setEnabled(false);
		// System.out.println("start: " + startHistoryPointer + " end: "
		// + endHistoryPointer + " next: " + nextHistoryPointer
		// + " current: " + currentHistoryPointer);
	}

	public void undo() {
		// decrement current and next
		currentHistoryPointer = decrementHistoryPointer(currentHistoryPointer);
		nextHistoryPointer = decrementHistoryPointer(nextHistoryPointer);
		// copy into the board
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y]
						.copyIn(boardHistory[currentHistoryPointer].cell[x][y]);
		lockButton.setSelected(boardHistory[currentHistoryPointer].lockButton);
		errorButton
				.setSelected(boardHistory[currentHistoryPointer].errorButton);
		announce1.setText(boardHistory[currentHistoryPointer].announce1);
		announce2.setText(boardHistory[currentHistoryPointer].announce2);
		// set undo/redo buttons
		redoButton.setEnabled(true);
		if (startHistoryPointer == currentHistoryPointer)
			undoButton.setEnabled(false);
		// System.out.println("start: " + startHistoryPointer + " end: "
		// + endHistoryPointer + " next: " + nextHistoryPointer
		// + " current: " + currentHistoryPointer);
	}

	public void redo() {
		// increment current and next
		currentHistoryPointer = incrementHistoryPointer(currentHistoryPointer);
		nextHistoryPointer = incrementHistoryPointer(nextHistoryPointer);
		// copy into the board
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y]
						.copyIn(boardHistory[currentHistoryPointer].cell[x][y]);
		lockButton.setSelected(boardHistory[currentHistoryPointer].lockButton);
		errorButton
				.setSelected(boardHistory[currentHistoryPointer].errorButton);
		announce1.setText(boardHistory[currentHistoryPointer].announce1);
		announce2.setText(boardHistory[currentHistoryPointer].announce2);
		// set undo/redo buttons
		undoButton.setEnabled(true);
		if (currentHistoryPointer == endHistoryPointer)
			redoButton.setEnabled(false);
		// System.out.println("start: " + startHistoryPointer + " end: "
		// + endHistoryPointer + " next: " + nextHistoryPointer
		// + " current: " + currentHistoryPointer);
	}

	public void dumpBoard()
	{
		for (int y=0; y<9; y++)
		{
			for (int x=0; x<9; x++)
				System.out.print(cell[x][y]);
			System.out.println();
		}
		System.out.println("=========");
	}
	/**
	 * This is the action listener for the "new" button.
	 */
	public class NewBoardButtonListener implements ActionListener, Runnable {
		int level;
		private boolean initHistory = false;

		/**
		 * Only use this constructor to generate the initial board.
		 */
		public NewBoardButtonListener(int level) {
			this.level = level;
			initHistory = true;
			setEnabledAll(false);
			(new Thread(this)).start();
		}

		int lastChoice = 3;

		/**
		 * Default constructor
		 */
		public NewBoardButtonListener() {
			initHistory = false;
		}

		public void actionPerformed(ActionEvent e) {
			// Custom button text
			unselectAllCells();
			String s = (String) JOptionPane
					.showInputDialog(
							me,
							"This will delete the current puzzle\nand create a new one in its place.\n\nDifficulty level:",
							"Create New Puzzle", JOptionPane.WARNING_MESSAGE,
							null, difficulties, difficulties[lastChoice - 1]);
			if (s != null) {
				if (s.equals(difficulties[4]))
					level = 5;
				else if (s.equals(difficulties[3]))
					level = 4;
				else if (s.equals(difficulties[2]))
					level = 3;
				else if (s.equals(difficulties[1]))
					level = 2;
				else
					level = 1;
				unlockAllCells(); // clear cells and shut down the gui
				clearAllCells();
				clearAnnotations();
				setEnabledAll(false);
				(new Thread(this)).start(); // spawn a thread to think about the
				// new
				
			}
		}

		/**
		 * Main thread for creating new board (code ripped from Sudoku2 main)
		 */
		public void run() {
			String symmetrical = "y", text; // DEBUG TEXT
			int minclues = -1, bottlenecks, stars, initialscan, scan, markup1, markup2;
			boolean success;
			double score, minscore = -1;
			String announce1Text="";
			lastChoice = level;
			announce1.setText("");

			if (level < 3) {
				minclues = Sudoku2.veryeasy_minclues;
				minscore = Sudoku2.veryeasy_minscore;
			}
			if (level == 3) {
				minclues = Sudoku2.easy_minclues;
				minscore = Sudoku2.easy_minscore;
			}
			if (level == 4) {
				minclues = Sudoku2.medium_minclues;
				minscore = Sudoku2.medium_minscore;
			}
			if (level > 4)
				minclues = 22;
			do {
				switch (difficulties[level - 1].charAt(0)) {
				case 'a':
				case 'e':
				case 'i':
				case 'o':
				case 'u':
				case 'A':
				case 'E':
				case 'I':
				case 'O':
				case 'U':
					announce2.setText("Searching for an "
							+ difficulties[level - 1] + " puzzle.");
					break;
				default:
					announce2.setText("Searching for a "
							+ difficulties[level - 1] + " puzzle.");
				}
				Sudoku2.init_board(board);
				Sudoku2.fill_board_random(board);
				// hill_climb_board convert
				HCBReturnValues hcbReturnValues = new HCBReturnValues();
				Sudoku2.hill_climb_board(board, hcbReturnValues);
				// swaps = hcbReturnValues.hcSwaps;
				// loops = hcbReturnValues.hcLoops;
				// END
				if (level < 4)
					if (symmetrical.equals("n"))
						Sudoku2
								.blank_out_unique_easy(board, minclues,
										minscore);
					else
						Sudoku2.blank_out_unique_easy_symmetrical(board,
								minclues, minscore);
				else if (symmetrical.equals("n"))
					Sudoku2.blank_out_unique_hard(board, minclues);
				else
					Sudoku2.blank_out_unique_hard_symmetrical(board, minclues);

				// scan_fill_board convert
				SFBReturnValues sfbReturnValues = new SFBReturnValues();
				Sudoku2.scan_fill_board(board, false, sfbReturnValues);
				success = sfbReturnValues.success;
				score = sfbReturnValues.meancount;
				bottlenecks = sfbReturnValues.bottlenecks;
				// END
				stars = 0;
				text = "";
				if (success) {
					if (bottlenecks >= 3 && score > Sudoku2.medium_minscore
							& score <= Sudoku2.medium_maxscore) {
						stars = 3;
						announce1Text ="Difficulty: " + difficulties[2];
						text = "Difficulty: * * *" + " ("
								+ Sudoku2.cell_count(board) + "," + score + ","
								+ bottlenecks + ")";
					}
					if (score > Sudoku2.easy_minscore
							& score <= Sudoku2.easy_maxscore & bottlenecks < 3
							& bottlenecks > 0) {
						stars = 2;
						announce1Text="Difficulty: " + difficulties[1];
						text = "Difficulty: * *" + " ("
								+ Sudoku2.cell_count(board) + "," + score + ","
								+ bottlenecks + ")";
					}
					if (score > Sudoku2.veryeasy_minscore & bottlenecks == 0) {
						announce1Text="Difficulty: " + difficulties[0];
						text = "Difficuty: *" + " ("
								+ Sudoku2.cell_count(board) + "," + score + ","
								+ bottlenecks + ")";
						stars = 1;
					}
				} else {
					// solver convert
					SolverReturnValues solverReturnValues = new SolverReturnValues();
					Sudoku2.solver(board, solverReturnValues, false);
					success = solverReturnValues.success;
					// given = solverReturnValues.given;
					initialscan = solverReturnValues.initialscan;
					scan = solverReturnValues.totalscan;
					markup1 = solverReturnValues.markup1;
					markup2 = solverReturnValues.markup2;
					// backtrack = solverReturnValues.backtrack;
					// unique = solverReturnValues.unique;
					// END
					if (markup2 > 0) {
						stars = 5;
						announce1Text="Difficulty: " + difficulties[4];
						text = "Difficulty: * * * * *" + "("
								+ Sudoku2.cell_count(board) + "," + scan + "/"
								+ initialscan + "," + markup1 + "," + markup2
								+ ")";
					} else {
						announce1Text="Difficulty: " + difficulties[3];
						text = "Difficulty: * * * *" + "("
								+ Sudoku2.cell_count(board) + "," + scan + "/"
								+ initialscan + "," + markup1 + "," + markup2
								+ ")";
						stars = 4;
					}
				}
			} while (stars != level
					|| Sudoku2.cell_count(board) > Sudoku2.maxclues);

			copyBoardToCells(); // copy into the cells
			lockFilledCells();// lock down the cells
			lockButton.setSelected(true);
			announce1.setText(announce1Text);
			announce2.setText("");
			boardChanged();
			setEnabledAll(true); // enable the gui
			// showStatus(text); // DEBUG display difficulty
			if (initHistory)
				initBoardHistory();
		}
	}

	public class UndoButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			unselectAllCells();
			undo();
		}
	}

	public class RedoButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			unselectAllCells();
			redo();
		}
	}

	/**
	 * listener for the Solve button
	 */
	public class SolveButtonListener implements ActionListener, Runnable {

		public void actionPerformed(ActionEvent e) {
			unselectAllCells();
			String[] options = { "Yes", "No" };
			int n = JOptionPane.showOptionDialog(me,
					"This will solve the puzzle for you.\n\nAre you sure?",
					"Solve Puzzle", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n == 0) {
				setEnabledAll(false);
				(new Thread(this)).start();
			}
		}

		/**
		 * Thread for solving board
		 */
		public void run() {
			Board_type oldBoard = new Board_type();
			copyCellsToBoard();
			Sudoku2.copy_board(board, oldBoard);
			clearAllCells();
			copyCellsToBoard();
			rateBoard();
			if (Sudoku2.score_board_full(board) == 0) {
				// First try without backtracking...
				SolverReturnValues solverReturnValues = new SolverReturnValues();
				board = Sudoku2.solver(board, solverReturnValues, false);
				if (solverReturnValues.success) {
					copyBoardToCellsWithHighlights(oldBoard);
				} else {
					// int cellsLeft = 81 - Sudoku2.cell_count(board);
					// double estimate = Math.pow(10.0, cellsLeft) / 2000000000
					// * 10000;
					// new SolveTimer(estimate, 10000, true);
					// solving = true;
					board = Sudoku2.solver(board, solverReturnValues, true);
					// solving = false;
					if (solverReturnValues.success) {
						copyBoardToCellsWithHighlights(oldBoard);
					}
				}
			} else {
				errorButton.setSelected(true);
				checkBoard();
			}
			captureBoardHistory();
			setEnabledAll(true);
		}
	}

	public class SolveTimer implements Runnable {
		double minute = 60;
		double hour = minute * 60;
		double day = hour * 24;
		double week = day * 7;
		double month = day * 30;
		double year = day * 365;
		double estimate;
		boolean interrupt;
		int pauseDuration;
		String estimateString;

		public SolveTimer(double estimate, int pauseDuration, boolean interrupt) {
			this.estimate = estimate;
			estimateString = "seconds";
			if (estimate > minute)
				estimateString = "minutes";
			else if (estimate > hour)
				estimateString = "hours";
			else if (estimate > day)
				estimateString = "days";
			else if (estimate > week)
				estimateString = "weeks";
			else if (estimate > month)
				estimateString = "months";
			else if (estimate > year)
				estimateString = "years";
			this.pauseDuration = pauseDuration;
			this.interrupt = interrupt;
			(new Thread(this)).start();
		}

		public void run() {
			// System.out.println("hello!");
			try {
				Thread.sleep(pauseDuration);
			} catch (InterruptedException e) {
			}
			// System.out.println("hello again!");
			if (solving & interrupt) {
				String[] options = { "Yes", "No" };
				int n = JOptionPane
						.showOptionDialog(
								me,
								"This is an extremely difficult board. If it has\na solution, I will find it, but I may have to\nthink about it for some time.\n\nIt could take "
										+ estimateString
										+ ".\n\nShould I keep going?",
								"Extremely Difficult Board",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options,
								options[1]);
				if (n == 1) {
					Sudoku2.stopFlag = true;
				}
			}
		}
	}

	/**
	 * Listener for clear button
	 */
	public class ClearButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			boolean notEmpty = false;
			for (int x = 0; x <= 8; x++)
				for (int y = 0; y <= 8; y++)
					if (!cell[x][y].isLocked() && cell[x][y].getValue() != 0
							| cell[x][y].annotations().length() != 0)
						notEmpty = true;
			if (notEmpty) {
				unselectAllCells();
				String[] options = { "Entries", "Conflicts", "Annotations",
						"Everything" };
				String s = (String) JOptionPane
						.showInputDialog(
								me,
								"This will clear entries and/or markups from unlocked cells.\n\nWhat do you want to clear?",
								"Clear Board", JOptionPane.WARNING_MESSAGE,
								null, options, options[0]);
				if (s != null) {
					if (s.equals(options[0]))
						clearAllCells();
					else if (s.equals(options[1]))
						clearConflicts();
					else if (s.equals(options[2]))
						clearAnnotations();
					else if (s.equals(options[3])) {
						clearAllCells();
						clearAnnotations();
					}
					boardChanged();
				}
			}
		}
	}

	/**
	 * Listener for lock button
	 */
	public class LockButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			unselectAllCells();
			if (lockButton.isSelected()) {
				lockFilledCells();
				copyCellsToBoard();
				rateBoard();
			} else {
				unlockAllCells();
				announce1.setText("");
				announce2.setText("");
			}
			captureBoardHistory();
		}
	}

	/**
	 * Listener for error button
	 */
	public class ErrorButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			unselectAllCells();
			if (errorButton.isSelected())
				checkBoard();
			else {
				checkBoard();
				clearErrors();
			}
			captureBoardHistory();
		}
	}

	/**
	 * Uses the solver to come up with (approximate) rating.
	 */
	public void rateBoard() {
		int bottlenecks, initialscan, scan, markup1, markup2;
		boolean success, unique;
		double score;
		String text = "unrateable";
		Board_type board3;

		if (Sudoku2.score_board_full(board) == 0) {
			// scan_fill_board convert
			SFBReturnValues sfbReturnValues = new SFBReturnValues();
			Sudoku2.scan_fill_board(board, false, sfbReturnValues);
			success = sfbReturnValues.success;
			score = sfbReturnValues.meancount;
			bottlenecks = sfbReturnValues.bottlenecks;
			// END
			if (success) {
				if (score <= Sudoku2.medium_maxscore | bottlenecks >= 3) {
					announce1.setText("Difficulty: " + difficulties[2]);
					announce2.setText("");
					text = "Difficulty: * * *" + " ("
							+ Sudoku2.cell_count(board) + "," + score + ","
							+ bottlenecks + ")";
				} else if (score <= Sudoku2.easy_maxscore) {
					announce1.setText("Difficulty: " + difficulties[1]);
					announce2.setText("");
					text = "Difficulty: * *" + " (" + Sudoku2.cell_count(board)
							+ "," + score + "," + bottlenecks + ")";
				} else {
					announce1.setText("Difficulty: " + difficulties[0]);
					announce2.setText("");
					text = "Difficuty: *" + " (" + Sudoku2.cell_count(board)
							+ "," + score + "," + bottlenecks + ")";
				}
			} else {
				// solver convert
				SolverReturnValues solverReturnValues = new SolverReturnValues();
				// new SolveTimer(-1, 2000, false);
				// solving = true;
				board3 = Sudoku2.solver(board, solverReturnValues, false);
				// solving = false;
				success = solverReturnValues.success;
				// given = solverReturnValues.given;
				initialscan = solverReturnValues.initialscan;
				scan = solverReturnValues.totalscan;
				markup1 = solverReturnValues.markup1;
				markup2 = solverReturnValues.markup2;
				// backtrack = solverReturnValues.backtrack;
				// unique = solverReturnValues.unique;
				// END
				if (success) {
					if (markup2 > 0) {
						announce1.setText("Difficulty: " + difficulties[4]);
						announce2.setText("");
						text = "Difficulty: * * * * *" + "("
								+ Sudoku2.cell_count(board) + "," + scan + "/"
								+ initialscan + "," + markup1 + "," + markup2
								+ ")";
					} else {
						announce1.setText("Difficulty: " + difficulties[3]);
						announce2.setText("");
						text = "Difficulty: * * * *" + "("
								+ Sudoku2.cell_count(board) + "," + scan + "/"
								+ initialscan + "," + markup1 + "," + markup2
								+ ")";
					}
				} else {
					// solver convert
					// System.out.println("hey");
					SolverReturnValues solverReturnValues2 = new SolverReturnValues();
					Sudoku2.solver(board3, solverReturnValues2, true);
					success = solverReturnValues2.success;
					// given = solverReturnValues2.given;
					initialscan = solverReturnValues2.initialscan;
					scan = solverReturnValues2.totalscan;
					markup1 = solverReturnValues2.markup1;
					markup2 = solverReturnValues2.markup2;
					// backtrack = solverReturnValues.backtrack;
					unique = solverReturnValues2.unique;
					// END
					if (success) {
						announce1.setText("Difficulty: Extremely Hard");
						if (!unique)
							announce2
									.setText("There is more than one solution.");
						text = "Difficulty: * * * * * * " + unique;
					} else {
						// System.out.println("success=false");
						announce1.setText("Difficulty: Unsolveable");
						text = "unsolveable";
					}
				}
			}
		} else
			text = "unsolveable";
		// DEBUG print out the message
		// showStatus(text);
		if (text.equals("unsolveable"))
			JOptionPane.showMessageDialog(this, "This Sudoku has no solution.",
					"Unsolvable Sudoku", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Clears the error status of every cell
	 */
	public void clearErrors() {
		// clear errors
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].setError(false);
		repaint();
	}

	/**
	 * Called whenever the board is changed
	 */
	public void boardChanged() {
		int cellCount = checkBoard();
		if (cellCount == 81)
			if (!errors)
				JOptionPane.showMessageDialog(me, "Congratulations!!!",
						"You're Finished", JOptionPane.INFORMATION_MESSAGE);
			else {
				errorButton.setSelected(true);
				JOptionPane.showMessageDialog(me, "You have errors...",
						"You're Not Finished", JOptionPane.ERROR_MESSAGE);
			}
		else if (!errorButton.isSelected())
			clearErrors();
		captureBoardHistory();
		if (debug) dumpBoard();
	}

	/**
	 * Check board and flag error cells
	 */
	public boolean errors;

	public int checkBoard() {
		clearErrors();
		errors = false;
		int cellCount = 0;
		// set column errors
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				if (cell[x][y].getValue() != 0) {
					cellCount++;
					for (int newy = y + 1; newy <= 8; newy++)
						if (cell[x][newy].getValue() == cell[x][y].getValue()) {
							cell[x][newy].setError(true);
							cell[x][y].setError(true);
							errors = true;
						}
				}
		// set row errors
		for (int y = 0; y <= 8; y++)
			for (int x = 0; x <= 8; x++)
				if (cell[x][y].getValue() != 0) {
					for (int newx = x + 1; newx <= 8; newx++)
						if (cell[newx][y].getValue() == cell[x][y].getValue()) {
							cell[newx][y].setError(true);
							cell[x][y].setError(true);
							errors = true;
						}
				}
		// set quad errors
		for (int quadx = 0; quadx <= 2; quadx++)
			for (int quady = 0; quady <= 2; quady++)
				for (int quadPos = 0; quadPos <= 8; quadPos++) {
					int x = quadPos % 3 + quadx * 3;
					int y = quadPos / 3 + quady * 3;
					if (cell[x][y].getValue() != 0) {
						for (int newQuadPos = quadPos + 1; newQuadPos <= 8; newQuadPos++) {
							int newx = newQuadPos % 3 + quadx * 3;
							int newy = newQuadPos / 3 + quady * 3;
							if (cell[newx][newy].getValue() == cell[x][y]
									.getValue()) {
								cell[newx][newy].setError(true);
								cell[x][y].setError(true);
								errors = true;
							}
						}
					}
				}
		// System.out.println(cellCount);
		return cellCount;
	}

	/**
	 * Clears all cells
	 */
	public void clearAllCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].clear();
	}

	/**
	 * Clears all cells marked as conflicts
	 */
	public void clearConflicts() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				if (cell[x][y].error())
					cell[x][y].clear();
	}

	/**
	 * Clears all annotations
	 */
	public void clearAnnotations() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].clearAnnotations();
	}

	/**
	 * Enables/disables all cells and buttons
	 * 
	 * @param enabled
	 *            True for enabled, False for disabled
	 */
	public void setEnabledAll(boolean enabled) {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].setEnabled(enabled);
		newBoardButton.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		lockButton.setEnabled(enabled);
		solveButton.setEnabled(enabled);
		errorButton.setEnabled(enabled);
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		if (enabled) {
			if (startHistoryPointer != currentHistoryPointer)
				undoButton.setEnabled(true);
			if (endHistoryPointer != currentHistoryPointer)
				redoButton.setEnabled(true);
		}
	}

	/**
	 * Copies the current view to the board variable
	 */
	public void copyCellsToBoard() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				board.locations[x + 1][y + 1] = cell[x][y].getValue();
	}

	/**
	 * Copies the board variable to the current view
	 */
	public void copyBoardToCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].setValue(board.locations[x + 1][y + 1]);
	}

	public void copyBoardToCellsWithHighlights(Board_type comparisonBoard) {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++) {
				cell[x][y].setValue(board.locations[x + 1][y + 1]);
				if (comparisonBoard.locations[x + 1][y + 1] == 0)
					cell[x][y].overline(Cell.SPACE_OVERLINE);
				else if (board.locations[x + 1][y + 1] != comparisonBoard.locations[x + 1][y + 1])
					cell[x][y].overline(Cell.ERROR_OVERLINE);
			}
	}

	/**
	 * Locks down any filled in cell
	 */
	public void lockFilledCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				if (cell[x][y].getValue() > 0)
					cell[x][y].lock();
	}

	/**
	 * Locks down all cells
	 */
	public void lockAllCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].lock();
	}

	/**
	 * Unlocks all cells
	 */
	public void unlockAllCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].unlock();
	}

	/**
	 * Unselects everything
	 */
	public void unselectAllCells() {
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++)
				cell[x][y].unselect();
	}

	public class URLLoader implements Runnable {
		JEditorPane pane;
		String url;

		public URLLoader(JEditorPane pane, String url) {
			this.pane = pane;
//			try {
				this.url = url;
//			} catch (MalformedURLException e) {
//				System.out.println("Bad URL: " + url);
//			}
			(new Thread(this)).start();
		}

		public void run() {
			try {
				//SudokuApplet.class.getResource("data/loading.html");
				pane.setPage(SudokuApplet.class.getResource("data/loading.html"));//new URL(getCodeBase(),"data/loading.html"));
			} catch (IOException e) {
				System.err.println("Attempted to read a bad URL: data/loading.html"
						);
			}
			try {
				pane.setPage(SudokuApplet.class.getResource(url));
			} catch (IOException e) {
				System.err.println("Attempted to read a bad URL: "
						+ url.toString());
			}
		}
	}

	/**
	 * Creates the main GUI layout in a JPanel.
	 * 
	 * @return A new JPanel with the layout for the JFrame.
	 */
	public String cellTip = "Click to enter a number. Right-click to enter markups.";

	public JPanel makeGUI() {

		// constants
		final int gridSquareSize = 50;

		// The Main Grid
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(3, 3));
		gridPanel.setMaximumSize(new Dimension(gridSquareSize * 9,
				gridSquareSize * 9));
		gridPanel.setMinimumSize(new Dimension(gridSquareSize * 9,
				gridSquareSize * 9));
		gridPanel.setPreferredSize(new Dimension(gridSquareSize * 9,
				gridSquareSize * 9));
		// The Cells 9x9
		for (int x = 0; x <= 8; x++)
			for (int y = 0; y <= 8; y++) {
				cell[x][y] = new Cell(" ", this);
				cell[x][y].setToolTipText(cellTip);
				cell[x][y].setBorder(BorderFactory
						.createLineBorder(Color.BLACK));
				cell[x][y].setFont(new Font("SansSerif", Font.BOLD, 30));
				cell[x][y].setMaximumSize(new Dimension(gridSquareSize,
						gridSquareSize));
				cell[x][y].setMinimumSize(new Dimension(gridSquareSize,
						gridSquareSize));
				cell[x][y].setPreferredSize(new Dimension(gridSquareSize,
						gridSquareSize));
			}
		// The Quadrants 3x3
		JPanel[][] quad = new JPanel[3][3];
		for (int qx = 0; qx <= 2; qx++)
			for (int qy = 0; qy <= 2; qy++) {
				quad[qx][qy] = new JPanel();
				quad[qx][qy].setLayout(new GridLayout(3, 3));
				quad[qx][qy].setBorder(BorderFactory
						.createLineBorder(Color.BLACK));
				quad[qx][qy].setMaximumSize(new Dimension(gridSquareSize * 3,
						gridSquareSize * 3));
				quad[qx][qy].setMinimumSize(new Dimension(gridSquareSize * 3,
						gridSquareSize * 3));
				quad[qx][qy].setPreferredSize(new Dimension(gridSquareSize * 3,
						gridSquareSize * 3));
				gridPanel.add(quad[qx][qy]);
				for (int x = qx * 3; x <= qx * 3 + 2; x++)
					for (int y = qy * 3; y <= qy * 3 + 2; y++)
						quad[qx][qy].add(cell[y][x]);
			}

		// Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		newBoardButton = new JButton("New");
		newBoardButton.setToolTipText("Create a new puzzle.");
		newBoardButton.addActionListener(new NewBoardButtonListener());
		newBoardButton.setMaximumSize(buttonSize);
		newBoardButton.setPreferredSize(buttonSize);
		newBoardButton.setMaximumSize(buttonSize);
		// newBoardSelector = new JComboBox(difficulties);
		// newBoardSelector.setSelectedIndex(2);
		// newBoardSelector.setMaximumSize(buttonSize);
		// newBoardSelector.setPreferredSize(buttonSize);
		// newBoardSelector.setMaximumSize(buttonSize);
		solveButton = new JButton("Solve");
		solveButton.setToolTipText("Solve the current puzzle.");
		solveButton.addActionListener(new SolveButtonListener());
		solveButton.setMaximumSize(buttonSize);
		solveButton.setPreferredSize(buttonSize);
		solveButton.setMaximumSize(buttonSize);
		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Clear entries, conflicts, and/or markups.");
		clearButton.addActionListener(new ClearButtonListener());
		clearButton.setMaximumSize(buttonSize);
		clearButton.setPreferredSize(buttonSize);
		clearButton.setMaximumSize(buttonSize);
		undoButton = new JButton("<");
		undoButton.setToolTipText("Undo.");
		undoButton.addActionListener(new UndoButtonListener());
		undoButton.setMaximumSize(buttonHalfSize);
		undoButton.setPreferredSize(buttonHalfSize);
		undoButton.setMaximumSize(buttonHalfSize);
		undoButton.setEnabled(false);
		redoButton = new JButton(">");
		redoButton.setToolTipText("Redo.");
		redoButton.addActionListener(new RedoButtonListener());
		redoButton.setMaximumSize(buttonHalfSize);
		redoButton.setPreferredSize(buttonHalfSize);
		redoButton.setMaximumSize(buttonHalfSize);
		redoButton.setEnabled(false);
		lockButton = new JCheckBox("Lock");
		lockButton.setToolTipText("Lock/unlock boxes.");
		lockButton.setSelected(false);
		lockButton.addActionListener(new LockButtonListener());
		// lockButton.setMaximumSize(buttonSize);
		// lockButton.setPreferredSize(buttonSize);
		// lockButton.setMaximumSize(buttonSize);
		errorButton = new JCheckBox("Conflicts");
		errorButton.setToolTipText("Show conflicts.");
		errorButton.setSelected(true);
		errorButton.addActionListener(new ErrorButtonListener());
		// errorButton.setMaximumSize(buttonSize);
		// errorButton.setPreferredSize(buttonSize);
		// errorButton.setMaximumSize(buttonSize);
		buttonPanel.add(newBoardButton);
		// buttonPanel.add(newBoardSelector);
		buttonPanel.add(solveButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(undoButton);
		buttonPanel.add(redoButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		buttonPanel.add(lockButton);
		buttonPanel.add(errorButton);

		// Heading Panel
		JPanel subHheadingPanel = new JPanel();
		subHheadingPanel.setLayout(new BoxLayout(subHheadingPanel,
				BoxLayout.X_AXIS));
		subHheadingPanel.setMaximumSize(new Dimension(gridSquareSize * 9, 30));
		subHheadingPanel.setMinimumSize(new Dimension(gridSquareSize * 9, 30));
		subHheadingPanel
				.setPreferredSize(new Dimension(gridSquareSize * 9, 30));
		JLabel heading = new JLabel("SCHMUDOKU!");
		heading.setFont(new Font("SansSerif", Font.BOLD, 30));
		JPanel subHeadingPanel = new JPanel();
		subHeadingPanel.setLayout(new BoxLayout(subHeadingPanel,
				BoxLayout.Y_AXIS));
		JLabel author = new JLabel(" by sam.scott@sheridanc.on.ca");
		author.setFont(new Font("MonoSpaced", Font.BOLD, 14));
		author.setVerticalAlignment(JLabel.BOTTOM);
		subHeadingPanel.add(Box.createVerticalGlue());
		subHeadingPanel.add(Box.createRigidArea(new Dimension(10, 12)));
		subHeadingPanel.add(author);
		subHeadingPanel.add(Box.createVerticalGlue());
		subHheadingPanel.add(Box.createHorizontalGlue());
		subHheadingPanel.add(heading);
		subHheadingPanel.add(subHeadingPanel);
		subHheadingPanel.add(Box.createHorizontalGlue());
		JPanel headingPanel = new JPanel();
		headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
		headingPanel.add(Box.createRigidArea(new Dimension(10, 5)));
		headingPanel.add(subHheadingPanel);
		headingPanel.add(Box.createRigidArea(new Dimension(10, 5)));

		// Middle Panel
		JPanel middleSubPanel = new JPanel();
		middleSubPanel
				.setLayout(new BoxLayout(middleSubPanel, BoxLayout.Y_AXIS));
		middleSubPanel.add(gridPanel);
		middleSubPanel.add(buttonPanel);
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
		middlePanel.add(Box.createHorizontalGlue());
		middlePanel.add(middleSubPanel);
		middlePanel.add(Box.createHorizontalGlue());

		// Footer Panel
		JPanel subFooterPanel = new JPanel();
		subFooterPanel.setLayout(new GridLayout(1, 2));
		announce1 = new JLabel("Difficulty: " + difficulties[2] + ".");
		announce2 = new JLabel("There is more than one solution.");
		// announce1.setHorizontalAlignment(JLabel.LEFT);
		// announce1.setPreferredSize(new Dimension(100,24));
		// announce1.setMaximumSize(new Dimension(100,24));
		// announce1.setMinimumSize(new Dimension(100,24));
		announce1.setFont(new Font("SansSerif", Font.BOLD, 12));
		announce2.setFont(new Font("SansSerif", Font.BOLD, 12));
		announce2.setForeground(Color.red);
		subFooterPanel.add(announce1);
		subFooterPanel.add(announce2);
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.X_AXIS));
		footerPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		footerPanel.add(subFooterPanel);

		// The puzzle panel
		JPanel puzzlePanel = new JPanel();
		puzzlePanel.setLayout(new BoxLayout(puzzlePanel, BoxLayout.Y_AXIS));
		puzzlePanel.add(middlePanel);
		puzzlePanel.add(footerPanel);
		puzzlePanel.add(Box.createVerticalGlue());

		// The instructions panel
		JPanel instructionPanel = new JPanel();
		JEditorPane instructions = new JEditorPane();
		new URLLoader(instructions, "data/instructions.html");
		JScrollPane instructionsScrollPane = new JScrollPane(instructions);
		instructionsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		instructions.setEditable(false);
		instructionPanel.add(instructionsScrollPane);

		// The wikipedia panel
		JPanel wikiPanel = new JPanel();
		JEditorPane wiki = new JEditorPane();
		new URLLoader(wiki, "data/wikipedia.html");
		JScrollPane wikiScrollPane = new JScrollPane(wiki);
		wikiScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		wiki.setEditable(false);
		wikiPanel.add(wikiScrollPane);

		// Tabbed Pane
		int buffer = 10;
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setMaximumSize(new Dimension(gridSquareSize * 9 + buffer,
				gridSquareSize * 9 + buffer + 35));
		tabbedPane.setMinimumSize(new Dimension(gridSquareSize * 9 + buffer,
				gridSquareSize * 9 + buffer * 4 + 35));
		tabbedPane.setPreferredSize(new Dimension(gridSquareSize * 9 + buffer,
				gridSquareSize * 9 + buffer * 4 + 35));
		tabbedPane.addTab("Puzzle", puzzlePanel);
		tabbedPane.addTab("Help", instructionsScrollPane);
		tabbedPane.addTab("Wikipedia", wikiScrollPane);

		// The whole thing
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
		JPanel mpanel = new JPanel();
		mpanel.setLayout(new BoxLayout(mpanel, BoxLayout.X_AXIS));
		mpanel.add(Box.createHorizontalGlue());
		JPanel mmpanel = new JPanel();
		mmpanel.setLayout(new BoxLayout(mmpanel, BoxLayout.Y_AXIS));
		mmpanel.add(headingPanel);
		mmpanel.add(tabbedPane);
		mmpanel.add(Box.createVerticalGlue());
		mpanel.add(mmpanel);
		mpanel.add(Box.createHorizontalGlue());
		newPanel.add(mpanel);
		newPanel.add(Box.createVerticalGlue());

		return newPanel;
	}
}
