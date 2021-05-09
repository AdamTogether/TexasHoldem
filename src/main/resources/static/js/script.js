// var turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
var turn = "";
var gameOn = false;
var waitingForPlayer = true;

function check() {
    makeAMove("CHECK", 0);
}

function bet() {
    makeAMove("BET", document.getElementById("betAmount").value);
}

function fold() {
    makeAMove("FOLD", 0);
}

function makeAMove(move, betAmount) {
    $.ajax({
        url: url + "/game/gameplay",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": null,
            "move": move,
            "betAmount": betAmount,
            "gameId": GAME_ID
        }),
        success: function (data) {
            console.log(data);
        },
        error: function (error) {
            alert("It's not your turn yet!");
            console.log(error);
        }
    });
}

function displayResponse(data) {
    let board = data.board;

    console.log("board: '" + board + "'");

    getMyCurrentHand();

    if (data.gameStatus == "IN_PROGRESS") {
        waitingForPlayer = false;
        $("#pot").text("Pot: $" + data.pot);
        document.getElementById("startGame").classList.add("hidden");
        document.getElementById("gameInterface").classList.remove("hidden");
        if (data.currentTurn.username != null) {
            document.getElementById("currentTurn").innerHTML = "Current turn: '" + data.currentTurn.username + "'";
        }
    } else if (data.gameStatus == "NEW") {
        document.getElementById("startGame").classList.remove("hidden");
        document.getElementById("gameInterface").classList.add("add");
        let currentLobbyString = "<p>Currently lobby:</p>";
        let i = 0;
        for (i = 0; i < data.players.length; i++) {
            currentLobbyString += "<p>" + data.players[i].username + "</p>";
            if (data.players[i+1] == null) {
                break;
            }
        }
        document.getElementById("currentLobby").innerHTML = currentLobbyString;
    }

    if (waitingForPlayer) {
        // let currentLobbyString = "<p>Currently lobby:</p>";
        // let i = 0;
        for (i = 0; i < data.players.length; i++) {
        //     currentLobbyString += "<p>" + data.players[i].username + "</p>";
            if (data.players[i+1] == null) {
                break;
            }
        }
        // document.getElementById("currentLobby").innerHTML = currentLobbyString;
        alert("Player '" + data.players[i].username + "' has joined the game.");
        populateLobbyList(data);
    }

    for (let i = 0; i < board.length; i++) {
        $("#board_" + i.toString()).text(board[i]);
    }

    // for (let i = 0; i < board.length; i++) {
    //     for (let j = 0; j < board[i].length; j++) {
    //         if (board[i][j] === 1) {
    //             turns[i][j] = 'X';
    //         } else if (board[i][j] === 2) {
    //             turns[i][j] = 'O';
    //         }
    //         let id = i + "_" + j;
    //         $("#" + id).text(turns[i][j]);
    //     }
    // }
    if (data.winner != null) {
        // document.getElementById("winner").innerHTML = data.winner.username + " won!";
        $("#winner").text(data.winner.username + " wins!");
        $("#currentTurn").text("");
        alert("Winner is " + data.winner.username);
        // gameOn = false;
    }
    // else {
    //     gameOn = true;
    // }
}

// function reset() {
//     // turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
//     $(".tic").text("#");
//     document.getElementById("startGame").classList.remove("hidden");
//     document.getElementById("currentTurn").innerHTML = "";
//     document.getElementById("curGameId").innerHTML = "Game ID: '" + GAME_ID + "'";
//     document.getElementById("currentLobby").innerHTML = "";
//     document.getElementById("winner").innerHTML = "";
//     for (let i = 0; i < currentHand.length; i++) {
//         $("#hand_" + i.toString()).text("");
//     }
// }

// function playerTurn(turn, id) {
//     if (gameOn) {
//         var spotTaken = $("#" + id).text();
//         if (spotTaken === "#") {
//             makeAMove(playerType, id.split("_")[0], id.split("_")[1]);
//         }
//     }
// }

// $(".tic").click(function () {
//     var slot = $(this).attr('id');
//     playerTurn(turn, slot);
// });

// $("#reset").click(function () {
//     reset();
// });