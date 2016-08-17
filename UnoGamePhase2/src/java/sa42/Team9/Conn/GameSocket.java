package sa42.Team9.Conn;

import Starter.GameEngine;
import Uno.model.Game;
import Uno.model.unoCard;
import Uno.model.unoPlayer;
import enums.Image;
import enums.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author VEERASH
 */
@Dependent
@ServerEndpoint("/game/{data}")
public class GameSocket {

    @Inject
    private GameTable gameTable;
    private Session session;
    private String data;

    @OnOpen
    public void open(Session s, @PathParam("data") String data) {

        System.out.println("session started");
        session = s;
        try {
            if (data.equals("table")) {
                session.getBasicRemote().sendText("Table");

            } else {
                System.out.println("this is from player");
                sendGameList();
            }

        } catch (Exception ex) {
            System.err.println("Exception in open");
        }
        System.out.println(">>>Connected to server " + session.getId());

    }

    @OnMessage
    public void onMessage(String msg) {

        System.out.println("msg came from index.js");

        JsonReader reader = Json.createReader(
                new ByteArrayInputStream(msg.getBytes()));
        JsonObject json = reader.readObject();

        System.out.println("Incomming Message");
        System.out.println(json.toString());

        String ConnectType = json.getString("ConnectType");
        System.out.println(ConnectType);

        switch (ConnectType) {
            case "GameDetails":
                sendWaitGamePage(json);  // Methods below
                break;
            case "PlayerDetails":
                addPlayertoList(json);
                break;
            case "StartGame":
                startGame(json);
                break;
            case "PlayerRefresh":
                sendPlayerCards(json);
                break;
            default:
                System.out.println("wait for other msg");
        }

    }

    @OnClose
    public void onClose() {
    }

    @OnError
    public void onError(Throwable t) {
    }

    //-----------METHODS-----------------------------// 
    public void sendWaitGamePage(JsonObject json) {

        Integer maxp = Integer.parseInt(json.getString("NoOfPlayer"));  //get NoofPlayer from incoming msg

        String gameTitle = json.getString("gname");
        String generatedID = UUID.randomUUID().toString().substring(0, 8);
        String gameUID = generatedID;

        Game game = new Game(gameUID, gameTitle, Status.GAME_WAITING, maxp);
        gameTable.getGametable().put(gameUID, game);

        System.out.print(">> Created the game：ID:" + gameUID + " TITLE:" + gameTitle);

        JsonObjectBuilder JsonBuilder = Json.createObjectBuilder() //send to waitgame article
                .add("ConnectType", "WaitForPlayers")
                .add("gameId", gameUID)
                .add("gameTitle", gameTitle)
                .add("maxplayers", maxp);

        JsonObject json1 = JsonBuilder.build();
        {
            try {
                session.getBasicRemote().sendText(json1.toString());
            } catch (IOException ex) {
                System.out.println("problem");
            }
        }
    }

    public void sendGameList() {
        System.out.print(">> Get the Game List");

        JsonArrayBuilder gameJsonArray = Json.createArrayBuilder();
        for (Map.Entry<String, Game> game : gameTable.getGametable().entrySet()) {
            gameJsonArray.add(game.getValue().toJson());
            System.out.println(game.getValue().toJson());
        }
        //gameJsonArray.build();
        System.out.println("builder - " + gameJsonArray.toString());

        JsonObjectBuilder outerJsonBuilder = Json.createObjectBuilder()
                .add("ConnectType", "GameList")
                .add("List", gameJsonArray.build());

        JsonObject outerJson = outerJsonBuilder.build();

        System.out.println("outerJson - " + outerJson.toString());
        try {
            session.getBasicRemote().sendText(outerJson.toString());
        } catch (IOException ex) {
            System.out.println("issue: getGameList");
        }

    }

    public void addPlayertoList(JsonObject json) {
        UUID id = UUID.randomUUID();
        String playerId = id.toString().substring(0, 6);

        String playerName = json.getString("pName");
        String gameId = json.getString("gId");

        unoPlayer player = new unoPlayer(playerName, playerId);

        Game g = gameTable.getGametable().get(gameId);

        System.out.println(">> Player Max " + g.getmaxPlayers() + "Current player " + g.getGamePlayers().size());
        if (g.getmaxPlayers() == g.getGamePlayers().size()) {
            try {
                session.getBasicRemote().sendText("Game is Full");
            } catch (IOException ex) {
                System.out.println("issue: addPlayertoList");
            }
        }
        g.addPlayer(player);

        int amount = 0;
        amount = g.getGamePlayers().size();
        JsonObject json2 = Json.createObjectBuilder()
                .add("ConnectType", "PlayerAmount")
                .add("amount", amount)
                .build();

        System.out.println("PlayerAmount- " + amount);

        session.getOpenSessions().stream().forEach(s -> {
            try {
                s.getBasicRemote().sendText(json2.toString());
            } catch (IOException ex) {
                System.out.println("issue: sendPlayerAmount");
            }
        });

    }

    public void startGame(JsonObject json) {

        String gameId = json.getString("gId");
        Game g = gameTable.getGametable().get(gameId);
        GameEngine.initDeck(g.getGameDeck());
        GameEngine.initGame(g);
        JsonArrayBuilder gameInfoJsonArray = Json.createArrayBuilder();

        JsonObject gameJson = Json.createObjectBuilder()
                .add("img1", Image.BACK)
                .add("img2", g.getDicardPile().getImage())
                .build();

        gameInfoJsonArray.add(gameJson);

        for (unoPlayer p : g.getGamePlayers()) {
            JsonObject gameJson1 = Json.createObjectBuilder()
                    .add("name", p.getName())
                    .build();
            gameInfoJsonArray.add(gameJson1);
        }

        JsonObjectBuilder outerJsonBuilder = Json.createObjectBuilder()
                .add("ConnectType", "GamePlay")
                .add("Cards", gameInfoJsonArray.build());

        JsonObject outerJson = outerJsonBuilder.build();

        System.out.println("StartGameJson - " + outerJson.toString());

        try {
            session.getBasicRemote().sendText(outerJson.toString());
        } catch (IOException ex) {
            System.out.println("issue: startGame ");
        }

    }

    public void sendPlayerCards(JsonObject json) {
        String playerName = json.getString("pName");
        String gameId = json.getString("gId");

        Game game = gameTable.getGametable().get(gameId);
        System.out.println(game.getGameStatus());

        if (!game.getGameStatus().equals(Status.GAME_START)) {
            System.out.println("CANNOT START THE STARTED GAME");

        } else {

            JsonArrayBuilder handCards = Json.createArrayBuilder();
            unoPlayer player = null;
            for (unoPlayer p : game.getGamePlayers()) {
                if (p.getName().equals(playerName)) {
                    player = p;
                }
            }
            for (unoCard c : player.getHandCards()) {
                JsonObject cards = Json.createObjectBuilder()
                        .add("card", c.getImage())
                        .build();
                handCards.add(cards);
            }

            JsonObjectBuilder outerJsonBuilder = Json.createObjectBuilder()
                    .add("ConnectType", "PlayerCards")
                    .add("List", handCards.build());

            JsonObject outerJson = outerJsonBuilder.build();

            try {
                session.getBasicRemote().sendText(outerJson.toString());
            } catch (IOException ex) {
                System.out.println("issue: sendPlayerCards ");
            }

        }
    }

}
