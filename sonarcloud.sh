#!/usr/bin/env bash

if [ "$TRAVIS_SECURE_ENV_VARS" == "true" ]; then
  ./gradlew sonarqube -Dsonar.host.url=$SONAR_HOST -Dsonar.organization=$SONAR_ORG -Dsonar.login=$SONAR_TOKEN -Dsonar.verbose=true
fi