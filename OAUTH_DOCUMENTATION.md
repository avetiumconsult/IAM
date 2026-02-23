# OAuth2/OIDC Authorization Server Documentation

## Overview

This Identity Service implements an OAuth2 Authorization Server with OpenID Connect (OIDC) support. It provides centralized authentication and authorization for multiple applications.

## Architecture

### Components

1. **Authorization Server** - Issues access tokens and ID tokens
2. **OAuth Clients** - Registered applications that can request tokens
3. **Users** - End users who authenticate
4. **Applications** - Business applications (POS, Payroll, Procurement, etc.)
5. **Roles & Permissions** - Application-specific access control

### Flow

```
User → Frontend App → OAuth Server → User Login → Token → Frontend App
```

## OAuth2 Authorization Code Flow

### Step 1: Authorization Request

The frontend redirects the user to the authorization endpoint:

```
GET /oauth2/authorize?
  client_id={client-id}&
  response_type=code&
  redirect_uri={redirect-uri}&
  scope=openid profile email&
  state={optional-state}
```

**Parameters:**
- `client_id` (required): The OAuth client ID (e.g., `payroll-client`, `pos-client`)
- `response_type` (required): Must be `code` for authorization code flow
- `redirect_uri` (required): Must match registered redirect URI for the client
- `scope` (optional): Space-separated list of scopes (default: `openid profile email`)
- `state` (optional): CSRF protection token

**Example:**
```
http://localhost:9000/oauth2/authorize?client_id=payroll-client&response_type=code&redirect_uri=http://localhost:3000/callback&scope=openid%20profile%20email
```

### Step 2: User Authentication

User is redirected to the custom login page (`/login`) which shows:
- Application-specific message: "Login to your Payroll account"
- Username/Email and Password fields

### Step 3: Authorization Code

After successful login, user is redirected back with an authorization code:

```
GET {redirect_uri}?code={authorization_code}&state={state}
```

**Example:**
```
http://localhost:3000/callback?code=q63rpKtK10BowarfIGqLbrkEDNr-adnAML21gwIqd674X5ejwOF_qIuSEgr7DtjcntVZhL5n9yp8T9tZg3f_TccdnxH_1Qd-cNH8tdqzuEDpjn3ssO8twaRv_PQIzu47
```

### Step 4: Token Exchange

Frontend exchanges the authorization code for tokens:

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic base64(client_id:client_secret)

grant_type=authorization_code
&code={authorization_code}
&redirect_uri={redirect_uri}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "eyJhbGciOiJSUzI1NiIs...",
  "scope": "openid profile email",
  "id_token": "eyJhbGciOiJSUzI1NiIs..."
}
```

## Available Scopes

### Standard OIDC Scopes

- **`openid`** (required): Enables OpenID Connect, returns ID token
- **`profile`**: User profile information (name, username, etc.)
- **`email`**: User's email address and verification status

### Custom Claims in Tokens

The JWT tokens include custom claims:
- `application`: Application name (e.g., "PAYROLL", "POS")
- `roles`: Array of user roles for the application (e.g., `["PAYROLL_ADMIN", "PAYROLL_MANAGER"]`)
- `perms`: Array of user permissions (e.g., `["EMPLOYEE:READ", "PAYRUN:CREATE"]`)

**Example Token Claims:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "email_verified": true,
  "name": "John Doe",
  "application": "PAYROLL",
  "roles": ["PAYROLL_ADMIN"],
  "perms": ["EMPLOYEE:READ", "EMPLOYEE:WRITE", "PAYRUN:CREATE"]
}
```

## OAuth Clients

### Client Configuration

Each application has an OAuth client with:
- **Client ID**: `{app-name}-client` (e.g., `payroll-client`)
- **Client Secret**: Auto-generated secure string (check logs on startup)
- **Redirect URIs**: Allowed callback URLs
- **Grant Types**: `authorization_code`, `refresh_token`
- **Scopes**: `openid`, `profile`, `email`

### Getting Client Credentials

OAuth clients are automatically created for all applications on startup (dev profile). Check application logs for:
```
=== OAUTH CLIENT CREATED FOR APPLICATION: PAYROLL ===
Client ID: payroll-client
Client Secret (RAW - SAVE THIS): abc123...
```

