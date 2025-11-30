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
    private static final int MAX_DEPTH = 5;

    private static final long TIME_LIMIT_MILLIS = 4800L;

    public AlphaBetaAgent(PlayerTurn playerTurn) {
        super(playerTurn);
    }

    public AlphaBetaAgent(String agentName) {
        super(agentName);
    }

    public AlphaBetaAgent(String agentName, PlayerTurn playerTurn) {
        super(agentName, playerTurn);
    }

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        long endTime = System.currentTimeMillis() + TIME_LIMIT_MILLIS;

        //reset counters
        this.nodesExamined = 0;
        this.reachedLeafNodes = 0;
        this.prunedCounter = 0;
        this.searchDepth = 0;

        List<ObjectiveWrapper> moves = AgentController.getAvailableMoves(gameState, playerTurn);

        // check for null before isEmpty to avoid NPE
        if (moves == null || moves.isEmpty()) {
            return null;
        }

        ObjectiveWrapper bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (ObjectiveWrapper move : moves) {
            GameBoardState child = AgentController.getNewState(gameState, move);

            // evaluate the child state after AI move; next player is opponent
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

        System.out.println("--------------------");
        System.out.println("Search depth: " + this.searchDepth);
        System.out.println("Nodes explored: " + nodesExamined);
        System.out.println("Leaf nodes reached: "  + reachedLeafNodes);
        System.out.println("Pruned counter : " + prunedCounter);
        System.out.println("--------------------");


        return new MoveWrapper(bestMove);

    }

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
                // pass opponent of currentPlayer to alternate turns correctly
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
