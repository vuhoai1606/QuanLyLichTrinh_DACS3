/**
 * DTO Validation for Auth endpoints
 */

export interface RegisterDTO {
  email: string;
  password: string;
  full_name: string;
}

export interface LoginDTO {
  email: string;
  password: string;
}

export interface UpdateProfileDTO {
  full_name?: string;
  avatar_url?: string;
  bio?: string;
  timezone?: string;
}

export interface ChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Validate Register DTO
 */
export function validateRegister(data: any): { valid: boolean; errors: string[] } {
  const errors: string[] = [];

  if (!data.email) errors.push("Email is required");
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errors.push("Invalid email format");

  if (!data.password) errors.push("Password is required");
  else if (data.password.length < 8) errors.push("Password must be at least 8 characters");

  if (!data.full_name) errors.push("Full name is required");
  else if (data.full_name.length < 2) errors.push("Full name must be at least 2 characters");
  else if (data.full_name.length > 100) errors.push("Full name must be less than 100 characters");

  return { valid: errors.length === 0, errors };
}

/**
 * Validate Login DTO
 */
export function validateLogin(data: any): { valid: boolean; errors: string[] } {
  const errors: string[] = [];

  if (!data.email) errors.push("Email is required");
  if (!data.password) errors.push("Password is required");

  return { valid: errors.length === 0, errors };
}

/**
 * Validate Update Profile DTO
 */
export function validateUpdateProfile(data: any): { valid: boolean; errors: string[] } {
  const errors: string[] = [];

  if (data.full_name !== undefined) {
    if (data.full_name.length < 2) errors.push("Full name must be at least 2 characters");
    if (data.full_name.length > 100) errors.push("Full name must be less than 100 characters");
  }

  if (data.avatar_url !== undefined) {
    try {
      new URL(data.avatar_url);
    } catch {
      errors.push("Invalid avatar URL");
    }
  }

  if (data.bio !== undefined && data.bio.length > 280) {
    errors.push("Bio must be less than 280 characters");
  }

  return { valid: errors.length === 0, errors };
}

/**
 * Validate Change Password DTO
 */
export function validateChangePassword(data: any): { valid: boolean; errors: string[] } {
  const errors: string[] = [];

  if (!data.oldPassword) errors.push("Old password is required");
  if (!data.newPassword) errors.push("New password is required");
  if (!data.confirmPassword) errors.push("Confirm password is required");

  if (data.newPassword && data.newPassword.length < 8) {
    errors.push("New password must be at least 8 characters");
  }

  if (data.newPassword !== data.confirmPassword) {
    errors.push("Passwords do not match");
  }

  return { valid: errors.length === 0, errors };
}
