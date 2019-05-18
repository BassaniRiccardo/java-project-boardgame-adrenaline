package it.polimi.ingsw.view.CLIRenderer;

import it.polimi.ingsw.view.ClientModel;

import java.util.List;

//TODO: rewrite all CLI rendering functions properly, make the code more robust
public class WeaponRenderer {

    public static String[][] get(ClientModel clientModel){
        String[][] box = new String[7][60];

        String[] weapons = new String[clientModel.getWeaponsOnGround().keySet().size()];
        List<Integer> shooters = clientModel.getKillShotTrack();
        String[] killshotTrack = new String[shooters.size()];

        for(int i=0; i<shooters.size(); i++){
            killshotTrack[i] = ClientModel.getEscapeCode(clientModel.getPlayerColor().get(i)) + "x" + "\u001b[0m";
        }

        int ind=0;
        for(int roomID : clientModel.getWeaponsOnGround().keySet()){
            weapons[ind] = "Room " + roomID + ": ";
            List<String> list = clientModel.getWeaponsOnGround().get(roomID);
            for (int i = 0; i< list.size(); i++){
                weapons[ind].concat(list.get(i));
                if(i!=list.size()-1){
                    weapons[ind].concat(", ");
                }
            }
            ind++;
        }

        for(int i=0; i<box.length; i++){
            for(int j=0; j<box[i].length; j++){
                box[i][j] = " ";
            }
        }

        for(int i=0; i<weapons.length; i++){
            for(int j=0; j<weapons[i].length(); j++){
                box[1+i][3+j] = String.valueOf(weapons[i].charAt(j));
            }
        }

        for(int i=0; i<"KillShot: ".length(); i++){
            box[5][i+3] = String.valueOf("Killshot: ".charAt(i));
        }

        for(int i=0; i< killshotTrack.length; i++){
            box[5][i+14] = String.valueOf(killshotTrack[i]);
        }

        return box;
    }
}