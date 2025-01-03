# Proyecto de Automatización con Cucumber, RestAssured y Allure

Este proyecto es una automatización de pruebas de un servicio utilizando **Java**, **Cucumber**, **RestAssured** y **Allure**. Está diseñado para validar la disponibilidad de clientes en un sistema basado en **RabbitMQ** y **PostgreSQL**. Los resultados de las pruebas se generan en reportes de Allure.

## Tecnologías utilizadas

- **Java 17**
- **Cucumber** (5.7.0) para pruebas basadas en comportamiento (BDD)
- **RestAssured** (4.3.3) para realizar peticiones HTTP y validación de respuestas
- **Allure** (2.13.6) para la generación de reportes de pruebas
- **JUnit** (4.12) como framework de pruebas
- **PostgreSQL** para interacción con base de datos
- **RabbitMQ** para simular el sistema de mensajería

## Requisitos previos

1. **Java 17**: Asegúrate de tener Java 17 instalado en tu máquina. 
2. **Gradle**: Este proyecto usa Gradle como sistema de construcción. 
3. **RabbitMQ**: Asegúrate de tener RabbitMQ (El servicio) ejecutándose en el entorno local o en un servidor accesible.
4. **PostgreSQL**: Se requiere una base de datos PostgreSQL activa y configurada para las pruebas.

## Configuración del proyecto

1. Clona el repositorio:
    
    git clone https://github.com/mauriciovalencia095/appgatechallenge.git

2. En el archivo config.properties, configurar los parámetros de conexión a RabbitMQ y PostgreSQL, como se muestra :

    rabbitmq.baseUri=http://localhost:15672
    rabbitmq.username=admin
    rabbitmq.password=admin
    rabbitmq.publishPath=/api/exchanges/%2F/testing.customer/publish
    rabbitmq.queueResponsePath=/api/queues/%2F/testing.customer.response.is-customer-available/get
    rabbitmq.queueCleanupPath=/api/queues/%2F/testing.customer.response.is-customer-available/contents
    database.host=localhost
    database.port=5432
    database.name=my_database
  

3. Asegúrate de que tu servicio de RabbitMQ esté activo y que PostgreSQL esté corriendo correctamente.

## Ejecución de las pruebas

Para ejecutar las pruebas, puedes usar Gradle desde la terminal.

1. **Ejecutar las pruebas con Gradle**:
./gradlew test 

2. **Generar los reportes de Allure**:

    Después de ejecutar las pruebas,  generar los reportes de Allure con lossiguiente comando:
    
    Primero:
   ./gradlew allureReport
   
    Y despues :
   ./gradlew allureServe

   Importante ejecutar los dos comandos en orden

    Esto abrirá el reporte de Allure en el navegador.



