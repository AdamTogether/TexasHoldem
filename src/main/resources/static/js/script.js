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
    document.getElementById("moves").classList.add("hidden");
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

    if (data.resetLobby) {
        reset();
    }

    console.log("board: '" + board + "'");

    if (data.gameStatus == "IN_PROGRESS") {
        getAmountNeededToMeetCheck();
        waitingForPlayer = false;
        $("#pot").text("Pot: $" + data.pot);
        document.getElementById("startGame").classList.add("hidden");
        document.getElementById("gameInterface").classList.remove("hidden");
        document.getElementById("moves").classList.remove("hidden");
        if (data.currentTurn.username != null) {
            document.getElementById("currentTurn").innerHTML = "Current turn: '" + data.currentTurn.username + "'";
        } 
        if (data.checkAmount != 0) {
            $("#bet").text("Raise");
        } else {
            $("#bet").text("Bet");
        }
    } else if (data.gameStatus == "NEW") {
        document.getElementById("startGame").classList.remove("hidden");
        document.getElementById("gameInterface").classList.add("hidden");
        let currentLobbyString = "<p>Currently lobby:</p>";
        let i = 0;
        for (i = 0; i < data.players.length; i++) {
            currentLobbyString += "<p>" + data.players[i].username + "</p>";
            if (data.players[i+1] == null) {
                break;
            }
        }
        document.getElementById("currentLobby").innerHTML = currentLobbyString;
    } else if (data.gameStatus == "FINISHED") {
        document.getElementById("startGame").classList.remove("hidden");
        $("#currentTurn").text("GAME OVER");
        document.getElementById("moves").classList.add("hidden");

        if (data.winners[1] == null) {
            // document.getElementById("winner").innerHTML = data.winner.username + " won!";
            $("#winner").text(data.winners[0].username + " wins!");
            alert("Winner is " + data.winners[0].username);
            // gameOn = false;
        } else {
            var tieString = "Tie between: " + data.winners[0].username;
            for (let j = 1; j < data.winnerCount; j++) {
                if (j == data.winnerCount-1) {
                    if (j != 1) { tieString += ","}
                    tieString += " and " + data.winners[j].username + ".";
                } else {
                    tieString += ", " + data.winners[j].username;
                }
            }
            alert(tieString);
            tieString +=  "</br>Splitting the pot between them.";
            document.getElementById("winner").innerHTML = tieString;
            // $("#winner").text(tieString);
        }
    }

    getMyCurrentHand();

    if (waitingForPlayer) {
        for (i = 0; i < data.players.length; i++) {
            if (data.players[i+1] == null) {
                break;
            }
        }
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