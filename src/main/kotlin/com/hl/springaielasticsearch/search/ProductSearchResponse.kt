package com.hl.springaielasticsearch.search

data class ProductSearchResponse(
    val products: List<ProductSearchResult>,
    val facets: Map<FacetType, List<FacetValue>> = emptyMap(),
    val searchMetadata: SearchMetadata,
    val suggestions: List<String> = emptyList()
)

data class ProductSearchResult(
    val productName: String,
    val description: String,
    val brand: String,
    val maker: String,
    val category: String,
    val price: Int,
    val similarityScore: Double,
    val relevanceScore: Double = similarityScore
) {
    companion object {
        fun fromProduct(
            productName: String,
            description: String,
            brand: String,
            maker: String,
            category: String,
            price: String,
            similarityScore: Double
        ): ProductSearchResult {
            return ProductSearchResult(
                productName = productName,
                description = description,
                brand = brand,
                maker = maker,
                category = category,
                price = price.toIntOrNull() ?: 0,
                similarityScore = similarityScore
            )
        }
    }
}

data class SearchMetadata(
    val totalResults: Int,
    val searchTime: Long,
    val searchType: SearchType,
    val appliedFilters: Map<String, Any> = emptyMap()
)

enum class SearchType {
    SEMANTIC,
    HYBRID,
    FILTERED
}

enum class FacetType {
    BRAND,
    CATEGORY,
    PRICE,
    MAKER
}

data class FacetValue(
    val value: String,
    val count: Int
)