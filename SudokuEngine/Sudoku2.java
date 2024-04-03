package SudokuEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Sudoku 4.3d -- Sam Scott -- February 2006 SamScott@Canada.com
 * 
 * This code was translated from Turing, December 15-16 2007.
 * 
 * @author Sam Scott
 * 
 */

public class Sudoku2 {

	// General constants
	public final static double medium_minscore = 1.5;
	public final static double medium_maxscore = 2.5;
	public final static double easy_minscore = 3;
	public final static double easy_maxscore = 4;
	public final static double veryeasy_minscore = 5.0;
	public final static int medium_minclues = 0;
	public final static int easy_minclues = 0;
	public final static int veryeasy_minclues = 0;
	public final static int hard_minclues = 0;
	public final static int maxclues = 30;

	public static boolean stopFlag = false;

	// Type declarations

	// type markup_type : array 1 .. 9 of array 1 .. 9 of array 1 .. 9 of
	// boolean
	// Replace with boolean[10][10], zero element unused

	// PROCEDURE init_board: Blanks out a board
	public static void init_board(Board_type board) {
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++) {
				board.locations[i][j] = 0;
				board.fixed[i][j] = false;
			}
	}

	// PROCEDURE fill_board: given an array of strings that contains a board in
	// raw text format, returns the equivalent board
	// as a board_type
	public static void fill_board(Board_type board, String[] rows) {
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++)
				if ((rows[i].charAt(j) >= '1') && (rows[i].charAt(j) <= '9')) {
					board.locations[j][i] = (int) (rows[i].charAt(j)) - 48;
					board.fixed[j][i] = true;
				} else
					board.fixed[j][i] = false;
	}

	// PROCEDURE fill_board_random: Fills each 3x3 square with digits 1 to 9
	public static void fill_board_random(Board_type board) {
		boolean[] used = new boolean[10];
		int num;

		for (int i = 0; i <= 2; i++)
			for (int j = 0; j <= 2; j++) {
				for (int k = 1; k <= 9; k++)
					used[k] = false;

				for (int x = 1; x <= 3; x++)
					for (int y = 1; y <= 3; y++)
						if (board.locations[i * 3 + x][j * 3 + y] > 0)
							used[board.locations[i * 3 + x][j * 3 + y]] = true;

				for (int x = 1; x <= 3; x++)
					for (int y = 1; y <= 3; y++)
						if (!board.fixed[i * 3 + x][j * 3 + y]) {
							do
								num = (int) (Math.random() * 9 + 1);
							while (used[num]);

							board.locations[i * 3 + x][j * 3 + y] = num;
							used[num] = true;
						}
			}
	}

	// FUNCTION full_board: outputs true if board has no empty spots
	public static boolean full_board(Board_type board) {
		for (int row = 1; row <= 9; row++)
			for (int col = 1; col <= 9; col++)
				if (board.locations[col][row] == 0)
					return false;
		return true;
	}

	// FUNCTION full_board: outputs true if board has no empty spots
	public static int cell_count(Board_type board) {
		int output = 0;

		for (int row = 1; row <= 9; row++)
			for (int col = 1; col <= 9; col++)
				if (board.locations[col][row] > 0)
					output = output + 1;

		return output;
	}

	// PROCEDURE show_board: prints a board to the screen
	public static void show_board(Board_type board) {
		System.out.println("");
		for (int i = 1; i <= 9; i++) {
			if (((i - 1) % 3) == 0)
				System.out.println("+---+---+---+");

			for (int j = 1; j <= 9; j++) {
				if (((j - 1) % 3) == 0)
					System.out.print("|");
				if (board.locations[j][i] == 0)
					System.out.print(" ");
				else
					System.out.print(board.locations[j][i]);
			}
			System.out.println("|   ");

		}

		System.out.println("+---+---+---+");
		System.out.println("");
	}

	/**
	 * Dumps board to screen as a single line.
	 * 
	 * @param board
	 */
	public static void dump_board(Board_type board) {
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++)
				if (board.locations[j][i] == 0)
					System.out.print(".");
				else
					System.out.print(board.locations[j][i]);

		System.out.println("");
	}

	// FUNCTION row_score: count how many times a given digit appears in a given
	// row
	public static int row_score(Board_type board, int row, int num) {
		int count = 0;
		for (int col = 1; col <= 9; col++)
			if (board.locations[col][row] == num)
				count = count + 1;
		if (count > 1)
			return count - 1;
		else
			return 0;
	}

	// FUNCTION col_score: count how many times a given digit appears in a given
	// col
	public static int col_score(Board_type board, int col, int num) {
		int count = 0;
		for (int row = 1; row <= 9; row++)
			if (board.locations[col][row] == num)
				count = count + 1;
		if (count > 1)
			return count - 1;
		else
			return 0;
	}

	// FUNCTION col_score: count how many times a given digit appears in a given
	// col
	public static int quad_score(Board_type board, int quadx, int quady, int num) {
		int count = 0;
		for (int col = quadx * 3 + 1; col <= (quadx + 1) * 3; col++)
			for (int row = quady * 3 + 1; row <= (quady + 1) * 3; row++)
				if (board.locations[col][row] == num)
					count = count + 1;
		if (count > 1)
			return count - 1;
		else
			return 0;
	}

	// FUNCTION score_board: add up all possible row and colum scores
	// this is a measure of how bad the board is for hill climbing (quads are
	// known to be ok)
	// - a 0 score is a good, solved board
	public static int score_board(Board_type board) {
		int score = 0;
		for (int num = 1; num <= 9; num++) {
			for (int row = 1; row <= 9; row++)
				score = score + row_score(board, row, num);
			for (int col = 1; col <= 9; col++)
				score = score + col_score(board, col, num);
		}
		return score;
	}

	// scores with the the quads as well...
	public static int score_board_full(Board_type board) {
		int score = 0;
		for (int num = 1; num <= 9; num++) {
			for (int row = 1; row <= 9; row++)
				score = score + row_score(board, row, num);
			for (int col = 1; col <= 9; col++)
				score = score + col_score(board, col, num);
			for (int quadx = 0; quadx <= 2; quadx++)
				for (int quady = 0; quady <= 2; quady++)
					score = score + quad_score(board, quadx, quady, num);
		}
		return score;
	}

	// FUNCTION random_swap: randomly chooses two cells (in the same 3x3 block)
	// swaps them, and checks to see if the board is
	// improved or made worse... if the board is made
	// worse by the swap, doesn't keep it
	// - returns the difference between the old board score
	// and the new one
	// - this function is a key part of the hill-climbing
	// algorithm that finds a good solved board
	public static int random_swap(Board_type board) {
		int xquad, yquad, row1, col1, row2, col2;
		int score, new_score, temp;

		// choose a quadrant (3x3 block)
		xquad = (int) (Math.random() * 3);
		yquad = (int) (Math.random() * 3);

		// choose elements to swap within that quadrant
		for (;;) {
			row1 = (int) (Math.random() * 3 + 1) + yquad * 3;
			col1 = (int) (Math.random() * 3 + 1) + xquad * 3;
			row2 = (int) (Math.random() * 3 + 1) + yquad * 3;
			col2 = (int) (Math.random() * 3 + 1) + xquad * 3;

			if ((board.fixed[col1][row1] == false)
					&& (board.fixed[col2][row2] == false))
				break;
		}

		// get the relevant row and column scores pre-swap
		score = row_score(board, row1, board.locations[col1][row1])
				+ col_score(board, col1, board.locations[col1][row1]);
		score = score + row_score(board, row2, board.locations[col2][row2])
				+ col_score(board, col2, board.locations[col2][row2]);

		// swap
		temp = board.locations[col1][row1];
		board.locations[col1][row1] = board.locations[col2][row2];
		board.locations[col2][row2] = temp;

		// get the relevant row and column scores post-swap
		new_score = row_score(board, row1, board.locations[col1][row1])
				+ col_score(board, col1, board.locations[col1][row1]);
		new_score = new_score
				+ row_score(board, row2, board.locations[col2][row2])
				+ col_score(board, col2, board.locations[col2][row2]);

		// swap back if the board got worse
		if ((score - new_score) < 0) {
			temp = board.locations[col1][row1];
			board.locations[col1][row1] = board.locations[col2][row2];
			board.locations[col2][row2] = temp;
		}

		// return score difference
		return score - new_score;

	}

	// PROCEDURE hill_climb_board: given a random board, swap elements until the
	// board score is 0
	// ***** hcSwaps and hcLoops replace var parameters
	public static void hill_climb_board(Board_type board,
			HCBReturnValues returnValues) {
		returnValues.hcSwaps = 0;
		returnValues.hcLoops = 0;
		for (;;) {
			if (random_swap(board) >= 0)
				returnValues.hcSwaps = returnValues.hcSwaps + 1;
			returnValues.hcLoops = returnValues.hcLoops + 1;
			if (score_board(board) == 0)
				break;
		}
	}

	// PROCEDURE btboard: main backtracking algorithm for solving boards
	// b = inSystem.out.println(board
	// output= outputboard
	// c, r = current column and row
	// min_col, min_row = starting row and column
	// max_col, max_row = stopping row and column
	// success = true if solution found *** Replaced with returnFlags
	// unique = true if unique solution found *** Replaced with returnFlags
	public static void btboard(Board_type b, Board_type output, int c, int r,
			int min_col, int min_row, int max_col, int max_row,
			BTBReturnFlags returnFlags) {
		boolean[] avail = new boolean[10];
		Board_type board = b;
		int counter, quad_row, quad_col;
		int column = c;
		int row = r;
		BTBReturnFlags tempReturnFlags = new BTBReturnFlags();

		if (!stopFlag) { // to stop things if necessary
			// System.out.println("btbboard: " + c + r + min_col + min_row +
			// max_col
			// + max_row);
			// show_board (board)

			// check for win
			if ((row == max_row) && (column == max_col)) {
				if (returnFlags.success == true)
					returnFlags.unique = false;
				else
					copy_board(board, output);
				returnFlags.success = true;
			}

			else { // increment board position
				column = column + 1;
				if (column == max_col + 1) {
					column = min_col;
					row = row + 1;
				}
				// check if already filled in
				if (board.locations[column][row] > 0) {
					// System.out.println("skip");
					Board_type newBoard = new Board_type();
					copy_board(board, newBoard);
					btboard(newBoard, output, column, row, min_col, min_row,
							max_col, max_row, returnFlags);
				} else {
					// compute possible next numbers
					for (int i = 1; i <= 9; i++)
						avail[i] = true;
					for (int i = 1; i <= 9; i++)
						if (board.locations[column][i] > 0)
							avail[board.locations[column][i]] = false;
					// System.out.println(board.locations [column] [i]..
					for (int i = 1; i <= 9; i++)
						if (board.locations[i][row] > 0)
							avail[board.locations[i][row]] = false;
					// System.out.println(board.locations [i] [row]..
					quad_row = (row - 1) / 3;
					quad_col = (column - 1) / 3;
					for (int i = 1; i <= 3; i++)
						for (int j = 1; j <= 3; j++)
							if (board.locations[quad_col * 3 + i][quad_row * 3
									+ j] > 0)
								avail[board.locations[quad_col * 3 + i][quad_row
										* 3 + j]] = false;
					// System.out.println(board.locations (quad_col * 3
					// + i) (quad_row * 3 + j)..
					counter = 1;
					for (;;) {
						if (avail[counter] == true) {
							board.locations[column][row] = counter;
							// System.out.println("try: " + column + row +
							// counter);
							tempReturnFlags.success = false;
							tempReturnFlags.unique = true;
							Board_type newBoard = new Board_type();
							copy_board(board, newBoard);
							btboard(newBoard, output, column, row, min_col,
									min_row, max_col, max_row, tempReturnFlags);
							if (tempReturnFlags.success) {
								if (returnFlags.success
										|| !tempReturnFlags.unique)
									returnFlags.unique = false;
								returnFlags.success = true;
							}
						}
						counter = counter + 1;
						if ((counter == 10) || (returnFlags.unique == false))
							break;
					}
				}
			}
		}
	}

	// FUNCTION backtrack_board: this is a wrapper for the main backtracking
	// algorithm in procedure "btboard"
	// success = true if solution found *** replaced with returnFlags
	// unique = true if the solution is unique *** replaced with returnFlags
	// return = solved board
	// min_col, max_col, min_row, max_row = box to backtrack within
	public static Board_type backtrack_board(Board_type board, int min_col,
			int min_row, int max_col, int max_row, BTBReturnFlags returnFlags) {
		Board_type output = new Board_type();
		returnFlags.success = false;
		returnFlags.unique = true;
		stopFlag = false;
		btboard(board, output, min_col - 1, min_row, min_col, min_row, max_col,
				max_row, returnFlags);
		stopFlag = false;
		// show_board(output);
		return output;
	}

	// PROCEDURE blank_out_unique: blanks out board to a maximum number of clues
	// - uses the backtracker to decide whether the board has a unique solution
	// - final board is guaranteed to have a unique solution
	// - no measure of hardness of board other than number of clues
	// - "clues" = prefered maximum number of clues in the board
	// - uses the "fixed" part of "board_type" to show which are clues, which
	// are not
	public static void blank_out_unique(Board_type board, int clues) {
		int x, y, tempi, count;
		BTBReturnFlags returnFlags = new BTBReturnFlags();
		int[] rows = new int[82], cols = new int[82]; // zero element unused

		// System.out.println("Reducing Clues: " ..

		// all board elements are fixed
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++)
				board.fixed[i][j] = true;

		// init number of clues in board currently
		count = 81;

		// this loop blanks out clues randomly until it finds one that makes the
		// board non-unique
		for (;;) {
			x = (int) (Math.random() * 9) + 1;
			y = (int) (Math.random() * 9) + 1;

			returnFlags.unique = true;

			if (board.locations[x][y] > 0) {
				count = count - 1;
				tempi = board.locations[x][y];
				board.locations[x][y] = 0;
				board.fixed[x][y] = false;
				backtrack_board(board, 1, 1, 9, 9, returnFlags);
				if (!returnFlags.unique) {
					board.locations[x][y] = tempi;
					board.fixed[x][y] = true;
					count = count + 1;
				}
			}
			if (!returnFlags.unique)
				break;
		}

		// initialize the rows and cols arrays to all possible cell coordinates
		for (int i = 1; i <= 81; i++) {
			rows[i] = (i + 8) / 9;
			cols[i] = ((i - 1) % 9) + 1;
			// System.out.println(rows[i],cols[i]," "..
		}

		// shuffle the rows and cols arrays to allow for a random selection of
		// cells
		for (int i = 1; i <= 81; i++) {
			x = (int) (Math.random() * 81) + 1;
			tempi = rows[i];
			rows[i] = rows[x];
			rows[x] = tempi;
			tempi = cols[i];
			cols[i] = cols[x];
			cols[x] = tempi;
		}

		// cycle through all the clue locations, and try blanking them out
		// only allow it if the return has a unique solution
		// stop when all clues tried, or when the board has few enough clues
		// left
		for (int i = 1; i <= 81; i++) {
			if (count > clues)
				if (board.locations[cols[i]][rows[i]] > 0) {
					count = count - 1;
					tempi = board.locations[cols[i]][rows[i]];
					board.locations[cols[i]][rows[i]] = 0;
					board.fixed[cols[i]][rows[i]] = false;
					backtrack_board(board, 1, 1, 9, 9, returnFlags);
					if (!returnFlags.unique) {
						board.locations[cols[i]][rows[i]] = tempi;
						board.fixed[cols[i]][rows[i]] = true;
						count = count + 1;
					}
					// else
					// System.out.println(count, " " ..
				}
		}
		// System.out.println(""
	}

	// FUNCTION same_board: returns true if the two boards are identical
	public static boolean same_board(Board_type board1, Board_type board2) {
		for (int x = 1; x <= 9; x++)
			for (int y = 1; y <= 9; y++)
				if (!(board1.locations[x][y] == board2.locations[x][y]))
					return false;
		return true;
	}

	// PROCEDURE copy_board: makes a copy of board1 in board2
	public static void copy_board(Board_type board1, Board_type board2) {
		for (int x = 1; x <= 9; x++)
			for (int y = 1; y <= 9; y++) {
				board2.locations[x][y] = board1.locations[x][y];
				board2.fixed[x][y] = board1.fixed[x][y];
			}
	}

	// PROCEDURE find_in_row: given a board, a row, and a digit, returns true if
	// digit is in the row
	public static boolean find_in_row(Board_type board, int row, int digit) {
		int col = 1;

		for (;;) {
			if (board.locations[col][row] == digit)
				return true;
			col = col + 1;
			if (col == 10)
				break;
		}
		return false;
	}

	// PROCEDURE find_in_col: given a board, a column, and a digit, returns true
	// if digit is in the column
	public static boolean find_in_col(Board_type board, int col, int digit) {
		int row = 1;

		for (;;) {
			if (board.locations[col][row] == digit)
				return true;
			row = row + 1;
			if (row == 10)
				break;
		}
		return false;
	}

	// PROCEDURE find_in_quad: given a board, a quadrant, and a digit, returns
	// true if digit is in the quadrant
	public static boolean find_in_quad(Board_type board, int quadrow,
			int quadcol, int digit) {
		int row = quadrow * 3 + 1;
		int col = quadcol * 3 + 1;
		int collimit = quadcol * 3 + 4;
		int rowlimit = quadrow * 3 + 4;

		for (;;) {
			if (board.locations[col][row] == digit)
				return true;
			col = col + 1;
			if (col == collimit) {
				col = quadcol * 3 + 1;
				row = row + 1;
			}
			if (row == rowlimit)
				break;
		}
		return false;
	}

	// FUNCTION scan_fill_rows: scans each row checking for digits that can only
	// go in one possible cell
	// any digit it finds that has only one possible cell gets inserted into
	// that cell
	// - if fill_in_all=false, only fill in the first value found
	public static int scan_fill_rows(Board_type board, boolean fill_in_all) {
		int count = 0;
		int col_loc, col;
		boolean nogood;

		for (int row = 1; row <= 9; row++) // loop on all the rows
		{
			for (int digit = 1; digit <= 9; digit++) // loop on all the
			// digits in each row
			{
				if (find_in_row(board, row, digit) == false) // if it's
				// here
				// already,
				// stop
				{
					col_loc = 0; // else, go to each column, check
					// quadrant && column
					nogood = false;
					col = 1;

					for (;;) {
						if (board.locations[col][row] == 0) // don't
							// bother
							// if
							// the
							// space
							// isn't
							// blank
							if ((find_in_quad(board, (row - 1) / 3,
									(col - 1) / 3, digit) == false)
									&& (find_in_col(board, col, digit) == false)) {
								if (col_loc > 0) // found a good spot. is
									// it the first one?
									nogood = true; // nope, so quit
								else
									col_loc = col; // yep, so record it
							}
						col = col + 1;
						if (nogood || col == 10) // exit if nogood, or done
							// all columns
							break;
					}

					if ((nogood == false) && (col_loc > 0)) // update the
					// board if a
					// spot found
					{
						if ((count == 0) || (fill_in_all == true))
							board.locations[col_loc][row] = digit;
						// System.out.println("found fr (", col_loc, ",", row,
						// ")=", digit
						count = count + 1;
					}
				}
			}
		}

		return count;
	}

	// FUNCTION scan_fill_cols: scans each column checking for digits that can
	// only go in one possible cell
	// any digit it finds that has only one possible cell gets inserted into
	// that cell
	// - if fill_in_all=false, only fill in the first value found
	public static int scan_fill_cols(Board_type board, boolean fill_in_all) {
		int count = 0;
		int row_loc, row;
		boolean nogood;

		for (int col = 1; col <= 9; col++) // loop on all the rows
		{
			// loop on all the digits in each row
			for (int digit = 1; digit <= 9; digit++) {
				// if it's already here stop
				if (find_in_col(board, col, digit) == false) {
					row_loc = 0; // else, go to each column, check
					// quadrant && column
					nogood = false;
					row = 1;

					for (;;) {
						// don't bother if the space isn't blank
						if (board.locations[col][row] == 0)
							if ((find_in_quad(board, (row - 1) / 3,
									(col - 1) / 3, digit) == false)
									&& (find_in_row(board, row, digit) == false))
								// found a good spot. is it the first one?
								if (row_loc > 0)
									// nope, so quit
									nogood = true;
								else
									// yep, so record it
									row_loc = row;
						row = row + 1;
						// exit if nogood or done all columns
						if (nogood || row == 10)
							break;
					}
					// update teh board if a spot found
					if ((nogood == false) && (row_loc > 0)) {
						if ((count == 0) || (fill_in_all == true))
							board.locations[col][row_loc] = digit;
						// System.out.println("found fc (", col, ",",
						// row_loc, ")=", digit
						count = count + 1;
					}
				}
			}
		}

		return count;
	}

	// FUNCTION scan_fill_quads: scans each quadrant checking for digits that
	// can only go in one possible cell
	// any digit it finds that has only one possible cell gets inserted into
	// that cell
	// - if fill_in_all=false, only fill in the first value found
	public static int scan_fill_quads(Board_type board, boolean fill_in_all) {
		int count = 0;
		int row, col, row_loc, col_loc, colstart, collimit, rowlimit;
		boolean nogood;

		for (int quadcol = 0; quadcol <= 2; quadcol++) {
			for (int quadrow = 0; quadrow <= 2; quadrow++) {
				// loop on all the digits in the quadrant
				for (int digit = 1; digit <= 9; digit++) {
					// if it's already here, stop
					if (find_in_quad(board, quadrow, quadcol, digit) == false) {
						row_loc = 0;
						col_loc = 0;
						nogood = false;
						colstart = quadcol * 3 + 1;
						collimit = quadcol * 3 + 4;
						rowlimit = quadrow * 3 + 4;
						col = colstart;
						row = quadrow * 3 + 1;
						for (;;) {
							// don't bother if the space isn't blank
							if (board.locations[col][row] == 0)
								if (nogood == false)
									if ((find_in_row(board, row, digit) == false)
											&& (find_in_col(board, col, digit) == false))
										// found a good spot. is it the first
										// one?
										if (row_loc > 0)
											// nope, soquit
											nogood = true;
										else {
											row_loc = row; // yep, so
											// record it
											col_loc = col;
										}
							// at some point, convert for to loop and exit when
							// nogood or whatever
							col = col + 1;
							if (col == collimit) {
								col = colstart;
								row = row + 1;
							}
							if ((row == rowlimit) || nogood)
								break;
						}
						// update the board if a spot found
						if ((nogood == false) && (row_loc > 0)) {
							if ((count == 0) || (fill_in_all == true))
								board.locations[col_loc][row_loc] = digit;
							// System.out.println("found fq (", col_loc,
							// ",", row_loc, ")=", digit
							count = count + 1;
						}
					}
				}
			}
		}
		return count;
	}

	// FUNCTION scan_fill_board: fills in the board as much as it can without
	// creating a markup or backtracking
	// easy && medium boards can be filled in using only this algorithm
	// - fast = false when you want a more meaningful score
	// - meancount = mean number of finds on each row, column, or quad scan
	// - bottlenecks = number of 0 finds on row, column, or quad scans
	// *** success, meancount, and bottlenecks replaced with returnValues
	public static Board_type scan_fill_board(Board_type board, boolean fast,
			SFBReturnValues returnValues) {
		int count, totalcount, loops;
		Board_type output = new Board_type();

		// System.out.println("scan fill board");

		copy_board(board, output);
		returnValues.success = false;

		loops = 0;
		returnValues.meancount = 0;
		returnValues.bottlenecks = 0;

		for (;;) {
			totalcount = 0;
			count = scan_fill_quads(output, fast);
			returnValues.meancount = returnValues.meancount + count;
			totalcount = totalcount + count;
			loops = loops + 1;
			if (count == 0)
				returnValues.bottlenecks = returnValues.bottlenecks + 1;
			// if count = 1
			// System.out.println("1"
			// if count = 0
			// System.out.println("0"
			count = scan_fill_cols(output, fast);
			returnValues.meancount = returnValues.meancount + count;
			totalcount = totalcount + count;
			loops = loops + 1;
			if (count == 0)
				returnValues.bottlenecks = returnValues.bottlenecks + 1;
			// if count = 1
			// System.out.println("1"
			// if count = 0
			// System.out.println("0"
			count = scan_fill_rows(output, fast);
			returnValues.meancount = returnValues.meancount + count;
			totalcount = totalcount + count;
			loops = loops + 1;
			if (count == 0)
				returnValues.bottlenecks = returnValues.bottlenecks + 1;
			// if count = 1
			// System.out.println("1"
			// if count = 0
			// System.out.println("0"
			if (totalcount == 0)
				break;
		}

		// System.out.println(loops

		returnValues.meancount = returnValues.meancount / loops;
		// subtract the useless scans on teh finished puzzle
		returnValues.bottlenecks = returnValues.bottlenecks - 3;
		if (full_board(output))
			returnValues.success = true;

		return output;
	}

	public static void clear_markups(boolean[][][] markups) {
		// System.out.println("Clearing markups");
		for (int row = 1; row <= 9; row++)
			for (int col = 1; col <= 9; col++)
				for (int digit = 1; digit <= 9; digit++)
					markups[col][row][digit] = false;
	}

	public static void get_markups(boolean[][][] markups, Board_type board) {
		// System.out.println("setting markups for...");
		// show_board(board);
		for (int row = 1; row <= 9; row++)
			for (int col = 1; col <= 9; col++)
				if (board.locations[col][row] == 0) {
					// System.out.println("blank location: "+col+" "+row);
					for (int digit = 1; digit <= 9; digit++)
						if (find_in_row(board, row, digit) == false) {
							// System.out.println("not found in row: "+digit);
							if (find_in_col(board, col, digit) == false) {
								// System.out.println("not found in col:
								// "+digit);
								if (find_in_quad(board, (row - 1) / 3,
										(col - 1) / 3, digit) == false) {
									// System.out.println("not found in quad
									// "+(row-1)/3+" "+(col-1)/3+": "+digit);
									markups[col][row][digit] = true;
									// System.out.println("Set Markup: "+col+"
									// "+row+" "+digit);
								}
							}
						}
				}
		// System.out.println("that's all folks");
	}

	// this procedure identifies non-exclusive doubles e.g. 1845 1824 within
	// rows columns and quads, and
	// reduces the markups to 18 18
	// *** converted deletedone to a return value
	public static boolean trim_markups2(boolean[][][] markups,
			Board_type board, int depth) {
		int count, qrow, qcol, next;
		int[] locs = new int[10], digits = new int[10], locx = new int[10], locy = new int[10];// zero
		// element
		// unused
		// System.out.println("trimming markups 2");
		boolean alreadyfound, good;

		boolean deletedone = false; // *** converted to local var

		// System.out.println(""
		// System.out.println("ROWS"
		for (int row = 1; row <= 9; row++) {
			for (int digit = 1; digit <= 9; digit++) {
				count = 0;
				for (int col = 1; col <= 9; col++)
					if (markups[col][row][digit]) {
						count = count + 1;
						locs[count] = col;
					}
				if (count == depth) // candidate first digit found
				{
					digits[1] = digit;
					next = 2;
					for (int digit2 = 1; digit2 <= 9; digit2++) {
						alreadyfound = false;
						for (int i = 1; i <= next - 1; i++)
							if (digit2 == digits[i])
								alreadyfound = true;
						if (!alreadyfound) {
							good = true;
							for (int i = 1; i <= depth; i++)
								if (!markups[locs[i]][row][digit2])
									good = false;
							if (good) {
								count = 0;
								for (int col = 1; col <= 9; col++)
									if (markups[col][row][digit2])
										count = count + 1;
								// another digit found
								if (count == depth) {
									digits[next] = digit2;
									next = next + 1;
								}
							}
						}
					}

					if (next == depth + 1)
						// clear out the extra markups if any
						for (int digit3 = 1; digit3 <= 9; digit3++) {
							good = true;
							for (int i = 1; i <= depth; i++)
								if (digit3 == digits[i])
									good = false;

							if (good)
								for (int i = 1; i <= depth; i++)
									if (markups[locs[i]][row][digit3]) {
										deletedone = true;
										markups[locs[i]][row][digit3] = false;
										// System.out.println("m2-", depth
									}
						}
				}
			}
		}

		// System.out.println(""
		// System.out.println("COLS"

		for (int col = 1; col <= 9; col++) {
			for (int digit = 1; digit <= 9; digit++) {
				count = 0;
				for (int row = 1; row <= 9; row++)
					if (markups[col][row][digit]) {
						count = count + 1;
						locs[count] = row;
					}
				if (count == depth) // candidate first digit found
				{
					digits[1] = digit;
					next = 2;
					for (int digit2 = 1; digit2 <= 9; digit2++) {
						alreadyfound = false;
						for (int i = 1; i <= next - 1; i++)
							if (digit2 == digits[i])
								alreadyfound = true;
						if (!alreadyfound) {
							good = true;
							for (int i = 1; i <= depth; i++)
								if (!markups[col][locs[i]][digit2])
									good = false;
							if (good) {
								count = 0;
								for (int row = 1; row <= 9; row++)
									if (markups[col][row][digit2])
										count = count + 1;
								// another digit found
								if (count == depth) {
									digits[next] = digit2;
									next = next + 1;
								}
							}
						}
					}

					if (next == depth + 1) {
						// clear out the estra markups if any
						for (int digit3 = 1; digit3 <= 9; digit3++) {
							good = true;
							for (int i = 1; i <= depth; i++)
								if (digit3 == digits[i])
									good = false;

							if (good)
								for (int i = 1; i <= depth; i++)
									if (markups[col][locs[i]][digit3]) {
										deletedone = true;
										markups[col][locs[i]][digit3] = false;
										// System.out.println("m2-", depth
									}
						}
					}
				}
			}
		}

		// System.out.println(""
		// System.out.println("QUADS"

		for (int quadx = 0; quadx <= 2; quadx++) {
			for (int quady = 0; quady <= 2; quady++) {
				for (int digit = 1; digit <= 9; digit++) {
					count = 0;
					qrow = quady * 3 + 1;
					qcol = quadx * 3 + 1;
					for (;;) {
						if (markups[qcol][qrow][digit]) {
							count = count + 1;
							locy[count] = qrow;
							locx[count] = qcol;
						}
						qcol = qcol + 1;
						if (qcol == quadx * 3 + 4) {
							qcol = quadx * 3 + 1;
							qrow = qrow + 1;
						}
						if (qrow == quady * 3 + 4)
							break;
					}
					if (count == depth) // candidate first digit found
					{
						digits[1] = digit;
						next = 2;
						for (int digit2 = 1; digit2 <= 9; digit2++) {
							alreadyfound = false;
							for (int i = 1; i <= next - 1; i++)
								if (digit2 == digits[i])
									alreadyfound = true;
							if (!alreadyfound) {
								good = true;
								for (int i = 1; i <= depth; i++)
									if (!markups[locx[i]][locy[i]][digit2])
										good = false;
								if (good) {
									count = 0;
									qrow = quady * 3 + 1;
									qcol = quadx * 3 + 1;
									for (;;) {
										if (markups[qcol][qrow][digit2])
											count = count + 1;
										qcol = qcol + 1;
										if (qcol == quadx * 3 + 4) {
											qcol = quadx * 3 + 1;
											qrow = qrow + 1;
										}
										if (qrow == quady * 3 + 4)
											break;
									}
									// another digit found
									if (count == depth) {
										digits[next] = digit2;
										next = next + 1;
									}
								}
							}
						}

						if (next == depth + 1)
							// clear out the extra markups if any
							for (int digit3 = 1; digit3 <= 9; digit3++) {
								good = true;
								for (int i = 1; i <= depth; i++)
									if (digit3 == digits[i])
										good = false;
								if (good)
									for (int i = 1; i <= depth; i++)
										if (markups[locx[i]][locy[i]][digit3]) {
											deletedone = true;
											markups[locx[i]][locy[i]][digit3] = false;
											// System.out.println("qq-", depth
										}
							}
					}
				}
			}
		}

		return deletedone; // *** added.
	}

	// this procedure identifies multiples e.g. 18 18 or 145 145 145 within rows
	// columns and quads, and removes
	// 1's and 8's from the markups in the rest of the row, column or quad.
	// depth: 2=doubles, 3=triples, etc. 1=illegal
	// *** converted deletedone to a return value
	public static boolean trim_markups(boolean[][][] markups, Board_type board,
			int depth) {
		boolean newloc;
		int count;
		int[] nums = new int[10], locs = new int[10], rowlocs = new int[10], collocs = new int[10]; // zero
		// element
		// unused
		int qrow, qcol, qrow2, qcol2, next;

		// System.out.println("trimming markups 1");

		// foundone = false;
		boolean deletedone = false; // *** added

		// System.out.println(""
		// System.out.println("ROWS"
		for (int row = 1; row <= 9; row++) {
			for (int start = 1; start <= 8; start++) {
				for (int i = 1; i <= 9; i++)
					locs[i] = 0;
				next = 1;
				for (int col = start; col <= 9; col++) {
					if (board.locations[col][row] == 0) {
						if (next == 1) {
							count = 0;
							for (int digit = 1; digit <= 9; digit++)
								if (markups[col][row][digit])
									count = count + 1;
							// System.out.println(count ..
							if (count == depth) {
								locs[next] = col;
								next = next + 1;
							}
						} else {
							if (locs[next] == 0) {
								locs[next] = col;
								for (int digit = 1; digit <= 9; digit++)
									if (!(markups[locs[1]][row][digit] == markups[col][row][digit]))
										locs[next] = 0;
								if (locs[next] > 0)
									next = next + 1;
								// if second = 0
								// System.out.println("." ..
								// else
								// System.out.println("M" ..
								// else
								// System.out.println("." ..
							}
							// if (locs[depth] > 0)
							// foundone = true;
						}
						// else
						// System.out.println("0" ..
					}
				}
				if (locs[depth] > 0) {
					for (int i = 1; i <= depth; i++)
						nums[i] = 0;
					next = 1;
					for (int digit = 1; digit <= 9; digit++)
						if (markups[locs[1]][row][digit]) {
							nums[next] = digit;
							next = next + 1;
						}
					for (int col = 1; col <= 9; col++) {
						newloc = true;
						for (int i = 1; i <= depth; i++)
							if (col == locs[i])
								newloc = false;
						if (newloc)
							for (int i = 1; i <= depth; i++)
								if (markups[col][row][nums[i]]) {
									deletedone = true;
									markups[col][row][nums[i]] = false;
								}
					}
				}
				// System.out.println(""
			}
		}

		// System.out.println(""
		// System.out.println("COLS"

		for (int col = 1; col <= 9; col++) {
			for (int start = 1; start <= 8; start++) {
				for (int i = 1; i <= 9; i++)
					locs[i] = 0;
				next = 1;
				for (int row = start; row <= 9; row++) {
					if (board.locations[col][row] == 0) {
						if (next == 1) {
							count = 0;
							for (int digit = 1; digit <= 9; digit++)
								if (markups[col][row][digit])
									count = count + 1;
							// System.out.println(count ..
							if (count == depth) {
								locs[next] = row;
								next = next + 1;
							}
						} else {
							if (locs[next] == 0) {
								locs[next] = row;
								for (int digit = 1; digit <= 9; digit++)
									if (!(markups[col][locs[1]][digit] == markups[col][row][digit]))
										locs[next] = 0;
								if (locs[next] > 0)
									next = next + 1;
								// if second = 0
								// System.out.println("." ..
								// else
								// System.out.println("M" ..
								// else
								// System.out.println("." ..
							}
							// if (locs[depth] > 0)
							// foundone = true;
						}
						// else
						// System.out.println("0" ..
					}
				}

				if (locs[depth] > 0) {
					for (int i = 1; i <= depth; i++)
						nums[i] = 0;
					next = 1;
					for (int digit = 1; digit <= 9; digit++)
						if (markups[col][locs[1]][digit]) {
							nums[next] = digit;
							next = next + 1;
						}
					for (int row = 1; row <= 9; row++) {
						newloc = true;
						for (int i = 1; i <= depth; i++)
							if (row == locs[i])
								newloc = false;
						if (newloc)
							for (int i = 1; i <= depth; i++)
								if (markups[col][row][nums[i]]) {
									deletedone = true;
									markups[col][row][nums[i]] = false;
								}
					}
				}
				// System.out.println(""
			}
		}
		// System.out.println(""

		// System.out.println(""
		// System.out.println("QUADS"

		for (int quadx = 0; quadx <= 2; quadx++) {
			for (int quady = 0; quady <= 2; quady++) {
				for (int start = 1; start <= 7; start++) {

					for (int i = 1; i <= 9; i++) {
						rowlocs[i] = 0;
						collocs[i] = 0;
					}
					next = 1;

					qrow = quady * 3 + 1;
					qcol = quadx * 3 + 1;

					for (int x = 1; x <= start; x++) {
						qcol = qcol + 1;
						if (qcol == quadx * 3 + 4) {
							qcol = quadx * 3 + 1;
							qrow = qrow + 1;
						}
					}
					for (;;) {
						if (board.locations[qcol][qrow] == 0) {
							if (next == 1) {
								count = 0;
								for (int digit = 1; digit <= 9; digit++)
									if (markups[qcol][qrow][digit])
										count = count + 1;
								// System.out.println(count ..
								if (count == depth) {
									rowlocs[next] = qrow;
									collocs[next] = qcol;
									next = next + 1;
								}
							} else {
								if (rowlocs[next] == 0) {
									rowlocs[next] = qrow;
									collocs[next] = qcol;
									for (int digit = 1; digit <= 9; digit++)
										if (!(markups[collocs[1]][rowlocs[1]][digit] == markups[qcol][qrow][digit])) {
											rowlocs[next] = 0;
											collocs[next] = 0;
										}
									if (rowlocs[next] > 0)
										next = next + 1;

									// if secondrow = 0
									// System.out.println("." ..
									// else
									// System.out.println("M" ..
									// else
									// System.out.println("." ..
								}
								// if (rowlocs[depth] > 0)
								// foundone = true;
							}
							// else
							// System.out.println("0" ..
						}
						qcol = qcol + 1;
						if (qcol == quadx * 3 + 4) {
							qcol = quadx * 3 + 1;
							qrow = qrow + 1;
						}
						if (qrow == quady * 3 + 4)
							break;
					}
					if (rowlocs[depth] > 0) {
						for (int i = 1; i <= depth; i++)
							nums[i] = 0;
						next = 1;
						for (int digit = 1; digit <= 9; digit++)
							if (markups[collocs[1]][rowlocs[1]][digit]) {
								nums[next] = digit;
								next = next + 1;
							}
						qrow2 = quady * 3 + 1;
						qcol2 = quadx * 3 + 1;
						for (;;) {
							newloc = true;
							for (int i = 1; i <= depth; i++)
								if ((qrow2 == rowlocs[i])
										&& (qcol2 == collocs[i]))
									newloc = false;
							if (newloc)
								for (int i = 1; i <= depth; i++)
									if (markups[qcol2][qrow2][nums[i]]) {
										deletedone = true;
										markups[qcol2][qrow2][nums[i]] = false;
									}
							qcol2 = qcol2 + 1;
							if (qcol2 == quadx * 3 + 4) {
								qcol2 = quadx * 3 + 1;
								qrow2 = qrow2 + 1;
							}
							if (qrow2 == quady * 3 + 4)
								break;
						}
					}
					// System.out.println(""
				}
			}
		}
		// System.out.println(""

		return deletedone; // *** added
	}

	public static Board_type find_single_markups(boolean[][][] markups,
			Board_type board, FSMFlags returnFlags) {
		// System.out.println("find_single_markups");

		Board_type output = new Board_type();
		int digit, found, count, location = -1, row2, col2, locationr = -1, locationc = -1;
		boolean ok;

		copy_board(board, output);
		returnFlags.foundone = false;

		// searches for pure singles

		for (int row = 1; row <= 9; row++) {
			for (int col = 1; col <= 9; col++) {
				if (output.locations[col][row] == 0) {
					found = 0;
					ok = true;
					digit = 1;
					for (;;) {
						if (markups[col][row][digit]) {
							// System.out.println("found markup: "+col+" "+row+"
							// "+digit);
							if (found == 0)
								found = digit;
							else
								ok = false;
						}
						digit = digit + 1;
						if ((ok == false) || (digit == 10))
							break;
					}
					if (ok) {
						// System.out.println("found smp (" + col + "," + row +
						// ")=" + found);
						// show_board (output);
						output.locations[col][row] = found;
						// show_board(output);
						returnFlags.foundone = true;
					}
				}
			}
		}

		// searches rows for markups that appear only once
		for (int row = 1; row <= 9; row++)
			for (int dig = 1; dig <= 9; dig++) {
				count = 0;
				for (int col = 1; col <= 9; col++)
					if (markups[col][row][dig]) {
						count = count + 1;
						location = col;
					}
				// System.out.println(count);
				if ((count == 1) && (output.locations[location][row] == 0)) {
					output.locations[location][row] = dig;
					returnFlags.foundone = true;
					// System.out.println("found smr ("+ location+ ","+
					// row+")="+ dig);
					// show_board (output)
				}
			}

		// searches columns for markups that appear only once
		for (int col = 1; col <= 9; col++)
			for (int dig = 1; dig <= 9; dig++) {
				count = 0;
				for (int row = 1; row <= 9; row++)
					if (markups[col][row][dig]) {
						count = count + 1;
						location = row;
					}
				if ((count == 1) && (output.locations[col][location] == 0)) {
					output.locations[col][location] = dig;
					returnFlags.foundone = true;
					// System.out.println("found smc ("+ col+ ","+ location+
					// ")="+ dig);
					// show_board (output)
				}
			}

		// searches quads for markups that appear only once
		for (int quadx = 0; quadx <= 2; quadx++)
			for (int quady = 0; quady <= 2; quady++)
				for (int dig = 1; dig <= 9; dig++) {
					count = 0;
					col2 = quadx * 3 + 1;
					row2 = quady * 3 + 1;
					for (;;) {
						if (markups[col2][row2][dig]) {
							count = count + 1;
							locationr = row2;
							locationc = col2;
						}
						col2 = col2 + 1;
						if (col2 == quadx * 3 + 4) {
							col2 = quadx * 3 + 1;
							row2 = row2 + 1;
						}
						if (row2 == quady * 3 + 4)
							break;
					}
					if ((count == 1)
							&& (output.locations[locationc][locationr] == 0)) {
						output.locations[locationc][locationr] = dig;
						returnFlags.foundone = true;
						// System.out.println("found smq ("+ locationc+ ","+
						// locationr+ ")="+ dig);
						// show_board (output)
					}
				}

		return output;
	}

	public static Board_type markup_fill_board(Board_type board, int depth,
			MFBReturnValues returnValues) {
		Board_type output = new Board_type();
		boolean[][][] markups = new boolean[10][10][10]; // markup_type
		boolean foundone, deletedone, deletedone2, deleted, exitflag;
		int old1, old2, current;
		// System.out.println("start markup_fill_board");

		copy_board(board, output);
		returnValues.progress = false;
		returnValues.success = false;
		// deletedone = false
		// deletedone2 = false
		deleted = false;
		returnValues.totalscan = 0;

		old2 = 0;
		old1 = 0;
		current = cell_count(output);

		for (;;) {
			if (old1 > 0) {
				// scan_fill_board conversion
				SFBReturnValues sfbReturnValues = new SFBReturnValues();
				output = scan_fill_board(output, true, sfbReturnValues);
				returnValues.success = sfbReturnValues.success;
				// score = sfbReturnValues.meancount;
				// bottlenecks = sfbReturnValues.bottlenecks;
				// END
				returnValues.totalscan = returnValues.totalscan
						+ cell_count(output) - current;
			}
			if (deleted == false) {
				clear_markups(markups);
				get_markups(markups, output);
			}
			foundone = false;
			// find_single_markups conversion
			FSMFlags fsmReturnValues = new FSMFlags();
			output = find_single_markups(markups, output, fsmReturnValues);
			foundone = fsmReturnValues.foundone;
			// END

			// System.out.println(foundone);
			if (foundone)
				returnValues.progress = true;

			if (depth > 1) {
				if (foundone) {
					clear_markups(markups);
					get_markups(markups, output);
				}

				deleted = false;
				for (;;) {
					exitflag = true;
					for (int d = 6; d >= 2; d--) // for decreasing d : 6 .. 2
					{
						// trim_markups2 conversion
						deletedone2 = trim_markups2(markups, output, d);
						// END
						if (deletedone2) {
							// System.out.println("2-", d
							deleted = true;
							exitflag = false;
						}
					}
					for (int d = 6; d >= 2; d--) // for decreasing d : 6 .. 2
					{
						// trim_markups2 conversion
						deletedone = trim_markups(markups, output, d);
						// END
						if (deletedone) {
							// System.out.println("1-", d
							deleted = true;
							exitflag = false;
						}
					}
					if (exitflag)
						break;
				}
			}
			old2 = old1;
			old1 = current;
			current = cell_count(output);

			if (current == old2) // twice through loop with no change
				// means abort
				break;
		}

		if (full_board(output))
			returnValues.success = true;
		// System.out.println("done markup_fill_board");
		return output;
	}

	public static boolean full_row(Board_type board, int row) {
		int col = 1;
		boolean output = true;

		for (;;) {
			if (board.locations[col][row] == 0)
				output = false;
			col = col + 1;
			if ((col == 10) || (!output))
				break;
		}
		return output;
	}

	public static boolean full_col(Board_type board, int col) {
		int row = 1;
		boolean output = true;

		for (;;) {
			if (board.locations[col][row] == 0)
				output = false;
			row = row + 1;
			if ((row == 10) || (!output))
				break;
		}

		return output;
	}

	public static boolean full_quad(Board_type board, int quadx, int quady) {
		int row = quady * 3 + 1;
		int col = quadx * 3 + 1;
		boolean output = true;

		for (;;) {
			if (board.locations[col][row] == 0)
				output = false;
			col = col + 1;
			if (col == quadx * 3 + 4) {
				col = quadx * 3 + 1;
				row = row + 1;
			}
			if ((row == quady * 3 + 4) || (!output))
				break;
		}
		return output;
	}

	public static Board_type solver(Board_type board,
			SolverReturnValues returnValues, boolean bt) {
		Board_type output = new Board_type();
		int cc, cc2, subscan;
		boolean /* progress, */initialscandone;

		copy_board(board, output);

		returnValues.markup1 = 0;
		returnValues.markup2 = 0;
		returnValues.backtrack = 0;
		returnValues.unique = true;
		returnValues.totalscan = 0;
		returnValues.initialscan = 0;
		returnValues.success = false;
		initialscandone = false;

		cc = cell_count(output);
		returnValues.given = cc;

		// for (;;) {
		// System.out.println("scan: ", cc
		// scan_fill_board convert
		SFBReturnValues sfbReturnValues = new SFBReturnValues();
		output = scan_fill_board(output, true, sfbReturnValues);
		returnValues.success = sfbReturnValues.success;
		// score = sfbReturnValues.meancount;
		// bottlenecks = sfbReturnValues.bottlenecks;
		// END
		cc2 = cc;
		cc = cell_count(output);
		returnValues.totalscan = returnValues.totalscan + cc - cc2;

		if (!initialscandone) {
			initialscandone = true;
			returnValues.initialscan = returnValues.totalscan;
		}

		if (!returnValues.success) {
			// System.out.println("markup 1: "+ cc + "
			// "+score_board(output));
			// show_board(output);
			// markup_fill_board convert
			MFBReturnValues mfbReturnValues = new MFBReturnValues();
			output = markup_fill_board(output, 1, mfbReturnValues);
			subscan = mfbReturnValues.totalscan;
			// progress = mfbReturnValues.progress;
			returnValues.success = mfbReturnValues.success;
			// END
			cc2 = cc;
			cc = cell_count(output);
			returnValues.markup1 = returnValues.markup1 + cc - cc2 - subscan;
			returnValues.totalscan = returnValues.totalscan + subscan;
		}
		if (!returnValues.success) {
			// System.out.println("markup 2: "+ cc);
			// markup_fill_board convert
			MFBReturnValues mfbReturnValues = new MFBReturnValues();
			output = markup_fill_board(output, 2, mfbReturnValues);
			subscan = mfbReturnValues.totalscan;
			// progress = mfbReturnValues.progress;
			returnValues.success = mfbReturnValues.success;
			// END cc2 = cc;
			cc = cell_count(output);
			returnValues.markup2 = returnValues.markup2 + cc - cc2 - subscan;
			returnValues.totalscan = returnValues.totalscan + subscan;
		}
		if (bt)
			if (!returnValues.success /* && !progress */) {
				//System.out.println("backtrack");
				// System.out.println("");
				// System.out.println("");
				// System.out.println("this is as far as I can go without
				// trial and error...");
				// show_board(output);
				// System.out.println("... backtracking");
				// backtrack_board convert
				BTBReturnFlags btbReturnValues = new BTBReturnFlags();
				output = backtrack_board(output, 1, 1, 9, 9, btbReturnValues);
				returnValues.success = btbReturnValues.success;
				// System.out.println(returnValues.success);
				returnValues.unique = btbReturnValues.unique;
				// END
				if (returnValues.success) {
					cc2 = cc;
					cc = cell_count(output);
					returnValues.backtrack = returnValues.markup1 + cc - cc2;
				}
			}
		// if (returnValues.success || (!progress))
		// break;
		// System.out.println("loop");
		// }
		// System.out.println("solver done: ", cell_count (output)

		return output;
	}

	// PROCEDURE blank_out_unique_easy: blanks out board to a maximum number of
	// clues
	// - uses the scan_fill_board to decide whether the board has a UNIQUE AND
	// EASY solution
	// - final board is guaranteed to have a unique solution AND SHOULD BE EASY
	// - "clues" = prefered minimum number of clues in the board
	// - uses the "fixed" part of "board_type" to show which are clues, which
	// are not
	public static void blank_out_unique_easy(Board_type board, int clues,
			double minscore) {
		int x, y, tempi, count;
		boolean success = false, nochange;
		int[] rows = new int[82], cols = new int[82];// zero element unused :
		// array 1 .. 81 of int
		double score;
		int counter;

		// System.out.println("Reducing Clues: " ..

		// init number of clues in board currently
		count = 81;
		counter = 1;
		// this loop blanks out clues randomly until it finds one that makes the
		// board non-unique
		for (;;) {
			x = (int) (Math.random() * 9) + 1;
			y = (int) (Math.random() * 9) + 1;

			if (board.locations[x][y] > 0) {
				count = count - 1;
				tempi = board.locations[x][y];
				board.locations[x][y] = 0;
				// board.fixed [x] [y] = false
				// scan_fill_board convert
				SFBReturnValues sfbReturnValues = new SFBReturnValues();
				scan_fill_board(board, true, sfbReturnValues);
				success = sfbReturnValues.success;
				score = sfbReturnValues.meancount;
				// bottlenecks = sfbReturnValues.bottlenecks;
				// END
				if (!success) {
					board.locations[x][y] = tempi;
					// board.fixed [x] [y] = true
					count = count + 1;
				}
			}
			counter = counter + 1;
			if ((counter > 40) && (!success))
				break;
		}

		// initialize the rows and cols arrays to all possible cell coordinates
		for (int i = 1; i <= 81; i++) {
			rows[i] = (i + 8) / 9;
			cols[i] = ((i - 1) % 9) + 1;
			// System.out.println(rows[i],cols[i]," "..
		}
		for (;;) // start randomly cycling through all cells until no
		// further changes can be made
		{
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 81; i++) {
				x = (int) (Math.random() * 81) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 81; i++) {
				if (count > clues)
					if (board.locations[cols[i]][rows[i]] > 0) {
						count = count - 1;
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						// board.fixed [cols [i]] [rows [i]] = false
						// scan_fill_board convert
						SFBReturnValues sfbReturnValues = new SFBReturnValues();
						scan_fill_board(board, true, sfbReturnValues);
						success = sfbReturnValues.success;
						score = sfbReturnValues.meancount;
						// bottlenecks = sfbReturnValues.bottlenecks;
						// END
						if (score < minscore)
							success = false;
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							// board.fixed [cols [i]] [rows [i]] = true
							count = count + 1;
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
			}
			// System.out.println(""
			if (nochange)
				break;
		}
	}

	// PROCEDURE blank_out_unique_hard: blanks out board to make it hard
	// - uses the solver to decide whether the board has a UNIQUE AND EASY
	// solution
	// - final board is guaranteed to have a unique solution AND SHOULD BE HARD
	// - "clues" = prefered minimum number of clues in the board
	// - uses the "fixed" part of "board_type" to show which are clues, which
	// are not
	public static void blank_out_unique_hard(Board_type board, int clues) {
		int x, y, tempi, count, markup2;
		boolean success = false, nochange;
		int[] rows = new int[82], cols = new int[82];// zero element unused :
		// array 1 .. 81 of int
		int counter;

		// System.out.println("Reducing Clues: " ..

		// init number of clues in board currently
		count = 81;
		counter = 0;

		// this loop blanks out clues randomly until it finds one that makes the
		// board non-unique
		for (;;) {
			x = (int) (Math.random() * 9) + 1;
			y = (int) (Math.random() * 9) + 1;

			if (board.locations[x][y] > 0) {
				count = count - 1;
				tempi = board.locations[x][y];
				board.locations[x][y] = 0;
				// board.fixed [x] [y] = false
				// solver convert
				SolverReturnValues solverReturnValues = new SolverReturnValues();
				solver(board, solverReturnValues, false);
				success = solverReturnValues.success;
				// given = solverReturnValues.given;
				// initialscan = solverReturnValues.initialscan;
				// scan = solverReturnValues.totalscan;
				// markup1 = solverReturnValues.markup1;
				markup2 = solverReturnValues.markup2;
				// backtrack = solverReturnValues.backtrack;
				// unique = solverReturnValues.unique;
				// END
				// temp = scan_fill_board (board, true, success, score,
				// bottlenecks)
				if (!success) {
					board.locations[x][y] = tempi;
					// board.fixed [x] [y] = true
					count = count + 1;
				}
			}
			counter = counter + 1;
			if ((counter > 10) && (!success))
				break;
		}
		// initialize the rows and cols arrays to all possible cell coordinates
		for (int i = 1; i <= 81; i++) {
			rows[i] = (i + 8) / 9;
			cols[i] = ((i - 1) % 9) + 1;
			// System.out.println(rows[i],cols[i]," "..
		}
		for (;;) // start randomly cycling through all cells until no
		// further changes can be made
		{
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 81; i++) {
				x = (int) (Math.random() * 81) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 81; i++) {
				if (count > clues)
					if (board.locations[cols[i]][rows[i]] > 0) {
						count = count - 1;
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						// board.fixed [cols [i]] [rows [i]] = false
						// solver convert
						SolverReturnValues solverReturnValues = new SolverReturnValues();
						solver(board, solverReturnValues, false);
						success = solverReturnValues.success;
						// given = solverReturnValues.given;
						// initialscan = solverReturnValues.initialscan;
						// scan = solverReturnValues.totalscan;
						// markup1 = solverReturnValues.markup1;
						markup2 = solverReturnValues.markup2;
						// backtrack = solverReturnValues.backtrack;
						// unique = solverReturnValues.unique;
						// END
						// temp = scan_fill_board (board, false, success, score,
						// bottlenecks)
						if (markup2 == 0)
							success = false;
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							// board.fixed [cols [i]] [rows [i]] = true
							count = count + 1;
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
			}
			// System.out.println(""
			if (nochange)
				break;
		}
	}

	// PROCEDURE blank_out_unique_easy_symmetrical: blanks out board to a
	// maximum number of clues
	// - uses the scan_fill_board to decide whether the board has a UNIQUE AND
	// EASY solution
	// - produces SYMMETRICAL boards
	// - final board is guaranteed to have a unique solution AND SHOULD BE EASY
	// - "clues" = prefered minimum number of clues in the board
	// - uses the "fixed" part of "board_type" to show which are clues, which
	// are not
	public static void blank_out_unique_easy_symmetrical(Board_type board,
			int clues, double minscore) {
		int x, y, tempi = -1, tempj = -1, tempk = -1, templ = -1, count;
		boolean success = false, nochange;
		int[] rows = new int[82], cols = new int[82]; // zero element unused :
		// array 1 .. 81 of int
		double score;
		int counter;

		// System.out.println("Reducing Clues: " ..

		// init number of clues in board currently
		count = 81;
		counter = 0;

		// this loop blanks out clues randomly until it finds one that makes the
		// board non-unique
		for (;;) {
			x = (int) (Math.random() * 4) + 1;
			y = (int) (Math.random() * 4) + 1;

			if (board.locations[x][y] > 0) {
				count = count - 1;
				tempi = board.locations[x][y];
				if (x < 5) {
					tempj = board.locations[10 - x][y];
					count = count - 1;
				}
				if (y < 5) {
					tempk = board.locations[x][10 - y];
					count = count - 1;
				}
				if ((x < 5) && (y < 5)) {
					templ = board.locations[10 - x][10 - y];
					count = count - 1;
				}
				board.locations[x][y] = 0;
				board.locations[10 - x][y] = 0;
				board.locations[x][10 - y] = 0;
				board.locations[10 - x][10 - y] = 0;
				// board.fixed [x] [y] = false
				// scan_fill_board convert
				SFBReturnValues sfbReturnValues = new SFBReturnValues();
				scan_fill_board(board, true, sfbReturnValues);
				success = sfbReturnValues.success;
				score = sfbReturnValues.meancount;
				// bottlenecks = sfbReturnValues.bottlenecks;
				// END
				if (!success) {
					board.locations[x][y] = tempi;
					count = count + 1;
					if (x < 5) {
						board.locations[10 - x][y] = tempj;
						count = count + 1;
					}
					if (y < 5) {
						board.locations[x][10 - y] = tempk;
						count = count + 1;
					}
					if ((x < 5) && (y < 5)) {
						board.locations[10 - x][10 - y] = templ;
						count = count + 1;
					}
					// board.fixed [x] [y] = true
				}
			}
			counter = counter + 1;
			if ((counter > 40) && (!success))
				break;
		}
		// initialize the rows and cols arrays to all possible cell coordinates
		for (int i = 1; i <= 25; i++) {
			rows[i] = (i + 4) / 5;
			cols[i] = ((i - 1) % 5) + 1;
			// System.out.println(rows[i],cols[i]," "..
		}
		for (;;) // start randomly cycling through all cells until no
		// further changes can be made
		{
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 25; i++) {
				x = (int) (Math.random() * 25) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 25; i++) {
				if (count > clues)
					if (board.locations[cols[i]][rows[i]] > 0) {
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						count = count - 1;
						if (cols[i] < 5) {
							tempj = board.locations[10 - cols[i]][rows[i]];
							board.locations[10 - cols[i]][rows[i]] = 0;
							count = count - 1;
						}
						if (rows[i] < 5) {
							tempk = board.locations[cols[i]][10 - rows[i]];
							board.locations[cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						if ((cols[i] < 5) && (rows[i] < 5)) {
							templ = board.locations[10 - cols[i]][10 - rows[i]];
							board.locations[10 - cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						// board.fixed [cols [i]] [rows [i]] = false
						// scan_fill_board convert
						SFBReturnValues sfbReturnValues = new SFBReturnValues();
						scan_fill_board(board, true, sfbReturnValues);
						success = sfbReturnValues.success;
						score = sfbReturnValues.meancount;
						// bottlenecks = sfbReturnValues.bottlenecks;
						// END
						if (score < minscore)
							success = false;
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							count = count + 1;
							if (cols[i] < 5) {
								board.locations[10 - cols[i]][rows[i]] = tempj;
								count = count + 1;
							}
							if (rows[i] < 5) {
								board.locations[cols[i]][10 - rows[i]] = tempk;
								count = count + 1;
							}
							if ((cols[i] < 5) && (rows[i] < 5)) {
								board.locations[10 - cols[i]][10 - rows[i]] = templ;
								count = count + 1;
							}
							// board.fixed [cols [i]] [rows [i]] = true
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
			}
			// System.out.println(""
			if (nochange)
				break;
		}
		for (;;) // same again, but only two at a time
		{
			// initialize the rows and cols arrays to all possible cell
			// coordinates
			for (int i = 0; i <= 8; i++)
				for (int j = 0; j <= 4; j++) {
					rows[j * 9 + i + 1] = j + 1;
					cols[j * 9 + i + 1] = i + 1;
					// System.out.println(rows[i],cols[i]," "..
				}
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 45; i++) {
				x = (int) (Math.random() * 45) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 45; i++)
				if (count > clues)
					if (board.locations[cols[i]][rows[i]] > 0) {
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						count = count - 1;
						if (!((cols[i] == 5) && (rows[i] == 5))) {
							templ = board.locations[10 - cols[i]][10 - rows[i]];
							board.locations[10 - cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						// board.fixed [cols [i]] [rows [i]] = false
						// scan_fill_board convert
						SFBReturnValues sfbReturnValues = new SFBReturnValues();
						scan_fill_board(board, true, sfbReturnValues);
						success = sfbReturnValues.success;
						score = sfbReturnValues.meancount;
						// bottlenecks = sfbReturnValues.bottlenecks;
						// END
						if (score < minscore)
							success = false;
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							count = count + 1;
							if (!((cols[i] == 5) && (rows[i] == 5))) {
								board.locations[10 - cols[i]][10 - rows[i]] = templ;
								count = count + 1;
							}
							// board.fixed [cols [i]] [rows [i]] = true
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
			// System.out.println(""
			if (nochange)
				break;
		}
	}

	// PROCEDURE blank_out_unique_hard_symmetrical: blanks out board to a
	// maximum number of clues
	// - uses the scan_fill_board to decide whether the board has a UNIQUE AND
	// EASY solution
	// - produces SYMMETRICAL boards
	// - final board is guaranteed to have a unique solution AND SHOULD BE EASY
	// - "clues" = prefered minimum number of clues in the board
	// - uses the "fixed" part of "board_type" to show which are clues, which
	// are not
	public static void blank_out_unique_hard_symmetrical(Board_type board,
			int clues) {
		int x, y, tempi = -1, tempj = -1, tempk = -1, templ = -1, count, markup2;
		boolean success = false, nochange;
		int[] rows = new int[82], cols = new int[82]; // zero element unused :
		// array 1 .. 81 of int
		int counter;

		// System.out.println("Reducing Clues: " ..

		// init number of clues in board currently
		count = 81;
		counter = 0;
		// show_board(board);
		// System.out.println(score_board(board));
		// this loop blanks out clues randomly until it finds one that makes the
		// board non-unique
		for (;;) {
			x = (int) (Math.random() * 4) + 1;
			y = (int) (Math.random() * 4) + 1;

			if (board.locations[x][y] > 0) {
				count = count - 1;
				tempi = board.locations[x][y];
				if (x < 5) {
					tempj = board.locations[10 - x][y];
					count = count - 1;
				}
				if (y < 5) {
					tempk = board.locations[x][10 - y];
					count = count - 1;
				}
				if ((x < 5) && (y < 5)) {
					templ = board.locations[10 - x][10 - y];
					count = count - 1;
				}
				board.locations[x][y] = 0;
				board.locations[10 - x][y] = 0;
				board.locations[x][10 - y] = 0;
				board.locations[10 - x][10 - y] = 0;
				// board.fixed [x] [y] = false
				// solver convert
				SolverReturnValues solverReturnValues = new SolverReturnValues();
				solver(board, solverReturnValues, false);
				success = solverReturnValues.success;
				// given = solverReturnValues.given;
				// initialscan = solverReturnValues.initialscan;
				// scan = solverReturnValues.totalscan;
				// markup1 = solverReturnValues.markup1;
				markup2 = solverReturnValues.markup2;
				// backtrack = solverReturnValues.backtrack;
				// unique = solverReturnValues.unique;
				// END
				if (!success) {
					board.locations[x][y] = tempi;
					count = count + 1;
					if (x < 5) {
						board.locations[10 - x][y] = tempj;
						count = count + 1;
					}
					if (y < 5) {
						board.locations[x][10 - y] = tempk;
						count = count + 1;
					}
					if ((x < 5) && (y < 5)) {
						board.locations[10 - x][10 - y] = templ;
						count = count + 1;
					}
					// board.fixed [x] [y] = true
				}
			}
			counter = counter + 1;
			if ((counter > 10) && (!success))
				break;
		}
		// show_board(board);

		// initialize the rows and cols arrays to all possible cell coordinates
		for (int i = 1; i <= 25; i++) {
			rows[i] = (i + 4) / 5;
			cols[i] = ((i - 1) % 5) + 1;
			// System.out.println(rows[i],cols[i]," "..
		}
		for (;;) // start randomly cycling through all cells until no
		// further changes can be made
		{
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 25; i++) {
				x = (int) (Math.random() * 25) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 25; i++) {
				// show_board(board);
				// System.out.println("i "+i+" count "+count+" clues"+clues);
				if (count > clues) {
					if (board.locations[cols[i]][rows[i]] > 0) {
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						count = count - 1;
						if (cols[i] < 5) {
							tempj = board.locations[10 - cols[i]][rows[i]];
							board.locations[10 - cols[i]][rows[i]] = 0;
							count = count - 1;
						}
						if (rows[i] < 5) {
							tempk = board.locations[cols[i]][10 - rows[i]];
							board.locations[cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						if ((cols[i] < 5) && (rows[i] < 5)) {
							templ = board.locations[10 - cols[i]][10 - rows[i]];
							board.locations[10 - cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						// board.fixed [cols [i]] [rows [i]] = false
						// System.out.println("solver");
						// solver convert
						SolverReturnValues solverReturnValues = new SolverReturnValues();
						solver(board, solverReturnValues, false);
						success = solverReturnValues.success;
						// given = solverReturnValues.given;
						// initialscan = solverReturnValues.initialscan;
						// scan = solverReturnValues.totalscan;
						// markup1 = solverReturnValues.markup1;
						markup2 = solverReturnValues.markup2;
						// backtrack = solverReturnValues.backtrack;
						// unique = solverReturnValues.unique;
						// END
						// System.out.println("solver done");
						if (markup2 == 0)
							success = false;
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							count = count + 1;
							if (cols[i] < 5) {
								board.locations[10 - cols[i]][rows[i]] = tempj;
								count = count + 1;
							}
							if (rows[i] < 5) {
								board.locations[cols[i]][10 - rows[i]] = tempk;
								count = count + 1;
							}
							if ((cols[i] < 5) && (rows[i] < 5)) {
								board.locations[10 - cols[i]][10 - rows[i]] = templ;
								count = count + 1;
							}
							// board.fixed [cols [i]] [rows [i]] = true
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
				}
			}
			// System.out.println(""
			if (nochange)
				break;
		}

		for (;;) // same again, but only two at a time
		{
			// initialize the rows and cols arrays to all possible cell
			// coordinates
			for (int i = 0; i <= 8; i++)
				for (int j = 0; j <= 4; j++) {
					rows[j * 9 + i + 1] = j + 1;
					cols[j * 9 + i + 1] = i + 1;
					// System.out.println(rows[i],cols[i]," "..
				}
			// shuffle the rows and cols arrays to allow for a random selection
			// of cells
			for (int i = 1; i <= 45; i++) {
				x = (int) (Math.random() * 45) + 1;
				tempi = rows[i];
				rows[i] = rows[x];
				rows[x] = tempi;
				tempi = cols[i];
				cols[i] = cols[x];
				cols[x] = tempi;
			}
			// cycle through all the clue locations, and try blanking them out
			// only allow it if the return has a unique solution
			// stop when all clues tried, or when the board has few enough clues
			// left
			nochange = true;
			for (int i = 1; i <= 45; i++)
				if (count > clues)
					if (board.locations[cols[i]][rows[i]] > 0) {
						tempi = board.locations[cols[i]][rows[i]];
						board.locations[cols[i]][rows[i]] = 0;
						count = count - 1;
						if (!((cols[i] == 5) && (rows[i] == 5))) {
							templ = board.locations[10 - cols[i]][10 - rows[i]];
							board.locations[10 - cols[i]][10 - rows[i]] = 0;
							count = count - 1;
						}
						// board.fixed [cols [i]] [rows [i]] = false
						// solver convert
						SolverReturnValues solverReturnValues = new SolverReturnValues();
						solver(board, solverReturnValues, false);
						success = solverReturnValues.success;
						// given = solverReturnValues.given;
						// initialscan = solverReturnValues.initialscan;
						// scan = solverReturnValues.totalscan;
						// markup1 = solverReturnValues.markup1;
						markup2 = solverReturnValues.markup2;
						// backtrack = solverReturnValues.backtrack;
						// unique = solverReturnValues.unique;
						// END
						// if (markup2 = 0)
						// success = false
						// end if
						if (!success) {
							board.locations[cols[i]][rows[i]] = tempi;
							count = count + 1;
							if (!((cols[i] == 5) && (rows[i] == 5))) {
								board.locations[10 - cols[i]][10 - rows[i]] = templ;
								count = count + 1;
							}
							// board.fixed [cols [i]] [rows [i]] = true
						} else
							nochange = false;
						// System.out.println(count, " " ..
					}
			// System.out.println(""
			if (nochange)
				break;
		}
	}

	public static void main(String[] args) throws IOException {
		// MAIN PROGRAM VARIABLES for the sudoku board generator
		Board_type board = new Board_type(), new_board;
		int bottlenecks, level, minclues = -1, initialscan, scan, markup1, markup2;
		boolean success;
		String text;
		String symmetrical;
		double score, minscore = -1;
		int stars;
		// string

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// MAIN PROGRAM for the sudoku board generator
		System.out.println("");
		System.out.println("");

		System.out
				.println("Hello. I am Sam's Sudoku program. I can make a sudoku board for you.");
		System.out.println("");

		do {
			for (;;) {
				System.out.print("Difficulty level? (1-5) ");
				level = Integer.parseInt(br.readLine());
				if ((level > 0) && (level < 6))
					break;
			}

			// get symmetrical
			symmetrical = "y";

			// create the boards and display them

			if (level < 3) {
				minclues = veryeasy_minclues;
				minscore = veryeasy_minscore;
			}
			if (level == 3) {
				minclues = easy_minclues;
				minscore = easy_minscore;
			}
			if (level == 4) {
				minclues = medium_minclues;
				minscore = medium_minscore;
			}
			if (level > 4)
				minclues = 22;
			do {
				init_board(board);
				fill_board_random(board);
				// hill_climb_board convert
				HCBReturnValues hcbReturnValues = new HCBReturnValues();
				hill_climb_board(board, hcbReturnValues);
				// swaps = hcbReturnValues.hcSwaps;
				// loops = hcbReturnValues.hcLoops;
				// END
				if (level < 4)
					if (symmetrical.equals("n"))
						blank_out_unique_easy(board, minclues, minscore);
					else
						blank_out_unique_easy_symmetrical(board, minclues,
								minscore);
				else if (symmetrical.equals("n"))
					blank_out_unique_hard(board, minclues);
				else
					blank_out_unique_hard_symmetrical(board, minclues);

				// scan_fill_board convert
				SFBReturnValues sfbReturnValues = new SFBReturnValues();
				new_board = scan_fill_board(board, false, sfbReturnValues);
				success = sfbReturnValues.success;
				score = sfbReturnValues.meancount;
				bottlenecks = sfbReturnValues.bottlenecks;
				// END
				stars = 0;
				text = "";
				if (success) {
					if (bottlenecks >= 3 && score > medium_minscore
							& score <= medium_maxscore) {
						stars = 3;
						text = "Difficulty: * * *" + " (" + cell_count(board)
								+ "," + score + "," + bottlenecks + ")";
					}
					if (score > easy_minscore & score <= easy_maxscore
							& bottlenecks < 3 & bottlenecks > 0) {
						stars = 2;
						text = "Difficulty: * *" + " (" + cell_count(board)
								+ "," + score + "," + bottlenecks + ")";
					}
					if (score > veryeasy_minscore & bottlenecks == 0) {
						text = "Difficuty: *" + " (" + cell_count(board) + ","
								+ score + "," + bottlenecks + ")";
						stars = 1;
					}
				} else {
					// solver convert
					SolverReturnValues solverReturnValues = new SolverReturnValues();
					new_board = solver(board, solverReturnValues, false);
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
						text = "Difficulty: * * * * *" + "("
								+ cell_count(board) + "," + scan + "/"
								+ initialscan + "," + markup1 + "," + markup2
								+ ")";
					} else {
						text = "Difficulty: * * * *" + "(" + cell_count(board)
								+ "," + scan + "/" + initialscan + ","
								+ markup1 + "," + markup2 + ")";
						stars = 4;
					}
				}
				// Progress bar
				// System.out.print(stars);
			} while (stars != level || cell_count(board) > maxclues);

			System.out.println();
			System.out.println();
			System.out.println(text);
			show_board(board);
			show_board(new_board);
		} while (true);
	}

}
