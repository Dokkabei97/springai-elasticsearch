package com.hl.springaielasticsearch

/**
 * 제품 도메인 모델
 *
 * 이 클래스는 검색 시스템의 핵심 데이터 모델입니다.
 * 각 제품은 벡터로 변환되어 Elasticsearch에 저장됩니다.
 *
 * **벡터 검색 흐름**:
 * 1. Product 객체 생성
 * 2. toDocument() 확장 함수로 Spring AI Document 변환
 * 3. Gemini API를 통해 768차원 벡터 생성
 * 4. Elasticsearch에 벡터 + 메타데이터 저장
 *
 * @property productName 제품명 (예: "삼성 비스포크 냉장고")
 * @property description 제품 설명 (예: "4도어 대용량 냉장고")
 * @property brand 브랜드 (예: "삼성")
 * @property maker 제조사 (예: "삼성전자")
 * @property category 카테고리 (예: "냉장고", "김치냉장고")
 * @property price 가격 (원 단위, 예: 2500000)
 *
 * @see ProductConvert.toDocument 벡터 변환 로직
 * @see SampleDataInit 샘플 데이터 생성 예시
 */
class Product(
    val productName: String,
    val description: String,
    val brand: String,
    val maker: String,
    val category: String,
    val price: Int,
) {
    companion object {
        /**
         * Product 객체를 생성하는 팩토리 메서드
         *
         * 명명된 파라미터를 사용하여 가독성을 높이고,
         * 필요시 검증 로직을 추가할 수 있습니다.
         *
         * **사용 예시**:
         * ```kotlin
         * val product = Product.of(
         *     productName = "삼성 비스포크 냉장고",
         *     description = "4도어 대용량 냉장고",
         *     brand = "삼성",
         *     maker = "삼성전자",
         *     category = "냉장고",
         *     price = 2500000
         * )
         * ```
         *
         * @return 생성된 Product 인스턴스
         */
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
