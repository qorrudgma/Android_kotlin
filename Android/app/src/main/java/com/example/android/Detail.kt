package com.example.android

import androidx.compose.ui.tooling.preview.Preview
import android.app.Activity
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

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 샘플 데이터 (나중에 Intent 값으로 변경 가능)
        val alcCode = intent.getStringExtra("alcCode") ?: "S00"
        val materialNo = intent.getStringExtra("materialNo") ?: "05203-SW000"
        val supplier = intent.getStringExtra("supplier") ?: "S994"
        val process = intent.getStringExtra("process") ?: "실장착1"
        val defectReason = intent.getStringExtra("defectReason") ?: "스크래치"
        val operator = intent.getStringExtra("operator") ?: "홍길동"
        val status = intent.getStringExtra("status") ?: "정상"

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
                    },
                    onRegisterClick = {
                        // 나중에 등록 API 연결
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
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val isDefect = status == "불량"

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
                    text = "상세 화면",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 상세 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    DetailItem("ALC코드", alcCode)
                    DetailItem("자재번호", materialNo)
                    DetailItem("공급업체", supplier)
                    DetailItem("실장착공정", process)

                    if (isDefect) {
                        DetailItem("불량사유", defectReason)
                        DetailItem("담당자", operator)
                    }

                    DetailItem("상태", status)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("뒤로가기")
                }

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF191970)
                    )
                ) {
                    Text("등록하기")
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
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
            text = "$value",
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_5"
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
            onBackClick = {},
            onRegisterClick = {}
        )
    }
}