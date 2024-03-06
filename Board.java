import java.io.File;
import java.util.Scanner;
import java.util.Stack;

public class Board {

    /*
     * The Sudoku Board is made of 9x9 cells for a total of 81 cells. In this
     * program we will be representing the Board using a 2D Array of cells.
     */
    private Stack<Cell[][]> stack = new Stack<Cell[][]>();
    private Stack<Integer> xStack = new Stack<Integer>(), yStack = new Stack<Integer>(), guessStack= new Stack<Integer>();
    private Cell[][] board = new Cell[9][9];

    // The variable "level" records the level of the puzzle being solved.
    private String level = "";

    // This must initialize every cell on the board with a generic cell. It must
    public Board() {
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++) {
                board[x][y] = new Cell();
                board[x][y].setBoxID(3 * (x / 3) + y / 3 + 1);
            }
    }

    /*
     * This method will take a single String as a parameter. The String must be
     * either "easy", "medium" or "hard" If it is none of these, the method will set
     * the String to "easy". The method will set each of the 9x9 grid of cells by
     * accessing either "easyPuzzle.txt", "mediumPuzzle.txt" or "hardPuzzle.txt" and
     * setting the Cell.number to the number given in the file.
     *
     * This must also set the "level" variable TIP: Remember that setting a cell's
     * number affects the other cells on the board.
     */
    public void loadPuzzle(String level) throws Exception {
        this.level = level;
        String fileName = "./data/testPuzzle.txt";
        if (level.contentEquals("medium"))
            fileName = "./data/mediumPuzzle.txt";
        else if (level.contentEquals("hard"))
            fileName = "./data/hardPuzzle.txt";
        else if(level.contentEquals("easy"))
            fileName = "./data/easyPuzzle.txt";

        Scanner input = new Scanner(new File(fileName));
        System.out.println("test");

        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++) {
                int number = input.nextInt();
                if (number != 0)
                    solve(x, y, number);
            }

        input.close();

    }



    /*
     * This method scans the board and returns TRUE if every cell has been solved.
     * Otherwise it returns FALSE
     *
     */
    public boolean isSolved() {
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++)
                if (board[x][y].getNumber() == 0)
                    return false;
        return true;
    }

    /*
     * This method displays the board neatly to the screen. It must have dividing
     * lines to show where the box boundaries are as well as lines indicating the
     * outer border of the puzzle
     */
    public void display() {
        System.out.println("Numbers:\n-------------------------");
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if ((y) % 3 == 0)
                    System.out.print("|_");
                System.out.print(board[x][y].getNumber() + "_");
            }
            System.out.print("|");
            if ((x + 1) % 3 == 0)
                System.out.println("\n-------------------------");
            else
                System.out.println();
        }
    }

    public void potDisplay() {
        System.out.println("Potentials: \n-------------------------");
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if ((y) % 3 == 0)
                    System.out.print("|_");
                System.out.print(board[x][y].numberOfPotentials() + "_");
            }
            System.out.print("|");
            if ((x + 1) % 3 == 0)
                System.out.println("\n-------------------------");
            else
                System.out.println();
        }
    }
    /*
     * This method solves a single cell at x,y for number. It also must adjust the
     * potentials of the remaining cells in the same row, column, and box. a and b
     * are placeholders for x and y respectively
     */
    public void solve(int x, int y, int number) {
        board[x][y].setNumber(number);
        for (int a = 0; a < board.length; a++)
            for (int b = 0; b < board[a].length; b++)
                if (a == x || b == y || board[a][b].getBoxID() == board[x][y].getBoxID())
                    if (board[x][y] != board[a][b] && board[a][b].getNumber() == 0)
                        board[a][b].cantBe(number);
    }

    // logicCycles() continuously cycles through the different logic algorithms
    // until no more changes are being made.
    public void logicCycles() throws Exception {
        stack.push(board);
        while (!isSolved()) {
            int changesMade = 0;
            do {
                changesMade = 0;
                changesMade += logic1();
                changesMade += logic2();
                changesMade += logic3();
                changesMade += logic4();
                while(errorFound()) {
                    board = stack.pop();
                    int x = xStack.pop(), y = yStack.pop();
                    board[x][y].cantBe(board[x][y].getFirstPotential());
                }
            } while (changesMade != 0);
            //Guessing
            if(!isSolved()) {
                Cell[][] guessBoard = new Cell[9][9];
                for(int x = 0; x < 9; x++) {
                    for(int y = 0; y < 9; y++) {
                        guessBoard[x][y] = new Cell();
                        guessBoard[x][y].setNumber(board[x][y].getNumber());
                        guessBoard[x][y].setBoxID(board[x][y].getBoxID());
                        guessBoard[x][y].clone(board[x][y].getPotential());
                    }
                }
                stack.push(guessBoard);
                boolean guessing = false;
                for(int x = 0; x < 9 && !guessing; x++) {
                    for(int y = 0; y < 9 && !guessing; y++) {
                        if(board[x][y].getNumber() == 0) {
                            guessing = true;
                            solve(x, y, board[x][y].getFirstPotential());
                            xStack.push(x);
                            yStack.push(y);
                            guessStack.push(board[x][y].getFirstPotential());
                        }
                    }
                }
            }
            while(errorFound()) {
                board = stack.pop();
                int x = xStack.pop(), y = yStack.pop(), guess = guessStack.pop();
                board[x][y].cantBe(guess);
            }
        }
    }

    /*
     * This method searches each row of the puzzle and looks for cells that only has
     * one potential. If it finds a cell like this, it solves the cell for that
     * number. This also tracks the number of cells that it solved as it traversed
     * the board and returns that number.
     */
    public int logic1() {
        int changesMade = 0;
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++)
                if (board[x][y].numberOfPotentials() == 1 && board[x][y].getNumber() == 0) {
                    solve(x, y, board[x][y].getFirstPotential());
                    changesMade++;
                }
