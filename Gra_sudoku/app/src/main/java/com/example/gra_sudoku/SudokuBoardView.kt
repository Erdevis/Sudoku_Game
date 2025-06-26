package com.example.gra_sudoku

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.random.Random
import kotlin.math.min
import kotlin.math.sqrt

class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SudokuBoardView"
    }

    // Interfejs do komunikacji z aktywnością
    interface OnCellSelectedListener {
        fun onCellSelected(row: Int, col: Int)
    }

    var cellSelectedListener: OnCellSelectedListener? = null

    private var cellSize = 0F
    private var selectedRow = -1
    private var selectedCol = -1
    var boardSize: Int = 9
        private set
    private var blockRows: Int = 3
    private var blockCols: Int = 3



    private lateinit var board: Array<IntArray>
    private lateinit var solution: Array<IntArray>
    private lateinit var originalBoard: Array<IntArray>

    private val thickLinePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4F
    }
    private val thinLinePaint = Paint().apply {
        color = Color.parseColor("#30000000") // Półprzezroczysta czerń
        style = Paint.Style.STROKE
        strokeWidth = 1.5F  // Zwiększona grubość
    }
    private val selectedCellPaint = Paint().apply {
        color = Color.parseColor("#6AB7FD")
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val fixedTextPaint = Paint().apply {
        color = Color.BLUE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }
    private val errorTextPaint = Paint().apply {
        color = Color.RED
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    init {
        // Zainicjuj domyślną planszę
        initializeBoard(9, "medium")
        isFocusable = true
        isFocusableInTouchMode = true
    }



    fun initializeBoard(size: Int, difficulty: String) {
        boardSize = size

        when (boardSize) {
            4 -> { blockRows = 2; blockCols = 2 }
            6 -> { blockRows = 2; blockCols = 3 }
            9 -> { blockRows = 3; blockCols = 3 }
            else -> {
                val b = sqrt(boardSize.toDouble()).toInt()
                blockRows = b; blockCols = b
            }
        }

        generateNewBoard(difficulty)
    }

    fun generateNewBoard(difficulty: String) {
        generatePuzzle()
        prepareBoard(difficulty)
        invalidate()
    }

    private fun generatePuzzle() {
        val side = boardSize
        // 1) pattern dla prostokątnych bloków
        fun pattern(r: Int, c: Int) =
            (blockCols * (r % blockRows) + r / blockRows + c) % side
        val nums = (1..side).shuffled()

        // 2) wypełniamy board
        board = Array(side) { r ->
            IntArray(side) { c -> nums[pattern(r, c)] }
        }

        // 3) liczba grup (band) i stacków
        val rowBands = side / blockRows
        val colStacks = side / blockCols

        // 4) przetasowania w bandach/stackach i między nimi
        repeat(20) {
            // swap wierszy w bandzie
            val b = Random.nextInt(rowBands)
            val r1 = b * blockRows + Random.nextInt(blockRows)
            val r2 = b * blockRows + Random.nextInt(blockRows)
            board.swapRows(r1, r2)

            // swap kolumn w stacku
            val s = Random.nextInt(colStacks)
            val c1 = s * blockCols + Random.nextInt(blockCols)
            val c2 = s * blockCols + Random.nextInt(blockCols)
            board.swapCols(c1, c2)

            // swap całych band
            val b1 = Random.nextInt(rowBands)
            val b2 = Random.nextInt(rowBands)
            for (i in 0 until blockRows) board.swapRows(b1*blockRows + i, b2*blockRows + i)

            // swap całych stacków
            val s1 = Random.nextInt(colStacks)
            val s2 = Random.nextInt(colStacks)
            for (i in 0 until blockCols) board.swapCols(s1*blockCols + i, s2*blockCols + i)
        }

        // 5) zapis rozwiązania
        solution = board.map { it.copyOf() }.toTypedArray()
    }

    // Zamienia dwie losowe wiersze wewnątrz tego samego bandu
    private fun swapRowsRandomlyInBand(base: Int) {
        val band = Random.nextInt(base)
        val r1 = band * base + Random.nextInt(base)
        val r2 = band * base + Random.nextInt(base)
        board.swapRows(r1, r2)
    }

    private fun swapColsRandomlyInStack(base: Int) {
        val stack = Random.nextInt(base)
        val c1 = stack * base + Random.nextInt(base)
        val c2 = stack * base + Random.nextInt(base)
        board.swapCols(c1, c2)
    }

    // Zamienia dwa całe bandy wierszy (każdy po `base` wierszy)
    private fun swapRowGroupsRandom(base: Int) {
        val b1 = Random.nextInt(base)
        val b2 = Random.nextInt(base)
        for (i in 0 until base) board.swapRows(b1*base + i, b2*base + i)
    }

    // Zamienia dwa całe stacki kolumn
    private fun swapColGroupsRandom(base: Int) {
        val s1 = Random.nextInt(base)
        val s2 = Random.nextInt(base)
        for (i in 0 until base) board.swapCols(s1*base + i, s2*base + i)
    }

    // Rozszerzenia tablicy
    private fun Array<IntArray>.swapRows(r1: Int, r2: Int) {
        val tmp = this[r1]; this[r1] = this[r2]; this[r2] = tmp
    }
    private fun Array<IntArray>.swapCols(c1: Int, c2: Int) {
        for (r in indices) {
            val tmp = this[r][c1]; this[r][c1] = this[r][c2]; this[r][c2] = tmp
        }
    }



    private fun prepareBoard(difficulty: String) {
        // określamy, ile komórek usunąć
        val cellsToRemove = when (difficulty) {
            "easy" -> boardSize * boardSize / 2
            "medium" -> boardSize * boardSize * 2 / 3
            else -> boardSize * boardSize * 3 / 4
        }

        // tu już mamy w `board` wstępnie wygenerowaną pełną planszę (wygaszamy wartości)
        var removed = 0
        while (removed < cellsToRemove) {
            val row = Random.nextInt(boardSize)
            val col = Random.nextInt(boardSize)
            if (board[row][col] != 0) {
                board[row][col] = 0
                removed++
            }
        }
        Log.d(TAG, "Usunięto $removed komórek. Edytowalnych: $cellsToRemove")

        // dopiero teraz zapamiętujemy, które pola są PUSTE (czyli edytowalne)
        originalBoard = Array(boardSize) { IntArray(boardSize) }
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                originalBoard[i][j] = board[i][j]
            }
        }
    }

    fun checkSolution() {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] != solution[i][j]) {
                    selectedRow = i
                    selectedCol = j
                    invalidate()
                    Toast.makeText(context, "Błędne rozwiązanie!", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
        Toast.makeText(context, "🎉 Gratulacje! Rozwiązanie poprawne!", Toast.LENGTH_LONG).show()
        // zablokuj dalsze zmiany
        originalBoard = Array(boardSize) { IntArray(boardSize) }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        cellSize = size / boardSize.toFloat()

        // Skalowanie grubości linii
        val scale = resources.displayMetrics.density
        thickLinePaint.strokeWidth = 4F * scale
        thinLinePaint.strokeWidth = 1.5F * scale

        textPaint.textSize = cellSize * 0.6F
        fixedTextPaint.textSize = cellSize * 0.6F
        errorTextPaint.textSize = cellSize * 0.6F
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawNumbers(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        // Podświetlenie wybranej komórki
        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(
                selectedCol * cellSize,
                selectedRow * cellSize,
                (selectedCol + 1) * cellSize,
                (selectedRow + 1) * cellSize,
                selectedCellPaint
            )
        }

        // Najpierw rysuj wszystkie cienkie linie
        for (i in 0..boardSize) {
            // Poziome
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, thinLinePaint)
            // Pionowe
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), thinLinePaint)
        }

        // Potem rysuj grube linie bloków (na wierzchu)
        // Poziome granice bloków
        for (i in 0..boardSize step blockRows) {
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, thickLinePaint)
        }

        // Pionowe granice bloków
        for (i in 0..boardSize step blockCols) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), thickLinePaint)
        }
    }

    private fun drawNumbers(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                if (board[row][col] != 0) {
                    val x = col * cellSize + cellSize / 2
                    val y = row * cellSize + cellSize / 2 - (textPaint.descent() + textPaint.ascent()) / 2

                    val paint = when {
                        originalBoard[row][col] != 0 -> fixedTextPaint
                        board[row][col] != solution[row][col] -> errorTextPaint
                        else -> textPaint
                    }

                    canvas.drawText(board[row][col].toString(), x, y, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                val col = (event.x / cellSize).toInt()
                val row = (event.y / cellSize).toInt()

                if (row in 0 until boardSize && col in 0 until boardSize) {
                    selectedRow = row
                    selectedCol = col

                    Log.d(TAG, "Selected cell: $row,$col - Editable: ${isCellEditable(row, col)}")

                    if (isCellEditable(row, col)) {
                        Log.d(TAG, "Notifying listener about cell selection")
                        cellSelectedListener?.onCellSelected(row, col)
                    }
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // Metoda do ustawiania wartości w komórce
    fun setCellValue(value: Int) {
        if (selectedRow != -1 && selectedCol != -1 && isCellEditable(selectedRow, selectedCol)) {
            board[selectedRow][selectedCol] = value
            invalidate()
            Log.d(TAG, "Set value $value at ($selectedRow, $selectedCol)")
        }
    }

    // Sprawdź czy komórka może być edytowana
    private fun isCellEditable(row: Int, col: Int): Boolean {
        // komórka edytowalna, jeśli w oryginalnym puzzlu była pusta
        return originalBoard[row][col] == 0
    }


    // Pomocnicze funkcje do generowania planszy
    private fun swapRows(row1: Int, row2: Int) {
        val temp = board[row1]
        board[row1] = board[row2]
        board[row2] = temp
    }

    private fun swapColumns(col1: Int, col2: Int) {
        for (i in board.indices) {
            val temp = board[i][col1]
            board[i][col1] = board[i][col2]
            board[i][col2] = temp
        }
    }

    private fun swapRowGroups() {
        if (boardSize != 9) return
        val group = Random.nextInt(3) * 3
        val group2 = (group + 3) % 9
        for (i in 0 until 3) {
            swapRows(group + i, group2 + i)
        }
    }

    private fun swapColumnGroups() {
        if (boardSize != 9) return
        val group = Random.nextInt(3) * 3
        val group2 = (group + 3) % 9
        for (i in 0 until 3) {
            swapColumns(group + i, group2 + i)
        }
    }
}