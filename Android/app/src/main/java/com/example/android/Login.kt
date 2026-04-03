package com.example.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                LoginScreen(
                    onLoginSuccess = {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_5"
)

@Composable
fun LoginScreenPreview() {
    AndroidTheme {
        LoginScreen(
            onLoginSuccess = {}
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .padding(horizontal = 20.dp)
    ) {
        LoginHeaderSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        LoginTitleSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        )

        LoginInputSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            id = id,
            onIdChange = { id = it },
            password = password,
            onPasswordChange = { password = it },
            checked = checked,
            onCheckedChange = { checked = it }
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        BottomSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            onLoginClick = {
                if (id == "1234" && password == "1234") {
                    errorMessage = ""
                    onLoginSuccess()
                } else {
                    errorMessage = "아이디 또는 비밀번호가 틀렸습니다."
                }
            },
            onSignupClick = {
                errorMessage = "회원가입 기능은 아직 준비 중입니다."
            }
        )
    }
}

@Composable
fun LoginHeaderSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "불량제품 전산 시스템",
            fontSize = 28.sp
        )
    }
}

@Composable
fun LoginTitleSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "로그인 후 시스템을 이용하세요.",
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun LoginInputSection(
    modifier: Modifier = Modifier,
    id: String,
    onIdChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "로그인",
                    fontSize = 22.sp
                )

                OutlinedTextField(
                    value = id,
                    onValueChange = { onIdChange(it) },
                    label = { Text("아이디") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { onPasswordChange(it) },
                    label = { Text("비밀번호") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { onCheckedChange(it) }
                    )
                    Text("아이디/비밀번호 저장")
                }
            }
        }
    }
}

@Composable
fun BottomSection(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onLoginClick() },
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF191970),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "로그인",
                    fontSize = 18.sp
                )
            }

            Button(
                onClick = { onSignupClick() },
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "회원가입",
                    fontSize = 18.sp
                )
            }
        }
    }
}