//		System.out.println("logic1: " + changesMade);
        return changesMade;
    }

    /*
     * This method searches each row for a cell that is the only cell that has the
     * potential to be a given number. If it finds such a cell and it is not already
     * solved, it solves the cell. It then does the same thing for the columns.This
     * also tracks the number of cells that it solved as it traversed the board and
     * returns that number.
     *
     *
     */
    public int logic2() {
        int changesMade = 0;

        for (int x = 0; x < board.length; x++)
            for (int a = 1; a < 10; a++) {
                int numCount = 0;
                for (int y = 0; y < board[0].length; y++) {
                    if (board[x][y].canBe(a) && board[x][y].getNumber() == 0)
                        numCount++;
                    if (numCount > 1)
                        break;
                }
                if (numCount == 1)
                    for (int y = 0; y < board[0].length; y++)
                        if (board[x][y].canBe(a) && board[x][y].getNumber() == 0) {
                            solve(x, y, a);
                            changesMade++;
                        }
            }
        for (int y = 0; y < board.length; y++)
            for (int a = 1; a < 10; a++) {
                int numCount = 0;
                for (int x = 0; x < board[0].length; x++) {
                    if (board[x][y].canBe(a) && board[x][y].getNumber() == 0)
                        numCount++;
                    if (numCount > 1)
                        break;
                }
                if (numCount == 1)
                    for (int x = 0; x < board[0].length; x++)
                        if (board[x][y].canBe(a) && board[x][y].getNumber() == 0) {
                            solve(x, y, a);
                            changesMade++;
                        }
            }
//		System.out.println("logic2: " + changesMade);
        return changesMade;
    }

    /*
     * This method searches each box for a cell that is the only cell that has the
     * potential to be a given number. If it finds such a cell and it is not already
     * solved, it solves the cell. This also tracks the number of cells that it
     * solved as it traversed the board and returns that number.
     *
     * a and b iterate through the boxes c iterates through the numbers that could
     * be in the cell
     */
    public int logic3() {
        int changesMade = 0;
        for (int a = 0; a < 3; a++)
            for (int b = 0; b < 3; b++)
                for (int num = 1; num < 10; num++) {
                    int numCount = 0;
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++)
                            if (board[x + 3 * a][y + 3 * b].canBe(num) && board[x + 3 * a][y + 3 * b].getNumber() == 0)
                                numCount++;
                        if (numCount > 1)
                            break;
                    }
                    if (numCount == 1)
                        for (int x = 0; x < 3; x++)
                            for (int y = 0; y < 3; y++)
                                if (board[x + 3 * a][y + 3 * b].canBe(num)
                                        && board[x + 3 * a][y + 3 * b].getNumber() == 0) {
                                    solve(x + 3 * a, y + 3 * b, num);
                                    changesMade++;
                                }
                }
