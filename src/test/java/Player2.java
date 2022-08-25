import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Player2 {
    private static final int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    private static final int[] dc = {0, -1, -1, -1, 0, 1, 1, 1};

    private static int index = 0;
    private static int[][] grid = new int[7][7];

    static void makeDecision() {
        List<String> moves = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (grid[i][j] == index) {
                    for (int dir = 0; dir < 8; dir++) {
                        int r = i + dr[dir];
                        int c = j + dc[dir];
                        if (r >= 0 && r < 7 && c >= 0 && c < 7 && grid[r][c] == 0 && dir%2 == 0) {
                            moves.add(String.format("%d %d %s %d %d", i, j, "MOVE", r ,c));
                        }
                        else if (r >= 0 && r < 7 && c >= 0 && c < 7 && grid[r][c] == 0 && dir%2 == 1) {
                            moves.add(String.format("%d %d %s %d %d", i, j, "REPL", r ,c));
                        }
                    }
                }
            }
        }
        int position = new Random().nextInt(moves.size());
        System.err.println("Player 2: "+position);
        System.out.println(moves.get(position));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
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
