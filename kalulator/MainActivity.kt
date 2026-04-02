package com.example.calc1

import kotlin.math.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Stack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Calc()
        }
    }
}

@Composable
fun Calc() {

    val expression = remember { mutableStateOf("") }

    fun add(x: String) {
        expression.value += x
    }

    fun clear() {
        expression.value = ""
    }

    fun calculate() {
        try {
            val result = eval(expression.value)
            expression.value = result.toString()
        } catch (e: Exception) {
            expression.value = "Error"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            text = if (expression.value.isEmpty()) "0" else expression.value,
            fontSize = 32.sp
        )

        @Composable
        fun btn(
            text: String,
            modifier: Modifier = Modifier.weight(1f),
            onClick: () -> Unit
        ) {
            Button(
                onClick = onClick,
                modifier = modifier.padding(4.dp)
            ) {
                Text(text)
            }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("1") { add("1") }
            btn("2") { add("2") }
            btn("3") { add("3") }
            btn("+") { add("+") }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("4") { add("4") }
            btn("5") { add("5") }
            btn("6") { add("6") }
            btn("-") { add("-") }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("7") { add("7") }
            btn("8") { add("8") }
            btn("9") { add("9") }
            btn("*") { add("*") }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("0") { add("0") }
            btn(".") { add(".") }
            btn("(") { add("(") }
            btn(")") { add(")") }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("/") { add("/") }
            btn("^") { add("^") }
            btn("C") { clear() }
            btn("=") { calculate() }
        }
        Row(Modifier.fillMaxWidth()) {
            btn("sin") { add("sin(") }
            btn("cos") { add("cos(") }
            btn("log") { add("log(") }
            btn("!") { add("!") }
        }
    }
}


fun eval(expression: String): Double {
    val nums = Stack<Double>()
    val ops = Stack<Char>()

    var i = 0
    while (i < expression.length) {
        when {
            expression[i].isDigit() || expression[i] == '.' -> {
                var num = ""
                while (i < expression.length &&
                    (expression[i].isDigit() || expression[i] == '.')
                ) {
                    num += expression[i]
                    i++
                }
                nums.push(num.toDouble())
                i--
            }

            expression[i] == '(' -> ops.push(expression[i])

            expression[i] == ')' -> {
                while (ops.peek() != '(') {
                    applyOp(nums,ops)
                }
                ops.pop()
                if (ops.isNotEmpty() && isFunction(ops.peek())){
                    applyUnaryOp(nums,ops.pop())
                }

            }
            expression[i].isLetter() -> {
                var func = ""
                while ( i < expression.length && expression[i].isLetter()){
                    func+= expression[i]
                    i++
                }
                val code = when (func) {
                    "sin" -> 's'
                    "cos" -> 'c'
                    "log" -> 'l'
                    else -> throw IllegalArgumentException("Unknonw")
                }
                ops.push(code)
                i--
            }
            expression[i] == '!' -> {
                if (nums.isEmpty()) throw IllegalArgumentException("needs number before factorial")
                nums.push(factorial(nums.pop()))
            }
            expression[i] in charArrayOf('+', '-', '*', '/','^') -> {
                while (ops.isNotEmpty() && precedence(ops.peek()) >= precedence(expression[i])) {
                    applyOp(nums, ops)
                }
                ops.push(expression[i])
            }
        }
        i++
    }

    while (ops.isNotEmpty()) {
        applyOp(nums,ops)
    }

    return nums.pop()
}



fun isFunction(op:Char): Boolean{
    return op in charArrayOf('s','c','l')
}
fun applyUnaryOp(nums: Stack<Double>, op: Char){
    val a = nums.pop()
    val result = when (op) {
        's' -> sin(a)
        'c' -> cos(a)
        'l' -> log10(a)
        else -> throw IllegalArgumentException("Unknown paramter")
    }
    nums.push(result)
}
fun precedence(op: Char): Int {
    return when (op) {
        '+', '-' -> 1
        '*', '/' -> 2
        '^' -> 3
        else -> 0
    }
}

fun applyOp(nums: Stack<Double>, ops: Stack<Char>) {
    val op = ops.pop()
    if (isFunction(op)){
        applyUnaryOp(nums,op)
    }
    val b = nums.pop()
    val a = nums.pop()

    val result = when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> a / b
        '^' -> a.pow(b)
        else -> 0.0
    }
    nums.push(result)
}

fun factorial(n: Double): Double{
    if (n < 0){throw IllegalArgumentException()}
    var result = 1.0
    for (i in 1..n.toInt()){
        result *= i
    }
    return result
}
