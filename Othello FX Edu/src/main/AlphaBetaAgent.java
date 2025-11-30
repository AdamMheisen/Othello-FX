package main;
import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;
import com.eudycontreras.othello.controllers.AgentController;


import java.util.List;

public class AlphaBetaAgent extends Agent {
    private static final int MAX_DEPTH = 6;

    private static final long TIME_LIMIT_MILLIS = 4800L;

    public AlphaBetaAgent(String agentName, PlayerTurn playerTurn) {
        super(agentName, playerTurn);
    }

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + TIME_LIMIT_MILLIS;

        //reset counters
        this.nodesExamined = 0;
        this.reachedLeafNodes = 0;
        this.prunedCounter = 0;
        this.searchDepth = 0;

        List<ObjectiveWrapper> moves = AgentController.getAvailableMoves(gameState, playerTurn);

        //This is here so it doesn't crash when the game is over
        if (moves == null || moves.isEmpty()) {

            return null;
        }

        ObjectiveWrapper bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (ObjectiveWrapper move : moves) {
            GameBoardState child = AgentController.getNewState(gameState, move);

            /**
             * This line starts the alpha-beta search for one of the AI’s possible moves.
             * The AI has already made the move, so we pass the resulting board (“child”).
             * We reduce the depth by one, because the top level was handled in getMove.
             * The next player in the simulation is the opponent, so we pass their turn.
             * Alpha and beta start at negative and positive infinity as usual.
             * We also pass the global time limit so the search stops if time runs out.
             * The current depth is set to 1 because we are one level below the root.
             *
             * @param child                     The board game state after making the move
             * @param MAX_DEPTH                 The maximum depth to search
             * @param Double.NEGATIVE_INFINITY  The initial alpha value
             * @param Double.POSITIVE_INFINITY  The initial beta value
             * @param getOpponent(playerTurn)   The opponent's turn
             * @param endTime                   The time limit for the search
             * @return                          The evaluated value of the move
             */
            double value = alphaBeta(
                    child,
                    MAX_DEPTH - 1,
                    Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    getOpponent(playerTurn),
                    endTime,
                    1
            );

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            bestMove = moves.get(0);
        }

        return new MoveWrapper(bestMove);
    }

    /**
     * This method runs a minimax search with alpha-beta pruning.
     * It checks three things before searching deeper:
     * (1) if the time limit is reached,
     * (2) if the depth limit is reached,
     * (3) if the position is terminal.
     * In those cases it returns a heuristic value.
     *
     * If it’s the AI’s turn, the method tries to maximize the score.
     * If it’s the opponent’s turn, it tries to minimize the score.
     * Alpha and beta values are updated, and branches are skipped when they cannot change the final result (alpha ≥ beta).
     *
     * @param state         The current board position being evaluated.
     * @param depth         How many levels deeper the search is allowed to go.
     * @param alpha         The best score the maximizing player has found so far.
     * @param beta          The best score the minimizing player has found so far.
     * @param currentPlayer Which player is about to move at this node.
     * @param endTime       Absolute time limit; used to stop the search if time runs out.
     * @param currentDepth  The current depth in the search tree (used for statistics).
     *
     * @return A heuristic evaluation of the state from the AI’s perspective.
     */
    private double alphaBeta( GameBoardState state, int depth, double alpha, double beta, PlayerTurn currentPlayer, long endTime, int currentDepth) {
        nodesExamined++;

        if(currentDepth > this.searchDepth) {
            this.searchDepth = currentDepth;
        }

        // time cutoff
        if(System.currentTimeMillis() >= endTime) {
            reachedLeafNodes++;
            return AgentController.getGameEvaluation(state, playerTurn);
        }

        // depth cutoff
        if (depth <= 0) {
            reachedLeafNodes++;
            return AgentController.getGameEvaluation(state, playerTurn);
        }

        List<ObjectiveWrapper> moves = AgentController.getAvailableMoves(state, currentPlayer);

        if (moves == null || moves.isEmpty()) {
            reachedLeafNodes++;
            return AgentController.getGameEvaluation(state, playerTurn);
        }

        boolean maximizing = (currentPlayer == playerTurn);

        if(maximizing) {
            double value = Double.NEGATIVE_INFINITY;
            for (ObjectiveWrapper move : moves) {
                GameBoardState child = AgentController.getNewState(state, move);
                value = Math.max(value, alphaBeta(child, depth -1, alpha, beta, getOpponent(currentPlayer), endTime, currentDepth + 1 ));
                alpha = Math.max(alpha, value);

                if(alpha >= beta) {
                    prunedCounter++;
                    break;
                }
            }
            return value;
        } else  {
            double value = Double.POSITIVE_INFINITY;
            for (ObjectiveWrapper move : moves) {
                GameBoardState child = AgentController.getNewState(state, move);
                value = Math.min(value, alphaBeta(child, depth - 1, alpha, beta, getOpponent(currentPlayer), endTime, currentDepth + 1));
                beta = Math.min(beta, value);
                if(alpha >= beta) {
                    prunedCounter++;
                    break;
                }
            }
            return value;
        }
    }

    private PlayerTurn getOpponent(PlayerTurn turn) {
        return (turn == PlayerTurn.PLAYER_ONE) ? PlayerTurn.PLAYER_TWO : PlayerTurn.PLAYER_ONE;
    }


}
