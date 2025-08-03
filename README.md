# **Waterman Analysis**

## **Features**
- User registration with unique username and password
- User login authentication
- Place orders by specifying address, quantity, can brand, and payment mode
- Orders saved securely in a MySQL database
- Instant bill generation displayed within the application

## **Prerequisites**
- Java Development Kit (JDK) 11 or higher
- A running MySQL database server
- MySQL Connector/J JDBC driver added to the project classpath

## **Setup Overview**
- Create the necessary database and tables in MySQL for users and orders
- Configure the database connection settings within the application
- Run the application from your Java IDE or command line with required dependencies

## **How to Use**
1. Register a new account with a unique username and password.
2. Log in using your credentials.
3. Enter order details including delivery address, quantity, preferred can, and payment method.
4. Submit the order and view your bill immediately displayed on the screen.

## **Technologies Used**
- Java Swing for the user interface
- MySQL for backend database management
- JDBC for database connectivity and operations

## **Notes**
- User and order data are stored persistently in the MySQL database.
- Bills are generated and shown within the application; email functionality is not included.
- Ensure database credentials are correctly set before running the application.
