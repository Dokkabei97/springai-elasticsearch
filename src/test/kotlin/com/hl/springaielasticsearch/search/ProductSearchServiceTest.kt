package com.hl.springaielasticsearch.search

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
@TestPropertySource(properties = [
    "spring.ai.vectorstore.elasticsearch.initialize-schema=true"
])
class ProductSearchServiceTest {

    @Autowired
    private lateinit var productSearchService: ProductSearchService

    @Test
    fun `test Korean semantic search for refrigerators`() {
        val request = ProductSearchRequest(
            query = "냉장고",
            pagination = Pagination(size = 5)
        )

        val response = productSearchService.search(request)

        println("=== 냉장고 검색 결과 ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")
        println("검색 타입: ${response.searchMetadata.searchType}")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   브랜드: ${product.brand}, 카테고리: ${product.category}")
            println("   유사도: ${String.format("%.3f", product.similarityScore)}")
            println("   가격: ${product.price}원")
            println()
        }

        assert(response.products.isNotEmpty()) { "냉장고 검색 결과가 없습니다" }
    }

    @Test
    fun `test brand search for Samsung`() {
        val request = ProductSearchRequest(
            query = "삼성",
            pagination = Pagination(size = 5)
        )

        val response = productSearchService.search(request)

        println("=== 삼성 브랜드 검색 결과 ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   브랜드: ${product.brand}, 제조사: ${product.maker}")
            println("   유사도: ${String.format("%.3f", product.similarityScore)}")
            println()
        }

        assert(response.products.isNotEmpty()) { "삼성 브랜드 검색 결과가 없습니다" }
    }

    @Test
    fun `test hybrid search with price filter`() {
        val request = ProductSearchRequest(
            query = "냉장고",
            filters = ProductFilters(
                priceRange = PriceRange(min = 500000, max = 1000000)
            ),
            pagination = Pagination(size = 5)
        )

        val response = productSearchService.search(request)

        println("=== 하이브리드 검색 결과 (냉장고 + 가격 필터) ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")
        println("검색 타입: ${response.searchMetadata.searchType}")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   가격: ${product.price}원 (${product.price in 500000..1000000})")
            println("   유사도: ${String.format("%.3f", product.similarityScore)}")
            println()
        }

        // Check that all results are within price range
        response.products.forEach { product ->
            assert(product.price in 500000..1000000) {
                "가격 범위를 벗어난 상품: ${product.productName} (${product.price}원)"
            }
        }
    }

    @Test
    fun `test category specific search`() {
        val request = ProductSearchRequest(
            query = "김치냉장고",
            pagination = Pagination(size = 10)
        )

        val response = productSearchService.search(request)

        println("=== 김치냉장고 카테고리 검색 결과 ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   카테고리: ${product.category}")
            println("   브랜드: ${product.brand}")
            println("   가격: ${product.price}원")
            println()
        }

        assert(response.products.isNotEmpty()) { "김치냉장고 검색 결과가 없습니다" }
    }

    @Test
    fun `test food category search`() {
        val request = ProductSearchRequest(
            query = "라면",
            pagination = Pagination(size = 5)
        )

        val response = productSearchService.search(request)

        println("=== 라면 검색 결과 ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   설명: ${product.description}")
            println("   가격: ${product.price}원")
            println()
        }

        assert(response.products.isNotEmpty()) { "라면 검색 결과가 없습니다" }
    }

    @Test
    fun `test multiple brand search`() {
        val request = ProductSearchRequest(
            query = "냉장고",
            filters = ProductFilters(
                brands = listOf("Samsung", "LG")
            ),
            pagination = Pagination(size = 10)
        )

        val response = productSearchService.search(request)

        println("=== 다중 브랜드 검색 결과 (Samsung, LG 냉장고) ===")
        println("검색 시간: ${response.searchMetadata.searchTime}ms")
        println("총 결과: ${response.searchMetadata.totalResults}개")

        response.products.forEachIndexed { index, product ->
            println("${index + 1}. ${product.productName}")
            println("   브랜드: ${product.brand}")
            println("   카테고리: ${product.category}")
            println("   가격: ${product.price}원")
            println()
        }

        // Verify only Samsung or LG products
        response.products.forEach { product ->
            assert(product.brand in listOf("Samsung", "LG", "삼성", "삼성전자")) {
                "브랜드 필터링 오류: ${product.productName} (브랜드: ${product.brand})"
            }
        }
    }
}