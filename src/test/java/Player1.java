import com.codingame.game.CellState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Player1 {
    private static final int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    private static final int[] dc = {0, -1, -1, -1, 0, 1, 1, 1};

    private static int index = 0;
    private static int[][] grid = new int[7][7];

    //TODO
    static void makeDecision() {
        List<String> moves = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {

            }
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Random random = new Random(0);
        index = in.nextInt();

        while (true) {
            int cellCount = in.nextInt();

            for (int i = 0; i < cellCount; i++) {
                int row = in.nextInt();
                int col = in.nextInt();
                int state = in.nextInt();
                grid[row][col] = state;
            }

            makeDecision();
        }
    }
}
