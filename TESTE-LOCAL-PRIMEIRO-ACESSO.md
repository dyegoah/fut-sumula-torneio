# Teste local do primeiro acesso por e-mail

## 1. Usar a branch de teste

```bash
git fetch origin
git switch agent/configurar-primeiro-acesso-email
git pull
```

## 2. Criar a configuração local privada

No diretório `src/main/resources`, copie:

```text
application-local.example.properties
```

para:

```text
application-local.properties
```

No arquivo copiado, altere somente:

```properties
spring.datasource.password=SUA_SENHA_DO_POSTGRES
spring.mail.password=SUA_SENHA_DE_APLICATIVO_DA_MICROSOFT
```

Não remova `application-local.properties` do `.gitignore` e não envie esse arquivo para o GitHub.

## 3. Confirmar o banco local

A configuração de exemplo usa:

```text
PostgreSQL: localhost:5433
Banco: fut_sumula_db
Usuário: postgres
```

Crie o banco se ele ainda não existir:

```sql
CREATE DATABASE fut_sumula_db;
```

## 4. Iniciar a aplicação

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Ou, quando o wrapper não estiver disponível:

```powershell
mvn spring-boot:run
```

A aplicação deve iniciar em:

```text
http://localhost:8080
```

## 5. Testar somente o SMTP

Abra outro PowerShell e execute, trocando o endereço de destino:

```powershell
$body = @{ destino = "SEU_EMAIL_DE_TESTE@gmail.com" } | ConvertTo-Json
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/auth/local/testar-email" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Resposta esperada:

```text
E-mail de teste enviado com sucesso para ...
```

Esse endpoint só existe quando o perfil ativo é `local`.

## 6. Testar o fluxo completo

1. Abra `http://localhost:8080/cadastro.html`.
2. Cadastre uma conta usando um e-mail diferente do remetente.
3. A resposta deve informar que o link de primeiro acesso foi enviado.
4. Tente entrar antes de confirmar o e-mail; o acesso deve ser bloqueado.
5. Abra o e-mail recebido e clique no link com endereço `http://localhost:8080/api/auth/confirmar-email?token=...`.
6. O navegador deve redirecionar para `/login.html?ativado=true`.
7. Faça login novamente; o acesso deve ser liberado.

## Diagnóstico rápido

- `535 Authentication unsuccessful`: senha de aplicativo rejeitada, SMTP AUTH indisponível ou atividade bloqueada pela Microsoft.
- `Connection timed out`: firewall, antivírus ou rede bloqueando a porta 587.
- `Mail server connection failed`: confira `smtp-mail.outlook.com`, porta `587` e STARTTLS.
- Link apontando para Render: confirme `app.base-url=http://localhost:8080` no arquivo local.
