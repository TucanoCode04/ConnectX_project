package connectx.abp_budspencer;

import connectx.euristics_budspencer.GeronimoStilton;
import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXPlayer;
import connectx.CXGameState;

import connectx.euristics.StartEuristicsCreator;

public class Rododendro implements CXPlayer{

    private int M, N, X;
    private CXGameState myWin;
    private CXGameState yourWin;
    private long timeout_in_ms;
    private long START_TIME;
    private boolean maximizingPlayer;
    private CXCellState player;
    private CXCellState enemy;


    private StartEuristicsCreator euristicsCreator;


    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
    this.M = M;
    this.N = N;
    this.X = X;
    //this.myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
    //this.yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    this.timeout_in_ms = timeout_in_secs * 1000;
    this.maximizingPlayer = first ? true : false;
    this.player = first ? CXCellState.P1 : CXCellState.P2;
    this.enemy = first ? CXCellState.P2 : CXCellState.P1;

    /* Inizializzo l'euristica */
    euristicsCreator = new StartEuristicsCreator(N, M, X, first);

    }

    @Override
    public String playerName() {
        return "Rododendro";
    }


    @Override
    public int selectColumn(CXBoard b) {
        int move = 0;
        int max = Integer.MIN_VALUE;
        int depth = 5;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (int i = 0; i < b.N-1; i++) {
            if (!(b.fullColumn(i))) {
                int value = minimax(b, depth, alpha, beta, maximizingPlayer);
                if (value > max) {
                    max = value;
                    move = i;
                }
            }
        }
        return move;
    }

    private int minimax(CXBoard b, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || !(b.gameState() == CXGameState.OPEN)) {
            return evaluate(b);
        }
        if (maximizingPlayer) {
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < b.N-1; i++) {
                if (!(b.fullColumn(i))) {
                    b.markColumn(i);
                    int value = minimax(b, depth - 1, alpha, beta, false);
                    b.unmarkColumn();
                    max = Math.max(max, value);
                    alpha = Math.max(alpha, value);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < b.N-1; i++) {
                if (!(b.fullColumn(i))) {
                    b.markColumn(i);
                    int value = minimax(b, depth - 1, alpha, beta, true);
                    b.unmarkColumn();
                    min = Math.min(min, value);
                    beta = Math.min(beta, value);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return min;
        }
    }

    //controlla dove poter mettere un pezzo

    private int middleColumnScore(CXBoard b, int column){
        int middle = Math.floorDiv(b.N, 2);
        if (!(b.fullColumn(middle)) && column == middle){
            return 100;
        }
        else{
            return 0;
        }
    }

    private int numberOfPiecesInAColumn(CXBoard b, int column, CXCellState player){
        int count = 0;
        for (int i = 0; i < b.M-1; i++){
            if (b.cellState(column, i) != CXCellState.FREE && b.cellState(column, i) == player){
                count++;
            }
        }
        return count;
    }


    //controlla dove poter mettere due pezzi vicini

    private int numberOfPiecesInARow(CXBoard b, int column, int row, CXCellState player){
        int count = 0;
        for (int i = 0; i < b.N-1; i++){
            if (b.cellState(i, row) != CXCellState.FREE && b.cellState(i, row) == player){
                count++;
            }
        }
        return count;
    }

    //check connected pieces in a diagonal
    private int numberOfPiecesInADiagonal(CXBoard b, int column, int row, CXCellState player){
        int count = 0;
        int i = column;
        int j = row;
        while (i < b.N-1 && j < b.M-1){
            if (b.cellState(i, j) != CXCellState.FREE && b.cellState(i, j) == player){
                count++;
            }
            i++;
            j++;
        }
        i = column;
        j = row;
        while (i >= 0 && j >= 0){
            if (b.cellState(i, j) != CXCellState.FREE && b.cellState(i, j) == player){
                count++;
            }
            i--;
            j--;
        }
        return count;
    }

    //check connected pieces in the other diagonal
    private int numberOfPiecesInADiagonal2(CXBoard b, int column, int row, CXCellState player){
        int count = 0;
        int i = column;
        int j = row;
        while (i < b.N-1 && j >= 0){
            if (b.cellState(i, j) != CXCellState.FREE && b.cellState(i, j) == player){
                count++;
            }
            i++;
            j--;
        }
        i = column;
        j = row;
        while (i >= 0 && j < b.M-1){
            if (b.cellState(i, j) != CXCellState.FREE && b.cellState(i, j) == player){
                count++;
            }
            i--;
            j++;
        }
        return count;
    }

    //apply score based of number of pieces in a row
    private int numberOfPiecesInARowScore(CXBoard b, int column, int row, CXCellState player){
        return numberOfPiecesInARow(b, column, row, player);
    }

    //apply score based of number of pieces in a diagonal
    private int numberOfPiecesInADiagonalScore(CXBoard b, int column, int row, CXCellState player){
        return numberOfPiecesInADiagonal(b, column, row, player);
    }

    //apply score based of number of pieces in the other diagonal
    private int numberOfPiecesInADiagonal2Score(CXBoard b, int column, int row, CXCellState player){
        return numberOfPiecesInADiagonal2(b, column, row, player);
    }

    //apply score based of number of pieces in a column
    private int numberOfPiecesInAColumnScore(CXBoard b, int column, CXCellState player){
        return numberOfPiecesInAColumn(b, column, player);
    }
    
    //evaluate the board using the scoring functions above
    private int evaluate(CXBoard b){
        int score = 0;
        for (int i = 0; i < b.N-1; i++){
            for (int j = 0; j < b.M-1; j++){
                if (b.cellState(i, j) == CXCellState.FREE){
                    if(player == CXCellState.P1){
                        score += middleColumnScore(b, i);
                        score += numberOfPiecesInAColumnScore(b, i, player);
                        score += numberOfPiecesInARowScore(b, i, j, player);
                        score += numberOfPiecesInADiagonalScore(b, i, j, player);
                        score += numberOfPiecesInADiagonal2Score(b, i, j, player);
                        score -= numberOfPiecesInAColumnScore(b, i, enemy);
                        score -= numberOfPiecesInARowScore(b, i, j, enemy);
                        score -= numberOfPiecesInADiagonalScore(b, i, j, enemy);
                        score -= numberOfPiecesInADiagonal2Score(b, i, j, enemy);
                    } else {
                        score -= middleColumnScore(b, i);
                        score -= numberOfPiecesInAColumnScore(b, i, player);
                        score -= numberOfPiecesInARowScore(b, i, j, player);
                        score -= numberOfPiecesInADiagonalScore(b, i, j, player);
                        score -= numberOfPiecesInADiagonal2Score(b, i, j, player);
                        score += numberOfPiecesInAColumnScore(b, i, enemy);
                        score += numberOfPiecesInARowScore(b, i, j, enemy);
                        score += numberOfPiecesInADiagonalScore(b, i, j, enemy);
                        score += numberOfPiecesInADiagonal2Score(b, i, j, enemy);
                    }
                }
            }
        }
        return score;
    }
}

    //controlla dove poter mettere tre pezzi vicini
    //etc etc
    //controlla se l'avversario ha due pezzi vicini
    //controlla se l'avversario ha tre pezzi vicini
    //etc etc
    //vedere se si può fare una funzione che controlla se ci sono pezzi vicini
    //e che ritorna un array di array di int con le coordinate dei pezzi vicini
    //dare un punteggio ad ogni mossa in base a quanti pezzi vicini ci sono
    //dare un punteggio ad ogni mossa in base a quanti pezzi vicini ha l'avversario
    //scegliere la mossa con il punteggio più alto
    //se ci sono più mosse con lo stesso punteggio, scegliere a caso tra quelle
    //se non ci sono mosse con punteggio alto, scegliere a caso tra le mosse possibili


    
    /* 
    //iterative deepening
    private int iterativeDeepening(CXBoard b, int depth, int alpha, int beta, boolean maximizingPlayer) {
        int move = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < b.M; i++) {
            if (!(b.fullColumn(i))) {
                b.markColumn(i);
                int value = minimax(b, depth - 1, alpha, beta, false);
                b.unmarkColumn();
                if (value > max) {
                    max = value;
                    move = i;
                }
            }
        }
        return move;
    }
    */

    //valutazione
    
    





