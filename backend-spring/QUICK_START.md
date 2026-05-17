# 🚀 CodeCraft Backend - ПОЛНОСТЬЮ ГОТОВ К ЗАПУСКУ

## Статус

✅ **Spring Boot Backend создан и готов к использованию**
✅ **Все endpoints реализованы**
✅ **Интеграция с Supabase (2306 data)**
✅ **Firebase аутентификация**
✅ **Система HP и решений заданий**
✅ **Docker контейнер готов**

## Путь новогоклиента (как описали)

```
1. Пользователь авторизуется (Firebase Auth)
2. Backend создает new профиль с:
   - XP = 0
   - Level = 1
   - Streak = 0
   - HP = как счетчик попыток (макс 3 за уровень)
3. Пользователь выбирает уровень и код
4. Отправляет решение на POST /api/levels/{id}/submit
5. Backend проверяет код с лучшего решения:
   - CORRECT: +50 XP → сохранить в Supabase
   - WRONG: 0 XP, -1 HP → сохранить попытку
6. Профиль обновляется в Supabase
7. При следующем входе - все данные загруженыих (persistent)
```

## Быстрый старт (Windows PowerShell)

### Вариант 1: Maven (локально без Docker)

```powershell
# Перейти в папку backend
cd "C:\Users\behru\Downloads\CodeCraftFrontAp\CodeCraftFrontApp\backend-spring"

# Установить зависимости и запустить
mvn spring-boot:run
```

Приложение запустится на `http://localhost:8080`

### Вариант 2: Docker Compose (если установлен Docker)

```powershell
cd "C:\Users\behru\Downloads\CodeCraftFrontAp\CodeCraftFrontApp\backend-spring"
docker-compose up --build
```

### Вариант 3: Собрать JAR и запустить

```powershell
cd "C:\Users\behru\Downloads\CodeCraftFrontAp\CodeCraftFrontApp\backend-spring"
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Environment Variables (ОБЯЗАТЕЛЬНО!)

Должны быть установлены ДО запуска:

```powershell
$env:SUPABASE_URL = "https://diuvzzrbwxdufagcdbuz.supabase.co/rest/v1"
$env:SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg4NTMwODIsImV4cCI6MjA5NDQyOTA4Mn0.Feg92AbV0UE3H3lFNnrOKqMVKNlhsO6hOhL1bVyQzk4"
$env:SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODg1MzA4MiwiZXhwIjoyMDk0NDI5MDgyfQ.wzYXd4bUb-lFFd4ljhYruCisbUuj2RQM8wZ2JtLWrLE"
$env:SERVER_PORT = "8080"
```

Или создать файл `.env` в папке `backend-spring` и скопировать из `.env.example`.

## Тестирование API

### 1. Проверить здоровье backend'а

```powershell
curl http://localhost:8080/api/health
```

Ответ:
```json
{
  "status": "ok",
  "supabase": "connected"
}
```

### 2. РЕГИСТРАЦИЯ / Создание профиля

Первый шаг - получить Firebase ID Token (из Android приложения после авторизации):

```powershell
$firebaseToken = "<ВАШЕ_FIREBASE_ID_TOKEN_ЗДЕСЬ>"

curl -X POST http://localhost:8080/api/profile/register `
  -H "Authorization: Bearer $firebaseToken" `
  -H "Content-Type: application/json" `
  -d @'
{
  "display_name": "CodeMaster"
}
'@
```

Ответ:
```json
{
  "uid": "firebase_uid",
  "supabaseId": "uuid",
  "profile": {
    "id": "uuid",
    "display_name": "CodeMaster",
    "email": "user@example.com",
    "xp": 0,
    "level": 1,
    "streak": 0
  },
  "created": true
}
```

### 3. Получить профиль

```powershell
curl http://localhost:8080/api/profile/me `
  -H "Authorization: Bearer $firebaseToken"
```

### 4. Получить уровни по языку

```powershell
curllocalhost:8080/api/levels?language=javascript&track=beginner"
```

### 5. ОТПРАВИТЬ РЕШЕНИЕ (самое важное!)

```powershell
curl -X POST http://localhost:8080/api/levels/1/submit `
  -H "Authorization: Bearer $firebaseToken" `
  -H "Content-Type: application/json" `
  -d @'
{
  "code": "function helloWorld() { return 'Hello World'; }"
}
'@
```

Ответ если ПРАВИЛЬНО:
```json
{
  "correct": true,
  "xp_earned": 50,
  "hp_used": 0,
  "message": "Correct! Well done!"
}
```

Ответ если НЕПРАВИЛЬНО:
```json
{
  "correct": false,
  "xp_earned": 0,
  "hp_used": 1,
  "message": "Incorrect. Try again!"
}
```

### 6. Получить данные профиля после решения

```powershell
curl http://localhost:8080/api/profile/me `
  -H "Authorization: Bearer $firebaseToken"
```

Будет видно:
- `xp` увеличился на 50 (если правильно)
- `level` пересчитан
- данные сохранены в Supabase

### 7. Лидборд

```powershell
curl "http://localhost:8080/api/leaderboard?limit=25"
```

### 8. Прогресс пользователя

```powershell
curl http://localhost:8080/api/progress `
  -H "Authorization: Bearer $firebaseToken"
