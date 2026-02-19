# ==========================================
# Stage 1: Build (Compilação)
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build

# Definir pasta de trabalho para a compilação
WORKDIR /build

# Copiar o pom.xml para descarregar dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar o código fonte e gerar o JAR com todas as dependências [cite: 33, 34]
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime (Execução)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# [Segurança] Instalar bibliotecas de compatibilidade para o SSL nativo (Conscrypt)
RUN apk add --no-cache gcompat libstdc++

# Definir a pasta de trabalho da aplicação
WORKDIR /app

# [Segurança] Criar utilizador e grupo 'javalin' para evitar correr como root
RUN addgroup -S javalin && adduser -S javalin -G javalin

# 1. Copiar o JAR compilado no Stage 1 (selecionando a versão com dependências) [cite: 32, 34]
COPY --from=build /build/target/taskslist-lab3-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# 2. Copiar os certificados necessários para o SslPlugin
COPY cert.pem /app/cert.pem
COPY key.pem /app/key.pem

# 3. Copiar ficheiros de configuração do Casbin (resolve CasbinAdapterException)
# Copiamos para a raiz (/app) para que o código os encontre sem caminhos complexos
COPY src/main/resources/model.conf /app/model.conf
COPY src/main/resources/policy.csv /app/policy.csv

# 4. Ajustar permissões para o utilizador non-root
RUN chown -R javalin:javalin /app

# Mudar para o utilizador seguro antes da execução
USER javalin

# Definir a variável de ambiente PORT conforme exigido [cite: 145, 147]
ENV PORT=7100

# Executar a aplicação usando o comando java -jar [cite: 34]
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]