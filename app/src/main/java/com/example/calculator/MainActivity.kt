package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView

    private val expr = StringBuilder()
    private val currentInput = StringBuilder()

    private var operand1: Double? = null
    private var pendingOp: Char? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvDisplay.text = "0"

        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { onDigit((it as Button).text.toString()) }
        }
        findViewById<Button>(R.id.btnDot).setOnClickListener { onDot() }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperator('+') }
        findViewById<Button>(R.id.btnSub).setOnClickListener { onOperator('-') }
        findViewById<Button>(R.id.btnMul).setOnClickListener { onOperator('×') }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { onOperator('÷') }

        findViewById<Button>(R.id.btnEq).setOnClickListener { onEquals() }

        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnDel).setOnClickListener { onBackspace() }
        findViewById<Button>(R.id.btnSign).setOnClickListener { toggleSign() }
    }

    private fun onDigit(d: String) {
        if (expr.isEmpty() && (tvDisplay.text.toString() == "0")) {
            expr.clear()
        }
        currentInput.append(d)
        expr.append(d)
        updateDisplay()
    }

    private fun onDot() {
        if (!currentInput.contains('.')) {
            if (currentInput.isEmpty()) {
                currentInput.append("0")
                expr.append("0")
            }
            currentInput.append('.')
            expr.append('.')
            updateDisplay()
        }
    }

    private fun onOperator(op: Char) {
        val currentVal = currentInput.toString().toDoubleOrNull()
        if (operand1 == null) {
            if (currentVal != null) {
                operand1 = currentVal
                currentInput.clear()
            } else if (expr.isEmpty()) {
                return
            }
        } else {
            if (currentVal != null && pendingOp != null) {
                val res = calculate(operand1!!, currentVal, pendingOp!!)
                operand1 = res
                currentInput.clear()
            }
        }

        if (expr.isNotEmpty()) {
            if (endsWithOperator()) {
                expr.delete(expr.length - 3, expr.length) // hapus " ␣op␣"
            }
            expr.append(" ").append(op).append(" ")
            updateDisplay()
        }
        pendingOp = op
    }

    private fun onEquals() {
        val second = currentInput.toString().toDoubleOrNull()
        if (operand1 != null && pendingOp != null && second != null) {
            val res = calculate(operand1!!, second, pendingOp!!)
            val shown = expr.toString().trimEnd()
            tvDisplay.text = if (res.isNaN()) "Error" else "$shown = ${trimTrailingZero(res)}"

            expr.clear()
            currentInput.clear()
            if (!res.isNaN()) {
                val asText = trimTrailingZero(res)
                expr.append(asText)
                currentInput.append(asText)
                updateDisplay()
            } else {
                clearAll()
            }
            operand1 = null
            pendingOp = null
        }
    }

    private fun onBackspace() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            expr.deleteCharAt(expr.length - 1)
        } else if (endsWithOperator()) {
            expr.delete(expr.length - 3, expr.length)
            pendingOp = null
        }
        if (expr.isEmpty()) tvDisplay.text = "0" else updateDisplay()
    }

    private fun toggleSign() {
        if (currentInput.isEmpty()) return
        val seg = currentInput.toString()
        val replacement = if (seg.startsWith("-")) seg.drop(1) else "-$seg"

        val startIdx = expr.length - seg.length
        if (startIdx >= 0) {
            expr.replace(startIdx, expr.length, replacement)
            currentInput.clear().append(replacement)
            updateDisplay()
        }
    }

    private fun clearAll() {
        expr.clear()
        currentInput.clear()
        operand1 = null
        pendingOp = null
        tvDisplay.text = "0"
    }

    private fun updateDisplay() {
        tvDisplay.text = if (expr.isEmpty()) "0" else expr.toString()
    }

    private fun endsWithOperator(): Boolean {
        if (expr.length < 3) return false
        val s = expr.toString()
        return s[s.length - 2] == ' ' && isOperatorChar(s[s.length - 3]) && s[s.length - 1] == ' '
    }

    private fun isOperatorChar(c: Char) = c == '+' || c == '-' || c == '×' || c == '÷'

    private fun calculate(a: Double, b: Double, op: Char): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '×' -> a * b
            '÷' -> if (b != 0.0) a / b else Double.NaN
            else -> b
        }
    }

    private fun trimTrailingZero(d: Double): String {
        val l = d.toLong()
        return if (d == l.toDouble()) l.toString() else d.toString()
    }
}
