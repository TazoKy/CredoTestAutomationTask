package org.credo.utils.wiremock;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.restassured.RestAssured.given;


public class WireMockUtils {
    private static final Logger logger = LogManager.getLogger(WireMockUtils.class);
    public static void registerStubs(List<String> jsonFileNames) throws Exception {
        String projectRoot = System.getProperty("user.dir");

        for (String fileName : jsonFileNames) {
            Path filePath = Paths.get(projectRoot, "src", "main", "java",
                    "org", "credo", "data", "mappings", fileName);

            String stubJson = Files.readString(filePath);

            Response response = given().header("Content-Type", "application/json")
                    .body(stubJson)
                    .post("http://localhost:8080/__admin/mappings");
            Assert.assertEquals(response.statusCode(), 201);
            logger.info("Mapping  ,{}, successfully",fileName);
        }

    }
}