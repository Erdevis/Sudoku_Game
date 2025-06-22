package com.example.gra_sudoku

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity(), SudokuBoardView.OnCellSelectedListener {

    private lateinit var sudokuBoard: SudokuBoardView
    private lateinit var tvDifficulty: TextView
    private lateinit var numberPad: LinearLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)


        // Inicjalizacja widoków
        sudokuBoard = findViewById(R.id.sudokuBoard)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        numberPad = findViewById(R.id.numberPad)

        // Ustaw naszą aktywność jako słuchacza zdarzeń
        sudokuBoard.cellSelectedListener = this

        // Pobierz ustawienia
        val prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE)
        val boardSize = prefs.getInt("board_size", 9)
        val difficulty = prefs.getString("difficulty", "medium") ?: "medium"

        tvDifficulty.text = "Poziom trudności: ${difficulty.capitalize()}"
        sudokuBoard.initializeBoard(boardSize, difficulty)

        // Inicjalizacja klawiatury numerycznej
        initNumberPad()

        // Obsługa przycisków
        findViewById<Button>(R.id.btnCheck).setOnClickListener {
            sudokuBoard.checkSolution()
            hideNumberPad()
        }

        findViewById<Button>(R.id.btnNewGame).setOnClickListener {
            sudokuBoard.generateNewBoard(difficulty)
            hideNumberPad()
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        Log.d("GameActivity", "Activity created")

    }



    override fun onCellSelected(row: Int, col: Int) {
        Log.d("GameActivity", "Cell selected: $row,$col")
        showNumberPad()
    }

    private fun initNumberPad() {
        Log.d("GameActivity", "Initializing number pad")

        // Obsługa przycisków numerycznych
        for (i in 1..9) {
            val buttonId = resources.getIdentifier("btnNumber$i", "id", packageName)
            val button = findViewById<Button>(buttonId)

            button?.setOnClickListener {
                Log.d("GameActivity", "Number $i selected")
                sudokuBoard.setCellValue(i)
                hideNumberPad()
            }
        }

        // Przycisk czyszczenia
        findViewById<Button>(R.id.btnClear)?.setOnClickListener {
            Log.d("GameActivity", "Clear button clicked")
            sudokuBoard.setCellValue(0)
            hideNumberPad()
        }
    }

    private fun showNumberPad() {
        Log.d("GameActivity", "Showing number pad")
        numberPad.visibility = View.VISIBLE
    }

    private fun hideNumberPad() {
        Log.d("GameActivity", "Hiding number pad")
        numberPad.visibility = View.GONE
    }

    // Ukryj klawiaturę po naciśnięciu przycisku Wstecz
    override fun onBackPressed() {
        if (numberPad.visibility == View.VISIBLE) {
            hideNumberPad()
        } else {
            super.onBackPressed()
        }
    }
}