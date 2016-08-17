/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa42.Team9.Conn;

import java.util.Map; 
import Uno.model.Game;
import java.util.HashMap;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author E0015359
 */
@ApplicationScoped
public class GameTable {
    
     private static Map<String, Game> gametable = new HashMap<>();

    public static Map<String, Game> getGametable() {
        return gametable;
    }
 
}
