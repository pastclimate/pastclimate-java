package com.pastclimate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class VersionTest {
    @Test
    void versionMatchesThePom() throws IOException {
        String pom = new String(Files.readAllBytes(Paths.get("pom.xml")), java.nio.charset.StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("<artifactId>pastclimate</artifactId>\\s*<version>([^<]+)</version>").matcher(pom);
        assertTrue(matcher.find(), "could not find the project version in pom.xml");
        assertEquals(matcher.group(1), Version.VERSION);
    }
}
