package com.studentrpg.campus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import campus.*
import com.studentrpg.campus.R

@Composable
internal fun MiniStat(icon: Int, label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(icon), label, Modifier.size(17.dp), tint = color)
        Text("$value", style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
internal fun Page(state: CampusState, title: String, subtitle: String, content: LazyListScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp))
                }
            }
            content()
        }
    }
}

@Composable
internal fun Panel(title: String, icon: Int, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(icon), null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
internal fun Banner(resource: Int, description: String) {
    Image(painterResource(resource), description, Modifier.fillMaxWidth().height(185.dp).clip(RoundedCornerShape(22.dp)),
        contentScale = ContentScale.Crop)
}

@Composable
internal fun Inventory(state: CampusState, onUse: (String) -> Unit) {
    Panel("Рюкзак · ${state.inventory.values.sum()}/20", R.drawable.ic_bag) {
        if (state.inventory.isEmpty()) Text("Пусто. Загляни в столовую или к автомату.")
        CampusEconomy.items.filter { (state.inventory[it.id] ?: 0) > 0 }.forEach { item ->
            Text("${item.title} ×${state.inventory[item.id]} · ${item.description}")
            OutlinedButton({ onUse(item.id) }, Modifier.fillMaxWidth(), enabled = !state.finished && state.minutesLeft >= 5) { Text("Использовать · 5 мин") }
        }
    }
}
