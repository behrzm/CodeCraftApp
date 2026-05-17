# CodeCraft Backend (Spring Boot)

A production-ready Spring Boot backend for CodeCraft learning platform. Integrates with **Supabase** for data storage and **Firebase** for authentication.

## Features

✅ Firebase JWT token verification  
✅ Complete user profiles management (XP, Level, Streak)  
✅ Level/Mission system with progress tracking  
✅ Solution submission with auto-grading (±50 XP)  
✅ HP system (max 3 per level attempt)  
✅ Achievements/Badges system  
✅ User preferences (theme, sound, notifications)  
✅ Leaderboard rankings  
✅ Health checks & monitoring  

## Tech Stack

- **Java 17**
- **Spring Boot 3.1.6**
- **Supabase REST API** (PostgreSQL)
- **Firebase Admin SDK** (Authentication)
- **WebClient** (async HTTP client)
- **Maven** (build tool)

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Supabase project with tables (see `BACKEND_SETUP_GUIDE.md`)
- Firebase project with service account JSON

### Local Setup

1. Clone & navigate:
```bash
cd backend-spring
```

2. Copy & configure environment:
```bash
cp .env.example .env
# Edit .env with your credentials
```

3. Run with Maven:
```bash
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

### Docker Setup

Build & run:
```bash
docker-compose up --build
```

Or build image:
```bash
docker build -t codecraft-backend .
docker run -p 8080:8080 \
  -e SUPABASE_URL=... \
  -e SUPABASE_APIKEY=... \
  -e SUPABASE_SERVICE_ROLE_KEY=... \
  codecraft-backend
```

## API Endpoints

### Authentication
- `POST /api/auth/verify` - Verify Firebase token
- `POST /api/profile/register` - Register new user

### Profile
- `GET /api/profile/me` - Get current user profile
- `POST /api/profile/me/xp` - Add XP to user

### Levels & Progress
- `GET /api/levels?language=javascript&track=beginner` - Get levels
- `GET /api/levels/{id}` - Get level details
- `POST /api/levels/{id}/submit` - Submit solution (auto-graded)
- `GET /api/progress` - Get user's progress
- `GET /api/progress/level/{id}` - Get progress for specific level

### Leaderboard
- `GET /api/leaderboard?limit=25` - Get top users

### Achievements
- `GET /api/achievements` - Get user's badges

### Preferences
- `GET /api/preferences` - Get user settings
- `PUT /api/preferences` - Update settings

### Health
- `GET /api/health` - Health check

## Example API Calls

### Verify Token & Create Profile
```bash
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Authorization: Bearer <FIREBASE_ID_TOKEN>"
```

Response:
```json
{
  "uid": "firebase_uid_here",
  "supabaseId": "uuid_here",
  "profile": {
    "id": "uuid_here",
    "display_name": "CodeMaster",
    "email": "user@example.com",
    "xp": 0,
    "level": 1,
    "streak": 0
  }
}
```

### Get Levels
```bash
curl "http://localhost:8080/api/levels?language=javascript&track=beginner"
```

### Submit Solution
```bash
curl -X POST http://localhost:8080/api/levels/1/submit \
  -H "Authorization: Bearer <FIREBASE_ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"code": "function helloWorld() { return '\''Hello World'\''; }"}'
```

Response (if correct):
```json
{
  "correct": true,
  "xp_earned": 50,
  "hp_used": 0,
  "message": "Correct! Well done!"
}
```

### Add XP
```bash
curl -X POST http://localhost:8080/api/profile/me/xp \
  -H "Authorization: Bearer <FIREBASE_ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100, "reason": "mission_completed"}'
```

### Get/Update Preferences
```bash
# Get
curl http://localhost:8080/api/preferences \
  -H "Authorization: Bearer <FIREBASE_ID_TOKEN>"

# Update
curl -X PUT http://localhost:8080/api/preferences \
  -H "Authorization: Bearer <FIREBASE_ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"dark_theme": false, "sound_enabled": true, "notifications_enabled": true}'
```

## Configuration

Set environment variables or edit `src/main/resources/application.properties`:

```properties
# Supabase
SUPABASE_URL=https://xxx.supabase.co/rest/v1
SUPABASE_APIKEY=eyJhbG...  # anon key
SUPABASE_SERVICE_ROLE_KEY=eyJhbG...  # service role key

# Server
SERVER_PORT=8080

# CORS
CORS_ORIGINS=http://localhost:3000,https://yourapp.com
```

## Data Models

### Profile
- `id` (UUID) - Converted from Firebase UID
- `display_name` (Text)
- `email` (Email)
- `xp` (Integer)
- `level` (Integer) - Level = (XP / 400) + 1
- `streak` (Integer)

### Level Progress
- `user_id` (UUID)
- `level_id` (Integer)
- `language` (Text)
- `track` (Text)
- `stars` (Integer, 0-3)
- `completed` (Boolean)
- `attempts` (Integer)

### XP History
- `user_id` (UUID)
- `amount` (Integer)
- `reason` (Text) - "level_completed", "wrong_submission", etc.
- `created_at` (Timestamp)

## Solution Checking

Solutions are validated using:
1. **Exact match** - Normalized code comparison
2. **Pattern match** - Basic substring matching of key components
3. **Test cases** - Can be extended to run actual test suites

Current implementation accepts normalized code that closely matches the expected solution. For production, integrate actual code execution sandboxes.

## HP System

- Each wrong submission costs 1 HP
- Max 3 HP per level attempt
- HP is tracked implicitly via submission history
- Can be enhanced with explicit HP tracking table

## Security Notes

⚠️ **Important:**
- Firebase service account JSON should be in environment, NOT in repo
- All secrets should use environment variables
- Supabase service role key should only be used server-side
- Implement rate limiting for production
- Add request validation & sanitization
- Consider adding input validation for code submissions

## Production Deployment

### Render.com / Fly.io / Railway

1. Set environment secrets in platform settings
2. Point to this repository
3. Platform will auto-detect Dockerfile and build
4. Scale as needed

### AWS / GCP / Azure

1. Build Docker image
2. Push to container registry
3. Deploy to Kubernetes or managed services
4. Set secrets in deployment config

## Troubleshooting

### "Firebase service account file not found"
- Ensure `firebase-service-account.json` is in `src/main/resources/`
- Add file to `.gitignore` before committing

### "Supabase connection refused"
- Check `SUPABASE_URL`, `SUPABASE_APIKEY`, `SUPABASE_SERVICE_ROLE_KEY`
- Verify Supabase project is active
- Test with `GET /api/health`

### "Invalid token" errors
- Ensure Firebase ID token is current (tokens expire after 1 hour)
- Check Firebase project configuration
- Verify token format: `Authorization: Bearer <token>`

## Development

### Build
```bash
mvn clean package
```

### Run tests
```bash
mvn test
```

### Generate docs
```bash
mvn javadoc:javadoc
```

## Contributing

1. Create feature branch
2. Make changes
3. Test locally: `mvn spring-boot:run`
4. Commit & push
5. Submit PR

## License

MIT
