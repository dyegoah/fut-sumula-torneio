# ESTÁGIO 1: Build (Compilação do projeto)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o arquivo pom.xml e baixa as dependências (otimiza o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia todo o código fonte (incluindo o HTML/CSS/JS em src/main/resources/static)
COPY src ./src

# Compila o projeto ignorando os testes (para fazer o deploy mais rápido)
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Run (Rodar o aplicativo no Render)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o arquivo .jar gerado no Estágio 1 para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar o sistema
ENTRYPOINT ["java", "-jar", "app.jar"]