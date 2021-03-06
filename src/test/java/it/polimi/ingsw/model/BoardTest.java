package it.polimi.ingsw.model;

import it.polimi.ingsw.controller.BoardConfigurer;
import it.polimi.ingsw.controller.PowerUpFactory;
import it.polimi.ingsw.controller.WeaponFactory;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.AmmoPack;
import it.polimi.ingsw.model.cards.AmmoTile;
import it.polimi.ingsw.model.cards.PowerUp;
import it.polimi.ingsw.model.cards.Weapon;
import it.polimi.ingsw.model.exceptions.NoMoreCardsException;
import it.polimi.ingsw.model.exceptions.UnacceptableItemNumberException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.polimi.ingsw.model.cards.Color.*;
import static org.junit.Assert.*;

/**
 * Tests all methods of the class Board.
 * The position of the squares in the map is shown using the notation map[row][column]
 * for a better readability.
 *
 * @author BassaniRiccardo
 */

public class BoardTest {


    /**
     * Tests the method getPlayerInside().

     * @throws NoMoreCardsException                 if thrown by simulateScenario().
     * @throws UnacceptableItemNumberException      if thrown by simulateScenario().
     */
    @Test
    public void getPlayersInside() throws NoMoreCardsException, UnacceptableItemNumberException {

        //declares the array the method is expected to return
        ArrayList<Player> expected = new ArrayList<>();

        //simulates a scenario
        Board board = BoardConfigurer.simulateScenario();

        //selects two player and a square; p1 is already in the square
        Player p1 = board.getPlayers().get(0);
        Player p2 = board.getPlayers().get(1);
        AmmoSquare ammoSquare = (AmmoSquare)board.getMap().get(0);

        //checks that p1 is in the square
        expected.add(p1);
        assertEquals(expected, board.getPlayersInside(board.getMap().get(0)));

        //adds p2 to the square
        p2.setPosition(ammoSquare);
        expected.add(p2);

        //checks that the square contains p1 the added players, p2
        assertEquals(expected, board.getPlayersInside(board.getMap().get(0)));

        //remove all the players from the square
        board.getMap().get(0).removePlayer(p1);
        board.getMap().get(0).removePlayer(p2);

        //checks that the square does not contain the removed players
        assertTrue(board.getPlayersInside(board.getMap().get(0)).isEmpty());

    }


    /**
     * Tests the method getAdjacentTest().
     */
    @Test
    public void getAdjacent() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        ArrayList<Square> expected = new ArrayList<>();

        //map.get(1) == map[1][2]: if right, if left covered
        expected.add(board1.getMap().get(0));      //map[1][1]
        expected.add(board1.getMap().get(2));      //map[1][3]
        assertEquals(expected, board1.getAdjacent(board1.getMap().get(1)));
        expected.clear();

        //map.get(3) == map[2][1]: if top covered
        expected.add(board1.getMap().get(0));     //map[1][1]
        expected.add(board1.getMap().get(4));     //map[2][2]
        assertEquals(expected, board1.getAdjacent(board1.getMap().get(3)));
        expected.clear();

        //map.get(4) == map[2][2]: if down covered
        expected.add(board1.getMap().get(3));     //map[2[1]
        expected.add(board1.getMap().get(5));     //map[2][3]
        expected.add(board1.getMap().get(7));     //map[3][2]
        assertEquals(expected, board1.getAdjacent(board1.getMap().get(4)));
        expected.clear();

