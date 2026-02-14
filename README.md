# 📝 ToDoApp — Backend Task Management API

## 📌 O projekcie

ToDoApp to backendowa aplikacja REST API stworzona w celu zaprezentowania moich umiejętności programistycznych oraz znajomości ekosystemu Java i Spring. Projekt powstał jako praktyczne portfolio developerskie, skupione przede wszystkim na architekturze backendowej, bezpieczeństwie oraz pracy z bazą danych.

Aktualna wersja projektu stanowi pierwszą fazę rozwoju systemu. Na tym etapie aplikacja koncentruje się na implementacji czystej logiki backendowej, uwierzytelnianiu użytkowników oraz zarządzaniu zadaniami poprzez REST API.

W kolejnych etapach planowany jest rozwój projektu o nowe funkcjonalności, między innymi:

- możliwość tworzenia grup użytkowników,
- zadania współdzielone (np. zadanie grupowe),
- przypisywanie zadań przez jednego użytkownika do drugiego,
- prosty interfejs graficzny (GUI),
- dalsze rozszerzenia funkcjonalne.

Obecna wersja projektu skupia się na prezentacji fundamentów backendowych oraz dobrych praktyk projektowych.

---

## ⚙️ Stack technologiczny

Projekt został zbudowany w oparciu o następujące technologie:

- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker & Docker Compose
- JUnit 5 / Mockito

---

## 🚀 Najważniejsze funkcjonalności

- Rejestracja i logowanie użytkowników
- Uwierzytelnianie oparte o token JWT
- Zabezpieczone endpointy API
- Zarządzanie zadaniami (CRUD)
- Migracje bazy danych przy starcie aplikacji (Flyway)
- Konfiguracja środowisk przez zmienne ENV
- Gotowość do uruchomienia przez Docker

---

## ▶️ Uruchomienie projektu

### 📋 Wymagania

- Docker Desktop
- Git
- Postman

### 1️⃣ Sklonuj repozytorium

```bash
git clone <URL_REPOZYTORIUM>
cd ToDoApp
```


### 2️⃣ Utwórz plik .env

Na podstawie pliku .env.example utwórz własny .env:

DB_USER=postgres
DB_PASS=postgres

JWT_SECRET=PUT_YOUR_BASE64_SECRET_HERE

### 3️⃣ Uruchom aplikację
```bash 
docker compose up --build
```

Podczas pierwszego uruchomienia
zbudowany zostanie obraz aplikacji,
uruchomiona zostanie baza PostgreSQL,
a Flyway automatycznie wykona migracje.

### 4️⃣ Dostęp do API i testowanie
W Postmanie: 

http://localhost:8080

Przykładowe endpointy:

Rejestracja użytkownika
POST /auth/register

Body (JSON):

    {

    "email": "test@test.test",
  
    "password": "password123"
  
    }

Logowanie
POST /auth/login

Po zalogowaniu zwracany jest token JWT, który należy dodawać do nagłówka:

Authorization: Bearer TOKEN

Następnie można testować działanie na taskach i userach przy pomocy dostępnych endpointów.

