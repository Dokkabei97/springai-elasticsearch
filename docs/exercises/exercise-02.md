# 연습 문제 2: 하이브리드 검색 분석

## 난이도: ⭐⭐ 중급

## 목표
SEMANTIC 검색과 HYBRID 검색의 차이를 이해하고, 성능을 비교 분석합니다.

## 문제

동일한 검색어로 SEMANTIC과 HYBRID 검색을 수행하고, 결과를 비교하는 프로그램을 작성하세요.

## 요구사항

### 1. 검색 모드 비교 함수

```python
def compare_search_modes(query: str, category: str = None):
    """
    SEMANTIC과 HYBRID 검색 결과를 비교합니다.

    Args:
        query: 검색어
        category: 카테고리 (HYBRID 검색용)

    Returns:
        비교 결과 딕셔너리
    """
    # TODO: 구현
    # 1. SEMANTIC 검색 수행 (/api/search/products/semantic)
    # 2. HYBRID 검색 수행 (/api/search/products?categories=...)
    # 3. 결과 비교
    pass
```

### 2. 성능 비교 함수

```python
def compare_performance(query: str, iterations: int = 10):
    """
    여러 번 검색을 수행하여 평균 응답 시간을 비교합니다.

    Args:
        query: 검색어
        iterations: 반복 횟수

    Returns:
        {
            'semantic_avg': SEMANTIC 평균 시간,
            'hybrid_avg': HYBRID 평균 시간,
            'difference': 시간 차이
        }
    """
    # TODO: 구현
    pass
```

### 3. 결과 품질 비교

```python
def compare_quality(query: str, category: str):
    """
    검색 결과의 품질을 비교합니다.

    품질 지표:
    - 평균 관련도 점수
    - 상위 10개 제품의 평균 점수
    - 카테고리 정확도

    Args:
        query: 검색어
        category: 예상 카테고리

    Returns:
        품질 비교 결과
    """
    # TODO: 구현
    pass
```

## 실습 과제

다음 검색어로 비교 분석을 수행하세요:

1. **"냉장고"** - 카테고리: 냉장고
2. **"프리미엄"** - 카테고리: 김치냉장고
3. **"에너지 효율"** - 카테고리: 냉장고

각 검색어에 대해:
- 결과 수 비교
- 평균 응답 시간 비교
- 평균 관련도 점수 비교
- 상위 5개 제품 비교

## 예상 출력

```
=== 검색어: "냉장고" ===

--- SEMANTIC 모드 ---
결과 수: 15개
평균 응답 시간: 245ms
평균 관련도: 0.82
상위 제품:
  1. 삼성 비스포크 냉장고 (0.95)
  2. LG 디오스 냉장고 (0.92)
  ...

--- HYBRID 모드 (카테고리: 냉장고) ---
결과 수: 12개
평균 응답 시간: 198ms
평균 관련도: 0.85
상위 제품:
  1. 삼성 비스포크 냉장고 (0.95)
  2. LG 디오스 냉장고 (0.92)
  ...

--- 비교 분석 ---
✓ HYBRID가 3개 적은 결과 (더 정확)
✓ HYBRID가 47ms 빠름 (19% 향상)
✓ HYBRID의 평균 관련도가 0.03 높음
✓ 상위 5개 제품 중 4개 일치

권장: HYBRID 모드 (카테고리 필터 적용 시)
```

## 분석 질문

다음 질문에 답하세요:

1. **언제 SEMANTIC 모드가 더 좋은가?**
   - 예시와 함께 설명

2. **언제 HYBRID 모드가 더 좋은가?**
   - 예시와 함께 설명

3. **성능 차이의 원인은?**
   - threshold 차이 (0.7 vs 0.5)
   - topK 차이
   - 필터링 오버헤드

4. **프로덕션 추천**
   - 어떤 상황에서 어떤 모드를 사용할 것인가?

## 보너스 과제

1. **시각화**:
   - 모드별 응답 시간 히스토그램
   - 관련도 점수 분포 비교

2. **Threshold 실험**:
   - 다양한 threshold 값(0.3, 0.5, 0.7, 0.9)으로 테스트
   - 최적의 threshold 찾기

3. **대용량 테스트**:
   - 100개 이상의 검색어로 자동 테스트
   - 통계적 유의미성 검증

## 제출

1. 완성된 코드
2. 실행 결과 (최소 3개 검색어)
3. 분석 보고서 (질문에 대한 답변)

## 도움말

- [CONCEPTS.md - 시맨틱 검색 vs 키워드 검색](../../CONCEPTS.md#시맨틱-검색-vs-키워드-검색)
- [TUTORIAL.md - Step 5: 하이브리드 검색](../../TUTORIAL.md#step-5-하이브리드-검색-구현)

## 정답 예시

정답은 `solutions/exercise-02-solution.py`에서 확인할 수 있습니다.
