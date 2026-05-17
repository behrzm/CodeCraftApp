-- ==========================================
-- 📋 SUPABASE SQL QUERIES - КОПИПЕЙСТ
-- ==========================================
-- Просто копируйте каждый запрос в Supabase SQL Editor и запустите!

-- ==========================================
-- ШАГ 1: ПРОВЕРКА СТРУКТУРЫ БАЗЫ ДАННЫХ
-- ==========================================

-- Запрос 1.1: Получить список всех таблиц
SELECT
  table_name,
  table_type
FROM
  information_schema.tables
WHERE
  table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY
  table_name;

-- Сохраните результат!
-- Ожидаемо: profiles, xp_history, achievements, level_progress, learning_profiles, levels, languages


-- ==========================================
-- ШАГ 2: СТРУКТУРА КАЖДОЙ ТАБЛИЦЫ
-- ==========================================

-- Запрос 2.1: Структура таблицы PROFILES
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'profiles'
ORDER BY
  ordinal_position;

-- Запрос 2.2: Структура таблицы LEVELS
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'levels'
ORDER BY
  ordinal_position;

-- Запрос 2.3: Структура таблицы XP_HISTORY
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'xp_history'
ORDER BY
  ordinal_position;

-- Запрос 2.4: Структура таблицы ACHIEVEMENTS
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'achievements'
ORDER BY
  ordinal_position;

-- Запрос 2.5: Структура таблицы LEVEL_PROGRESS
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'level_progress'
ORDER BY
  ordinal_position;

-- Запрос 2.6: Структура таблицы LEARNING_PROFILES
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'learning_profiles'
ORDER BY
  ordinal_position;

-- Запрос 2.7: Структура таблицы LANGUAGES (если существует)
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'languages'
ORDER BY
  ordinal_position;

-- Запрос 2.8: Структура таблицы USER_PREFERENCES (если существует)
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default,
  ordinal_position
FROM
  information_schema.columns
WHERE
  table_name = 'user_preferences'
ORDER BY
  ordinal_position;


-- ==========================================
-- ШАГ 3: ДАННЫЕ ДЛЯ BACKEND'А
-- ==========================================

-- Запрос 3.1: Все языки программирования
SELECT
  COALESCE(id, ROW_NUMBER() OVER ()) as id,
  code,
  name,
  COUNT(*) OVER (PARTITION BY code) as total_levels
FROM
  languages
ORDER BY
  code;

-- Если языки хранятся в другой таблице:
SELECT DISTINCT
  language
FROM
  levels
ORDER BY
  language;


-- Запрос 3.2: Все треки
SELECT DISTINCT
  track
FROM
  levels
ORDER BY
  track;


-- Запрос 3.3: Количество уровней по языку и треку
SELECT
  language,
  track,
  COUNT(*) as total_levels,
  MIN(level_number) as min_level,
  MAX(level_number) as max_level
FROM
  levels
GROUP BY
  language, track
ORDER BY
  language, track;


-- Запрос 3.4: ВСЕ УРОВНИ (БОЛЬШОЙ ЭКСПОРТ!)
SELECT
  id,
  language,
  track,
  level_number,
  title,
  description,
  difficulty,
  estimated_time,
  initial_code,
  test_cases,
  hints,
  solution,
  xp_reward,
  created_at
FROM
  levels
ORDER BY
  language, track, level_number;

-- СОХРАНИТЕ ВЕСЬ РЕЗУЛЬТАТ!


-- ==========================================
-- ШАГ 4: ДАННЫЕ ПОЛЬЗОВАТЕЛЕЙ
-- ==========================================

-- Запрос 4.1: Количество профилей
SELECT
  COUNT(*) as total_profiles
FROM
  profiles;


-- Запрос 4.2: Топ 50 пользователей (лидборд)
SELECT
  id,
  display_name,
  email,
  xp,
  level,
  streak,
  created_at
FROM
  profiles
ORDER BY
  xp DESC
LIMIT 50;

-- СКОПИРУЙТЕ СПИСОК!


-- Запрос 4.3: Статистика XP
SELECT
  user_id,
  COUNT(*) as total_xp_entries,
  SUM(amount) as total_xp_gained,
  AVG(amount) as avg_xp_per_entry
FROM
  xp_history
GROUP BY
  user_id
ORDER BY
  total_xp_gained DESC
LIMIT 20;


-- Запрос 4.4: История XP (последние 50 записей)
SELECT
  id,
  user_id,
  amount,
  reason,
  created_at
FROM
  xp_history
ORDER BY
  created_at DESC
LIMIT 50;


-- Запрос 4.5: Статистика достижений
SELECT
  badge_code,
  COUNT(*) as unlocked_by_users
FROM
  achievements
GROUP BY
  badge_code
ORDER BY
  unlocked_by_users DESC;


-- Запрос 4.6: Все достижения пользователя (замените USER_ID)
SELECT
  badge_code,
  unlocked_at
