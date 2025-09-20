package com.hl.springaielasticsearch.search

data class ProductSearchRequest(
    val query: String,
    val semanticWeight: Double = 0.6,
    val useSemanticExpansion: Boolean = true,
    val filters: ProductFilters = ProductFilters(),
    val sort: SearchSort = SearchSort.RELEVANCE,
    val pagination: Pagination = Pagination()
) {
    val normalizedQuery: String get() = query.trim().lowercase()
        .replace(Regex("[^가-힣a-zA-Z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
}

data class ProductFilters(
    val categories: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val makers: List<String> = emptyList(),
    val priceRange: PriceRange? = null,
    val inStock: Boolean? = null
)

data class PriceRange(
    val min: Int? = null,
    val max: Int? = null
)

enum class SearchSort {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    NAME_ASC,
    NAME_DESC
}

data class Pagination(
    val page: Int = 0,
    val size: Int = 10
) {
    val offset: Int get() = page * size
}