/*
public class Rododendro implements CXPlayer {

    public Rododendro() {
        super("Rododendro");
    }

    @Override
    public int nextMove(CXBoard b) {
        int move = 0;
        int max = Integer.MIN_VALUE;
        int depth = 5;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (int i = 0; i < b.getWidth(); i++) {
            if (b.canPlay(i)) {
                int value = minimax(b, depth, alpha, beta, true);
                if (value > max) {
                    max = value;
                    move = i;
                }
            }
        }
        return move;
    }


            int score = 0;
        int[][] board = b.getBoard();
        int width = b.M;
        int height = b.N;
        int player = b.getCurrentPlayer();
        int opponent = 3 - player;
        int[][][] patterns = {
                {{0, 0}, {0, 1}, {0, 2}, {0, 3}},
                {{0, 0}, {1, 0}, {2, 0}, {3, 0}},
                {{0, 0}, {1, 1}, {2, 2}, {3, 3}},
                {{0, 0}, {1, -1}, {2, -2}, {3, -3}} // 4    4   
        };
        for (int[][] pattern : patterns) {
            int myCount = 0;
            int yourCount = 0;
            for (int[] p : pattern) {
                int x = p[0];
                int y = p[1];
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (board[x][y] == player) {
                        myCount++;
                    } else if (board[x][y] == opponent) {
                        yourCount++;
                    }
                }
            }
            if (myCount == 4) {
                return 1000000;
            } else if (yourCount == 4) {
                return -1000000;
            } else if (myCount == 3 && yourCount == 0) {
                score += 100;
            } else if (myCount == 2 && yourCount == 0) {
                score += 10;
            } else if (myCount == 1 && yourCount == 0) {
                score += 1;
            } else if (yourCount == 3 && myCount == 0) {
                score -= 100;
            } else if (yourCount == 2 && myCount == 0) {
                score -= 10;
            } else if (yourCount == 1 && myCount == 0) {
                score -= 1;
            }
        }
        return score;
    

    private int minimax(CXBoard b, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || b.isGameOver()) {
            return evaluate(b);
        }
        if (maximizingPlayer) {
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < b.getWidth(); i++) {
                if (b.canPlay(i)) {
                    b.play(i);
                    int value = minimax(b, depth - 1, alpha, beta, false);
                    b.undo();
                    max = Math.max(max, value);
                    alpha = Math.max(alpha, value);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < b.getWidth(); i++) {
                if (b.canPlay(i)) {
                    b.play(i);
                    int value = minimax(b, depth - 1, alpha, beta, true);
                    b.undo();
                    min = Math.min(min, value);
                    beta = Math.min(beta, value);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return min;
        }
    }

    private int evaluate(CXBoard b) {
        int score = 0;
        int[][] board = b.getBoard();
        int width = b.getWidth();
        int height = b.getHeight();
        int player = b.getCurrentPlayer();
        int opponent = 3 - player;
        int[][][] patterns = {
                {{0, 0}, {0, 1}, {0, 2}, {0, 3}},
                {{0, 0}, {1, 0}, {2, 0}, {3, 0}},
                {{0, 0}, {1, 1}, {2, 2}, {3, 3}},
                {{0, 0}, {1, -1}, {2, -2}, {3, -3}} // 4    4   






    }


    public class ConnectXAI {

    private int M; // Number of rows
    private int N; // Number of columns
    private int X; // Number of pieces needed to win

    public ConnectXAI(int M, int N, int X) {
        this.M = M;
        this.N = N;
        this.X = X;
    }

    *//* 
    public int evaluateBoard(CXBoard b, int currentPlayer) {
        int opponentPlayer = (b.currentPlayer() == 0) ? 2 : 1; // Assuming player IDs are 1 and 2

        int playerScore = calculatePlayerScore(board, currentPlayer);
        int opponentScore = calculatePlayerScore(board, opponentPlayer);

        // Calculate the difference in scores, giving a positive score for the current player
        int score = playerScore - opponentScore;

        // Optionally, add more heuristics here and adjust the weights

        return score;
    }

    /* 
    private int calculatePlayerScore(int[][] board, int player) {
        int score = 0;

        // Check for winning configurations (X in a row)
        score += countWinningConfigurations(board, player);

        // Check for open lines that could lead to a win
        score += countOpenLines(board, player);

        // Add other heuristics as needed (e.g., center control)

        return score;
    }

    private int countWinningConfigurations(int[][] board, int player) {
        // Implement logic to count the number of winning configurations
        // For example, you can check horizontally, vertically, and diagonally
        // and count how many sequences of X pieces exist.

        int winningConfigurations = 0;
        // Implement the counting logic here.

        return winningConfigurations;
    }

    private int countOpenLines(int[][] board, int player) {
        // Implement logic to count open lines that could lead to a win
        // For example, you can check for sequences of (X - 1) player pieces
        // with an empty slot on either end.

        int openLines = 0;
        // Implement the counting logic here.

        return openLines;
    }

    // Add more helper methods and heuristics as needed

    public static void main(String[] args) {
        int M = 6;
        int N = 7;
        int X = 4;
        ConnectXAI connectXAI = new ConnectXAI(M, N, X);

        int[][] board = new int[M][N];
        int currentPlayer = 1; // Assuming player 1 is the current player

        int evaluation = connectXAI.evaluateBoard(board, currentPlayer);
        System.out.println("Board Evaluation: " + evaluation);
    }
}


Creating an effective evaluation function for a Connect X game (where X is a given value) on an MxN board requires a combination of heuristics and strategies to assess the current game state. The goal is to assign a score to a game board that reflects the desirability of that state for the current player. Here's a step-by-step guide on how to approach this:

Understand the Game Rules:

Make sure you have a deep understanding of the rules of the Connect X game, including how players win and what constitutes a good or bad board position.
Heuristics and Scoring Factors:

Identify key factors that contribute to a good or bad board position. These factors may include:
The number of player pieces in a winning configuration (X in a row).
The presence of open lines that can potentially lead to a win.
Blocking the opponent from forming winning lines.
Center control (favoring moves closer to the center).
Edge and corner control.
Mobility (the ability to make future moves).
Assign scores to these factors based on their importance. For example, forming a winning line might have a very high score, while controlling the center might have a moderate score.
Implement Scoring Functions:

Write scoring functions that calculate scores for each of the identified factors. For example, you might have separate functions to evaluate the number of pieces in a winning configuration, open lines, and center control.
These functions should consider both the current player's pieces and the opponent's pieces.
Combine and Weight Scores:

Combine the scores from the various scoring functions to calculate an overall board score. You can use a weighted sum or other combination methods depending on the importance of each factor.
Adjust the weights of these functions through experimentation to find a balance that makes your AI play effectively.
Minimax and Alpha-Beta Pruning:

Integrate the evaluation function into the minimax algorithm with alpha-beta pruning (as you've shown in your previous code) to search for the best move.
The AI should explore different moves, applying the evaluation function to each resulting board state and selecting the move that maximizes its expected outcome while minimizing the opponent's.
Test and Iterate:

Test your AI against human players or other AI opponents to fine-tune the evaluation function and scoring parameters.
Iterate on your AI's performance and adjust the heuristics and weights as needed.
Optimization:

Consider performance optimizations to make your AI run efficiently, especially if the search space becomes too large. Techniques like transposition tables and move ordering can be beneficial.
Learning and Machine Learning (Optional):

For more advanced AI, you can explore machine learning approaches like neural networks to learn the evaluation function from data. This can be particularly effective in complex games.
Remember that creating a strong Connect X AI can be challenging and may require a lot of experimentation and fine-tuning. The quality of your evaluation function plays a crucial role in determining the AI's strength.


*/



