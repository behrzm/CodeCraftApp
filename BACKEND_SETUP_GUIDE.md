# 🚀 CodeCraft Backend Setup - Полное руководство по сбору данных

## ЭТАП 1️⃣: Получение Supabase данных

### Шаг 1.1: Заходим в Supabase Dashboard
1. Откройте https://app.supabase.com
2. Выберите ваш проект `diuvzzrbwxdufagcdbuz`

### Шаг 1.2: Получаем Supabase Project Credentials
1. **Settings** → **Project Settings** → **API**
   - Скопируйте **Project URL**: `https://diuvzzrbwxdufagcdbuz.supabase.co`
   - Скопируйте **Anon Key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (видно в коде)
   - Скопируйте **Service Role Key** (приватный key для backend): нужно получить

2. **Settings** → **Database** → Get Connection String
   - Нужен PostgreSQL Connection String

### Шаг 1.3: Экспортируем структуру базы данных
В Supabase Console перейдите в **SQL Editor** и запустите:
```sql
-- Получить список всех таблиц
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';

-- Для каждой таблицы получить структуру:
-- SELECT * FROM information_schema.columns 
-- WHERE table_name = 'profiles';
```

**Документируйте все таблицы и их поля:**

✅ **Ожидаемые таблицы:**
- `profiles` - Пользовательские профили
- `xp_history` - История получения XP
- `achievements` - Достижения/Бейджи
- `level_progress` - Прогресс по уровням
- `learning_profiles` - Профили обучения

---

## ЭТАП 2️⃣: Получение Firebase данных

### Шаг 2.1: Firebase Project Credentials
1. Откройте https://console.firebase.google.com
2. Выберите ваш проект
3. **Settings** → **Project Settings** → **Service Accounts**
   - Нажмите "Generate new private key"
   - Сохраните JSON файл как `firebase-service-account.json`

4. **Settings** → **General**
   - Скопируйте **Web API Key**

### Шаг 2.2: Firebase Authentication настройка
1. **Authentication** → **Sign-in method**
   - Проверьте, какие методы включены
   - Обычно: Email/Password, Google, GitHub

---

## ЭТАП 3️⃣: Структура данных, которые ДОЛЖНЫ быть в Supabase

### Таблица 1: `profiles`
```sql
CREATE TABLE profiles (
  id UUID PRIMARY KEY,           -- Firebase UID преобразованный в UUID
  display_name TEXT,
  email TEXT UNIQUE,
  xp INTEGER DEFAULT 0,
  level INTEGER DEFAULT 1,
  streak INTEGER DEFAULT 0,
  avatar_url TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

### Таблица 2: `xp_history`
```sql
CREATE TABLE xp_history (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  amount INTEGER NOT NULL,
  reason TEXT,                 -- Например: "level_completed", "achievement_unlocked"
  created_at TIMESTAMP DEFAULT NOW()
);
```

### Таблица 3: `achievements` (Бейджи)
```sql
CREATE TABLE achievements (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  badge_code TEXT NOT NULL,     -- Код бейджа: "first_level", "10_levels", итд
  unlocked_at TIMESTAMP DEFAULT NOW()
);
```

### Таблица 4: `level_progress`
```sql
CREATE TABLE level_progress (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  language TEXT NOT NULL,       -- "javascript", "python", "java", итд
  track TEXT NOT NULL,          -- "beginner", "advanced", "expert"
  level_id INTEGER NOT NULL,
  stars INTEGER DEFAULT 0,      -- 0-3 звезды за уровень
  completed BOOLEAN DEFAULT FALSE,
  completed_at TIMESTAMP,
  attempts INTEGER DEFAULT 0
);
```

### Таблица 5: `learning_profiles`
```sql
CREATE TABLE learning_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  language TEXT NOT NULL,
  track TEXT NOT NULL,
  level_id INTEGER NOT NULL,
  attempts INTEGER DEFAULT 0,
  failed_commands TEXT,         -- JSON array строк команд
  last_error TEXT,
  updated_at TIMESTAMP DEFAULT NOW()
);
```

### Таблица 6: `levels` (Метаданные уровней)
```sql
CREATE TABLE levels (
  id SERIAL PRIMARY KEY,
  language TEXT NOT NULL,       -- javascript, python, java, bash, sql
  track TEXT NOT NULL,          -- beginner, advanced, expert
  level_number INTEGER NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  difficulty INTEGER,           -- 1-10
  estimated_time INTEGER,       -- в минутах
  initial_code TEXT,            -- стартовый код
  test_cases TEXT,              -- JSON array тестов
  hints TEXT,                   -- JSON array подсказок
  solution TEXT,                -- эталонное решение
  xp_reward INTEGER,            -- XP за прохождение
  created_at TIMESTAMP DEFAULT NOW()
);
```

### Таблица 7: `languages` (Языки программирования)
```sql
CREATE TABLE languages (
  id SERIAL PRIMARY KEY,
  code TEXT UNIQUE NOT NULL,    -- javascript, python, java, bash, sql
  name TEXT NOT NULL,
  icon_url TEXT,
  description TEXT,
  enabled BOOLEAN DEFAULT TRUE
);
```

### Таблица 8: `user_preferences`
```sql
CREATE TABLE user_preferences (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  dark_theme BOOLEAN DEFAULT TRUE,
  sound_enabled BOOLEAN DEFAULT TRUE,
  notifications_enabled BOOLEAN DEFAULT TRUE,
  updated_at TIMESTAMP DEFAULT NOW()
);
```

---

## ЭТАП 4️⃣: Получение текущего состояния в Supabase

### Шаг 4.1: Проверить существующие таблицы
В Supabase Console → **SQL Editor** запустить:
```sql
-- Список всех таблиц
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

