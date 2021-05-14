// var turns = [["#", "#", "#"], ["#", "#", "#"], ["#", "#", "#"]];
var turn = "";
var gameOn = false;
var WAITING_FOR_GAME_TO_START = true;

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

    for (let i = 0; i < board.length; i++) {
        if (board[i] == null) {
            document.getElementById("board_" + i.toString()).src="";
        } else {
            document.getElementById("board_" + i.toString()).src="/images/" + board[i] + ".jpg";
        }
    }

    if (data.resetLobby) {
        reset();
    }

    console.log("board: '" + board + "'");

    if (data.gameStatus == "IN_PROGRESS") {
        getMyCurrentHand();
        getAmountNeededToMeetCheck();
        WAITING_FOR_GAME_TO_START = false;
        $("#pot").text("Pot: $" + data.pot);
        document.getElementById("startGame").classList.add("hidden");
        document.getElementById("leaveLobby").classList.add("hidden");
        document.getElementById("gameInterface_1").classList.remove("hidden");
        document.getElementById("gameInterface_2").classList.remove("hidden");
        document.getElementById("moves").classList.remove("hidden");
        if (data.currentTurn.username != null) {
            document.getElementById("currentTurn").innerHTML = "Current turn: '" + data.currentTurn.username + "'";
        } 
        if (data.checkAmount != 0) {
            $("#bet").text("Raise");
        } else {
            $("#bet").text("Bet");
        }
    } else {
        if (WAITING_FOR_GAME_TO_START && !data.resetLobby) {
            if (data.justLeftLobby != null) {
                alert("Player '" + data.justLeftLobby.username + "' has left the lobby.");
            } else {
                for (i = 0; i < data.players.length; i++) {
                    if (data.players[i+1] == null) {
                        break;
                    }
                }
                alert("Player '" + data.players[i].username + "' has joined the lobby.");
            }
            populateLobbyList(data);
        }

        if (data.gameStatus == "NEW") {
            document.getElementById("startGame").classList.remove("hidden");
            document.getElementById("leaveLobby").classList.remove("hidden");
            document.getElementById("gameInterface_1").classList.add("hidden");
            document.getElementById("gameInterface_2").classList.add("hidden");
            let currentLobbyString = "<p>Currently lobby:</p>";
            let i = 0;
            for (i = 0; i < data.players.length; i++) {
                currentLobbyString += "<p>" + data.players[i].username + "</p>";
                if (data.players[i+1] == null) {
                    break;
                }
            }
            document.getElementById("currentLobby").innerHTML = currentLobbyString;
        } else if (data.gameStatus == "FINISHED" && !WAITING_FOR_GAME_TO_START) {
            WAITING_FOR_GAME_TO_START = true;
            document.getElementById("startGame").classList.remove("hidden");
            document.getElementById("leaveLobby").classList.remove("hidden");
            document.getElementById("moves").classList.add("hidden");
            $("#currentTurn").text("");

            if (data.winners[0] != null) {
                if (data.winners[1] == null) {
                    $("#winner").text(data.winners[0].username + " wins (" + data.holdemWinString + ")!");
                    alert("Winner is " + data.winners[0].username);
                } else {
                    var tieString = "Tie between: " + data.winners[0].username;
                    for (let j = 1; j < data.winnerCount; j++) {
                        if (j == data.winnerCount-1) {
                            if (j != 1) { tieString += ","}
                            tieString += " and " + data.winners[j].username + " (" + data.holdemWinString + ").";
                        } else {
                            tieString += ", " + data.winners[j].username;
                        }
                    }
                    alert(tieString);
                    tieString +=  "</br>Splitting the pot between them.";
                    document.getElementById("winner").innerHTML = tieString;
                }
            }
        }
    }

    getMyBalance();

}
