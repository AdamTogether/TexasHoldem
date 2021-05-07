var turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
var turn = "";
var gameOn = false;
var waitingForPlayer = true;

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
            "gameId": gameId
        }),
        success: function (data) {
            // document.getElementById("currentTurn").innerHTML = "Current turn: '" + data.currentTurn.username + "'";
            console.log(data);
        },
        error: function (error) {
            alert("Couldn't start match.");
            console.log(error);
        }
    });
}

function check() {
    makeAMove("CHECK", 0);
}

function playerTurn(turn, id) {
    if (gameOn) {
        var spotTaken = $("#" + id).text();
        if (spotTaken === "#") {
            makeAMove(playerType, id.split("_")[0], id.split("_")[1]);
        }
    }
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
            "gameId": gameId
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

    if (data.gameStatus == "IN_PROGRESS") {
        document.getElementById("startGame").classList.add("hidden");
        document.getElementById("currentTurn").innerHTML = "Current turn: '" + data.currentTurn.username + "'";
    } else if (data.gameStatus == "NEW") {
        document.getElementById("startGame").classList.remove("hidden");
    }

    if (waitingForPlayer) {
        document.getElementById("oponentUsername").innerHTML = "Currently playing with player '" + data.players[1].username + "'";
        alert("Player '" + data.players[1].username + "' has joined the game. Make the first move!");
        waitingForPlayer = false;
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
    // if (data.winner != null) {
    //     document.getElementById("winner").innerHTML = data.winner + " won!";
    //     alert("Winner is " + data.winner);
    //     gameOn = false;
    // } else {
    //     gameOn = true;
    // }
}

$(".tic").click(function () {
    var slot = $(this).attr('id');
    playerTurn(turn, slot);
});

function reset() {
    turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
    $(".tic").text("#");
}

$("#reset").click(function () {
    reset();
});