-- Если нужны детали структуры каждой таблицы
\d profiles
\d xp_history
\d achievements
-- и т.д.
```

### Шаг 4.2: Проверить текущие данные
```sql
-- Сколько пользователей
SELECT COUNT(*) FROM profiles;

-- Сколько записей в xp_history
SELECT COUNT(*) FROM xp_history;

-- Какие языки есть
SELECT DISTINCT language FROM levels;

-- Какие треки есть
SELECT DISTINCT track FROM levels;
```

### Шаг 4.3: Экспортировать существующие данные
```sql
-- Экспортировать всех пользователей
SELECT * FROM profiles;

-- Экспортировать всех уровней
SELECT * FROM levels;

-- Экспортировать все языки
SELECT * FROM languages;
```

---

## ЭТАП 5️⃣: Проверка RLS (Row Level Security) политик

В Supabase Console → **Authentication** → **Policies**
Убедиться, что настроены правильно для каждой таблицы:

```sql
-- PROFILES - пользователь может читать свой профиль и всех (для лидбордов)
CREATE POLICY "Users can read own profile"
  ON profiles FOR SELECT
  USING (auth.uid()::text = id::text OR true);  -- Все могут читать для лидборда

-- PROFILES - пользователь может обновлять только свой профиль
CREATE POLICY "Users can update own profile"
  ON profiles FOR UPDATE
  USING (auth.uid()::text = id::text);

-- XP_HISTORY - пользователь может читать свою историю
CREATE POLICY "Users can read own xp history"
  ON xp_history FOR SELECT
  USING (auth.uid()::text = user_id::text);
```

---

## ЭТАП 6️⃣: Данные для backend конфигурации

Создайте файл `backend-credentials.md` и заполните:

```
=== SUPABASE CREDENTIALS ===
Project URL: https://diuvzzrbwxdufagcdbuz.supabase.co
Anon Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Service Role Key: (ПОЛУЧИТЬ ИЗ SUPABASE)
JWT Secret: (Settings → Project Settings → API)

=== FIREBASE CREDENTIALS ===
Project ID: codequestdiplomaproject
Web API Key: (получить из Firebase)
Service Account Private Key: (из JSON файла)

=== BACKEND URLS ===
Frontend URL: http://localhost:3000 (или боевой)
Backend URL: http://localhost:8080 (или боевой)

