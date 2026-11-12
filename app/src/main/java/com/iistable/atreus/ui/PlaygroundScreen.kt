package com.iistable.atreus.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun PlaygroundScreen() {
    // Game State Variables
    var ballX by remember { mutableStateOf(500f) }
    var ballY by remember { mutableStateOf(500f) }
    var ballDX by remember { mutableStateOf(12f) }
    var ballDY by remember { mutableStateOf(12f) }
    
    var paddleLeftY by remember { mutableStateOf(400f) }
    var paddleRightY by remember { mutableStateOf(400f) }
    
    val paddleHeight = 200f
    val paddleWidth = 30f
    val ballRadius = 20f

    // The Game Loop (Runs automatically)
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 Frames per second
            
            // Move Ball
            ballX += ballDX
            ballY += ballDY

            // AI Logic: Right Paddle follows the ball
            if (ballX > 500f) {
                if (paddleRightY + paddleHeight / 2 < ballY) paddleRightY += 10f
                else if (paddleRightY + paddleHeight / 2 > ballY) paddleRightY -= 10f
            }
            
            // AI Logic: Left Paddle follows the ball
            if (ballX < 500f) {
                if (paddleLeftY + paddleHeight / 2 < ballY) paddleLeftY += 10f
                else if (paddleLeftY + paddleHeight / 2 > ballY) paddleLeftY -= 10f
            }

            // Bounce off Top/Bottom Walls
            if (ballY <= ballRadius || ballY >= 1000f - ballRadius) {
                ballDY = -ballDY
            }

            // Bounce off Left Paddle
            if (ballX <= 50f + paddleWidth && ballY >= paddleLeftY && ballY <= paddleLeftY + paddleHeight) {
                ballDX = -ballDX
                ballX = 50f + paddleWidth // Prevent sticking
            }

            // Bounce off Right Paddle
            if (ballX >= 950f - paddleWidth && ballY >= paddleRightY && ballY <= paddleRightY + paddleHeight) {
                ballDX = -ballDX
                ballX = 950f - paddleWidth // Prevent sticking
            }

            // Reset if ball goes out of bounds (Left)
            if (ballX < 0) {
                ballX = 500f; ballY = 500f; ballDX = 12f; ballDY = 12f
            }
            // Reset if ball goes out of bounds (Right)
            if (ballX > 1000f) {
                ballX = 500f; ballY = 500f; ballDX = -12f; ballDY = -12f
            }
        }
    }

    // The UI Renderer
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Draw Center Line
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(498f, 0f),
            size = Size(4f, 1000f)
        )

        // Draw Left Paddle
        drawRect(
            color = Color(0xFF00E676),
            topLeft = Offset(50f, paddleLeftY),
            size = Size(paddleWidth, paddleHeight)
        )

        // Draw Right Paddle
        drawRect(
            color = Color(0xFF00E676),
            topLeft = Offset(950f, paddleRightY),
            size = Size(paddleWidth, paddleHeight)
        )

        // Draw Ball
        drawCircle(
            color = Color.White,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )
    }
}
