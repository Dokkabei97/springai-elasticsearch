# Spring AI + Elasticsearch 벡터 검색 튜토리얼

> **검색 엔지니어를 위한 단계별 실습 가이드**
>
> 이 튜토리얼은 Spring AI와 Elasticsearch를 활용한 벡터 검색 시스템을 처음부터 단계별로 학습할 수 있도록 구성되었습니다.

## 📋 목차

- [준비하기](#준비하기)
- [Step 1: 환경 구성 및 프로젝트 실행](#step-1-환경-구성-및-프로젝트-실행)
- [Step 2: 첫 벡터 검색 실행하기](#step-2-첫-벡터-검색-실행하기)
- [Step 3: 벡터 임베딩 이해하기](#step-3-벡터-임베딩-이해하기)
- [Step 4: 시맨틱 검색 vs 키워드 검색](#step-4-시맨틱-검색-vs-키워드-검색)
- [Step 5: 하이브리드 검색 구현](#step-5-하이브리드-검색-구현)
- [Step 6: 검색 결과 최적화](#step-6-검색-결과-최적화)
- [Step 7: 커스텀 검색 기능 추가](#step-7-커스텀-검색-기능-추가)
- [Step 8: 프로덕션 준비](#step-8-프로덕션-준비)

---

## 준비하기

### 필요한 사전 지식

- ✅ Java/Kotlin 기본 문법
- ✅ Spring Boot 기초 (의존성 주입, REST API)
- ✅ REST API 개념
- ✅ 기본적인 Docker 사용법
- ⚠️ 벡터 검색 지식 (선택사항 - 튜토리얼에서 학습)

### 설치할 도구

1. **Java 21** - [다운로드](https://adoptium.net/)
2. **Docker Desktop** - [다운로드](https://www.docker.com/products/docker-desktop/)
3. **IDE** - IntelliJ IDEA (권장) 또는 VS Code
4. **Git** - 소스 코드 클론용
5. **curl** 또는 **Postman** - API 테스트용

### Google Gemini API 키 발급

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. "API 및 서비스" → "API 사용 설정" 클릭
4. "Generative Language API" 검색 후 활성화
5. "사용자 인증 정보" → "사용자 인증 정보 만들기" → "API 키" 선택
6. 생성된 API 키 복사 (나중에 사용)

---

## Step 1: 환경 구성 및 프로젝트 실행

**목표**: 프로젝트를 로컬 환경에서 실행하고 기본 구조를 이해합니다.

### 1.1 프로젝트 클론

```bash
git clone <repository-url>
cd springai-elasticsearch
```

### 1.2 API 키 설정

`src/main/resources/application.yml` 파일을 열고 Gemini API 설정을 추가합니다:

```yaml
spring:
  ai:
    google:
      genai:
        embedding:
          project-id: your-project-id-here    # Google Cloud 프로젝트 ID
          api-key: your-api-key-here          # 발급받은 API 키
```

**보안 팁**: 실제 프로젝트에서는 환경 변수를 사용하세요:

```yaml
spring:
  ai:
    google:
      genai:
        embedding:
          project-id: ${GOOGLE_PROJECT_ID}
          api-key: ${GOOGLE_API_KEY}
```

### 1.3 Elasticsearch 시작

```bash
# Docker Compose로 Elasticsearch와 Kibana 시작
docker-compose up -d

# 실행 확인
docker-compose ps

# Elasticsearch 상태 확인
curl http://localhost:9200
```

**예상 출력**:
```json
{
  "name" : "es01",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "9.1.2"
  },
  "tagline" : "You Know, for Search"
}
```

### 1.4 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

**성공 로그 확인**:
```
Started SpringaiElasticsearchApplication in X.XXX seconds
Initializing sample data...
Added 54 products to vector store
```

### 1.5 프로젝트 구조 파악

```
src/main/kotlin/com/dokkabei97/springaielasticsearch/
│
├── domain/                          # 비즈니스 도메인
│   ├── Product.kt                   # 제품 데이터 모델
│   └── ProductConvert.kt            # 벡터 변환 로직
│
├── search/                          # 검색 기능
│   ├── ProductSearchService.kt      # 핵심 검색 로직
│   ├── ProductSearchController.kt   # REST API
│   └── dto/                         # 데이터 전송 객체
│
├── config/                          # 설정
│   └── VectorStoreConfig.kt         # Vector Store 설정
│
└── init/                            # 초기화
    └── SampleDataInit.kt            # 샘플 데이터 로딩
```

### 1.6 첫 API 호출

```bash
# 건강 체크
curl http://localhost:8080/api/search/test/korean

# 기본 검색
curl "http://localhost:8080/api/search/products?q=냉장고&size=3"
```

**✅ 체크포인트**:
- [ ] Elasticsearch가 정상 실행 중
- [ ] 애플리케이션이 정상 시작
- [ ] 샘플 데이터 54개 로딩 완료
- [ ] API 호출이 정상 응답

---

## Step 2: 첫 벡터 검색 실행하기

**목표**: 벡터 검색이 어떻게 작동하는지 실제로 경험합니다.

### 2.1 간단한 검색 실행

```bash
# "냉장고" 검색
curl "http://localhost:8080/api/search/products?q=냉장고&size=5" | jq
```

**응답 예시**:
```json
{
  "totalResults": 15,
  "page": 0,
  "size": 5,
  "hasMore": true,
  "searchMode": "SEMANTIC",
  "products": [
    {
      "productName": "삼성 비스포크 냉장고",
      "description": "4도어 대용량 냉장고",
      "brand": "삼성",
      "category": "냉장고",
      "price": 2500000,
      "relevanceScore": 0.95
    },
    // ...
  ]
}
```

### 2.2 다양한 검색어 실험

**실험 1: 구체적인 제품명**
```bash
curl "http://localhost:8080/api/search/products?q=삼성%20비스포크"
```

**실험 2: 추상적인 개념**
```bash
curl "http://localhost:8080/api/search/products?q=건강한%20음식"
```

**실험 3: 기능적 설명**
```bash
curl "http://localhost:8080/api/search/products?q=음식을%20신선하게%20보관하는%20제품"
```

### 2.3 검색 결과 분석

각 검색어에 대해 다음을 확인하세요:

1. **relevanceScore**: 검색어와 제품의 유사도 점수 (0.0 ~ 1.0)
2. **searchMode**: 사용된 검색 모드 (SEMANTIC 또는 HYBRID)
3. **totalResults**: 찾은 총 결과 수

**🤔 생각해보기**:
- 왜 "음식을 신선하게 보관하는 제품"이 냉장고를 찾아줄까?
- 키워드 검색과 어떻게 다를까?

### 2.4 Kibana에서 벡터 확인

1. 브라우저에서 http://localhost:5601 접속
2. 왼쪽 메뉴 → "Dev Tools" 클릭
3. 다음 쿼리 실행:

```json
GET /product/_search
{
  "size": 1,
  "query": {
    "match_all": {}
  }
}
```

**확인 사항**:
- `embedding` 필드: 768차원의 벡터 배열
- `metadata`: 제품 정보 (카테고리, 브랜드 등)
- `content`: 임베딩된 텍스트 내용

**✅ 체크포인트**:
- [ ] 다양한 검색어로 검색 실행
- [ ] relevanceScore의 의미 이해
- [ ] Kibana에서 벡터 데이터 확인
- [ ] 시맨틱 검색의 장점 체감

---

## Step 3: 벡터 임베딩 이해하기

**목표**: 텍스트가 어떻게 벡터로 변환되고 저장되는지 이해합니다.

### 3.1 Product → Document 변환 과정

`src/main/kotlin/com/dokkabei97/springaielasticsearch/domain/ProductConvert.kt` 파일을 확인하세요:

```kotlin
fun Product.toDocument(): Document {
    // 1단계: 제품 정보를 자연어 텍스트로 구성
    val text = """
        제품명: $productName
        설명: $description
        브랜드: $brand
        제조사: $maker
        카테고리: $category
        가격: ${price}원
    """.trimIndent()

    // 2단계: 메타데이터 구성 (필터링용)
    val metadata = mapOf(
        "productName" to productName,
        "description" to description,
        "brand" to brand,
        "maker" to maker,
        "category" to category,
        "price" to price.toString(),
        "type" to "product"
    )

    // 3단계: Spring AI Document 생성
    return Document(text, metadata)
}
```

**핵심 개념**:
- **text**: Gemini API로 전송되어 768차원 벡터로 변환
- **metadata**: 벡터와 함께 저장되어 필터링에 사용
- **자연어 형식**: "제품명: 삼성 비스포크" 형태로 문맥 제공

### 3.2 임베딩 생성 과정

```
1. Product 객체
   ↓
2. toDocument() 호출
   ↓
3. 자연어 텍스트 생성
   "제품명: 삼성 비스포크 냉장고
    설명: 4도어 대용량 냉장고
    브랜드: 삼성
    ..."
   ↓
4. Gemini API 호출
   ↓
5. 768차원 벡터 반환
   [0.023, -0.145, 0.678, ..., 0.234]
   ↓
6. Elasticsearch 저장
   {
     "embedding": [0.023, -0.145, ...],
     "content": "제품명: 삼성...",
     "metadata": {"brand": "삼성", ...}
   }
```

### 3.3 실습: 커스텀 Document 생성

`src/main/kotlin/com/dokkabei97/springaielasticsearch/init/SampleDataInit.kt`에 새 제품 추가:

```kotlin
@PostConstruct
fun initSampleData() {
    logger.info("Initializing sample data...")

    // 기존 샘플 데이터...

    // 새 제품 추가
    val customProduct = Product.of(
        productName = "나만의 스마트 냉장고",
        description = "AI 기반 식품 관리 시스템이 탑재된 냉장고",
        brand = "커스텀",
        maker = "나",
        category = "냉장고",
        price = 3000000
    )

    products.add(customProduct)

    // 벡터 저장소에 추가
    val documents = products.map { it.toDocument() }
    vectorStore.add(documents)

    logger.info("Added ${products.size} products to vector store")
}
```

**재실행 후 검색**:
```bash
# 애플리케이션 재시작
./gradlew bootRun

# 새 제품 검색
curl "http://localhost:8080/api/search/products?q=AI%20냉장고"
```

### 3.4 벡터 유사도 계산 이해

**Cosine Similarity**:
```
similarity = (A · B) / (||A|| × ||B||)

A: 검색 쿼리 벡터
B: 제품 벡터
결과: -1 ~ 1 (1에 가까울수록 유사)
```

**왜 Cosine Similarity를 사용할까?**
- 벡터의 방향(의미)만 비교
- 크기에 영향받지 않음
- 텍스트 유사도 측정에 최적

**✅ 체크포인트**:
- [ ] Product → Document 변환 과정 이해
- [ ] 텍스트가 벡터로 변환되는 과정 이해
- [ ] 메타데이터의 역할 이해
- [ ] Cosine Similarity 개념 파악

---

## Step 4: 시맨틱 검색 vs 키워드 검색

**목표**: 벡터 기반 시맨틱 검색과 전통적인 키워드 검색의 차이를 이해합니다.

### 4.1 키워드 검색의 한계

**전통적인 키워드 검색 (Elasticsearch의 match 쿼리)**:
```json
GET /product/_search
{
  "query": {
    "match": {
      "content": "신선한 음식 보관"
    }
  }
}
```

**문제점**:
- 정확히 "신선한", "음식", "보관"이라는 단어가 있어야 매칭
- "냉장고"라는 단어가 없으면 냉장고 제품을 못 찾음
- 동의어, 유사어 처리 어려움

### 4.2 시맨틱 검색의 장점

```bash
# 시맨틱 검색으로 냉장고 찾기
curl "http://localhost:8080/api/search/products/semantic?q=음식을%20신선하게%20보관하는%20장치"
```

**장점**:
- 의미를 이해하여 관련 제품 검색
- "냉장고"라는 단어 없이도 냉장고 검색 가능
- 자연어 질문 처리 가능

### 4.3 실험: 동일한 의미, 다른 표현

```bash
# 표현 1
curl "http://localhost:8080/api/search/products?q=고급스러운%20냉장고"

# 표현 2
curl "http://localhost:8080/api/search/products?q=프리미엄%20냉장고"

# 표현 3
curl "http://localhost:8080/api/search/products?q=비싼%20냉장고"
```

**🤔 관찰하기**:
- 세 검색의 결과가 비슷한가?
- relevanceScore는 어떻게 다른가?
- 왜 비슷한 제품이 나올까?

### 4.4 한국어 시맨틱 검색의 장점

```bash
# 한국어 동의어 테스트
curl "http://localhost:8080/api/search/products?q=저렴한"
curl "http://localhost:8080/api/search/products?q=가성비%20좋은"
curl "http://localhost:8080/api/search/products?q=합리적인%20가격"
```

**Gemini 임베딩의 한국어 처리**:
- 다양한 표현의 동일한 의미 인식
- 문맥 이해 (예: "차가운 냉장고" vs "차가운 성격")
- 띄어쓰기, 맞춤법 오류 허용

**✅ 체크포인트**:
- [ ] 키워드 검색의 한계 이해
- [ ] 시맨틱 검색의 장점 파악
- [ ] 동의어 처리 능력 확인
- [ ] 한국어 특화 처리 체험

---

## Step 5: 하이브리드 검색 구현

**목표**: 벡터 검색과 메타데이터 필터링을 결합한 하이브리드 검색을 이해합니다.

### 5.1 하이브리드 검색의 필요성

**시나리오**: "100만원 이하의 삼성 냉장고를 찾고 싶다"

- **순수 시맨틱 검색**: 가격, 브랜드 필터 어려움
- **메타데이터만**: 의미 기반 검색 불가능
- **하이브리드**: 두 장점을 결합 ✅

### 5.2 필터 없는 검색 (SEMANTIC 모드)

```bash
curl "http://localhost:8080/api/search/products?q=냉장고&size=10" | jq '.searchMode'
# 출력: "SEMANTIC"
```

**동작**:
- threshold: 0.7 (높은 유사도 요구)
- 메타데이터 필터 없음
- 순수 벡터 유사도만

### 5.3 필터 있는 검색 (HYBRID 모드)

```bash
curl "http://localhost:8080/api/search/products?q=냉장고&categories=김치냉장고&size=10" | jq '.searchMode'
# 출력: "HYBRID"
```

**동작**:
- threshold: 0.5 (낮은 유사도로 더 많은 결과 확보)
- 벡터 검색 후 메타데이터 필터 적용
- 필터링 → 정렬 → 페이지네이션

### 5.4 다중 필터 조합

**카테고리 + 브랜드**:
```bash
curl "http://localhost:8080/api/search/products?q=스마트&categories=냉장고&brands=삼성"
```

**카테고리 + 가격 범위**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&categories=김치냉장고&minPrice=500000&maxPrice=1500000"
```

**브랜드 + 제조사 + 가격**:
```bash
curl "http://localhost:8080/api/search/products?q=프리미엄&brands=삼성&makers=삼성전자&minPrice=2000000"
```

### 5.5 하이브리드 검색 로직 분석

`src/main/kotlin/com/dokkabei97/springaielasticsearch/search/ProductSearchService.kt` 확인:

```kotlin
fun search(request: ProductSearchRequest): ProductSearchResponse {
    // 1. 검색 모드 결정
    val searchMode = determineSearchMode(request)

    // 2. threshold 조정
    val threshold = if (searchMode == SearchMode.HYBRID) 0.5 else 0.7

    // 3. 벡터 검색
    val documents = vectorStore.similaritySearch(
        SearchRequest.query(normalizedQuery)
            .withTopK(100)  // 충분한 결과 확보
            .withSimilarityThreshold(threshold)
    )

    // 4. 메타데이터 필터링 (HYBRID 모드)
    val filteredDocs = if (searchMode == SearchMode.HYBRID) {
        applyFilters(documents, request)
    } else {
        documents
    }

    // 5. 정렬 및 페이지네이션
    val sortedDocs = applySorting(filteredDocs, request.sortBy)
    val paginatedDocs = applyPagination(sortedDocs, request.page, request.size)

    return ProductSearchResponse(...)
}
```

### 5.6 실습: threshold 값 실험

코드를 수정하여 다른 threshold 값 실험:

```kotlin
// ProductSearchService.kt
private fun determineSearchMode(request: ProductSearchRequest): SearchMode {
    // threshold를 0.3, 0.5, 0.7, 0.9로 변경하며 실험
    val threshold = 0.5  // 여기를 변경
    // ...
}
```

**관찰**:
- threshold가 낮을수록: 더 많은 결과, 낮은 품질
- threshold가 높을수록: 적은 결과, 높은 품질

**✅ 체크포인트**:
- [ ] SEMANTIC vs HYBRID 모드 차이 이해
- [ ] 다중 필터 조합 실습
- [ ] threshold의 역할 이해
- [ ] 하이브리드 검색 로직 분석

---

## Step 6: 검색 결과 최적화

**목표**: 정렬, 페이지네이션, 검색 품질 개선 방법을 학습합니다.

### 6.1 정렬 옵션 활용

**관련도순 (기본)**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&sortBy=RELEVANCE"
```

**가격 오름차순**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&sortBy=PRICE_ASC"
```

**가격 내림차순**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&sortBy=PRICE_DESC"
```

**이름 순**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&sortBy=NAME_ASC"
```

### 6.2 페이지네이션

**1페이지 (10개)**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&page=0&size=10"
```

**2페이지**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&page=1&size=10"
```

**응답에서 페이지 정보 확인**:
```json
{
  "totalResults": 25,
  "page": 0,
  "size": 10,
  "hasMore": true,  // 다음 페이지 존재 여부
  "products": [...]
}
```

### 6.3 검색 쿼리 정규화

`ProductSearchService.kt`의 `normalizeQuery()` 함수:

```kotlin
private fun normalizeQuery(query: String): String {
    return query
        .trim()                        // 공백 제거
        .replace(Regex("\\s+"), " ")  // 다중 공백을 단일 공백으로
        .lowercase()                   // 소문자 변환 (선택적)
}
```

**개선 아이디어**:
- 특수문자 제거
- 불용어(stopwords) 제거
- 동의어 확장
- 오타 수정

### 6.4 검색 품질 개선 팁

**1. 텍스트 구성 최적화**

`ProductConvert.kt`에서 더 풍부한 컨텍스트 제공:

```kotlin
val text = """
    제품명: $productName
    상세설명: $description
    브랜드: $brand (제조사: $maker)
    카테고리: $category
    가격대: ${formatPriceRange(price)}
    주요특징: ${extractFeatures(description)}
""".trimIndent()
```

**2. 메타데이터 활용**

필터링에 유용한 메타데이터 추가:

```kotlin
val metadata = mapOf(
    // 기존 필드...
    "priceRange" to getPriceRange(price),  // "저가", "중가", "고가"
    "features" to extractFeatures(description),  // ["스마트", "IoT", ...]
    "tags" to generateTags(this)  // 자동 태그 생성
)
```

**3. 검색 결과 리랭킹**

```kotlin
private fun rerankResults(documents: List<Document>, query: String): List<Document> {
    return documents.sortedByDescending { doc ->
        var score = doc.metadata["similarity_score"] as Double

        // 제품명에 쿼리 포함 시 보너스
        if (doc.metadata["productName"].toString().contains(query, ignoreCase = true)) {
            score += 0.1
        }

        // 인기 브랜드 보너스
        if (doc.metadata["brand"] in listOf("삼성", "LG")) {
            score += 0.05
        }

        score
    }
}
```

### 6.5 실습: 커스텀 정렬 추가

새로운 정렬 옵션 추가 (예: 인기도순):

```kotlin
// SortOption.kt
enum class SortOption {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    NAME_ASC,
    NAME_DESC,
    POPULARITY  // 새로 추가
}

// ProductSearchService.kt
private fun applySorting(documents: List<Document>, sortOption: SortOption): List<Document> {
    return when (sortOption) {
        // 기존 정렬...
        SortOption.POPULARITY -> documents.sortedByDescending {
            it.metadata["viewCount"]?.toString()?.toIntOrNull() ?: 0
        }
    }
}
```

**✅ 체크포인트**:
- [ ] 다양한 정렬 옵션 실습
- [ ] 페이지네이션 동작 이해
- [ ] 쿼리 정규화 방법 학습
- [ ] 검색 품질 개선 기법 파악

---

## Step 7: 커스텀 검색 기능 추가

**목표**: 프로젝트에 새로운 검색 기능을 직접 추가해봅니다.

### 7.1 추천 시스템 구현

**목표**: "이 제품과 유사한 제품" 기능 추가

**1) Service 메서드 추가**:

```kotlin
// ProductSearchService.kt
fun findSimilarProducts(productName: String, limit: Int = 5): ProductSearchResponse {
    logger.info("Finding products similar to: $productName")

    // 1. 기준 제품 찾기
    val baseProduct = vectorStore.similaritySearch(
        SearchRequest.query(productName)
            .withTopK(1)
            .withSimilarityThreshold(0.8)
    ).firstOrNull() ?: throw IllegalArgumentException("Product not found: $productName")

    // 2. 유사 제품 검색 (자신 제외)
    val similarDocs = vectorStore.similaritySearch(
        SearchRequest.query(baseProduct.content)
            .withTopK(limit + 1)
            .withSimilarityThreshold(0.6)
    ).filter { it.id != baseProduct.id }

    // 3. 응답 생성
    val products = similarDocs.take(limit).mapIndexed { index, doc ->
        documentToProductSearchResult(doc, index, SearchMode.SEMANTIC)
    }

    return ProductSearchResponse(
        products = products,
        totalResults = products.size,
        searchMode = SearchMode.SEMANTIC,
        message = "Products similar to: $productName"
    )
}
```

**2) Controller 엔드포인트 추가**:

```kotlin
// ProductSearchController.kt
@GetMapping("/similar")
fun findSimilarProducts(
    @RequestParam productName: String,
    @RequestParam(defaultValue = "5") limit: Int
): ResponseEntity<ProductSearchResponse> {
    val response = searchService.findSimilarProducts(productName, limit)
    return ResponseEntity.ok(response)
}
```

**3) 테스트**:

```bash
curl "http://localhost:8080/api/search/products/similar?productName=삼성%20비스포크%20냉장고&limit=5"
```

### 7.2 자동완성 기능

**목표**: 검색어 입력 시 제안 제공

```kotlin
// ProductSearchService.kt
fun getSuggestions(partialQuery: String, limit: Int = 5): List<String> {
    if (partialQuery.length < 2) return emptyList()

    // 벡터 검색으로 관련 제품 찾기
    val documents = vectorStore.similaritySearch(
        SearchRequest.query(partialQuery)
            .withTopK(limit * 2)
            .withSimilarityThreshold(0.5)
    )

    // 제품명 추출 및 중복 제거
    return documents
        .mapNotNull { it.metadata["productName"]?.toString() }
        .distinct()
        .take(limit)
}
```

**Controller**:

```kotlin
@GetMapping("/suggestions")
fun getSuggestions(
    @RequestParam q: String,
    @RequestParam(defaultValue = "5") limit: Int
): ResponseEntity<Map<String, List<String>>> {
    val suggestions = searchService.getSuggestions(q, limit)
    return ResponseEntity.ok(mapOf("suggestions" to suggestions))
}
```

**테스트**:

```bash
curl "http://localhost:8080/api/search/products/suggestions?q=삼성"
```

### 7.3 다중 카테고리 탐색

**목표**: 한 번의 검색으로 여러 카테고리 결과 표시

```kotlin
data class MultiCategorySearchResponse(
    val query: String,
    val categories: Map<String, List<ProductSearchResult>>
)

fun searchMultiCategory(query: String, topPerCategory: Int = 3): MultiCategorySearchResponse {
    // 전체 검색
    val allDocs = vectorStore.similaritySearch(
        SearchRequest.query(query)
            .withTopK(100)
            .withSimilarityThreshold(0.6)
    )

    // 카테고리별 그룹화
    val byCategory = allDocs.groupBy { it.metadata["category"]?.toString() ?: "기타" }

    // 각 카테고리에서 상위 N개
    val results = byCategory.mapValues { (category, docs) ->
        docs.take(topPerCategory).mapIndexed { index, doc ->
            documentToProductSearchResult(doc, index, SearchMode.SEMANTIC)
        }
    }

    return MultiCategorySearchResponse(query, results)
}
```

### 7.4 검색 히스토리 및 인기 검색어

**간단한 인메모리 구현**:

```kotlin
@Service
class SearchHistoryService {
    private val searchHistory = ConcurrentHashMap<String, AtomicInteger>()

    fun recordSearch(query: String) {
        searchHistory.computeIfAbsent(query) { AtomicInteger(0) }
            .incrementAndGet()
    }

    fun getPopularSearches(limit: Int = 10): List<Pair<String, Int>> {
        return searchHistory.entries
            .sortedByDescending { it.value.get() }
            .take(limit)
            .map { it.key to it.value.get() }
    }
}
```

**✅ 체크포인트**:
- [ ] 유사 제품 추천 기능 구현
- [ ] 자동완성 기능 추가
- [ ] 다중 카테고리 검색 구현
- [ ] 검색 히스토리 기능 추가

---

## Step 8: 프로덕션 준비

**목표**: 실제 서비스에 배포하기 위한 준비를 합니다.

### 8.1 환경 변수 설정

**application.yml**:

```yaml
spring:
  ai:
    google:
      genai:
        embedding:
          project-id: ${GOOGLE_PROJECT_ID:default-project}
          api-key: ${GOOGLE_API_KEY:your-api-key}
    vectorstore:
      elasticsearch:
        url: ${ELASTICSEARCH_URL:http://localhost:9200}
        index-name: ${ELASTICSEARCH_INDEX:product}
        dimensions: 768

server:
  port: ${SERVER_PORT:8080}
```

**Docker Compose 설정**:

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  elasticsearch:
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=true
      - ELASTIC_PASSWORD=${ELASTIC_PASSWORD}
    volumes:
      - esdata:/usr/share/elasticsearch/data

  app:
    build: .
    environment:
      - GOOGLE_PROJECT_ID=${GOOGLE_PROJECT_ID}
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  esdata:
```

### 8.2 에러 처리 및 로깅

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid request: ${e.message}")
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse("INVALID_REQUEST", e.message ?: "Invalid request"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", e)
        return ResponseEntity
            .status(500)
            .body(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 8.3 캐싱 전략

```kotlin
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("searchResults", "suggestions")
    }
}

@Service
class ProductSearchService(
    private val vectorStore: VectorStore
) {

    @Cacheable(value = ["searchResults"], key = "#request")
    fun search(request: ProductSearchRequest): ProductSearchResponse {
        // 검색 로직...
    }

    @Cacheable(value = ["suggestions"], key = "#query")
    fun getSuggestions(query: String, limit: Int): List<String> {
        // 제안 로직...
    }
}
```

### 8.4 모니터링 및 메트릭

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

**커스텀 메트릭**:

```kotlin
@Service
class ProductSearchService(
    private val vectorStore: VectorStore,
    private val meterRegistry: MeterRegistry
) {
    private val searchCounter = meterRegistry.counter("search.requests.total")
    private val searchTimer = meterRegistry.timer("search.duration")

    fun search(request: ProductSearchRequest): ProductSearchResponse {
        searchCounter.increment()

        return searchTimer.recordCallable {
            // 검색 로직...
        }!!
    }
}
```

### 8.5 성능 최적화

**1) 벡터 검색 최적화**:

```kotlin
// topK 값 조정
val documents = vectorStore.similaritySearch(
    SearchRequest.query(query)
        .withTopK(50)  // 너무 크지 않게 (100 → 50)
        .withSimilarityThreshold(threshold)
)
```

**2) 배치 처리**:

```kotlin
fun addProductsBatch(products: List<Product>) {
    val documents = products.map { it.toDocument() }

    // 배치 단위로 나누어 추가 (예: 100개씩)
    documents.chunked(100).forEach { batch ->
        vectorStore.add(batch)
    }
}
```

**3) 연결 풀 설정**:

```yaml
spring:
  elasticsearch:
    rest:
      connection-timeout: 5s
      read-timeout: 60s
    uris: http://localhost:9200
    connection-request-timeout: 5s
```

### 8.6 보안 설정

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }  // API 서버는 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/search/**").permitAll()
                  .requestMatchers("/actuator/**").hasRole("ADMIN")
                  .anyRequest().authenticated()
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("https://yourdomain.com")
        configuration.allowedMethods = listOf("GET", "POST")
        configuration.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

### 8.7 배포 체크리스트

- [ ] 환경 변수로 모든 시크릿 관리
- [ ] Elasticsearch 보안 활성화
- [ ] API 요청 제한 (Rate Limiting) 설정
- [ ] HTTPS 설정
- [ ] 로깅 레벨 조정 (INFO 이상)
- [ ] 에러 처리 및 모니터링
- [ ] 캐싱 전략 적용
- [ ] 성능 테스트 완료
- [ ] 백업 전략 수립
- [ ] 문서화 완료

**✅ 체크포인트**:
- [ ] 환경 변수 설정 완료
- [ ] 에러 처리 구현
- [ ] 캐싱 적용
- [ ] 모니터링 설정
- [ ] 보안 설정 완료

---

## 🎓 다음 단계

축하합니다! 튜토리얼을 완료했습니다. 이제 다음 단계로 나아가세요:

1. **[ARCHITECTURE.md](ARCHITECTURE.md)** - 시스템 아키텍처 심화 학습
2. **[CONCEPTS.md](CONCEPTS.md)** - 벡터 검색 이론 학습
3. **`docs/exercises/`** - 실습 문제 풀어보기
4. **프로젝트 확장** - 새로운 기능 추가 (이미지 검색, 다국어 지원 등)

## 📚 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Elasticsearch Vector Search Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html)
- [Google Gemini API Documentation](https://ai.google.dev/docs)
- [Vector Embeddings 이해하기](https://www.pinecone.io/learn/vector-embeddings/)

## 💬 피드백

튜토리얼에 대한 피드백이나 질문이 있다면 GitHub Issues로 남겨주세요!

---

**Happy Coding! 🚀**
