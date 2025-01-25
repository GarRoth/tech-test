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
   git clone https://github.com/GarRoth/tech-test.git
   cd tech-test
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
  For ease of use it's recommended to use an API development tool, for exmaple: [Postman](https://www.postman.com/) see example

## Project Structure

- `service/`: Contains the main application code.
- `build.gradle`: Gradle configuration for the root project.
- `service/build.gradle`: Gradle configuration for the `service` subproject.

---

## **Endpoint List**

### **Base Path**: `/credit-cards`

1. **GET** `/credit-cards`
    - Retrieves all credit card accounts. (Note sensitive fields are masked in returned values)

2. **POST** `/credit-cards`
    - Creates a new credit card account.  
      **Example Payload:**
      ```json
      {
        "cardholderName": "Jake Peralta",
        "creditLimit": 500,
        "currency": "EUR",
        "creditCard": {
          "cardNumber": "4111111111111111",
          "expiry": "12/25",
          "cvv": 123,
          "cardType": "VISA"
        }
      }
      ```

3. **POST** `/credit-cards/charge`
    - Charges a credit card account for a specified amount.  
      **Example Payload:**
      ```json
      {
        "amount": 100,
        "cardNumber": "4111111111111111"
      }
      ```

4. **POST** `/credit-cards/credit`
    - Credits a credit card account for a specified amount.  
      **Example Payload:**
      ```json
      {
        "amount": 50,
        "cardNumber": "4111111111111111"
      }
      ```

5. **GET** `/credit-cards/{id}`
    - Retrieves a specific credit card account by its unique ID.

6. **PUT** `/credit-cards/{id}?newCreditLimit={value}`
    - Updates the credit limit for a specific account.  
      **Query Parameter:**
        - `newCreditLimit`: The new credit limit value (Int).

7. **DELETE** `/credit-cards/{id}`
    - Deletes a specific credit card account by its unique ID.


## Design Notes and Future Improvements
### Security
Security is a key concern when handling sensitive information like card data. Because of this, the card number is encrypted at rest.
However, one crucial improvement would be enforcing TLS for all communications to prevent traffic interception, such as eavesdropping or man-in-the-middle attacks. 
Additionally, the use of Slick for database operations enhances security by preventing SQL injection through type-safe query composition and parameterized queries, ensuring user input is handled safely.

### Error Handling
This design emphasizes clear separation of layers (API, Service, and DB), with each layer defining its own domain-specific errors. 
This separation creates a robust contract between layers, preventing domain logic from bleeding across boundaries. 
It also facilitates precise and meaningful error handling at each layer, ensuring errors are appropriately captured and handled without exposing sensitive internal details.


### Performance
The application uses two thread pools for processing: 
 - Http Dispatcher: Manages Http Processes
 - Default dispatcher: Manages service processes
This was set up in such a way that it would be possible to further bulkhead different sections of the application, 
ensuring that no single aspect of the application can prevent others from functioning.

Another performance consideration was lookups by card number. 
As we have to encrypt the card at rest, a lookup instantly becomes more expensive. 
Because of this, a second column was added with an index, that is a one-way has of the card number. 
This was the card number is still secure at rest, but lookups by the card number are slightly optimised.

### Composition Approach
This Scala project is designed using the Cake Pattern with mixins. 
This approach strongly supports the separation of concerns by dividing functionality into distinct modules, each responsible for a specific layer or aspect of the application.

Within this structure:
 - Members specific to a module are defined withn that module's trait.
 - Other modules or compnoents can "mixin" these traits to access their functionality, enabling clear composition and modularity.
This design is evident in the *Module traits and the main Application class where the modules are composed into the Application.