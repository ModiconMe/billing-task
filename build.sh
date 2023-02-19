# bin/bash
./gradlew clean build
docker build -t modiconme/billing:latest .
docker login -u $1 -p $2
docker push modiconme/billing:latest