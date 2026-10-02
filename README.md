# FinSight — Backend

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

API REST para gestão financeira pessoal, com upload de extratos bancários, categorização de transações e geração de insights financeiros via inteligência artificial.

**Demo ao vivo:** https://finsight-frontend-lemon.vercel.app

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Segurança](#segurança)
- [Rodando localmente](#rodando-localmente)
- [Deploy](#deploy)

## Sobre o projeto

O FinSight permite que o usuário importe extratos bancários (CSV ou OFX), acompanhe suas transações categorizadas, visualize gráficos de receitas e despesas, e receba uma análise em linguagem natural do seu comportamento financeiro, gerada por IA a partir dos próprios dados.

Este repositório contém a API. O front-end (React) está em um [repositório separado](https://github.com/sxnozz/finsight-frontend).

## Funcionalidades

- Cadastro com verificação de e-mail obrigatória
- Autenticação via JWT
- Upload e parsing de extratos em CSV e OFX
- Categorização e edição manual de transações
- Geração de insights financeiros via Google Gemini
- Histórico de análises geradas
- Exclusão de conta com remoção completa dos dados
- Conta de demonstração somente leitura, para avaliação sem necessidade de cadastro

## Tecnologias

- **Java 21** + **Spring Boot 4**
- **Spring Security** — autenticação stateless via JWT
- **Spring Data JPA** + **PostgreSQL**
- **Google Gemini API** — geração de insights
- **Brevo API** — envio transacional de e-mail
- **Docker** — build e deploy

## Segurança

Pontos implementados ao longo do desenvolvimento:

- Senhas com hash BCrypt — nunca armazenadas ou retornadas em texto puro
- JWT assinado com HMAC256, com verificação de emissor e expiração independente de fuso horário
- Rate limiting nos endpoints de cadastro, login e verificação, para mitigar força bruta
- Verificação de propriedade em todos os endpoints que acessam dados de usuário, prevenindo acesso indevido a dados de terceiros (IDOR)
- Consumo de cotas (tokens de IA e de extrato) feito via operação atômica no banco de dados, eliminando condição de corrida em requisições concorrentes
- Nenhum segredo (chaves de API, credenciais de banco, chave de assinatura JWT) fica no código, tudo via variáveis de ambiente
- CORS restrito a uma origem configurável, não aberto a qualquer domínio

## Rodando localmente

Pré-requisitos: Java 21, Maven, e um banco PostgreSQL (uma instância gratuita no [Neon](https://neon.com) funciona bem).


Defina as variáveis de ambiente abaixo antes de rodar:

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USER` | Usuário do banco |
| `DB_PASS` | Senha do banco |
| `JWT_SECRET` | Chave de assinatura dos tokens |
| `GEMINI_API_KEY` | Chave da API do Google Gemini |
| `BREVO_API_KEY` | Chave da API da Brevo |
| `MAIL_USER` | E-mail remetente configurado na Brevo |
| `CORS_ORIGINS` | URL do front-end autorizada a consumir a API |


A API sobe em `http://localhost:8080`.

## Deploy

Hospedado gratuitamente no [Render](https://render.com) via Docker, com banco PostgreSQL no [Neon](https://neon.com).

## Licença

Distribuído sob a licença MIT.

## Autor

[Autor](https://github.com/sxnozz) — [Linkedin](https://www.linkedin.com/in/gustavo-bizarro-soares)
