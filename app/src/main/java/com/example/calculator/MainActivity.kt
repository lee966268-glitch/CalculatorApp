package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 简易计算器
 * 功能：加减乘除、百分号、正负号、小数、退格、清零
 * 支持连续运算和运算符优先级（先乘除后加减）
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView

    // 当前输入的表达式，如 "12+3×4"
    private var currentInput = ""
    // 上一次是否刚按过 =（显示的是结果）
    private var lastResultDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvExpression = findViewById(R.id.tvExpression)

        // ---- 数字键 ----
        findViewById<Button>(R.id.btn0).setOnClickListener { appendInput("0") }
        findViewById<Button>(R.id.btn1).setOnClickListener { appendInput("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { appendInput("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { appendInput("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { appendInput("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { appendInput("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { appendInput("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { appendInput("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { appendInput("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { appendInput("9") }
        findViewById<Button>(R.id.btnDot).setOnClickListener { appendInput(".") }

        // ---- 运算符 ----
        findViewById<Button>(R.id.btnPlus).setOnClickListener { appendInput("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { appendInput("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendInput("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendInput("÷") }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { appendInput("%") }

        // ---- 功能键 ----
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deleteLast() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener { toggleSign() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculate() }
    }

    /** 追加输入，带合法性校验（防止 1++2、1..2 等） */
    private fun appendInput(value: String) {
        // 刚算出结果时：如果按数字/小数点，重新开始；如果按运算符，基于结果继续算
        if (lastResultDisplayed) {
            if (value in listOf("+", "-", "×", "÷", "%")) {
                lastResultDisplayed = false
            } else {
                currentInput = ""
                tvExpression.text = ""
                lastResultDisplayed = false
            }
        }

        // 小数点：同一个数字里只能有一个
        if (value == ".") {
            val lastNumber = currentInput.split('+', '-', '×', '÷', '%').lastOrNull() ?: ""
            if (lastNumber.contains(".")) return
            if (lastNumber.isEmpty()) {
                currentInput += "0"
            }
        }

        // 运算符不能在开头（负号除外），也不能连续出现
        if (value in listOf("+", "×", "÷", "%")) {
            if (currentInput.isEmpty()) return
            val last = currentInput.last()
            if (last in listOf('+', '-', '×', '÷', '%', '.')) return
        }
        if (value == "-") {
            if (currentInput.isNotEmpty()) {
                val last = currentInput.last()
                if (last in listOf('+', '-', '×', '÷', '%', '.')) return
            }
        }

        currentInput += value
        updateDisplay()
    }

    /** C：全部清空 */
    private fun clearAll() {
        currentInput = ""
        tvExpression.text = ""
        lastResultDisplayed = false
        updateDisplay()
    }

    /** ⌫：删除最后一个字符 */
    private fun deleteLast() {
        if (lastResultDisplayed) {
            clearAll()
            return
        }
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            updateDisplay()
        }
    }

    /** +/−：切换最后一个数字的正负号 */
    private fun toggleSign() {
        if (currentInput.isEmpty() || currentInput == "0") return

        // 刚算出结果：整体取反
        if (lastResultDisplayed) {
            currentInput = if (currentInput.startsWith("-")) {
                currentInput.substring(1)
            } else {
                "-$currentInput"
            }
            lastResultDisplayed = false
            updateDisplay()
            return
        }

        // 找到最后一个“二元运算符”的位置（跳过一元负号）
        var lastOpIndex = -1
        for (i in currentInput.length - 1 downTo 0) {
            val c = currentInput[i]
            if (c in listOf('+', '×', '÷', '%')) {
                lastOpIndex = i
                break
            }
            if (c == '-') {
                // 减号：前面是数字/小数点/% 才是二元减法，否则是一元负号
                if (i > 0 && (currentInput[i - 1].isDigit() || currentInput[i - 1] == '.' || currentInput[i - 1] == '%')) {
                    lastOpIndex = i
                    break
                }
                // 一元负号则跳过继续找
            }
        }

        val prefix = if (lastOpIndex == -1) "" else currentInput.substring(0, lastOpIndex + 1)
        var lastNum = if (lastOpIndex == -1) currentInput else currentInput.substring(lastOpIndex + 1)
        if (lastNum.isEmpty()) return

        lastNum = if (lastNum.startsWith("-")) lastNum.substring(1) else "-$lastNum"
        currentInput = prefix + lastNum
        updateDisplay()
    }

    /** =：计算结果 */
    private fun calculate() {
        if (currentInput.isEmpty()) return
        val last = currentInput.last()
        if (last in listOf('+', '-', '×', '÷', '%', '.')) return

        tvExpression.text = "$currentInput ="
        try {
            val result = evaluate(currentInput)
            currentInput = formatResult(result)
            lastResultDisplayed = true
            updateDisplay()
        } catch (e: ArithmeticException) {
            tvDisplay.text = "错误：除数不能为零"
            currentInput = ""
            lastResultDisplayed = false
        } catch (e: Exception) {
            tvDisplay.text = "错误"
            currentInput = ""
            lastResultDisplayed = false
        }
    }

    /**
     * 求值：支持 + - × ÷ %，先乘除后加减
     * % 会被转成 /100，例如 50% = 0.5，200+10% = 200.1
     */
    private fun evaluate(expr: String): Double {
        var e = expr.replace('×', '*').replace('÷', '/').replace("%", "/100")

        val numbers = mutableListOf<Double>()
        val ops = mutableListOf<Char>()
        var numStr = ""

        for (i in e.indices) {
            val c = e[i]
            when {
                c.isDigit() || c == '.' -> numStr += c
                // 一元负号：数字开头是 -，或紧跟在运算符后面
                c == '-' && numStr.isEmpty() && (i == 0 || e[i - 1] in "+-*/") -> numStr += c
                c in "+-*/" -> {
                    if (numStr.isEmpty() || numStr == "-") throw IllegalArgumentException("表达式错误")
                    numbers.add(numStr.toDouble())
                    numStr = ""
                    ops.add(c)
                }
                else -> throw IllegalArgumentException("非法字符")
            }
        }
        if (numStr.isEmpty() || numStr == "-") throw IllegalArgumentException("表达式错误")
        numbers.add(numStr.toDouble())

        // 第一遍：先算乘除
        var i = 0
        while (i < ops.size) {
            if (ops[i] == '*' || ops[i] == '/') {
                val a = numbers[i]
                val b = numbers[i + 1]
                val res = if (ops[i] == '*') {
                    a * b
                } else {
                    if (b == 0.0) throw ArithmeticException("除数不能为零")
                    a / b
                }
                numbers[i] = res
                numbers.removeAt(i + 1)
                ops.removeAt(i)
            } else {
                i++
            }
        }

        // 第二遍：再算加减
        i = 0
        while (i < ops.size) {
            val a = numbers[i]
            val b = numbers[i + 1]
            val res = if (ops[i] == '+') a + b else a - b
            numbers[i] = res
            numbers.removeAt(i + 1)
            ops.removeAt(i)
        }

        return numbers[0]
    }

    /** 格式化结果：整数不显示小数点，小数最多保留 10 位并去尾零 */
    private fun formatResult(d: Double): String {
        if (d.isNaN() || d.isInfinite()) throw ArithmeticException("计算错误")
        return if (d % 1.0 == 0.0) {
            // 防止超大整数溢出 Long
            if (d > Long.MAX_VALUE || d < Long.MIN_VALUE) d.toString()
            else d.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.10f", d).trimEnd('0').trimEnd('.')
        }
    }

    private fun updateDisplay() {
        tvDisplay.text = if (currentInput.isEmpty()) "0" else currentInput
    }
}
