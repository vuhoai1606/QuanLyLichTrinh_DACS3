import collaborationService from "@services/CollaborationService";
import { successResponse, errorResponse, AppError } from "@utils/errors";

export class CollaborationController {
  // Create group
  async createGroup(body: any) {
    const { name, leader_id } = body;

    if (!name || !leader_id) {
      return errorResponse(400, "name and leader_id required", "MISSING_FIELDS");
    }

    try {
      const group = await collaborationService.createGroup(name, leader_id);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Group created successfully",
          data: group,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get user groups
  async getUserGroups(userId: string) {
    try {
      const groups = await collaborationService.getUserGroups(userId);
      return successResponse(groups, "User groups retrieved successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Add member to group
  async addMemberToGroup(body: any) {
    const { group_id, user_id, requester_id } = body;

    if (!group_id || !user_id || !requester_id) {
      return errorResponse(400, "group_id, user_id, and requester_id required", "MISSING_FIELDS");
    }

    try {
      const member = await collaborationService.addMemberToGroup(group_id, user_id, requester_id);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Member added successfully",
          data: member,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get group members
  async getGroupMembers(groupId: string) {
    try {
      const members = await collaborationService.getGroupMembers(groupId);
      return successResponse(members, "Group members retrieved successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get group activity
  async getGroupActivity(groupId: string) {
    try {
      const activity = await collaborationService.getGroupActivity(groupId);
      return successResponse(activity, "Group activity retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Send group invite
  async sendGroupInvite(body: any) {
    const { group_id, inviter_id, invitee_id } = body;

    if (!group_id || !inviter_id || !invitee_id) {
      return errorResponse(400, "group_id, inviter_id, and invitee_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.sendGroupInvite(group_id, inviter_id, invitee_id);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Invite sent",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Leave group
  async leaveGroup(body: any) {
    const { group_id, user_id } = body;

    if (!group_id || !user_id) {
      return errorResponse(400, "group_id and user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.leaveGroup(group_id, user_id);
      return successResponse(result, "Left group successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Delete group
  async deleteGroup(body: any) {
    const { group_id, requester_id } = body;

    if (!group_id || !requester_id) {
      return errorResponse(400, "group_id and requester_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.deleteGroup(group_id, requester_id);
      return successResponse(result, "Group deleted");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Update group info
  async updateGroupInfo(body: any) {
    const { group_id, requester_id } = body;

    if (!group_id || !requester_id) {
      return errorResponse(400, "group_id and requester_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.updateGroupInfo(group_id, requester_id, body);
      return successResponse(result, "Group updated");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get shared with me
  async getSharedWithMe(userId: string) {
    try {
      const result = await collaborationService.getSharedWithMe(userId);
      return successResponse(result, "Shared schedules retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Request collaboration
  async requestCollaboration(body: any) {
    const { schedule_id, requester_id, target_user_id } = body;

    if (!schedule_id || !requester_id || !target_user_id) {
      return errorResponse(400, "schedule_id, requester_id, and target_user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.requestCollaboration(
        schedule_id,
        requester_id,
        target_user_id
      );

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Collaboration request sent",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Transfer leadership
  async transferLeadership(body: any) {
    const { group_id, current_leader_id, new_leader_id } = body;

    if (!group_id || !current_leader_id || !new_leader_id) {
      return errorResponse(400, "group_id, current_leader_id, and new_leader_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.transferGroupLeadership(
        group_id,
        current_leader_id,
        new_leader_id
      );

      return successResponse(result, "Leadership transferred");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Share copy
  async shareScheduleCopy(body: any) {
    const { schedule_id, sender_id, recipient_id } = body;

    if (!schedule_id || !sender_id || !recipient_id) {
      return errorResponse(400, "schedule_id, sender_id, and recipient_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.shareScheduleCopy(
        schedule_id,
        sender_id,
        recipient_id
      );

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Schedule copied and shared",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Share collaboration
  async shareScheduleCollab(body: any) {
    const { schedule_id, sender_id, recipient_id, permission_level } = body;

    if (!schedule_id || !sender_id || !recipient_id) {
      return errorResponse(400, "schedule_id, sender_id, and recipient_id required", "MISSING_FIELDS");
    }

    try {
      const result = await collaborationService.shareScheduleCollab(
        schedule_id,
        sender_id,
        recipient_id,
        permission_level
      );

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Collaborator added successfully",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get collaborators
  async getScheduleCollaborators(scheduleId: string) {
    try {
      const collaborators = await collaborationService.getScheduleCollaborators(scheduleId);
      return successResponse(collaborators, "Collaborators retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Remove collaborator
  async removeCollaborator(scheduleId: string, userId: string) {
    try {
      const result = await collaborationService.removeCollaborator(scheduleId, userId);
      return successResponse({ success: result }, "Collaborator removed");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new CollaborationController();
