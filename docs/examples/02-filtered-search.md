# 예제 2: 필터링 검색

## 학습 목표
- 카테고리, 브랜드 필터 사용법
- 가격 범위 필터링
- 하이브리드 검색 모드 이해

## 카테고리 필터

```bash
# 김치냉장고만 검색
curl "http://localhost:8080/api/search/products?q=냉장고&categories=김치냉장고"
```

**특징**:
- `searchType`이 `HYBRID`로 변경됨
- 지정된 카테고리 제품만 반환

## 브랜드 필터

```bash
# 삼성 제품만 검색
curl "http://localhost:8080/api/search/products?q=냉장고&brands=삼성"
```

## 다중 필터 조합

```bash
# 삼성 또는 LG 브랜드, 냉장고 카테고리
curl "http://localhost:8080/api/search/products?q=프리미엄&categories=냉장고&brands=삼성,LG"
```

## 가격 필터

```bash
# 100만원 이하
curl "http://localhost:8080/api/search/products?q=냉장고&maxPrice=1000000"

# 100만원 ~ 200만원
curl "http://localhost:8080/api/search/products?q=냉장고&minPrice=1000000&maxPrice=2000000"

# 200만원 이상
curl "http://localhost:8080/api/search/products?q=프리미엄&minPrice=2000000"
```

## Python 고급 예제

```python
import requests

def search_with_filters(
    query,
    categories=None,
    brands=None,
    min_price=None,
    max_price=None
):
    """필터를 적용한 검색"""
    url = "http://localhost:8080/api/search/products"
    params = {"q": query}

    if categories:
        params["categories"] = ",".join(categories)
    if brands:
        params["brands"] = ",".join(brands)
    if min_price:
        params["minPrice"] = min_price
    if max_price:
        params["maxPrice"] = max_price

    response = requests.get(url, params=params)
    return response.json()

# 사용 예시 1: 100만원 이하의 LG 김치냉장고
results = search_with_filters(
    query="김치냉장고",
    categories=["김치냉장고"],
    brands=["LG"],
    max_price=1000000
)

print(f"검색 모드: {results['searchMetadata']['searchType']}")
print(f"결과 수: {results['searchMetadata']['totalResults']}\n")

# 사용 예시 2: 200만원 이상의 프리미엄 냉장고
premium_results = search_with_filters(
    query="프리미엄",
    categories=["냉장고"],
    min_price=2000000
)
```

## 실습 과제

1. 50만원 ~ 150만원 사이의 냉장고 검색
2. 삼성과 LG 브랜드의 김치냉장고 검색
3. 각 브랜드별 평균 가격 계산
4. 가장 비싼 제품과 가장 저렴한 제품 찾기

## 다음 단계

[예제 3: 정렬과 페이지네이션](03-sorting-pagination.md)
