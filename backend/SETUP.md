# BFY Backend Setup Guide - Step by Step

## ⚠️ Important: Install Bun First

The BFY backend uses **Bun** as the runtime, which is much faster than Node.js. You **MUST** install Bun before proceeding.

### 1. Install Bun

**On Windows (PowerShell):**
```powershell
# Run this command in PowerShell as Administrator
$Response = Invoke-Expression -Command "powershell -Command 'Invoke-RestMethod -Uri https://bun.sh/install.ps1 | Invoke-Expression'"
```

Or download the Windows installer:
```powershell
curl https://bun.sh/install.ps1 -o install.ps1 | .\install.ps1
```

**On macOS/Linux:**
```bash
curl -fsSL https://bun.sh/install | bash
```

**Verify Installation:**
```bash
bun --version
```

You should see a version number like `1.0.0+` or higher.

### 2. Navigate to Project

```bash
cd d:\laptrinhdidong_DACS3\backend
```

### 3. Install Dependencies

```bash
bun install
```

This will install:
- ElysiaJS - Web framework
- TypeORM - SQL ORM for PostgreSQL
- pg - PostgreSQL driver
- JWT libraries - Authentication
- TypeScript - Type safety
- bcryptjs - Password hashing
- And more...

### 4. Setup Environment Variables

```bash
# Copy example to .env
cp .env.example .env
```

Edit `.env` file and set PostgreSQL connection:
```env
DATABASE_TYPE=postgres
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=bfy
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_SSL=false
JWT_SECRET=super_secret_key_change_this_in_production
CORS_ORIGIN=http://localhost:3001
```

### 5. Start PostgreSQL

**Option A: Local PostgreSQL Installation**

First, [download PostgreSQL](https://www.postgresql.org/download/)

Then start the service:
- **Windows**: Use pgAdmin or command line
- **macOS**: `brew services start postgresql`
- **Linux**: `sudo systemctl start postgresql`

Verify:
```bash
psql -U postgres -c "SELECT version();"
```

**Option B: PostgreSQL with Docker**
```bash
docker run -d -p 5432:5432 --name postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16
```

**Option C: PostgreSQL in Cloud (Supabase - Recommended)**
1. Go to [supabase.com](https://supabase.com)
2. Create free account
3. Create new project
4. Copy PostgreSQL connection string
5. Add to `.env`:
   ```env
   DATABASE_HOST=your-project.supabase.co
   DATABASE_PORT=5432
   DATABASE_NAME=postgres
   DATABASE_USER=postgres
   DATABASE_PASSWORD=your_password
   DATABASE_SSL=true
   ```

### 6. Run Development Server

```bash
bun run dev
```

You should see:
```
✅ Server running at http://localhost:3000
✅ PostgreSQL connected successfully
🗄️  Database: PostgreSQL
```

## Available Commands

```bash
# Development - Hot reload
bun run dev

# Build for production
bun run build

# Run production build
bun run prod

# Run tests
bun test
```

## ✅ Verification Checklist

- [ ] Bun is installed (`bun --version` works)
- [ ] Node modules installed (`bun install` completed)
- [ ] `.env` file created and configured
- [ ] PostgreSQL is running (local or Atlas connected)
- [ ] Development server starts (`bun run dev` works)
- [ ] Health endpoint responds (`curl http://localhost:3000/health`)
- [ ] Can register user
- [ ] Can login and receive JWT token

## 🆘 Troubleshooting

### "bun: command not found"
- Bun not installed or not in PATH
- Try: `curl -fsSL https://bun.sh/install | bash`
- Add to PATH if needed

### "Cannot connect to PostgreSQL"
- Make sure PostgreSQL is running
- Check DATABASE_HOST, DATABASE_PORT, DATABASE_NAME in .env
- For Supabase: ensure DATABASE_SSL=true

### "Port 5432 already in use"
- Change DATABASE_PORT in .env to different number (5433, 5434, etc)
- Or kill existing PostgreSQL process

### "Type error in TypeScript"
- Run: `bun run build` to see all issues
- Check tsconfig.json paths are correct

## 📚 Next Steps

1. Review [README.md](./README.md) for API documentation
2. Check [src/models](./src/models) for database entities
3. Explore [src/services](./src/services) for business logic
4. Test all endpoints in [API_EXAMPLES.md](./API_EXAMPLES.md)

## 🚀 Production Deployment

Before deploying:

1. **Set environment to production:**
   ```env
   NODE_ENV=production
   DATABASE_SSL=true
   JWT_SECRET=<long_random_string>
   DATABASE_HOST=<production_postgres_host>
   CORS_ORIGIN=<your_frontend_domain>
   ```

2. **Build the project:**
   ```bash
   bun run build
   ```

3. **Run production server:**
   ```bash
   bun run prod
   ```

4. **Use PM2 or Docker** for process management:
   ```bash
   npm install -g pm2
   pm2 start dist/index.js --name "bfy-api"
   ```

---

**Questions?** Check documentation or contact the team.  
**Deadline:** 20/05/2026
