package com.hl.springaielasticsearch

class Product(
    val productName: String,
    val description: String,
    val brand: String,
    val maker: String,
    val category: String,
    val price: Int,
) {
    companion object {
        fun of(
            productName: String,
            description: String,
            brand: String,
            maker: String,
            category: String,
            price: Int,
        ): Product =
            Product(
                productName,
                description,
                brand,
                maker,
                category,
                price,
            )
    }
}
