Feature: Validación de Disponibilidad de Cliente
  Validar si un cliente está disponible para el servicio de detección de phishing.

  Background:
    Given que el servicio RabbitMQ está corriendo
    And la base de datos PostgreSQL está activa


  Scenario: Cliente activo con detección de phishing habilitada
    When se consulta al servicio RabbitMQ con nombre "CUSTOMER1"
    Then el sistema responde que el cliente está disponible

  Scenario: Cliente activo sin detección de phishing habilitada
    When se consulta al servicio RabbitMQ con nombre "CUSTOMER2"
    Then el sistema responde que el "CUSTOMER2" no está disponible

  Scenario: Cliente inactivo con detección de phishing habilitada
    When se consulta al servicio RabbitMQ con nombre "CUSTOMER3"
    Then el sistema responde que el "CUSTOMER3" no está disponible

  Scenario: Cliente inactivo sin detección de phishing habilitada
    When se consulta al servicio RabbitMQ con nombre "CUSTOMER4"
    Then el sistema responde que el "CUSTOMER4" no está disponible

  Scenario: Cliente inexistente
    When se consulta al servicio RabbitMQ con nombre "CUSTOMER5"
    Then el sistema responde que el "CUSTOMER5" no está disponible

  Scenario: Solicitudes concurrentes para clientes
    When se realizan solicitudes simultáneas para "CUSTOMER1", "CUSTOMER2", "CUSTOMER3", "CUSTOMER4"
    Then el sistema responde que "CUSTOMER1" está disponible
    And "CUSTOMER2,CUSTOMER3,CUSTOMER4" no están disponibles










