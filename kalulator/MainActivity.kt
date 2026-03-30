
package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Calc()
        }
    }

    @Composable
    fun Calc(){

        val s: MutableState<String> = remember{ mutableStateOf("0") }
        val op: MutableState<Char> = remember{ mutableStateOf('+') }
        val buf: MutableIntState = remember{ mutableIntStateOf(0) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){

            Row{
                Text(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    text = s.value,
                    fontSize = 20.sp
                )
            }

            Row{

                Button(onClick = { if (s.value == "0") s.value = "1" else s.value += "1" }) {
                    Text(text = "1")
                }

                Button(onClick = { if(s.value == "0") s.value="2" else s.value+="2" }) {
                    Text("2")
                }

                Button(onClick = { if(s.value == "0") s.value="3" else s.value+="3" }) {
                    Text("3")
                }

                Button(onClick = {
                    op.value='+'
                    buf.intValue = s.value.toInt()
                    s.value="0"
                }) {
                    Text("+")
                }
            }

            Row{

                Button(onClick = { if(s.value == "0") s.value="4" else s.value+="4" }) {
                    Text("4")
                }

                Button(onClick = { if(s.value == "0") s.value="5" else s.value+="5" }) {
                    Text("5")
                }

                Button(onClick = { if(s.value == "0") s.value="6" else s.value+="6" }) {
                    Text("6")
                }

                Button(onClick = {
                    op.value='-'
                    buf.intValue = s.value.toInt()
                    s.value="0"
                }) {
                    Text("-")
                }
            }

            Row{

                Button(onClick = { if(s.value == "0") s.value="7" else s.value+="7" }) {
                    Text("7")
                }

                Button(onClick = { if(s.value == "0") s.value="8" else s.value+="8" }) {
                    Text("8")
                }

                Button(onClick = { if(s.value == "0") s.value="9" else s.value+="9" }) {
                    Text("9")
                }

                Button(onClick = {
                    op.value='*'
                    buf.intValue = s.value.toInt()
                    s.value="0"
                }) {
                    Text("*")
                }
            }

            Row{

                Button(onClick = { s.value="0"; buf.intValue=0 }) {
                    Text("C")
                }

                Button(
                    onClick = {
                        val current = s.value.toInt()
                        val result = when(op.value){
                            '+' -> buf.intValue + current
                            '-' -> buf.intValue - current
                            '*' -> buf.intValue * current
                            else -> current
                        }
                        s.value = result.toString()
                        buf.intValue = result
                    },
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text("=")
                }
            }
        }
    }
}