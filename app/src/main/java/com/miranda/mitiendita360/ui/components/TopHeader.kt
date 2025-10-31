package com.miranda.mitiendita360.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miranda.mitiendita360.R
import com.miranda.mitiendita360.ui.theme.VerdeLimon

@Composable
fun TopHeader() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        Image(
            painterResource(R.drawable.suppliers),
            contentDescription = "Header Image",
            colorFilter = ColorFilter.tint(color = VerdeLimon),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(250.dp)
        )
        Column(
            modifier = Modifier.padding(top = 145.dp, start = 210.dp),
        ){
            Text("Detalle", color = Color.White, fontSize = 25.sp)
            Text("Proveedor", color = Color.White, fontSize = 25.sp)
        }
    }
}