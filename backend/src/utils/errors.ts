export class AppError extends Error {
  constructor(
    public status: number,
    message: string,
    public code?: string
  ) {
    super(message);
    Object.setPrototypeOf(this, AppError.prototype);
  }
}

export const errorResponse = (status: number, message: string, code?: string) => {
  return {
    status,
    error: {
      message,
      code: code || "ERROR",
    },
  };
};

export const successResponse = (data: any, message: string = "Success") => {
  return {
    status: 200,
    success: true,
    message,
    data,
  };
};
