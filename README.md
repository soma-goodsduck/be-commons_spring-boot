<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]

 
<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/soma-goodsduck/goodsduck_front">
    <img src="https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/icon/logo.svg" alt="Logo" width="400" height="80">
  </a>

  <p align="center">
    굿즈를 모으는 덕후들을 위한 서비스
    <br />
    <a href="https://www.goods-duck.com/"><strong>goods-duck.com »</strong></a>
  </p>
</p>

<br>
<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#setting-environment-variables">Setting Environment Variables</a></li>
      </ul> 
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<img src="https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/image/goodsduck_readme.png" alt="Logo" width="800" height="400">

최근 국내에는 중고거래 시장이 활성화 되었으며, 좋은 서비스를 제공하는 플랫폼들이 다수 존재합니다. 하지만, 아이돌을 덕질을 하는 팬(덕후)들을 위한 거래 플랫폼은 많지 않습니다. 이에 저희 덕스(Ducks) 팀은 덕후들을 만족시키기 위한 플랫폼을 기획하였습니다.

검증 가설은 다음과 같습니다:
* 필터링 기능을 제공하면 편한 거래 플랫폼이 될 것이고, 가격제안으로 채팅이 이어지면 시간이 줄어들 것이다.
* 투표 기능을 제공하면 유저들의 유입방안이 되고, 커뮤니티가 있으면 재밌어서 유저가 오래 머물 것이다.
* 개인화 추천 기능을 제공하면 사용자들이 우리 플랫폼을 선호하는 이유가 될 것이다.

현재 지속적으로 애자일(Agile) 방법론을 기반으로 개발을 진행하고 있습니다. 제공하는 서비스의 중심이 거래이기 때문에, 거래 기능 사이클의 완성을 1차 목표(~8월 중순)로 하고 있습니다. 추가적인 투표 및 커뮤니티 기능은 2차 목표(~9월 말), 추천 시스템 적용은 3차 목표(~10월 중순)로 계획하고 있습니다.  

### Built With

This section should list any major frameworks that you built your project using. Leave any add-ons/plugins for the acknowledgements section. Here are a few examples.
* Windows / macOS
* Java version 11.0.11 / JVM
* Gradle
* Spring boot version 2.4.8
* MySQL
* Intellij



<!-- GETTING STARTED -->
## Getting Started

### Installation
1. Intellij를 설치한다.
2. 레포지토리를 clone 한다.
3. 필요한 환경 변수 및 파일에 대해 설정한다.

### Setting Environment Variables

```sh
# application-[name].yml
# You need to set environment varialbes

# localmysql
spring.jpa.database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.hibernate.ddl-auto: update
spring.jpa.open-in-view: false
spring.jpa.properties.hibernate.format_sql: true
spring.jpa.properties.hibernate.default_batch_fetch_size: 100
spring.datasource.sql-script-encoding: UTF-8
spring.datasource.initialization-mode: embedded
spring.datasource.driver-class-name: com.mysql.cj.jdbc.Driver
spring.datasource.url:
spring.datasource.username:
spring.datasource.password:
spring.logging.level.org.hibernate.SQL: DEBUG

# oauth2
spring.security.oauth2.client.registration.naver.client-id:
spring.security.oauth2.client.registration.naver.client-secret:
spring.security.oauth2.client.registration.naver.redirect-uri:
spring.security.oauth2.client.registration.naver.authorization-grant-type: authorization_code
spring.security.oauth2.client.registration.naver.scope:	name,email
spring.security.oauth2.client.registration.naver.client-name: Naver
spring.security.oauth2.client.registration.kakao.client-id:
spring.security.oauth2.client.registration.kakao.redirect-uri: https://www.goods-duck.com/auth/kakao/callback
spring.security.oauth2.client.registration.kakao.authorization-grant-type: authorization_code
spring.security.oauth2.client.provider.naver.authorization-uri:	https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri:	https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri: https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.provider.naver.user-name-attribute: response
spring.security.oauth2.client.provider.kakao.user-info-uri: https://kapi.kakao.com/v2/user/me
spring.security.oauth2.client.provider.kakao.token-uri:	https://kauth.kakao.com/oauth/token
spring.security.jwt.expire-time:
spring.security.jwt.secret-key:

# s3
spring.servlet.multipart.max-file-size: 10MB
spring.servlet.multipart.max-request-size: 10MB
spring.servlet.multipart.file.path.local:
cloud.aws.stack.auto: false
cloud.aws.region.static:
cloud.aws.credentials.accessKey:
cloud.aws.credentials.secretKey:
cloud.aws.s3.itemBucket:
cloud.aws.s3.profileBucket:
cloud.aws.s3.chatBucket:

#### DataBase (MySQL)

#### 소셜 로그인(OAuth)
- NAVER ([Document](https://developers.naver.com/docs/login/devguide/devguide.md#%EB%84%A4%EC%9D%B4%EB%B2%84%EC%95%84%EC%9D%B4%EB%94%94%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EA%B0%9C%EB%B0%9C%EA%B0%80%EC%9D%B4%EB%93%9C))

- KAKAO ([Document](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api))

#### AWS Secrets Manager
#### AWS S3
#### FCM (Firebase Cloud Messaging)



<!-- CONTRIBUTING -->
## Contributing

컨트리뷰션은 오픈소스 커뮤니티를 모두가 참여하여 배울 수 있는 멋진 공간으로 만들어주는 활동입니다. 어떤 컨트리뷰션이라도 달아주시면 **굉장히 감사하겠습니다.** 

1. 프로젝트를 포크(Fork) 해주세요.
2. 피처 브랜치(Branch)를 생성하고 동시에 checkout합니다. (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋(Commit)합니다. (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치를 remote에 푸시(Push)합니다. (`git push origin feature/AmazingFeature`)
5. 풀 리퀘스트(Pull Request)를 열어주세요.



## License

[MIT](./LICENSE)

<div align="center">

<sub><sup>Project by <a href="https://github.com/2dowon">@2dowon</a> <a href="https://github.com/Ting-Kim">@Ting-Kim</a> <a href="https://github.com/W0nee">@W0nee</a></sup></sub>

</div>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->




<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/soma-goodsduck/be-commons_spring-boot.svg?style=for-the-badge
[contributors-url]: https://github.com/soma-goodsduck/be-commons_spring-boot/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/soma-goodsduck/be-commons_spring-boot.svg?style=for-the-badge
[forks-url]: https://github.com/soma-goodsduck/be-commons_spring-boot/network/members
[stars-shield]: https://img.shields.io/github/stars/soma-goodsduck/be-commons_spring-boot.svg?style=for-the-badge
[stars-url]: https://github.com/soma-goodsduck/be-commons_spring-boot/stargazers
[issues-shield]: https://img.shields.io/github/issues/soma-goodsduck/be-commons_spring-boot.svg?style=for-the-badge
[issues-url]: https://github.com/soma-goodsduck/be-commons_spring-boot/issues
[license-shield]: https://img.shields.io/github/license/soma-goodsduck/be-commons_spring-boot.svg?&style=for-the-badge
[license-url]: https://github.com/soma-goodsduck/be-commons_spring-boot/blob/main/LICENSE
