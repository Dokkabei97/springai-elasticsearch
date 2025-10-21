package com.hl.springaielasticsearch

import org.springframework.ai.document.Document

/**
 * Product를 Spring AI Document로 변환하는 확장 함수
 *
 * 이 함수는 벡터 검색 시스템의 핵심 변환 로직입니다.
 * Product 객체를 Gemini API로 임베딩할 수 있는 형태로 변환합니다.
 *
 * **변환 과정**:
 * 1. **텍스트 구성**: 모든 제품 정보를 자연어 형태로 조합
 *    - "상품명: 삼성 비스포크 냉장고\n설명: ..." 형태
 *    - 이 텍스트가 Gemini API로 전송되어 768차원 벡터 생성
 *
 * 2. **메타데이터 구성**: 필터링과 검색 결과 표시에 사용할 구조화된 데이터
 *    - 카테고리, 브랜드, 가격 등의 정확한 값
 *    - 하이브리드 검색 시 필터링에 활용
 *
 * **텍스트 vs 메타데이터**:
 * - **text**: 의미 검색용 (벡터 생성 → 유사도 계산)
 * - **metadata**: 정확한 필터링용 (category='냉장고', price<1000000)
 *
 * **왜 자연어 형태로 변환하나?**:
 * ```
 * ❌ 나쁜 예: "삼성 비스포크 냉장고 4도어 대용량 냉장고 삼성..."
 * ✅ 좋은 예: "상품명: 삼성 비스포크 냉장고\n설명: 4도어 대용량 냉장고..."
 * ```
 * 자연어 형태가 Gemini API에서 더 정확한 문맥 이해를 가능하게 합니다.
 *
 * **사용 예시**:
 * ```kotlin
 * val product = Product.of(
 *     productName = "삼성 비스포크 냉장고",
 *     description = "4도어 대용량 냉장고",
 *     // ...
 * )
 *
 * val document = product.toDocument()
 * vectorStore.add(listOf(document))  // Elasticsearch에 저장
 * ```
 *
 * **생성되는 Document 구조**:
 * ```
 * Document {
 *   content: "상품명: 삼성 비스포크 냉장고\n설명: 4도어 대용량 냉장고\n..."
 *   metadata: {
 *     productName: "삼성 비스포크 냉장고",
 *     category: "냉장고",
 *     price: "2500000",
 *     // ...
 *   }
 *   embedding: [0.023, -0.145, 0.678, ..., 0.234]  // 768 dimensions (Gemini API 생성)
 * }
 * ```
 *
 * @receiver Product 변환할 제품 객체
 * @return Spring AI Document (텍스트 + 메타데이터 + 임베딩)
 *
 * @see Product 도메인 모델
 * @see org.springframework.ai.document.Document Spring AI Document 클래스
 */
fun Product.toDocument(): Document =
    Document
        .Builder()
        // 1. 텍스트 구성: 자연어 형태로 모든 정보 포함
        // 이 텍스트가 Gemini API로 전송되어 벡터로 변환됩니다
        .text(
            buildString {
                appendLine("상품명: $productName")
                appendLine("설명: $description")
                appendLine("브랜드: $brand")
                appendLine("제조사: $maker")
                appendLine("카테고리: $category")
                appendLine("가격: ${price}원")
            },
        )
        // 2. 메타데이터 구성: 필터링과 결과 표시용 구조화된 데이터
        // 하이브리드 검색 시 이 메타데이터로 필터링합니다
        .metadata(
            mapOf(
                "productName" to productName,
                "description" to description,
                "brand" to brand,
                "maker" to maker,
                "category" to category,
                "price" to price.toString(),  // String으로 변환 (Elasticsearch 저장 형식)
                "type" to "product",          // 문서 타입 식별자
            ),
        ).build()

/**
 * ProductConvert 클래스
 *
 * 이 클래스는 실제 기능은 없으며, 파일 구조를 위한 마커 클래스입니다.
 * 실제 변환 로직은 위의 확장 함수 (Product.toDocument)에 구현되어 있습니다.
 */
class ProductConvert
