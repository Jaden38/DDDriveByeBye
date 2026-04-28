FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests package || \
    (curl -sSL https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz \
       | tar -xz -C /opt && /opt/apache-maven-3.9.9/bin/mvn -B -DskipTests package)

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
