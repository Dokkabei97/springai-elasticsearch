package com.hl.springaielasticsearch

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class SampleDataInit(
    private val vectorStore: VectorStore,
) {
    @PostConstruct
    fun initializeData() {
        println("🚀 샘플 데이터 초기화 시작...")
        vectorStore.add(samples())
        println("✅ 샘플 데이터 ${samples().size}개 추가 완료!")
    }

    private fun samples(): List<Document> {
        val products = mutableListOf<Document>()

        products.add(
            Product
                .of(
                    productName = "LG 디오스 오브제컬렉션 김치톡톡 K328S12E",
                    description = "김치 전용 온도 관리 시스템과 특화 저장실을 갖춘 LG 김치냉장고, 용량 328L, 에너지효율 1등급",
                    brand = "오브제컬렉션",
                    maker = "LG전자",
                    category = "김치냉장고",
                    price = 800000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 비스포크 큐브 김치플러스 RQ32R7602AP",
                    description = "AI 김치 케어 기능과 큐브 디자인의 프리미엄 김치냉장고, 용량 327L, 에너지효율 1등급",
                    brand = "비스포크",
                    maker = "삼성전자",
                    category = "김치냉장고",
                    price = 900000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "위니아 딤채 스탠드형 WDT33CLMBS",
                    description = "최적 숙성 기술과 옵티멀 케어 시스템을 갖춘 위니아 김치냉장고, 용량 330L, 에너지효율 1등급",
                    brand = "딤채",
                    maker = "위니아대우",
                    category = "김치냉장고",
                    price = 750000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "SK매직 김치냉장고 ERK33ASMSS",
                    description = "5가지 저장모드와 정온 시스템을 갖춘 SK매직 김치냉장고, 용량 324L, 에너지효율 1등급",
                    brand = "SK매직",
                    maker = "SK매직",
                    category = "김치냉장고",
                    price = 680000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 디오스 오브제컬렉션 김치냉장고 K221PR14BR",
                    description = "2도어 스탠드형 김치냉장고, 온습도 최적화 시스템, 용량 221L, 에너지효율 1등급",
                    brand = "오브제컬렉션",
                    maker = "LG전자",
                    category = "김치냉장고",
                    price = 650000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 김치플러스 일반형 RQ22K5L01EC",
                    description = "메탈쿨링 플레이트와 트윈쿨링 시스템, 용량 221L, 에너지효율 1등급",
                    brand = "김치플러스",
                    maker = "삼성전자",
                    category = "김치냉장고",
                    price = 620000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "위니아 딤채 미니 WDT18CRMGS",
                    description = "컴팩트 사이즈 김치냉장고, 1인~2인 가구용, 용량 180L, 에너지효율 1등급",
                    brand = "딤채",
                    maker = "위니아대우",
                    category = "김치냉장고",
                    price = 580000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "하이얼 김치냉장고 HK-185NDA",
                    description = "스마트 온도조절과 특수 보관실 설계, 용량 185L, 에너지효율 2등급",
                    brand = "하이얼",
                    maker = "하이얼아시아",
                    category = "김치냉장고",
                    price = 520000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "캐리어 김치냉장고 CRF-KC255B",
                    description = "정밀 온도제어 시스템과 멀티 저장실, 용량 255L, 에너지효율 1등급",
                    brand = "캐리어",
                    maker = "캐리어에어컨",
                    category = "김치냉장고",
                    price = 590000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 디오스 오브제컬렉션 S631MC75Q",
                    description = "인버터 리니어 컴프레서와 노크온 도어 기능을 갖춘 프리미엄 냉장고, 용량 631L, 에너지효율 1등급",
                    brand = "오브제컬렉션",
                    maker = "LG전자",
                    category = "냉장고",
                    price = 2200000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 비스포크 4도어 프렌치도어 RF60A91R1AP",
                    description = "맞춤형 컬러 패널과 패밀리허브 기능을 갖춘 스마트 냉장고, 용량 609L, 에너지효율 1등급",
                    brand = "비스포크",
                    maker = "삼성전자",
                    category = "냉장고",
                    price = 2500000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 트윈스 양문형 냉장고 S833MC55Q",
                    description = "문인문 냉장실과 선형 컴프레서를 탑재한 대용량 양문형 냉장고, 용량 833L, 에너지효율 1등급",
                    brand = "LG",
                    maker = "LG전자",
                    category = "냉장고",
                    price = 1800000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 푸드쇼케이스 양문형 냉장고 RS63R5571SL",
                    description = "푸드쇼케이스와 트리플 쿨링 시스템을 갖춘 고급형 양문형 냉장고, 용량 635L, 에너지효율 1등급",
                    brand = "Samsung",
                    maker = "삼성전자",
                    category = "냉장고",
                    price = 1900000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "위니아 클라쎄 프렌치도어 ERF-V50FPS",
                    description = "스테인리스 프렌치도어 냉장고, 멀티에어플로우 시스템, 용량 505L, 에너지효율 1등급",
                    brand = "위니아",
                    maker = "위니아대우",
                    category = "냉장고",
                    price = 1200000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "하이얼 프렌치도어 냉장고 HRF-SF468W",
                    description = "슬림핏 디자인과 인버터 컴프레서, 용량 468L, 에너지효율 1등급",
                    brand = "하이얼",
                    maker = "하이얼아시아",
                    category = "냉장고",
                    price = 980000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 일반형 냉장고 B507SM",
                    description = "실버 메탈 디자인과 듀얼 냉각 시스템, 용량 507L, 에너지효율 2등급",
                    brand = "LG",
                    maker = "LG전자",
                    category = "냉장고",
                    price = 850000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 일반형 냉장고 RT53K6035SL",
                    description = "메탈쿨링 플레이트와 트윈쿨링 시스템, 용량 528L, 에너지효율 2등급",
                    brand = "Samsung",
                    maker = "삼성전자",
                    category = "냉장고",
                    price = 920000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "일렉트로룩스 프렌치도어 EQE6007SD",
                    description = "유럽 프리미엄 디자인과 고급 냉각 기술, 용량 600L, 에너지효율 1등급",
                    brand = "일렉트로룩스",
                    maker = "일렉트로룩스",
                    category = "냉장고",
                    price = 1600000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "보쉬 프렌치도어 냉장고 KFN96APEAG",
                    description = "독일 프리미엄 브랜드, 바이탈프레시 기술, 용량 610L, 에너지효율 1등급",
                    brand = "보쉬",
                    maker = "보쉬전자",
                    category = "냉장고",
                    price = 2800000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "캐리어 양문형 냉장고 CRF-TN550B",
                    description = "강화유리 선반과 디지털 온도조절, 용량 550L, 에너지효율 2등급",
                    brand = "캐리어",
                    maker = "캐리어에어컨",
                    category = "냉장고",
                    price = 780000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 미니 냉장고 B162W",
                    description = "원룸형 소형 냉장고, 직냉식 냉각, 용량 162L, 에너지효율 3등급",
                    brand = "LG",
                    maker = "LG전자",
                    category = "냉장고",
                    price = 380000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 미니 냉장고 RT18M6213SR",
                    description = "컴팩트 디자인 원룸형, 직냉식, 용량 182L, 에너지효율 3등급",
                    brand = "Samsung",
                    maker = "삼성전자",
                    category = "냉장고",
                    price = 420000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "위니아 소형 냉장고 ERT171DS",
                    description = "원룸 및 사무실용 소형 냉장고, 용량 171L, 에너지효율 3등급",
                    brand = "위니아",
                    maker = "위니아대우",
                    category = "냉장고",
                    price = 350000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "하이얼 소형 냉장고 HRF-176DA",
                    description = "슬림 디자인 소형 냉장고, 직냉식, 용량 176L, 에너지효율 3등급",
                    brand = "하이얼",
                    maker = "하이얼아시아",
                    category = "냉장고",
                    price = 320000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "SK매직 소형 냉장고 ERF-185SM",
                    description = "저소음 설계 소형 냉장고, 용량 185L, 에너지효율 3등급",
                    brand = "SK매직",
                    maker = "SK매직",
                    category = "냉장고",
                    price = 390000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼성 비즈니스 냉장고 RS84T5190M9",
                    description = "대용량 업소용 양문형 냉장고, 강화 내구성, 용량 847L, 에너지효율 2등급",
                    brand = "Samsung",
                    maker = "삼성전자",
                    category = "냉장고",
                    price = 2100000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "LG 상업용 냉장고 B761GM",
                    description = "상업용 대용량 냉장고, 강화 압축기, 용량 761L, 에너지효율 2등급",
                    brand = "LG",
                    maker = "LG전자",
                    category = "냉장고",
                    price = 1950000,
                ).toDocument(),
        )

        // 식품 제품 추가
        products.add(
            Product
                .of(
                    productName = "오뚜기 진라면 매운맛",
                    description = "깔끔하고 얼큰한 국물맛의 대표 라면, 120g",
                    brand = "오뚜기",
                    maker = "오뚜기",
                    category = "식품",
                    price = 850,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "농심 신라면",
                    description = "한국인이 가장 사랑하는 매운맛 라면, 120g",
                    brand = "농심",
                    maker = "농심",
                    category = "식품",
                    price = 900,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "삼양 불닭볶음면",
                    description = "매운맛의 대명사 불닭볶음면, 140g",
                    brand = "삼양",
                    maker = "삼양식품",
                    category = "식품",
                    price = 1200,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "CJ 백설 설탕",
                    description = "순수한 백설탕, 1kg",
                    brand = "백설",
                    maker = "CJ제일제당",
                    category = "식품",
                    price = 3500,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "해표 간장",
                    description = "전통 조선간장, 500ml",
                    brand = "해표",
                    maker = "해표",
                    category = "식품",
                    price = 4800,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "오뚜기 참기름",
                    description = "100% 참깨로 만든 순수 참기름, 320ml",
                    brand = "오뚜기",
                    maker = "오뚜기",
                    category = "식품",
                    price = 12000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "샘표 연두 쌈장",
                    description = "신선한 채소와 함께 즐기는 쌈장, 500g",
                    brand = "샘표",
                    maker = "샘표식품",
                    category = "식품",
                    price = 5500,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "동원 참치캔 라이트",
                    description = "부드럽고 담백한 참치캔, 100g",
                    brand = "동원",
                    maker = "동원F&B",
                    category = "식품",
                    price = 2800,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "대상 청정원 고추장",
                    description = "전통 발효 고추장, 500g",
                    brand = "청정원",
                    maker = "대상",
                    category = "식품",
                    price = 4200,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "롯데 초코파이",
                    description = "부드러운 마시멜로우와 초콜릿 코팅, 12개입",
                    brand = "롯데",
                    maker = "롯데제과",
                    category = "식품",
                    price = 3200,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "오리온 후레쉬베리",
                    description = "상큼한 딸기맛 젤리, 66g",
                    brand = "오리온",
                    maker = "오리온",
                    category = "식품",
                    price = 1500,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "농심 새우깡",
                    description = "바삭한 새우맛 과자, 90g",
                    brand = "농심",
                    maker = "농심",
                    category = "식품",
                    price = 1800,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "하이트진로 참이슬",
                    description = "깔끔한 맛의 대표 소주, 360ml",
                    brand = "참이슬",
                    maker = "하이트진로",
                    category = "식품",
                    price = 1800,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "매일유업 매일우유",
                    description = "신선한 원유 100% 우유, 1L",
                    brand = "매일",
                    maker = "매일유업",
                    category = "식품",
                    price = 2800,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "남양유업 프렌치카페",
                    description = "부드럽고 진한 커피우유, 240ml",
                    brand = "남양",
                    maker = "남양유업",
                    category = "식품",
                    price = 1200,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "빙그레 바나나맛 우유",
                    description = "달콤한 바나나맛 우유, 240ml",
                    brand = "빙그레",
                    maker = "빙그레",
                    category = "식품",
                    price = 1300,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "코카콜라",
                    description = "시원하고 상쾌한 콜라, 350ml 캔",
                    brand = "코카콜라",
                    maker = "코카콜라",
                    category = "식품",
                    price = 1500,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "칠성사이다",
                    description = "톡 쏘는 시원한 사이다, 350ml 캔",
                    brand = "칠성",
                    maker = "롯데칠성음료",
                    category = "식품",
                    price = 1400,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "웅진 하늘보리",
                    description = "구수한 보리차, 500ml",
                    brand = "웅진",
                    maker = "웅진식품",
                    category = "식품",
                    price = 1000,
                ).toDocument(),
        )

        products.add(
            Product
                .of(
                    productName = "대상 미원",
                    description = "감칠맛을 더하는 조미료, 250g",
                    brand = "미원",
                    maker = "대상",
                    category = "식품",
                    price = 3800,
                ).toDocument(),
        )

        return products
    }
}