//		System.out.println("logic3: " + changesMade);
        return changesMade;
    }

    /*
     * This method searches each row for the following conditions: 1. There are two
     * unsolved cells that only have two potential numbers that they can be 2. These
     * two cells have the same two potentials (They can't be anything else)
     *
     * Once this occurs, all of the other cells in the row cannot have these two
     * potentials. Write an algorithm to set these two potentials to be false for
     * all other cells in the row.
     *
     * Repeat this process for columns
     *
     * This also tracks the number of cells that it solved as it traversed the board
     * and returns that number.
     */
    public int logic4() {
        int changesMade = 0;
        Stack<Integer> xList = new Stack<Integer>(), yList = new Stack<Integer>(), numberList = new Stack<Integer>();
//		rows and columns
        int temp1 = 0, temp2 = 0;
        for(int a = 0; a < 9; a++) {
            for(int y = 0; y < 9; y++) {
                if(board[a][y].numberOfPotentials() == 2) {
                    if(numberList.empty()) {
                        yList.push(y);
                        temp1 = board[a][y].getFirstPotential();
                        temp2 = board[a][y].getSecondPotential();
                        numberList.push(temp1);
                        numberList.push(temp2);
                    }else if(board[a][y].getSecondPotential() == temp2 && board[a][y].getFirstPotential() == temp1){
                        yList.push(y);
                        numberList.push(board[a][y].getFirstPotential());
                        numberList.push(board[a][y].getSecondPotential());
                    }
                }
            }
            if(yList.size() == 2) {
                while(!yList.empty()) {
                    int tempY1 = yList.pop(), tempY2 = yList.pop(), tempNum = numberList.pop();
                    for(int y = 0; y < 9; y++) {
                        if(board[a][y].canBe(tempNum) && y != tempY1 && y != tempY2) {
                            board[a][y].cantBe(tempNum);
                            changesMade++;
                        }
                    }
                }
            }
            if(changesMade != 0)
                System.out.println(changesMade);
            while(!yList.empty()) {
                yList.pop();
            }
            while(!numberList.empty()) {
                numberList.pop();
            }
            for(int x = 0; x < 9; x++) {
                if(board[x][a].numberOfPotentials() == 2) {
                    if(numberList.empty()) {
                        xList.push(x);
                        temp1 = board[x][a].getFirstPotential();
                        temp2 = board[x][a].getSecondPotential();
                        numberList.push(temp1);
                        numberList.push(temp2);
                    }else if(board[x][a].getSecondPotential() == temp2 && board[x][a].getFirstPotential() == temp1){
                        xList.push(x);
                        numberList.push(board[x][a].getFirstPotential());
                        numberList.push(board[x][a].getSecondPotential());
                    }
                }
            }
            if(xList.size() == 2) {
                while(!xList.empty()) {
                    int tempX1 = xList.pop(), tempX2 = xList.pop(), tempNum = numberList.pop();
                    for(int x = 0; x < 9; x++) {
                        if(board[x][a].canBe(tempNum) && x != tempX1 && x != tempX2) {
                            board[x][a].cantBe(tempNum);
                            changesMade++;
                        }
                    }
                }
            }
            while(!xList.empty()) {
                xList.pop();
            }
            while(!numberList.empty()) {
                numberList.pop();
            }
        }
        /*
         * boxes
         * a and b are to loop through the boxes
         */
        for(int a = 0; a < 3; a++) {
            for(int b = 0; b < 3; b++) {
                for(int x = 0; x < 3; x++) {
                    for(int y = 0; y < 3; y++) {
                        if(board[3*a + x][a].numberOfPotentials() == 2) {
                            if(numberList.empty()) {
                                xList.push(3*a + x);
                                yList.push(3*b + y);
                                temp1 = board[3*a + x][3*b + y].getFirstPotential();
                                temp2 = board[3*a + x][3*b + y].getSecondPotential();
                                numberList.push(temp1);
                                numberList.push(temp2);
                            }else if(board[3*a + x][a].getSecondPotential() == temp2 && board[3*a + x][a].getFirstPotential() == temp1){
                                xList.push(3*a + x);
                                yList.push(3*b + y);
                                numberList.push(board[3*a + x][3*b + y].getFirstPotential());
                                numberList.push(board[3*a + x][3*b + y].getSecondPotential());
                            }
                        }
                    }
                }
                if(xList.size() == 2 && yList.size() == 2) {
                    while(!xList.empty() && !yList.empty()) {
                        int tempX1 = xList.pop(), tempX2 = xList.pop(), tempNum = numberList.pop();
                        int tempY1 = yList.pop(), tempY2 = yList.pop();
                        for(int x = 0; x < 3; x++) {
                            for(int y = 0; y < 3; y++) {
                                if(board[3*a + x][3*b + y].canBe(tempNum) && board[3*a + x][3*b + y] != board[tempX1][tempY1]
                                        && board[3*a + x][3*b + y] != board[tempX2][tempY2]) {
                                    board[3*a + x][3*b + y].cantBe(tempNum);
                                    changesMade++;
                                }
                            }
                        }
                    }
                }
                while(!xList.empty()) {
                    xList.pop();
                }
                while(!yList.empty()) {
                    yList.pop();
                }
                while(!numberList.empty()) {
                    numberList.pop();
                }
            }
        }
//		System.out.println("logic4: " + changesMade);
        return changesMade;
    }

    public boolean[] getPotentialArrayAt( int x, int y) {
        boolean[] temp = new boolean[10];
        temp[0] = false;
        boolean[] copy = board[x][y].getPotential();
        for(int a = 1; a < 10; a++) {
            temp[a] = copy[a];
        }
        return temp;
    }
    /*
     * This method scans the board to see if any logical errors have been made. It
     * can detect this by looking for a cell that no longer has the potential to be
     * any number.
     */
    public boolean errorFound() {
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y].numberOfPotentials() == 0) {
                    potDisplay();
                    display();
                    return true;
                }
            }
        return false;
    }

}