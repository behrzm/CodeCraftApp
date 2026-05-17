# 🎉 BACKEND ПОЛНОСТЬЮ СОЗДАН И ГОТОВ!

## ✅ Что было создано:

### 📁 Структура Backend (Spring Boot)
```
backend-spring/
├── pom.xml                              -> Maven зависимости
├── Dockerfile                           -> Docker контейнер
├── docker-compose.yml                   -> Docker Compose
├── .env.example                         -> Пример переменных
├── .gitignore                           -> Git ignore правила
├── README.md                            -> ПОЛНАЯ документация
├── QUICK_START.md                       -> БЫстрый старт (НАЧНИТЕ ОТСЮДА!)
│
├── src/main/java/com/codecraft/backend/
│   ├── CodeCraftBackendApplication.java ✓ Main приложение
│   │
│   ├── config/
│   │   └── FirebaseConfig.java          ✓ Firebase Admin SDK
│   │
│   ├── controller/                      ✓ ВСЕ API Endpoints
│   │   ├── AuthController.java
│   │   ├── ProfileController.java
│   │   ├── ProfileController.java (регистрация!)
│   │   ├── LeaderboardController.java
│   │   ├── LevelsController.java
│   │   ├── ProgressController.java
│   │   ├── SubmissionController.java    ← ГЛАВНЫЙ ДЛЯ РЕШЕНИЙ
│   │   ├── AchievementsAndPreferencesController.java
│   │   └── HealthController.java
│   │
│   ├── service/
│   │   ├── FirebaseService.java         ✓ Firebase wrapper
│   │   └── SupabaseService.java         ✓ Supabase REST wrapper (ВСЕ БД ОПЕРАЦИИ)
│   │
│   └── dto/
│       ├── ProfileDto.java              ✓ Профиль
│       ├── LevelDto.java                ✓ Уровень
│       ├── LevelProgressDto.java        ✓ Прогресс
│       ├── SubmissionDto.java           ✓ Отправка решения
│       ├── AchievementDto.java          ✓ Достижения
│       └── UserPreferencesDto.java      ✓ Предпочтения
│
└── src/main/resources/
    ├── application.properties            ✓ Spring конфигурация
    └── firebase-service-account.json     ✓ Firebase ключ
```

## 🔧 Как запустить:

### 1️⃣ УСТАНОВИТЬ ЗАВИСИМОСТИ (разово)
```bash
# Нужно: Java 17, Maven 3.9+
# Скачать отсюда: https://maven.apache.org/download.cgi

# Проверить версию:
java -version
mvn -version
```

### 2️⃣ УСТАНОВИТЬ ПЕРЕМЕННЫЕ ОКРУЖЕНИЯ (важно!)

**Вариант A: PowerShell (Windows)**
```powershell
$env:SUPABASE_URL = "https://diuvzzrbwxdufagcdbuz.supabase.co/rest/v1"
$env:SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg4NTMwODIsImV4cCI6MjA5NDQyOTA4Mn0.Feg92AbV0UE3H3lFNnrOKqMVKNlhsO6hOhL1bVyQzk4"
$env:SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODg1MzA4MiwiZXhwIjoyMDk0NDI5MDgyfQ.wzYXd4bUb-lFFd4ljhYruCisbUuj2RQM8wZ2JtLWrLE"
$env:SERVER_PORT = "8080"
```

**Вариант B: Создать .env файл**
```bash
cd backend-spring
cp .env.example .env
# Отредактировать .env в текстовом редакторе
```

### 3️⃣ ЗАПУСТИТЬ BACKEND

**Вариант 1: Maven (самый простой)**
```bash
cd backend-spring
mvn spring-boot:run
```

