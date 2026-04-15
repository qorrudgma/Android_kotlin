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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // qr스캔
    private var onQrScannedCallback: ((String) -> Unit)? = null
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            Log.d("QR_RESULT", "읽은 QR 값: ${result.contents}")
            onQrScannedCallback?.invoke(result.contents)
        }
    }

    fun startQrScan() {
        val options = ScanOptions().apply {
            setPrompt("QR 코드를 스캔하세요")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }

        qrLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                MainScreen(
                    onLogoutClick = {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSettingClick ={
                        val intent = Intent(this@MainActivity, DetailActivity::class.java)
                        startActivity(intent)
                    },
                    onQrClick = {
                        startQrScan()
                    },
                    onQrScanned = { callback ->
                        onQrScannedCallback = callback
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
        MainScreen(
            onLogoutClick = {},
            onSettingClick = {},
            onQrClick = {},
            onQrScanned = {}
        )
    }
}

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit,
    onSettingClick: () -> Unit,
    onQrClick: () -> Unit,
    onQrScanned: ((String) -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()

    var alcOptions by remember { mutableStateOf(listOf<String>()) }
    var materialOptions by remember { mutableStateOf(listOf<String>()) }
    var supplierOptions by remember { mutableStateOf(listOf<String>()) }
    var processOptions by remember { mutableStateOf(listOf<String>()) }
    var operaterOptions by remember { mutableStateOf(listOf<String>()) }
    var OptionsOne by remember { mutableStateOf(listOf<String>()) }
    var OptionsTwo by remember { mutableStateOf(listOf<String>()) }

    var materialStatus by remember { mutableStateOf("") }

    var selectedAlcCode by remember { mutableStateOf("") }
    var selectedMaterialNo by remember { mutableStateOf("") }
    var selectedSupplier by remember { mutableStateOf("") }
    var selectedProcess by remember { mutableStateOf("") }
    var selectedDefectReason by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf("") }

    var showRegisterDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    var defectReasonFromServer by remember { mutableStateOf("") }
    var defectOperaterFromServer by remember { mutableStateOf("") }

    val defectReasonOptions = listOf(
        "스크래치",
        "오염",
        "변형",
        "파손",
        "기타"
    )

    // 초기 옵션 api
    suspend fun loadInitialOptions() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "http://10.0.2.2:7237/api/MaterialsControllers/options"
                Log.d("OPTION_API", "옵션 불러오기 시작")

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

    // 옵션 선택시 개수 줄이는 api
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

    // 불량등록 하는 api
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

    // 불량 상태 확인하는 api
    suspend fun statusCheckApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "http://10.0.2.2:7237/api/MaterialsControllers/status-check"

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
//            val id = json.getInt("id")
            val status = json.getBoolean("status")
            defectReasonFromServer = json.optString("reason", "")
            defectOperaterFromServer = json.optString("name", "")
            Log.e("STATUS_API", "전체 응답 JSON => $result")
            Log.e("STATUS_API", "defectReasonFromServer => $defectReasonFromServer")
            Log.e("STATUS_API", "defectOperaterFromServer => $defectOperaterFromServer")

            materialStatus =
                if (status) "불량" else "정상"

        } catch (e: Exception) {
            Log.e("STATUS_API", "상태 조회 오류", e)
            materialStatus = "조회 실패"
        }
    }

    // QR 읽어서 처리하기
    suspend fun readQR(qrValue: String) {
        try {
            Log.d("QR_readQR", "qrValue => $qrValue")

            val parts = qrValue.split("<GS>")
            val valuesAfterGs = parts.drop(1)

            for (item in valuesAfterGs) {

                if (item.isEmpty()) continue

                val firstChar = item.first()
                val remainingText = item.drop(1)

                when (firstChar) {
                    'S' -> {
                        Log.d("QR_readQR", "ALC코드 => $remainingText")
                        selectedAlcCode = remainingText
                    }

                    'P' -> {
                        Log.d("QR_readQR", "자재번호 => $remainingText")
                        selectedMaterialNo = remainingText
                    }

                    'V' -> {
                        Log.d("QR_readQR", "공급업체 => $remainingText")
                        selectedSupplier = remainingText
                    }

                    else -> {
                        Log.d("QR_readQR", "알 수 없는 타입 => $item")
                    }
                }
            }
            selectedProcess = ""

            scope.launch {
                loadInitialOptions()
            }
        } catch (e: Exception) {
            Log.e("QR_PARSE", "QR 파싱 오류", e)
        }
    }

    // 불량 등록취소 api
    suspend fun cancelDefectApi() {
        try {
            val result = withContext(Dispatchers.IO) {
                val apiUrl =
                    "http://10.0.2.2:7237/api/MaterialsControllers/cancel-defect"

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
                    "불량 취소가 완료되었습니다."
                )
            showResultDialog = true

        } catch (e: Exception) {
            Log.e("REGISTER_API", "등록 오류", e)
            resultMessage = "불량 취소 실패"
            showResultDialog = true
        }
    }

    LaunchedEffect(Unit) {
        loadInitialOptions()

        onQrScanned { qrValue ->
            scope.launch {
                readQR(qrValue)
            }
        }
    }

    // 등록이나 취소 시 내용 확인하는 팝업 창
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
                    buildAnnotatedString {
                        append("ALC 코드: $selectedAlcCode\n")
                        append("자재번호: $selectedMaterialNo\n")
                        append("공급업체: $selectedSupplier\n")
                        append("공정: $selectedProcess\n")

                        if (materialStatus == "불량") {
                            append("불량 사유: ")
                            withStyle(
                                style = SpanStyle(color = Color.Red)
                            ) {
                                append("$defectReasonFromServer\n")
                            }
                            append("담당자: ")
                            withStyle(
                                style = SpanStyle(color = Color.Red)
                            ) {
                                append("$defectOperaterFromServer\n")
                            }
                        } else {
                            append("불량 사유: $selectedDefectReason\n")
                            append("담당자: $selectedOperator\n")
                        }

                        append("상태: ")

                        withStyle(
                            style = SpanStyle(
                                color = if (materialStatus == "불량")
                                    Color.Red
                                else
                                    Color(0xFF006400)
                            )
                        ) {
                            append(materialStatus)
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegisterDialog = false
                        scope.launch {
                            if (materialStatus == "불량") {
                                // 불량일 때 취소 처리
                                cancelDefectApi()
                            } else {
                                // 정상일 때 등록 처리
                                registerDefectApi()
                            }
                        }
                    }
                ) {
                    val isDefect = materialStatus == "불량"

                    Text(
                        text = if (isDefect) "불량 취소하기" else "불량 등록하기",
                        color = if (isDefect) Color.Red else Color(0xFF006400)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRegisterDialog = false
                    }
                ) {
                    Text("뒤로가기")
                }
            }
        )
    }

    // 기본 화면
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F8))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LogoutSection(
                onLogoutClick = onLogoutClick,
                onSettingClick = onSettingClick
            )

            HeaderSection()

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
                    when {
                        selectedAlcCode.isBlank() -> {
                            resultMessage = "ALC 코드를 선택하세요."
                            showResultDialog = true
                        }

                        selectedMaterialNo.isBlank() -> {
                            resultMessage = "자재번호를 선택하세요."
                            showResultDialog = true
                        }

                        selectedSupplier.isBlank() -> {
                            resultMessage = "공급업체를 선택하세요."
                            showResultDialog = true
                        }

                        selectedProcess.isBlank() -> {
                            resultMessage = "실장착공정을 선택하세요."
                            showResultDialog = true
                        }

                        selectedDefectReason.isBlank() -> {
                            resultMessage = "불량 사유를 선택하세요."
                            showResultDialog = true
                        }

                        selectedOperator.isBlank() -> {
                            resultMessage = "담당자를 선택하세요."
                            showResultDialog = true
                        }

                        else -> {
                            scope.launch {
                                statusCheckApi()
                                showRegisterDialog = true
                            }
                        }
                    }
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

        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = {
                    showResultDialog = false
                },
                title = {
                    Text("선택해주세요.")
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

        FloatingActionButton(
            onClick = {
                Log.d("QR_DEBUG", "QR 스캔 버튼 클릭")
                onQrClick()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFF191970)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "QR Scan",
                tint = Color.White
            )
        }
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

//@Composable
//fun LogoutSection(
//    onLogoutClick: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 16.dp),
//        contentAlignment = Alignment.CenterStart
//    ) {
//        Button(
//            onClick = { onLogoutClick() },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF191970),
//                contentColor = Color.White
//            )
//        ) {
//            Text("로그아웃")
//        }
//    }
//}

@Composable
fun LogoutSection(
    onLogoutClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 왼쪽 로그아웃
        Button(
            onClick = { onLogoutClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191970),
                contentColor = Color.White
            )
        ) {
            Text("로그아웃")
        }

        // 오른쪽 설정 아이콘
        IconButton(
            onClick = { onSettingClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = Color(0xFF191970)
            )
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
    var query by remember(selectedValue) {
        mutableStateOf(selectedValue)
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
            if (query.isBlank()) {
                if (options.size > 100) {
                    options.take(50)
                } else {
                    options
                }
            } else {
                options.filter {
                    it.contains(query, ignoreCase = true)
                }
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