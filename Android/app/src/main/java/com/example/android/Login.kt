package com.example.android

import android.util.Log
import android.content.Context
import java.net.HttpURLConnection
import kotlinx.coroutines.*
import java.net.URL
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.json.JSONObject
import androidx.core.content.edit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.widthIn

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
                    },
                    onSettingClick = {
                        val intent = Intent(this@LoginActivity, SettingActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSettingClick: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        val savedId = prefs.getString("saved_id", "") ?: ""
        val savedPw = prefs.getString("saved_pw", "") ?: ""
        val savedChecked = prefs.getBoolean("checked", false)

        if (savedChecked) {
            id = savedId
            password = savedPw
            checked = true
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
//        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { onSettingClick() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = Color(0xFF191970),
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 600.dp)
                .background(Color(0xFFF5F6F8))
                .padding(horizontal = 20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {


            LoginHeaderSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            LoginTitleSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            )

            LoginInputSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
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
                    scope.launch {
                        val success = loginApi(context, id, password)

                        if (success) {
                            errorMessage = ""

                            if (checked) {
                                prefs.edit {
                                    putString("saved_id", id)
                                    putString("saved_pw", password)
                                    putBoolean("checked", true)
                                }
                            } else {
                                prefs.edit {
                                    clear()
                                }
                            }

                            onLoginSuccess()
                        }
                    }
                },
                onSignupClick = {
                    errorMessage = "회원가입 기능은 아직 준비 중입니다."
                }
            )
        }
    }
}

suspend fun loginApi(
    context: Context,
    id: String,
    pw: String
): Boolean {
    return try {
        val result = withContext(Dispatchers.IO) {
            val apiUrl = "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/login"

            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val requestJson = JSONObject().apply {
                    put("id", id)
                    put("pw", pw)
                }

                connection.outputStream.use {
                    it.write(requestJson.toString().toByteArray())
                }

                val responseCode = connection.responseCode

                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                stream.bufferedReader().use { it.readText() }

            } finally {
                connection.disconnect()
            }
        }

        val json = JSONObject(result)
        json.optBoolean("success", false)

    } catch (e: Exception) {
        Log.e("LOGIN", "에러", e)
        false
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
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=1080px,height=2340px,dpi=420"
//    device = "spec:width=1200px,height=1920px,dpi=240"
//    device = "spec:width=800px,height=1280px,dpi=240"
)

@Composable
fun LoginScreenPreview() {
    AndroidTheme {
        LoginScreen(
            onLoginSuccess = {},
            onSettingClick = {}
        )
    }
}