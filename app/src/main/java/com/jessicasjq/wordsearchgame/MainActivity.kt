package com.jessicasjq.wordsearchgame

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), View.OnTouchListener {
    // Initialize the timer
    var totalTimeSpent : Long = 0
    private var startTime : Long = 0

    // Setting up variables needed
    private var xInitial = -1f
    private var yInitial = -1f
    private var xDiff = -1f
    private var yDiff = -1f
    private var prevXDiff = -1f
    private var prevYDiff = -1f

    // Whether the user is swiping vertically or horizontally
    enum class SwipeState { Undefined, Vertical, Horizontal }
    private var swipeState = SwipeState.Undefined

    private var cellWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Timer starts
        startTime = System.currentTimeMillis()

        // Hide the congratulation message
        congrats_layout.visibility = View.GONE
        celebrate.visibility = View.GONE

        // Set the width of each grid cell (for a 10*10 grid only)
        cellWidth = resources.displayMetrics.widthPixels/10

        for (i in 0 until numWords){
            wordArray[i] = Word(words[i])
        }

        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                linearLayout.getChildAt(t).setOnTouchListener(this)
            }
        }

        // Adjust height of the grid
        val params = words_grid.layoutParams as ConstraintLayout.LayoutParams
        params.height = resources.displayMetrics.widthPixels
        words_grid.layoutParams = params

        // Fill the rest of the grid with random letters
        generateRandomLetters()
    }

    // Each time the user swipes the word, a random colour will be used for the stroke
    private var selectedColour = R.drawable.selected_cell_background_blue

    override fun onTouch(v: View, event: MotionEvent): Boolean{

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // User started selecting cells
                // We now select a random colour to colour the cells they touched
                val random = Random()
                val randomColour = random.nextInt(5)
                if(randomColour == 0){
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background)
                    selectedColour = R.drawable.selected_cell_background
                } else if (randomColour == 1){
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background_blue)
                    selectedColour = R.drawable.selected_cell_background_blue
                } else if (randomColour == 2){
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background_green)
                    selectedColour = R.drawable.selected_cell_background_green
                } else if (randomColour == 3){
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background_red)
                    selectedColour = R.drawable.selected_cell_background_red
                } else if (randomColour == 4){
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background_yellow)
                    selectedColour = R.drawable.selected_cell_background_yellow
                }
                else{
                    v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background_blue)
                    selectedColour = R.drawable.selected_cell_background_blue
                }
                xInitial = event.x
                yInitial = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // User is still selecting cells
                if (xInitial != -1f && yInitial != -1f) {

                    val tag = v.tag.toString()
                    val tagInt = tag.toInt()

                    xDiff = xInitial - event.x
                    yDiff = yInitial - event.y

                    if (swipeState == SwipeState.Undefined || swipeState == SwipeState.Horizontal) {
                        when {
                            xDiff > cellWidth -> {
                                // moving left
                                if (prevXDiff == -1f || prevXDiff != -1f && prevXDiff < xDiff) {
                                    selectSingleCell((tagInt - (xDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Horizontal
                                } else if (prevXDiff != -1f && prevXDiff > xDiff) {
                                    unselectSingleCell((tagInt - (prevXDiff / cellWidth).toInt()).toString())
                                }
                            }
                            (-1) * xDiff > cellWidth -> {
                                // moving right
                                if (prevXDiff == -1f || prevXDiff != -1f && prevXDiff > xDiff) {
                                    selectSingleCell((tagInt + -1 * (xDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Horizontal
                                } else if (prevXDiff != -1f && prevXDiff < xDiff) {
                                    unselectSingleCell((tagInt - (prevXDiff / cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }

                    if (swipeState == SwipeState.Undefined || swipeState == SwipeState.Vertical) {
                        when {
                            yDiff > cellWidth -> {
                                // moving up
                                if (prevYDiff == -1f || prevYDiff != -1f && prevYDiff < yDiff) {
                                    selectSingleCell((tagInt - 10 * (yDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Vertical
                                } else if (prevYDiff != -1f && prevYDiff > yDiff) {
                                    unselectSingleCell((tagInt - 10 * (yDiff / cellWidth).toInt()).toString())
                                }
                            }
                            (-1) * yDiff > cellWidth -> {
                                // moving down
                                if (prevYDiff == -1f || prevYDiff != -1f && prevYDiff > yDiff) {
                                    selectSingleCell((tagInt + -10 * (yDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Vertical
                                } else if (prevYDiff != -1f && prevYDiff < yDiff) {
                                    unselectSingleCell((tagInt - 10 * (yDiff / cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }
                    prevXDiff = xDiff
                    prevYDiff = yDiff
                }
            }

            MotionEvent.ACTION_UP -> {
                // User is done selecting cells
                // We check if the word they touched is valid
                val tag = v.tag.toString()
                val tagInt = tag.toInt()
                var finalTag = tag

                if (swipeState == SwipeState.Horizontal) {
                    finalTag = when {
                        xDiff > cellWidth -> {
                            (tagInt - (xDiff / cellWidth).toInt()).toString()
                        }
                        -1 * xDiff > cellWidth -> {
                            (tagInt + -1 * (xDiff / cellWidth).toInt()).toString()
                        }
                        else -> tag
                    }
                } else if (swipeState == SwipeState.Vertical) {
                    finalTag = when {
                        yDiff > cellWidth -> {
                            (tagInt - 10 * (yDiff / cellWidth).toInt()).toString()
                        }
                        -1 * yDiff > cellWidth -> {
                            (tagInt + -10 * (yDiff / cellWidth).toInt()).toString()
                        }
                        else -> tag
                    }
                }
                checkIfRangeIsValid(v.tag.toString(), finalTag)
            }
        }
        return true
    }

    // If a valid word is swiped over, the word hint will be crossed out and be highlighted green
    private fun crossOutWords(wordObj : Word){
        if (wordObj.length == 4 && wordObj.content.trim() == "JAVA"){
            javaTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            javaTxt.setTextColor(Color.GREEN)
        } else if (wordObj.length == 5 && wordObj.content.trim() == "SWIFT"){
            swiftTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            swiftTxt.setTextColor(Color.GREEN)
        } else if (wordObj.content.trim() == "KOTLIN"){
            kotlinTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            kotlinTxt.setTextColor(Color.GREEN)
        } else if (wordObj.content.trim() == "MOBILE"){
            mobileTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            mobileTxt.setTextColor(Color.GREEN)
        } else if (wordObj.length == 8 && wordObj.content.trim() == "VARIABLE"){
            variableTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            variableTxt.setTextColor(Color.GREEN)
        } else if (wordObj.length == 10 && wordObj.content.trim() == "OBJECTIVEC"){
            objectivecTxt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            objectivecTxt.setTextColor(Color.GREEN)
        }
    }

    // Redo all the crossing out and highlighting
    private fun redoAllCrossedOutWords(){
        javaTxt.paintFlags = 0
        swiftTxt.paintFlags = 0
        kotlinTxt.paintFlags = 0
        mobileTxt.paintFlags = 0
        variableTxt.paintFlags = 0
        objectivecTxt.paintFlags = 0
        javaTxt.setTextColor(Color.BLACK)
        swiftTxt.setTextColor(Color.BLACK)
        kotlinTxt.setTextColor(Color.BLACK)
        mobileTxt.setTextColor(Color.BLACK)
        variableTxt.setTextColor(Color.BLACK)
        objectivecTxt.setTextColor(Color.BLACK)
    }

    // Checks whether the word that user found is a one of the target words
    private fun checkIfRangeIsValid(initTag: String, endTag: String){
        var found = false
        for(wordObj in wordArray){
            if(wordObj.checkLoc(
                    initTag.toInt(),
                    endTag.toInt(),
                    swipeState == SwipeState.Horizontal
                )){
                // It is a match with one of the target words
                if(wordObj.found){
                    // But the word has been already found
                    // So it cannot be found again
                    xInitial = -1f
                    yInitial = -1f
                    xDiff = -1f
                    yDiff = -1f
                    prevXDiff = -1f
                    prevYDiff = -1f
                    swipeState = SwipeState.Undefined
                    return
                }

                // Otherwise the user have found a valid word
                markCellsAsFound(
                    initTag.toInt(),
                    endTag.toInt(),
                    swipeState == SwipeState.Horizontal
                )
                wordObj.found = true
                found = true
                crossOutWords(wordObj)  // We cross out the hint for this word
                break
            }
        }

        // If the user found a valid word, display a happy cat
        if (found){
            greenCheck.visibility = View.VISIBLE
            happyCat.visibility = View.VISIBLE
            Timer("delay", false).schedule(1200) {
                runOnUiThread{
                    greenCheck.visibility = View.GONE
                    happyCat.visibility = View.GONE
                }
            }
            var showCongrats = true
            // Cross out the words that we already found
            for(wordObj in wordArray){
                if(wordObj.found) {
                    crossOutWords(wordObj)
                }
            }
            for(wordObj in wordArray){
                if(!wordObj.found){
                    showCongrats = false
                    break
                }
            }
            // If all valid words have been found by the user
            // Display the congratulation message and the total time spent to finish the game
            if (showCongrats){
                totalTimeSpent = System.currentTimeMillis() - startTime
                if (totalTimeSpent/1000 >= 60){
                    var minutes = totalTimeSpent/1000/60
                    var seconds = totalTimeSpent/1000%60
                    if (minutes < 10 && seconds < 10){
                        timeTxt.text = "Time Spent: 0" + minutes.toString() + ":0" + seconds.toString()
                    } else if (minutes >= 10 && seconds < 10){
                        timeTxt.text = "Time Spent: " + minutes.toString() + ":0" + seconds.toString()
                    } else if (minutes < 10 && seconds >= 10){
                        timeTxt.text = "Time Spent: 0" + minutes.toString() + ":" + seconds.toString()
                    } else{
                        timeTxt.text = "Time Spent: " + minutes.toString() + ":" + seconds.toString()
                    }

                }else {
                    timeTxt.text = "Time Spent: " + (totalTimeSpent / 1000).toString() + "s"
                }
                happyCat.visibility = View.GONE
                congrats_layout.visibility = View.VISIBLE

                // Display an even cuter cat
                winningCat.visibility = View.VISIBLE
                celebrate.visibility = View.VISIBLE
            }

        } else {
            // Otherwise, the user found a word that is invalid
            // Display a sad cat
            redX.visibility = View.VISIBLE
            sadCat.visibility = View.VISIBLE
            Timer("delay", false).schedule(1200) {
                runOnUiThread{
                    redX.visibility = View.GONE
                    sadCat.visibility = View.GONE
                }
            }
            unselectCellRange(initTag.toInt(), endTag.toInt(), swipeState == SwipeState.Horizontal)
        }

        // Resetting values
        xInitial = -1f
        yInitial = -1f
        xDiff = -1f
        yDiff = -1f
        swipeState = SwipeState.Undefined
    }

    // Unselect a range of cells
    private fun unselectCellRange(initTag: Int, endTag: Int, isHorizontal: Boolean){
        var start = initTag
        var end = endTag
        if (endTag < initTag){
            start = endTag
            end = initTag
        }
        if(isHorizontal){
            for (i in start..end){
                unselectSingleCell(i.toString())
            }
        } else {
            for (i in start..end step 10){
                unselectSingleCell(i.toString())
            }
        }
    }

    // Select a cell by tag
    private fun selectSingleCell(tag: String){
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                if(linearLayout.getChildAt(t).tag == tag){
                    linearLayout.getChildAt(t).background = ContextCompat.getDrawable(this, selectedColour)
                    return
                }
            }
        }
    }


    private fun unselectSingleCell(tag: String){
        var tagInt = tag.toInt()
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                if(linearLayout.getChildAt(t).tag == tag){
                    if(!foundWordsFlags[tagInt / 10][tagInt % 10]){
                        linearLayout.getChildAt(t).background = ContextCompat.getDrawable(this, R.drawable.unselected_cell_background)
                    }
                    return
                }
            }
        }
    }

    // Flag the found cells so that the don't get unselected until a new game is started
    private fun markCellsAsFound(initTag: Int, endTag: Int, isHorizontal: Boolean){
        var start = initTag
        var end = endTag
        if (endTag < initTag){
            start = endTag
            end = initTag
        }
        if(isHorizontal){
            for (i in start..end){
                foundWordsFlags[i / 10][i % 10] = true
            }
        } else {
            for (i in start..end step 10){
                foundWordsFlags[i / 10][i % 10] = true
            }
        }
    }

    // Clear all previously selected cells
    private fun unselectAllCells(){
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                linearLayout.getChildAt(t).background = ContextCompat.getDrawable(
                    this,
                    R.drawable.unselected_cell_background
                )
            }
        }
    }

    // Randomly re-arranges the letters in the grid
    private fun generateRandomLetters(){
        gridFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        foundWordsFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        val rnd = Random()
        var toggle: Boolean = rnd.nextInt(2) != 0

        for(r in 0 until gridSize){
            for (c in 0 until gridSize){
                gridLetters[r][c] = vocabulary[rnd.nextInt(vocabulary.length)].toString()
            }
        }

        // Positioning words to be found
        for (w in 0 until words.size){
            var found = false

            while (!found){
                var r = 0
                if(words[w].length < gridSize){
                    r = rnd.nextInt(gridSize - (words[w].length))
                } else if (words[w].length > gridSize){
                    // Not happening since we are only testing with the 6 given words
                    // that have a max of length 10
                    break
                }

                // Check all rows or columns depending on the toggle value
                var start = rnd.nextInt(gridSize - 1)

                for (n in 0 until gridSize){
                    var _n = (n + start) % gridSize
                    // Check if the row or column is empty enough to place the word
                    for (i in r until r + words[w].length ) {
                        if(toggle){
                            // Look along the row
                            if(gridFlags[_n][i] && gridLetters[_n][i] != words[w][i - r].toString()) {
                                break
                            } else if (i == r + words[w].length - 1) {
                                // We've reached the end
                                found = true
                            }
                        } else {
                            // Look along the column
                            if(gridFlags[i][_n]&& gridLetters[i][_n] != words[w][i - r].toString()) {
                                break
                            } else if (i == r + words[w].length - 1) {
                                // We've reached the end
                                found = true
                            }
                        }
                    }
                    if(found) {
                        // Register the valid word location in Word object
                        if(toggle){
                            wordArray[w].setLoc(_n * 10 + r, toggle)
                        } else {
                            wordArray[w].setLoc(r * 10 + _n, toggle)
                        }

                        for (i in r until r + words[w].length ) {
                            if(toggle){
                                // Filling along the row
                                gridLetters[_n][i] = words[w][i - r].toString()
                                gridFlags[_n][i] = true
                            } else {
                                // Filling along the column
                                gridLetters[i][_n] = words[w][i - r].toString()
                                gridFlags[i][_n] = true
                            }
                        }
                        break
                    }
                }
                toggle = !toggle
            }
        }

        // Display the letters
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                (linearLayout.getChildAt(t) as TextView).text = gridLetters[i][t]
            }
        }
    }

    // Re-start the game when the user clicks on the new game button
    fun reStartGame(view: View){
        congrats_layout.visibility = View.GONE
        winningCat.visibility = View.GONE
        celebrate.visibility = View.GONE
        for (i in 0 until numWords){
            wordArray[i] = Word(words[i])
        }
        unselectAllCells()
        redoAllCrossedOutWords()
        generateRandomLetters()
        startTime = System.currentTimeMillis()
    }


    companion object {
        const val vocabulary = "ABCDEFGHIJKLMOPQRSTUVWSYZ"
        const val numWords = 6
        const val gridSize = 10 // 10*10 grid

        var gridLetters = Array(gridSize) { Array<String>(gridSize) { "A" } }
        var gridFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        var foundWordsFlags = Array(gridSize) { BooleanArray(gridSize) { false } }

        val wordArray = Array<Word>(numWords) { Word("")}

        // The valid words that the user will need to locate
        val words = arrayOf("OBJECTIVEC", "VARIABLE", "MOBILE", "KOTLIN", "SWIFT", "JAVA")
    }
}
