package com.example.gra_sudoku

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        // Przywróć zapisane ustawienia
        val savedBoardSize = prefs.getInt("board_size", 9)
        val savedDifficulty = prefs.getString("difficulty", "medium") ?: "medium"
        val savedVolume = prefs.getInt("volume", 100)

        findViewById<SeekBar>(R.id.seekBarVolume).progress = savedVolume

        when (savedBoardSize) {
            9 -> findViewById<RadioButton>(R.id.rb9x9).isChecked = true
            6 -> findViewById<RadioButton>(R.id.rb6x6).isChecked = true
            4 -> findViewById<RadioButton>(R.id.rb4x4).isChecked = true
        }

        when (savedDifficulty) {
            "easy" -> findViewById<RadioButton>(R.id.rbEasy).isChecked = true
            "medium" -> findViewById<RadioButton>(R.id.rbMedium).isChecked = true
            "hard" -> findViewById<RadioButton>(R.id.rbHard).isChecked = true
        }

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            // Zapisz wielkość planszy
            val boardSize = when {
                findViewById<RadioButton>(R.id.rb9x9).isChecked -> 9
                findViewById<RadioButton>(R.id.rb6x6).isChecked -> 6
                else -> 4
            }
            editor.putInt("board_size", boardSize)

            // Zapisz trudność
            val difficulty = when {
                findViewById<RadioButton>(R.id.rbEasy).isChecked -> "easy"
                findViewById<RadioButton>(R.id.rbMedium).isChecked -> "medium"
                else -> "hard"
            }
            editor.putString("difficulty", difficulty)

            // Zapisz głośność
            val volume = findViewById<SeekBar>(R.id.seekBarVolume).progress
            editor.putInt("volume", volume)

            editor.apply()
            Toast.makeText(this, "Ustawienia zapisane!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}