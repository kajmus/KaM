package com.example.kalkulator

// import.kotlin.math.* to sie przyda do sin() i cos() to zostawaim tobie
// dodaj przyciki i git bedzie moze zrobisz bo jak tera dodam to sie
// bede bawil w ukladnie tego jakos ladnie a mi sie juz nie chce
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
            btn("C") { clear() }
            btn("=") { calculate() }
        }
    }
}

// -------- ale OP kalkulator matematyczny twoja jebana mac --------

//expression przechowuje całe działanie w jednej lini spoko ok ok


/*
 jak szukalem po necie to znalazlem kod i ogarnoalem te nawiasy w sensie kolejnosc dzialan
 czyba jest dobrze zrobiane ale nwm na dole to masz eval
 Obsługuje:
 + - * / oraz nawiasy
*/

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
                    nums.push(applyOp(ops.pop(), nums.pop(), nums.pop()))
                }
                ops.pop()
            }

            expression[i] in charArrayOf('+', '-', '*', '/') -> {
                while (
                    ops.isNotEmpty() &&
                    precedence(ops.peek()) >= precedence(expression[i])


                // zobacz to ^^^ i kometarz na dole tam wyjasniam


                ) {
                    nums.push(applyOp(ops.pop(), nums.pop(), nums.pop()))
                }
                ops.push(expression[i])
            }
        }
        i++
    }

    while (ops.isNotEmpty()) {
        nums.push(applyOp(ops.pop(), nums.pop(), nums.pop()))
    }

    return nums.pop()
}
/*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 to oraz to co jest pod spodem  odpowiada za to ze nawiasy, * , /, sa wykonywanie
 przed dodawanie i odejmowaniem jakos to dziala i w koncu nie wyjebao bledu wiec KURWA NIE TYKAC
 bo sie rozjebie znowu thx :-) (UwU)
*/
fun precedence(op: Char): Int {
    return when (op) {
        '+', '-' -> 1
        '*', '/' -> 2
        else -> 0
    }
}

fun applyOp(op: Char, b: Double, a: Double): Double { //
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> a / b
        else -> 0.0
    }
}