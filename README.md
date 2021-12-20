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

![springboot-shield]
![mysql-shield]
![aws-shield]
![s3-shield]
![firebase-shield]

 
<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/soma-goodsduck/goodsduck_front">
    <img src="https://github.com/soma-goodsduck/goodsduck_front/blob/main/public/img/goodsduck.png?raw=true" alt="Logo" width="400">
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
        <li><a href="#run-the-application">Run the application</a></li>
      </ul> 
    </li>
    <li>
      <a href="#used-tech-stack">Used Tech Stack</a>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<a href="https://www.youtube.com/watch?v=CF4kLo6neUs">
  <img src="https://github.com/soma-goodsduck/goodsduck_front/blob/main/public/img/goodsduck_readme.png?raw=true" alt="Logo">
</a>

<div align="center">
  <sup><a href="https://www.youtube.com/watch?v=CPJONwdI1OA">Click for Demo Video</a> </sup>
</div>

최근 국내에는 중고거래 시장이 활성화 되었으며, 좋은 서비스를 제공하는 플랫폼들이 다수 존재합니다. 하지만, 아이돌을 덕질을 하는 팬(덕후)들을 위한 거래 플랫폼은 많지 않습니다. 이에 저희 덕스(Ducks) 팀은 덕후들을 만족시키기 위한 플랫폼을 기획하였습니다.

검증 가설은 다음과 같습니다:
* 필터링 기능을 제공하면 편한 거래 플랫폼이 될 것이고, 가격제안으로 채팅이 이어지면 시간이 줄어들 것이다.
* 투표 기능을 제공하면 유저들의 유입방안이 되고, 커뮤니티가 있으면 재밌어서 유저가 오래 머물 것이다.
* 개인화 추천 기능을 제공하면 사용자들이 우리 플랫폼을 선호하는 이유가 될 것이다.

현재 지속적으로 애자일(Agile) 방법론을 기반으로 개발을 진행하고 있습니다. 제공하는 서비스의 중심이 거래이기 때문에, 거래 기능 사이클의 완성을 1차 목표(~8월 중순)로 하고 있습니다. 추가적인 투표 및 커뮤니티 기능은 2차 목표(~9월 말), 추천 시스템 적용은 3차 목표(~10월 중순)로 계획하고 있습니다.

현재 구현된 기능(API)에 대한 명세서는 <a href="https://api.goods-duck.com/swagger-ui/index.html">여기</a>서 확인하실 수 있습니다.

### Built With

* MacOS (Big Sur 11.4)
* Java version 11.0.11
* Gradle
* Spring boot version 2.4.8
* MySQL 5.7
* (IDE) Intellij Community Edition 2021.01



<!-- GETTING STARTED -->
## Getting Started

> This backend application is design with React project. ([goodsduck-front repository](https://github.com/soma-goodsduck/goodsduck_front))  

### Installation
1. Install the <a href="https://www.jetbrains.com/ko-kr/idea/download/">Intellij</a> (IDE)
2. Install <a href="https://dev.mysql.com/downloads/mysql/5.7.html">MySQL</a> database. 
3. Clone the repository.
4. Set the environment variables and files.
    - MySQL
    - OAuth2 ([Naver](https://developers.naver.com/docs/login/devguide/devguide.md#%EB%84%A4%EC%9D%B4%EB%B2%84%EC%95%84%EC%9D%B4%EB%94%94%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EA%B0%9C%EB%B0%9C%EA%B0%80%EC%9D%B4%EB%93%9C), [Kakao](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api))
    - FCM (Firebase Cloud Messaging)
5. Run the application.

### Setting Environment Variables

```yaml
# application-[name].yml
# You need to set environment varialbes
# (if you want to run this application in AWS EC2, you can manage this Environment Variables with AWS Secrets Manager)

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
```

### Setting FCM (Firebase Cloud Messaging)

If you want to use feature of alarm(messaging), you have to set something for firebase.
(please refer this firebase guide. ([Link](https://firebase.google.com/docs/cloud-messaging/auth-server?hl=ko)))

- put your `secret-key file(json)` of service account in `<project-root-path>/src/main/resources/<your-secret-key-filename.json>`
- add this variables in `application.yml` file 
```yaml
# application.yml

firebase:
  config-path: <your-secret-key-filename.json>
  database-url: <your-firebase-database-url>
```

<!-- Run the application -->
## Run the application

- It is needed that running application with environment property files. You have to edit configuration in Intellij (IDE).

<img src="https://github.com/soma-goodsduck/static_storage/blob/main/images/intellij_run_edit-configuration.png?raw=true" alt="Logo" width="700" height="350">

- When you open build.gradle, you may see this gradle mark. please click that for loading gradle changes.

<img src="https://github.com/soma-goodsduck/static_storage/blob/main/images/intellij_load-gradle-changes.png?raw=true" alt="Logo" width="500" height="350">

- Then, open gradle tab on right side and execute gradle task of `compileQuerysl`.

<img src="https://github.com/soma-goodsduck/static_storage/blob/main/images/intellij_execute-gradle-task-compileQuerydsl.png?raw=true?raw=true" alt="Logo" width="420" height="500">

- Before run spring-boot application, please turn on the MySQL Server.
   - In the terminal, run a command `mysql.server start`.


- It's done. Now, you can run application for running `CommonsApplication.java`!   

<!-- Used Tech Stack -->
## Used Tech Stack

- JWT
- OAuth2
- Web MVC Pattern
- Spring Data JPA
- QueryDSL
- FCM (Firebase Cloud Messaging)

<!-- CONTRIBUTING -->
## Contributing

This repository managed based on forked pull request strategy

```sh
# Fork this repository to yours.
$ git clone [YOUR_REPOSITORY_URL]
$ cd be-commons_spring-boot

# Open with Intellij IDE.

$ git checkout -b [feature/YOUR_REPOSITORY]
# (Working...)

$ git commit [...]
$ git push origin [feature/YOUR_REPOSITORY]

# Enroll pull-request!
# in https://github.com/soma-goodsduck/be-commons_spring-boot/pulls
```

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
[firebase-shield]: https://img.shields.io/badge/Firebase-FFCA28.svg?&style=for-the-badge&logo=Firebase&logoColor=white
[s3-shield]: https://img.shields.io/badge/AmazonS3-569A31.svg?&style=for-the-badge&logo=AmazonS3&logoColor=white
[aws-shield]: https://img.shields.io/badge/AmazonAWS-232F3E.svg?&style=for-the-badge&logo=AmazonAWS&logoColor=white
[mysql-shield]: https://img.shields.io/badge/MySQL-569A31.svg?&style=for-the-badge&logo=MySQL&logoColor=white
[springboot-shield]: https://img.shields.io/badge/SpringBoot-6DB33F.svg?&style=for-the-badge&logo=SpringBoot&logoColor=white

