package org.credo.data.dataproviders;

import org.credo.models.User;
import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;

public class UserAPITestData {

    @DataProvider(name = "positiveStubs")
    public Object[][] positiveStubs() {
        User alice = User.builder().id(1).name("Alice").age(30).gender("female").build();
        User bob   = User.builder().id(2).name("Bob").age(25).gender("male").build();
        List<User> allUsers = Arrays.asList(alice, bob);

        return new Object[][] {
                { "/users", 200, "testGetAllUsers_Positive", allUsers, null, null },
                { "/users?age=25", 200, "testFilterByAge_Positive", null, 25, null },
                { "/users?gender=male", 200, "testFilterByGender_Positive", null, null, "male" }
        };
    }


    @DataProvider(name = "negativeStubs")
    public static Object[][] negativeStubs() {
        return new Object[][] {
            {"/users?age=-1", 400, "testInvalidAge_Negative"},
            {"/users?simulateError=true", 500, "testInternalServerError_Negative"},
            {"/users?gender=unknown", 422, "testInvalidGender_Negative"}
        };
    }
}
