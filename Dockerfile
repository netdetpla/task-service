FROM openjdk:11.0.5-jre-stretch

ADD ["build/libs/task-service-1-all.jar", "settings.properties", "config.yaml", "job-template.json", "/"]

CMD java -jar task-service-1-all.jar