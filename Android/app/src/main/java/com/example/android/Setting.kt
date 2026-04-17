package com.example.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                SettingScreen(
                    onBackClick = {
                        finish()
                    },onLogoutClick = {
                        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit { remove("saved_operator") }

                        val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var ip by remember {
        mutableStateOf(ApiSettings.getIp(context))
    }
    var port by remember {
        mutableStateOf(ApiSettings.getPort(context))
    }
    var showDialog by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 뒤로가기 + 제목
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }

                Text(
                    text = "설정 화면",
                    fontSize = 24.sp
                )
            }

            // IP 입력창
            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                label = {
                    Text("IP")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Port 입력창
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = {
                    Text("Port")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        ApiSettings.saveServerInfo(context, ip, port)
                        showDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF191970)
                    )
                ) {
                    Text("적용")
                }

                Button(
                    onClick = { onLogoutClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("로그아웃")
                }
            }

        }

        // 적용 완료 팝업
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text("알림")
                },
                text = {
                    Text("적용되었습니다.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            onLogoutClick()
                        }
                    ) {
                        Text("확인")
                    }
                }
            )
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
fun SettingScreenPreview() {
    AndroidTheme {
        SettingScreen(
            onBackClick = {},
            onLogoutClick = {}
        )
    }
}