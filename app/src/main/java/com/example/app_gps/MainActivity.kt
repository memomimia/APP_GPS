package com.example.app_gps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.sync.Mutex

class MainActivity : AppCompatActivity() {
    private var canAddOperation = false
    private var canAddDecimal = true
    private lateinit var workingTV: androidx.appcompat.widget.AppCompatTextView
    private lateinit var resultsTV: androidx.appcompat.widget.AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Инициализация TextView
        workingTV = findViewById(R.id.workingTV)
        resultsTV = findViewById(R.id.resultsTV)

        // Настройка отступов для полноэкранного режима
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun numberAction(view: View)
    {
        if(view is Button)
        {
            if(view.text == ".")
            {
                if(canAddDecimal)
                    workingTV.append(view.text)
                canAddDecimal = false
            }
            else
                workingTV.append(view.text)
            canAddOperation = true
        }
    }
    fun operationAction(view: View)
    {
        if(view is Button && canAddOperation)
        {
            workingTV.append(view.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }
    fun clearAction(view: View)
    {
        workingTV.text = ""
        resultsTV.text = ""
    }
    fun backspaceAction(view: View)
    {
       val length = workingTV.length()
       if(length > 0)
            workingTV.text = workingTV.text.subSequence(0, length -1)
    }
    fun equalsAction(view: View)
    {
        resultsTV.text = calculateResults()
    }

    private fun calculateResults(): String
    {
        val digitsOperators = digitsOperators()
        if (digitsOperators.isEmpty()) return  ""

        val timesDivision = timesDivisionCalculate(digitsOperators)
        if (timesDivision.isEmpty()) return  ""
        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float
    {
        var result = passedList[0] as Float
        for(i in passedList.indices)
        {
            if(passedList[i] is Char && i != passedList.lastIndex)
            {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '-')
                    result -= nextDigit
            }
        }

        return result
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any>
    {
       var list = passedList
       while (list.contains('x') || list.contains('/'))
       {
           list = calcTimesDiv(list)
       }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any>
    {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size
        for (i in passedList.indices)
        {
            if(passedList[i] is Char && i != passedList.lastIndex && i < restartIndex)
            {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when(operator)
                {
                   'x' ->
                   {
                       newList.add(prevDigit * nextDigit)
                       restartIndex = i + 1
                   }
                    '/' ->
                    {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else ->
                    {
                       newList.add(prevDigit)
                       newList.add(operator)
                    }
                }
            }
            if (i > restartIndex)
                newList.add(passedList[i])
        }

        return newList
    }

    private fun digitsOperators(): MutableList<Any>
    {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (character in workingTV.text)
        {
            if(character.isDigit() || character == '.')
                currentDigit += character
            else
            {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(character)
            }
        }
        if(currentDigit != "")
            list.add(currentDigit.toFloat())


        return  list
    }

}