# Spring AI + Elasticsearch Vector Search

> **벡터 검색 시스템 구축을 위한 종합 학습 프로젝트**
> Spring AI와 Elasticsearch를 활용한 한국어 제품 검색 및 추천 시스템

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.0--M1-blue.svg)](https://spring.io/projects/spring-ai)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-purple.svg)](https://kotlinlang.org/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.1.2-orange.svg)](https://www.elastic.co/)

## 📚 학습 자료

이 프로젝트는 **검색 엔지니어**를 위한 종합 학습 자료입니다:

- **[개념 가이드](CONCEPTS.md)** - Vector Search, Embeddings, RAG의 기본 개념
- **[튜토리얼](TUTORIAL.md)** - 단계별 실습 가이드 (초급 → 고급)
- **[아키텍처](ARCHITECTURE.md)** - 시스템 설계 및 데이터 플로우
- **[API 가이드](API_GUIDE.md)** - REST API 사용법 및 예제
- **[FAQ](FAQ.md)** - 자주 묻는 질문 및 문제 해결

## 🎯 프로젝트 목적

이 프로젝트는 다음을 학습하고 실습할 수 있도록 설계되었습니다:

1. **Spring AI 기초** - Spring AI 프레임워크의 핵심 개념과 사용법
2. **벡터 임베딩** - Google Gemini API를 활용한 텍스트 임베딩 생성
3. **벡터 데이터베이스** - Elasticsearch를 Vector Store로 활용
4. **시맨틱 검색** - 의미 기반 검색 구현 (키워드 검색 vs 벡터 검색)
5. **하이브리드 검색** - 벡터 검색 + 메타데이터 필터링 조합
6. **한국어 처리** - 한국어 텍스트의 벡터 임베딩 및 검색
7. **프로덕션 패턴** - 페이지네이션, 정렬, 필터링 등 실전 기능

## ✨ 주요 기능

### 검색 기능
- ✅ **시맨틱 검색** - 의미 기반 유사도 검색 (Cosine Similarity)
- ✅ **하이브리드 검색** - 벡터 검색 + 메타데이터 필터링
- ✅ **다중 필터** - 카테고리, 브랜드, 제조사, 가격 범위
- ✅ **정렬 옵션** - 관련도, 가격, 이름 기준 정렬
- ✅ **페이지네이션** - 효율적인 결과 탐색

### 기술적 특징
- ✅ **768차원 벡터** - Gemini embedding-001 모델 사용
- ✅ **Cosine Similarity** - 코사인 유사도 기반 매칭
- ✅ **자동 스키마** - Elasticsearch 인덱스 자동 생성
- ✅ **한국어 최적화** - 한국어 텍스트 처리 및 검색
- ✅ **샘플 데이터** - 54개 제품 데이터 자동 로딩

## 🛠 기술 스택

| 카테고리 | 기술 | 버전 | 용도 |
|---------|------|------|------|
| **언어** | Kotlin | 1.9.25 | 주 개발 언어 |
| **프레임워크** | Spring Boot | 3.5.6 | 애플리케이션 프레임워크 |
| **AI** | Spring AI | 1.1.0-M1 | AI 통합 프레임워크 |
| **임베딩** | Google Gemini | embedding-001 | 768차원 벡터 생성 |
| **Vector DB** | Elasticsearch | 9.1.2 | 벡터 저장 및 검색 |
| **빌드** | Gradle | 8.x | 빌드 도구 (Kotlin DSL) |
| **Java** | OpenJDK | 21 | 런타임 환경 |

### 주요 의존성

```kotlin
// Spring AI - Vector Store
implementation("org.springframework.ai:spring-ai-starter-vector-store-elasticsearch:1.1.0-M1")

// Spring AI - Gemini Embeddings
implementation("org.springframework.ai:spring-ai-starter-model-google-genai-embedding:1.1.0-M1")

// Elasticsearch Client
implementation("co.elastic.clients:elasticsearch-java:9.1.2")
```

## 🚀 Quick Start

### 1. 사전 요구사항

- **Java 21** - [다운로드](https://adoptium.net/)
- **Docker & Docker Compose** - [설치 가이드](https://docs.docker.com/get-docker/)
- **Google Cloud Project** - Gemini API 사용을 위해 필요
  - [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
  - Gemini API 활성화 및 API 키 발급

### 2. 환경 설정

**Gemini API 설정** (`src/main/resources/application.yml`):

```yaml
spring.ai.google.genai.embedding:
  project-id: your-google-cloud-project-id  # 여기에 프로젝트 ID 입력
  api-key: your-gemini-api-key              # 여기에 API 키 입력
```

### 3. Elasticsearch 실행

```bash
# Elasticsearch와 Kibana 시작
docker-compose up -d

# 실행 확인
curl http://localhost:9200
curl http://localhost:5601  # Kibana UI
```

### 4. 애플리케이션 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

애플리케이션이 시작되면:
- 샘플 데이터 54개 자동 로딩
- REST API 서버: `http://localhost:8080`
- Elasticsearch 인덱스: `product` 자동 생성

### 5. 첫 검색 실행

```bash
# 냉장고 검색
curl "http://localhost:8080/api/search/products?q=냉장고&size=5"

# 삼성 제품 검색
curl "http://localhost:8080/api/search/products?q=삼성&categories=김치냉장고"

# 가격 범위로 검색
curl "http://localhost:8080/api/search/products?q=스마트&minPrice=500000&maxPrice=1500000"
```

## 📁 프로젝트 구조

```
src/main/kotlin/com/dokkabei97/springaielasticsearch/
│
├── domain/                     # 도메인 모델
│   ├── Product.kt             # 제품 데이터 클래스
│   └── ProductConvert.kt      # Product → Spring AI Document 변환
│
├── search/                     # 검색 기능
│   ├── ProductSearchService.kt        # 검색 비즈니스 로직
│   ├── ProductSearchController.kt     # REST API 컨트롤러
│   └── dto/                           # 요청/응답 DTO
│       ├── ProductSearchRequest.kt
│       ├── ProductSearchResponse.kt
│       └── enums/
│           ├── SearchMode.kt          # SEMANTIC, HYBRID
│           └── SortOption.kt          # 정렬 옵션
│
├── config/                     # 설정
│   └── VectorStoreConfig.kt   # Elasticsearch Vector Store 설정
│
└── init/                       # 초기화
    └── SampleDataInit.kt      # 샘플 데이터 로딩

src/main/resources/
├── application.yml             # 애플리케이션 설정
└── application-local.yml       # 로컬 개발 설정

docker-compose.yml              # Elasticsearch + Kibana
build.gradle.kts               # Gradle 빌드 설정
```

## 🔍 핵심 컴포넌트

### 1. Product Domain (`domain/Product.kt`)

```kotlin
data class Product(
    val productName: String,    // 제품명
    val description: String,    // 설명
    val brand: String,          // 브랜드
    val maker: String,          // 제조사
    val category: String,       // 카테고리
    val price: Int              // 가격
)
```

### 2. Vector Conversion (`domain/ProductConvert.kt`)

제품을 Spring AI Document로 변환:

```kotlin
fun Product.toDocument(): Document {
    val text = """
        제품명: $productName
        설명: $description
        브랜드: $brand
        제조사: $maker
        카테고리: $category
        가격: ${price}원
    """.trimIndent()

    val metadata = mapOf(
        "productName" to productName,
        "category" to category,
        // ...
    )

    return Document(text, metadata)
}
```

### 3. Search Service (`search/ProductSearchService.kt`)

두 가지 검색 모드 지원:

- **SEMANTIC**: 순수 벡터 유사도 검색 (threshold: 0.7)
- **HYBRID**: 벡터 검색 + 메타데이터 필터 (threshold: 0.5)

```kotlin
fun search(request: ProductSearchRequest): ProductSearchResponse {
    // 1. 검색 모드 결정 (필터 있으면 HYBRID)
    // 2. 벡터 검색 실행
    // 3. 메타데이터 필터링 적용
    // 4. 정렬 및 페이지네이션
    // 5. 응답 반환
}
```

## 🌐 API 엔드포인트

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | `/api/search/products` | 일반 검색 (쿼리 파라미터) |
| POST | `/api/search/products/advanced` | 고급 검색 (요청 본문) |
| GET | `/api/search/products/semantic` | 순수 시맨틱 검색 |
| GET | `/api/search/products/category/{category}` | 카테고리별 검색 |
| GET | `/api/search/products/brand/{brand}` | 브랜드별 검색 |
| GET | `/api/search/test/korean` | 한국어 검색 테스트 |

자세한 사용법은 [API_GUIDE.md](API_GUIDE.md)를 참조하세요.

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "ProductSearchServiceTest"

# 특정 테스트 메서드 실행
./gradlew test --tests "ProductSearchServiceTest.test Korean semantic search for refrigerators"
```

**주의**: 테스트는 실제 Elasticsearch를 사용합니다. 테스트 전 `docker-compose up -d`로 Elasticsearch를 실행하세요.

## 📊 데이터 플로우

```
1. Product 객체 생성
   ↓
2. Product.toDocument() 변환
   ↓
3. Gemini API로 768차원 벡터 생성
   ↓
4. Elasticsearch에 저장 (벡터 + 메타데이터)
   ↓
5. 검색 쿼리도 벡터로 변환
   ↓
6. Cosine Similarity로 유사 문서 검색
   ↓
7. 메타데이터 필터링 적용 (HYBRID 모드)
   ↓
8. 정렬 및 페이지네이션
   ↓
9. 결과 반환
```

## 📖 학습 경로

### 초급 (Beginner)
1. [CONCEPTS.md](CONCEPTS.md)의 "벡터 검색 기초" 읽기
2. [TUTORIAL.md](TUTORIAL.md)의 "Step 1-3" 따라하기
3. 간단한 검색 API 호출 실습

### 중급 (Intermediate)
1. [ARCHITECTURE.md](ARCHITECTURE.md)에서 시스템 구조 이해
2. [TUTORIAL.md](TUTORIAL.md)의 "Step 4-6" 실습
3. 하이브리드 검색 구현 분석
4. `docs/exercises/` 연습문제 풀어보기

### 고급 (Advanced)
1. Vector Store 설정 커스터마이징
2. 임베딩 모델 변경 실습
3. 검색 알고리즘 개선
4. 프로덕션 배포 준비

## 🔧 개발 도구

### Elasticsearch 관리

```bash
# Kibana Dev Tools에서 인덱스 확인
GET /product/_search
{
  "query": {"match_all": {}},
  "size": 10
}

# 인덱스 매핑 확인
GET /product/_mapping

# 특정 문서 조회
GET /product/_doc/{document_id}
```

### Gradle 명령어

```bash
./gradlew clean          # 빌드 파일 삭제
./gradlew build          # 프로젝트 빌드
./gradlew bootRun        # 애플리케이션 실행
./gradlew test           # 테스트 실행
./gradlew dependencies   # 의존성 트리 확인
```

## ❓ 문제 해결

### Elasticsearch 연결 실패

```bash
# Elasticsearch 상태 확인
docker-compose ps
docker-compose logs elasticsearch

# 재시작
docker-compose restart elasticsearch
```

### Gemini API 에러

- API 키가 올바른지 확인
- Google Cloud 프로젝트에서 Gemini API가 활성화되어 있는지 확인
- API 쿼터 제한에 도달하지 않았는지 확인

### 빌드 실패

```bash
# Gradle 캐시 정리
./gradlew clean --refresh-dependencies

# Gradle Wrapper 재생성
gradle wrapper
```

자세한 내용은 [FAQ.md](FAQ.md)를 참조하세요.

## 🤝 기여하기

이 프로젝트는 학습 목적으로 만들어졌습니다. 개선 사항이나 제안이 있다면:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 라이센스

이 프로젝트는 학습 및 교육 목적으로 자유롭게 사용할 수 있습니다.

## 📚 참고 자료

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Google Gemini API](https://ai.google.dev/docs)
- [Elasticsearch Vector Search](https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

## 📧 문의

프로젝트 관련 문의사항은 GitHub Issues를 통해 남겨주세요.

---

**Happy Learning! 🚀**
