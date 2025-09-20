package com.hl.springaielasticsearch.search

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = ["http://localhost:3000"]) // For frontend development
class ProductSearchController(
    private val productSearchService: ProductSearchService,
) {
    @GetMapping("/products")
    fun searchProducts(
        @RequestParam q: String,
        @RequestParam(defaultValue = "0.6") semanticWeight: Double,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "RELEVANCE") sort: String,
        @RequestParam(required = false) categories: List<String>?,
        @RequestParam(required = false) brands: List<String>?,
        @RequestParam(required = false) makers: List<String>?,
        @RequestParam(required = false) minPrice: Int?,
        @RequestParam(required = false) maxPrice: Int?,
    ): ResponseEntity<ProductSearchResponse> {
        val filters =
            ProductFilters(
                categories = categories ?: emptyList(),
                brands = brands ?: emptyList(),
                makers = makers ?: emptyList(),
                priceRange =
                    if (minPrice != null || maxPrice != null) {
                        PriceRange(minPrice, maxPrice)
                    } else {
                        null
                    },
            )

        val request =
            ProductSearchRequest(
                query = q,
                semanticWeight = semanticWeight,
                filters = filters,
                sort = SearchSort.valueOf(sort.uppercase()),
                pagination = Pagination(page = page, size = size),
            )

        val response = productSearchService.search(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/products/advanced")
    fun advancedSearch(
        @RequestBody request: ProductSearchRequest,
    ): ResponseEntity<ProductSearchResponse> {
        val response = productSearchService.search(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/products/semantic")
    fun semanticSearch(
        @RequestParam q: String,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "0.7") threshold: Double,
    ): ResponseEntity<ProductSearchResponse> {
        val request =
            ProductSearchRequest(
                query = q,
                pagination = Pagination(size = limit),
                filters = ProductFilters(), // No filters for pure semantic search
            )

        val response = productSearchService.search(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/products/category/{category}")
    fun searchByCategory(
        @PathVariable category: String,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(required = false) q: String?,
    ): ResponseEntity<ProductSearchResponse> {
        val query = q ?: category
        val response = productSearchService.searchByCategory(category, limit)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/products/brand/{brand}")
    fun searchByBrand(
        @PathVariable brand: String,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(required = false) q: String?,
    ): ResponseEntity<ProductSearchResponse> {
        val response = productSearchService.searchByBrand(brand, limit)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/products/suggestions")
    fun getSearchSuggestions(
        @RequestParam prefix: String,
        @RequestParam(defaultValue = "5") limit: Int,
    ): ResponseEntity<List<String>> {
        // Basic implementation - could be enhanced with completion suggester
        val suggestions =
            when {
                prefix.contains("냉장고") -> listOf("김치냉장고", "일반냉장고", "양문형냉장고", "프렌치도어냉장고")
                prefix.contains("삼성") -> listOf("삼성 비스포크", "삼성 김치플러스", "삼성 푸드쇼케이스")
                prefix.contains("LG") -> listOf("LG 디오스", "LG 오브제컬렉션", "LG 트윈스")
                prefix.contains("라면") -> listOf("진라면", "신라면", "불닭볶음면")
                prefix.contains("스마트폰") -> listOf("iPhone", "Galaxy", "삼성 갤럭시")
                else -> emptyList()
            }.take(limit)

        return ResponseEntity.ok(suggestions)
    }

    @GetMapping("/test/korean")
    fun testKoreanSearch(): ResponseEntity<Map<String, Any>> {
        val testQueries =
            listOf(
                "냉장고",
                "삼성",
                "김치냉장고",
                "에너지효율 1등급",
                "라면",
                "스마트폰",
            )

        val results =
            testQueries.associateWith { query ->
                try {
                    val request =
                        ProductSearchRequest(
                            query = query,
                            pagination = Pagination(size = 5),
                        )
                    val response = productSearchService.search(request)
                    mapOf(
                        "query" to query,
                        "resultCount" to response.products.size,
                        "searchTime" to response.searchMetadata.searchTime,
                        "topResults" to response.products.take(3).map { it.productName },
                    )
                } catch (e: Exception) {
                    mapOf(
                        "query" to query,
                        "error" to e.message,
                    )
                }
            }

        return ResponseEntity.ok(
            mapOf(
                "testResults" to results,
                "summary" to
                    mapOf(
                        "totalQueries" to testQueries.size,
                        "successfulQueries" to results.values.count { !it.containsKey("error") },
                    ),
            ),
        )
    }
}
