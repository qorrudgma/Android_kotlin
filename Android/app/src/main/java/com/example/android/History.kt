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
import androidx.compose.material.icons.filled.Refresh
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
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Search
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
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var selectedOperator by remember { mutableStateOf("") }
    var operatorOptions by remember { mutableStateOf(listOf<String>()) }
    var historyList by remember { mutableStateOf(listOf<HistoryItem>()) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    fun getToday(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(java.util.Date())
    }

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
        Log.d("HISTORY_API", selectedDate)
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
                        put("Date", selectedDate)
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

    // 디테일 가기전 조회
    suspend fun detailApiFromHistory(item: HistoryItem): JSONObject? {
        return try {
            Log.d("DETAIL_API", "History DetailApi")

            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/detail"

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
                        put("AlcCode", item.alcCode)
                        put("MaterialNo", item.materialNo)
                        put("Supplier", item.supplier)
                        put("Process", item.process)
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

            JSONObject(result)

        } catch (e: Exception) {
            Log.e("DETAIL_API", "History 상세조회 오류", e)
            null
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                            selectedDate = sdf.format(java.util.Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(Unit) {
        loadOperatorList()
        selectedDate = getToday()
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
                    .padding(top = 30.dp),
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

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        selectedOperator = ""
                        historyList = listOf()
                        selectedDate = getToday()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "초기화",
                        tint = Color(0xFF191970),
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            // 날짜 선택
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("날짜 선택") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "날짜 선택"
                        )
                    }
                }
            )

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
                            dialogMessage = "$selectedDate \n $selectedOperator 님의 작업 이력이 없습니다."
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
                        HistoryCard(
                            item = item,
                            onClick = {
                                scope.launch {

                                    val json = detailApiFromHistory(item)

                                    if (json != null) {
                                        val intent = Intent(context, DetailActivity::class.java).apply {
                                            putExtra("id", json.getInt("id"))
                                            putExtra("alcCode", item.alcCode)
                                            putExtra("materialNo", item.materialNo)
                                            putExtra("supplier", item.supplier)
                                            putExtra("process", item.process)
                                            putExtra("defectReason", json.optString("defectReason"))
                                            putExtra("operator", json.optString("operater"))
                                            putExtra("status", json.optString("status"))
                                        }

                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
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
fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
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

                val statusColor = when (item.status) {
                    "정상 등록" -> Color(0xFF006400)
                    "불량 등록" -> Color.Red
                    "불량 수정" -> Color(0xFFFF8C00)
                    else -> Color.Black
                }

                Text(
                    text = item.status,
                    fontSize = 18.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("ALC 코드: ${item.alcCode}")
            Text("자재번호: ${item.materialNo}")
            Text("공급업체: ${item.supplier}")
            Text("공정: ${item.process}")

            if (item.status == "불량 등록" || item.status == "불량 수정") {
                Text(
                    text = "불량사유: ${item.defectReason}",
                    color = Color.Red
                )
            }

            Text("작업시간: ${item.createdAt}")
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
fun HistoryScreenPreview() {
    AndroidTheme {
        HistoryScreen(
            onBackClick = {}
        )
    }
}