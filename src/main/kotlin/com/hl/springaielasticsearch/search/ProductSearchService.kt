package com.hl.springaielasticsearch.search

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class ProductSearchService(
    private val vectorStore: VectorStore,
) {
    fun search(request: ProductSearchRequest): ProductSearchResponse {
        val startTime = System.currentTimeMillis()

        val searchResults =
            when {
                request.filters.hasActiveFilters() -> performHybridSearch(request)
                else -> performSemanticSearch(request)
            }

        val searchTime = System.currentTimeMillis() - startTime
        val searchType = if (request.filters.hasActiveFilters()) SearchType.HYBRID else SearchType.SEMANTIC

        return ProductSearchResponse(
            products = searchResults,
            searchMetadata =
                SearchMetadata(
                    totalResults = searchResults.size,
                    searchTime = searchTime,
                    searchType = searchType,
                    appliedFilters =
                        mapOf(
                            "query" to request.query,
                            "categories" to request.filters.categories,
                            "brands" to request.filters.brands,
                        ),
                ),
        )
    }

    private fun performSemanticSearch(request: ProductSearchRequest): List<ProductSearchResult> {
        val searchRequest =
            SearchRequest
                .builder()
                .query(request.normalizedQuery)
                .topK(request.pagination.size)
                .similarityThreshold(0.7)
                .build()

        val documents = vectorStore.similaritySearch(searchRequest)

        return documents
            .mapIndexed { index, document ->
                convertDocumentToResult(document, calculateRelevanceScore(document, index))
            }.take(request.pagination.size)
    }

    private fun performHybridSearch(request: ProductSearchRequest): List<ProductSearchResult> {
        // Start with semantic search
        val semanticRequest =
            SearchRequest
                .builder()
                .query(request.normalizedQuery)
                .topK(50) // Get more results for filtering
                .similarityThreshold(0.5) // Lower threshold for broader results
                .build()

        val documents = vectorStore.similaritySearch(semanticRequest)

        // Apply metadata filters
        val filteredDocuments =
            documents.filter { document ->
                matchesFilters(document, request.filters)
            }

        // Apply sorting and pagination
        val sortedDocuments = applySorting(filteredDocuments, request.sort)

        return sortedDocuments
            .drop(request.pagination.offset)
            .take(request.pagination.size)
            .mapIndexed { index, document ->
                convertDocumentToResult(document, calculateRelevanceScore(document, index))
            }
    }

    private fun matchesFilters(
        document: Document,
        filters: ProductFilters,
    ): Boolean {
        val metadata = document.metadata

        // Category filter
        if (filters.categories.isNotEmpty()) {
            val category = metadata["category"]?.toString() ?: ""
            if (!filters.categories.any { it.equals(category, ignoreCase = true) }) {
                return false
            }
        }

        // Brand filter
        if (filters.brands.isNotEmpty()) {
            val brand = metadata["brand"]?.toString() ?: ""
            if (!filters.brands.any { it.equals(brand, ignoreCase = true) }) {
                return false
            }
        }

        // Maker filter
        if (filters.makers.isNotEmpty()) {
            val maker = metadata["maker"]?.toString() ?: ""
            if (!filters.makers.any { it.equals(maker, ignoreCase = true) }) {
                return false
            }
        }

        // Price range filter
        filters.priceRange?.let { priceRange ->
            val price = metadata["price"]?.toString()?.toIntOrNull() ?: 0
            if (priceRange.min != null && price < priceRange.min) return false
            if (priceRange.max != null && price > priceRange.max) return false
        }

        return true
    }

    private fun applySorting(
        documents: List<Document>,
        sort: SearchSort,
    ): List<Document> =
        when (sort) {
            SearchSort.RELEVANCE -> documents // Already sorted by relevance
            SearchSort.PRICE_ASC -> documents.sortedBy { it.metadata["price"]?.toString()?.toIntOrNull() ?: 0 }
            SearchSort.PRICE_DESC ->
                documents.sortedByDescending {
                    it.metadata["price"]?.toString()?.toIntOrNull() ?: 0
                }

            SearchSort.NAME_ASC -> documents.sortedBy { it.metadata["productName"]?.toString() ?: "" }
            SearchSort.NAME_DESC -> documents.sortedByDescending { it.metadata["productName"]?.toString() ?: "" }
        }

    private fun convertDocumentToResult(
        document: Document,
        relevanceScore: Double,
    ): ProductSearchResult {
        val metadata = document.metadata

        return ProductSearchResult.fromProduct(
            productName = metadata["productName"]?.toString() ?: "",
            description = metadata["description"]?.toString() ?: "",
            brand = metadata["brand"]?.toString() ?: "",
            maker = metadata["maker"]?.toString() ?: "",
            category = metadata["category"]?.toString() ?: "",
            price = metadata["price"]?.toString() ?: "0",
            similarityScore = relevanceScore,
        )
    }

    private fun calculateRelevanceScore(
        document: Document,
        index: Int,
    ): Double {
        // Spring AI similarity search returns documents in order of relevance
        // Higher score for earlier results, with diminishing returns
        return 1.0 - (index * 0.05).coerceAtMost(0.9)
    }

    fun searchByCategory(
        category: String,
        limit: Int = 10,
    ): ProductSearchResponse {
        val request =
            ProductSearchRequest(
                query = category,
                filters = ProductFilters(categories = listOf(category)),
                pagination = Pagination(size = limit),
            )
        return search(request)
    }

    fun searchByBrand(
        brand: String,
        limit: Int = 10,
    ): ProductSearchResponse {
        val request =
            ProductSearchRequest(
                query = brand,
                filters = ProductFilters(brands = listOf(brand)),
                pagination = Pagination(size = limit),
            )
        return search(request)
    }
}

private fun ProductFilters.hasActiveFilters(): Boolean =
    categories.isNotEmpty() ||
        brands.isNotEmpty() ||
        makers.isNotEmpty() ||
        priceRange != null ||
        inStock != null
