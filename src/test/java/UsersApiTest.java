import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.credo.data.dataproviders.UserAPITestData;
import org.credo.data.staticdata.WiremockData;
import org.credo.models.User;
import org.credo.utils.dbutils.SQLiteHelper;

import org.credo.utils.wiremock.WireMockUtils;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;


public class UsersApiTest {

    private static final Logger logger = LogManager.getLogger(UsersApiTest.class);

    @BeforeSuite
    public void startWireMock() throws IOException, InterruptedException {
        String command = String.format(
                WiremockData.START_DOCKER_COMMAND,
                WiremockData.CONTAINER_NAME, WiremockData.MAPPINGS_DIR
        );
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        logger.info("Wiremock started successfully");

        Thread.sleep(5000);

        RestAssured.baseURI = "http://localhost:8080";
        given()
                .post("http://localhost:8080/__admin/mappings/reset")
                .then()
                .statusCode(200);


        try {
            WireMockUtils.registerStubs(List.of(
                    "get-all-users.json",
                    "get-users-gender-male.json",
                    "get-users-gender-female.json",
                    "get-users-unknown-gender.json",
                    "get-users-internal-server-error.json",
                    "get-users-age-invalid.json",
                    "get-users-age-30.json",
                    "get-users-age-25.json"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Thread.sleep(5000);
    }


    @AfterSuite
    public void stopWireMock() throws IOException, InterruptedException {
        Process stop = Runtime.getRuntime().exec(WiremockData.DOCKER_STOP_COMMAND);
        stop.waitFor();
        logger.info("Wiremock stopped successfully");
    }



    @Test(dataProvider = "positiveStubs", dataProviderClass = UserAPITestData.class)
    public void testPositiveStubs(String endpoint, int expectedStatus, String testName, List<User> expectedUsers, Integer expectedAge, String expectedGender) throws JsonProcessingException {
        logger.info("executing test: {}" ,testName);
        Response response = given()
                .when()
                .get(RestAssured.baseURI + endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response();



        User[] users = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(response.asString(), User[].class);
        List<User> userList = Arrays.asList(users);

        if (expectedUsers != null) {
            Assert.assertEquals(userList.size(), expectedUsers.size(), "User list size mismatch");
            for (User expected : expectedUsers) {
                boolean found = userList.stream().anyMatch(u ->
                        u.getId() == expected.getId() &&
                                u.getName().equals(expected.getName()) &&
                                u.getAge() == expected.getAge() &&
                                u.getGender().equals(expected.getGender())
                );
                Assert.assertTrue(found, "Expected user not found: " + expected.getName());
            }
        }

        if (expectedAge != null) {
            for (User user : userList) {
                Assert.assertEquals(user.getAge(), expectedAge,
                        "User age mismatch for user: " + user.getName());
            }
        }

        if (expectedGender != null) {
            for (User user : userList) {
                Assert.assertEquals(user.getGender(), expectedGender,
                        "User gender mismatch for user: " + user.getName());
            }
        }
    }



    @Test(dataProvider = "negativeStubs", dataProviderClass = UserAPITestData.class)
    public void testNegativeStubs(String endpoint, int expectedStatus, String testName) {
        logger.info("executing test : {}" ,testName);
        Response response = given()
                .when()
                .get(RestAssured.baseURI + endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response();



        logger.info(response.asString());
    }

    @AfterMethod
    public void recordResult(ITestResult result) {
        Object[] params = result.getParameters();
        String testName = (params.length > 2) ? (String) params[2] : result.getMethod().getMethodName();
        String status = (result.getStatus() == ITestResult.SUCCESS) ? "PASSED" : "FAILED";

        SQLiteHelper.saveTestResult(testName, status);
        System.out.println("Recorded result: " + testName + " -> " + status);
    }
}
