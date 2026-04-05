package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                MainScreen(
                    onLogoutClick = {
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

data class DefectItem(
    val id: String,
    val alccode: String,
    val materialNo: String,
    val supplier: String,
    val name: String,
    val status: String
)

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit
) {
    val sampleList = listOf(
        DefectItem(
            id = "1",
            alccode = "S00",
            materialNo = "05203-SW000",
            supplier = "S994",
            name = "광성기업(주)",
            status = "0"
        ),
        DefectItem(
            id = "2",
            alccode = "S01",
            materialNo = "05203-SW010",
            supplier = "S994",
            name = "(주)한화미 의령공장",
            status = "0"
        ),
        DefectItem(
            id = "3",
            alccode = "S100",
            materialNo = "10189-04123",
            supplier = "E819",
            name = "Pneumatic Components Limited",
            status = "1"
        )
    )

    val options = listOf("1", "2", "3", "4", "5")

    var alcExpanded by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }
    var processExpanded by remember { mutableStateOf(false) }
    var defectExpanded by remember { mutableStateOf(false) }

    var selectedAlcCode by remember { mutableStateOf(options[0]) }
    var selectedMaterialNo by remember { mutableStateOf(options[0]) }
    var selectedSupplier by remember { mutableStateOf(options[0]) }
    var selectedProcess by remember { mutableStateOf(options[0]) }
    var selectedDefectReason by remember { mutableStateOf(options[0]) }

    var showRegisterDialog by remember { mutableStateOf(false) }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = {
                Text("등록 완료")
            },
            text = {
                Text(
                    "선택한 항목으로 불량 등록을 진행했습니다.\n" +
                            "ALC 코드: $selectedAlcCode\n" +
                            "자재번호: $selectedMaterialNo\n" +
                            "공급업체: $selectedSupplier\n" +
                            "실장착공정: $selectedProcess\n" +
                            "불량 사유: $selectedDefectReason"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showRegisterDialog = false }
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
            options = options,

            alcExpanded = alcExpanded,
            onAlcExpandedChange = { alcExpanded = it },
            selectedAlcCode = selectedAlcCode,
            onSelectedAlcCodeChange = { selectedAlcCode = it },

            materialExpanded = materialExpanded,
            onMaterialExpandedChange = { materialExpanded = it },
            selectedMaterialNo = selectedMaterialNo,
            onSelectedMaterialNoChange = { selectedMaterialNo = it },

            supplierExpanded = supplierExpanded,
            onSupplierExpandedChange = { supplierExpanded = it },
            selectedSupplier = selectedSupplier,
            onSelectedSupplierChange = { selectedSupplier = it },

            processExpanded = processExpanded,
            onProcessExpandedChange = { processExpanded = it },
            selectedProcess = selectedProcess,
            onSelectedProcessChange = { selectedProcess = it },

            defectExpanded = defectExpanded,
            onDefectExpandedChange = { defectExpanded = it },
            selectedDefectReason = selectedDefectReason,
            onSelectedDefectReasonChange = { selectedDefectReason = it }
        )

        ButtonSection(
            onRegisterClick = {
                showRegisterDialog = true
            },
            onResetClick = {
                selectedAlcCode = options[0]
                selectedMaterialNo = options[0]
                selectedSupplier = options[0]
                selectedProcess = options[0]
                selectedDefectReason = options[0]
            }
        )

//        TableSection(
//            items = sampleList
//        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onLogoutClick() }
        ) {
            Text("로그아웃")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    options: List<String>,

    alcExpanded: Boolean,
    onAlcExpandedChange: (Boolean) -> Unit,
    selectedAlcCode: String,
    onSelectedAlcCodeChange: (String) -> Unit,

    materialExpanded: Boolean,
    onMaterialExpandedChange: (Boolean) -> Unit,
    selectedMaterialNo: String,
    onSelectedMaterialNoChange: (String) -> Unit,

    supplierExpanded: Boolean,
    onSupplierExpandedChange: (Boolean) -> Unit,
    selectedSupplier: String,
    onSelectedSupplierChange: (String) -> Unit,

    processExpanded: Boolean,
    onProcessExpandedChange: (Boolean) -> Unit,
    selectedProcess: String,
    onSelectedProcessChange: (String) -> Unit,

    defectExpanded: Boolean,
    onDefectExpandedChange: (Boolean) -> Unit,
    selectedDefectReason: String,
    onSelectedDefectReasonChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DropdownField(
            label = "ALC 코드",
            options = options,
            expanded = alcExpanded,
            onExpandedChange = onAlcExpandedChange,
            selectedValue = selectedAlcCode,
            onValueSelected = onSelectedAlcCodeChange
        )

        DropdownField(
            label = "자재번호",
            options = options,
            expanded = materialExpanded,
            onExpandedChange = onMaterialExpandedChange,
            selectedValue = selectedMaterialNo,
            onValueSelected = onSelectedMaterialNoChange
        )

        DropdownField(
            label = "공급업체",
            options = options,
            expanded = supplierExpanded,
            onExpandedChange = onSupplierExpandedChange,
            selectedValue = selectedSupplier,
            onValueSelected = onSelectedSupplierChange
        )

        DropdownField(
            label = "실장착공정",
            options = options,
            expanded = processExpanded,
            onExpandedChange = onProcessExpandedChange,
            selectedValue = selectedProcess,
            onValueSelected = onSelectedProcessChange
        )

        DropdownField(
            label = "불량 사유",
            options = options,
            expanded = defectExpanded,
            onExpandedChange = onDefectExpandedChange,
            selectedValue = selectedDefectReason,
            onValueSelected = onSelectedDefectReasonChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        onExpandedChange(false)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { onRegisterClick() },
            modifier = Modifier
                .weight(1f)
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
                .weight(1f)
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

@Composable
fun TableSection(
    items: List<DefectItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "불량 정보",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        TableHeader()

        items.forEach { item ->
            TableRow(item = item)
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDDE3EA))
            .border(1.dp, Color.Gray)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell("id", 0.7f, true)
        TableCell("alccode", 1.4f, true)
        TableCell("materialNo", 1.1f, true)
        TableCell("supplier", 1.4f, true)
        TableCell("name", 1.0f, true)
        TableCell("status", 1.6f, true)
    }
}

@Composable
fun TableRow(item: DefectItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFCCCCCC))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(item.id, 0.7f, false)
        TableCell(item.alccode, 1.4f, false)
        TableCell(item.materialNo, 1.1f, false)
        TableCell(item.supplier, 1.4f, false)
        TableCell(item.name, 1.0f, false)
        TableCell(item.status, 1.6f, false)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 14.sp else 13.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black
        )
    }
}