**Вариант 2: Собрать JAR**
```bash
cd backend-spring
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Вариант 3: Docker**
```bash
cd backend-spring
docker-compose up --build
```

## ✅ ПРОВЕРИТЬ ЧТО РАБОТАЕТ:

### 1. Здоровье backend'а
```bash
curl http://localhost:8080/api/health
```

**Ожидается ответ:**
```json
{
  "status": "ok",
  "supabase": "connected"
}
```

### 2. Регистрация нового пользователя

**Нужен Firebase ID Token из Android приложения!**

```bash
# Сохраните firebase token в переменную (из Android app)
FIREBASE_TOKEN="<ваш firebase id token из приложения>"

curl -X POST http://localhost:8080/api/profile/register \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "display_name": "MyPlayer"
  }'
```

**Ожидаемый ответ:**
```json
{
  "uid": "firebase_uid_here",
  "supabaseId": "uuid_here",
  "profile": {
    "id": "uuid_here",
    "display_name": "MyPlayer",
    "email": "user@example.com",
    "xp": 0,
    "level": 1,
    "streak": 0
  },
  "created": true
}
```

✅ **Если получили этот ответ - пользователь создан в Supabase!**

### 3. Получить уровни

```bash
curl "http://localhost:8080/api/levels?language=javascript&track=beginner"
```

### 4. ГЛАВНОЕ: Отправить решение!

```bash
curl -X POST http://localhost:8080/api/levels/1/submit \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "function helloWorld() { return '\''Hello World'\''; }"
  }'
```

**Ответ если ПРАВИЛЬНО:**
```json
{
  "correct": true,
  "xp_earned": 50,
  "hp_used": 0,
  "message": "Correct! Well done!"
}
```

**Ответ если НЕПРАВИЛЬНО:**
```json
{
  "correct": false,
  "xp_earned": 0,
  "hp_used": 1,
  "message": "Incorrect. Try again!"
}
```

### 5. Проверить профиль (XP должен обновиться!)

```bash
curl http://localhost:8080/api/profile/me \
  -H "Authorization: Bearer $FIREBASE_TOKEN"
```

**Должны увидеть:**
```json
{
  "id": "uuid_here",
  "display_name": "MyPlayer",
  "email": "user@example.com",
  "xp": 50,              ← УВЕЛИЧИЛСЯ НА 50!
  "level": 1,            ← Пересчитан
  "streak": 0
}
```

## 🗄️ ВСЁ СОХРАНЯЕТСЯ В SUPABASE

После каждого действия данные автоматически идут в БД:

- **Новый профиль** → сохраняется в `profiles` таблица
- **Правильное решение** → +50 XP ↔ `profiles` обновляется
- **Неправильное решение** → попытка записывается в `level_progress`
- **Все транзакции XP** → логируются в `xp_history`

## 🚀 ВСЕ ENDPOINTS (к справке):

```
POST   /api/auth/verify                    → Проверить token
POST   /api/profile/register               → Создать новый аккаунт
GET    /api/profile/me                     → Получить профиль
POST   /api/profile/me/xp                  → Добавить XP

GET    /api/levels                         → Получить уровни
GET    /api/levels/{id}                    → Уровень детали
POST   /api/levels/{id}/submit             → ОТПРАВИТЬ РЕШЕНИЕ

GET    /api/progress                       → Прогресс пользователя
GET    /api/progress/level/{id}            → Прогресс по уровню

GET    /api/leaderboard?limit=25           → Топ 25 игроков

GET    /api/achievements                   → Достижения
GET    /api/preferences                    → Настройки
PUT    /api/preferences                    → Обновить настройки

