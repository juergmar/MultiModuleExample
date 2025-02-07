# Multi-Module Spring Boot Application Instructions

## Structure
The project follows a multi-module Maven structure:

### my-app-parent (Root)
- The parent module that manages dependencies and common configurations
- Contains the main `pom.xml` with dependency management
- Defines common properties like Java version
- Lists all child modules

### my-app-base
- Core module containing shared components
- Houses base entities and repositories
- No executable components
- Dependencies:
    - spring-boot-starter
    - spring-boot-starter-data-jpa

### my-app-default
- Implementation module using PostgreSQL
- Executable Spring Boot application
- Dependencies:
    - my-app-base
    - spring-boot-starter
    - spring-boot-starter-web
    - postgresql
    - liquibase-core

### my-app-customerA
- Custom implementation for Customer A using Oracle
- Executable Spring Boot application
- Dependencies:
    - my-app-base
    - spring-boot-starter
    - spring-boot-starter-web
    - ojdbc8
    - liquibase-core

## Requirements

### Development Environment
- JDK 17 or higher
- Maven 3.6 or higher
- IDE with Spring Boot support (e.g., IntelliJ IDEA, Eclipse)
- Git

### Database Requirements
- PostgreSQL instance for default implementation
- Oracle instance for Customer A implementation
- Proper database credentials and permissions

### Configuration
Each executable module (default and customerA) requires:
1. Database configuration in `application.properties`
2. Liquibase changelog files
3. Proper JPA configuration
4. Component scanning configuration

## How to Create Executables

### Building the Project
1. Navigate to the root directory (my-app-parent)
2. Run Maven clean install:
   ```bash
   mvn clean install
   ```

### Creating Executable JARs
For each implementation module (default and customerA):

1. Navigate to the specific module directory:
   ```bash
   cd my-app-default
   # or
   cd my-app-customerA
   ```

2. Build the executable JAR:
   ```bash
   mvn spring-boot:repackage
   ```

3. Find the executable JAR in the `target` directory:
    - `my-app-default/target/my-app-default-1.0.0.jar`
    - `my-app-customerA/target/my-app-customerA-1.0.0.jar`

### Running the Applications
1. Start the default implementation:
   ```bash
   java -jar my-app-default/target/my-app-default-1.0.0.jar
   ```

2. Start the Customer A implementation:
   ```bash
   java -jar my-app-customerA/target/my-app-customerA-1.0.0.jar
   ```

### Configuration Files
Before running, ensure proper configuration in `application.properties`:

For default implementation:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=none
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

For Customer A implementation:
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=none
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### Verification
After starting each application:
1. Check application logs for successful startup
2. Verify database migrations completed successfully
3. Test REST endpoints:
   ```bash
   curl http://localhost:8080/customers
   ```

### Troubleshooting
Common issues and solutions:
1. Bean creation errors:
    - Verify proper component scanning configuration
    - Check JPA configuration and entity scanning
2. Database connection issues:
    - Verify database credentials
    - Ensure database service is running
3. Liquibase migration failures:
    - Check changelog syntax
    - Verify database user permissions
