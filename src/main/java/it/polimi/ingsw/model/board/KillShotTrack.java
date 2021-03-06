package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.Updater;
import it.polimi.ingsw.model.exceptions.UnacceptableItemNumberException;
import it.polimi.ingsw.model.exceptions.WrongTimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.polimi.ingsw.model.board.Board.MAX_SKULL_NUMBER;

/**
 * Represents the kill shot track of the board.
 * Registers the kills memorizing the killer for each kill.
 * Rewards the killers at the end of the game.
 *
 * @author  BassaniRiccardo
 */


public class KillShotTrack {

    private int skullsLeft;
    private List<Player> killers;
    private Board board;
    private static final Logger LOGGER = Logger.getLogger("serverLogger");
    private static final int MAX_POINTS_TO_GIVE_KILLSHOT_TRACK = 8;
    private static final int POINTS_TO_GIVE_STANDARD_DECREASE = 2;
    private static final int POINTS_TO_GIVE_LAST_DECREASE = 1;
    private static final int THRESHOLD_FOR_LAST_DECREASE = 2;


    /**
     * Constructs a kill shot track with the number of skulls to addList to the track and a reference to the board.
     *
     * @param skullsNumber  the number of skulls to addList to the track.
     * @param board         the board the kill shot track is in.
     * @throws              IllegalArgumentException if the number of skulls is higher than 8.
     */
    public KillShotTrack(int skullsNumber, Board board) {

        if (skullsNumber > MAX_SKULL_NUMBER) throw new IllegalArgumentException("The number of skulls can not be higher than " + MAX_SKULL_NUMBER + ".");

        this.skullsLeft = skullsNumber;
        this.killers = new ArrayList<>();
        this.board = board;

    }


    /**
     * Getter for skullsLeft.
     *
     * @return          the number of left skulls.
     */
    public int getSkullsLeft() {
        return skullsLeft;
    }

    /**
     * Getter for killers
     *
     * @return          the players responsible for the kills, orderly and with an occurrence
     *                  per kill and an extra occurrence for each overkill.
     */
    public List<Player> getKillers() {
        return killers;
    }


    /**
     * Removes a specified number of skulls from the track.
     *
     * @param quantity  the number of skulls to remove.
     * @throws UnacceptableItemNumberException if removing the skulls would lead to have a negative number of skulls.
     */
    public void removeSkulls(int quantity) throws UnacceptableItemNumberException{

        if (quantity > skullsLeft) throw new UnacceptableItemNumberException("The number of skulls left must be major or equal to zero.");
        skullsLeft -= quantity;

    }


    /**
     * Registers a kill.
     * Adds the killer to the killers list, two times if he overkilled the opponent.
     * If there are still skulls left, removes a skull and updates the awards given by the next death of the dead.
     * If there are no skulls left, it means the kill occurred in the final turn or in the final frenzy,
     * therefore the awards do not need to be updated.
     *
     * @param killer        the player who killed the opponent.
     * @param dead          the player who had been killed by the opponent.
     * @param overkill      whether the killer overkilled the opponent.
     * @throws              UnacceptableItemNumberException     if thrown by removeSkulls().
     * @throws              WrongTimeException                  if the player in the "dead" parameter is not dead.
     * @throws              IllegalArgumentException            if the killer and the dead are the same player.
     */
    public void registerKill(Player killer, Player dead, boolean overkill) throws UnacceptableItemNumberException, WrongTimeException {

        if (!dead.isDead())         throw new WrongTimeException("A kill can be registered only when a player dies.");
        if (killer.equals(dead))    throw new IllegalArgumentException("The killer and the dead can not be the same player.");
        killers.add(killer);
        boolean ok = false;
        if (overkill){
            killers.add(killer);
            ok = true;
        }
        if (skullsLeft != 0) {
            dead.updateAwards();
            removeSkulls(1);
            board.addToUpdateQueue(Updater.get(Updater.REMOVE_SKULL_UPD, 1, killer, ok));
            LOGGER.log(Level.INFO, "Skulls left: {0}. ", skullsLeft);
        }
        else {
            dead.setDead(false);
            dead.getDamages().clear();
            board.addToUpdateQueue(Updater.get(Updater.REMOVE_SKULL_UPD, 0, killer, ok));
        }
    }


    /**
     * Rewards the players who killed at least one opponent, in accordance with the number of opponents killed.
     * The first gets 8 points, the second 6, the third 4, the fourth 2 and the fifth 1.
     * In the event of a draw, the player who got the earlier kill shot wins the draw.
     */
    public void rewardKillers() {

        //asks the board for the players
        List<Player> playersToReward = new ArrayList<>(this.board.getPlayers());

        //properly orders the playersToReward
        board.sort(playersToReward, killers);

        //assigns the points
        int pointsToGive = MAX_POINTS_TO_GIVE_KILLSHOT_TRACK;
        for(Player p : playersToReward){
            if (killers.contains(p)){
                p.addPoints(pointsToGive);
                String msg = p.getId() + " gains " + pointsToGive + " points.";
                LOGGER.log(Level.INFO, msg);

            }
            if (pointsToGive == THRESHOLD_FOR_LAST_DECREASE) pointsToGive -= POINTS_TO_GIVE_LAST_DECREASE;
            else pointsToGive -= POINTS_TO_GIVE_STANDARD_DECREASE;
        }

    }

}