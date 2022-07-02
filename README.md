# planit-server

dev 서버 1차 자동 배포
- yml 파일 에러 해결
- 로그인, 회원가입 API
- 친구 API (추가, 조회, 검색)
- 서버의 yml 파일 사라지는지 테스트

dev 서버 2차 자동 배포
- 아이템 API

dev 서버 3차 자동 배포
- application.yml 추가
- 자동 배포 테스트
- application.yml을 dev, local 환경으로 분리
- 액세스 토큰 시간 변경
- 액세스 토큰 시간 하루로 변경
- FCM 관련 설정 수정
- 테스트2

dev 서버 4차 자동 배포
- 가출 관련 스프링 배치 및 스케줄러 작성
- application-dev.yml, deploy.yml 
- 재시작

dev 서버 5차 자동 배포
- 회원 탈퇴 API 호출 시, Users 테이블과 연관된 모든 데이터 삭제
- application-prod.yml 추가
- @Transactional 어노테이션 추가