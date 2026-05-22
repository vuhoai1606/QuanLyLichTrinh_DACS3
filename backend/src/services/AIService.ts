import { GoogleGenerativeAI } from "@google/generative-ai";
import { config } from "@config/env";
import { logger } from "@utils/logger";

export class AIService {
  private genAI: GoogleGenerativeAI;
  private model: any;

  constructor() {
    const apiKey = process.env.GEMINI_API_KEY || "";
    this.genAI = new GoogleGenerativeAI(apiKey);
    this.model = this.genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
  }

  async breakdownTask(taskTitle: string, taskDescription: string): Promise<string[]> {
    try {
      const prompt = `Break down this task into a list of 3-5 small, actionable sub-tasks:
      Title: ${taskTitle}
      Description: ${taskDescription}
      Return only the list of sub-tasks as a JSON array of strings.`;

      const result = await this.model.generateContent(prompt);
      const response = await result.response;
      const text = response.text();
      
      // Basic JSON extraction
      const jsonStr = text.substring(text.indexOf("["), text.lastIndexOf("]") + 1);
      return JSON.parse(jsonStr);
    } catch (error) {
      logger.error("AI breakdownTask error", error);
      return ["Review task details", "Plan implementation", "Execute work"];
    }
  }

  async suggestSchedule(userId: string, currentTasks: any[]): Promise<string> {
    try {
      const taskList = currentTasks.map(t => `- ${t.title} (Priority: ${t.priority}, Deadline: ${t.deadline})`).join("\n");
      const prompt = `Based on these tasks, suggest an optimized schedule for today:
      ${taskList}
      Provide a brief, encouraging plan.`;

      const result = await this.model.generateContent(prompt);
      const response = await result.response;
      return response.text();
    } catch (error) {
      logger.error("AI suggestSchedule error", error);
      return "Focus on your highest priority task first!";
    }
  }
}

export default new AIService();
