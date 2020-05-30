## 실습 - 지하철 노선도 조회

### 요구사항
- [x] 모든 지하철 노선과 지하철역 목록을 조회한다
- [x] 페이지 연동

## 1단계 - 캐시 적용

### 요구사항
- [x] HTTP 캐시 적용하기
    - [x] 지하철 노선도 조회 시 ETag를 통해 캐시를 적용
    - [x] LineControllerTest의 ETag 테스트를 성공 시키기

## 2단계 - 지하철 경로 조회 1 (Happy Case)

### 요구사항
- [x] 최단 거리 기준으로 경로, 총 소요시간, 총 거리 응답
- [x] 최단 경로가 하나가 아닐 경우 어느 경로든 하나만 응답
- [x] API를 활용하여 페이지 연동

## 3단계 - 지하철 경로 조회 2 (Side Case)

### 요구사항
- [x] 단위 테스트를 통해 Side Case 검증
    - [x] 출발역이나 도착역을 입력하지 않은 경우
    - [x] 출발역, 도착역이 같은 경우
    - [x] 존재하지 않은 출발역이나 도착역을 조회 할 경우
    - [x] 출발역, 도착역이 연결되어 있지 않은 경우
    
## 4단계 - 지하철 경로 조회 3

- [x] 최소 시간 경로 기능을 추가
- [x] 최단 경로 조회 기능과의 중복 제거를 수행(테스트 코드도 마찬가지로 중복제거)

## TODO
- [x] graph 클래스 분리
- [x] line 도메인 일급 컬렉션 및 list로 변경
- [x] exception 추상화
- [x] exception handler 추가
- [ ] javascript 리팩토링