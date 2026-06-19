import { config } from "@config/env";
import { successResponse, errorResponse, AppError } from "@utils/errors";
import { AuthContext } from "@middleware/auth";
import calendarSyncService from "@services/CalendarSyncService";
import { google } from "googleapis";

// Initialize Google OAuth2 Client
const oauth2Client = new google.auth.OAuth2(
  config.google.clientId,
  config.google.clientSecret,
  config.google.redirectUri
);

// Define requested scopes
const SCOPES = [
  "https://www.googleapis.com/auth/calendar",
  "https://www.googleapis.com/auth/calendar.events"
];

export class CalendarController {
  /**
   * Get Google OAuth Consent URL
   * GET /api/calendar/auth
   */
  async getAuthUrl(ctx: AuthContext): Promise<Response> {
    if (!ctx.user) {
      return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    }

    try {
      const authUrl = oauth2Client.generateAuthUrl({
        access_type: "offline",
        scope: SCOPES,
        prompt: "consent", // Force to get refresh token
        state: ctx.user.userId, // Pass userId in state to identify them on callback
      });

      return successResponse({ url: authUrl }, "Auth URL generated");
    } catch (error) {
      return errorResponse(500, "Failed to generate auth URL");
    }
  }

  /**
   * Handle Google OAuth Callback
   * GET /api/calendar/callback?code=...&state=...
   */
  async handleCallback(query: any): Promise<Response> {
    const { code, state: userId } = query;

    if (!code || !userId) {
      return new Response(
        "Missing code or user state. Please try again.",
        { status: 400, headers: { "Content-Type": "text/html" } }
      );
    }

    try {
      // Exchange code for tokens
      const { tokens } = await oauth2Client.getToken(code);
      
      // Save tokens to database
      await calendarSyncService.saveGoogleTokens(userId, tokens);

      // Return a success HTML page that closes itself or redirects back to app
      const html = `
        <html>
          <head><title>Success</title></head>
          <body>
            <h2>Google Calendar connected successfully!</h2>
            <p>You can close this window and return to the app.</p>
            <script>
              setTimeout(() => {
                window.close();
              }, 3000);
            </script>
          </body>
        </html>
      `;

      return new Response(html, {
        headers: { "Content-Type": "text/html" },
      });
    } catch (error) {
      console.error("OAuth callback error", error);
      return new Response(
        "Authentication failed. Please try again.",
        { status: 500, headers: { "Content-Type": "text/html" } }
      );
    }
  }

  /**
   * Trigger bi-directional sync with Google Calendar
   * POST /api/calendar/sync
   */
  async sync(ctx: AuthContext): Promise<Response> {
    if (!ctx.user) {
      return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    }

    try {
      const result = await calendarSyncService.syncWithGoogleCalendar(ctx.user.userId);
      return successResponse(result, "Calendar sync completed successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, error instanceof Error ? error.message : "Sync failed");
    }
  }
}

export default new CalendarController();
