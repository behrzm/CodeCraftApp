# 📊 Пошаговая инструкция - Как получить ВСЕ данные

## 🔴 ПЕРВЫЙ ШАГ: Supabase Credentials

### Шаг 1: Откройте Supabase Console
```
URL: https://app.supabase.com
Логин: используйте свой аккаунт
```

### Шаг 2: Перейдите в Project Settings
```
Left Sidebar → Settings (иконка шестеренки внизу) → Project Settings
```

### Шаг 3: Скопируйте API Keys
Вкладка **API** - вы увидите:
- **Project URL**: начинается с `https://diuvzzrbwxdufagcdbuz.supabase.co` ✅
- **Anon (public) Key**: длинная строка с `eyJhbGc...` ✅  
- **Service Role Key**: НУЖНО СКОПИРОВАТЬ (приватная) ⭐

**СОХРАНИТЕ ВСЕ ТРИ!**

```
SUPABASE_URL=https://diuvzzrbwxdufagcdbuz.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=ВАШ_SERVICE_ROLE_KEY_ЗДЕСЬ
```

### Шаг 4: Получите JWT Secret
```
Settings → Project Settings → Configuration → API
Найти "JWT Secret" - это нужно для подписи токенов на backend'е
```

---

## 🔵 ВТОРОЙ ШАГ: Firebase Credentials

### Шаг 1: Откройте Firebase Console
```
URL: https://console.firebase.google.com
Project: codequestdiplomaproject
```

### Шаг 2: Получите Service Account JSON
```
Settings (иконка шестеренки вверху справа) → Project Settings
Вкладка: "Service Accounts"
Нажмите: "Generate New Private Key"
```

**ЭТО ВАЖНО! Сохраните JSON файл как `firebase-service-account.json`**

В JSON будут поля:
```json
{
  "type": "service_account",
  "project_id": "codequestdiplomaproject",
  "private_key_id": "...",
  "private_key": "-----BEGIN RSA PRIVATE KEY-----...",
  "client_email": "firebase-adminsdk-...@...iam.gserviceaccount.com",
  "client_id": "...",
  ...
}
```

### Шаг 3: Получите Web API Key
```
Settings → Project Settings → General
Найти раздел "Web API Key" или в Apps section
Скопируйте значение после "apiKey": "..."
```

---

## 🟡 ТРЕТИЙ ШАГ: Проверка Supabase Tables

### Откройте Supabase SQL Editor
```
Left Sidebar → SQL Editor
Нажмите "+ New Query"
```

### Запрос 1: Список всех таблиц
```sql
SELECT 
  table_name 
FROM 
  information_schema.tables 
WHERE 
  table_schema = 'public' 
  AND table_type = 'BASE TABLE'
ORDER BY 
  table_name;
```

**СКОПИРУЙТЕ РЕЗУЛЬТАТ - это критично!**

### Для каждой таблицы получите структуру:

#### Таблица: profiles
```sql
-- Структура таблицы
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'profiles'
ORDER BY ordinal_position;

-- Примеры данных
SELECT * FROM profiles LIMIT 5;
```

**СОХРАНИТЕ:**
- Все названия колонок
- Все типы данных (UUID, TEXT, INTEGER, BOOLEAN, TIMESTAMP, итд)
- Какие поля NOT NULL
- Первые 5 примеров данных

#### Таблица: levels (если существует)
```sql
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'levels'
ORDER BY ordinal_position;

SELECT * FROM levels LIMIT 10;
```

#### Таблица: xp_history
```sql
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'xp_history'
ORDER BY ordinal_position;

SELECT * FROM xp_history LIMIT 10;
```

#### Таблица: achievements
```sql
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'achievements'
ORDER BY ordinal_position;

SELECT * FROM achievements LIMIT 10;
```

#### Таблица: level_progress
```sql
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'level_progress'
ORDER BY ordinal_position;

SELECT * FROM level_progress LIMIT 10;
```

#### Таблица: learning_profiles
```sql
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'learning_profiles'
ORDER BY ordinal_position;

SELECT * FROM learning_profiles LIMIT 10;
```

---

## 🟢 ЧЕТВЁРТЫЙ ШАГ: Экспортируйте текущие данные

### Получите все языки программирования
```sql
-- Если таблица существует
SELECT * FROM languages;

-- Если нет, найти в другой таблице
SELECT DISTINCT language FROM levels;
```

**НУЖНО СОБРАТЬ:**
- Список всех языков (javascript, python, java, bash, sql, итд)
- Иконки для каждого
- Описания

### Получите все треки
```sql
SELECT DISTINCT track FROM levels;
```

