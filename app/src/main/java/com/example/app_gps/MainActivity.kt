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
    private var canAddOperation = false //флаг для добавления операции
    private var canAddDecimal = true//флаг  определяет можно ли добавить десятичную точку в текущее число
    private lateinit var workingTV: androidx.appcompat.widget.AppCompatTextView
    private lateinit var resultsTV: androidx.appcompat.widget.AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Инициализация TextView
        workingTV = findViewById(R.id.workingTV)
        resultsTV = findViewById(R.id.resultsTV)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun numberAction(view: View)//Функция для активации цифр
    {
        if(view is Button)
        {
            if(view.text == ".")//Проверка если число с точкой
            {
                if(canAddDecimal)//Проверка можно ли добавить десятичную точку
                    workingTV.append(view.text)
                canAddDecimal = false//ставим флаг на false чтобы нельззя было поставить еще точку
            }
            else
                workingTV.append(view.text)//Иначе добавляем нажатую цифру
            canAddOperation = true
        }
    }
    fun operationAction(view: View)//функция активации операций
    {
        if(view is Button && canAddOperation)//Если наша view это кнопка и мы можем добавить операцию пока canAddOperation = true
        {
            workingTV.append(view.text)//Добавляем нажатую операцию
            canAddOperation = false
            canAddDecimal = true
        }
    }
    fun clearAction(view: View)//функция очистки
    {
        workingTV.text = ""//очищаем строку
        resultsTV.text = "0"//в результат ставим 0
    }
    fun backspaceAction(view: View)//функция стирания
    {
       val length = workingTV.length()
       if(length > 0)
            workingTV.text = workingTV.text.subSequence(0, length -1)
    }
    fun equalsAction(view: View)//функция вычисления результата
    {
        resultsTV.text = calculateResults()
    }

    private fun calculateResults(): String//функция для вычисления результата
    {
        val digitsOperators = digitsOperators()//возвращает список цифр или операций введенных пользователем
        if (digitsOperators.isEmpty()) return  ""//Если список пуст, то выводится пустота

        val timesDivision = timesDivisionCalculate(digitsOperators)//выполняет вычисления для операций которые выполняются в первую очередь
        if (timesDivision.isEmpty()) return  ""
        val result = addSubtractCalculate(timesDivision)//выполняет вычисления для операций которые выполняются после
        return result.toString()
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float//функция,которая принимает список с элементами произвольного типа и возвращает число типа float
    {
        var result = passedList[0] as Float//первый элемент в списке  float
        for(i in passedList.indices)//перебираем все индексы
        {
            if(passedList[i] is Char && i != passedList.lastIndex)//проверка на оператор при условии, что это не последний элемент в списке
            {
                val operator = passedList[i]//Если оператор, то сохраняется в эту переменную
                val nextDigit = passedList[i + 1] as Float//следующий элемент сохраняется как float, тк это не оператор
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
       while (list.contains('x') || list.contains('/'))//Пока есть операции умножения и деления
       {
           list = calcTimesDiv(list)//выполняем соотв. операцию
       }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()//новый список для хранения вычислений
        var restartIndex = passedList.size//отслеживает индекс который не был учтен
        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]//если текуций символ оператор
                val prevDigit = passedList[i - 1] as Float//предыдущий это число
                val nextDigit = passedList[i + 1] as Float//следующий число
                if (operator == 'x') {
                    newList.add(prevDigit * nextDigit)
                    restartIndex = i + 1
                } else if (operator == '/') {
                    newList.add(prevDigit / nextDigit)
                    restartIndex = i + 1
                } else {//если оператор умножение и не деление добавляем предыдущее число и оператор
                    newList.add(prevDigit)
                    newList.add(operator)
                }
            }
            if (i > restartIndex) {
                newList.add(passedList[i])
            }
        }
        return newList
    }

    private fun digitsOperators(): MutableList<Any>
    {
        val list = mutableListOf<Any>()//новый список для
        var currentDigit = ""//переменная для цифр
        for (simvol in workingTV.text)//перебор по каждому символу
        {
            if(simvol.isDigit() || simvol == '.')//если наш символ это цифра или точка
                currentDigit += simvol// то символ добавляем это число
            else
            {//если не символ
                list.add(currentDigit.toFloat())//то добавляем текущее число во float
                currentDigit = ""//текущее число очищается для ввода нового
                list.add(simvol)
            }
        }
        if(currentDigit != "")//если не пустое значение
            list.add(currentDigit.toFloat())//добавляем число типом float
        return  list
    }

}