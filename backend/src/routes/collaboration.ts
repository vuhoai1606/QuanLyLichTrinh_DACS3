import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import { errorResponse } from "@utils/errors";
import { extractToken, verifyToken } from "@utils/jwt";
import collaborationController from "@controllers/CollaborationController";

export const collaborationRoutes = new Elysia({ prefix: "/collaboration" })
  .get(
    "/groups",
    async (ctx: AuthContext) => {
      const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
      if (!token) {
        return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
      }
      const payload = verifyToken(token);
      if (!payload) {
        return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
      }
      ctx.user = payload as any;
      const userId = (ctx as any).user?.userId;
      if (!userId) {
        return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
      }
      return collaborationController.getUserGroups(userId);
    },
    { tags: ["Collaboration"] }
  )
  .post("/groups", async ({ body }: { body: any }) => collaborationController.createGroup(body), { tags: ["Collaboration"] })
  .get("/groups/user/:userId", async ({ params }: { params: any }) => collaborationController.getUserGroups(params.userId), { tags: ["Collaboration"] })
  .get("/groups/:group_id/tasks", async ({ params }: { params: any }) => collaborationController.getGroupTasks(params.group_id), { tags: ["Collaboration"] })
  .post("/groups/:group_id/members", async ({ params, body }: { params: any; body: any }) => collaborationController.addMemberToGroup({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .delete("/groups/:group_id/members/:user_id", async ({ params, body }: { params: any; body: any }) => collaborationController.removeMemberFromGroup({ ...body, group_id: params.group_id, user_id: params.user_id }), { tags: ["Collaboration"] })
  .get("/groups/:group_id/members", async ({ params }: { params: any }) => collaborationController.getGroupMembers(params.group_id), { tags: ["Collaboration"] })
  .get("/groups/:group_id/activity", async ({ params }: { params: any }) => collaborationController.getGroupActivity(params.group_id), { tags: ["Collaboration"] })
  .post("/groups/:group_id/invite", async ({ params, body }: { params: any; body: any }) => collaborationController.sendGroupInvite({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .post("/groups/:group_id/leave", async ({ params, body }: { params: any; body: any }) => collaborationController.leaveGroup({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .delete("/groups/:group_id", async ({ params, body }: { params: any; body: any }) => collaborationController.deleteGroup({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .put("/groups/:group_id", async ({ params, body }: { params: any; body: any }) => collaborationController.updateGroupInfo({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .get("/shared-with-me", async ({ query }: { query: any }) => collaborationController.getSharedWithMe(query.user_id), { tags: ["Collaboration"] })
  .post("/request", async ({ body }: { body: any }) => collaborationController.requestCollaboration(body), { tags: ["Collaboration"] })
  .put("/groups/:group_id/transfer-leadership", async ({ params, body }: { params: any; body: any }) => collaborationController.transferLeadership({ ...body, group_id: params.group_id }), { tags: ["Collaboration"] })
  .post("/schedules/share-copy", async ({ body }: { body: any }) => collaborationController.shareScheduleCopy(body), { tags: ["Collaboration"] })
  .post("/schedules/share-collab", async ({ body }: { body: any }) => collaborationController.shareScheduleCollab(body), { tags: ["Collaboration"] })
  .get("/schedules/:scheduleId/collaborators", async ({ params }: { params: any }) => collaborationController.getScheduleCollaborators(params.scheduleId), { tags: ["Collaboration"] })
  .post("/schedules/:scheduleId/remove-collaborator", async ({ params, query }: { params: any; query: any }) => collaborationController.removeCollaborator(params.scheduleId, query.user_id), { tags: ["Collaboration"] });
