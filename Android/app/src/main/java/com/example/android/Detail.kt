package com.example.android

import androidx.compose.ui.tooling.preview.Preview
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import android.content.Context
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import androidx.core.content.edit

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 샘플 데이터 (나중에 Intent 값으로 변경 가능)
        val alcCode = intent.getStringExtra("alcCode") ?: "없음"
        val materialNo = intent.getStringExtra("materialNo") ?: "없음"
        val supplier = intent.getStringExtra("supplier") ?: "없음"
        val process = intent.getStringExtra("process") ?: "없음"
        val defectReason = intent.getStringExtra("defectReason") ?: "없음"
        val operator = intent.getStringExtra("operator") ?: "없음"
        val status = intent.getStringExtra("status") ?: "없음"

        setContent {
            AndroidTheme {
                DetailScreen(
                    alcCode = alcCode,
                    materialNo = materialNo,
                    supplier = supplier,
                    process = process,
                    defectReason = defectReason,
                    operator = operator,
                    status = status,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun DetailScreen(
    alcCode: String,
    materialNo: String,
    supplier: String,
    process: String,
    defectReason: String,
    operator: String,
    status: String,
    onBackClick: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf(status) }
    var currentDefectReason by remember { mutableStateOf(defectReason) }
    var currentOperator by remember { mutableStateOf(operator) }

    val isDefect = currentStatus.contains("불량")
    var selectedDefectReason by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val defectOptions = listOf(
        "스크래치",
        "오염",
        "변형",
        "파손",
        "기타"
    )
    val scope = rememberCoroutineScope()

    var operaterOptions by remember {
        mutableStateOf(listOf<String>())
    }

    // 담당자 조회 api
    suspend fun operatorApi() {
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
            val operatorArray = json.getJSONArray("operatorList")

            operaterOptions =
                List(operatorArray.length()) { i ->
                    operatorArray.getString(i)
                }

            Log.d("OPERATOR_API", "담당자 목록: $operaterOptions")

        } catch (e: Exception) {
            Log.e("OPERATOR_API", "담당자 조회 오류", e)
        }
    }

    // 상세화면 조회 api
    suspend fun detailApi() {
        try {
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
                        put("AlcCode", alcCode)
                        put("MaterialNo", materialNo)
                        put("Supplier", supplier)
                        put("Process", process)
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

            val json = JSONObject(result)

            currentDefectReason =
                json.optString("defectReason", "")

            currentOperator =
                json.optString("operater", "")

            currentStatus =
                json.optString("status", "")

        } catch (e: Exception) {
            Log.e("DETAIL_API", "상세조회 오류", e)
        }
    }

    // 불량 등록 api
    suspend fun registerDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/register-defect"

                val url = URL(apiUrl)
                val connection =
                    url.openConnection() as HttpURLConnection

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
                        put("AlcCode", alcCode)
                        put("MaterialNo", materialNo)
                        put("Supplier", supplier)
                        put("Process", process)
                        put("DefectReason", selectedDefectReason)
                        put("Operator", selectedOperator)
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

            val json = JSONObject(result)

            resultMessage =
                json.optString(
                    "message",
                    "불량 등록 완료"
                )
            prefs.edit {
                putString("saved_operator", selectedOperator)
            }

            detailApi()
            showResultDialog = true
        } catch (e: Exception) {
            Log.e("REGISTER_API", "등록 오류", e)
            resultMessage = "불량 등록 실패"

            detailApi()
            showResultDialog = true
        }
    }

    // 불량 등록 취소 api
    suspend fun cancelDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/cancel-defect"

                val url = URL(apiUrl)
                val connection =
                    url.openConnection() as HttpURLConnection

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
                        put("AlcCode", alcCode)
                        put("MaterialNo", materialNo)
                        put("Supplier", supplier)
                        put("Process", process)
                        put("DefectReason", "정상")
                        put("Operator", selectedOperator)
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

            val json = JSONObject(result)

            resultMessage =
                json.optString(
                    "message",
                    "불량 취소 완료"
                )

            prefs.edit {
                putString("saved_operator", selectedOperator)
            }

            scope.launch {
                detailApi()
            }
            showResultDialog = true
        } catch (e: Exception) {
            Log.e("CANCEL_API", "취소 오류", e)
            resultMessage = "불량 취소 실패"
            scope.launch {
                detailApi()
            }
            showResultDialog = true
        }
    }

    // 불량 수정 api
    suspend fun updateDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {

                val apiUrl =
                    "${ApiSettings.getBaseUrl(context)}/api/MaterialsControllers/update-defect"

                val url = URL(apiUrl)
                val connection =
                    url.openConnection() as HttpURLConnection

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
                        put("AlcCode", alcCode)
                        put("MaterialNo", materialNo)
                        put("Supplier", supplier)
                        put("Process", process)
                        put("DefectReason", selectedDefectReason)
                        put("Operator", selectedOperator)
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

            val json = JSONObject(result)

            resultMessage =
                json.optString(
                    "message",
                    "수정 완료"
                )

            prefs.edit {
                putString("saved_operator", selectedOperator)
            }

            // 최신 데이터 다시 조회
            detailApi()

            showResultDialog = true

        } catch (e: Exception) {
            Log.e("UPDATE_API", "수정 오류", e)

            resultMessage = "수정 실패"

            detailApi()
            showResultDialog = true
        }
    }

    LaunchedEffect(Unit) {
        operatorApi()
        selectedOperator =
            prefs.getString("saved_operator", "") ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 뒤로가기 + 제목
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
                    text = "상세 화면",
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
                        selectedDefectReason = ""
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

            if (!isDefect || isEditMode) {
                SearchableDropdownField(
                    label = "불량 사유",
                    options = defectOptions,
                    selectedValue = selectedDefectReason,
                    onValueSelected = {
                        selectedDefectReason = it
                    }
                )
            }

            SearchableDropdownField(
                label = "담당자",
                options = operaterOptions,
                selectedValue = selectedOperator,
                onValueSelected = {
                    selectedOperator = it

                    prefs.edit {
                        putString("saved_operator", it)
                    }
                }
            )

            // 상세 카드
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 30.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                        ) {
                            DetailItem("ALC코드", alcCode)
                            DetailItem("공급업체", supplier)
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 30.dp)
                        ) {
                            DetailItem("자재번호", materialNo)
                            DetailItem("실장착공정", process)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDefect) {
                            DetailItem("담당자", currentOperator)
                            DetailItem("불량사유", currentDefectReason, Color.Red)
                            DetailItem("상태", currentStatus, Color.Red)
                        } else{
                            DetailItem("상태", currentStatus, Color(0xFF006400))
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 하단 버튼
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 뒤로가기/불량취소
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 뒤로가기
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("뒤로가기")
                    }

                    if (isDefect) {
                        // 불량 취소
                        Button(
                            onClick = {
                                if (!isEditMode) {
                                    scope.launch {
                                        cancelDefectApi()
                                    }
                                }
                            },
                            enabled = !isEditMode,   // 클릭 차단
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (isEditMode) Color(0xFF6A6AD9)
                                    else Color(0xFF191970)
                            )
                        ) {
                            Text(
                                "불량 취소",
                                color = if (isEditMode) Color(0xFFE0E0E0) else Color.White
                            )
                        }
                    } else {
                        // 정상일 때는 불량 등록
                        Button(
                            onClick = {
                                if (selectedDefectReason.isBlank()) {
                                    resultMessage = "불량 사유를 선택하세요."
                                    showResultDialog = true
                                    return@Button
                                }

                                if (selectedOperator.isBlank()) {
                                    resultMessage = "담당자를 선택하세요."
                                    showResultDialog = true
                                    return@Button
                                }

                                scope.launch {
                                    registerDefectApi()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF191970)
                            )
                        ) {
                            Text("불량 등록")
                        }
                    }
                }

                // 수정하기
                if (isDefect) {
                    Button(
                        onClick = {
                            if (!isEditMode) {
                                // 수정모드 진입
                                isEditMode = true

                                selectedDefectReason = ""

                            } else {
                                // 👉 수정 완료

                                if (selectedDefectReason.isBlank()) {
                                    resultMessage = "불량 사유를 선택하세요."
                                    showResultDialog = true
                                    return@Button
                                }

                                if (selectedOperator.isBlank()) {
                                    resultMessage = "담당자를 선택하세요."
                                    showResultDialog = true
                                    return@Button
                                }

                                scope.launch {
                                    updateDefectApi()
                                    isEditMode = false
                                    detailApi()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF191970)
                        )
                    ) {
                        Text(if (isEditMode) "수정 완료" else "수정하기")
                    }
                }
            }


            if (showResultDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showResultDialog = false
                    },
                    title = {
                        Text("알림")
                    },
                    text = {
                        Text(resultMessage)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showResultDialog = false
                            }
                        ) {
                            Text("확인")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
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
fun DetailScreenPreview() {
    AndroidTheme {
        DetailScreen(
            alcCode = "S00",
            materialNo = "05203-SW000",
            supplier = "S994",
            process = "실장착1",
            defectReason = "스크래치",
            operator = "홍길동",
            status = "불량",
            onBackClick = {}
        )
    }
}