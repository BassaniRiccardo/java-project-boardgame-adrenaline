package it.polimi.ingsw.model.cards;
import it.polimi.ingsw.model.board.Player;
import it.polimi.ingsw.model.board.Square;
import it.polimi.ingsw.model.exceptions.NotAvailableAttributeException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class modeling a single firemode of a weapon.
 *
 * @author marcobaga
 */

public class FireMode implements Targeted {

    public enum FireModeName {
        MAIN, SECONDARY, OPTION1, OPTION2
    }

    private final FireModeName name;
    private final AmmoPack cost;
    private Weapon weapon;

    private final Effect effect;
    private final TargetFinder targetFinder;
    private final DestinationFinder destinationFinder;
    private static final Logger LOGGER = Logger.getLogger("serverLogger");

    /**
     * Constructs a firemode, with a name, the number of targets, the cost in ammo packs,
     * the related destination finder, target finder and effect.
     *
     * @param name                  the firemode name.
     * @param cost                  the cost in ammo packs.
     * @param destinationFinder     the related destination finder.
     * @param targetFinder          the related target finder.
     * @param effect                the related effect.
     */
    public FireMode(FireModeName name, AmmoPack cost, DestinationFinder destinationFinder, TargetFinder targetFinder, Effect effect){

        this.name = name;
        this.cost = cost;
        this.destinationFinder = destinationFinder;
        this.targetFinder = targetFinder;
        this.effect = effect;

    }

    /*
     * Getters
     */


    public AmmoPack getCost() { return cost; }

    public FireModeName getName() { return name; }

    public TargetFinder getTargetFinder() { return targetFinder; }

    public DestinationFinder getDestinationFinder() { return destinationFinder; }

    public Weapon getWeapon() { return weapon; }

    /*
     * Setters.
     */
    public void setWeapon(Weapon weapon){ this.weapon = weapon; }

    /**
     *Applies the effects of this firemode to targets chosen.
     *
     * @param  targets           the ArrayList of players being targeted
     * @param  destination       the Square players are moved to, if relevant
     */
    public void applyEffects(List<Player> targets, Square destination) throws NotAvailableAttributeException {

        if(targets == null || targets.isEmpty()){
            throw new IllegalArgumentException("A target is necessary for the effects to be applied.");
        }
        LOGGER.log(Level.INFO, ()->name + "Weapon used: " + weapon);
        String msg = name + "Holder: " + weapon.getHolder();
        LOGGER.log(Level.INFO, msg);
        LOGGER.log(Level.INFO, ()->name + "Targets: " + targets);
        LOGGER.log(Level.INFO, ()->name + "Destination: " + destination);
        for(Player p : new ArrayList<>(targets)){
            effect.apply(weapon.getHolder(), p, destination);
        }
    }

    /**
     * Finds players that can be chosen as targets.
     *
     * @return      an ArrayList containing sets of targets to be chosen, each saved as an ArrayList
     */
    public List<List<Player>> findTargets() throws NotAvailableAttributeException{
        List<List<Player>> res = targetFinder.find(weapon.getHolder());
        String msg = name + " " + weapon + "Targets found: " + res;
        LOGGER.log(Level.INFO, msg);
        return res;

    }

    /**
     * Finds Squares that can be chosen as destination, if relevant
     *
     * @param  targets  the ArrayList of already selected targets
     * @return          the set of possible destination Square objects
     */
    public List<Square> findDestinations(List<Player> targets) throws NotAvailableAttributeException{
        if(targets == null){
            throw new NullPointerException("The firemode must have some targets.");
        }
        List<Square> res = destinationFinder.find(weapon.getHolder(), targets);
        LOGGER.log(Level.FINE, "Destinations found: {0}", res);
        return res;
    }

    /**
     *Establishes if this firemode can be selected according to the current board state
     *
     * @return      true is this FireMode can be used
     */
    public boolean isAvailable() throws NotAvailableAttributeException {
        for (List<Player> targets : findTargets()){
            if (!targets.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Returns a string representing the firemode.
     *
     * @return      the description of the firemode.
     */
    @Override
    public String toString() {
        return this.weapon + ": " + this.getName();
    }
}