        //explicitly shows that walls are considered, since map[2][2] is not adjacent to map[1][2]
        assertFalse(board1.getAdjacent(board1.getMap().get(4)).contains(board1.getMap().get(1)));

    }


    /**
     * Tests the method getReachable().
     */
    @Test
    public void getReachable() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        ArrayList<Square> expected = new ArrayList<>();

        //map.get(0): map[1][1]: all instructions covered

        expected.add(board1.getMap().get(0));      //map[1][2]
        expected.add(board1.getMap().get(1));      //map[1][2]
        expected.add(board1.getMap().get(2));      //map[1][3]
        expected.add(board1.getMap().get(3));      //map[2][1]
        expected.add(board1.getMap().get(4));      //map[2][2]
        expected.add(board1.getMap().get(5));      //map[2][3]
        expected.add(board1.getMap().get(7));      //map[3][2]
        assertEquals(expected, board1.getReachable(board1.getMap().get(0), 3));
    }


    /**
     * Tests the method getVisible().
     */
    @Test
    public void getVisible() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        ArrayList<Square> expected = new ArrayList<>();

        //map.get(5) == map[2][3], red room: all instructions covered

        //squares in the same room of one of the adjacent squares: blue room
        expected.add(board1.getMap().get(0));      //map[1][1]
        expected.add(board1.getMap().get(1));      //map[1][2]
        expected.add(board1.getMap().get(2));      //map[1][3]
        // squares in the same room: red room
        expected.add(board1.getMap().get(3));      //map[2][1]
        expected.add(board1.getMap().get(4));      //map[2][2]
        expected.add(board1.getMap().get(5));      //map[2][3]
        //squares in the same room of one of the adjacent squares: yellow room
        expected.add(board1.getMap().get(6));      //map[2][4]
        expected.add(board1.getMap().get(9));      //map[3][4]

        assertEquals(expected, board1.getVisible(board1.getMap().get(5)));
    }


    /**
     * Tests the method getInRoom().
     */
    @Test
    public void getSquaresInRoom() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        ArrayList<Square> expected = new ArrayList<>();

        //map.get(5) == map[2][3], red room: all instructions covered

        // squares in the same room: red room
        expected.add(board1.getMap().get(3));      //map[2][1]
        expected.add(board1.getMap().get(4));      //map[2][2]
        expected.add(board1.getMap().get(5));      //map[2][3]

        assertEquals(expected, board1.getSquaresInRoom(2));
    }


    /**
     * Tests the method getSquaresInLine(), in the event an empty list should be returned.
     * The square map{2}[2] has a wall in the top direction.
     */
    @Test
    public void getSquaresInLineEmpty() {

        Board board1 = BoardConfigurer.configureMap(1);

        //map.get(4): map[2][2]
        assertTrue(board1.getSquaresInLine(board1.getMap().get(4), Board.Direction.UP).isEmpty());

    }


    /**
     * Tests the method getSquaresInLine(), covering all the directions.
     */
    @Test
    public void getSquaresInLineCoveringAllDirections() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        ArrayList<Square> expected = new ArrayList<>();

        // map.get(4): map[2][2]

        //down
        expected.add(board1.getMap().get(7));      //map[3][2]
        assertEquals(expected, board1.getSquaresInLine(board1.getMap().get(4), Board.Direction.DOWN));
        expected.clear();

        //left
        expected.add(board1.getMap().get(3));      //map[2][1]
        assertEquals(expected, board1.getSquaresInLine(board1.getMap().get(4), Board.Direction.LEFT));
        expected.clear();

        //right
        expected.add(board1.getMap().get(5));      //map[2][3]
        expected.add(board1.getMap().get(6));      //map[2][4]
        assertEquals(expected, board1.getSquaresInLine(board1.getMap().get(4), Board.Direction.RIGHT));
        expected.clear();

        //map.get(3): map[2][1]

        //top
        expected.add(board1.getMap().get(0));      //map[2][3]
        assertEquals(expected, board1.getSquaresInLine(board1.getMap().get(3), Board.Direction.UP));
    }


    /**
     * Tests the method getSquaresInLineIgnoringWalls(), in the event an empty list should be returned.
     * The square map[1][1] is in the top-left corner, therefore there are no squares moving in the top direction.
     */
    @Test
    public void getSquaresInLineIgnoringWallsEmpty() {

        Board board1 = BoardConfigurer.configureMap(1);

        //map.get(0): map[1][1]
        assertTrue(board1.getSquaresInLineIgnoringWalls(board1.getMap().get(0), Board.Direction.UP).isEmpty());

    }


    /**
     * Tests the method getSquaresInLineIgnoringWalls(), covering all the directions.
     */
    @Test
    public void getSquaresInLineIgnoringWallsCoveringAllDirections() {

        Board board1 = BoardConfigurer.configureMap(1);

        //declares the array the method is expected to return
        List<Square> expected = new ArrayList<>();

        //map.get(5): map[2][3]

        //top
        expected.add(board1.getMap().get(2));      //map[1][3]
        assertEquals(expected, board1.getSquaresInLineIgnoringWalls(board1.getMap().get(5), Board.Direction.UP));
        expected.clear();

        //down
        expected.add(board1.getMap().get(8));      //map[3][2]
        assertEquals(expected, board1.getSquaresInLineIgnoringWalls(board1.getMap().get(5), Board.Direction.DOWN));
        expected.clear();

        //left
        expected.add(board1.getMap().get(3));      //map[2][1]
        expected.add(board1.getMap().get(4));      //map[2][2]
        assertEquals(expected, board1.getSquaresInLineIgnoringWalls(board1.getMap().get(5), Board.Direction.LEFT));
        expected.clear();

        //right
        expected.add(board1.getMap().get(6));      //map[2][4]
        assertEquals(expected, board1.getSquaresInLineIgnoringWalls(board1.getMap().get(5), Board.Direction.RIGHT));
    }


    /**
     * Tests the method getDistance(), when the arguments are the same square.
     */
    @Test
    public void getDistanceSameSquare() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(0, board1.getDistance(board1.getMap().get(6), board1.getMap().get(6)));

    }


    /**
     * Tests the method getDistance(), when the arguments are adjacent squares.
     */
    @Test
    public void getDistanceAdjacentSquares() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(1, board1.getDistance(board1.getMap().get(0), board1.getMap().get(1) ));

    }


    /**
     * Tests the method getDistance(), when the arguments are squares divided by a wall.
     */
    @Test
    public void getDistanceSquaresDividedByWall() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(3, board1.getDistance(board1.getMap().get(1), board1.getMap().get(4) ));

    }


    /**
     * Tests the method getDistance(), when the arguments are squares located in opposite corners of the map.
     */
    @Test
    public void getDistanceOppositeSquares() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(5, board1.getDistance(board1.getMap().get(0), board1.getMap().get(9) ));

    }


    /**
     * Tests the method getDistance(), when the arguments are squares on the same row, at the opposite side of the map.
     */
    @Test
    public void getDistanceFourSquaresInLine() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(3, board1.getDistance(board1.getMap().get(3), board1.getMap().get(6) ));

    }


    /**
     * Tests the method getDistance(), covering the left distance values.
     */
    @Test
    public void getDistanceCoveringAllDistances() {

        Board board1 = BoardConfigurer.configureMap(1);

        assertEquals(2, board1.getDistance(board1.getMap().get(0), board1.getMap().get(2) ));
        assertEquals(4, board1.getDistance(board1.getMap().get(1), board1.getMap().get(7) ));

    }


    /**
     * Tests the method getDistance(), when an exception should be thrown since a square does not belong to the map.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDistanceBadArguments() {

        Board board1 = BoardConfigurer.configureMap(1);

        AmmoSquare externalSquare = new AmmoSquare(board1, 15,2,3,4, RED);
        board1.getDistance(board1.getMap().get(0), externalSquare);

    }


    /**
     * Tests the method setLeftWalls(), when a bad parameter is entered.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setLeftWallsBadArgument() {

        Board board1 = BoardConfigurer.configureMap(1);

        boolean[][] walls = {{true,true,true,true},{false,false,false,false}};
        board1.setLeftWalls(walls);
    }


    /**
     * Tests the method setTopWalls(), when a bad parameter is entered.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setTopWallsBadArgument() {

        Board board1 = BoardConfigurer.configureMap(1);

        boolean[][] walls = {{true,true,true,true},{false,false,false,false}, {false, true, false,false,false}};
        board1.setTopWalls(walls);
    }


    /**
     * Tests the method setSpawnPoints(), when a bad parameter is entered: less than three swap points.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setSpawnPointsOnlyOne() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<WeaponSquare> spawnPoints = new ArrayList<>();
        spawnPoints.add(new WeaponSquare(board1, 1,1,1,1,RED));
        board1.setSpawnPoints(spawnPoints);
    }


    /**
     * Tests the method setSpawnPoints(), when a bad parameter is entered: a color different from red, blue, green.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setSpawnPointsWrongColors() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<WeaponSquare> spawnPoints = new ArrayList<>();
        spawnPoints.add(new WeaponSquare(board1, 1,1,1,1,RED));
        spawnPoints.add(new WeaponSquare(board1, 2,2,2,2,GREEN));
        spawnPoints.add(new WeaponSquare(board1, 3,3,3,3,BLUE));
        board1.setSpawnPoints(spawnPoints);
    }


    /**
     * Tests the method setSpawnPoints(), when a bad parameter is entered: two swap points have the same color.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setSpawnPointsNotDistinctColors() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<WeaponSquare> spawnPoints = new ArrayList<>();
        spawnPoints.add(new WeaponSquare(board1,1,1,1,1,RED));
        spawnPoints.add(new WeaponSquare(board1, 2,2,2,2,BLUE));
        spawnPoints.add(new WeaponSquare(board1, 3,3,3,3,BLUE));
        board1.setSpawnPoints(spawnPoints);
    }


    /**
     * Tests the method setSpawnPoints(), when a bad parameter is entered: two swap points are in the same room.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setSpawnPointsNotDistinctRooms() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<WeaponSquare> spawnPoints = new ArrayList<>();
        spawnPoints.add(new WeaponSquare(board1,1,1,1,1,RED));
        spawnPoints.add(new WeaponSquare(board1, 2,2,2,2,BLUE));
        spawnPoints.add(new WeaponSquare(board1, 3,2,3,3,YELLOW));
        board1.setSpawnPoints(spawnPoints);
    }


    /**
     * Tests the method setPlayer(), when a bad parameter is entered: player number not between 3 and 5.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setPlayersWrongNumber() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<Player> players = new ArrayList<>();
        players.add(new Player(1, Player.HeroName.BANSHEE, board1));
        board1.setPlayers(players);
    }


    /**
     * Tests the method setWeaponDeck(), when a bad parameter is entered: not 21 drawable cards.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setWeaponDeckWrongNumberOfCards() {

        Board board1 = BoardConfigurer.configureMap(1);
        WeaponFactory weaponFactory = new WeaponFactory(board1);

        Deck weaponDeck = new Deck();
        weaponDeck.addCard(weaponFactory.createWeapon(Weapon.WeaponName.LOCK_RIFLE));
        board1.setWeaponDeck(weaponDeck);
    }


    /**
     * Tests the method setPowerUpDeck(), when a bad parameter is entered: not 24 drawable cards.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setPowerUpDeckWrongNumberOfCards() {

        Board board1 = BoardConfigurer.configureMap(1);
        PowerUpFactory powerUpFactory = new PowerUpFactory(board1);

        Deck powerUpDeck = new Deck();
        for (int i = 0; i< 50; i++) powerUpDeck.addCard(powerUpFactory.createPowerUp(PowerUp.PowerUpName.TARGETING_SCOPE, RED));
        board1.setPowerUpDeck(powerUpDeck);
    }


    /**
     * Tests the method setAmmoDeck(), when a bad parameter is entered: not 0 discarded cards.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setAmmoDeckNotEmptyDiscards() {

        Board board1 = BoardConfigurer.configureMap(1);

        Deck ammoDeck = new Deck();
        for (int i = 0; i < 36; i++) ammoDeck.addCard(new AmmoTile(false, new AmmoPack(0,1,2)));
        ammoDeck.addDiscardedCard(new AmmoTile(true,new AmmoPack(1,0,2)));
        board1.setWeaponDeck(ammoDeck);
    }


    /**
     * Tests the method setKillShotTrack(), when a bad parameter is entered: less than 5 skulls.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setKillShotTrack() {

        Board board1 = BoardConfigurer.configureMap(1);

        board1.setKillShotTrack(new KillShotTrack(12, board1));
    }


    /**
     * Tests the method setMap(), when a bad parameter is entered: less than 10 squares.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setMap() {

        Board board1 = BoardConfigurer.configureMap(1);

        List<Square> map = new ArrayList<>();
        map.add(new AmmoSquare(board1,1,1,1,1,GREEN));
        board1.setMap(map);
    }


    /**
     * Tests the method sort().
     */
    @Test
    public void sort(){

        Board b = BoardConfigurer.configureMap(4);
        Player p1 = new Player(1, Player.HeroName.BANSHEE, b);
        Player p2 = new Player(2, Player.HeroName.D_STRUCT_OR, b);
        Player p3 = new Player(3, Player.HeroName.SPROG, b);
        Player p4 = new Player(4, Player.HeroName.VIOLET, b);

        List<Player> toSort = new ArrayList<>(Arrays.asList(p1, p2, p3, p4));
        List<Player> occurrences = new ArrayList<>(Arrays.asList(p2, p1, p3, p3, p2, p1, p3, p3, p4));

        b.sort(toSort, occurrences);

        //p3 has the highest number of occurrences, p2 and p1 has the same but p2 appears first in the list; p4 has the lowest number of occurrences
        assertEquals(new ArrayList<>(Arrays.asList(p3, p2, p1, p4)), toSort);

    }
}