const url = 'http://localhost:8080';
let stompClient;
let gameId;
let playerType;

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
        })
    })
}

function createGame() {
    // let username = document.getElementById("username").value;
    // if (username == null || username === '') {
    //     alert("Please enter username");
    // } else {
    $.ajax({
        url: url + "/game/start",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        // data: JSON.stringify({
        //     "username": username
        // }),
        success: function (data) {
            waitingForPlayer = true;
            gameId = data.gameId;
            playerType = 'X';
            reset();
            connectToSocket(gameId);
            alert("Your created a game. Game id is: " + data.gameId);
            document.getElementById("curGameId").innerHTML = "Game ID: '" + gameId + "'";
            document.getElementById("oponentUsername").innerHTML = "";
            document.getElementById("winner").innerHTML = "";
            gameOn = true;
        },
        error: function (error) {
            console.log(error);
        }
    })
    // }
}


function connectToRandom() {
    // let username = document.getElementById("username").value;
    // if (username == null || username === '') {
    //     alert("Please enter username");
    // } else {
    $.ajax({
        url: url + "/game/connect/random",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        // data: JSON.stringify({
        //     "username": username
        // }),
        success: function (data) {
            waitingForPlayer = false;
            gameId = data.gameId;
            playerType = 'O';
            reset();
            connectToSocket(gameId);
            document.getElementById("curGameId").innerHTML = "Game ID: '" + gameId + "'";
            document.getElementById("oponentUsername").innerHTML = "Currently playing with player '" + data.player1.username + "'";
            document.getElementById("winner").innerHTML = "";
            gameOn = true;
            alert("You've joined a game with player '" + data.player1.username + "'");
        },
        error: function (error) {
            console.log(error);
        }
    })
    // }
}

function connectToSpecificGame() {
    let username = document.getElementById("username").value;
    if (username == null || username === '') {
        alert("Please enter username");
    } else {
        gameId = document.getElementById("gameId").value;
        if (gameId == null || gameId === '') {
            alert("Please enter game id");
        }
        $.ajax({
            url: url + "/game/connect",
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "player": {
                    "username": username
                },
                "gameId": gameId
            }),
            success: function (data) {
                waitingForPlayer = false;
                gameId = data.gameId;
                playerType = 'O';
                reset();
                connectToSocket(gameId);
                document.getElementById("curGameId").innerHTML = "Game ID: '" + gameId + "'";
                document.getElementById("oponentUsername").innerHTML = "Currently playing with player '" + data.player1.username + "'";
                document.getElementById("winner").innerHTML = "";
                gameOn = true;
                alert("You've joined a game with player '" + data.player1.username + "'");
            },
            error: function (error) {
                console.log(error);
            }
        })
    }
}