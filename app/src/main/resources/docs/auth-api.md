# Authentication API Documentation

This document provides details about the authentication endpoints available in the Course App API.

## Base URL

All endpoints are relative to: `/api/auth`

## Authentication Endpoints

### Login

Authenticates a user and returns JWT tokens.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "userId": "number",
  "username": "string",
  "role": "SUPERADMIN | ADMIN | STUDENT"
}
```

**Status Codes:**
- `200 OK`: Authentication successful
- `401 Unauthorized`: Invalid credentials
- `400 Bad Request`: Invalid request format

### Register

Registers a new user and returns JWT tokens.

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "string",
  "password": "string",
  "role": "SUPERADMIN | ADMIN | STUDENT"
}
```

**Response:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "userId": "number",
  "username": "string",
  "role": "SUPERADMIN | ADMIN | STUDENT"
}
```

**Status Codes:**
- `200 OK`: Registration successful
- `400 Bad Request`: Username already exists or invalid request format

## Authentication Flow

1. **Login/Register**: Call the respective endpoint to obtain JWT tokens
2. **Using the token**: Include the access token in the Authorization header for protected endpoints:
   ```
   Authorization: Bearer {accessToken}
   ```

## Default Users

The system automatically creates the following default users on startup if they don't exist:

1. **Superadmin**
   - Username: superadmin
   - Password: superadmin123
   - Role: SUPERADMIN

2. **Admin**
   - Username: admin
   - Password: admin123
   - Role: ADMIN

## Security Notes

- Access tokens expire after 24 hours
- Refresh tokens expire after 7 days
- All passwords are stored encrypted in the database
- User roles determine access to different parts of the application
