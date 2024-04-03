import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import SudokuEngine.Board_type;
import SudokuEngine.SolverReturnValues;
import SudokuEngine.Sudoku2;

public class SudokuBatchSolver {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		System.out
				.print("This program solves batches of Sudokus.\n Enter the filename for the batch: ");
		String fileName = stdin.readLine();
		File file = new File(fileName);

		if (file.exists()) {
			BufferedReader batchFile = new BufferedReader(new FileReader(file));
			Board_type board = new Board_type();
			boolean eof = false;
			int numBoards = 0, solved = 0, backtrack = 0, nonUnique = 0;
			while (!eof) {
				Sudoku2.init_board(board);
				int squareCounter = 1;
				// int tokens = 0;
				int row = 1;
				int col = 1;
				int nextSquare = 0;
				while (!eof & squareCounter <= 81) {
					do
						nextSquare = batchFile.read();
					while (nextSquare >= 10 & nextSquare <= 13);
					// System.out.print((char) nextSquare);
					if (nextSquare >= 49 && nextSquare <= 57)
						board.locations[col][row] = nextSquare - 48;
					if (nextSquare != -1) {
						squareCounter++;
						col++;
						if (col > 9) {
							row++;
							col = 1;
						}
					} else
						eof = true;
				}
				// System.out.println();
				if (!eof) {
					numBoards++;
					Sudoku2.dump_board(board);
					SolverReturnValues returnValues = new SolverReturnValues();
					board = Sudoku2.solver(board, returnValues, true);
					Sudoku2.dump_board(board);
					if (returnValues.success)
						solved++;
					if (returnValues.backtrack > 0)
						backtrack++;
					if (!returnValues.unique)
						nonUnique++;
					if (!returnValues.unique)
						System.out.println("*** Warning: Solution not unique.");
				} else if (squareCounter != 1)
					System.out.println("*** Error: Incomplete Board");
				System.out.println();
			}
			System.out.println("End of File. \n\t" + numBoards
					+ " boards processed. \n\t" + solved
					+ " solved successfully. \n\t" + backtrack
					+ " required backtracking.\n\t" + nonUnique
					+ " did not have unique solutions.");
		} else
			System.out.println("*** Error: File " + fileName
					+ " does not exist.");

	}
}
