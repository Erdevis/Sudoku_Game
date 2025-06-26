package com.example.gra_sudoku

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity(), SudokuBoardView.OnCellSelectedListener {

    private lateinit var sudokuBoard: SudokuBoardView
    private lateinit var numberPad: LinearLayout
    private lateinit var numberButtons: List<Button>
    private lateinit var btnClear: Button
    private lateinit var btnCheck: Button
    private lateinit var btnNewGame: Button
    private lateinit var btnBack: Button
    private lateinit var tvDifficulty: TextView

    // nie trzymamy tych zmiennych jako pola – pobieramy je w onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 1) SharedPreferences
        val prefs = getSharedPreferences("GraSudokuPrefs", Context.MODE_PRIVATE)
        val boardSize  = prefs.getInt("BOARD_SIZE", 9)
        val difficulty = prefs.getString("DIFFICULTY", "medium")!!

        // 2) Widoki
        sudokuBoard  = findViewById(R.id.sudokuBoard)
        numberPad    = findViewById(R.id.numberPad)
        btnCheck     = findViewById(R.id.btnCheck)
        btnNewGame   = findViewById(R.id.btnNewGame)
        btnBack      = findViewById(R.id.btnBack)
        tvDifficulty = findViewById(R.id.tvDifficulty)

        numberButtons = listOf(
            findViewById(R.id.btnNumber1),
            findViewById(R.id.btnNumber2),
            findViewById(R.id.btnNumber3),
            findViewById(R.id.btnNumber4),
            findViewById(R.id.btnNumber5),
            findViewById(R.id.btnNumber6),
            findViewById(R.id.btnNumber7),
            findViewById(R.id.btnNumber8),
            findViewById(R.id.btnNumber9)
        )
        btnClear = findViewById(R.id.btnClear)

        // 3) Inicjuj widok: rozmiar i trudność
        sudokuBoard.cellSelectedListener = this
        tvDifficulty.text = "Poziom: ${difficulty.replaceFirstChar { it.uppercase() }}"
        sudokuBoard.initializeBoard(boardSize, difficulty)

        // 4) Number pad
        setupNumberPad()

        // 5) Dolny pasek
        btnCheck.setOnClickListener {
            sudokuBoard.checkSolution()
        }
        btnNewGame.setOnClickListener {
            sudokuBoard.initializeBoard(boardSize, difficulty)
            numberPad.visibility = View.GONE
        }
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onCellSelected(row: Int, col: Int) {
        setupNumberPad()
        numberPad.visibility = View.VISIBLE
    }

    private fun setupNumberPad() {
        val size = sudokuBoard.boardSize

        numberPad.visibility = View.GONE
        numberButtons.forEachIndexed { idx, btn ->
            if (idx < size) {
                btn.visibility = View.VISIBLE
                btn.setOnClickListener {
                    sudokuBoard.setCellValue(idx + 1)
                    numberPad.visibility = View.GONE
                }
            } else {
                btn.visibility = View.GONE
            }
        }
        btnClear.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                sudokuBoard.setCellValue(0)
                numberPad.visibility = View.GONE
            }
        }
    }
}
