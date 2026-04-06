package com.example.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                MainScreen(
                    onLogoutClick = {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
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
fun MainScreenPreview() {
    AndroidTheme {
        MainScreen(
            onLogoutClick = {}
        )
    }
}

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var alcOptions by remember { mutableStateOf(listOf<String>()) }
    var materialOptions by remember { mutableStateOf(listOf<String>()) }
    var supplierOptions by remember { mutableStateOf(listOf<String>()) }
    var processOptions by remember { mutableStateOf(listOf<String>()) }
    var operaterOptions by remember { mutableStateOf(listOf<String>()) }

    var selectedAlcCode by remember { mutableStateOf("") }
    var selectedMaterialNo by remember { mutableStateOf("") }
    var selectedSupplier by remember { mutableStateOf("") }
    var selectedProcess by remember { mutableStateOf("") }
    var selectedDefectReason by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf("") }

    var showRegisterDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // 불량 사유는 일단 예시값
    val defectReasonOptions = listOf(
        "스크래치",
        "오염",
        "변형",
        "파손",
        "기타"
    )

    suspend fun registerDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl = "http://10.0.2.2:7237/api/MaterialsControllers/register-defect"

                Log.d("API_TEST", "호출 URL: $apiUrl")

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                    val requestJson = JSONObject().apply {
                        put("AlcCode", selectedAlcCode)
                        put("MaterialNo", selectedMaterialNo)
                        put("Supplier", selectedSupplier)
                        put("Process", selectedProcess)
                        put("DefectReason", selectedDefectReason)
                        put("Operator", selectedOperator)
                    }

                    Log.d("API_TEST", "등록 요청값: $requestJson")

                    connection.outputStream.use { outputStream ->
                        outputStream.write(requestJson.toString().toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }

                    val responseCode = connection.responseCode
                    Log.d("API_TEST", "응답 코드: $responseCode")

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

            Log.d("API_TEST", "등록 응답값: $result")

            val json = try {
                JSONObject(result)
            } catch (e: Exception) {
                null
            }

            resultMessage = json?.optString("message", "불량 등록이 완료되었습니다.") ?: "불량 등록이 완료되었습니다."
            showResultDialog = true

        } catch (e: Exception) {
            Log.e("API_TEST", "등록 에러", e)
            resultMessage = "불량 등록 중 오류가 발생했습니다.\n${e.message}"
            showResultDialog = true
        }
    }

    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl = "http://10.0.2.2:7237/api/MaterialsControllers/options"

                Log.d("API_TEST", "호출 URL: $apiUrl")

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val responseCode = connection.responseCode
                    Log.d("API_TEST", "응답 코드: $responseCode")

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

            Log.d("API_TEST", "응답값: $result")

            val json = JSONObject(result)

            val alcArray = json.getJSONArray("alcCodeList")
            val materialArray = json.getJSONArray("materialNoList")
            val supplierArray = json.getJSONArray("supplierList")
            val processArray = json.getJSONArray("processList")
            val operaterArray = json.getJSONArray("operaterList")

            alcOptions = List(alcArray.length()) { index -> alcArray.getString(index) }
            materialOptions = List(materialArray.length()) { index -> materialArray.getString(index) }
            supplierOptions = List(supplierArray.length()) { index -> supplierArray.getString(index) }
            processOptions = List(processArray.length()) { index -> processArray.getString(index) }
            operaterOptions = List(operaterArray.length()) { index -> operaterArray.getString(index) }

        } catch (e: Exception) {
            Log.e("API_TEST", "조회 에러", e)
        } finally {
            isLoading = false
        }
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = {
                Text("등록 확인")
            },
            text = {
                Text(
                    "선택한 항목으로 불량 등록을 진행합니다.\n\n" +
                            "ALC 코드: $selectedAlcCode\n" +
                            "자재번호: $selectedMaterialNo\n" +
                            "공급업체: $selectedSupplier\n" +
                            "실장착공정: $selectedProcess\n" +
//                            "이름: $selectedNameReason\n" +
                            "불량 사유: $selectedDefectReason\n" +
                            "담당자: $selectedOperator"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegisterDialog = false
                        scope.launch {
                            registerDefectApi()
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRegisterDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = {
                Text("결과")
            },
            text = {
                Text(resultMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showResultDialog = false }
                ) {
                    Text("확인")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderSection()

        LogoutSection(
            onLogoutClick = onLogoutClick
        )

//        if (isLoading) {
//            Text(
//                text = "데이터 불러오는 중...",
//                fontSize = 16.sp,
//                modifier = Modifier.padding(vertical = 16.dp)
//            )
//        } else {
        FilterSection(
            alcOptions = alcOptions,
            materialOptions = materialOptions,
            supplierOptions = supplierOptions,
            processOptions = processOptions,
            defectOptions = defectReasonOptions,
            operaterOptions = operaterOptions,

            selectedAlcCode = selectedAlcCode,
            onSelectedAlcCodeChange = { selectedAlcCode = it },

            selectedMaterialNo = selectedMaterialNo,
            onSelectedMaterialNoChange = { selectedMaterialNo = it },

            selectedSupplier = selectedSupplier,
            onSelectedSupplierChange = { selectedSupplier = it },

            selectedProcess = selectedProcess,
            onSelectedProcessChange = { selectedProcess = it },

            selectedDefectReason = selectedDefectReason,
            onSelectedDefectReasonChange = { selectedDefectReason = it },

            selectedOperator = selectedOperator,
            onSelectedOperatorChange = { selectedOperator = it }
        )

        ButtonSection(
            onRegisterClick = {
                showRegisterDialog = true
            },
            onResetClick = {
                selectedAlcCode = ""
                selectedMaterialNo = ""
                selectedSupplier = ""
                selectedProcess = ""
                selectedDefectReason = ""
            }
        )
    }
//    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "제품 불량 전산 시스템",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LogoutSection(
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(
            onClick = { onLogoutClick() }
        ) {
            Text("로그아웃")
        }
    }
}

@Composable
fun FilterSection(
    alcOptions: List<String>,
    materialOptions: List<String>,
    supplierOptions: List<String>,
    processOptions: List<String>,
    defectOptions: List<String>,
    operaterOptions: List<String>,

    selectedAlcCode: String,
    onSelectedAlcCodeChange: (String) -> Unit,

    selectedMaterialNo: String,
    onSelectedMaterialNoChange: (String) -> Unit,

    selectedSupplier: String,
    onSelectedSupplierChange: (String) -> Unit,

    selectedProcess: String,
    onSelectedProcessChange: (String) -> Unit,

    selectedDefectReason: String,
    onSelectedDefectReasonChange: (String) -> Unit,

    selectedOperator: String,
    onSelectedOperatorChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchableDropdownField(
            label = "ALC 코드",
            options = alcOptions,
            selectedValue = selectedAlcCode,
            onValueSelected = onSelectedAlcCodeChange
        )

        SearchableDropdownField(
            label = "자재번호",
            options = materialOptions,
            selectedValue = selectedMaterialNo,
            onValueSelected = onSelectedMaterialNoChange
        )

        SearchableDropdownField(
            label = "공급업체",
            options = supplierOptions,
            selectedValue = selectedSupplier,
            onValueSelected = onSelectedSupplierChange
        )

        SearchableDropdownField(
            label = "이름",
            options = defectOptions,
            selectedValue = selectedDefectReason,
            onValueSelected = onSelectedDefectReasonChange
        )

        SearchableDropdownField(
            label = "실장착공정",
            options = processOptions,
            selectedValue = selectedProcess,
            onValueSelected = onSelectedProcessChange
        )

        SearchableDropdownField(
            label = "불량 사유",
            options = defectOptions,
            selectedValue = selectedDefectReason,
            onValueSelected = onSelectedDefectReasonChange
        )

        SearchableDropdownField(
            label = "담당자",
            options = operaterOptions,
            selectedValue = selectedOperator,
            onValueSelected = onSelectedOperatorChange
        )

        SearchableDropdownField(
            label = "비고1",
            options = defectOptions,
            selectedValue = selectedDefectReason,
            onValueSelected = onSelectedDefectReasonChange
        )

        SearchableDropdownField(
            label = "비고2",
            options = defectOptions,
            selectedValue = selectedDefectReason,
            onValueSelected = onSelectedDefectReasonChange
        )

//        Text(
//            text = "불량",
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Red
//        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filteredOptions = remember(query, options) {
        if (query.isBlank()) {
            emptyList()
        } else {
            options
                .filter { it.contains(query, ignoreCase = true) }
                .take(50)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text("검색어를 입력하세요") },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            when {
                query.isBlank() -> {
                    DropdownMenuItem(
                        text = { Text("검색어를 입력하세요") },
                        onClick = { }
                    )
                }

                filteredOptions.isEmpty() -> {
                    DropdownMenuItem(
                        text = { Text("검색 결과 없음") },
                        onClick = { }
                    )
                }

                else -> {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueSelected(option)
                                query = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonSection(
    onRegisterClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { onRegisterClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191970),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "불량 등록",
                fontSize = 18.sp
            )
        }

        Button(
            onClick = { onResetClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E9E9E),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "리셋",
                fontSize = 18.sp
            )
        }
    }
}