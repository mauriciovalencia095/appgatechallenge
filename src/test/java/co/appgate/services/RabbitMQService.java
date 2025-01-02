package co.appgate.services;

import io.restassured.response.Response;
import io.restassured.http.ContentType;
import co.appgate.utils.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class RabbitMQService {

    private static final String BASE_URI = ConfigManager.get("rabbitmq.baseUri");
    private static final String USERNAME = ConfigManager.get("rabbitmq.username");
    private static final String PASSWORD = ConfigManager.get("rabbitmq.password");
    private static final Logger log = LoggerFactory.getLogger(RabbitMQService.class);


    public void limpiarCola() {
        given()
                .baseUri("http://localhost:15672")
                .basePath("/api/queues/%2F/testing.customer.response.is-customer-available/contents")
                .urlEncodingEnabled(false)
                .auth().basic("admin", "admin")
                .delete()
                .then()
                .statusCode(204);
    }

    public Response publicarMensaje(String payload) {


        return given()
                .baseUri(BASE_URI)
                .basePath(ConfigManager.get("rabbitmq.publishPath"))
                .urlEncodingEnabled(false)
                .auth().basic(USERNAME, PASSWORD)
                .header("Content-Type", "application/json")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept", "*/*")
                .body(payload)
                .when()
                .post();

    }

    public Response obtenerRespuesta(String payload) {
        return given()
                .baseUri("http://localhost:15672") // Base URI
                .basePath("/api/queues/%2F/testing.customer.response.is-customer-available/get") // Path completo codificado manualmente
                .urlEncodingEnabled(false) // Desactivar codificación automática
                .contentType(ContentType.JSON) // Content-Type
                .auth().basic("admin", "admin") // Autenticación básica
                .body(payload) // Cuerpo de la solicitud
                .when()
                .post() // Método POST
                .then()
                .extract().response();
    }
}