FROM maven:3-amazoncorretto-17 AS build

WORKDIR /opt/myapp
COPY pom.xml .
COPY ./src ./src

# Declare build-time variables
ARG COGNITO_REGION
ARG USER_POOL
ARG CLIENT_ID
ARG ACCESS_KEY
ARG SECRET_KEY

# Set environment variables using build-time variables
ENV AWS_COGNITO_REGION=$COGNITO_REGION
ENV AWS_COGNITO_POOL_ID=$USER_POOL
ENV AWS_COGNITO_CLIENT_ID=$CLIENT_ID
ENV AWS_ACCESS_KEY=$ACCESS_KEY
ENV AWS_SECRET_KEY=$SECRET_KEY

RUN mvn package

FROM amazoncorretto:17-alpine
WORKDIR /opt/myapp
COPY --from=build /opt/myapp/target/ROOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
