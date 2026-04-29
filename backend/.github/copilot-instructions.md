# BFY Backend Development Instructions

## Project Overview

- **Project**: BFY (Better For Yourself) Backend API
- **Framework**: ElysiaJS with Bun runtime
- **Language**: TypeScript
- **Database**: PostgreSQL with TypeORM ORM
- **Architecture**: RESTful API with modular service layer
- **Deadline**: 20/05/2026

## Development Guidelines

### Code Structure
- `/src/config` - Database and environment configuration
- `/src/models` - TypeORM entities (not Mongoose schemas!)
- `/src/services` - Business logic and data operations
- `/src/routes` - API endpoints organized by feature
- `/src/middleware` - Authentication and CORS handling
- `/src/utils` - Helper functions (JWT, password, validation)

### TypeScript Conventions
- Always use strict TypeScript (enabled in tsconfig.json)
- Define interfaces/types for all data models
- Use absolute imports with @ alias paths
- No `any` types unless absolutely necessary
- Use TypeORM decorators for entities: @Entity, @Column, @PrimaryColumn, @ManyToOne, etc.

### API Design
- Use standard HTTP methods (GET, POST, PUT, DELETE, PATCH)
- Return consistent JSON response format
- Always include proper error codes
- Validate input before processing
- Check user authorization on protected endpoints

### Database Operations
- Use TypeORM repositories for all DB operations
- TypeORM automatically creates tables with .synchronize option
- Use UUID as primary keys
- Implement proper relationships (@ManyToOne, @OneToMany, etc.)
- Always validate foreign key constraints

### Error Handling
- Use AppError class for application errors
- Include meaningful error messages and codes
- Return appropriate HTTP status codes
- Log errors to console in development/debug mode

### Authentication & Security
- JWT tokens expire after configured time
- Password minimum: 8 chars, letters + numbers
- Email normalized to lowercase
- Never expose sensitive information in responses
- Hash passwords with bcryptjs (10 salt rounds)

### Testing & Deployment
- Test all endpoints with provided curl examples
- Verify PostgreSQL connections before deployment
- Use environment files for configuration
- Build with: `bun run build`
- Run production with: `bun run prod`

## Quick Commands

```bash
bun install              # Install dependencies
bun run dev             # Start dev server with auto-reload
bun run build           # Compile TypeScript to dist/
bun run prod            # Run production build
bun test                # Run tests (if configured)
```

## Key Files to Know

- `src/index.ts` - Main server entry point
- `src/config/env.ts` - Environment variables
- `src/config/database.ts` - PostgreSQL + TypeORM setup
- `.env.example` - Environment template (copy to .env)
- `package.json` - Dependencies and scripts
- `tsconfig.json` - TypeScript configuration

## Important Notes

- Start PostgreSQL before running the server
- Adjust CORS_ORIGIN in .env for your frontend URL
- JWT_SECRET should be long and random in production
- Database entities use TypeORM decorators (@Entity, @Column, etc.)
- Follow the response format: `{ status, success/error, message, data }`
- TypeORM repositories are accessed via AppDataSource.getRepository(Entity)

---

*Last Updated: April 2026 - PostgreSQL Migration*
