import { AppDataSource } from "@config/database";
import { generateUUID } from "@utils/validation";
import { AppError } from "@utils/errors";

export class CollaborationService {
  async createGroup(userId: string, name: string, description?: string, avatar_url?: string, membersList: {user_id: string, role: string}[] = []): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = groupRepository.create({
      name,
      description,
      avatar_url,
      leader_id: userId,
    });

    const savedGroup = await groupRepository.save(group);

    // Add creator as LEADER
    const members = [{ group_id: savedGroup.id, user_id: userId, role: "LEADER" }];
    
    // Add other members
    if (membersList && membersList.length > 0) {
      membersList.forEach(m => {
        if (m.user_id !== userId) {
          members.push({ group_id: savedGroup.id, user_id: m.user_id, role: m.role || "MEMBER" });
        }
      });
    }

    await groupMemberRepository.save(members);

    return this.getGroupDetails(savedGroup.id);
  }

  async getGroupDetails(group_id: string): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) return null;
    return group;
  }

  async addMemberToGroup(group_id: string, user_id: string, requester_id: string, role: string = "MEMBER"): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    const requesterMember = await groupMemberRepository.findOne({ where: { group_id, user_id: requester_id } });
    if (!requesterMember || (requesterMember.role !== "LEADER" && requesterMember.role !== "DEPUTY")) {
      throw new AppError(403, "Only leader or deputy can add members", "UNAUTHORIZED");
    }

    const existing = await groupMemberRepository.findOne({ where: { group_id, user_id } });
    if (existing) {
      throw new AppError(400, "User already a member", "ALREADY_MEMBER");
    }

    const member = groupMemberRepository.create({
      group_id,
      user_id,
      role,
    });

    return groupMemberRepository.save(member);
  }

  async removeMemberFromGroup(group_id: string, user_id: string, requester_id: string): Promise<any> {
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");
    const groupRepository = AppDataSource.getRepository("Group");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    const requesterMember = await groupMemberRepository.findOne({ where: { group_id, user_id: requester_id } });
    const targetMember = await groupMemberRepository.findOne({ where: { group_id, user_id } });

    if (!requesterMember || !targetMember) {
      throw new AppError(404, "User not in group", "NOT_MEMBER");
    }

    // Role checks
    if (requesterMember.role === "MEMBER") {
      throw new AppError(403, "MEMBER cannot remove others", "UNAUTHORIZED");
    }
    if (requesterMember.role === "DEPUTY" && (targetMember.role === "LEADER" || targetMember.role === "DEPUTY")) {
      throw new AppError(403, "DEPUTY can only remove MEMBER", "UNAUTHORIZED");
    }

    await groupMemberRepository.remove(targetMember);
    return { success: true };
  }


  async assignScheduleToUser(schedule_id: string, assignee_id: string, leader_id: string): Promise<any> {
    const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");

    const assignment = assignmentRepository.create({
      schedule_id,
      assignee_id,
      assign_status: "PENDING",
    });

    return assignmentRepository.save(assignment);
  }

  async updateAssignmentStatus(schedule_id: string, assignee_id: string, status: "ACCEPTED" | "DECLINED"): Promise<any> {
    const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");

    const assignment = await assignmentRepository.findOne({
      where: { schedule_id, assignee_id },
    });

    if (!assignment) {
      throw new AppError(404, "Assignment not found", "ASSIGNMENT_NOT_FOUND");
    }

    assignment.assign_status = status;
    return assignmentRepository.save(assignment);
  }

  async getUserGroups(user_id: string): Promise<any[]> {
    try {
      const groupMemberRepository = AppDataSource.getRepository("GroupMember");

      const userMemberships = await groupMemberRepository.find({
        where: { user_id },
        relations: ["group"],
      });

      return userMemberships.map((m: any) => ({
        id: m.group.id,
        name: m.group.name,
        description: m.group.description,
        avatar_url: m.group.avatar_url,
        leader_id: m.group.leader_id,
        role: m.role,
        created_at: m.group.created_at,
      }));
    } catch (error) {
      console.error("Error in getUserGroups:", error);
      throw error;
    }
  }

  async getGroupTasks(group_id: string): Promise<any[]> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const tasks = await scheduleRepository.find({
      where: { group_id },
      relations: ["category", "reminders", "assignments", "assignments.assignee"],
      order: { created_at: "DESC" },
    });

    return tasks.map((task: any) => ({
      id: task.id,
      group_id: task.group_id,
      title: task.title,
      description: task.description,
      type: task.type,
      status: task.status,
      priority: task.priority,
      deadline: task.deadline,
      start_time: task.start_time,
      end_time: task.end_time,
      creator_id: task.creator_id,
      assignees: task.assignments?.map((a: any) => a.assignee?.full_name).filter(Boolean) || [],
      completed_assignees: task.assignments?.filter((a: any) => a.is_completed).length || 0,
      total_assignees: task.assignments?.length || 0,
      created_at: task.created_at,
    }));
  }

  async getGroupMembers(group_id: string): Promise<any[]> {
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const members = await groupMemberRepository.find({
      where: { group_id },
      relations: ["user"],
    });

    return members.map((m: any) => ({
      ...m.user,
      role: m.role,
      joined_at: m.created_at,
    }));
  }

  async getAssignmentsForUser(user_id: string, status?: "PENDING" | "ACCEPTED" | "DECLINED"): Promise<any[]> {
    const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");

    const where: any = { assignee_id: user_id };
    if (status) {
      where.assign_status = status;
    }

    return assignmentRepository.find({
      where,
      relations: ["schedule", "schedule.creator"],
    });
  }

  // Mechanism 1: Share Copy - Gửi bản sao
  async shareScheduleCopy(schedule_id: string, sender_id: string, recipient_id: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const shareLogRepository = AppDataSource.getRepository("ShareLog");

    // Get original schedule
    const originalSchedule = await scheduleRepository.findOne({
      where: { id: schedule_id },
    });

    if (!originalSchedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    // Create a copy of the schedule
    const newSchedule = scheduleRepository.create({
      ...originalSchedule,
      id: generateUUID(),
      creator_id: recipient_id,
      created_at: new Date(),
      updated_at: new Date(),
    });

    const savedSchedule = await scheduleRepository.save(newSchedule);

    // Log the share
    await shareLogRepository.save(
      shareLogRepository.create({
        schedule_id,
        sender_id,
        recipient_id,
        share_type: "COPY",
        new_schedule_id: savedSchedule.id,
      })
    );

    return {
      original_schedule_id: schedule_id,
      new_schedule_id: savedSchedule.id,
      schedule: savedSchedule,
    };
  }

  // Mechanism 2: Share Collab - Cùng xem/Cùng làm
  async shareScheduleCollab(
    schedule_id: string,
    sender_id: string,
    recipient_id: string,
    permissionLevel: "VIEW" | "EDIT" = "VIEW"
  ): Promise<any> {
    const collaboratorRepository = AppDataSource.getRepository("TaskCollaborator");
    const shareLogRepository = AppDataSource.getRepository("ShareLog");
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    // Verify schedule exists
    const schedule = await scheduleRepository.findOne({ where: { id: schedule_id } });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    // Check if already a collaborator
    const existing = await collaboratorRepository.findOne({
      where: { schedule_id, user_id: recipient_id },
    });

    if (existing) {
      throw new AppError(400, "User is already a collaborator", "ALREADY_COLLABORATOR");
    }

    // Add collaborator
    const collaborator = collaboratorRepository.create({
      schedule_id,
      user_id: recipient_id,
      permission_level: permissionLevel,
    });

    await collaboratorRepository.save(collaborator);

    // Log the share
    await shareLogRepository.save(
      shareLogRepository.create({
        schedule_id,
        sender_id,
        recipient_id,
        share_type: "COLLAB",
      })
    );

    return collaborator;
  }

  async removeCollaborator(schedule_id: string, recipient_id: string): Promise<boolean> {
    const collaboratorRepository = AppDataSource.getRepository("TaskCollaborator");

    await collaboratorRepository.delete({
      schedule_id,
      user_id: recipient_id,
    });

    return true;
  }

  async getScheduleCollaborators(schedule_id: string): Promise<any[]> {
    const collaboratorRepository = AppDataSource.getRepository("TaskCollaborator");

    const collaborators = await collaboratorRepository
      .createQueryBuilder("tc")
      .leftJoinAndSelect("tc.user", "user")
      .where("tc.schedule_id = :schedule_id", { schedule_id })
      .select([
        "user.id",
        "user.full_name",
        "user.avatar_url",
        "tc.permission_level",
        "tc.added_at",
      ])
      .getMany();

    return collaborators.map((c: any) => ({
      ...c.user,
      permission_level: c.permission_level,
      added_at: c.added_at,
    }));
  }

  async getGroupActivity(group_id: string, limit: number = 50): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = await groupMemberRepository.find({
      where: { group_id },
    });

    const memberIds = group.map(m => m.user_id);

    const activity = await scheduleRepository.find({
      where: {
        creator_id: { in: memberIds },
      },
      order: { updated_at: "DESC" },
      take: limit,
    });

    return {
      groupId: group_id,
      recentActivity: activity,
      totalActivities: activity.length,
    };
  }

  async sendGroupInvite(group_id: string, inviter_id: string, invitee_id: string): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    // In real app, this would create an invitation record and send notification
    return {
      groupId: group_id,
      inviterId: inviter_id,
      inviteeId: invitee_id,
      status: "PENDING",
      invitedAt: new Date(),
      inviteLink: `https://app.bfy.com/invite/${group_id}/${generateUUID()}`,
    };
  }

  async leaveGroup(group_id: string, user_id: string): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    if (group.leader_id === user_id) {
      throw new AppError(400, "Leader cannot leave group. Transfer leadership first.", "LEADER_CANNOT_LEAVE");
    }

    const member = await groupMemberRepository.findOne({
      where: { group_id, user_id },
    });

    if (!member) {
      throw new AppError(404, "User not in group", "NOT_MEMBER");
    }

    await groupMemberRepository.remove(member);

    return {
      groupId: group_id,
      userId: user_id,
      leftAt: new Date(),
    };
  }

  async deleteGroup(group_id: string, requester_id: string): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    if (group.leader_id !== requester_id) {
      throw new AppError(403, "Only leader can delete group", "UNAUTHORIZED");
    }

    await groupMemberRepository.delete({ group_id });
    await groupRepository.remove(group);

    return {
      groupId: group_id,
      deletedAt: new Date(),
      message: "Group deleted successfully",
    };
  }

  async updateGroupInfo(group_id: string, requester_id: string, updateData: any): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");
    const groupMemberRepository = AppDataSource.getRepository("GroupMember");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    const requesterMember = await groupMemberRepository.findOne({ where: { group_id, user_id: requester_id } });
    if (!requesterMember || (requesterMember.role !== "LEADER" && requesterMember.role !== "DEPUTY")) {
      throw new AppError(403, "Only LEADER or DEPUTY can update group", "UNAUTHORIZED");
    }

    if (updateData.name) group.name = updateData.name;
    if (updateData.description) group.description = updateData.description;
    if (updateData.avatar_url) group.avatar_url = updateData.avatar_url;

    return groupRepository.save(group);
  }

  async getSharedWithMe(user_id: string): Promise<any> {
    const collaboratorRepository = AppDataSource.getRepository("TaskCollaborator");
    const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const { In } = require("typeorm");

    const collaborations = await collaboratorRepository.find({
      where: { user_id },
    });

    const assignments = await assignmentRepository.find({
      where: { assignee_id: user_id },
    });

    const collabScheduleIds = collaborations.map(c => c.schedule_id);
    const assignScheduleIds = assignments.map(a => a.schedule_id);

    const allScheduleIds = Array.from(new Set([...collabScheduleIds, ...assignScheduleIds]));

    if (allScheduleIds.length === 0) {
      return {
        totalShared: 0,
        schedules: [],
      };
    }

    const schedules = await scheduleRepository.find({
      where: { id: In(allScheduleIds) },
      relations: ["creator", "category", "reminders"],
    });

    const filteredSchedules = schedules.filter((s: any) => s.group_id !== null && s.group_id !== undefined);

    return {
      totalShared: filteredSchedules.length,
      schedules: filteredSchedules.map((s: any) => ({
        ...s,
        permission: collaborations.find(c => c.schedule_id === s.id)?.permission_level || "VIEW",
      })),
    };
  }

  async requestCollaboration(schedule_id: string, requester_id: string, target_user_id: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const schedule = await scheduleRepository.findOne({ where: { id: schedule_id } });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    // In real app, this would create a collaboration request record
    return {
      requestId: generateUUID(),
      scheduleId: schedule_id,
      requesterId: requester_id,
      targetUserId: target_user_id,
      status: "PENDING",
      requestedAt: new Date(),
      expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
    };
  }

  async transferGroupLeadership(group_id: string, current_leader_id: string, new_leader_id: string): Promise<any> {
    const groupRepository = AppDataSource.getRepository("Group");

    const group = await groupRepository.findOne({ where: { id: group_id } });
    if (!group) {
      throw new AppError(404, "Group not found", "GROUP_NOT_FOUND");
    }

    if (group.leader_id !== current_leader_id) {
      throw new AppError(403, "Only current leader can transfer leadership", "UNAUTHORIZED");
    }

    group.leader_id = new_leader_id;
    return groupRepository.save(group);
  }
}

export default new CollaborationService();
