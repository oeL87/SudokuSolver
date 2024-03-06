public class Main {

    public static void main(String[] args) {
        Board puzzle = new Board();
        try {
            puzzle.loadPuzzle("easy");
            puzzle.logicCycles();
        } catch (Exception e) {
            System.out.println("Exception occurred");
        }

        puzzle.display();
        System.out.println("Error found?: " + puzzle.errorFound());
        System.out.println("Solved?: " + puzzle.isSolved());

    }

}