# Teste local do primeiro acesso por e-mail

## 1. Atualizar a branch de teste

Abra o PowerShell dentro da pasta do projeto e execute:

```powershell
git fetch origin
git switch agent/configurar-primeiro-acesso-email
git pull
```

## 2. Iniciar pelo arquivo automático

Na raiz do projeto, dê dois cliques em:

```text
iniciar-local.bat
```

Na primeira execução, ele criará automaticamente:

```text
src/main/resources/application-local.properties
```

A partir do arquivo seguro de exemplo.

## 3. Preencher a configuração privada

Abra `src/main/resources/application-local.properties` e substitua:

```properties
spring.datasource.password=COLOQUE_A_SENHA_DO_POSTGRES
spring.mail.password=COLOQUE_A_SENHA_DE_APLICATIVO
```

Não coloque aspas. Não envie esse arquivo para o GitHub.

## 4. Banco local esperado

```text
PostgreSQL: localhost:5432
Banco: fut-sumula-torneio-db
Usuário: postgres
```

Crie o banco somente se ele ainda não existir:

```sql
CREATE DATABASE "fut-sumula-torneio-db";
```

## 5. Iniciar novamente

Execute novamente `iniciar-local.bat`.

O arquivo entra automaticamente na pasta que contém o `pom.xml` e executa:

```text
mvnw.cmd clean spring-boot:run
```

Quando o wrapper não estiver disponível, usa:

```text
mvn -f pom.xml clean spring-boot:run
```

A aplicação deve iniciar em:

```text
http://localhost:8011
```

## 6. Testar somente o SMTP

Abra outro PowerShell:

```powershell
$body = @{ destino = "SEU_EMAIL_DE_TESTE@gmail.com" } | ConvertTo-Json
Invoke-RestMethod `
  -Uri "http://localhost:8011/api/auth/local/testar-email" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Resposta esperada:

```text
E-mail de teste enviado com sucesso para ...
```

## 7. Testar o fluxo completo

1. Abra `http://localhost:8011/cadastro.html`.
2. Cadastre uma conta usando um e-mail diferente do remetente.
3. A resposta deve informar que o link de primeiro acesso foi enviado.
4. Tente entrar antes de confirmar; o acesso deve ser bloqueado.
5. Abra o e-mail e clique no link iniciado por `http://localhost:8011/api/auth/confirmar-email?token=`.
6. O navegador deve redirecionar para `/login.html?ativado=true`.
7. Faça login novamente; o acesso deve ser liberado.

## Diagnóstico rápido

- `No plugin found for prefix 'spring-boot'`: comando executado fora da pasta do `pom.xml`; use `iniciar-local.bat`.
- `535 Authentication unsuccessful`: senha de aplicativo rejeitada ou SMTP AUTH bloqueado.
- `Connection timed out`: firewall, antivírus ou rede bloqueando a porta 587.
- `Mail server connection failed`: confira `smtp-mail.outlook.com`, porta `587` e STARTTLS.
- Link apontando para Render ou porta errada: confirme `app.base-url=http://localhost:8011`.