## API Endpoints

### Authentication Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/verify-email` | POST | Verify email with token |
| `/api/v1/auth/forgot-password` | POST | Request password reset |
| `/api/v1/auth/reset-password` | POST | Reset password with token |
| `/api/v1/auth/logout` | POST | Logout current user |

### OAuth Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Authorization endpoint |
| `/oauth2/token` | POST | Token endpoint |
| `/oauth2/jwks` | GET | JSON Web Key Set |
| `/.well-known/openid-configuration` | GET | OIDC discovery document |

### Admin Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/admin/users/roles` | POST | Assign role to user |
| `/api/v1/admin/users/{userId}/applications/{appName}/roles/{roleName}` | DELETE | Remove role from user |

## Frontend Integration Examples

### JavaScript/TypeScript

```typescript
// 1. Redirect to authorization
const clientId = 'payroll-client';
const redirectUri = encodeURIComponent('http://localhost:3000/callback');
const scope = encodeURIComponent('openid profile email');

window.location.href = 
  `http://localhost:9000/oauth2/authorize?` +
  `client_id=${clientId}&` +
  `response_type=code&` +
  `redirect_uri=${redirectUri}&` +
  `scope=${scope}`;

// 2. Handle callback
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');

// 3. Exchange code for token
const response = await fetch('http://localhost:9000/oauth2/token', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': `Basic ${btoa(`${clientId}:${clientSecret}`)}`
  },
  body: new URLSearchParams({
    grant_type: 'authorization_code',
    code: code,
    redirect_uri: 'http://localhost:3000/callback'
  })
});

const tokens = await response.json();
// Store tokens securely
localStorage.setItem('access_token', tokens.access_token);
```

### Python Example

```python
import requests
from urllib.parse import urlencode

# 1. Authorization URL
auth_url = "http://localhost:9000/oauth2/authorize"
params = {
    "client_id": "payroll-client",
    "response_type": "code",
    "redirect_uri": "http://localhost:3000/callback",
    "scope": "openid profile email"
}
authorization_url = f"{auth_url}?{urlencode(params)}"
# Redirect user to authorization_url

# 2. After user approves, exchange code for token
code = request.args.get('code')  # From callback
token_response = requests.post(
    "http://localhost:9000/oauth2/token",
    data={
        "grant_type": "authorization_code",
        "code": code,
        "redirect_uri": "http://localhost:3000/callback"
    },
    auth=("payroll-client", "your-client-secret")
)
tokens = token_response.json()
access_token = tokens["access_token"]
```

## Token Refresh

To refresh an access token:

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic base64(client_id:client_secret)

grant_type=refresh_token
&refresh_token={refresh_token}
```

## Security Considerations

1. **Client Secret**: Store securely, never expose in frontend code
2. **HTTPS**: Always use HTTPS in production
3. **State Parameter**: Use state parameter for CSRF protection
4. **Token Storage**: Store tokens securely (httpOnly cookies recommended)
5. **Token Expiry**: Access tokens expire in 1 hour, refresh tokens in 24 hours

## Testing

### Using Swagger UI

1. Navigate to `http://localhost:9000/swagger-ui.html`
2. Test endpoints interactively
3. Use "Authorize" button to authenticate

### Manual Testing

1. Register user: `POST /api/v1/auth/register`
2. Verify email: `POST /api/v1/auth/verify-email` (use token from logs)
3. OAuth flow: Visit authorization URL in browser
4. Exchange code: Use token endpoint with code

## Configuration

### SMTP Settings

Configure in `application-dev.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    from: ${SMTP_FROM}
```

Set environment variables:
- `SMTP_USERNAME`: Your SMTP username
- `SMTP_PASSWORD`: Your SMTP password/app password
- `SMTP_FROM`: Sender email address

## Troubleshooting

### Common Issues

1. **"Invalid client_id"**: Ensure OAuth client exists in database
2. **"Invalid redirect_uri"**: Check redirect URI matches registered URI exactly
3. **"Token expired"**: Request new token or use refresh token
4. **Email not sending**: Check SMTP configuration and credentials

## Support

For issues or questions, check:
- Swagger UI: `http://localhost:9000/swagger-ui.html`
- OIDC Discovery: `http://localhost:9000/.well-known/openid-configuration`
