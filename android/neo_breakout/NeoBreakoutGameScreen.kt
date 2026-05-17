package com.tjsanrjs.neobreakout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun NeoBreakoutGameScreen() {
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var combo by remember { mutableStateOf(0) }
    var paddleX by remember { mutableStateOf(340f) }

    val balls = remember { mutableStateListOf(Ball(400f, 400f, 4.5f, -5.5f, 9f, 0.08f)) }
    val bricks = remember {
        mutableStateListOf<Brick>().apply {
            for (r in 0 until 7) {
                for (c in 0 until 10) {
                    add(Brick(
                        x = c * 72f + 45f,
                        y = r * 28f + 80f,
                        hp = if (r < 3) 1 else 2,
                        color = if (r < 3) Color(0xFFFF0088) else Color(0xFF00AAFF)
                    ))
                }
            }
        }
    }
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(Unit) {
        while (true) {
            balls.forEach { ball ->
                ball.x += ball.dx
                ball.y += ball.dy
                ball.dy += ball.gravity

                if (ball.x - ball.radius < 0 || ball.x + ball.radius > 800f) ball.dx = -ball.dx
                if (ball.y - ball.radius < 0) ball.dy = -ball.dy

                if (ball.y + ball.radius > 550f && ball.y - ball.radius < 565f &&
                    ball.x > paddleX && ball.x < paddleX + 120f) {
                    ball.dy = -kotlin.math.abs(ball.dy) - 1
                    ball.dx = (ball.x - (paddleX + 60f)) * 0.2f
                    combo++
                }

                bricks.forEach { brick ->
                    if (brick.hp > 0 &&
                        ball.x > brick.x && ball.x < brick.x + 65f &&
                        ball.y > brick.y && ball.y < brick.y + 22f) {
                        
                        brick.hp--
                        if (brick.hp <= 0) {
                            score += 10 * (combo + 1)
                            repeat(12) {
                                particles.add(Particle(brick.x + 32f, brick.y + 11f, 
                                    Random.nextFloat()*8-4, Random.nextFloat()*8-6, 40))
                            }
                            if (Random.nextFloat() < 0.45f) {
                                bricks.forEach { other ->
                                    if (other.hp > 0 && Random.nextFloat() < 0.5f) other.hp = 0
                                }
                            }
                        }
                        ball.dy = -ball.dy
                    }
                }
            }

            particles.removeAll { it.life-- <= 0 }
            kotlinx.coroutines.delay(16)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("점수: $score | 볼: $lives | 콤보: ${combo}x", style = MaterialTheme.typography.headlineSmall)

        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            drawRect(Color.Cyan, topLeft = Offset(paddleX, 550f), size = androidx.compose.ui.geometry.Size(120f, 15f))
            balls.forEach { drawCircle(Color.Red, it.radius, Offset(it.x, it.y)) }
            bricks.forEach {
                if (it.hp > 0) drawRect(it.color, topLeft = Offset(it.x, it.y), size = androidx.compose.ui.geometry.Size(65f, 22f))
            }
        }

        Button(onClick = { /* Rewarded 광고 표시 */ }, modifier = Modifier.fillMaxWidth()) {
            Text("광고 보고 추가 볼 받기")
        }
    }
}

data class Ball(var x: Float, var y: Float, var dx: Float, var dy: Float, var radius: Float, var gravity: Float)
data class Brick(var x: Float, var y: Float, var hp: Int, var color: Color)
data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Int)
