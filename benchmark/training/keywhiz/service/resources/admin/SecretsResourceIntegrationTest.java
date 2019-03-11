/**
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package keywhiz.service.resources.admin;


import DbSeedCommand.defaultPassword;
import DbSeedCommand.defaultUser;
import KeywhizClient.ConflictException;
import KeywhizClient.NotFoundException;
import KeywhizClient.UnauthorizedException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import keywhiz.IntegrationTestRule;
import keywhiz.api.SecretDetailResponse;
import keywhiz.api.model.SanitizedSecret;
import keywhiz.client.KeywhizClient;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;


public class SecretsResourceIntegrationTest {
    KeywhizClient keywhizClient;

    @ClassRule
    public static final RuleChain chain = IntegrationTestRule.rule();

    @Test
    public void listsSecrets() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        assertThat(keywhizClient.allSecrets().stream().map(SanitizedSecret::name).toArray()).contains("Nobody_PgPass", "Hacking_Password", "General_Password", "NonexistentOwner_Pass", "Versioned_Password");
    }

    @Test
    public void listingExcludesSecretContent() throws IOException {
        // This is checking that the response body doesn't contain the secret information anywhere, not
        // just that the resulting Java objects parsed by gson don't.
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        List<SanitizedSecret> sanitizedSecrets = keywhizClient.allSecrets();
        assertThat(sanitizedSecrets.toString()).doesNotContain("MTMzNw==").doesNotContain(new String(Base64.getDecoder().decode("MTMzNw=="), StandardCharsets.UTF_8));
    }

    @Test(expected = UnauthorizedException.class)
    public void adminRejectsNonKeywhizUsers() throws IOException {
        keywhizClient.login("username", "password".toCharArray());
        keywhizClient.allSecrets();
    }

    @Test(expected = UnauthorizedException.class)
    public void adminRejectsWithoutCookie() throws IOException {
        keywhizClient.allSecrets();
    }

    @Test
    public void createsSecret() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        SecretDetailResponse secretDetails = keywhizClient.createSecret("newSecret", "", "content".getBytes(StandardCharsets.UTF_8), ImmutableMap.of(), 0);
        assertThat(secretDetails.name).isEqualTo("newSecret");
        assertThat(keywhizClient.allSecrets().stream().map(SanitizedSecret::name).toArray()).contains("newSecret");
    }

    @Test(expected = ConflictException.class)
    public void rejectsCreatingDuplicateSecretWithoutVersion() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        keywhizClient.createSecret("passage", "v1", "content".getBytes(StandardCharsets.UTF_8), ImmutableMap.of(), 0);
        keywhizClient.createSecret("passage", "v2", "content".getBytes(StandardCharsets.UTF_8), ImmutableMap.of(), 0);
    }

    @Test
    public void deletesSecret() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        keywhizClient.deleteSecretWithId(739);
        try {
            keywhizClient.secretDetailsForId(739);
            failBecauseExceptionWasNotThrown(NotFoundException.class);
        } catch (KeywhizClient e) {
            // Secret was successfully deleted
        }
    }

    @Test
    public void listsSpecificSecret() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        SecretDetailResponse response = keywhizClient.secretDetailsForId(737);
        assertThat(response.name).isEqualTo("Nobody_PgPass");
    }

    @Test
    public void listSpecificNonVersionedSecretByName() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        SanitizedSecret sanitizedSecret = keywhizClient.getSanitizedSecretByName("Nobody_PgPass");
        assertThat(sanitizedSecret.id()).isEqualTo(737);
    }

    @Test(expected = NotFoundException.class)
    public void notFoundOnBadSecretId() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        keywhizClient.secretDetailsForId(283092384);
    }

    @Test(expected = NotFoundException.class)
    public void notFoundOnBadSecretName() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        keywhizClient.getSanitizedSecretByName("non-existent-secret");
    }

    @Test
    public void doesNotRetrieveDeletedSecretVersions() throws IOException {
        keywhizClient.login(defaultUser, defaultPassword.toCharArray());
        String name = "versionSecret";
        // Create a secret
        SecretDetailResponse secretDetails = keywhizClient.createSecret(name, "first secret", "content".getBytes(StandardCharsets.UTF_8), ImmutableMap.of(), 0);
        assertThat(secretDetails.name).isEqualTo(name);
        assertThat(keywhizClient.allSecrets().stream().map(SanitizedSecret::name).toArray()).contains(name);
        // Retrieve versions for the first secret
        List<SanitizedSecret> versions = keywhizClient.listSecretVersions(name, 0, 10);
        assertThat(versions.size()).isEqualTo(1);
        assertThat(versions.get(0).description()).isEqualTo("first secret");
        // Delete this first secret
        keywhizClient.deleteSecretWithId(secretDetails.id);
        assertThat(keywhizClient.allSecrets().stream().map(SanitizedSecret::name).toArray()).doesNotContain(name);
        // Create a second secret with the same name
        secretDetails = keywhizClient.createSecret(name, "second secret", "content".getBytes(StandardCharsets.UTF_8), ImmutableMap.of(), 0);
        assertThat(secretDetails.name).isEqualTo(name);
        // Retrieve versions for the second secret and check that the first secret's version is not included
        versions = keywhizClient.listSecretVersions(name, 0, 10);
        assertThat(versions.size()).isEqualTo(1);
        assertThat(versions.get(0).description()).isEqualTo("second secret");
    }
}
