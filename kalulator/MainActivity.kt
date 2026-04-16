package com.example.kalkulator

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

    fun add(x: String) { expression.value += x }

    fun clear() {
        expression.value = ""
        plotExpression.value = ""
        deltaResult.value = "-"
        rootsResult.value = "-"
    }

    fun calculatePlot() {
        try {
            val raw = expression.value.replace("f(x)=", "").replace("y=", "").replace(" ", "")
            plotExpression.value = raw

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
            val centerX = size.width / 2
            val centerY = size.height / 2
            val scale = 40f // Skala 40px
            drawLine(Color.LightGray, Offset(0f, centerY), Offset(size.width, centerY), 2f)
            drawLine(Color.LightGray, Offset(centerX, 0f), Offset(centerX, size.height), 2f)

            if (plotExpression.value.isNotEmpty()) {
                val points = mutableListOf<Offset>()
                // Zakres od -20 do 20
                for (i in -200..200) {
                    val x = i / 10.0
                    try {
                        val substituted = plotExpression.value.replace("x", "($x)")
                        val y = eval(substituted)

                        if (!y.isNaN() && !y.isInfinite()) {
                            val px = centerX + (x.toFloat() * scale)
                            val py = centerY - (y.toFloat() * scale)

                            if (px in 0f..size.width && py in 0f..size.height) {
                                points.add(Offset(px, py))

                            }
                        }
                    } catch (e: Exception) {}
                }
                if (points.size > 1) drawPoints(points, PointMode.Polygon, Color.Blue, 4f)
            }

            drawText(textMeasurer, "Wykres f(x)", topLeft = Offset(10f, 10f), style = TextStyle(fontSize = 12.sp))
        }

        // 2. PANEL INFORMACYJNY (Delta i MZ)
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Δ: ${deltaResult.value}", fontWeight = FontWeight.Bold, color = Color.Red)
            Text("Miejsca: ${rootsResult.value}", fontSize = 13.sp)
        }


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
            listOf("f", "x", "y=", "="),
            listOf("sin", "cos", "^2", "+")
        )

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { char ->
                    Btn(char){
                        when (char) {
                            "=" -> {
                                try{
                                    val cleanExpr = expression.value.replace("f(x)=", "").replace("y=","")
                                    val result = eval(cleanExpr)
                                    expression.value = result.toString()
                                }catch (e: Exception){
                                    expression.value = "Bład"
                                }
                            }
                            "f" -> add("f(x)=")
                            else -> add(char)

                        }

                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Btn("C", color = Color.Gray) { clear() }
            Btn("PLOT", flex = 2f) { calculatePlot() }
        }
    }
}

// --- PARSER MATEMATYCZNY (STOSY) ---
fun eval(expr: String): Double {
    val nums = Stack<Double>()
    val ops = Stack<String>()

    fun applyOp() {
        val op = ops.pop()
        if (op == "sin" || op == "cos") {
            nums.push(if (op == "sin") sin(nums.pop()) else cos(nums.pop()))
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