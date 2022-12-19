FROM gradle:7-jdk17 AS build
# todo: copy build.gradle/.. first and cache dependencies to avoid download on every code change
COPY --chown=gradle:gradle . /home/app/src
WORKDIR /home/app/src
RUN gradle uberJarBuild

FROM eclipse-temurin:17-alpine
EXPOSE 8085
RUN mkdir /app
COPY --from=build /home/app/src/build/*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