=== CORS WHITELIST ===
- https://codequestdiplomaproject.firebaseapp.com
- https://codequestdiplomaproject.web.app
- http://localhost:3000 (для разработки)
```

---

## ЭТАП 7️⃣: Структура API endpoints для backend'а

### Authentication
- `POST /api/auth/register` - регистрация (Firebase handle на фронте)
- `POST /api/auth/login` - авторизация (Firebase handle на фронте)
- `GET /api/auth/me` - текущий пользователь

### Profile
- `GET /api/profile/:userId` - получить профиль
- `PUT /api/profile/:userId` - обновить профиль
- `GET /api/profile/:userId/stats` - статистика пользователя

### Leaderboard
- `GET /api/leaderboard?limit=25&offset=0` - получить лидборд
- `GET /api/leaderboard/:userId/rank` - ранг пользователя

### Levels & Progress
- `GET /api/levels?language=javascript&track=beginner` - все уровни
- `GET /api/levels/:levelId` - деталь уровня
- `GET /api/progress/:userId?language=javascript` - прогресс пользователя
- `POST /api/progress/:userId/level/:levelId/submit` - отправить решение

### Achievements
- `GET /api/achievements` - все доступные бейджи
- `GET /api/achievements/:userId` - бейджи пользователя
- `POST /api/achievements/:userId/unlock` - разблокировать бейдж

### XP System
- `POST /api/xp/add` - добавить XP (внутренний, только для backend)
- `GET /api/xp-history/:userId` - история XP

### Preferences
- `GET /api/preferences/:userId` - получить предпочтения
- `PUT /api/preferences/:userId` - обновить предпочтения

---

## ЭТАП 8️⃣: Данные для примеров

### Пример профиля пользователя:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "display_name": "CodeMaster",
  "email": "user@example.com",
  "xp": 1240,
  "level": 5,
  "streak": 7,
  "avatar_url": "https://...",
  "created_at": "2025-01-15T10:30:00Z"
}
```

### Пример языков программирования:
```json
[
  {
    "code": "javascript",
    "name": "JavaScript",
    "icon_url": "https://...",
    "description": "Начните с веба"
  },
  {
    "code": "python",
    "name": "Python",
    "icon_url": "https://...",
    "description": "Универсальный язык"
  },
  {
    "code": "java",
    "name": "Java",
    "icon_url": "https://..."
  },
  {
    "code": "bash",
    "name": "Bash",
    "icon_url": "https://..."
  },
  {
    "code": "sql",
    "name": "SQL",
    "icon_url": "https://..."
  }
]
```

### Пример уровня:
```json
{
  "id": 1,
  "language": "javascript",
  "track": "beginner",
  "level_number": 1,
  "title": "Hello World",
  "description": "Напишите первую программу",
  "difficulty": 1,
  "estimated_time": 5,
  "initial_code": "function helloWorld() {\n  // Ваш код здесь\n}",
  "test_cases": [
    {
      "input": "",
      "expected_output": "Hello World",
      "description": "Функция должна вернуть Hello World"
    }
  ],
  "hints": [
    "Используйте console.log()",
    "Или используйте return"
  ],
  "solution": "function helloWorld() {\n  return 'Hello World';\n}",
  "xp_reward": 50
}
```

---

## ИТОГОВЫЙ ЧЕКЛИСТ 📋

- [ ] Получил URL Supabase проекта
- [ ] Получил Anon Key из Supabase
- [ ] Получил Service Role Key из Supabase
- [ ] Получил JWT Secret
- [ ] Скачал Firebase Service Account JSON
- [ ] Получил Web API Key Firebase
- [ ] Проверил существующие таблицы в Supabase
- [ ] Экспортировал структуру всех таблиц
- [ ] Экспортировал текущие данные (профили, уровни)
- [ ] Проверил RLS политики
- [ ] Задокументировал все языки программирования
- [ ] Задокументировал все треки (beginner, advanced, expert)
- [ ] Определился с API endpoints

---

## Когда собрали ВСЕ данные:

**Отправьте мне в чат:**
1. Supabase Project URL + Keys
2. Firebase Service Account JSON
3. Список таблиц (результаты SQL запросов)
4. Текущие данные (профили, уровни, языки)
5. Информацию о языках программирования и треках

**Тогда я создам полный Spring Boot backend! 🚀**

