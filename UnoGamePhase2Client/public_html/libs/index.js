var connection = null;
$(function () {

    $("#create").on("singletap", function () {
        var gameTitle = $("#gameTitle").val();
        var noPlayers = $("#maxplayers").val();
        $("#gameTitle").empty();
        $("#capacity").empty();
        console.log(">>> Connecting");
        connection = new WebSocket("ws://localhost:8080/UnoGamePhase2/game/table");
//-------------------------------------------------------------------//
        connection.onopen = function () {
            console.log(">>> connected");
        };
//-------------------------------------------------------------------//
        connection.onmessage = function (evt) {

            if (evt.data === "Table") {
                console.info("connect ok is comming");
                var msg = {
                    ConnectType: "GameDetails",
                    ConnectBy: "Creator",
                    gname: gameTitle,
                    NoOfPlayer: noPlayers
                };
                connection.send(JSON.stringify(msg));
            } else {
                jsondata = JSON.parse(evt.data);
                console.info(jsondata.ConnectType);

                if (jsondata.ConnectType === "WaitForPlayers") {   //Switch case can be used

                    $("#waitGameId").text(jsondata.gameId);
                    $("#waitGameTitle").text(jsondata.gameTitle);
                    $("#waitGameCapacity").text(jsondata.maxplayers);
                    $.UIGoToArticle("#waitGame");
                    getPlayerJoined();

                } else if (jsondata.ConnectType === "PlayerAmount") {
                    $("#playerAmount").text(jsondata.amount);
                    selfStart();
                } else if (jsondata.ConnectType === "GamePlay") {
                    console.log("Connect Type is GamePlay");
                    discardPileCards(jsondata.Cards);
                }

            }
            console.log(">> data came");
        };

        var getPlayerJoined = function () {

            var msg = {
                ConnectType: "getPlayerJoined",
                ConnectBy: "Creator",
                gId: $("#waitGameId").text()

            };
            connection.send(JSON.stringify(msg));
            console.log(">>> getPlayerJoined msg send");
        };


        var selfStart = function () {
            var PA = $("#playerAmount").text();
            var WGC = $("#waitGameCapacity").text();
            console.log("playerAmount:  " + PA);
            console.log("waitGameCapacity:  " + WGC);

            if ($("#playerAmount").text() >= $("#waitGameCapacity").text()) {

                var gameId = $("#waitGameId").text();
                var msg = {
                    ConnectType: "StartGame",
                    ConnectBy: "Creator",
                    gId: gameId
                };
                connection.send(JSON.stringify(msg));
                console.log(">>> game Start msg send");

            } else {
                console.log(">> Not triggered");
            }
        };

        var discardPileCards = function (result) {
            $("#discrad").attr("src", "http://localhost:8080/UnoGamePhase2/img/" + result[0].img2);
            var playerTemplate = Handlebars.compile($("#playerTemplate").html());
            for (var i = 1; i < result.length; i++) {
                $("#playerList").append(playerTemplate(result[i]));
            }
            $.UIGoToArticle("#gameTable");
        };

    });

});