FROM
  achievements
WHERE
  user_id = 'YOUR_USER_ID_HERE'
ORDER BY
  unlocked_at DESC;


-- ==========================================
-- ШАГ 5: ПРОГРЕСС ПОЛЬЗОВАТЕЛЕЙ
-- ==========================================

-- Запрос 5.1: Прогресс по языкам и трекам
SELECT
  user_id,
  language,
  track,
  COUNT(*) as total_levels_attempted,
  SUM(CASE WHEN completed = true THEN 1 ELSE 0 END) as completed_levels,
  AVG(stars) as avg_stars
FROM
  level_progress
GROUP BY
  user_id, language, track
LIMIT 50;


-- Запрос 5.2: Прогресс конкретного пользователя (замените USER_ID)
SELECT
  language,
  track,
  level_id,
  stars,
  completed,
  completed_at,
  attempts
FROM
  level_progress
WHERE
  user_id = 'YOUR_USER_ID_HERE'
ORDER BY
  language, track, level_id;


-- Запрос 5.3: Последние ошибки обучения (LEARNING_PROFILES)
SELECT
  user_id,
  language,
  track,
  level_id,
  attempts,
  last_error,
  updated_at
FROM
  learning_profiles
WHERE
  last_error IS NOT NULL
ORDER BY
  updated_at DESC
LIMIT 30;


-- ==========================================
-- ШАГ 6: АГРЕГИРОВАННАЯ СТАТИСТИКА
-- ==========================================

-- Запрос 6.1: Общая статистика системы
SELECT
  (SELECT COUNT(*) FROM profiles) as total_users,
  (SELECT COUNT(*) FROM levels) as total_levels,
  (SELECT COUNT(DISTINCT badge_code) FROM achievements) as total_badges,
  (SELECT COUNT(*) FROM xp_history) as total_xp_transactions;


-- Запрос 6.2: Самые популярные языки
SELECT
  language,
  COUNT(DISTINCT user_id) as users_learning,
  COUNT(*) as total_level_attempts,
  ROUND(100.0 * SUM(CASE WHEN completed = true THEN 1 ELSE 0 END) / COUNT(*), 2) as completion_rate
FROM
  level_progress
GROUP BY
  language
ORDER BY
  users_learning DESC;


-- Запрос 6.3: Сложность пользователей (средний XP)
SELECT
  CASE
    WHEN level < 5 THEN 'Beginner'
    WHEN level < 10 THEN 'Intermediate'
    WHEN level < 15 THEN 'Advanced'
    ELSE 'Expert'
  END as skill_level,
  COUNT(*) as user_count,
  ROUND(AVG(xp), 0) as avg_xp,
  ROUND(AVG(streak), 1) as avg_streak
FROM
  profiles
GROUP BY
  skill_level
ORDER BY
  user_count DESC;


-- ==========================================
-- ШАГ 7: ПРОВЕРКА RLS POLICIES
-- ==========================================

-- Запрос 7.1: Список всех policies
SELECT
  policyname,
  tablename,
  qual,
  with_check,
  cmd
FROM
  pg_policies
WHERE
  schemaname = 'public'
ORDER BY
  tablename, policyname;

-- Если вывод пустой - RLS НЕ НАСТРОЕНА! Нужно добавить.


-- ==========================================
-- ШАГ 8: ЭКСПОРТ ВСЕХ ДАННЫХ В JSON
-- ==========================================

-- Запрос 8.1: Все профили в JSON
SELECT
  json_agg(
    json_build_object(
      'id', id,
      'display_name', display_name,
      'email', email,
      'xp', xp,
      'level', level,
      'streak', streak,
      'created_at', created_at
    )
  ) as profiles
FROM
  profiles;


-- Запрос 8.2: Все уровни в JSON
SELECT
  json_agg(
    json_build_object(
      'id', id,
      'language', language,
      'track', track,
      'title', title,
      'xp_reward', xp_reward
    )
  ) as levels
FROM
  levels;


-- ==========================================
-- ДОПОЛНИТЕЛЬНЫЕ ЗАПРОСЫ
-- ==========================================

-- Запрос Д1: Проверить наличие пользователя
SELECT * FROM profiles WHERE id = 'YOUR_UUID_HERE';

-- Запрос Д2: Проверить уровень
SELECT * FROM levels WHERE id = 1;

-- Запрос Д3: Проверить связи
SELECT
  COUNT(DISTINCT p.id) as profiles,
  COUNT(DISTINCT l.id) as level_rows,
  COUNT(DISTINCT x.user_id) as xp_users,
  COUNT(DISTINCT a.user_id) as achievement_users
FROM
  profiles p
  LEFT JOIN level_progress l ON p.id = l.user_id
  LEFT JOIN xp_history x ON p.id = x.user_id
  LEFT JOIN achievements a ON p.id = a.user_id;

-- Если count'ы совпадают - данные целостны!

