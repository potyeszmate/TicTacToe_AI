import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class SamplePlayer extends GomokuPlayer {

    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>();
    protected ArrayList<int[][]> futureBoards = new ArrayList<int[][]>();


    public SamplePlayer(int color, int[][] board, Random random) {
        super(color, board, random);
        // store possible actions
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                actions.add(new GomokuAction(i, j));
            }
        }
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        // I am the first, center start (not necessary)
        if (prevAction == null) {
            int i = board.length / 2;
            int j = board[i].length / 2;
            while (board[i][j] != GomokuGame.EMPTY) {
                i = random.nextInt(board.length);
                j = random.nextInt(board[i].length);
            }
            board[i][j] = color;
            return new GomokuAction(i, j);
        }

        // store enemy's step
        board[prevAction.i][prevAction.j] = 1 - color;

        // find best steps and choose a random one
        Collections.shuffle(actions, random);
        GomokuAction action = null;
        int score = -1,myMax=-1,enemyMax=-1;
        for (GomokuAction a : actions) {
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                int myScore=score(a.i, a.j, color);
                int enemyScore=score(a.i, a.j, 1 - color);
                if(myScore>myMax){
                    myMax=myScore;
                }

                if(enemyScore>enemyMax){
                    enemyMax=enemyScore;
                }
                //System.out.println(enemyScore);
                if(myScore==1000){
                    action = a;
                    break;
                }
                if(enemyScore==1000 || enemyScore==500){
                    action = a;
                    break;
                }
                int s=myScore+enemyScore;

                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }
      //  System.out.println("my max "+myMax);
     //   System.out.println("enemy max "+enemyMax);

        // store and do best step
        board[action.i][action.j] = color;
        return action;
    }

    /**
     * The score of a cell is the aggregated value of the any direction from
     * there computed by {@link GreedyPlayer#countDirection(int, int, int, int, int)} function.
     * @param i row of the cell
     * @param j column of the cell
     * @param c color
     * @return aggregated neighbor score
     */
    protected int score(int i, int j, int c) {
        int direction1,direction2,direction3,direction4,result,maxPlace1,maxPlace2,maxPlace3,maxPlace4 = 0;

        // right up & left down
        direction1 = countDirection(i, j, -1, 1, c) + countDirection(i, j, 1, -1, c);
        maxPlace1=1+countEmpty(i,j,-1,1,c,0)+countEmpty(i, j, 1, -1, c,0);
        if(maxPlace1<5){
            direction1=0;
        }
        else if(checkIfClosed(i, j, -1, 1, c) || checkIfClosed(i, j, 1, -1, c))
        {
            direction1--;
        }


        // right + left
        direction2 = countDirection(i, j, 0, 1, c) + countDirection(i, j, 0, -1, c);
        maxPlace2=1+countEmpty(i,j,0,1,c,0)+countEmpty(i, j, 0, -1, c,0);
        if(maxPlace2<5){
            direction2=0;
        }
        else if(checkIfClosed(i, j, 0,1, c) || checkIfClosed(i, j, 0, -1, c))
        {
            direction2--;
        }

        // right down + left up
        direction3 = countDirection(i, j, 1, 1, c) + countDirection(i, j, -1, -1, c);
        maxPlace3=1+countEmpty(i,j,1,1,c,0)+countEmpty(i, j, -1, -1, c,0);
        if(maxPlace3<5){
            direction3=0;
        }
        else if(checkIfClosed(i, j, 1,1, c) || checkIfClosed(i, j, -1, -1, c))
        {
            direction3--;
        }

        // down + up
        direction4 = countDirection(i, j, 1, 0, c)  + countDirection(i, j, -1, 0, c);
        maxPlace4=1+countEmpty(i,j,1,0,c,0)+countEmpty(i, j, -1, 0, c,0);
        if(maxPlace4<5){
            direction4=0;
        }
        else if(checkIfClosed(i, j, 1,0,c) || checkIfClosed(i, j, -1, 0,  c))
        {
            direction4--;
        }

      /*  if(i==5 && j==5){

            System.out.println("("+i+","+j+")");
            System.out.println("/ "+ direction1);
            System.out.println("- "+ direction2);
            System.out.println("\\ "+ direction3);
            System.out.println("| "+ direction4);
            System.out.println("max "+maxPlace4);
        }*/
        result=direction1+direction2+direction3+direction4;

        if(direction1==4 || direction2==4 || direction3==4 ||direction4==4){
            result=1000;
        }

        if(direction1==3 || direction2==3|| direction3==3 ||direction4==3){
            result=500;
        }

        return result;
    }

    /**
     * Counts the number of consecutive cells from the specified start point at
     * the specified direction belongs to the specified color.
     * @param i row of start position
     * @param j column of start position
     * @param di row direction
     * @param dj column direction
     * @param c color
     * @return number of consecutive cells
     */
    protected int countDirection(int i, int j, int di, int dj, int c) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if (board[ni][nj] != c) {
            return 0;
        }
        return 1 + countDirection(ni, nj, di, dj, c);
    }

    protected int countEmpty(int i, int j, int di, int dj, int c, int sum) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if ((board[ni][nj] == GomokuGame.EMPTY || board[ni][nj]==c) && sum<15) {
            return countEmpty(ni, nj, di, dj, c,sum+1);
        }
        return sum;

    }


    protected boolean checkIfClosed(int i, int j, int di, int dj, int c) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if (board[ni][nj] == c) {
            return checkIfClosed(ni, nj, di, dj, c);
        }
        else if(board[ni][nj] == GomokuGame.EMPTY){
            return false;
        }
        else{
            return true;
        }

    }
}

