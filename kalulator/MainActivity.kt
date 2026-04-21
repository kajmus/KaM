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
import android.util.Log
//import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Calc() }
    }
}

@Composable
fun Calc() {
    val expression = remember { mutableStateOf("") }
    val plotExpression = remember { mutableStateOf("") }
    val deltaResult = remember { mutableStateOf("-") }
    val rootsResult = remember { mutableStateOf("-") }
    val textMeasurer = rememberTextMeasurer()
    val graphStart = remember { mutableStateOf("-20")}
    val graphEnd = remember {mutableStateOf("20")}

    fun add(x: String) { expression.value += x }

    fun clear() {
        expression.value = ""
        plotExpression.value = ""
        deltaResult.value = "-"
        rootsResult.value = "-"
        graphStart.value = "-20"
        graphEnd.value = "20"
    }

    fun calculatePlot() {
        try {
            val raw = expression.value.replace("f(x)=", "").replace("y=", "").replace(" ", "")
            val parts = raw.split(",")
//            Log.d("DEBUG", "wartość: $parts")
            if (parts.isNotEmpty() && parts.size == 3) {
                plotExpression.value = parts[0]
                if (parts[1].toDouble() < parts[2].toDouble()){
                    graphStart.value = parts[1]
                    graphEnd.value = parts[2]
                }
            }else {
                plotExpression.value = raw
            }
            val parsed = parseQuadratic(raw)
            if (parsed != null) {
                val (a, b, c) = parsed
                val d = b * b - 4 * a * c
                deltaResult.value = "%.2f".format(d)
                rootsResult.value = calcRootsUi(a, b, c)
            } else {
                deltaResult.value = "N/A"
                rootsResult.value = "Funkcja złożona"
            }
        } catch (e: Exception) {
            deltaResult.value = "Błąd"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF0F0F0))) {
            val yMin = -15.0
            val yMax = 15.0
            val yRange = yMax - yMin

            if (plotExpression.value.isNotEmpty()) {
                val xMin = graphStart.value.toDoubleOrNull() ?: -20.0
                val xMax = graphEnd.value.toDoubleOrNull() ?: 20.0
                val xRange = xMax - xMin

                if (xRange != 0.0 && yRange != 0.0) {
                    fun mapX(x: Double) = ((x - xMin) / xRange * size.width).toFloat()
                    fun mapY(y: Double) = (size.height - (y - yMin) / yRange * size.height).toFloat()

                    if (yMin <= 0.0 && yMax >= 0.0) {
                        val py = mapY(0.0)
                        drawLine(Color.LightGray, Offset(0f, py), Offset(size.width, py), 2f)
                    }
                    if (xMin <= 0.0 && xMax >= 0.0) {
                        val px = mapX(0.0)
                        drawLine(Color.LightGray, Offset(px, 0f), Offset(px, size.height), 2f)
                    }

                    val steps = 500
                    val step = xRange / steps
                    val points = mutableListOf<Offset>()

                    for (i in 0..steps) {
                        val x = xMin + i * step
                        try {
                            val xStr = String.format(java.util.Locale.US, "%.10f", x)
                                .trimEnd('0')
                                .trimEnd('.')
                            val substituted = plotExpression.value.replace("x", "($xStr)")
                            val y = eval(substituted)

                            if (!y.isNaN() && !y.isInfinite()) {
                                points.add(Offset(mapX(x), mapY(y)))
                            }
                        } catch (e: Exception) { }
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(Color.Blue, points[i], points[i + 1], strokeWidth = 3f)
                    }
                }
            }
            drawText(textMeasurer, "Wykres f(x)= ${expression.value}", topLeft = Offset(10f, 10f), style = TextStyle(fontSize = 12.sp))
        }

        // 2. PANEL INFORMACYJNY (Delta i MZ) nie działa w zakresach
