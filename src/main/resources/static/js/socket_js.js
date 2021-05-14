const url = 'http://texasholdemonlinedemo-env.eba-nugyxqgx.us-east-2.elasticbeanstalk.com';
let stompClient;
let GAME_ID;
let playerType;
let MY_BALANCE = getMyBalance();

function getMyCurrentHand() {
    $.ajax({
        url: url + "/game/myHand",
        type: 'GET',
        dataType: "json",
        contentType: "application/json",
        success: function (currentHand) {
            console.log("Current hand: " + currentHand);
            if (currentHand[0] != null) {
                for (let i = 0; i < currentHand.length; i++) {
                    if (currentHand[i] == null) {
                        break;
                    }
                    document.getElementById("hand_" + i.toString()).src="/images/" + currentHand[i] + ".jpg";
                }
            }
        },
        error: function (error) {
            console.log(error);
        }
    });
}

function getMyBalance() {
    $.ajax({
        url: url + "/game/myBalance",
        type: 'GET',
        contentType: "application/json",
        success: function (formattedBalance) {
            console.log("Getting balance...")
            console.log("My balance: " + formattedBalance);
            $("#myBalance").text("My Balance: " + formattedBalance);
            MY_BALANCE = formattedBalance;
        },
        error: function (error) {
            console.log(error);
        }
    });
}

function getAmountNeededToMeetCheck() {
    $.ajax({
        url: url + "/game/amountNeededToMeetCheck",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "gameId": GAME_ID
        }),
        success: function (amountNeededToMeetCheck) {
            console.log("amountNeededToMeetCheck: " + amountNeededToMeetCheck);
            $("#check").text("Check ($" + amountNeededToMeetCheck.toString() + ")");
        },
        error: function (error) {
            console.log(error);
        }
    });
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
            WAITING_FOR_GAME_TO_START = true;
            GAME_ID = data.gameId;
            reset();
            connectToSocket(data.gameId);
            populateLobbyList(data);
            alert("Your created a game. Game id is: " + data.gameId);
            gameOn = true;
        },
        error: function (error) {
            console.log(error);
        }
    });
}

function startGame() {
    WAITING_FOR_GAME_TO_START = false;
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
            alert("Couldn't start match, waiting for more players.");
            console.log(error);
        }
    });
}

function leaveLobby() {
    WAITING_FOR_GAME_TO_START = false;
    stompClient.disconnect(function() {alert("You have left the lobby.")});
    $.ajax({
        url: url + "/game/disconnect",
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
            $("#pot").text("");
            $("#checkAmount").text("");
            document.getElementById("findGameInterface").classList.remove("hidden");
            document.getElementById("startGame").classList.add("hidden");
            document.getElementById("leaveLobby").classList.add("hidden");
            document.getElementById("gameInterface_1").classList.add("hidden");
            document.getElementById("gameInterface_2").classList.add("hidden");
            document.getElementById("currentTurn").innerHTML = "";
            document.getElementById("curGameId").innerHTML = "";
            document.getElementById("currentLobby").innerHTML = "";
            document.getElementById("winner").innerHTML = "";
            for (let i = 0; i < 2; i++) {
                $("#hand_" + i.toString()).text("");
            }
        },
        error: function (error) {
            console.log(error);
            alert("You must finish the current game before leaving the lobby.");
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
            WAITING_FOR_GAME_TO_START = true;
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
            WAITING_FOR_GAME_TO_START = true;
            GAME_ID = data.gameId;
            reset();
            populateLobbyList(data);
            connectToSocket(data.gameId);
            gameOn = true;
        },
        error: function (error) {
            alert("Could not find a game with Game ID '" + GAME_ID + "'. Try creating your own!")
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
    $("#pot").text("");
    $("#checkAmount").text("");
    document.getElementById("findGameInterface").classList.add("hidden");
    document.getElementById("startGame").classList.remove("hidden");
    document.getElementById("leaveLobby").classList.remove("hidden");
    document.getElementById("gameInterface_1").classList.add("hidden");
    document.getElementById("gameInterface_2").classList.add("hidden");
    document.getElementById("currentTurn").innerHTML = "";
    document.getElementById("curGameId").innerHTML = "Game ID: " + GAME_ID + "";
    document.getElementById("currentLobby").innerHTML = "";
    document.getElementById("winner").innerHTML = "";
    for (let i = 0; i < 2; i++) {
        $("#hand_" + i.toString()).text("");
    }
}