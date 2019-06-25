package it.polimi.ingsw.view.CLIRenderer;

import it.polimi.ingsw.view.ClientModel;

import java.util.logging.Logger;
import static it.polimi.ingsw.view.CLIRenderer.MainRenderer.RESET;

/**
 * Class creating a bidimensional String array containing data about the player using this client
 */
public class HandRenderer {

    private static final Logger LOGGER = Logger.getLogger("clientLogger");
    private static final int HAND_HEIGHT = 7;
    private static final int HAND_WIDTH = 55;


    private HandRenderer(){}

    public static String[][] get(ClientModel clientModel) {

        String[][] box = new String[HAND_HEIGHT][HAND_WIDTH];
        for (int i = 0; i < box.length; i++) {
            for (int j = 0; j < box[i].length; j++) {
                box[i][j] = " ";
            }
        }

        if (HAND_HEIGHT<5 || HAND_WIDTH <50) {
            return box;
        }

        //printing name
        ClientModel.SimplePlayer you = clientModel.getPlayer(clientModel.getPlayerID());
        String name =  "You (" + you.getUsername() + ")";
        for(int i=0; i<name.length()&&i<HAND_WIDTH; i++){
            box[1][i+3] = ClientModel.getEscapeCode(you.getColor()) + name.charAt(i) + RESET;
        }

        //printing life
        String life = "Life: ";
        for(int i =0; i<life.length()&&i+3<HAND_WIDTH; i++){
            box[2][i+3] = String.valueOf(life.charAt(i));
        }
        int j = life.length()+3;
        for (int shooter : you.getDamageID()) {
            if(j>HAND_WIDTH-1){
                break;
            }
            box[2][j] = ClientModel.getEscapeCode(clientModel.getPlayer(shooter).getColor()) + "●" + RESET;
            j++;
        }

        //printing marks
        String marks = "Marks: ";
        for(int i = 0; i<marks.length()&&i+30<HAND_WIDTH; i++){
            box[2][i+30] = String.valueOf(marks.charAt(i));
        }
        j=marks.length()+30;
        for (int shooter : you.getMarksID()) {
            if(j>HAND_WIDTH-1){
                break;
            }
            box[2][j] = ClientModel.getEscapeCode(clientModel.getPlayer(shooter).getColor()) + "◎" + RESET;
            j++;
        }

        //printing ammo
        for(int i=0; i<"Ammo: ".length()&&i+3<HAND_WIDTH; i++){
            box[3][i+3] = String.valueOf("Ammo: ".charAt(i));
        }
        j=9;
        for(int i=0; i<you.getBlueAmmo()&&j<HAND_WIDTH; i++){
            box[3][j] = ClientModel.getEscapeCode("blue") + "|" + RESET;
            j++;
        }
        for(int i=0; i<you.getRedAmmo()&&j<HAND_WIDTH; i++){
            box[3][j] = ClientModel.getEscapeCode("red") + "|" + RESET;
            j++;
        }
        for(int i=0; i<you.getYellowAmmo()&&j<HAND_WIDTH; i++){
            box[3][j] = ClientModel.getEscapeCode("yellow") + "|" + RESET;
            j++;
        }

        //printing weapons
        StringBuilder weapons = new StringBuilder();
        weapons.append("Weapons: ");
        if(you.getWeapons().isEmpty()){
            weapons.append("none");
        } else{
            for(int i=0; i<you.getWeapons().size(); i++){
                ClientModel.SimpleWeapon w = you.getWeapons().get(i);
                weapons.append(w.getName());
                if(w.isLoaded()){
                    weapons.append("*");
                }
                if(i!=you.getWeapons().size()-1){
                    weapons.append(", ");
                }
            }
        }
        for(int i=0; i<weapons.toString().length()&&i+3<HAND_WIDTH; i++){
            box[4][i+3] = String.valueOf(weapons.toString().charAt(i));
        }

        //printing hand
        String hand = "Hand: ";
        int start = 0;
        for(; start<hand.length()&&start+3<HAND_WIDTH; start++){
            box[5][start+3] = String.valueOf(hand.charAt(start));
        }

        if(clientModel.getPowerUpInHand().isEmpty()){
            String empty = "empty";
            for(int i=0; i<empty.length()&&3+start<HAND_WIDTH; i++){
                box[5][3+start] = String.valueOf(empty.charAt(i));
                start++;
            }
        } else {
            for(int i=0; i<clientModel.getPowerUpInHand().size(); i++){
                String pup = clientModel.getPowerUpInHand().get(i);
                String color = clientModel.getColorPowerUpInHand().get(i);
                for(int k=0; k<pup.length()&&3+start<HAND_WIDTH; k++) {
                    box[5][3+start] = (clientModel.getEscapeCode(color)+pup.charAt(k)+RESET);
                    start++;
                }
                if(i<clientModel.getPowerUpInHand().size()-1&&3+start<HAND_WIDTH) {
                    box[5][3+start] = ",";
                    start=start+2;
                }
            }
        }
        return box;
    }
}
