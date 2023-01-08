package chess

import kotlin.math.*

const val SIZE = 8

val board = List(SIZE) { MutableList(SIZE) { PIECE.EMPTY } }
var latestMoveBlack = ""
var latestMoveWhite = ""
var whitePawns = 8
var blackPawns = 8
var rankReachedWhite = false
var rankReachedBlack = false
var stalemate = false

fun String.check(): String? = if (matches(Regex("[a-h][1-8][a-h][1-8]"))) this else null
operator fun List<MutableList<PIECE>>.set(column: Char, row: Int, piece: PIECE) {
    board[SIZE - row][column - 'a'] = piece
}

operator fun List<MutableList<PIECE>>.get(column: Char, row: Int) = board[SIZE - row][column - 'a']

enum class PIECE { EMPTY, WHITE, BLACK }

fun main() {
    val players = List(2) {
        object {
            var name: String = ""
            var colour: PIECE = PIECE.EMPTY
        }
    }
    ('a'..'h').forEach { files ->
        board[files, 2] = PIECE.WHITE
        board[files, 7] = PIECE.BLACK
    }
    players[0].colour = PIECE.WHITE
    players[1].colour = PIECE.BLACK
    println("Pawns-Only Chess")
    println("First Player's name:")
    players[0].name = readln()
    println("Second Player's name:")
    players[1].name = readln()
    printBoard()
    var turn = 0
    while (true) {
        println(players[turn].name + "'s turn:")
        val move = readln()
        when {
            move == "exit" -> break
            move.check() == null -> println("Invalid Input")
            else -> {
                val colourPiece = players[turn].colour
                val colour = when (colourPiece) {
                    PIECE.WHITE -> "white"
                    PIECE.BLACK -> "black"
                    else -> ' '
                }
                val from = move.substring(0, 2)
                val to = move.substring(2)
                if (!checkPosition(from, colourPiece)) {
                    println("No $colour pawn at $from")
                    continue
                }
                if (!checkMove(from, to, colourPiece)) {
                    println("Invalid Input")
                    continue
                }
                move(from, to, colourPiece)
                turn = 1 - turn
            }
        }
        printBoard()
        for (i in 'a'..'h') {
            if (board[i, 8] == PIECE.WHITE) rankReachedWhite = true
            else if (board[i, 1] == PIECE.BLACK) rankReachedBlack = true
        }
        stalemate = checkMoves(PIECE.WHITE) || checkMoves(PIECE.BLACK)
        if (whitePawns == 0 || blackPawns == 0 || rankReachedWhite || rankReachedBlack) break
        if (stalemate) break
    }
    var winner = ""
    if (whitePawns == 0 || rankReachedBlack) winner = "Black"
    else if (blackPawns == 0 || rankReachedWhite) winner = "White"
    if (winner != "") println("$winner Wins!")
    else if (stalemate) println("Stalemate!")
    println("Bye!")
}

fun printBoard() {
    val game = """
          +---+---+---+---+---+---+---+---+
        8 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        7 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        6 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        5 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        4 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        3 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        2 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
        1 | %c | %c | %c | %c | %c | %c | %c | %c |
          +---+---+---+---+---+---+---+---+
            a   b   c   d   e   f   g   h""".trimIndent().format(
        *board.flatten()
            .map { field ->
                when (field) {
                    PIECE.EMPTY -> ' '
                    PIECE.WHITE -> 'W'
                    PIECE.BLACK -> 'B'
                }
            }.toTypedArray()
    )
    println(game)
}

fun checkPosition(position: String, colour: PIECE): Boolean {
    val column = position[0]
    val row = position[1].digitToInt()
    return board[column, row] == colour
}

fun move(from: String, to: String, colour: PIECE) {
    val currentRow = from[1].digitToInt()
    val currentColumn = from[0]
    val targetRow = to[1].digitToInt()
    val targetColumn = to[0]
    if (board[targetColumn, targetRow] != PIECE.EMPTY) {
        if (board[targetColumn, targetRow] == PIECE.WHITE) whitePawns -= 1
        else blackPawns -= 1
    }
    if (checkEnPassant(to, colour) && from[0] != to[0]) {
        if (colour == PIECE.WHITE) board[targetColumn, targetRow - 1] = PIECE.EMPTY
        else board[targetColumn, targetRow + 1] = PIECE.EMPTY
    }
    board[currentColumn, currentRow] = PIECE.EMPTY
    board[targetColumn, targetRow] = colour
    if (colour == PIECE.WHITE) {
        latestMoveWhite = to
    } else {
        latestMoveBlack = to
    }
}


fun checkMove(from: String, to: String, colour: PIECE): Boolean {
    val currentRow = from[1].digitToInt()
    val currentColumn = from[0]
    val targetRow = to[1].digitToInt()
    val targetColumn = to[0]
    if (currentColumn != targetColumn) return capturePossible(from, to, colour)
    return moveStraight(currentRow, targetColumn, targetRow, colour)
}

fun moveStraight(currentRow: Int, targetColumn: Char, targetRow: Int, colour: PIECE): Boolean {
    if (currentRow == 7 || currentRow == 2) {
        return if (colour == PIECE.WHITE)
            currentRow + 2 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY || currentRow + 1 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY
        else currentRow - 2 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY || currentRow - 1 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY
    }
    return if (colour == PIECE.WHITE)
        currentRow + 1 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY
    else currentRow - 1 == targetRow && board[targetColumn, targetRow] == PIECE.EMPTY
}

fun capturePossible(from: String, to: String, colour: PIECE): Boolean {
    val opponent = board[to[0], to[1].digitToInt()]
    if (opponent == colour) return false
    if (abs(from[0] - to[0]) > 1) return false
    return opponent != PIECE.EMPTY || checkEnPassant(to, colour)
}

fun checkEnPassant(to: String, colour: PIECE): Boolean {
    val opponent = if (colour == PIECE.WHITE) board[to[0], to[1].digitToInt() - 1]
    else board[to[0], to[1].digitToInt() + 1]
    if (opponent == colour) return false
    if (opponent == PIECE.EMPTY) return false
    return if (colour == PIECE.WHITE) {
        val latestColumn = latestMoveBlack[0]
        to[0] == latestColumn && abs(to[1].digitToInt() - latestMoveBlack[1].digitToInt()) == 1
    } else {
        val latestColumn = latestMoveWhite[0]
        to[0] == latestColumn && abs(to[1].digitToInt() - latestMoveWhite[1].digitToInt()) == 1
    }
}

fun checkMoves(colour: PIECE): Boolean {
    for (column in 'a'..'h') {
        for (row in 1 until SIZE) {
            val from = "$column$row"
            if (board[column, row] == colour) {
                for (toColumn in 'a'..'h') {
                    val to = "$toColumn${row + if (colour == PIECE.WHITE) 1 else -1}"
                    if (checkMove(from, to, colour)) {
                        return false
                    }
                }
            }
        }
    }
    return true
}