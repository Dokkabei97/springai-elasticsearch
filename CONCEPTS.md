# 핵심 개념 가이드

> **Vector Search, Embeddings, RAG 완벽 이해**
>
> 이 문서는 벡터 검색 시스템을 이해하는 데 필요한 핵심 개념을 검색 엔지니어 관점에서 설명합니다.

## 📋 목차

- [벡터 검색이란?](#벡터-검색이란)
- [임베딩 (Embeddings)](#임베딩-embeddings)
- [유사도 측정](#유사도-측정)
- [벡터 데이터베이스](#벡터-데이터베이스)
- [시맨틱 검색 vs 키워드 검색](#시맨틱-검색-vs-키워드-검색)
- [하이브리드 검색](#하이브리드-검색)
- [RAG (Retrieval-Augmented Generation)](#rag-retrieval-augmented-generation)
- [프로덕션 고려사항](#프로덕션-고려사항)

---

## 벡터 검색이란?

### 전통적인 검색의 한계

**키워드 기반 검색**:
```
검색어: "음식을 신선하게 보관하는 제품"

전통적 방법:
1. 토큰화: ["음식", "신선하게", "보관", "제품"]
2. 역인덱스 검색: 각 토큰이 포함된 문서 찾기
3. 결과: 해당 단어들이 있는 문서만 반환

문제점:
❌ "냉장고"라는 단어가 없으면 냉장고 제품 못 찾음
❌ 의미는 같지만 다른 표현은 매칭 안 됨
❌ 동의어 처리 어려움
```

**벡터 기반 검색**:
```
검색어: "음식을 신선하게 보관하는 제품"

벡터 방법:
1. 검색어를 768차원 벡터로 변환
   [0.23, -0.45, 0.78, ..., 0.12]

2. 모든 제품도 미리 벡터로 변환되어 있음
   냉장고: [0.25, -0.43, 0.76, ..., 0.15]

3. 벡터 간 거리/유사도 계산
   cosine similarity = 0.92 (매우 유사!)

4. 결과: "냉장고"라는 단어 없이도 냉장고 검색 ✅
```

### 벡터 검색의 핵심 아이디어

**"의미가 비슷한 텍스트는 벡터 공간에서 가까이 위치한다"**

```
3차원 공간 시각화 (실제로는 768차원):

        냉장고 •
              / \
             /   \
    김치냉장고 •   • 식품보관함
            |
            |
        와인셀러 •


                              • 노트북
                             /
                            /
                    스마트폰 •
```

**특징**:
- 동의어, 유사어 자동 처리
- 문맥 이해
- 다국어 지원 (같은 의미는 가까운 벡터)
- 오타에 강함

---

## 임베딩 (Embeddings)

### 임베딩이란?

**정의**: 텍스트(또는 이미지, 오디오)를 고차원 벡터로 변환하는 과정

```
입력 (텍스트):
"삼성 비스포크 냉장고는 4도어 대용량 냉장고입니다"

↓ 임베딩 모델 (Gemini, OpenAI 등)

출력 (벡터):
[
  0.0234,   // 차원 1
  -0.1456,  // 차원 2
  0.6789,   // 차원 3
  ...
  0.2341    // 차원 768
]
```

### 왜 고차원일까?

**차원 수와 표현력**:

```
2차원 예시 (너무 단순):
- 차원 1: "가전제품인가?"  (0: 아니오, 1: 예)
- 차원 2: "비싼가?"        (0: 싸다, 1: 비싸다)

문제: 복잡한 의미를 표현하기 어려움

768차원:
- 차원 1: "냉장 관련성"
- 차원 2: "고급스러움"
- 차원 3: "에너지 효율"
- 차원 4: "스마트 기능"
- ...
- 차원 768: "브랜드 신뢰도"

→ 미묘한 의미 차이까지 표현 가능
```

### 임베딩 모델 비교

| 모델 | 제공자 | 차원 | 특징 | 한국어 |
|------|--------|------|------|--------|
| **gemini-embedding-001** | Google | 768 | 다국어 우수 | ⭐⭐⭐⭐⭐ |
| text-embedding-ada-002 | OpenAI | 1536 | 높은 정확도 | ⭐⭐⭐⭐ |
| text-embedding-3-small | OpenAI | 512 | 빠르고 저렴 | ⭐⭐⭐⭐ |
| multilingual-e5-large | Microsoft | 1024 | 오픈소스 | ⭐⭐⭐ |
| ko-sbert | 커뮤니티 | 768 | 한국어 전용 | ⭐⭐⭐⭐⭐ |

**이 프로젝트 선택**: `gemini-embedding-001`
- 이유: 한국어 성능 우수, Spring AI 공식 지원, 무료 티어

### 임베딩 생성 과정

```kotlin
// 1. Product 객체
val product = Product(
    productName = "삼성 비스포크 냉장고",
    description = "4도어 대용량 냉장고",
    brand = "삼성",
    category = "냉장고",
    price = 2500000
)

// 2. 자연어 텍스트 구성
val text = """
    제품명: 삼성 비스포크 냉장고
    설명: 4도어 대용량 냉장고
    브랜드: 삼성
    카테고리: 냉장고
    가격: 2500000원
""".trimIndent()

// 3. Gemini API 호출
val embedding = embeddingClient.embed(text)
// 결과: [0.023, -0.145, 0.678, ..., 0.234] (768개 숫자)

// 4. Elasticsearch 저장
vectorStore.add(Document(text, metadata, embedding))
```

### 임베딩 품질을 높이는 팁

**1. 풍부한 컨텍스트 제공**

❌ 나쁜 예:
```kotlin
val text = product.productName  // "삼성 비스포크"
```

✅ 좋은 예:
```kotlin
val text = """
    제품명: ${product.productName}
    상세설명: ${product.description}
    브랜드: ${product.brand}
    카테고리: ${product.category}
    가격대: ${formatPrice(product.price)}
    주요특징: ${extractFeatures(product)}
""".trimIndent()
```

**2. 일관된 형식 유지**

모든 제품을 동일한 형식으로 변환:
```
제품명: ...
설명: ...
브랜드: ...
```

**3. 의미 있는 정보만 포함**

❌ 불필요한 정보:
```
ID: 12345
생성일시: 2025-01-01
수정자: admin
```

✅ 검색에 유용한 정보:
```
제품 특징: AI 온도 조절, 스마트폰 연동
사용 용도: 가정용, 대가족
에너지등급: 1등급
```

---

## 유사도 측정

### Cosine Similarity (코사인 유사도)

**정의**: 두 벡터 사이의 각도를 측정 (방향의 유사도)

```
수학 공식:
similarity = cos(θ) = (A · B) / (||A|| × ||B||)

A: 검색 쿼리 벡터
B: 제품 벡터
θ: 두 벡터 사이의 각도
```

**시각화**:
```
        벡터 A
        ↗
       /  ) θ (작은 각도)
      /  ↗
     / ↗
    ↗ 벡터 B

cos(0°) = 1.0   (동일)
cos(30°) = 0.87  (매우 유사)
cos(60°) = 0.5   (다소 유사)
cos(90°) = 0.0   (무관)
```

**예시**:
```kotlin
// 검색어
query = "스마트 냉장고"
queryVector = [0.8, 0.6, 0.1, ...]

// 제품 1: 삼성 스마트 냉장고
product1Vector = [0.82, 0.58, 0.12, ...]
similarity1 = cosineSimilarity(queryVector, product1Vector)
// 결과: 0.95 (매우 유사!)

// 제품 2: LG 세탁기
product2Vector = [0.1, 0.3, 0.9, ...]
similarity2 = cosineSimilarity(queryVector, product2Vector)
// 결과: 0.25 (관련 없음)
```

### 다른 유사도 측정 방법

**1. Dot Product (내적)**

```
similarity = A · B = a₁×b₁ + a₂×b₂ + ... + aₙ×bₙ
```

- 특징: 벡터의 크기(magnitude)도 고려
- 사용: 이미지 임베딩, 추천 시스템
- 범위: -∞ ~ ∞

**2. Euclidean Distance (유클리드 거리)**

```
distance = √[(a₁-b₁)² + (a₂-b₂)² + ... + (aₙ-bₙ)²]
```

- 특징: 실제 거리 측정
- 사용: 지리 좌표, 색상 공간
- 범위: 0 ~ ∞ (작을수록 유사)

**왜 Cosine Similarity를 사용하나?**

```
예시:
문장 A: "냉장고"                  → 짧은 벡터 (크기: 1.0)
문장 B: "삼성 비스포크 4도어 냉장고" → 긴 벡터 (크기: 2.5)

Cosine Similarity:
→ 방향만 비교: 0.98 (매우 유사!) ✅

Euclidean Distance:
→ 크기 차이 때문에: 1.5 (멀다고 판단) ❌
```

### Threshold (임계값) 설정

**이 프로젝트 설정**:
```kotlin
// SEMANTIC 모드
threshold = 0.7  // 70% 이상 유사

// HYBRID 모드
threshold = 0.5  // 50% 이상 유사 (더 많은 결과 확보)
```

**Threshold 조정 가이드**:

| Threshold | 결과 | 품질 | 사용 시나리오 |
|-----------|------|------|--------------|
| 0.9 ~ 1.0 | 매우 적음 | 매우 높음 | 정확도 중요 (법률, 의료) |
| 0.7 ~ 0.9 | 적당 | 높음 | 일반 검색 (현재) |
| 0.5 ~ 0.7 | 많음 | 중간 | 탐색적 검색, 추천 |
| 0.3 ~ 0.5 | 매우 많음 | 낮음 | 관련 항목 모두 찾기 |

**실험 예시**:
```bash
# Threshold 0.9 - 매우 정확한 매칭만
curl "http://localhost:8080/api/search/products?q=삼성%20비스포크"
# 결과: 3개 (정확히 "삼성 비스포크" 제품만)

# Threshold 0.5 - 관련 제품 모두
curl "http://localhost:8080/api/search/products?q=냉장고"
# 결과: 25개 (모든 냉장고 관련 제품)
```

---

## 벡터 데이터베이스

### 왜 일반 DB로는 안 될까?

**문제: 벡터 검색은 계산 집약적**

```
일반 SQL:
SELECT * FROM products WHERE category = '냉장고'
→ 인덱스 사용: O(log N) - 빠름 ✅

벡터 검색 (일반 DB):
SELECT * FROM products
ORDER BY cosine_similarity(embedding, query_vector)
→ 모든 행을 스캔: O(N) - 느림 ❌

100만 개 제품:
- 일반 쿼리: ~10ms
- 벡터 검색 (naive): ~30초!
```

### 벡터 DB의 해법: ANN

**ANN (Approximate Nearest Neighbors)**

정확한 답 대신 "충분히 가까운" 답을 빠르게 찾기:

```
전체 검색 (KNN):
- 100만 개 모두 비교
- 정확도: 100%
- 속도: 30초 ❌

근사 검색 (ANN):
- ~1000개만 비교
- 정확도: 95%
- 속도: 50ms ✅
```

### Elasticsearch의 HNSW 알고리즘

**HNSW (Hierarchical Navigable Small World)**

```
계층적 그래프 구조:

Level 2 (최상위):  A ————— B

Level 1 (중간):    A — C — D — B — E

Level 0 (하위):    A-C-F-G-D-H-I-B-E-J-K

검색 과정:
1. 최상위 레벨에서 대략적 위치 찾기
2. 아래 레벨로 내려가며 정밀 검색
3. 최하위 레벨에서 최종 후보 선택

복잡도: O(log N)
```

**설정**:
```yaml
# Elasticsearch 인덱스 설정
{
  "mappings": {
    "properties": {
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,           # HNSW 인덱스 사용
        "similarity": "cosine",  # 유사도 함수
        "index_options": {
          "type": "hnsw",
          "m": 16,               # 연결 수 (높을수록 정확, 느림)
          "ef_construction": 100 # 인덱싱 품질 (높을수록 정확)
        }
      }
    }
  }
}
```

### 벡터 DB 비교

| DB | 알고리즘 | 특징 | 사용 |
|----|---------|------|------|
| **Elasticsearch** | HNSW | 범용, 하이브리드 검색 | 이 프로젝트 |
| Pinecone | HNSW | 전용 벡터 DB, 관리형 | 프로덕션 |
| Weaviate | HNSW | GraphQL, 오픈소스 | 복잡한 쿼리 |
| Milvus | IVF, HNSW | 대규모, 분산 | 엔터프라이즈 |
| pgvector | IVF | PostgreSQL 확장 | 기존 PG 활용 |
| Chroma | HNSW | 경량, RAG 특화 | 개발/테스트 |

---

## 시맨틱 검색 vs 키워드 검색

### 비교표

| 특성 | 키워드 검색 | 시맨틱 검색 |
|------|------------|------------|
| **원리** | 단어 매칭 | 의미 유사도 |
| **정확도** | 정확한 단어만 | 유사한 의미도 |
| **동의어** | 수동 등록 필요 | 자동 처리 |
| **오타** | 매칭 실패 | 어느 정도 허용 |
| **자연어 질문** | 어려움 | 우수 |
| **속도** | 매우 빠름 | 상대적으로 느림 |
| **비용** | 낮음 | 높음 (임베딩 API) |

### 실제 예시 비교

**검색 시나리오**: "가성비 좋은 냉장고"

**키워드 검색 (Elasticsearch match)**:
```json
GET /product/_search
{
  "query": {
    "match": {
      "content": "가성비 좋은 냉장고"
    }
  }
}

결과:
1. "가성비 좋은 김치냉장고" ✅
2. "냉장고 좋은 성능" ✅
3. "성능 좋은 제품" ❌ (냉장고 단어 없음)
```

**시맨틱 검색 (Vector Search)**:
```kotlin
vectorStore.similaritySearch("가성비 좋은 냉장고")

결과:
1. "합리적인 가격의 삼성 냉장고" ✅ (동의어 인식)
2. "저렴한 LG 냉장고" ✅ (의미 유사)
3. "중저가 냉장고 추천" ✅ ("가성비" 단어 없어도)
```

### 언제 무엇을 사용할까?

**키워드 검색이 좋은 경우**:
- 정확한 제품명 검색 (SKU, 모델명)
- 카테고리, 브랜드 필터링
- 실시간성 중요
- 예산 제약

**시맨틱 검색이 좋은 경우**:
- 자연어 질문 ("어떤 냉장고가 좋을까?")
- 탐색적 검색
- 다국어 검색
- 추천 시스템

**결론**: 둘 다 사용! (하이브리드 검색)

---

## 하이브리드 검색

### 개념

**"벡터 검색의 의미 이해 + 메타데이터 필터링의 정확성"**

```
┌─────────────────────────────────────────┐
│         사용자 검색 의도                 │
│  "100만원 이하의 삼성 스마트 냉장고"     │
└────────────┬────────────────────────────┘
             │
    ┌────────┴─────────┐
    ▼                  ▼
┌─────────┐      ┌──────────┐
│ 시맨틱   │      │ 필터     │
│ 검색     │      │          │
│"스마트   │      │brand=삼성 │
│냉장고"   │      │price≤100만│
└────┬────┘      └─────┬────┘
     │                 │
     └────────┬────────┘
              ▼
       ┌──────────────┐
       │ 하이브리드    │
       │ 결과         │
       └──────────────┘
```

### 구현 전략

**Strategy 1: Filter then Search**
```
1. 먼저 메타데이터 필터 (brand=삼성, price≤100만)
2. 필터링된 결과 내에서 벡터 검색

장점: 검색 범위 축소, 빠름
단점: 필터가 너무 strict하면 결과 없을 수 있음
```

**Strategy 2: Search then Filter (이 프로젝트)**
```
1. 먼저 벡터 검색으로 관련 제품 100개 확보
2. 메타데이터 필터 적용 (brand, price 등)
3. 정렬 및 상위 10개 반환

장점: 더 많은 후보에서 선택, 품질 높음
단점: 약간 느림
```

### 코드 예시

```kotlin
fun hybridSearch(
    query: String,
    filters: Map<String, Any>
): List<Product> {
    // 1. 벡터 검색 (넉넉하게)
    val candidates = vectorStore.similaritySearch(
        SearchRequest.query(query)
            .withTopK(100)
            .withSimilarityThreshold(0.5)  // 낮은 threshold
    )

    // 2. 메타데이터 필터링
    val filtered = candidates.filter { doc ->
        // 브랜드 필터
        if (filters["brand"] != null) {
            doc.metadata["brand"] == filters["brand"]
        }

        // 가격 필터
        val price = doc.metadata["price"]?.toString()?.toInt()
        if (price != null) {
            val minPrice = filters["minPrice"] as? Int
            val maxPrice = filters["maxPrice"] as? Int

            if (minPrice != null && price < minPrice) return@filter false
            if (maxPrice != null && price > maxPrice) return@filter false
        }

        true
    }

    // 3. 정렬 (관련도 + 기타 요소)
    val sorted = filtered.sortedByDescending { doc ->
        var score = doc.similarityScore

        // 인기 브랜드 보너스
        if (doc.metadata["brand"] in listOf("삼성", "LG")) {
            score += 0.05
        }

        score
    }

    // 4. 상위 N개 반환
    return sorted.take(10).map { convertToProduct(it) }
}
```

### 성능 최적화

**문제**: 벡터 검색 100개 + 필터링은 비효율적일 수 있음

**해결책**: Elasticsearch의 하이브리드 쿼리

```json
POST /product/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "script_score": {
            "query": {"match_all": {}},
            "script": {
              "source": "cosineSimilarity(params.query_vector, 'embedding') + 1.0",
              "params": {"query_vector": [0.1, 0.2, ...]}
            }
          }
        }
      ],
      "filter": [
        {"term": {"metadata.brand": "삼성"}},
        {"range": {"metadata.price": {"lte": 1000000}}}
      ]
    }
  }
}
```

**장점**:
- Elasticsearch가 효율적으로 처리
- 인덱스 활용
- 병렬 처리

---

## RAG (Retrieval-Augmented Generation)

### RAG란?

**정의**: 검색(Retrieval)과 생성(Generation)을 결합한 AI 패턴

```
전통적 LLM:
사용자: "삼성 비스포크 냉장고 어때?"
LLM: "잘 모르겠습니다" (학습 데이터에 없음)

RAG:
사용자: "삼성 비스포크 냉장고 어때?"
    ↓
1. 벡터 검색으로 관련 제품 정보 찾기
    ↓
2. 찾은 정보를 LLM에 컨텍스트로 제공
    ↓
LLM: "삼성 비스포크 냉장고는 4도어 대용량으로..."
     (검색된 정보 기반 답변)
```

### RAG 아키텍처

```
┌─────────────┐
│ 사용자 질문  │
│"냉장고 추천" │
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ 1. Retrieval     │
│ Vector Search    │
│ ↓                │
│ 관련 제품 3개     │
│ 검색             │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ 2. Augmentation  │
│ 프롬프트 구성     │
│ ↓                │
│ "다음 제품 정보를 │
│  바탕으로 추천..." │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ 3. Generation    │
│ LLM 호출         │
│ ↓                │
│ 자연어 답변 생성  │
└──────────────────┘
```

### 이 프로젝트에 RAG 추가하기

**현재**: 검색 결과만 반환

**RAG 추가 후**: 검색 + 자연어 답변

```kotlin
@Service
class ProductRAGService(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient  // Gemini Chat API
) {
    fun answerQuestion(question: String): String {
        // 1. Retrieval: 관련 제품 검색
        val relatedProducts = vectorStore.similaritySearch(
            SearchRequest.query(question)
                .withTopK(3)
                .withSimilarityThreshold(0.7)
        )

        // 2. Augmentation: 프롬프트 구성
        val context = relatedProducts.joinToString("\n") { doc ->
            doc.content
        }

        val prompt = """
            사용자 질문: $question

            참고 제품 정보:
            $context

            위 정보를 바탕으로 사용자 질문에 친절하게 답변해주세요.
        """.trimIndent()

        // 3. Generation: LLM 호출
        val response = chatClient.call(prompt)

        return response.result.output.content
    }
}
```

**API 엔드포인트**:
```kotlin
@GetMapping("/ask")
fun askQuestion(@RequestParam q: String): String {
    return ragService.answerQuestion(q)
}
```

**사용 예시**:
```bash
curl "http://localhost:8080/api/search/ask?q=100만원으로%20살%20수%20있는%20냉장고%20추천해줘"

응답:
"100만원 예산이시군요. LG 디오스 냉장고가 적합할 것 같습니다.
이 제품은 850,000원으로 예산 내에 있으며, 2도어 구조로
일반 가정에서 사용하기 좋습니다. 에너지 효율도 1등급이라
전기료 절감에도 도움이 됩니다..."
```

### RAG vs Fine-tuning

| 특성 | RAG | Fine-tuning |
|------|-----|-------------|
| **데이터 업데이트** | 즉시 (검색만) | 재학습 필요 |
| **비용** | 낮음 | 높음 |
| **정확도** | 높음 (출처 명확) | 매우 높음 |
| **할루시네이션** | 적음 | 있음 |
| **사용 사례** | Q&A, 문서 검색 | 전문 도메인 |

**이 프로젝트**: RAG가 더 적합
- 제품 정보 자주 변경
- 출처 추적 가능
- 비용 효율적

---

## 프로덕션 고려사항

### 1. 임베딩 비용 관리

**문제**: Gemini API 호출 비용

```
100만 개 제품:
- 최초 인덱싱: 100만 번 API 호출
- 검색 쿼리: 매 검색마다 1번 API 호출
```

**해결책**:

```kotlin
// 1. 배치 처리
fun embedBatch(texts: List<String>): List<List<Double>> {
    return embeddingClient.embed(texts)  // 한 번에 처리
}

// 2. 캐싱
@Cacheable("embeddings")
fun getEmbedding(text: String): List<Double> {
    return embeddingClient.embed(text)
}

// 3. 자주 검색되는 쿼리 미리 임베딩
val popularQueries = listOf("냉장고", "세탁기", ...)
popularQueries.forEach { query ->
    cache.put(query, embeddingClient.embed(query))
}
```

### 2. 검색 품질 모니터링

**메트릭**:

```kotlin
@Service
class SearchQualityMonitor {
    // 1. 클릭률 (CTR)
    fun trackClick(query: String, clickedProduct: String, position: Int)

    // 2. 평균 관련도 점수
    fun trackRelevanceScore(query: String, avgScore: Double)

    // 3. 제로 결과 비율
    fun trackZeroResults(query: String)

    // 4. 검색 지연시간
    fun trackLatency(query: String, latencyMs: Long)
}
```

### 3. A/B 테스트

```kotlin
@Service
class SearchExperiment {
    fun search(query: String, userId: String): SearchResponse {
        // 50%는 기존 알고리즘
        // 50%는 새 알고리즘
        return if (userId.hashCode() % 2 == 0) {
            searchV1(query)  // threshold=0.7
        } else {
            searchV2(query)  // threshold=0.6
        }
    }
}
```

### 4. 보안

**API 키 보호**:
```yaml
# 환경 변수 사용
spring.ai.google.genai.embedding.api-key: ${GOOGLE_API_KEY}

# 절대 하지 말것
spring.ai.google.genai.embedding.api-key: "AIza..."  # ❌ 노출!
```

**Rate Limiting**:
```kotlin
@Configuration
class RateLimitConfig {
    @Bean
    fun rateLimiter(): RateLimiter {
        return RateLimiter.create(100.0)  // 초당 100 요청
    }
}
```

### 5. 스케일링

**수평 확장**:
```
┌─────────────┐
│ Load        │
│ Balancer    │
└──────┬──────┘
       │
   ┌───┴───┬───────┬───────┐
   ▼       ▼       ▼       ▼
┌──────┐┌──────┐┌──────┐┌──────┐
│ App 1││ App 2││ App 3││ App 4│
└───┬──┘└───┬──┘└───┬──┘└───┬──┘
    └───────┴───────┴───────┘
            │
    ┌───────▼────────┐
    │ Elasticsearch  │
    │ Cluster        │
    │ (3 nodes)      │
    └────────────────┘
```

---

## 학습 리소스

### 추천 논문

1. **Attention Is All You Need** - Transformer 아키텍처 (임베딩의 기초)
2. **BERT: Pre-training of Deep Bidirectional Transformers** - 문맥 임베딩
3. **Efficient and robust approximate nearest neighbor search** - HNSW 알고리즘

### 추천 강의

- [Stanford CS224N: NLP with Deep Learning](https://web.stanford.edu/class/cs224n/)
- [Pinecone Learning Center](https://www.pinecone.io/learn/)
- [Google Cloud Vector Search](https://cloud.google.com/vertex-ai/docs/vector-search/)

### 실습 자료

- **이 프로젝트의 [TUTORIAL.md](TUTORIAL.md)** - 단계별 실습
- **[docs/exercises/](docs/exercises/)** - 연습 문제

---

## 요약

| 개념 | 핵심 포인트 |
|------|-----------|
| **벡터 검색** | 의미 기반 검색, 키워드 제약 극복 |
| **임베딩** | 텍스트 → 고차원 벡터, 의미 보존 |
| **유사도** | Cosine similarity, 방향 비교 |
| **벡터 DB** | HNSW 알고리즘, 빠른 ANN 검색 |
| **하이브리드** | 벡터 검색 + 메타데이터 필터 |
| **RAG** | 검색 + LLM, 동적 지식 활용 |

---

**이 문서를 읽었다면, [TUTORIAL.md](TUTORIAL.md)로 실습을 시작해보세요!**
