var connection = null;
$(function () {

    $("#gameListRefresh").on("singletap", function () {
        connection = new WebSocket("ws://localhost:8080/UnoGamePhase2/game/player");
        connection.onopen = function () {
            console.log("connected");
        };
        connection.onmessage = function (evt) {

            console.log("in onmessage" + evt.data);
            var data = JSON.parse(evt.data);
            console.log(data.ConnectType);
            if (data.ConnectType === "GameList") {
                console.log("Data Connect Type is GameList");
                GameList(data.List);   // method below

                $("#gameList").on("doubletap", "li", function () {  //First tap
                    var id = $(this).find("#selected-id").text();
                    if ($(this).find("#selected-status").text() === "GAME_STARTED") {
                        alert("The Game Already Started!");
                    }
                    $("#detailsGameId").text(id);
                    $.UIGoToArticle("#player_details");

                    $("#joinBtn").on("singletap", function () {      //Second tap
                        sendPlayerDetail(); // method below    
                        $("#refreshGameBtn").on("singletap", function () {
                            sendforCardsInfo();
                        });

                    });

                });

                console.log("Msg is comming");
            } else if (data.ConnectType === "PlayerCards") {
                console.log("Data Connect Type is PlayerCards");
                CardsList(data.List);   // method below
            }
            ;

        };

     //==========JQUERY METHODS ============== //
        var GameList = function (result) {

            console.log("inGameList function");
            $("#gameList").html("");
            var listTemplate = Handlebars.compile($("#listTemplate").html());
            var count = 0;
            for (var i in result) {
                count++;
                var game = result[i];
                $("#gameList").append(listTemplate(game));
            }
            $("#gameAmount").text(count);
        };

        var sendPlayerDetail = function () {

            var gameId = $("#detailsGameId").text();
            var playerName = $("#playerName").val();
            $("#playerName").empty();
            var msg = {
                ConnectType: "PlayerDetails",
                ConnectBy: "Player",
                gId: gameId,
                pName: playerName
            };
            connection.send(JSON.stringify(msg));
            $("#waitingGameId").text(gameId);
            $("#player").text(playerName);
            $.UIGoToArticle("#waitingGame");
        };

        var sendforCardsInfo = function () {
            var gameId = $("#detailsGameId").text();
            var playerName = $("#playerName").val();
            $("#playerName").empty();
            var msg = {
                ConnectType: "PlayerRefresh",
                ConnectBy: "Player",
                gId: gameId,
                pName: playerName
            };
            connection.send(JSON.stringify(msg));
        };

        var CardsList = function (result) {
            for (var i = 0; i < result.length; i++)
            {
                var cardUrl = $('<li class="special">');
                var img = $("<img>").attr("src", "http://localhost:8080/UnoGamePhase2/img/" + result[i].card);
                cardUrl.append(img);
                $("#handCards").append(cardUrl);
            }
            $.UIGoToArticle("#gameStart");
        };
    });
});