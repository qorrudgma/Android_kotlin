package com.example.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    AndroidTheme {
        MainScreen(onLogoutClick = {})
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

    val defectReasonOptions = listOf(
        "스크래치",
        "오염",
        "변형",
        "파손",
        "기타"
    )

    suspend fun loadInitialOptions() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "http://10.0.2.2:7237/api/MaterialsControllers/options"

                val url = URL(apiUrl)
                val connection =
                    url.openConnection() as HttpURLConnection

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

            val alcArray = json.getJSONArray("alcCodeList")
            val materialArray = json.getJSONArray("materialNoList")
            val supplierArray = json.getJSONArray("supplierList")
            val processArray = json.getJSONArray("processList")
            val operaterArray = json.getJSONArray("operaterList")

            alcOptions =
                List(alcArray.length()) { i ->
                    alcArray.getString(i)
                }

            materialOptions =
                List(materialArray.length()) { i ->
                    materialArray.getString(i)
                }

            supplierOptions =
                List(supplierArray.length()) { i ->
                    supplierArray.getString(i)
                }

            processOptions =
                List(processArray.length()) { i ->
                    processArray.getString(i)
                }

            operaterOptions =
                List(operaterArray.length()) { i ->
                    operaterArray.getString(i)
                }

        } catch (e: Exception) {
            Log.e("INIT_API", "초기 로딩 오류", e)
        }
    }

    suspend fun loadFilteredOptions(
        alcCode: String,
        materialNo: String,
        supplier: String,
        process: String
    ): JSONObject {
        return withContext(Dispatchers.IO) {

            val apiUrl =
                "http://10.0.2.2:7237/api/MaterialsControllers/filtered-options" +
                        "?alcCode=$alcCode" +
                        "&materialNo=$materialNo" +
                        "&supplier=$supplier" +
                        "&process=$process"

            Log.d("FILTER_API", apiUrl)

            val url = URL(apiUrl)
            val connection =
                url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val result =
                    connection.inputStream.bufferedReader().use {
                        it.readText()
                    }

                JSONObject(result)

            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun registerDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "http://10.0.2.2:7237/api/MaterialsControllers/register-defect"

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
                        put("AlcCode", selectedAlcCode)
                        put("MaterialNo", selectedMaterialNo)
                        put("Supplier", selectedSupplier)
                        put("Process", selectedProcess)
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
                    "불량 등록이 완료되었습니다."
                )
            showResultDialog = true

        } catch (e: Exception) {
            Log.e("REGISTER_API", "등록 오류", e)
            resultMessage = "불량 등록 실패"
            showResultDialog = true
        }
    }

    LaunchedEffect(Unit) {
        loadInitialOptions()
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegisterDialog = false
            },
            title = {
                Text("등록 확인")
            },
            text = {
                Text(
                    "ALC 코드: $selectedAlcCode\n" +
                            "자재번호: $selectedMaterialNo\n" +
                            "공급업체: $selectedSupplier\n" +
                            "공정: $selectedProcess\n" +
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
                    onClick = {
                        showRegisterDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
            },
            title = {
                Text("결과")
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

        FilterSection(
            alcOptions = alcOptions,
            materialOptions = materialOptions,
            supplierOptions = supplierOptions,
            processOptions = processOptions,
            defectOptions = defectReasonOptions,
            operaterOptions = operaterOptions,

            selectedAlcCode = selectedAlcCode,
            onSelectedAlcCodeChange = {
                selectedAlcCode = it

                selectedMaterialNo = ""
                selectedSupplier = ""
                selectedProcess = ""

                scope.launch {
                    val json = loadFilteredOptions(
                        selectedAlcCode,
                        "",
                        "",
                        ""
                    )

                    val materialArray =
                        json.getJSONArray("materialNoList")
                    materialOptions =
                        List(materialArray.length()) { index ->
                            materialArray.getString(index)
                        }

                    val supplierArray =
                        json.getJSONArray("supplierList")
                    supplierOptions =
                        List(supplierArray.length()) { index ->
                            supplierArray.getString(index)
                        }

                    val processArray =
                        json.getJSONArray("processList")
                    processOptions =
                        List(processArray.length()) { index ->
                            processArray.getString(index)
                        }
                }
            },

            selectedMaterialNo = selectedMaterialNo,
            onSelectedMaterialNoChange = {
                selectedMaterialNo = it

                selectedSupplier = ""
                selectedProcess = ""

                scope.launch {
                    val json = loadFilteredOptions(
                        selectedAlcCode,
                        selectedMaterialNo,
                        "",
                        ""
                    )

                    val supplierArray =
                        json.getJSONArray("supplierList")
                    supplierOptions =
                        List(supplierArray.length()) { index ->
                            supplierArray.getString(index)
                        }

                    val processArray =
                        json.getJSONArray("processList")
                    processOptions =
                        List(processArray.length()) { index ->
                            processArray.getString(index)
                        }
                }
            },

            selectedSupplier = selectedSupplier,
            onSelectedSupplierChange = {
                selectedSupplier = it

                selectedProcess = ""

                scope.launch {
                    val json = loadFilteredOptions(
                        selectedAlcCode,
                        selectedMaterialNo,
                        selectedSupplier,
                        ""
                    )

                    val processArray =
                        json.getJSONArray("processList")
                    processOptions =
                        List(processArray.length()) { index ->
                            processArray.getString(index)
                        }
                }
            },

            selectedProcess = selectedProcess,
            onSelectedProcessChange = {
                selectedProcess = it
            },

            selectedDefectReason = selectedDefectReason,
            onSelectedDefectReasonChange = {
                selectedDefectReason = it
            },

            selectedOperator = selectedOperator,
            onSelectedOperatorChange = {
                selectedOperator = it
            }
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
                selectedOperator = ""

                scope.launch {
                    loadInitialOptions()
                }
            }
        )
    }
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
            onClick = { onLogoutClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191970),
                contentColor = Color.White
            )
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
            options = operaterOptions,
            selectedValue = selectedOperator,
            onValueSelected = onSelectedOperatorChange
        )

        SearchableDropdownField(
            label = "비고2",
            options = operaterOptions,
            selectedValue = selectedOperator,
            onValueSelected = onSelectedOperatorChange
        )
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
    var query by remember { mutableStateOf(selectedValue) }

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
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        val filteredOptions =
            options.filter {
                it.contains(query, ignoreCase = true)
            }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        query = option
                        onValueSelected(option)
                        expanded = false
                    }
                )
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
                containerColor = Color(0xFF191970),
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