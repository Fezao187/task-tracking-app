package com.katlego.task_tracking_api.docs;

public class ApiDocs {

    public static class AuthApi {
        public static final String TAG = "Auth API";

        public static final String SIGNUP_SUMMARY = "User signup";
        public static final String SIGNUP_DESC = "Registers a new user and returns JWT access and refresh tokens.";

        public static final String LOGIN_SUMMARY = "User login";
        public static final String LOGIN_DESC = "Authenticates a user and returns JWT access and refresh tokens.";

        public static final String REFRESH_SUMMARY = "Refresh access token";
        public static final String REFRESH_DESC = "Refreshes the access token using a valid refresh token.";
    }

    public static class TaskApi {
        public static final String TAG = "Task API";

        public static final String CREATE_TASK_SUMMARY = "Create task";
        public static final String CREATE_TASK_DESC = "Creates a new task. Admin only.";

        public static final String UPDATE_TASK_SUMMARY = "Update task";
        public static final String UPDATE_TASK_DESC = "Updates an existing task by id. Admin only.";

        public static final String SEARCH_TASKS_SUMMARY = "Search tasks";
        public static final String SEARCH_TASKS_DESC = "Returns tasks filtered by status, due date, and assignee with pagination. Admin only.";

        public static final String ASSIGN_TASK_SUMMARY = "Assign task";
        public static final String ASSIGN_TASK_DESC = "Assigns a task to a user. Admin only.";

        public static final String GET_TASK_BY_ID_SUMMARY = "Get task by id";
        public static final String GET_TASK_BY_ID_DESC = "Returns details of a single task by id.";

        public static final String GET_MY_TASKS_SUMMARY = "Get my tasks";
        public static final String GET_MY_TASKS_DESC = "Returns tasks assigned to the authenticated user.";

        public static final String GET_ALL_TASKS_SUMMARY = "Get all tasks";
        public static final String GET_ALL_TASKS_DESC = "Returns all tasks in the system. Admin only.";

        public static final String DELETE_TASK_SUMMARY = "Delete task";
        public static final String DELETE_TASK_DESC = "Deletes a task by id. Admin only.";
    }

    public static class UserApi {
        public static final String TAG = "User API";

        public static final String ADMIN_CREATE_USER_SUMMARY = "Create user (admin)";
        public static final String ADMIN_CREATE_USER_DESC = "Creates a new user account with USER role. Admin only.";
    }
}
