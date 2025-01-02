package co.appgate.stepdefinitions;

import co.appgate.services.RabbitMQService;
import co.appgate.services.ResponseValidator;
import co.appgate.utils.DatabaseConnection;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import static io.restassured.RestAssured.given;

public class ValidacionDeUsuarios {
    private RabbitMQService rabbitMQService = new RabbitMQService();
    private ResponseValidator responseValidator = new ResponseValidator();

    private String receivedResponse;
    private String receivedResponse2;
    private List<String> multipleResponse = new ArrayList<>();

    @Given("que el servicio RabbitMQ está corriendo")
    public void rabbitMQServiceIsRunning() {
        given()
                .auth().basic("admin", "admin")
                .get("http://localhost:15672/api/queues")
                .then().statusCode(200);
    }

    @Given("la base de datos PostgreSQL está activa")
    public void databaseIsActive() {
        boolean isActive = DatabaseConnection.isDatabaseConnectionActive();
        Assert.assertTrue("La conexión a la base de datos no se pudo establecer.", isActive);
    }

    @When("se consulta al servicio RabbitMQ con nombre {string}")
    public void seConsultaAlServicioRabbitMQConNombre(String customerName) throws IOException {
        try {
            String publishPayload = "{ \"properties\": {}, \"routing_key\": \"query.is-customer-available\", " +
                    "\"payload\": \"{\\\"customer\\\": \\\"" + customerName + "\\\"}\", \"payload_encoding\": \"string\" }";
            rabbitMQService.limpiarCola();
            rabbitMQService.publicarMensaje(publishPayload);
            String responsePayload = "{\"count\":1,\"requeue\":false,\"encoding\":\"auto\",\"ackmode\":\"ack_requeue_false\"}";
            receivedResponse = rabbitMQService.obtenerRespuesta(responsePayload).getBody().asString();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al interactuar con RabbitMQ: " + e.getMessage());
        }

    }

    @Then("el sistema responde que el cliente está disponible")
    public void elSistemaRespondeQueElClienteEstaDisponible() {
         responseValidator.validarUnicoClienteDisponible(receivedResponse, "CUSTOMER1");
    }

    @Then("el sistema responde que el {string} no está disponible")
    public void elSistemaRespondeQueElClienteNoEstaDisponible(String costumerName) {
       responseValidator.validarUnicoClienteNoDisponible(receivedResponse,costumerName);
    }

