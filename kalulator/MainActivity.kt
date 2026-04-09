package com.example.calc1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Stack
import kotlin.math.*

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
    val plotExpression = remember { mutableStateOf("") }
    val deltaResult = remember { mutableStateOf("-") }
    val rootsResult = remember { mutableStateOf("-") }


    fun add(x: String) {
        expression.value += x
    }

    fun clear() {
        expression.value = ""
        deltaResult.value = "-"
        rootsResult.value = "-"

    }

    fun calculate() {
        try {
            val result = eval(expression.value)
            expression.value = result.toString()
        } catch (e: Exception) {
            expression.value = "Error"
        }
    }
    fun calculateChart() {
        try {
            // ta część jest po kliknięciu plot odpalać będzię parser czyli eval Equation
            // tu poniżej masz jak aktualizować  wartośći wyświetlane obok wykresu
            // deltaResult.value = obliczDelte(a, b, c).toString()
            // rootsResult.value = obliczMiejscaZeroweString(a, b, c)
            plotExpression.value = expression.value
            evalEquation(expression.value)
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
//        Spacer(modifier = Modifier.height(100.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
        // Canvas
        val textMeasurer = rememberTextMeasurer()

        Canvas(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray)
        ) {
            // Rozmiar Canvas
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Środek układu współrzędnych
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            // Skala (jak duży ma być wykres na Canvasie)
            val scaleX = canvasWidth / 20 // Skala dla osi X (przedział -10 do 10)
            val scaleY = canvasHeight / 4 // Skala dla osi Y

            // Rysowanie osi X i Y
            drawLine(
                color = Color.Black,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Black,
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasHeight),
                strokeWidth = 2f
            )
            //
            // !!!!! tutaj trzeba zrobić aby rysowało funkcję czyli wynik z parsera
            //
            // Rysowanie wykresu funkcji sin(x)/x
            val points = mutableListOf<Offset>()
            for (x in -1000..1000) { // Iteracja po wartościach w przedziale od -10 do 10 (skalowane)
                val realX = x / 100f // Przeliczenie wartości na rzeczywisty przedział (-10 do 10)
                val yValue = if (realX == 0f) 1f else kotlin.math.sin(realX) / realX // Obliczenie sin(x)/x

                // Przeliczenie współrzędnych na Canvas (skala i przesunięcie środka)
                val canvasX = centerX + realX * scaleX
                val canvasY = centerY - yValue * scaleY

                points.add(Offset(canvasX, canvasY))
            }

            // Rysowanie punktów jako wykresu funkcji
            drawPoints(
                points = points,
                pointMode = PointMode.Polygon, // Łączenie punktów linią
                color = Color.Blue,
                strokeWidth = 2f
            )

            // Tekst
            drawText(
                textMeasurer = textMeasurer,
                text = "f(x)=${plotExpression.value}",
                topLeft = Offset(x = 10f, y = 10f),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp, color = Color.Black
                )
            )

        }

        Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Delta:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = deltaResult.value, fontSize = 16.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Miejsca zerowe:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = rootsResult.value, fontSize = 16.sp, color = Color.DarkGray)
            }
        }
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
            btn("sin") { add("sin(") }
            btn("cos") { add("cos(") }
            btn("log") { add("log(") }
            btn("!") { add("!") }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("/") { add("/") }
            btn("^") { add("^") }
            btn("C") { clear() }
            btn("=") { calculate() }
        }

        Row(Modifier.fillMaxWidth()) {
            btn("x") { add("x") }
            btn("y") { add("y") }
            btn("f") { add("f") }
            btn("plot") { calculateChart() }
        }
    }
}

// parser do wypełnienia
fun evalEquation(expression: String) {}
// oblicza delte
fun calcDelte(a: Double, b: Double, c: Double): Double {
    return b * b - 4 * a * c
}

// zwróci string który wyświetli się obok tabelki
fun calcRootsUi(a:Double, b:Double, c: Double): String{
    if (a == 0.0) return "Brak (a = 0)"
    val d = calcDelte(a,b,c)

    if (d > 0){
        val x1 = (-b  - sqrt(d))/ (2*a)
        val x2 = (-b  + sqrt(d))/ (2*a)
        return "x1= ${"%.2f".format(x1)}\nx2= ${"%.2f".format(x2)}"
    }
    else if (d == 0.0){
        val x = (-b )/ (2*a)
        return "x0= ${".2f".format(x)}"
    }
    else {
        return "Brak pierwiastków rzeczywistych"
    }
}

// wchodzi lista współczynników wychodzi jeden float
fun horner(coefficients: DoubleArray, x: Double): Double{
    if (coefficients.isEmpty()) return 0.0
    var result = coefficients[0]
    for (i in 1 until coefficients.size) {
        result = result*x + coefficients[i]
    }
    return result
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




