# API 가이드

> **Spring AI + Elasticsearch Vector Search REST API 완벽 가이드**
>
> 모든 API 엔드포인트의 사용법, 파라미터, 응답 형식, 예제를 상세히 설명합니다.

## 📋 목차

- [API 개요](#api-개요)
- [공통 사항](#공통-사항)
- [검색 API](#검색-api)
  - [일반 검색](#1-일반-검색)
  - [고급 검색](#2-고급-검색)
  - [시맨틱 검색](#3-시맨틱-검색)
  - [카테고리별 검색](#4-카테고리별-검색)
  - [브랜드별 검색](#5-브랜드별-검색)
  - [검색 제안](#6-검색-제안)
  - [한국어 테스트](#7-한국어-테스트)
- [응답 형식](#응답-형식)
- [에러 처리](#에러-처리)
- [사용 예제](#사용-예제)
- [성능 최적화 팁](#성능-최적화-팁)

---

## API 개요

### Base URL

```
로컬 개발: http://localhost:8080
프로덕션: https://your-domain.com
```

### API 경로

```
/api/search/products                      # 메인 검색 API
/api/search/products/advanced             # 고급 검색
/api/search/products/semantic             # 시맨틱 검색
/api/search/products/category/{category}  # 카테고리 검색
/api/search/products/brand/{brand}        # 브랜드 검색
/api/search/products/suggestions          # 검색 제안
/api/search/test/korean                   # 한국어 테스트
```

---

## 공통 사항

### Content-Type

```
요청 (POST): application/json
응답: application/json; charset=UTF-8
```

### 인증

**현재**: 인증 불필요 (학습용 프로젝트)

**프로덕션 적용 시**:
```bash
# API Key 방식
curl -H "X-API-Key: your-api-key" \
  http://localhost:8080/api/search/products?q=냉장고

# JWT 방식
curl -H "Authorization: Bearer your-jwt-token" \
  http://localhost:8080/api/search/products?q=냉장고
```

### Rate Limiting

**현재**: 제한 없음

**권장 설정** (프로덕션):
- 인증 사용자: 1000 요청/분
- 비인증: 60 요청/분

### 페이지네이션

모든 검색 API는 페이지네이션을 지원합니다:

```
page: 페이지 번호 (0부터 시작)
size: 페이지 크기 (기본 10, 최대 100)
```

---

## 검색 API

### 1. 일반 검색

**가장 많이 사용되는 API**. 쿼리 파라미터로 간단하게 검색.

#### 엔드포인트

```
GET /api/search/products
```

#### 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `q` | String | ✅ | - | 검색어 |
| `categories` | String[] | ❌ | [] | 카테고리 필터 (콤마 구분) |
| `brands` | String[] | ❌ | [] | 브랜드 필터 (콤마 구분) |
| `makers` | String[] | ❌ | [] | 제조사 필터 (콤마 구분) |
| `minPrice` | Integer | ❌ | null | 최소 가격 |
| `maxPrice` | Integer | ❌ | null | 최대 가격 |
| `sortBy` | Enum | ❌ | RELEVANCE | 정렬 옵션 |
| `page` | Integer | ❌ | 0 | 페이지 번호 (0부터) |
| `size` | Integer | ❌ | 10 | 페이지 크기 (1~100) |

#### 정렬 옵션 (sortBy)

- `RELEVANCE`: 관련도순 (기본)
- `PRICE_ASC`: 가격 낮은순
- `PRICE_DESC`: 가격 높은순
- `NAME_ASC`: 이름 오름차순
- `NAME_DESC`: 이름 내림차순

#### 예제

**기본 검색**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고"
```

**카테고리 필터**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&categories=김치냉장고"
```

**다중 카테고리**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&categories=냉장고,김치냉장고"
```

**브랜드 + 가격 필터**:
```bash
curl "http://localhost:8080/api/search/products?q=스마트&brands=삼성&minPrice=500000&maxPrice=2000000"
```

**정렬 + 페이지네이션**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&sortBy=PRICE_ASC&page=0&size=20"
```

**복합 필터**:
```bash
curl "http://localhost:8080/api/search/products?\
q=프리미엄&\
categories=냉장고&\
brands=삼성,LG&\
minPrice=1000000&\
maxPrice=3000000&\
sortBy=PRICE_DESC&\
page=0&\
size=10"
```

#### 응답

```json
{
  "products": [
    {
      "productName": "삼성 비스포크 냉장고",
      "description": "4도어 대용량 프리미엄 냉장고",
      "brand": "삼성",
      "maker": "삼성전자",
      "category": "냉장고",
      "price": 2500000,
      "relevanceScore": 0.95
    }
  ],
  "totalResults": 15,
  "page": 0,
  "size": 10,
  "hasMore": true,
  "searchMode": "HYBRID",
  "query": "프리미엄",
  "appliedFilters": {
    "categories": ["냉장고"],
    "brands": ["삼성", "LG"],
    "priceRange": {
      "min": 1000000,
      "max": 3000000
    }
  }
}
```

---

### 2. 고급 검색

**POST 요청으로 복잡한 검색 수행**. 요청 본문에 JSON 형식으로 전달.

#### 엔드포인트

```
POST /api/search/products/advanced
```

#### 요청 본문

```json
{
  "query": "스마트 냉장고",
  "categories": ["냉장고", "김치냉장고"],
  "brands": ["삼성", "LG"],
  "makers": ["삼성전자", "LG전자"],
  "minPrice": 500000,
  "maxPrice": 3000000,
  "sortBy": "PRICE_DESC",
  "page": 0,
  "size": 20
}
```

#### 예제

```bash
curl -X POST http://localhost:8080/api/search/products/advanced \
  -H "Content-Type: application/json" \
  -d '{
    "query": "에너지 효율 좋은 냉장고",
    "categories": ["냉장고"],
    "brands": ["삼성", "LG"],
    "minPrice": 1000000,
    "maxPrice": 2500000,
    "sortBy": "RELEVANCE",
    "page": 0,
    "size": 10
  }'
```

**Python 예제**:
```python
import requests

url = "http://localhost:8080/api/search/products/advanced"
payload = {
    "query": "에너지 효율 좋은 냉장고",
    "categories": ["냉장고"],
    "brands": ["삼성", "LG"],
    "minPrice": 1000000,
    "maxPrice": 2500000,
    "sortBy": "RELEVANCE",
    "page": 0,
    "size": 10
}

response = requests.post(url, json=payload)
data = response.json()

for product in data["products"]:
    print(f"{product['productName']}: {product['price']:,}원")
```

**JavaScript 예제**:
```javascript
const response = await fetch('http://localhost:8080/api/search/products/advanced', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    query: "에너지 효율 좋은 냉장고",
    categories: ["냉장고"],
    brands: ["삼성", "LG"],
    minPrice: 1000000,
    maxPrice: 2500000,
    sortBy: "RELEVANCE",
    page: 0,
    size: 10
  })
});

const data = await response.json();
console.log(data.products);
```

---

### 3. 시맨틱 검색

**순수 벡터 유사도 검색**. 메타데이터 필터 없이 의미만으로 검색.

#### 엔드포인트

```
GET /api/search/products/semantic
```

#### 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `q` | String | ✅ | - | 검색어 |
| `size` | Integer | ❌ | 10 | 결과 수 (1~100) |

#### 특징

- **SearchMode**: 항상 `SEMANTIC`
- **Threshold**: 0.7 (높은 유사도 요구)
- **필터**: 적용되지 않음
- **정렬**: 관련도순만 지원

#### 예제

**자연어 질문**:
```bash
curl "http://localhost:8080/api/search/products/semantic?q=음식을%20신선하게%20보관하는%20제품"
```

**추상적 개념**:
```bash
curl "http://localhost:8080/api/search/products/semantic?q=건강한%20식단을%20위한%20제품"
```

**동의어 테스트**:
```bash
# "프리미엄" 검색
curl "http://localhost:8080/api/search/products/semantic?q=프리미엄%20냉장고"

# "고급" 검색 (유사한 결과 기대)
curl "http://localhost:8080/api/search/products/semantic?q=고급%20냉장고"

# "비싼" 검색 (유사한 결과 기대)
curl "http://localhost:8080/api/search/products/semantic?q=비싼%20냉장고"
```

#### 응답

```json
{
  "products": [
    {
      "productName": "삼성 비스포크 냉장고",
      "description": "4도어 대용량 냉장고",
      "brand": "삼성",
      "category": "냉장고",
      "price": 2500000,
      "relevanceScore": 0.92
    }
  ],
  "totalResults": 5,
  "page": 0,
  "size": 10,
  "hasMore": false,
  "searchMode": "SEMANTIC",
  "query": "음식을 신선하게 보관하는 제품"
}
```

---

### 4. 카테고리별 검색

**특정 카테고리 내에서 검색**

#### 엔드포인트

```
GET /api/search/products/category/{category}
```

#### Path 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `category` | String | ✅ | 카테고리명 |

#### Query 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `q` | String | ✅ | - | 검색어 |
| `size` | Integer | ❌ | 10 | 결과 수 |

#### 예제

**냉장고 카테고리에서 삼성 제품 검색**:
```bash
curl "http://localhost:8080/api/search/products/category/냉장고?q=삼성&size=5"
```

**김치냉장고 카테고리에서 프리미엄 제품 검색**:
```bash
curl "http://localhost:8080/api/search/products/category/김치냉장고?q=프리미엄&size=10"
```

**URL 인코딩 주의**:
```bash
# 한글 카테고리는 URL 인코딩 필요
# "냉장고" → "%EB%83%89%EC%9E%A5%EA%B3%A0"

curl "http://localhost:8080/api/search/products/category/%EB%83%89%EC%9E%A5%EA%B3%A0?q=삼성"
```

#### 응답

```json
{
  "products": [...],
  "totalResults": 8,
  "searchMode": "HYBRID",
  "query": "삼성",
  "appliedFilters": {
    "categories": ["냉장고"]
  }
}
```

---

### 5. 브랜드별 검색

**특정 브랜드 제품만 검색**

#### 엔드포인트

```
GET /api/search/products/brand/{brand}
```

#### Path 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `brand` | String | ✅ | 브랜드명 |

#### Query 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `q` | String | ✅ | - | 검색어 |
| `size` | Integer | ❌ | 10 | 결과 수 |

#### 예제

**삼성 브랜드에서 스마트 제품 검색**:
```bash
curl "http://localhost:8080/api/search/products/brand/삼성?q=스마트&size=10"
```

**LG 브랜드에서 김치냉장고 검색**:
```bash
curl "http://localhost:8080/api/search/products/brand/LG?q=김치냉장고"
```

#### 응답

```json
{
  "products": [...],
  "totalResults": 6,
  "searchMode": "HYBRID",
  "query": "스마트",
  "appliedFilters": {
    "brands": ["삼성"]
  }
}
```

---

### 6. 검색 제안

**검색어 자동완성/제안 기능**

#### 엔드포인트

```
GET /api/search/products/suggestions
```

#### 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `q` | String | ✅ | - | 부분 검색어 (최소 2자) |
| `size` | Integer | ❌ | 5 | 제안 수 (1~20) |

#### 예제

**"삼" 입력 시**:
```bash
curl "http://localhost:8080/api/search/products/suggestions?q=삼&size=5"
```

**응답**:
```json
{
  "query": "삼",
  "suggestions": [
    "삼성 비스포크 냉장고",
    "삼성 김치냉장고 프리미엄",
    "삼성 디지털 인버터",
    "삼성 스마트 냉장고",
    "삼성 프렌치도어"
  ]
}
```

**"냉장" 입력 시**:
```bash
curl "http://localhost:8080/api/search/products/suggestions?q=냉장"
```

**응답**:
```json
{
  "query": "냉장",
  "suggestions": [
    "냉장고",
    "김치냉장고",
    "프리미엄 냉장고",
    "대용량 냉장고",
    "스마트 냉장고"
  ]
}
```

#### 사용 시나리오

**검색창 자동완성**:
```javascript
// 사용자가 입력할 때마다 호출
const input = document.getElementById('search-input');

input.addEventListener('input', async (e) => {
  const query = e.target.value;

  if (query.length >= 2) {
    const response = await fetch(
      `http://localhost:8080/api/search/products/suggestions?q=${query}&size=5`
    );
    const data = await response.json();

    // 드롭다운에 제안 표시
    showSuggestions(data.suggestions);
  }
});
```

---

### 7. 한국어 테스트

**한국어 검색 기능 테스트 엔드포인트**

#### 엔드포인트

```
GET /api/search/test/korean
```

#### 파라미터

없음

#### 응답

```json
{
  "message": "한국어 검색 테스트 성공",
  "sampleSearches": [
    {
      "query": "냉장고",
      "resultCount": 15
    },
    {
      "query": "김치냉장고",
      "resultCount": 8
    },
    {
      "query": "프리미엄",
      "resultCount": 12
    }
  ],
  "status": "OK"
}
```

#### 예제

```bash
curl "http://localhost:8080/api/search/test/korean"
```

---

## 응답 형식

### 성공 응답 (200 OK)

```json
{
  "products": [
    {
      "productName": "제품명",
      "description": "설명",
      "brand": "브랜드",
      "maker": "제조사",
      "category": "카테고리",
      "price": 1000000,
      "relevanceScore": 0.95
    }
  ],
  "totalResults": 25,
  "page": 0,
  "size": 10,
  "hasMore": true,
  "searchMode": "HYBRID",
  "query": "검색어",
  "appliedFilters": {
    "categories": ["냉장고"],
    "brands": ["삼성"],
    "priceRange": {
      "min": 500000,
      "max": 2000000
    }
  },
  "message": "검색 완료"
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `products` | Array | 검색 결과 제품 목록 |
| `totalResults` | Integer | 전체 결과 수 |
| `page` | Integer | 현재 페이지 번호 |
| `size` | Integer | 페이지 크기 |
| `hasMore` | Boolean | 다음 페이지 존재 여부 |
| `searchMode` | String | SEMANTIC 또는 HYBRID |
| `query` | String | 검색어 |
| `appliedFilters` | Object | 적용된 필터 정보 |
| `message` | String | 메시지 (선택적) |

### Product 객체

| 필드 | 타입 | 설명 |
|------|------|------|
| `productName` | String | 제품명 |
| `description` | String | 제품 설명 |
| `brand` | String | 브랜드 |
| `maker` | String | 제조사 |
| `category` | String | 카테고리 |
| `price` | Integer | 가격 (원) |
| `relevanceScore` | Double | 관련도 점수 (0.0 ~ 1.0) |

---

## 에러 처리

### 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "timestamp": 1640000000000,
  "path": "/api/search/products"
}
```

### HTTP 상태 코드

| 코드 | 설명 | 예시 |
|------|------|------|
| **200** | 성공 | 검색 완료 |
| **400** | 잘못된 요청 | 필수 파라미터 누락, 잘못된 형식 |
| **404** | 찾을 수 없음 | 존재하지 않는 엔드포인트 |
| **500** | 서버 오류 | 내부 처리 오류 |
| **503** | 서비스 불가 | Elasticsearch 연결 실패 |

### 에러 예시

**400 Bad Request - 검색어 누락**:
```bash
curl "http://localhost:8080/api/search/products"
```

응답:
```json
{
  "code": "INVALID_REQUEST",
  "message": "검색어(q)는 필수입니다",
  "timestamp": 1640000000000,
  "path": "/api/search/products"
}
```

**400 Bad Request - 잘못된 파라미터**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&size=200"
```

응답:
```json
{
  "code": "INVALID_PARAMETER",
  "message": "size는 1~100 사이여야 합니다",
  "timestamp": 1640000000000
}
```

**500 Internal Server Error - Elasticsearch 오류**:
```json
{
  "code": "SEARCH_ERROR",
  "message": "검색 처리 중 오류가 발생했습니다",
  "timestamp": 1640000000000
}
```

---

## 사용 예제

### 사용 사례 1: 가격대별 제품 검색

**시나리오**: "100만원 ~ 200만원 사이의 삼성 냉장고"

```bash
curl "http://localhost:8080/api/search/products?\
q=냉장고&\
brands=삼성&\
minPrice=1000000&\
maxPrice=2000000&\
sortBy=PRICE_ASC"
```

### 사용 사례 2: 페이지네이션

**시나리오**: "냉장고 검색 결과를 20개씩 페이지별로 가져오기"

**1페이지**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&page=0&size=20"
```

**2페이지**:
```bash
curl "http://localhost:8080/api/search/products?q=냉장고&page=1&size=20"
```

**Python 예제 - 모든 결과 가져오기**:
```python
import requests

def get_all_results(query):
    all_products = []
    page = 0
    size = 20

    while True:
        response = requests.get(
            f"http://localhost:8080/api/search/products",
            params={"q": query, "page": page, "size": size}
        )
        data = response.json()

        all_products.extend(data["products"])

        if not data["hasMore"]:
            break

        page += 1

    return all_products

products = get_all_results("냉장고")
print(f"총 {len(products)}개 제품 검색")
```

### 사용 사례 3: 다중 필터 조합

**시나리오**: "500만원 이하의 삼성/LG 김치냉장고, 가격 높은순"

```bash
curl "http://localhost:8080/api/search/products?\
q=프리미엄&\
categories=김치냉장고&\
brands=삼성,LG&\
maxPrice=5000000&\
sortBy=PRICE_DESC&\
size=10"
```

### 사용 사례 4: 검색 결과 비교

**키워드 vs 시맨틱 검색 비교**:

```bash
# 일반 검색 (HYBRID 모드)
curl "http://localhost:8080/api/search/products?q=에너지%20절약" \
  | jq '.searchMode, .totalResults'

# 시맨틱 검색
curl "http://localhost:8080/api/search/products/semantic?q=에너지%20절약" \
  | jq '.searchMode, .totalResults'
```

### 사용 사례 5: 검색 + 필터링 UI 구현

**React 예제**:
```javascript
import React, { useState, useEffect } from 'react';

function ProductSearch() {
  const [query, setQuery] = useState('');
  const [filters, setFilters] = useState({
    categories: [],
    brands: [],
    minPrice: null,
    maxPrice: null
  });
  const [results, setResults] = useState([]);

  const search = async () => {
    const params = new URLSearchParams({
      q: query,
      ...filters,
      page: 0,
      size: 20
    });

    const response = await fetch(
      `http://localhost:8080/api/search/products?${params}`
    );
    const data = await response.json();
    setResults(data.products);
  };

  return (
    <div>
      <input
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="검색어 입력"
      />
      <button onClick={search}>검색</button>

      {/* 필터 UI */}
      <div>
        <label>
          카테고리:
          <select onChange={(e) => setFilters({
            ...filters,
            categories: [e.target.value]
          })}>
            <option value="">전체</option>
            <option value="냉장고">냉장고</option>
            <option value="김치냉장고">김치냉장고</option>
          </select>
        </label>
      </div>

      {/* 결과 표시 */}
      <div>
        {results.map(product => (
          <div key={product.productName}>
            <h3>{product.productName}</h3>
            <p>{product.description}</p>
            <p>{product.price.toLocaleString()}원</p>
            <p>관련도: {(product.relevanceScore * 100).toFixed(0)}%</p>
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## 성능 최적화 팁

### 1. 적절한 페이지 크기 설정

```bash
# ❌ 나쁜 예: 너무 큰 페이지 크기
curl "http://localhost:8080/api/search/products?q=냉장고&size=100"

# ✅ 좋은 예: 적절한 크기
curl "http://localhost:8080/api/search/products?q=냉장고&size=20"
```

**권장**:
- 모바일: 10~15개
- 데스크톱: 20~30개
- API: 최대 50개

### 2. 필터 우선 적용

```bash
# ❌ 비효율적: 광범위한 검색 후 필터
curl "http://localhost:8080/api/search/products?q=제품"

# ✅ 효율적: 필터와 함께 검색
curl "http://localhost:8080/api/search/products?q=제품&categories=냉장고&brands=삼성"
```

### 3. 캐싱 활용

**클라이언트 캐싱**:
```javascript
const cache = new Map();

async function search(query) {
  if (cache.has(query)) {
    return cache.get(query);
  }

  const response = await fetch(
    `http://localhost:8080/api/search/products?q=${query}`
  );
  const data = await response.json();

  cache.set(query, data);
  return data;
}
```

### 4. 디바운싱 (검색창)

```javascript
// 사용자 입력 후 300ms 대기
const debounce = (func, delay) => {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
};

const debouncedSearch = debounce(search, 300);

input.addEventListener('input', (e) => {
  debouncedSearch(e.target.value);
});
```

---

## Postman Collection

### Import 방법

1. Postman 실행
2. "Import" 클릭
3. 아래 JSON 복사/붙여넣기

### Collection JSON

```json
{
  "info": {
    "name": "Spring AI Elasticsearch Search API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "일반 검색",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/search/products?q=냉장고&size=10",
          "host": ["http://localhost:8080"],
          "path": ["api", "search", "products"],
          "query": [
            {"key": "q", "value": "냉장고"},
            {"key": "size", "value": "10"}
          ]
        }
      }
    },
    {
      "name": "고급 검색",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"query\": \"프리미엄 냉장고\",\n  \"categories\": [\"냉장고\"],\n  \"brands\": [\"삼성\"],\n  \"minPrice\": 1000000,\n  \"maxPrice\": 3000000\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/search/products/advanced",
          "host": ["http://localhost:8080"],
          "path": ["api", "search", "products", "advanced"]
        }
      }
    }
  ]
}
```

---

## 추가 자료

- [TUTORIAL.md](TUTORIAL.md) - 단계별 튜토리얼
- [CONCEPTS.md](CONCEPTS.md) - 벡터 검색 개념
- [ARCHITECTURE.md](ARCHITECTURE.md) - 시스템 아키텍처
- [FAQ.md](FAQ.md) - 자주 묻는 질문

---

**API에 대한 질문이나 제안사항이 있다면 GitHub Issues로 남겨주세요!**
