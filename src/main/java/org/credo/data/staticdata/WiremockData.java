package org.credo.data.staticdata;

import java.nio.file.Paths;

public class WiremockData {
    public static final String PROJECT_ROOT = System.getProperty("user.dir");

    public static final String MAPPINGS_DIR = Paths.get(
            PROJECT_ROOT, "src", "main", "resources", "wiremock"
    ).toString(),
            CONTAINER_NAME = "wiremock",START_DOCKER_COMMAND = "docker run --rm -d --name %s -p 8080:8080 -v %s:/home/wiremock wiremock/wiremock:2.35.0",
    DOCKER_STOP_COMMAND = "docker stop wiremock";
}
