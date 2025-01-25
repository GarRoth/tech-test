# Credit Card Management System

## Quickstart Guide

### Prerequisites

Ensure the following are installed on your machine:

1. **Java Development Kit (JDK)**: Version 17 or higher
2. **Gradle**: This project uses the Gradle wrapper, so no manual Gradle installation is required.
3. **Docker**: Docker must be installed and running to build and run the containerized application.

### Running the Application

Follow these steps to get the application up and running:

1. Clone the repository:

   ```bash
   git clone 
   cd 
   ```

2. Build the project:

   ```bash
   ./gradlew clean build
   ```

3. Build and run the Docker container:

   ```bash
   ./gradlew :service:jibDockerBuild :service:runContainer
   ```

4. The application will start and listen on port **9000**.

### Accessing the Application

Once the container is running, you can interact with the application through its exposed endpoints. For example:

- **Health Check (GET)**:

  ```bash
  curl http://localhost:9000/health
  ```

- **Create a Credit Card Account (POST)**:

  ```bash
  curl -X POST http://localhost:9000/credit-cards \
       -H "Content-Type: application/json" \
       -d '{
             "cardholderName": "Ben Howard",
             "creditLimit": 5000,
             "currency": "EUR",
             "creditCard": {
               "cardNumber": "4111111111111111",
               "expiry": "12/25",
               "cvv": 123,
               "cardType": "VISA"
             }
           }'
  ```

## Project Structure

- `service/`: Contains the main application code.
- `build.gradle`: Gradle configuration for the root project.
- `service/build.gradle`: Gradle configuration for the `service` subproject.

---