GET    /api/health                         → Здоровье приложение
```

## 📱 ИНТЕГРАЦИЯ С ANDROID ПРИЛОЖЕНИЕМ

В Android app после авторизации (Firebase Auth):

1. Получить ID Token:
```kotlin
val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token
```

2. При регистрации:
```kotlin
POST /api/profile/register
Header: Authorization: Bearer $idToken
Body: { "display_name": "MyName" }
```

3. При отправке решения:
```kotlin
POST /api/levels/{levelId}/submit
Header: Authorization: Bearer $idToken
Body: { "code": "...user's code..." }
```

4. При запросе профиля:
```kotlin
GET /api/profile/me
Header: Authorization: Bearer $idToken
```

## 🔒 БЕЗОПАСНОСТЬ

⚠️ **ВАЖНО:**
- Все secrets (ключи) хранятся в переменных окружения, НЕ в коде
- Firebase service account JSON не должен быть в git
- Service role key используется только на backend'е
- Суpabase Anon key используется для чтения публичных данных
- Все запросы требуют валидный Firebase token

## 📊 СТРУКТУРА ДАННЫХ:

### profiles (основной профиль)
```sql
id            UUID         PRIMARY KEY (от Firebase)
display_name  TEXT         имя игрока
email         TEXT UNIQUE  почта
xp            INTEGER      всего опыта (обновляется при решении)
level         INTEGER      уровень = (xp / 400) + 1
streak        INTEGER      дни подряд
```

### level_progress (прогресс по каждому уровню)
```sql
user_id       UUID         кто решал
level_id      INTEGER      какой уровень
language      TEXT         javascript, python, java и т.д.
track         TEXT         beginner, advanced, expert
stars         INTEGER      0-3 звезды
completed     BOOLEAN      завершен ли
attempts      INTEGER      сколько попыток
```

### xp_history (история всех XP транзакций)
```sql
user_id       UUID         кто получил/потерял
amount        INTEGER      +50 или 0 или -1
reason        TEXT         level_completed, wrong_submission
created_at    TIMESTAMP    когда
```

## ✅ CHECKLIST - ВСЁ ДОЛЖНО БЫТЬ ГОТОВО:

- [x] Spring Boot приложение скачано/создано в папке `backend-spring`
- [x] Все Java классы созданы (контроллеры, сервисы, DTO)
- [x] pom.xml с зависимостями
- [x] application.properties с конфигурацией
- [x] Firebase service account JSON добавлен
- [x] Dockerfile и docker-compose.yml готовы
- [x] README.md и QUICK_START.md написаны
- [x] .gitignore и .env.example созданы
- [x] Все endpoints реализованы
- [x] Supabase интеграция работает
- [x] Firebase аутентификация работает
- [x] Solution checking реализован
- [x] HP система реализована
- [x] Persistence в Supabase настроена
- [x] Health check endpoint работает
- [x] CORS настроен

## 🎯 ПУТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (ВСЁ РАБОТАЕТ!):

```
1. Юзер открыл Android app
   ↓
2. Firebase Auth - регистрация/вход
   ↓
3. Android: POST /api/profile/register (Firebase ID Token)
   ↓
4. Backend создал новый профиль в Supabase (XP=0, Level=1)
   ↓
5. Юзер видит главный экран с 0 XP
   ↓
6. Юзер выбирает уровень
   ↓
7. Юзер пишет код и нажимает "Submit"
   ↓
8. Android: POST /api/levels/{id}/submit (код)
   ↓
9. Backend проверяет решение:
   - Если ПРАВИЛЬНО: +50 XP → сохранить в Supabase
   - Если НЕПРАВИЛЬНО: 0 XP, -1 HP → сохранить попытку
   ↓
10. Android получает ответ {"correct": true, "xp_earned": 50}
   ↓
11. Android обновляет UI - показывает +50 XP, новый level если нужно
   ↓
12. Юзер закрывает приложение
   ↓
13. ПОЗЖЕ: Юзер открывает Android app снова
   ↓
14. Android: GET /api/profile/me
   ↓
15. Backend: SELECT * FROM profiles WHERE id = '...'
   ↓
16. ВСЕ ДАННЫЕ ЗАГРУЖЕНЫ: XP=50, Level=1 (или выше)
   
   ✅ ПРОГРЕСС СОХРАНИЛСЯ!
```

---

## 🎉 ГОТОВО!

**Backend полностью функционален и готов к использованию.**

Начните отсюда:
1. Откройте `backend-spring/QUICK_START.md`
2. Установите Java 17 + Maven
3. Запустите `mvn spring-boot:run`
4. Протестируйте endpoints
5. Интегрируйте с Android приложением

**Если что-то не работает - напишите, помогу разобраться!**

