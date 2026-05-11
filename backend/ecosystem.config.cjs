/**
 * PM2 Ecosystem Configuration
 * 
 * Start: pm2 start ecosystem.config.js
 * Monitor: pm2 monit
 * Logs: pm2 logs bfy-api
 * Stop: pm2 stop bfy-api
 * Restart: pm2 restart bfy-api
 */

module.exports = {
  apps: [
    {
      name: "bfy-api",
      script: "./src/index.ts",
      interpreter: "bun",
      
      // Environment
      env: {
        NODE_ENV: "development",
        PORT: 3000,
      },
      
      // Production environment
      env_production: {
        NODE_ENV: "production",
        PORT: 3000,
      },
      
      // Clustering
      instances: "max",
      exec_mode: "cluster",
      
      // Auto restart
      watch: ["src"],
      ignore_watch: ["node_modules", "dist", "logs"],
      watch_delay: 1000,
      
      // Error handling
      max_memory_restart: "500M",
      error_file: "./logs/error.log",
      out_file: "./logs/out.log",
      log_date_format: "YYYY-MM-DD HH:mm:ss Z",
      
      // Restart strategy
      max_restarts: 10,
      min_uptime: "10s",
      
      // Health check
      kill_timeout: 5000,
      listen_timeout: 3000,
      
      // Graceful shutdown
      shutdown_with_message: true,
    },
  ],

  // Deploy configuration
  deploy: {
    production: {
      user: "deploy",
      host: "your-server.com",
      ref: "origin/main",
      repo: "git@github.com:your-org/bfy.git",
      path: "/var/www/bfy-api",
      "post-deploy": "bun install && bun run build && pm2 reload ecosystem.config.js --env production",
    },
    staging: {
      user: "deploy",
      host: "staging-server.com",
      ref: "origin/develop",
      repo: "git@github.com:your-org/bfy.git",
      path: "/var/www/bfy-api-staging",
      "post-deploy": "bun install && pm2 reload ecosystem.config.js --env production",
    },
  },
};
