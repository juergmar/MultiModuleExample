# Example Email Application

This application demonstrates how to use the email modules to send type-safe, templated emails.

## Features

- Send welcome emails to new users
- Send password reset emails
- Send order confirmation emails with order details

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- SMTP server for sending emails

### Running the Application

1. Configure your SMTP server details in `application.properties`
2. Build the application:
   ```
   mvn clean install
   ```
3. Run the application:
   ```
   mvn spring-boot:run
   ```

## API Endpoints

### Send Welcome Email

```
POST /api/emails/welcome
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe"
}
```

### Send Password Reset Email

```
POST /api/emails/password-reset
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Send Order Confirmation Email

```
POST /api/emails/order-confirmation
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe"
}
```

## How It Works

1. Email templates are defined in `email-definitions.json`
2. The `email-generator` Maven plugin generates a type-safe `AppEmailService` class
3. The service provides methods for creating each email type with proper parameters
4. The application uses Spring's mail infrastructure to send the emails

## Adding New Email Templates

1. Add a new template definition to `email-definitions.json`
2. Run `mvn compile` to generate the updated service
3. Use the new methods in your application code
