# Tax Consultancy Web Portal

A comprehensive web application for tax consultancy services built with Angular frontend and Spring Boot backend.

## 🏗️ Architecture

- **Frontend**: Angular 17+ with TypeScript
- **Backend**: Spring Boot 3.x with Java
- **Database**: MySQL
- **Build Tool**: Maven (Backend), npm (Frontend)

## 📁 Project Structure

```
Tax Consultancy Web Portal/
├── auth service/                 # Spring Boot Backend
│   ├── src/
│   │   ├── main/java/com/adspeek/authservice/
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── service/         # Business Logic
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── repository/      # Data Access Layer
│   │   │   └── config/          # Configuration Classes
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml                  # Maven Dependencies
│   └── uploads/                 # File Upload Directory
├── ui/ui/                       # Angular Frontend
│   ├── src/app/
│   │   ├── admin-dashboard/     # Admin Dashboard
│   │   ├── client-dashboard/    # Client Dashboard
│   │   ├── staff-dashboard/     # Staff Dashboard
│   │   ├── lead-management/     # Lead Management
│   │   ├── service-request/     # Service Requests
│   │   ├── task-management/     # Task Management
│   │   ├── document-management/ # Document Management
│   │   ├── notifications/       # Notification System
│   │   └── services/            # Angular Services
│   ├── package.json             # Node Dependencies
│   └── angular.json             # Angular Configuration
└── *.sql                        # Database Schema Files
```

## 🚀 Features

### Admin Features

- Dashboard with analytics and overview
- Staff management and role assignment
- Client management
- Service request monitoring
- Document approval workflow
- Notification management

### Staff Features

- Task management and assignment
- Lead management and conversion
- Service request handling
- Document upload and management
- Client communication
- Activity tracking

### Client Features

- Service request submission
- Document upload
- Progress tracking
- Communication with staff
- Profile management

## 🛠️ Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: 18.x or higher
- **npm**: 9.x or higher
- **MySQL**: 8.0 or higher
- **Maven**: 3.8 or higher

## 📦 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd "Tax Consultancy Web Portal"
```

### 2. Database Setup

1. Create a MySQL database named `tax_consultancy`
2. Run the SQL files in the following order:
   ```bash
   mysql -u root -p tax_consultancy < database_schema.sql
   mysql -u root -p tax_consultancy < database_schema_part1.sql
   mysql -u root -p tax_consultancy < database_schema_part2.sql
   mysql -u root -p tax_consultancy < database_schema_part3.sql
   mysql -u root -p tax_consultancy < database_migration.sql
   ```

### 3. Backend Setup (Spring Boot)

```bash
cd "auth service"
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 4. Frontend Setup (Angular)

```bash
cd ui/ui
npm install
ng serve
```

The frontend will start on `http://localhost:4200`

## ⚙️ Configuration

### Backend Configuration

Update `auth service/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/tax_consultancy
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Server Configuration
server.port=8080
```

### Frontend Configuration

Update `ui/ui/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
};
```

## 🔐 Authentication & Authorization

The application uses JWT-based authentication with role-based access control:

- **ADMIN**: Full system access
- **STAFF**: Limited access to assigned tasks and clients
- **CLIENT**: Access to own data and service requests

## 📊 Database Schema

Key entities include:

- **Users**: Admin, Staff, and Client accounts
- **Leads**: Potential client information
- **ServiceRequests**: Client service requests
- **Tasks**: Staff task assignments
- **Documents**: File uploads and management
- **Notifications**: System notifications
- **Activities**: Audit trail and activity logs

## 🧪 Testing

### Backend Testing

```bash
cd "auth service"
mvn test
```

### Frontend Testing

```bash
cd ui/ui
ng test
```

## 📱 API Documentation

The backend provides REST APIs for:

- Authentication (`/api/auth/*`)
- User Management (`/api/users/*`)
- Lead Management (`/api/leads/*`)
- Service Requests (`/api/service-requests/*`)
- Task Management (`/api/tasks/*`)
- Document Management (`/api/documents/*`)
- Notifications (`/api/notifications/*`)

## 🚀 Deployment

### Backend Deployment

1. Build the JAR file:
   ```bash
   mvn clean package
   ```
2. Deploy the JAR file to your server
3. Configure environment variables for production

### Frontend Deployment

1. Build for production:
   ```bash
   ng build --configuration production
   ```
2. Deploy the `dist/ui/browser` folder to your web server

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Your Name** - _Initial work_ - [YourGitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Spring Boot community
- Angular team
- MySQL documentation
- All contributors and testers

## 📞 Support

For support, email your-email@example.com or create an issue in the repository.

---

**Note**: Make sure to update the database credentials and API URLs according to your environment before running the application.
