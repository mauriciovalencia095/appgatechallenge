package co.appgate.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.net.URLDecoder;
import java.util.List;

public class ResponseValidator {

    private ObjectMapper objectMapper = new ObjectMapper();

    public boolean validarClienteDisponible(List<String> responses, String customer) {
        return responses.stream().anyMatch(response -> {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode payloadNode = rootNode.get(0).get("payload");
                String payload = payloadNode.asText();

                String decodedPayload = URLDecoder.decode(payload, "UTF-8");
                JsonNode payloadJsonNode = objectMapper.readTree(decodedPayload);
                String customerValue = payloadJsonNode.get("customer").asText();
                boolean isAvailable = payloadJsonNode.get("isAvailable").asBoolean();

                return customerValue.equals(customer) && isAvailable;
            } catch (Exception e) {
                return false;
            }
        });
    }


    public void validarUnicoClienteDisponible(String response, String customer) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode firstNode = rootNode.get(0);
                JsonNode payloadNode = firstNode.get("payload");
                Assert.assertNotNull("El campo 'payload' no está presente en la respuesta", payloadNode);

                String payload = payloadNode.asText();
                System.out.println("Payload extraído: " + payload);

                Assert.assertTrue("El cliente no está disponible",
                        payload.contains("{\\\"customer\\\":\\\"CUSTOMER1\\\",\\\"isAvailable\\\":true}"));
            } else {
                Assert.fail("El JSON recibido no es un array o está vacío.");
            }
        } catch (Exception e) {
            Assert.fail("Error al procesar la respuesta: " + e.getMessage());
        }
    }

    public boolean validarClienteNoDisponible(List<String> responses, String customer) {
        return responses.stream().anyMatch(response -> {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode payloadNode = rootNode.get(0).get("payload");
                String payload = payloadNode.asText();

                String decodedPayload = URLDecoder.decode(payload, "UTF-8");
                JsonNode payloadJsonNode = objectMapper.readTree(decodedPayload);
                String customerValue = payloadJsonNode.get("customer").asText();
                boolean isAvailable = payloadJsonNode.get("isAvailable").asBoolean();

                return customerValue.equals(customer) && !isAvailable;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void validarUnicoClienteNoDisponible(String response, String customer) {
        Assert.assertNotNull("No se recibió ninguna respuesta", response);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);


            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode firstNode = rootNode.get(0);
                JsonNode payloadNode = firstNode.get("payload");

                Assert.assertNotNull("El campo 'payload' no está presente en la respuesta", payloadNode);

                String payload = payloadNode.asText(); // Obtén el valor como String
                System.out.println("Payload extraído: " + payload);


                Assert.assertTrue("El cliente no está disponible",
                        payload.contains("{\\\"customer\\\":\\\"" + customer + "\\\",\\\"isAvailable\\\":false}"));
            } else {
                Assert.fail("El JSON recibido no es un array o está vacío.");
            }
        } catch (Exception e) {
            Assert.fail("Error al procesar la respuesta: " + e.getMessage());
        }
    }
}