```

## Структура Файлов Backend

```
backend-spring/
├── pom.xml                          # Maven конфигурация
├── Dockerfile                        # Docker контейнер
├── docker-compose.yml               # Docker Compose (локально)
├── .env.example                     # Пример переменных окружения
├── .gitignore                       # Git ignore
├── README.md                        # Полная документация
├── src/main/java/com/codecraft/backend/
│   ├── CodeCraftBackendApplication.java
│   ├── config/
│   │   └── FirebaseConfig.java      # Firebase Admin SDK инициализация
│   ├── controller/
│   │   ├── AuthController.java      # Верификация токенов
│   │   ├── ProfileController.java   # Профиль & регистрация
│   │   ├── LeaderboardController.java  # Лидборд
│   │   ├── LevelsController.java    # Уровни
│   │   ├── ProgressController.java  # Прогресс
│   │   ├── SubmissionController.java # Отправка решений (ГЛАВНЫЙ!)
│   │   ├── AchievementsAndPreferencesController.java
│   │   └── HealthController.java    # Health check
│   ├── service/
│   │   ├── FirebaseService.java     # Firebase Admin SDK wrapper
│   │   └── SupabaseService.java     # Supabase REST API wrapper (ОСНОВНОЙ СЕРВИС)
│   └── dto/
│       ├── ProfileDto.java
│       ├── LevelDto.java
│       ├── LevelProgressDto.java
│       ├── SubmissionDto.java
│       ├── AchievementDto.java
│       └── UserPreferencesDto.java
└── src/main/resources/
    ├── application.properties        # Конфигурация Spring
    └── firebase-service-account.json # Firebase ключ (ВАЖНО: НЕ КОММИТИТЬ!)
```

## Основной Workflow Приложения

```
┌─────────────────┐
│ Android App     │
│ (Firebase Auth) │
└────────┬────────┘
         │
         │ 1. POST /api/profile/register
         │    (Firebase ID Token)
         ▼
┌─────────────────────────────────────┐
│ Backend (Spring Boot - Port 8080)   │
│─────────────────────────────────────│
│ ✓ Verify Firebase Token             │
│ ✓ Create Profile (XP=0, Level=1)    │
│ ✓ Create Preferences                │
│ ✓ Save to Supabase                  │
└────────┬──────────────────────────┬─┘
         │                          │
    GET /profile/me         POST /levels/{id}/submit
    (current stats)         (code solution)
         │                          │
         │              ┌───────────┴────────┐
         │              │                    │
         │              ▼                    ▼
         │         ┌──────────────┐  ┌──────────────┐
         │         │ CORRECT ✓    │  │ WRONG ✗      │
         │         │ +50 XP       │  │ 0 XP, -1 HP  │
         │         │ Level up     │  │ Save attempt │
         │         └──────┬───────┘  └──────┬───────┘
         │                │                 │
         │                │  ┌──────────────┘
         │                │  │
         │                ▼  ▼
         │         ┌─────────────────────┐
         │         │ Update in Supabase  │
         │         │ - profiles          │
         │         │ - xp_history        │
         │         │ - level_progress    │
         │         └──────────┬──────────┘
         │                    │
         └────────────────────┴────────────┐
                                           │
                         GET /profile/me (следующий вход)
                         ALL DATA PERSISTED ✓
```

## Данные в Supabase (где сохраняется всё)

### Таблица `profiles` (основной профиль)
- `id` (UUID от Firebase)
- `display_name` - имя игрока
- `email` - почта
- `xp` - всего опыта (обновляется при каждом решении)
- `level` - уровень (рассчитывается: level = xp / 400 + 1)
- `streak` - серия дней подряд

### Таблица `level_progress` (прогресс по каждому уровню)
- `user_id` - чей профиль
- `level_id` - какой уровень
- `language` - язык программирования
- `track` - категория (beginner/advanced/expert)
- `stars` - звезды (0-3)
- `completed` - завершен ли уровень
- `attempts` - сколько попыток

### Таблица `xp_history` (история всех транзакций XP)
- `user_id` - чей профиль
- `amount` - сколько XP добавлено/вычтено
- `reason` - причина (level_completed, wrong_submission, др.)
- `created_at` - когда

Всё это автоматически сохраняется в `SupabaseService.java`!

## Проверка Работы

**После регистрации:**
```
✓ БД: select * from profiles where id='...';
✓ Должен быть новый row с xp=0, level=1
```

**После решения (правильного):**
```
✓ БД: select xp, level from profiles where id='...';
✓ xp должен быть 50
✓ level должен быть пересчитан
✓ БД: select * from level_progress where user_id='...' and level_id=1;
✓ Должна быть запись с stars=3, completed=true
```

**После неправильного решения:**
```
✓ xp не меняется
✓ В level_progress появляется новая попытка
✓ Отслеживаются неправильные попытки через xp_history
```

## Если что-то не работает

### Ошибка: "Firebase service account file not found"
→ Убедиться, что `firebase-service-account.json` в `src/main/resources/`

### Ошибка: "Supabase connection refused"
→ Проверить `SUPABASE_URL`, все ключи, что Supabase онлайн

### Ошибка: "invalid_token"
→ Firebase токен истек (срок 1 час) или неправильный формат

### Ошибка: "CORS error"
→ Добавить URL приложения в `CORS_ORIGINS` environment variable

## Что дальше?

Когда приложение работает, можно:
- ✅ Запустить Android app и авторизоваться
- ✅ Получить Firebase ID Token
- ✅ Оттестировать каждый endpoint
- ✅ Проверить, что данные сохраняются в Supabase
- ✅ Развернуть на production (Render, Fly.io, AWS, GCP)

---

**Статус: ПОЛНОСТЬЮ ГОТОВО К ИСПОЛЬЗОВАНИЮ** ✅