//        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//            Text("Δ: ${deltaResult.value}", fontWeight = FontWeight.Bold, color = Color.Red)
//            Text("Miejsca: ${rootsResult.value}", fontSize = 13.sp)
//        }


        Text(
            text = expression.value.ifEmpty { "0" },
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(8.dp)
        )


        @Composable
        fun Btn(txt: String, flex: Float = 1f, color: Color = Color(0xFF2196F3), onClick: () -> Unit) {
            Button(
                onClick = onClick,
                modifier = Modifier.weight(flex).padding(1.dp).height(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text(txt, fontSize = 12.sp) }
        }

        val rows = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ".", "(", ")"),
            listOf("y=", "x", ",", "^"),
            listOf("sin", "cos", "log", "+")
        )

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { char ->
                    Btn(char){
                            add(char)
                        }
                    }
                }
            }

        Row(modifier = Modifier.fillMaxWidth()) {
            Btn("C", color = Color.Gray) { clear() }
            Btn("PLOT",) { calculatePlot() }
            Btn("=") {
                try{
                    val cleanExpr = expression.value.replace("f(x)=", "").replace("y=","")
                    val result = eval(cleanExpr)
                    expression.value = result.toString()
                }catch (e: Exception){
                    expression.value = "Bład"
                }
            }
        }
    }
}

// --- PARSER MATEMATYCZNY (STOSY) ---
fun eval(expr: String): Double {
    val nums = Stack<Double>()
    val ops = Stack<String>()

    fun applyOp() {
        val op = ops.pop()
        if (op == "sin" || op == "cos" || op == "log") {
            nums.push(if (op == "sin") {sin(nums.pop())} else if (op == "cos") {cos(nums.pop())} else {log10(nums.pop())})
        } else {
            val b = nums.pop(); val a = nums.pop()
            nums.push(when(op) {
                "+" -> a + b; "-" -> a - b; "*" -> a * b; "/" -> a / b; "^" -> a.pow(b); else -> 0.0
            })
        }
    }

    var i = 0
    val s = expr.replace(" ", "")
    while (i < s.length) {
        val c = s[i]
        when {
            c.isDigit() || c == '.' -> {
                var n = ""; while(i < s.length && (s[i].isDigit() || s[i] == '.')) n += s[i++]
                nums.push(n.toDouble()); i--
            }
            c == '(' -> ops.push("(")
            c == ')' -> {
                while(ops.peek() != "(") applyOp()
                ops.pop()
                if(ops.isNotEmpty() && (ops.peek() == "sin" || ops.peek() == "cos")) applyOp()
            }
            c.isLetter() -> {
                var f = ""; while(i < s.length && s[i].isLetter()) f += s[i++]
                if (f != "x" && f != "y") ops.push(f)
                i--
            }
            c in "+-*/^" -> {
                if (c== '-' && (i==0 || s[i-1] == '(')) {
                    nums.push(0.0)
                }
                val p = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3)
                while(ops.isNotEmpty() && ops.peek() != "(" && (p[ops.peek()] ?: 0) >= (p[c.toString()] ?: 0)) applyOp()
                ops.push(c.toString())
            }
        }
        i++
    }
    while(ops.isNotEmpty()) applyOp()
    return if(nums.isEmpty()) 0.0 else nums.pop()
}

// --- PARSER WSPÓŁCZYNNIKÓW KWADRATOWYCH ---
fun parseQuadratic(e: String): Triple<Double, Double, Double>? {
    return try {
        val s = e.split("=").last()
            .replace(" ","")
            .replace("*", "")
            .replace("-","+-")

        val parts = s.split("+").filter { it.isNotEmpty() }
        var a = 0.0; var b = 0.0; var c = 0.0


        for (p in parts) {
            when {
                p.contains("x^2") || p.contains("x2") -> {
                    val oef = p.replace("x^2", "").replace("x2", "")
                    a += when(oef) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> oef.toDouble()
                    }
                }

                p.contains("x") || p.contains("x^2") -> {
                    val oef = p.replace("x", "")
                    b += when(oef) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> oef.toDouble()
                    }
                }
                else -> {
                    c += p.toDouble()
                }

            }
        }
        Triple(a, b, c)
    } catch (ex: Exception) { null }
}

fun calcRootsUi(a: Double, b: Double, c: Double): String {
    if (a == 0.0) return "Liniowa"
    val d = b * b - 4 * a * c
    return when {
        d > 0 -> "x1=${"%.1f".format((-b-sqrt(d))/(2*a))}, x2=${"%.1f".format((-b+sqrt(d))/(2*a))}"
        d == 0.0 -> "x0=${"%.1f".format(-b/(2*a))}"
        else -> "Brak"
    }
}