package com.hl.springaielasticsearch

import org.springframework.ai.document.Document

fun Product.toDocument(): Document =
    Document
        .Builder()
        .text(
            buildString {
                appendLine("상품명: $productName")
                appendLine("설명: $description")
                appendLine("브랜드: $brand")
                appendLine("제조사: $maker")
                appendLine("카테고리: $category")
                appendLine("가격: ${price}원")
            },
        ).metadata(
            mapOf(
                "productName" to productName,
                "description" to description,
                "brand" to brand,
                "maker" to maker,
                "category" to category,
                "price" to price.toString(),
                "type" to "product",
            ),
        ).build()

class ProductConvert