    @When("se realizan solicitudes simultáneas para {string}, {string}, {string}, {string}")
    public void solicitudesConcurrentes(String customer1, String customer2, String customer3, String customer4) {
        String[] customers = {customer1, customer2, customer3, customer4};

        List<Thread> threads = new ArrayList<>();
        for (String customer : customers) {
            Thread thread = new Thread(() -> {
                try {
                    seConsultaAlServicioRabbitMQConNombre2(customer);
                } catch (IOException e) {
                    throw new RuntimeException("Error procesando cliente: " + customer, e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Error esperando a los hilos.", e);
            }
        }
    }

    @Then("el sistema responde que {string} está disponible")
    public void elSistemaRespondeQueEstaDisponibleYNoEstanDisponibles(String availableCustomer) throws InterruptedException {
        Assert.assertNotNull("No se recibió ninguna respuesta", multipleResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> multipleResponseJsonNode = new ArrayList<>();

        try {

            for (int i = 0; i < multipleResponse.size(); i++) {
                JsonNode rootNode = objectMapper.readTree(multipleResponse.get(i));
                JsonNode payloadNode = rootNode.get(0).path("payload");
                String payload = payloadNode.asText();
                String decodedPayload = URLDecoder.decode(payload, "UTF-8");
                JsonNode payloadJsonNode = objectMapper.readTree(decodedPayload);
                multipleResponseJsonNode.add(payloadJsonNode);
            }
            validarClienteDisponible(multipleResponseJsonNode, availableCustomer);
        } catch (Exception e) {
            Assert.fail("Error al procesar la respuesta: " + e.getMessage());
        }
    }
    @Then("{string} no están disponibles")
    public void elSistemaRespondeQueNoEstanDisponibles(String unavailableCustomers) {
        Assert.assertNotNull("No se recibió ninguna respuesta", multipleResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> multipleResponseJsonNode = new ArrayList<>();
        try {

            for (int i = 0; i < multipleResponse.size(); i++) {
                JsonNode rootNode = objectMapper.readTree(multipleResponse.get(i));
                JsonNode payloadNode = rootNode.get(0).path("payload");
                String payload = payloadNode.asText();
                String decodedPayload = URLDecoder.decode(payload, "UTF-8");
                JsonNode payloadJsonNode = objectMapper.readTree(decodedPayload);
                multipleResponseJsonNode.add(payloadJsonNode);
            }
            String[] unavailableCustomersArray = unavailableCustomers.split(",");
            for (String customer : unavailableCustomersArray) {
                validarClienteNoDisponible(multipleResponseJsonNode, customer.trim());
            }

        } catch (Exception e) {
            Assert.fail("Error al procesar la respuesta: " + e.getMessage());
        }
    }


    private void validarClienteDisponible(List<JsonNode> multipleResponseJsonNode, String customer) {
        boolean isUnavailable = multipleResponseJsonNode.stream()
                .anyMatch(payload -> {
                    try {
                        String payloadEscapado = payload.asText();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode payloadJsonNode = objectMapper.readTree(payloadEscapado);
                        JsonNode customerNode = payloadJsonNode.get("customer");
                        JsonNode isAvailableNode = payloadJsonNode.get("isAvailable");
                        if (customerNode == null || isAvailableNode == null) {
                            return false;
                        }
                        String customerValue = customerNode.asText();
                        boolean isAvailableValue = !isAvailableNode.asBoolean();
                        return customerValue.equals(customer) && !isAvailableValue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                });

        Assert.assertTrue("El cliente no disponible " + customer + " no fue encontrado en la respuesta.", isUnavailable);
    }



    private void validarClienteNoDisponible(List<JsonNode> multipleResponseJsonNode, String customer) {
        boolean isUnavailable = multipleResponseJsonNode.stream()
                .anyMatch(payload -> {
                    try {
                        String payloadEscapado = payload.asText();
                        String payloadDesescapado = URLDecoder.decode(payloadEscapado, "UTF-8");
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode payloadJsonNode = objectMapper.readTree(payloadDesescapado);
                        JsonNode customerNode = payloadJsonNode.get("customer");
                        JsonNode isAvailableNode = payloadJsonNode.get("isAvailable");
                        if (customerNode == null || isAvailableNode == null) {
                            return false;
                        }
                        String customerValue = customerNode.asText();
                        boolean isAvailableValue = isAvailableNode.asBoolean();
                        return customerValue.equals(customer) && !isAvailableValue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                });

        Assert.assertTrue("El cliente no disponible " + customer + " no fue encontrado en la respuesta.", isUnavailable);
    }


    public void seConsultaAlServicioRabbitMQConNombre2(String customerName) throws IOException {
        try {
            String publishPayload = "{ \"properties\": {}, \"routing_key\": \"query.is-customer-available\", " +
                    "\"payload\": \"{\\\"customer\\\": \\\"" + customerName + "\\\"}\", \"payload_encoding\": \"string\" }";
           rabbitMQService.publicarMensaje(publishPayload);
            String responsePayload = "{\"count\":1,\"requeue\":false,\"encoding\":\"auto\",\"ackmode\":\"ack_requeue_false\"}";
            Response response = rabbitMQService.obtenerRespuesta(responsePayload);
            multipleResponse.add(response.getBody().asString());
            if (response.statusCode() == 200 && !response.getBody().asString().isEmpty()) {
                receivedResponse2 = response.getBody().asString();
            } else {
                System.out.println("No se recibió respuesta de RabbitMQ.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al interactuar con RabbitMQ: " + e.getMessage());
        }

    }




}