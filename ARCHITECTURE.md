# 시스템 아키텍처

> **Spring AI + Elasticsearch Vector Search 시스템 구조**
>
> 이 문서는 프로젝트의 전체 아키텍처, 컴포넌트 구조, 데이터 플로우를 상세히 설명합니다.

## 📋 목차

- [전체 아키텍처](#전체-아키텍처)
- [레이어 구조](#레이어-구조)
- [핵심 컴포넌트](#핵심-컴포넌트)
- [데이터 플로우](#데이터-플로우)
- [검색 프로세스](#검색-프로세스)
- [Spring AI 통합](#spring-ai-통합)
- [Elasticsearch 구조](#elasticsearch-구조)
- [확장성 고려사항](#확장성-고려사항)

---

## 전체 아키텍처

### 시스템 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│                   (HTTP REST API Clients)                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ HTTP Requests
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Controller Layer                         │  │
│  │              ProductSearchController                      │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │                   Service Layer                           │  │
│  │              ProductSearchService                         │  │
│  │    - Search Logic                                         │  │
│  │    - Filter Application                                   │  │
│  │    - Sorting & Pagination                                 │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │                   Domain Layer                            │  │
│  │    Product ──toDocument()──> Spring AI Document          │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │              Spring AI Vector Store                       │  │
│  │    - Embedding Generation (via Gemini API)               │  │
│  │    - Similarity Search                                    │  │
│  └────────────────────────┬─────────────────────────────────┘  │
└───────────────────────────┼─────────────────────────────────────┘
                            │
                            │ Elasticsearch Client
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Elasticsearch                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   product Index                           │  │
│  │  - Vector Field: embedding (768 dimensions)              │  │
│  │  - Metadata Fields: category, brand, price, etc.         │  │
│  │  - Content Field: text representation                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ External API Call
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Google Gemini API                           │
│                   (gemini-embedding-001)                         │
│              Text → 768-dimensional Vector                       │
└─────────────────────────────────────────────────────────────────┘
```

### 기술 스택 맵핑

| 레이어 | 기술 | 역할 |
|--------|------|------|
| **API Layer** | Spring Web MVC | REST 엔드포인트 제공 |
| **Service Layer** | Spring Service | 비즈니스 로직 처리 |
| **Domain Layer** | Kotlin Data Class | 도메인 모델 정의 |
| **AI Integration** | Spring AI | 임베딩 생성 및 벡터 검색 추상화 |
| **Vector Store** | Elasticsearch | 벡터 저장 및 유사도 검색 |
| **Embedding** | Google Gemini | 텍스트 → 벡터 변환 |

---

## 레이어 구조

### 1. API Layer (Controller)

**위치**: `src/main/kotlin/com/dokkabei97/springaielasticsearch/search/ProductSearchController.kt`

```kotlin
@RestController
@RequestMapping("/api/search")
class ProductSearchController(
    private val searchService: ProductSearchService
) {
    // REST 엔드포인트 정의
    // 요청 파라미터 검증
    // 응답 형식 변환
}
```

**책임**:
- HTTP 요청/응답 처리
- 요청 파라미터 검증 및 변환
- HTTP 상태 코드 관리
- 에러 응답 포매팅

**엔드포인트 구조**:
```
GET  /api/search/products                     # 일반 검색
POST /api/search/products/advanced            # 고급 검색
GET  /api/search/products/semantic            # 시맨틱 검색
GET  /api/search/products/category/{category} # 카테고리 검색
GET  /api/search/products/brand/{brand}       # 브랜드 검색
GET  /api/search/products/suggestions         # 검색 제안
GET  /api/search/test/korean                  # 한국어 테스트
```

### 2. Service Layer

**위치**: `src/main/kotlin/com/dokkabei97/springaielasticsearch/search/ProductSearchService.kt`

```kotlin
@Service
class ProductSearchService(
    private val vectorStore: VectorStore
) {
    fun search(request: ProductSearchRequest): ProductSearchResponse {
        // 1. 검색 모드 결정
        // 2. 쿼리 정규화
        // 3. 벡터 검색 실행
        // 4. 메타데이터 필터링
        // 5. 정렬 및 페이지네이션
        // 6. 응답 생성
    }
}
```

**책임**:
- 검색 비즈니스 로직
- 검색 모드 결정 (SEMANTIC vs HYBRID)
- 필터 적용
- 정렬 및 페이지네이션
- 결과 변환

**주요 메서드**:
```kotlin
// 메인 검색
fun search(request: ProductSearchRequest): ProductSearchResponse

// 검색 모드 결정
private fun determineSearchMode(request: ProductSearchRequest): SearchMode

// 쿼리 정규화
private fun normalizeQuery(query: String): String

// 필터 적용
private fun applyFilters(documents: List<Document>, request: ProductSearchRequest): List<Document>

// 정렬
private fun applySorting(documents: List<Document>, sortOption: SortOption): List<Document>

// 페이지네이션
private fun applyPagination(documents: List<Document>, page: Int, size: Int): List<Document>
```

### 3. Domain Layer

**위치**: `src/main/kotlin/com/dokkabei97/springaielasticsearch/domain/`

**Product.kt** - 도메인 모델
```kotlin
data class Product(
    val productName: String,
    val description: String,
    val brand: String,
    val maker: String,
    val category: String,
    val price: Int
) {
    companion object {
        fun of(/* params */): Product = Product(/* ... */)
    }
}
```

**ProductConvert.kt** - 변환 로직
```kotlin
fun Product.toDocument(): Document {
    val text = buildTextRepresentation()
    val metadata = buildMetadata()
    return Document(text, metadata)
}
```

**책임**:
- 비즈니스 도메인 모델 정의
- Product → Spring AI Document 변환
- 메타데이터 구성

### 4. DTO Layer

**위치**: `src/main/kotlin/com/dokkabei97/springaielasticsearch/search/dto/`

```
dto/
├── ProductSearchRequest.kt       # 검색 요청
├── ProductSearchResponse.kt      # 검색 응답
├── ProductSearchResult.kt        # 개별 검색 결과
└── enums/
    ├── SearchMode.kt             # SEMANTIC, HYBRID
    └── SortOption.kt             # 정렬 옵션
```

**ProductSearchRequest**:
```kotlin
data class ProductSearchRequest(
    val query: String,                        // 검색어
    val categories: List<String> = emptyList(), // 카테고리 필터
    val brands: List<String> = emptyList(),     // 브랜드 필터
    val makers: List<String> = emptyList(),     // 제조사 필터
    val minPrice: Int? = null,                  // 최소 가격
    val maxPrice: Int? = null,                  // 최대 가격
    val page: Int = 0,                          // 페이지 번호
    val size: Int = 10,                         // 페이지 크기
    val sortBy: SortOption = SortOption.RELEVANCE // 정렬
)
```

### 5. Configuration Layer

**위치**: `src/main/kotlin/com/dokkabei97/springaielasticsearch/config/`

**VectorStoreConfig.kt**:
```kotlin
@Configuration
class VectorStoreConfig {
    @Bean
    fun elasticsearchVectorStore(
        restClient: RestClient,
        embeddingClient: EmbeddingClient
    ): ElasticsearchVectorStore {
        return ElasticsearchVectorStore(
            ElasticsearchVectorStoreOptions.builder()
                .withRestClient(restClient)
                .withEmbeddingClient(embeddingClient)
                .withIndexName("product")
                .withDimensions(768)
                .withSimilarity(Similarity.COSINE)
                .build()
        )
    }
}
```

**application.yml**:
```yaml
spring:
  ai:
    vectorstore:
      elasticsearch:
        url: http://localhost:9200
        index-name: product
        dimensions: 768
        initialize-schema: true
    google:
      genai:
        embedding:
          model: gemini-embedding-001
          project-id: ${GOOGLE_PROJECT_ID}
          api-key: ${GOOGLE_API_KEY}
```

---

## 핵심 컴포넌트

### 1. Spring AI VectorStore

**역할**: 벡터 저장 및 검색의 추상화 레이어

```kotlin
interface VectorStore {
    // 문서 추가 (자동으로 임베딩 생성)
    fun add(documents: List<Document>)

    // 유사도 검색
    fun similaritySearch(request: SearchRequest): List<Document>

    // 문서 삭제
    fun delete(ids: List<String>)
}
```

**동작 방식**:
1. `add()` 호출 시 자동으로 Gemini API 호출하여 임베딩 생성
2. 생성된 임베딩과 메타데이터를 Elasticsearch에 저장
3. `similaritySearch()` 호출 시 쿼리도 임베딩으로 변환하여 검색

### 2. EmbeddingClient

**역할**: 텍스트를 벡터로 변환

```kotlin
interface EmbeddingClient {
    fun embed(text: String): List<Double>
    fun embed(texts: List<String>): List<List<Double>>
}
```

**Gemini 임베딩 특징**:
- 모델: `gemini-embedding-001`
- 차원: 768
- 최대 입력 길이: ~2048 토큰
- 한국어 지원: 우수

### 3. Elasticsearch Client

**역할**: 실제 벡터 저장소와의 통신

```kotlin
// Spring AI가 내부적으로 사용
RestClient → Elasticsearch REST API
```

**인덱스 구조**:
```json
{
  "mappings": {
    "properties": {
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      },
      "content": {
        "type": "text"
      },
      "metadata": {
        "type": "object",
        "properties": {
          "productName": {"type": "keyword"},
          "category": {"type": "keyword"},
          "brand": {"type": "keyword"},
          "maker": {"type": "keyword"},
          "price": {"type": "integer"}
        }
      }
    }
  }
}
```

---

## 데이터 플로우

### 1. 제품 데이터 인덱싱 플로우

```
┌─────────────────┐
│  Product 객체   │
│  (SampleData)   │
└────────┬────────┘
         │
         │ Product.toDocument()
         ▼
┌─────────────────┐
│ Spring AI       │
│ Document        │
│ - text          │
│ - metadata      │
└────────┬────────┘
         │
         │ vectorStore.add()
         ▼
┌─────────────────┐
│ EmbeddingClient │  ──HTTP──> ┌──────────────┐
│ (Gemini API)    │  <──────   │ Gemini API   │
└────────┬────────┘             └──────────────┘
         │                       (768-dim vector)
         │
         │ Generated Embedding
         ▼
┌─────────────────┐
│ Elasticsearch   │
│ REST Client     │
└────────┬────────┘
         │
         │ HTTP POST /_bulk
         ▼
┌─────────────────────────────────────┐
│     Elasticsearch Index             │
│  ┌───────────────────────────────┐ │
│  │ Document ID: abc123           │ │
│  │ - embedding: [0.023, ...]     │ │
│  │ - content: "제품명: 삼성..."  │ │
│  │ - metadata:                   │ │
│  │   - productName: "삼성..."    │ │
│  │   - category: "냉장고"        │ │
│  │   - price: 2500000            │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

### 2. 검색 쿼리 플로우

```
┌──────────────────┐
│ HTTP GET Request │
│ /api/search/     │
│ products?q=냉장고 │
└────────┬─────────┘
         │
         ▼
┌────────────────────────┐
│ ProductSearchController│
│ - 파라미터 검증         │
│ - DTO 변환              │
└────────┬───────────────┘
         │
         │ ProductSearchRequest
         ▼
┌────────────────────────┐
│ ProductSearchService   │
│ 1. determineSearchMode │  ─> SEMANTIC or HYBRID?
│ 2. normalizeQuery      │  ─> "냉장고" 정규화
│ 3. vectorStore.search  │
└────────┬───────────────┘
         │
         │ SearchRequest
         ▼
┌────────────────────────┐
│ EmbeddingClient        │  ──> Gemini API
│ "냉장고" → vector      │  <── [0.145, ...]
└────────┬───────────────┘
         │
         │ Query Vector
         ▼
┌────────────────────────┐
│ Elasticsearch          │
│ KNN Search             │
│ - script_score query   │
│ - cosine similarity    │
└────────┬───────────────┘
         │
         │ List<Document>
         ▼
┌────────────────────────┐
│ ProductSearchService   │
│ 4. applyFilters        │  ─> 카테고리, 가격 필터
│ 5. applySorting        │  ─> 관련도/가격/이름
│ 6. applyPagination     │  ─> page=0, size=10
└────────┬───────────────┘
         │
         │ List<ProductSearchResult>
         ▼
┌────────────────────────┐
│ ProductSearchResponse  │
│ - products             │
│ - totalResults         │
│ - searchMode           │
└────────┬───────────────┘
         │
         │ JSON Response
         ▼
┌──────────────────┐
│ HTTP Response    │
│ 200 OK           │
│ Content-Type:    │
│ application/json │
└──────────────────┘
```

---

## 검색 프로세스

### SEMANTIC 검색 모드

**조건**: 필터가 없는 순수 검색

```kotlin
// 1. 모드 결정
searchMode = SEMANTIC

// 2. Threshold 설정
threshold = 0.7  // 높은 유사도 요구

// 3. 벡터 검색
documents = vectorStore.similaritySearch(
    SearchRequest.query("냉장고")
        .withTopK(10)
        .withSimilarityThreshold(0.7)
)

// 4. 필터링 없음

// 5. 정렬 (관련도순)
sorted = documents.sortedByDescending { it.similarityScore }

// 6. 응답 반환
```

**Elasticsearch 쿼리**:
```json
{
  "query": {
    "script_score": {
      "query": {"match_all": {}},
      "script": {
        "source": "cosineSimilarity(params.query_vector, 'embedding') + 1.0",
        "params": {
          "query_vector": [0.145, 0.023, ...]
        }
      }
    }
  },
  "min_score": 1.7,
  "size": 10
}
```

### HYBRID 검색 모드

**조건**: 카테고리, 브랜드, 가격 등 필터 존재

```kotlin
// 1. 모드 결정
searchMode = HYBRID

// 2. Threshold 낮춤
threshold = 0.5  // 더 많은 결과 확보

// 3. 벡터 검색 (많은 결과)
documents = vectorStore.similaritySearch(
    SearchRequest.query("냉장고")
        .withTopK(100)  // 충분한 후보 확보
        .withSimilarityThreshold(0.5)
)

// 4. 메타데이터 필터링
filtered = documents.filter { doc ->
    // 카테고리 필터
    if (categories.isNotEmpty()) {
        doc.metadata["category"] in categories
    }

    // 브랜드 필터
    if (brands.isNotEmpty()) {
        doc.metadata["brand"] in brands
    }

    // 가격 필터
    val price = doc.metadata["price"].toInt()
    if (minPrice != null && price < minPrice) return@filter false
    if (maxPrice != null && price > maxPrice) return@filter false

    true
}

// 5. 정렬
sorted = applySorting(filtered, sortOption)

// 6. 페이지네이션
paginated = sorted.drop(page * size).take(size)
```

**처리 순서**:
```
벡터 검색 (100개)
    ↓
카테고리 필터 (30개 남음)
    ↓
브랜드 필터 (15개 남음)
    ↓
가격 필터 (10개 남음)
    ↓
정렬 (관련도/가격/이름)
    ↓
페이지네이션 (10개 반환)
```

---

## Spring AI 통합

### VectorStore 추상화

Spring AI는 다양한 벡터 데이터베이스를 동일한 인터페이스로 사용 가능:

```kotlin
// Elasticsearch
@Bean
fun elasticsearchVectorStore(...): ElasticsearchVectorStore

// 다른 Vector DB로 교체 가능 (인터페이스 동일)
// - PineconeVectorStore
// - ChromaVectorStore
// - WeaviateVectorStore
// - PgVectorStore
```

### EmbeddingClient 추상화

임베딩 모델도 교체 가능:

```kotlin
// Google Gemini (현재)
@Bean
fun geminiEmbeddingClient(...): GoogleGenAiEmbeddingClient

// 다른 모델로 교체 가능
// - OpenAiEmbeddingClient (text-embedding-ada-002)
// - AzureOpenAiEmbeddingClient
// - OllamaEmbeddingClient (로컬)
```

### 통합 이점

1. **벤더 종속성 감소**: 벡터 DB 교체 용이
2. **일관된 API**: 학습 곡선 감소
3. **자동 임베딩**: 수동 API 호출 불필요
4. **Spring 생태계**: DI, AOP, 트랜잭션 등 활용

---

## Elasticsearch 구조

### 인덱스 설정

```json
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "index": {
      "knn": true,
      "knn.algo_param.ef_search": 100
    }
  },
  "mappings": {
    "properties": {
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      },
      "content": {
        "type": "text",
        "analyzer": "standard"
      },
      "metadata": {
        "properties": {
          "productName": {"type": "keyword"},
          "description": {"type": "text"},
          "brand": {"type": "keyword"},
          "maker": {"type": "keyword"},
          "category": {"type": "keyword"},
          "price": {"type": "integer"},
          "type": {"type": "keyword"}
        }
      }
    }
  }
}
```

### Similarity 옵션

| Similarity | 범위 | 특징 | 사용 |
|------------|------|------|------|
| **cosine** | -1 ~ 1 | 방향 유사도 | 텍스트 (현재 사용) |
| dot_product | -∞ ~ ∞ | 크기 고려 | 이미지 |
| l2_norm | 0 ~ ∞ | 유클리드 거리 | 좌표 |

### KNN vs ANN

- **KNN** (K-Nearest Neighbors): 정확하지만 느림
- **ANN** (Approximate NN): 빠르지만 근사치

Elasticsearch는 **HNSW** (Hierarchical Navigable Small World) 알고리즘 사용:
- 그래프 기반 ANN
- O(log N) 검색 속도
- 높은 정확도 유지

---

## 확장성 고려사항

### 1. 대용량 데이터 처리

**현재**: 54개 제품 (테스트용)

**확장 시나리오**: 100만 개 제품

**필요한 변경**:

```kotlin
// 1. 배치 인덱싱
fun indexProductsBatch(products: List<Product>) {
    products.chunked(1000).forEach { batch ->
        val documents = batch.map { it.toDocument() }
        vectorStore.add(documents)
    }
}

// 2. 비동기 처리
@Async
fun indexProductsAsync(products: List<Product>) {
    // 백그라운드에서 인덱싱
}

// 3. Elasticsearch 샤딩
// application.yml
spring:
  ai:
    vectorstore:
      elasticsearch:
        number-of-shards: 5      # 샤드 분산
        number-of-replicas: 1    # 복제본
```

### 2. 검색 성능 최적화

**인덱스 최적화**:
```bash
# Force merge (읽기 전용 인덱스)
POST /product/_forcemerge?max_num_segments=1
```

**캐싱 전략**:
```kotlin
@Cacheable(value = ["searchResults"], key = "#request")
fun search(request: ProductSearchRequest): ProductSearchResponse
```

**응답 시간 목표**:
- p50: < 100ms
- p95: < 500ms
- p99: < 1000ms

### 3. 고가용성 (HA)

```yaml
# docker-compose.prod.yml
services:
  es01:
    # 마스터 노드
  es02:
    # 데이터 노드 1
  es03:
    # 데이터 노드 2

  app1:
    # 애플리케이션 인스턴스 1
  app2:
    # 애플리케이션 인스턴스 2

  loadbalancer:
    image: nginx
    # 로드 밸런서
```

### 4. 모니터링

**메트릭 수집**:
```kotlin
@Service
class ProductSearchService(
    private val meterRegistry: MeterRegistry
) {
    fun search(request: ProductSearchRequest): ProductSearchResponse {
        // 검색 횟수
        meterRegistry.counter("search.requests",
            "mode", searchMode.name).increment()

        // 검색 지연시간
        return meterRegistry.timer("search.latency").recordCallable {
            performSearch(request)
        }!!
    }
}
```

**주요 메트릭**:
- `search.requests.total`: 총 검색 횟수
- `search.latency`: 검색 지연시간
- `search.results.count`: 평균 결과 수
- `elasticsearch.query.time`: ES 쿼리 시간
- `gemini.api.calls`: Gemini API 호출 횟수

---

## 아키텍처 진화 방향

### 단기 (현재 → 3개월)

1. **캐싱 레이어 추가**
   - Redis 도입
   - 인기 검색어 캐싱

2. **검색 품질 개선**
   - 리랭킹 알고리즘
   - 사용자 피드백 반영

3. **모니터링 강화**
   - Prometheus + Grafana
   - 알림 시스템

### 중기 (3~6개월)

1. **다국어 지원**
   - 영어, 일본어 등
   - 언어별 임베딩 모델

2. **개인화 검색**
   - 사용자 프로필 기반
   - 검색 히스토리 활용

3. **A/B 테스트 프레임워크**
   - 검색 알고리즘 실험
   - 메트릭 비교

### 장기 (6개월~1년)

1. **멀티모달 검색**
   - 이미지 + 텍스트
   - 음성 검색

2. **추천 시스템**
   - 협업 필터링
   - 벡터 기반 추천

3. **실시간 인덱싱**
   - Kafka + Stream Processing
   - 제품 업데이트 즉시 반영

---

## 참고 문서

- [Spring AI Architecture](https://docs.spring.io/spring-ai/reference/concepts.html)
- [Elasticsearch Vector Search Architecture](https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html)
- [HNSW Algorithm](https://arxiv.org/abs/1603.09320)
- [Vector Database Comparison](https://www.pinecone.io/learn/vector-database/)

---

**이 아키텍처는 학습 및 실험 목적으로 설계되었으며, 프로덕션 환경에서는 추가적인 고려사항이 필요합니다.**
