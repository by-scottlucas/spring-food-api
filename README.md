# Spring - Food API

## 📌 Introdução  

O projeto se trata de uma aplicação backend desenvolvida usando o Spring Framework para um sistema de delivery. A API contempla recursos para gerenciamento de usuários, Autenticação, gerenciamento de pedidos e checkout. O projeto adota uma arquitetura baseada em configs, controllers, services e repositories, models e enums. A aplicação também conta com testes unitários e de integração.

## 🚀 Como Rodar o Projeto  

Para executar o projeto localmente, siga os passos abaixo:  

1. **Clone o repositório**  
   ```bash
   git clone https://github.com/by-scottlucas/spring-food-api.git
   ```
2. **Acesse o diretório do projeto**  
   ```bash
   cd spring-food-api
   ```
3. **Configure o banco de dados no arquivo `application.properties`**  
   Exemplo com H2:
   ```properties
    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=password

    spring.h2.console.enabled=true
    spring.h2.console.path=/h2-console

    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=create
   ```
4. **Execute o projeto**  
   Pelo terminal ou pela sua IDE:
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Acesse a aplicação**  
   - A API estará disponível em: `http://localhost:8080`

## 🛠️ Tecnologias Utilizadas  

O projeto foi desenvolvido utilizando as seguintes tecnologias e ferramentas:

- **Spring Boot** – Framework para construção de aplicações Java modernas  
- **Spring Security + JWT** – Autenticação e autorização  
- **JPA / Hibernate** – Persistência de dados  
- **H2 Database** – Banco de dados em memória para testes  
- **JUnit / Mockito** – Testes unitários e de integração  
- **Maven** – Gerenciador de dependências  

## 📦 Funcionalidades  

- 🔐 **Autenticação com JWT**  
- 👤 **Gerenciamento de usuários**  
- 🛒 **Checkout de pedidos e finalização**  
- 📦 **Gerenciamento de pedidos**  
- 📄 **Enums para status de pedidos, pagamentos e formas de pagamento**  
- 🧪 **Testes unitários (services) e testes de integração (controllers)**  
- ⚠️ **Tratamento de exceções com mensagens customizadas**

## 👨‍💻 Autor  

Este projeto foi desenvolvido por **Lucas Santos Silva**, Desenvolvedor Full Stack, graduado pela **Escola Técnica do Estado de São Paulo (ETEC)** nos cursos de **Informática (Suporte)** e **Informática para Internet**.  

## 📜 Licença  

Este projeto está licenciado sob a [**Licença MIT**](./LICENSE).