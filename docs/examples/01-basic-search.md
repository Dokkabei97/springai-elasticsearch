# 예제 1: 기본 검색

## 학습 목표
- 간단한 검색 API 호출 방법 학습
- 검색 응답 구조 이해
- 페이지네이션 활용

## 전제 조건
- 애플리케이션 실행 중 (`./gradlew bootRun`)
- Elasticsearch 실행 중
- 샘플 데이터 로딩 완료

## 예제 코드

### 1. curl을 사용한 기본 검색

```bash
# 냉장고 검색
curl "http://localhost:8080/api/search/products?q=냉장고"
```

**예상 응답**:
```json
{
  "products": [
    {
      "productName": "삼성 비스포크 냉장고",
      "description": "4도어 대용량 냉장고",
      "brand": "삼성",
      "maker": "삼성전자",
      "category": "냉장고",
      "price": 2500000,
      "relevanceScore": 0.95
    },
    ...
  ],
  "searchMetadata": {
    "totalResults": 15,
    "searchTime": 245,
    "searchType": "SEMANTIC"
  }
}
```

### 2. 페이지 크기 지정

```bash
# 5개만 가져오기
curl "http://localhost:8080/api/search/products?q=냉장고&size=5"
```

### 3. 페이지네이션

```bash
# 1페이지 (첫 10개)
curl "http://localhost:8080/api/search/products?q=냉장고&page=0&size=10"

# 2페이지 (다음 10개)
curl "http://localhost:8080/api/search/products?q=냉장고&page=1&size=10"
```

## Python 예제

```python
import requests

def search_products(query, page=0, size=10):
    """제품 검색 함수"""
    url = "http://localhost:8080/api/search/products"
    params = {
        "q": query,
        "page": page,
        "size": size
    }

    response = requests.get(url, params=params)
    return response.json()

# 사용 예시
results = search_products("냉장고", size=5)

print(f"총 {results['searchMetadata']['totalResults']}개 제품 발견")
print(f"검색 시간: {results['searchMetadata']['searchTime']}ms\n")

for product in results['products']:
    print(f"[{product['brand']}] {product['productName']}")
    print(f"  가격: {product['price']:,}원")
    print(f"  관련도: {product['relevanceScore']:.2f}\n")
```

## JavaScript 예제

```javascript
async function searchProducts(query, page = 0, size = 10) {
  const params = new URLSearchParams({ q: query, page, size });
  const response = await fetch(
    `http://localhost:8080/api/search/products?${params}`
  );
  return await response.json();
}

// 사용 예시
searchProducts("냉장고", 0, 5)
  .then(data => {
    console.log(`총 ${data.searchMetadata.totalResults}개 제품 발견`);
    console.log(`검색 시간: ${data.searchMetadata.searchTime}ms\n`);

    data.products.forEach(product => {
      console.log(`[${product.brand}] ${product.productName}`);
      console.log(`  가격: ${product.price.toLocaleString()}원`);
      console.log(`  관련도: ${product.relevanceScore.toFixed(2)}\n`);
    });
  });
```

## 실습 과제

1. "삼성" 검색 후 결과 수 확인
2. 페이지 크기를 20으로 변경하여 검색
3. 여러 페이지를 순회하며 모든 결과 가져오기
4. 검색 시간이 가장 짧은 쿼리 찾기

## 다음 단계

[예제 2: 필터링 검색](02-filtered-search.md)
