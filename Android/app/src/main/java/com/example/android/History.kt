package com.example.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.text.font.FontWeight

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                HistoryScreen(
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

data class HistoryItem(
    val alcCode: String,
    val materialNo: String,
    val supplier: String,
    val process: String,
    val status: String,
    val createdAt: String,
    val operator: String,
    val defectReason: String
)

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    previewData: List<HistoryItem>? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var selectedOperator by remember { mutableStateOf("") }
    var operatorOptions by remember { mutableStateOf(listOf<String>()) }
    var historyList by remember { mutableStateOf(listOf<HistoryItem>()) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 담당자 목록 API
    suspend fun loadOperatorList() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/operator"

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    connection.inputStream.bufferedReader().use {
                        it.readText()
                    }
                } finally {
                    connection.disconnect()
                }
            }

            val json = JSONObject(result)
            val arr = json.getJSONArray("operatorList")

            operatorOptions =
                List(arr.length()) { i ->
                    arr.getString(i)
                }

        } catch (e: Exception) {
            Log.e("HISTORY_OPERATOR", "담당자 조회 오류", e)
        }
    }

    // 이력 조회 API
    suspend fun loadHistory() {
        Log.d("HISTORY_API", "loadHistory 시작")
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/operator-history"

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.doOutput = true
                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )

                    val requestJson = JSONObject().apply {
                        put("Name", selectedOperator)
                    }

                    connection.outputStream.use {
                        it.write(
                            requestJson.toString()
                                .toByteArray(Charsets.UTF_8)
                        )
                    }

                    connection.inputStream.bufferedReader().use {
                        it.readText()
                    }

                } finally {
                    connection.disconnect()
                }
            }

            val jsonArray = JSONArray(result)

            historyList =
                List(jsonArray.length()) { i ->
                    val item = jsonArray.getJSONObject(i)

                    HistoryItem(
                        alcCode = item.optString("alcCode"),
                        materialNo = item.optString("materialNo"),
                        supplier = item.optString("supplier"),
                        process = item.optString("process"),
                        status = item.optString("status"),
                        createdAt = item.optString("createdAt"),
                        operator = item.optString("operater"),
                        defectReason = item.optString("defectReason")
                    )
                }

        } catch (e: Exception) {
            Log.e("HISTORY_API", "이력 조회 오류", e)
        }
    }

    LaunchedEffect(Unit) {
        loadOperatorList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp, top = 30.dp),
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
                    text = "작업 이력 조회",
                    fontSize = 24.sp
                )
            }

            // 담당자 선택
            SearchableDropdownField(
                label = "담당자",
                options = operatorOptions,
                selectedValue = selectedOperator,
                onValueSelected = {
                    selectedOperator = it
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 조회 버튼
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (selectedOperator.isBlank()) {
                        dialogMessage = "담당자를 선택하세요."
                        showDialog = true
                        return@Button
                    }

                    scope.launch {
                        loadHistory()

                        if (historyList.isEmpty()) {
                            dialogMessage = "오늘 작업 이력이 없습니다."
                            showDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF191970)
                )
            ) {
                Text("조회")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 결과 리스트
            if (historyList.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { item ->
                        HistoryCard(item)
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text("알림")
                },
                text = {
                    Text(dialogMessage)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryCard(item: HistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.operator} / ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.status,
                    fontSize = 18.sp,
                    color =
                        if (item.status == "정상 등록") {
                            Color(0xFF006400)
                        } else {
                            Color.Red
                        },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text("ALC 코드: ${item.alcCode}")
            Text("자재번호: ${item.materialNo}")
            Text("공급업체: ${item.supplier}")
            Text("공정: ${item.process}")
            if (item.status == "불량 등록") {
                Text("불량사유: ${item.defectReason}")
            }
            Text("작업시간: ${item.createdAt}")
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=1080px,height=2340px,dpi=420"
)
@Composable
fun HistoryScreenPreview() {
    AndroidTheme {

        val sampleList = listOf(
            HistoryItem(
                alcCode = "S00",
                materialNo = "05203-SW000",
                supplier = "S994",
                process = "실장착1",
                status = "불량 등록",
                createdAt = "2026-04-16 09:30:21",
                operator = "담당1",
                defectReason = "스크래치"
            )
        )

        HistoryScreen(
            onBackClick = {},
            previewData = sampleList   // 👈 핵심
        )
    }
}