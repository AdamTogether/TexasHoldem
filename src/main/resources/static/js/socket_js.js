const url = 'http://localhost:8080';
let stompClient;
let GAME_ID;
let playerType;

function getMyCurrentHand() {
        $.ajax({
        url: url + "/game/myHand",
        type: 'GET',
        dataType: "json",
        contentType: "application/json",
        success: function (currentHand) {
            console.log("Current hand: " + currentHand);
            for (let i = 0; i < currentHand.length; i++) {
                $("#hand_" + i.toString()).text(currentHand[i]);
            }
        },
        error: function (error) {
            console.log(error);
        }
    })
}

function connectToSocket(gameId) {
    console.log("connecting to the game");
    let socket = new SockJS(url + "/gameplay");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log("connected to the frame: " + frame);
        stompClient.subscribe("/topic/game-progress/" + gameId, function (response) {
            let data = JSON.parse(response.body);
            console.log(data);
            displayResponse(data);
            populateLobbyList(data);
        })
    });
}

function createGame() {
    $.ajax({
        url: url + "/game/create",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            waitingForPlayer = true;
            GAME_ID = data.gameId;
            reset();
            connectToSocket(data.gameId);
            alert("Your created a game. Game id is: " + data.gameId);
            gameOn = true;
        },
        error: function (error) {
            console.log(error);
        }
    });
}

function startGame() {
    $.ajax({
        url: url + "/game/startGame",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": null,
            "move": null,
            "betAmount": null,
            "gameId": GAME_ID
        }),
        success: function (data) {
            console.log(data);
        },
        error: function (error) {
            alert("Couldn't start match.");
            console.log(error);
        }
    });
}

function connectToRandom() {
    $.ajax({
        url: url + "/game/connect/random",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            alert("You've joined a game hosted by player '" + data.players[0].username + "'.");
            waitingForPlayer = true;
            GAME_ID = data.gameId;
            reset();
            populateLobbyList(data);
            connectToSocket(data.gameId);
            gameOn = true;
        },
        error: function (error) {
            alert("Couldn't find any games, try creating your own!");
            console.log(error);
        }
    });
}

function connectToSpecificGame() {
    GAME_ID = document.getElementById("gameId").value;

    if (GAME_ID == null || GAME_ID === '') {
        alert("Please enter game id");
    }

    $.ajax({
        url: url + "/game/connect",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "gameId": GAME_ID
        }),
        success: function (data) {
            alert("You've joined a game hosted by player '" + data.players[0].username + "'.");
            waitingForPlayer = true;
            gameId = data.gameId;
            reset();
            populateLobbyList(data);
            connectToSocket(data.gameId);
            gameOn = true;
        },
        error: function (error) {
            console.log(error);
        }
    })
}

function populateLobbyList(data) {    
    let currentLobbyString = "<p>Current lobby:</p>";
    let i = 0;
    for (i = 0; i < data.players.length; i++) {
        currentLobbyString += "<p>" + data.players[i].username;
        for (let j = 0; j < data.foldedPlayers.length; j++) {
            if (data.foldedPlayers[j] == null) {
                break;
            } else if (data.players[i].username == data.foldedPlayers[j].username) {
                currentLobbyString += " (FOLDED)";
            }
        }
        currentLobbyString += "</p>";
        if (data.players[i+1] == null) {
            break;
        }
    }
    document.getElementById("currentLobby").innerHTML = currentLobbyString;
}

function reset() {
    // turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
    $(".tic").text("#");
    $("#pot").text("");
    $("#checkAmount").text("");
    document.getElementById("startGame").classList.remove("hidden");
    document.getElementById("currentTurn").innerHTML = "";
    document.getElementById("curGameId").innerHTML = "Game ID: '" + GAME_ID + "'";
    document.getElementById("currentLobby").innerHTML = "";
    document.getElementById("winner").innerHTML = "";
    for (let i = 0; i < 2; i++) {
        $("#hand_" + i.toString()).text("");
    }
}