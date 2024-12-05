LABEL authors="izanr"

# Frontend
FROM node:20.18.0 as frontend

WORKDIR /frontend

# Copy build and config files
COPY /frontend/package.json /frontend/package-lock.json /frontend/angular.json /frontend/tsconfig.app.json /frontend/tsconfig.json /frontend/tsconfig.spec.json /frontend/

RUN npm install && npm install -g @angular/cli

# Copy src files
COPY frontend/src /frontend/src

RUN ng build --configuration production

COPY /frontend/dist/frontend/browser /frontend/dist/frontend/browser

# Maven (frontend to backend)
FROM maven:3.9.9-openjdk-23 as build

WORKDIR /app

ENV RUNNING_IN_DOCKER = true

COPY backend/pom.xml /app/

COPY backend/src/ /app/src

# Copy production files from Angular to backend-public
COPY --from=frontend /frontend/dist/frontend/browser /app/src/main/resources/static

RUN mvn clean install -DskipTests

# Java
FROM amazoncorretto:17

WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app