**Ожидается: beginner, advanced, expert (или другие)**

### Все уровни по языкам
```sql
SELECT 
  language, 
  track, 
  COUNT(*) as total_levels 
FROM 
  levels 
GROUP BY 
  language, 
  track 
ORDER BY 
  language, track;
```

### Полный экспорт уровней
```sql
SELECT * FROM levels ORDER BY language, track, level_number;
```

**СКОПИРУЙТЕ ВЕСЬ РЕЗУЛЬТАТ!**

---

## 🟣 ПЯТЫЙ ШАГ: Проверка текущих пользователей и данных

### Сколько пользователей
```sql
SELECT COUNT(*) as total_users FROM profiles;
```

### Топ 10 пользователей
```sql
SELECT id, display_name, xp, level, streak 
FROM profiles 
ORDER BY xp DESC 
LIMIT 10;
```

### История XP
```sql
SELECT COUNT(*) as total_xp_entries FROM xp_history;
SELECT * FROM xp_history ORDER BY created_at DESC LIMIT 20;
```

### Достижения
```sql
SELECT COUNT(*) as total_achievements FROM achievements;
SELECT DISTINCT badge_code FROM achievements;
```

---

## 🟠 ШЕСТОЙ ШАГ: Проверка RLS Policies

### Откройте Authentication → Policies
```
Left Sidebar → Authentication → Policies
```

**Должны быть SET UP для:**
- ✅ profiles (читать все, обновлять свой)
- ✅ xp_history (читать свой)
- ✅ achievements (читать свой)
- ✅ level_progress (читать свой, обновлять свой)
- ✅ learning_profiles (читать свой)
- ✅ levels (все могут читать)

**Если политик нет - НУЖНО ДОБАВИТЬ!**

---

## 📝 ИТОГОВЫЙ ДОКУМЕНТ: Что вам нужно отправить

### Создайте файл `backend_data.json` и заполните:

```json
{
  "supabase": {
    "project_url": "https://diuvzzrbwxdufagcdbuz.supabase.co",
    "anon_key": "eyJhbGc...",
    "service_role_key": "HERE_YOUR_KEY",
    "jwt_secret": "HERE_YOUR_JWT_SECRET"
  },
  "firebase": {
    "project_id": "codequestdiplomaproject",
    "web_api_key": "HERE_YOUR_API_KEY",
    "note": "Service Account JSON прикрепить отдельно как файл"
  },
  "tables": {
    "profiles": [
      {
        "column": "id",
        "type": "uuid",
        "nullable": false,
        "example": "550e8400-e29b-41d4-a716-446655440000"
      },
      {
        "column": "display_name",
        "type": "text",
        "nullable": true,
        "example": "CodeMaster"
      }
    ],
    "levels": [
      {
        "column": "id",
        "type": "integer",
        "nullable": false
      }
    ]
  },
  "programming_languages": [
    {
      "code": "javascript",
      "name": "JavaScript",
      "total_levels": 20
    },
    {
      "code": "python",
      "name": "Python",
      "total_levels": 20
    }
  ],
  "tracks": ["beginner", "advanced", "expert"],
  "examples": {
    "top_users": [
      {
        "display_name": "User1",
        "xp": 5000,
        "level": 10
      }
    ],
    "sample_level": {
      "language": "javascript",
      "track": "beginner",
      "level_number": 1,
      "title": "Hello World"
    }
  }
}
```

---

## ✅ ФИНАЛЬНЫЙ ЧЕКЛИСТ

- [ ] Скопировал Supabase Project URL
- [ ] Скопировал Supabase Anon Key
- [ ] Скопировал Supabase Service Role Key
- [ ] Скопировал Суpabase JWT Secret
- [ ] Скачал Firebase Service Account JSON
- [ ] Скопировал Firebase Web API Key
- [ ] Запустил все SQL запросы и сохранил результаты
- [ ] Получил список ВСЕХ таблиц и их структуры
- [ ] Получил примеры данных из каждой таблицы
- [ ] Задокументировал все языки программирования
- [ ] Задокументировал все треки
- [ ] Получил информацию о всех уровнях
- [ ] Создал файл backend_data.json с информацией
- [ ] ГОТОВ ОТПРАВИТЬ ВСЕ ДАННЫЕ

---

## 🎯 Когда ВСЁ собрал - отправь мне:

1. **backend_data.json** файл
2. **firebase-service-account.json** файл
3. **Screenshot RLS policies** (если кому-то нужно будет дебажить)

**И я создам полный Spring Boot backend за раз!** 🚀

