# Web-History-AI-Tool-Backend

<br/>

## 프로젝트 소개

CAU 2024-02 캡스톤 프로젝트 WHAT의 백엔드 애플리케이션입니다.

백엔드에서 책임지는 대략적인 기능들은 아래와 같습니다:

- 사용자 인증 및 JWT 토큰 발급
- 브라우징 기록 저장 및 키워드 추출
- 사용자 검색어에 대한 코사인 유사도 검색

전체 프로젝트에 대한 자세한 설명과 사용법은 [프론트엔드 레포지토리](https://github.com/bagzaru/what-web-history-ai-tool-frontend)를 참조해주세요.

<br/>

## 기술 스택

![image](https://github.com/user-attachments/assets/89090907-17b3-4f23-8698-33571c56fec8)

### 버전

- JAVA 22.0.1

- Spring Boot 3.3.5

- Gradle 8.10.1

- PostgreSQL 16+

<br/>

## 설치 및 실행 방법

### Redis 설치

#### Windows

[Windows Redis 레포지토리](https://github.com/microsoftarchive/redis/releases) 에서 수동 설치

#### Ubuntu
```
sudo apt-get update
sudo apt-get install redis-server
```

#### MacOS
```
$ brew install redis
$ brew services start redis
```

<br/>

Redis 경로에서 redis.conf를 찾아 주석처리된 비밀번호 설정을 수정
```
# requirepass foobared
requirepass your-password
```

<br/>

### application.properties 설정

<br/>

#### JDBC

spring.datasource.url=YOUR-DB-URL

spring.datasource.username=YOUR-DB-USERNAME

spring.datasource.password=YOUR-DB-PASSWORD

<br/>

#### OPENAI

spring.ai.openai.api-key=YOUR-OPENAI-API-KEY

<br/>

#### PINECONE

pinecone.api-key=YOUR-PINECONE-API-KEY

pinecone.index=YOUR-PINECONE-INDEX-NAME

<br/>

#### JWT

jwt.secret=YOUR-JWT-SECRET-KEY

<br/>

#### REDIS

spring.data.redis.host=127.0.0.1 (서버 로컬)

spring.data.redis.port=6379

spring.data.redis.password=YOUR-REDIS-PASSWORD

<br/>

### 실행
#### 1. 프론트엔드 레포지토리 메뉴얼 대로 설치 후 실행

#### 2. 직접 서버를 사용자 검색어에 대한 코사인 유사도 검색

전체 프로젝트에 대한 자세한 설명과 사용법은 [프론트엔드 레포지토리](https://github.com/bagzaru/what-web-history-ai-tool-frontend)를 참조해주세요.

<br/>

## 기술 스택

![image](https://github.com/user-attachments/assets/89090907-17b3-4f23-8698-33571c56fec8)

### 버전

- JAVA 22.0.1

- Spring Boot 3.3.5

- Gradle 8.10.1

- PostgreSQL 16+

<br/>

## 설치 및 실행 방법

### Redis 설치

#### Windows

[Windows Redis 레포지토리](https://github.com/microsoftarchive/redis/releases) 에서 수동 설치

#### Ubuntu
```
sudo apt-get update
sudo apt-get install redis-server
```

#### MacOS
```
$ brew install redis
$ brew services start redis
```

<br/>

Redis 경로에서 redis.conf를 찾아 주석처리된 비밀번호 설정을 수정
```
# requirepass foobared
requirepass your-password
```

<br/>

### application.properties 설정

<br/>

#### JDBC

spring.datasource.url=YOUR-DB-URL

spring.datasource.username=YOUR-DB-USERNAME

spring.datasource.password=YOUR-DB-PASSWORD

<br/>

#### OPENAI

spring.ai.openai.api-key=YOUR-OPENAI-API-KEY

<br/>

#### PINECONE

pinecone.api-key=YOUR-PINECONE-API-KEY

pinecone.index=YOUR-PINECONE-INDEX-NAME

<br/>

#### JWT

jwt.secret=YOUR-JWT-SECRET-KEY

<br/>

#### REDIS

spring.data.redis.host=127.0.0.1 (서버 로컬)

spring.data.redis.port=6379

spring.data.redis.password=YOUR-REDIS-PASSWORD

<br/>

### 실행
#### 1. 프론트엔드 레포지토리 메뉴얼 대로 설치 후 실행

#### 2. 직접 서버를 구축하길 원할 경우

1. 로컬 혹은 서버에서 백엔드 애플리케이션 실행

2. 프론트엔드에서 [networkManager.js](https://github.com/bagzaru/what-web-history-ai-tool-frontend/blob/main/scripts/service-worker/networking/networkManager.js) 코드의 기본 연결 서버 주소 변경
```
//defaultHost: 기본 연결 서버 주소
const defaultHost = "http://localhost:8080";
```
혹은
```
//defaultHost: 기본 연결 서버 주소
const defaultHost = "https://yourserver.com";
```
