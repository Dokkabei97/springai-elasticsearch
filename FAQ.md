# FAQ (자주 묻는 질문)

> **자주 묻는 질문과 문제 해결 가이드**
>
> 프로젝트 사용 중 발생할 수 있는 문제와 해결 방법을 정리했습니다.

## 📋 목차

- [설치 및 환경 설정](#설치-및-환경-설정)
- [Elasticsearch 관련](#elasticsearch-관련)
- [Gemini API 관련](#gemini-api-관련)
- [검색 기능](#검색-기능)
- [성능 및 최적화](#성능-및-최적화)
- [개발 관련](#개발-관련)
- [프로덕션 배포](#프로덕션-배포)

---

## 설치 및 환경 설정

### Q: Java 버전 오류가 발생합니다

**증상**:
```
Unsupported class file major version 65
```

**원인**: Java 21이 설치되지 않았거나, 다른 Java 버전이 사용되고 있습니다.

**해결**:
```bash
# 현재 Java 버전 확인
java -version

# Java 21 설치 (Ubuntu/Debian)
sudo apt install openjdk-21-jdk

# macOS (Homebrew)
brew install openjdk@21

# JAVA_HOME 설정
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

---

### Q: Gradle build 실패합니다

**증상**:
```
Could not resolve dependencies
```

**해결 1**: Gradle 캐시 정리
```bash
./gradlew clean --refresh-dependencies
./gradlew build
```

**해결 2**: Gradle Wrapper 재생성
```bash
gradle wrapper
./gradlew build
```

**해결 3**: 프록시 설정 (회사 방화벽 등)
```bash
# ~/.gradle/gradle.properties
systemProp.http.proxyHost=proxy.company.com
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=proxy.company.com
systemProp.https.proxyPort=8080
```

---

### Q: 애플리케이션이 시작되지 않습니다

**증상 1**: 포트 이미 사용 중
```
Port 8080 is already in use
```

**해결**:
```bash
# 사용 중인 프로세스 찾기
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# 프로세스 종료 후 재시작
kill -9 <PID>
./gradlew bootRun
```

**증상 2**: Elasticsearch 연결 실패
```
Connection refused: localhost/127.0.0.1:9200
```

**해결**:
```bash
# Elasticsearch 실행 여부 확인
docker-compose ps

# 실행되지 않았다면 시작
docker-compose up -d elasticsearch

# 상태 확인
curl http://localhost:9200
```

---

## Elasticsearch 관련

### Q: Elasticsearch가 시작되지 않습니다

**증상 1**: 메모리 부족
```
es01 exited with code 137
```

**해결**: Docker 메모리 증가
```yaml
# docker-compose.yml
services:
  elasticsearch:
    environment:
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"  # 메모리 줄이기
```

또는 Docker Desktop 설정에서 메모리 할당 증가 (최소 4GB 권장)

**증상 2**: 포트 충돌
```
Bind for 0.0.0.0:9200 failed: port is already allocated
```

**해결**:
```bash
# 9200 포트 사용 중인 프로세스 확인
lsof -i :9200

# 포트 변경 (docker-compose.yml)
ports:
  - "9201:9200"  # 다른 포트 사용

# application.yml도 수정
spring.ai.vectorstore.elasticsearch.url: http://localhost:9201
```

---

### Q: Elasticsearch 인덱스가 생성되지 않습니다

**증상**:
```
No index found: product
```

**원인**: 자동 스키마 생성이 비활성화되었거나, 샘플 데이터 로딩 실패

**확인**:
```bash
# 인덱스 목록 확인
curl http://localhost:9200/_cat/indices?v

# product 인덱스 확인
curl http://localhost:9200/product
```

**해결**:
```yaml
# application.yml 확인
spring:
  ai:
    vectorstore:
      elasticsearch:
        initialize-schema: true  # 이 설정이 true인지 확인
```

**수동 생성**:
```bash
# Kibana Dev Tools 또는 curl 사용
PUT /product
{
  "mappings": {
    "properties": {
      "embedding": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      },
      "content": {"type": "text"},
      "metadata": {"type": "object"}
    }
  }
}
```

---

### Q: Elasticsearch 데이터를 초기화하고 싶습니다

**해결**:
```bash
# 인덱스 삭제
curl -X DELETE http://localhost:9200/product

# 애플리케이션 재시작 (자동으로 재생성 및 샘플 데이터 로딩)
./gradlew bootRun
```

또는

```bash
# Kibana Dev Tools
DELETE /product

# 또는 모든 데이터만 삭제
POST /product/_delete_by_query
{
  "query": {
    "match_all": {}
  }
}
```

---

## Gemini API 관련

### Q: Gemini API 키 오류가 발생합니다

**증상**:
```
401 Unauthorized: Invalid API key
```

**해결**:

1. **API 키 확인**:
   - [Google AI Studio](https://makersuite.google.com/app/apikey)에서 키 재확인
   - 올바른 키가 `application.yml`에 설정되었는지 확인

2. **프로젝트 ID 확인**:
   - Google Cloud Console에서 프로젝트 ID 확인
   - `application.yml`의 `project-id`가 정확한지 확인

3. **API 활성화 확인**:
   - [Google Cloud Console](https://console.cloud.google.com)
   - "API 및 서비스" → "라이브러리"
   - "Generative Language API" 검색 후 활성화

---

### Q: Gemini API 요청 제한에 걸렸습니다

**증상**:
```
429 Too Many Requests: Resource has been exhausted
```

**원인**: API 할당량 초과

**확인**:
```bash
# Google Cloud Console
# "API 및 서비스" → "할당량"
```

**해결**:

1. **무료 티어 한도**:
   - 분당 60 요청
   - 일일 1,500 요청

2. **요청 줄이기**:
```kotlin
// 배치 처리
val embeddings = embeddingClient.embed(listOf(
    text1, text2, text3  // 여러 텍스트 한 번에
))

// 캐싱
@Cacheable("embeddings")
fun getEmbedding(text: String): List<Double>
```

3. **유료 플랜 고려**:
   - [Google Cloud Pricing](https://ai.google.dev/pricing)

---

### Q: 임베딩 생성이 느립니다

**증상**: 검색 응답이 5초 이상 걸립니다

**원인**: 매번 Gemini API 호출

**해결**:

1. **쿼리 캐싱**:
```kotlin
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager() = ConcurrentMapCacheManager("embeddings")
}

@Cacheable("embeddings")
fun embed(text: String) = embeddingClient.embed(text)
```

2. **인기 검색어 사전 임베딩**:
```kotlin
@PostConstruct
fun preloadPopularQueries() {
    val popular = listOf("냉장고", "세탁기", "TV", ...)
    popular.forEach { query ->
        embeddingClient.embed(query)  // 캐시에 저장
    }
}
```

---

## 검색 기능

### Q: 검색 결과가 없습니다 (Zero Results)

**원인 1**: Threshold가 너무 높음

**해결**:
```kotlin
// ProductSearchService.kt
val threshold = 0.5  // 0.7 → 0.5로 낮춤
```

**원인 2**: 샘플 데이터 미로딩

**확인**:
```bash
# 인덱스 문서 수 확인
curl http://localhost:9200/product/_count

# 결과가 0이면 샘플 데이터 없음
```

**해결**:
```bash
# 애플리케이션 로그 확인
# "Added 54 products to vector store" 메시지 있는지 확인

# 없다면 SampleDataInit.kt 확인 및 재시작
```

---

### Q: 검색 결과가 기대와 다릅니다

**예시**: "냉장고" 검색 시 세탁기가 나옴

**원인**:
- Threshold가 너무 낮음
- 샘플 데이터의 품질 문제
- 임베딩 모델의 한계

**디버깅**:
```kotlin
// 검색 결과의 similarity score 확인
fun search(request: ProductSearchRequest): ProductSearchResponse {
    val documents = vectorStore.similaritySearch(...)

    documents.forEach { doc ->
        logger.info("Product: ${doc.metadata["productName"]}, " +
                   "Score: ${doc.similarityScore}")
    }

    // ...
}
```

**해결**:
```kotlin
// 1. Threshold 조정
val threshold = 0.75  // 더 엄격하게

// 2. 제품 설명 개선
val text = """
    제품명: $productName
    상세설명: $description
    카테고리: $category  // 카테고리 정보 강조
    ...
""".trimIndent()

// 3. 메타데이터 필터 활용
// "냉장고" 검색 시 category 필터 자동 추가
```

---

### Q: 한국어 검색이 잘 안 됩니다

**증상**: 영어는 잘 되는데 한국어는 결과가 이상함

**확인**:
```bash
# 한국어 테스트 엔드포인트
curl "http://localhost:8080/api/search/test/korean"
```

**원인 가능성**:
- 인코딩 문제
- Gemini API의 한국어 처리 문제

**해결**:

1. **URL 인코딩 확인**:
```bash
# ❌ 잘못된 예
curl "http://localhost:8080/api/search/products?q=냉장고"

# ✅ 올바른 예
curl "http://localhost:8080/api/search/products?q=%EB%83%89%EC%9E%A5%EA%B3%A0"
```

2. **Content-Type 확인**:
```bash
curl -H "Content-Type: application/json; charset=UTF-8" \
  -X POST http://localhost:8080/api/search/products/advanced \
  -d '{"query": "냉장고"}'
```

---

### Q: 시맨틱 검색과 일반 검색의 차이가 없습니다

**확인**: SearchMode 확인
```bash
curl "http://localhost:8080/api/search/products?q=냉장고" | jq '.searchMode'
# SEMANTIC or HYBRID?
```

**이해**:
- **SEMANTIC**: 필터 없을 때, threshold 0.7
- **HYBRID**: 필터 있을 때, threshold 0.5

**테스트**:
```bash
# SEMANTIC 모드 강제
curl "http://localhost:8080/api/search/products/semantic?q=냉장고"

# HYBRID 모드 (카테고리 필터)
curl "http://localhost:8080/api/search/products?q=냉장고&categories=김치냉장고"
```

---

## 성능 및 최적화

### Q: 검색이 느립니다

**증상**: 응답 시간이 2초 이상

**진단**:
```kotlin
// 로그에서 시간 확인
@Service
class ProductSearchService {
    fun search(request: ProductSearchRequest): ProductSearchResponse {
        val start = System.currentTimeMillis()

        // ... 검색 로직 ...

        val elapsed = System.currentTimeMillis() - start
        logger.info("Search took ${elapsed}ms")

        return response
    }
}
```

**병목 지점**:
1. Gemini API 호출: ~200-500ms
2. Elasticsearch 쿼리: ~50-200ms
3. 필터링/정렬: ~10-50ms

**해결**:

1. **캐싱**:
```kotlin
// 검색 결과 캐싱
@Cacheable(value = ["searchResults"], key = "#request")
fun search(request: ProductSearchRequest): ProductSearchResponse
```

2. **인덱스 최적화**:
```bash
# Force merge (읽기 전용 인덱스)
POST /product/_forcemerge?max_num_segments=1
```

3. **쿼리 최적화**:
```kotlin
// topK 줄이기
SearchRequest.query(query)
    .withTopK(50)  // 100 → 50
```

---

### Q: 메모리 사용량이 높습니다

**증상**: 애플리케이션이 OutOfMemoryError 발생

**확인**:
```bash
# JVM 힙 메모리 확인
jps  # Java 프로세스 찾기
jmap -heap <pid>
```

**해결**:

1. **JVM 옵션 조정**:
```bash
# application 실행 시
./gradlew bootRun -Dspring-boot.run.jvmArguments="-Xmx2g -Xms1g"
```

2. **캐시 크기 제한**:
```kotlin
@Bean
fun cacheManager(): CacheManager {
    return CaffeineCacheManager().apply {
        setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)  // 최대 1000개
            .expireAfterWrite(1, TimeUnit.HOURS))
    }
}
```

3. **배치 크기 조정**:
```kotlin
// 한 번에 너무 많은 문서 처리하지 않기
products.chunked(100).forEach { batch ->
    vectorStore.add(batch.map { it.toDocument() })
}
```

---

## 개발 관련

### Q: 코드 변경이 반영되지 않습니다

**해결**:
```bash
# Clean build
./gradlew clean build

# 캐시 삭제
rm -rf .gradle/ build/

# 재빌드
./gradlew build

# 실행
./gradlew bootRun
```

---

### Q: 테스트가 실패합니다

**증상**:
```
Connection refused: elasticsearch
```

**원인**: Elasticsearch가 실행되지 않음

**해결**:
```bash
# Elasticsearch 실행
docker-compose up -d elasticsearch

# 테스트 실행
./gradlew test
```

**특정 테스트만 실행**:
```bash
./gradlew test --tests "ProductSearchServiceTest"
```

---

### Q: 새로운 제품을 추가하고 싶습니다

**방법 1**: SampleDataInit.kt 수정
```kotlin
@PostConstruct
fun initSampleData() {
    val products = mutableListOf<Product>()

    // 기존 제품들...

    // 새 제품 추가
    products.add(Product.of(
        productName = "내 제품",
        description = "설명",
        brand = "브랜드",
        maker = "제조사",
        category = "카테고리",
        price = 1000000
    ))

    vectorStore.add(products.map { it.toDocument() })
}
```

**방법 2**: API 추가 (프로덕션)
```kotlin
@RestController
@RequestMapping("/api/admin")
class ProductAdminController(
    private val vectorStore: VectorStore
) {
    @PostMapping("/products")
    fun addProduct(@RequestBody product: Product): ResponseEntity<*> {
        val document = product.toDocument()
        vectorStore.add(listOf(document))
        return ResponseEntity.ok("Product added")
    }
}
```

---

### Q: 다른 임베딩 모델을 사용하고 싶습니다

**OpenAI로 변경**:

1. **의존성 변경** (`build.gradle.kts`):
```kotlin
// 제거
implementation("org.springframework.ai:spring-ai-starter-model-google-genai-embedding:1.1.0-M1")

// 추가
implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
```

2. **설정 변경** (`application.yml`):
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        model: text-embedding-3-small  # 또는 text-embedding-ada-002
        dimensions: 1536  # OpenAI는 1536차원
```

3. **Elasticsearch 인덱스 재생성**:
```bash
# 기존 인덱스 삭제
DELETE /product

# dimensions 변경 필요
```

---

## 프로덕션 배포

### Q: Docker로 배포하고 싶습니다

**Dockerfile**:
```dockerfile
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml** (전체 스택):
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - GOOGLE_PROJECT_ID=${GOOGLE_PROJECT_ID}
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    depends_on:
      - elasticsearch

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:9.1.2
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - esdata:/usr/share/elasticsearch/data

volumes:
  esdata:
```

**실행**:
```bash
docker-compose up -d
```

---

### Q: 환경 변수로 설정을 관리하고 싶습니다

**application.yml**:
```yaml
spring:
  ai:
    google:
      genai:
        embedding:
          project-id: ${GOOGLE_PROJECT_ID:default-project}
          api-key: ${GOOGLE_API_KEY}
    vectorstore:
      elasticsearch:
        url: ${ELASTICSEARCH_URL:http://localhost:9200}
        index-name: ${ELASTICSEARCH_INDEX:product}

server:
  port: ${SERVER_PORT:8080}
```

**.env 파일** (docker-compose):
```bash
GOOGLE_PROJECT_ID=my-project
GOOGLE_API_KEY=AIza...
ELASTICSEARCH_URL=http://elasticsearch:9200
SERVER_PORT=8080
```

**실행**:
```bash
docker-compose --env-file .env up -d
```

---

### Q: HTTPS를 설정하고 싶습니다

**Spring Boot SSL 설정**:

1. **인증서 생성** (개발용):
```bash
keytool -genkeypair -alias springboot \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 \
  -validity 3650
```

2. **application.yml**:
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: springboot
```

3. **프로덕션**: Nginx/Apache 리버스 프록시 사용 권장

---

### Q: 로그를 파일로 저장하고 싶습니다

**application.yml**:
```yaml
logging:
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
  level:
    root: INFO
    com.dokkabei97.springaielasticsearch: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**로그 파일 위치**:
```
logs/
  application.log
  application.log.2025-01-01.0.gz
  application.log.2025-01-02.0.gz
```

---

## 추가 도움말

### 유용한 명령어

```bash
# Elasticsearch 상태 확인
curl http://localhost:9200/_cluster/health?pretty

# 인덱스 통계
curl http://localhost:9200/product/_stats?pretty

# 매핑 확인
curl http://localhost:9200/product/_mapping?pretty

# 샘플 문서 조회
curl http://localhost:9200/product/_search?pretty&size=1

# 로그 실시간 확인
docker-compose logs -f app

# 컨테이너 재시작
docker-compose restart app
```

### 문제 해결 체크리스트

- [ ] Java 21 설치 확인
- [ ] Docker & Docker Compose 실행 확인
- [ ] Elasticsearch 실행 확인 (http://localhost:9200)
- [ ] Gemini API 키 설정 확인
- [ ] 샘플 데이터 로딩 확인 (로그)
- [ ] 네트워크/방화벽 설정 확인

### 더 많은 도움이 필요하신가요?

1. **문서 참조**:
   - [TUTORIAL.md](TUTORIAL.md) - 단계별 가이드
   - [ARCHITECTURE.md](ARCHITECTURE.md) - 시스템 구조
   - [CONCEPTS.md](CONCEPTS.md) - 개념 설명

2. **GitHub Issues**: 새로운 문제 보고

3. **커뮤니티**:
   - [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
   - [Elasticsearch 포럼](https://discuss.elastic.co/)
   - [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-ai)

---

**이 FAQ에 없는 질문이 있다면 GitHub Issues로 제보해주세요!**
