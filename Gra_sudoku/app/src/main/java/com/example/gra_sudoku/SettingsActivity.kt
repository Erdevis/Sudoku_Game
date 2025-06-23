package com.example.gra_sudoku

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {

    private lateinit var rgBoardSize: RadioGroup
    private lateinit var rgDifficulty: RadioGroup
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        rgBoardSize   = findViewById(R.id.rgBoardSize)
        rgDifficulty  = findViewById(R.id.rgDifficulty)
        btnSave       = findViewById(R.id.btnSaveSettings)

        // Wczytaj poprzednie ustawienia (jeśli są)
        val prefs = getSharedPreferences("GraSudokuPrefs", Context.MODE_PRIVATE)
        when (prefs.getInt("BOARD_SIZE", 9)) {
            9 -> rgBoardSize.check(R.id.rb9x9)
            6 -> rgBoardSize.check(R.id.rb6x6)
            4 -> rgBoardSize.check(R.id.rb4x4)
        }
        when (prefs.getString("DIFFICULTY", "medium")) {
            "easy"   -> rgDifficulty.check(R.id.rbEasy)
            "medium" -> rgDifficulty.check(R.id.rbMedium)
            "hard"   -> rgDifficulty.check(R.id.rbHard)
        }

        btnSave.setOnClickListener {
            val editor = prefs.edit()
            // Board size
            val size = when (rgBoardSize.checkedRadioButtonId) {
                R.id.rb9x9 -> 9
                R.id.rb6x6 -> 6
                else       -> 4
            }
            editor.putInt("BOARD_SIZE", size)

            // Difficulty
            val diff = when (rgDifficulty.checkedRadioButtonId) {
                R.id.rbEasy   -> "easy"
                R.id.rbHard   -> "hard"
                else          -> "medium"
            }
            editor.putString("DIFFICULTY", diff)
            editor.apply()

            Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
