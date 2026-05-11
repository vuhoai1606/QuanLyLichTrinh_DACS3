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
  return new Response(
    JSON.stringify({
      status,
      success: false,
      message,
      code: code || "ERROR",
      data: null,
    }),
    { status, headers: { "Content-Type": "application/json" } }
  );
};

export const successResponse = (data: any, message: string = "Success", httpStatus: number = 200) => {
  return new Response(
    JSON.stringify({
      status: httpStatus,
      success: true,
      message,
      data,
    }),
    { status: httpStatus, headers: { "Content-Type": "application/json" } }
  );
};
