# 연습 문제 1: 기본 검색 구현

## 난이도: ⭐ 초급

## 목표
검색 API를 활용하여 다양한 조건으로 제품을 검색하는 프로그램을 작성합니다.

## 문제

Python 또는 JavaScript로 다음 기능을 구현하세요:

1. **기본 검색 함수**
   - 검색어를 입력받아 제품 검색
   - 결과를 보기 좋게 출력

2. **통계 함수**
   - 검색 결과의 평균 가격 계산
   - 최고가, 최저가 제품 출력
   - 브랜드별 제품 수 집계

3. **필터 검색 함수**
   - 카테고리, 브랜드, 가격 범위를 지정하여 검색

## 요구사항

### 기본 검색 함수

```python
def search_products(query: str) -> dict:
    """
    제품을 검색하고 결과를 반환합니다.

    Args:
        query: 검색어

    Returns:
        검색 결과 딕셔너리
    """
    # TODO: 구현
    pass

# 사용 예시
results = search_products("냉장고")
print(f"총 {len(results['products'])}개 제품 발견")
```

### 통계 함수

```python
def calculate_statistics(products: list) -> dict:
    """
    제품 목록의 통계를 계산합니다.

    Args:
        products: 제품 리스트

    Returns:
        {
            'average_price': 평균 가격,
            'max_price_product': 최고가 제품,
            'min_price_product': 최저가 제품,
            'brand_counts': 브랜드별 개수
        }
    """
    # TODO: 구현
    pass

# 사용 예시
stats = calculate_statistics(results['products'])
print(f"평균 가격: {stats['average_price']:,}원")
```

### 필터 검색 함수

```python
def search_with_budget(
    query: str,
    max_price: int,
    category: str = None
) -> list:
    """
    예산 내에서 제품을 검색합니다.

    Args:
        query: 검색어
        max_price: 최대 가격
        category: 카테고리 (선택)

    Returns:
        필터링된 제품 리스트
    """
    # TODO: 구현
    pass

# 사용 예시
affordable = search_with_budget("냉장고", max_price=1500000)
print(f"{len(affordable)}개의 합리적인 제품 발견")
```

## 테스트 케이스

프로그램이 다음 케이스를 올바르게 처리하는지 확인하세요:

```python
# 테스트 1: 기본 검색
results = search_products("냉장고")
assert len(results['products']) > 0
assert results['searchMetadata']['searchType'] in ['SEMANTIC', 'HYBRID']

# 테스트 2: 통계 계산
stats = calculate_statistics(results['products'])
assert stats['average_price'] > 0
assert stats['max_price_product']['price'] >= stats['min_price_product']['price']

# 테스트 3: 예산 검색
affordable = search_with_budget("냉장고", max_price=1000000)
for product in affordable:
    assert product['price'] <= 1000000
```

## 보너스 과제

1. **시각화**: matplotlib을 사용하여 가격 분포 그래프 그리기
2. **캐싱**: 동일한 검색어를 캐싱하여 성능 개선
3. **비동기**: 여러 검색을 동시에 수행

## 예상 출력

```
=== 냉장고 검색 결과 ===
총 15개 제품 발견
검색 시간: 234ms

[삼성] 삼성 비스포크 냉장고
  가격: 2,500,000원
  관련도: 0.95

[LG] LG 디오스 냉장고
  가격: 1,800,000원
  관련도: 0.92

=== 통계 ===
평균 가격: 1,650,000원
최고가: 3,500,000원 (삼성 프리미엄 냉장고)
최저가: 800,000원 (LG 일반 냉장고)

브랜드별 제품 수:
  삼성: 8개
  LG: 7개

=== 예산 내 제품 (150만원 이하) ===
5개의 합리적인 제품 발견
- LG 디오스 냉장고: 1,800,000원
- ...
```

## 제출

완성된 코드와 실행 결과 스크린샷을 제출하세요.

## 도움말

- [API_GUIDE.md](../../API_GUIDE.md) 참조
- [예제 1: 기본 검색](../examples/01-basic-search.md) 참조

## 정답 예시

정답은 `solutions/exercise-01-solution.py`에서 확인할 수 있습니다.
