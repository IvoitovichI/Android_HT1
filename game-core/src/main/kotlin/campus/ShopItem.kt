package campus

data class ShopItem(val id: String, val title: String, val place: String, val price: Int,
    val health: Int = 0, val mana: Int = 0, val mind: Int = 0, val description: String)
