#!/usr/bin/env bash

REPOSITORY=/opt/be-commons_spring-boot
cd $REPOSITORY

APP_NAME=be-commons_spring-boot
JAR_PATH=$REPOSITORY/build/libs/
ENV_PATH=/opt/application_env

CURRENT_PID=$(pgrep -fl $REPOSITORY/build/libs/ | grep java | awk '{print $1}')

echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]
then
  echo "> 종료할것 없음."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "> JAR PATH: $JAR_PATH"
echo "> $JAR_PATH 배포"

JAR_NAME=$(ls -tr $JAR_PATH/*.jar | tail -n 1)
TEST_PATH=$(ls $ENV_PATH/application-db.yml)

echo "ENV PATH 인식 > $TEST_PATH"

nohup java -javaagent:/opt/newrelic/newrelic.jar \
        -jar -Dspring.config.location=classpath:/application.yml,$ENV_PATH/application-develop.yml,$ENV_PATH/application-s3.yml \
        $JAR_NAME > $JAR_PATH/nohup.out 2>&1 &
