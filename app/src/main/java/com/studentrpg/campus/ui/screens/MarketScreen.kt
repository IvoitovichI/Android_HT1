package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import campus.*
import com.studentrpg.campus.R
import com.studentrpg.campus.ui.components.*

@Composable
internal fun MarketScreen(state: CampusState, onBuy: (String) -> Unit, onWork: () -> Unit, onUse: (String) -> Unit) {
    Page(state, "Перерыв на жизнь", "Монеты превращаются в силы. Силы — в зачёты.") {
        item {
            Panel("Подработка: курьер кампуса", R.drawable.ic_work) {
                Text("120 минут · +45 монет · +15 XP\n−8 здоровья · −12 концентрации\n25% шанс получить кофе в подарок, если в рюкзаке есть место.")
                Button(onWork, Modifier.fillMaxWidth(), enabled = !state.finished && state.minutesLeft >= 120 && state.health >= 15 && state.mind >= 20) { Text("Выйти на смену") }
            }
        }
        listOf("Столовая", "Автомат").forEach { place ->
            item { Text(place, style = MaterialTheme.typography.titleLarge) }
            items(CampusEconomy.items.filter { it.place == place }) { item ->
                Panel(item.title, if (item.id == "coffee" || item.id == "tea") R.drawable.ic_coffee else R.drawable.ic_food) {
                    Text(item.description)
                    Text("В рюкзаке: ${state.inventory[item.id] ?: 0} · использование занимает 5 минут")
                    Button({ onBuy(item.id) }, Modifier.fillMaxWidth(), enabled = !state.finished && state.coins >= item.price && state.inventory.values.sum() < 20) { Text("Купить · ${item.price} монет") }
                }
            }
        }
        item { Inventory(state, onUse) }
    }
}
