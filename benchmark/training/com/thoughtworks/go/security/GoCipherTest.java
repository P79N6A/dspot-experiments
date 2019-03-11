/**
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.security;


import com.thoughtworks.go.util.SystemEnvironment;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;


public class GoCipherTest {
    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ResetCipher resetCipher = new ResetCipher();

    private File desCipherFile;

    private File aesCipherFile;

    private SystemEnvironment systemEnvironment;

    @Test
    public void shouldCreateAnAESCipherFileWithTheCipherIfNotFound() throws CryptoException, IOException {
        assertThat(desCipherFile.exists()).isFalse();
        assertThat(aesCipherFile.exists()).isFalse();
        GoCipher goCipher = new GoCipher(systemEnvironment);
        assertThat(desCipherFile.exists()).isFalse();
        assertThat(aesCipherFile.exists()).isTrue();
        String plainText = goCipher.decrypt(goCipher.encrypt("user-password!"));
        assertThat(plainText).isEqualTo("user-password!");
        assertThat(desCipherFile.exists()).isFalse();
        assertThat(aesCipherFile.exists()).isTrue();
    }

    @Test
    public void shouldWorkEvenAfterCipherFileHasBeenDeleted() throws CryptoException, IOException {
        // serialization friendliness
        resetCipher.setupAESCipherFile();
        resetCipher.setupDESCipherFile();
        GoCipher goCipher = new GoCipher(systemEnvironment);
        FileUtils.deleteQuietly(desCipherFile);
        FileUtils.deleteQuietly(aesCipherFile);
        assertThat(desCipherFile.exists()).isFalse();
        assertThat(aesCipherFile.exists()).isFalse();
        String plainText = goCipher.decrypt(goCipher.encrypt("user-password!"));
        assertThat(plainText).isEqualTo("user-password!");
        assertThat(desCipherFile.exists()).isFalse();
        assertThat(aesCipherFile.exists()).isFalse();
    }

    @Test
    public void shouldNotCreateDesCipherIfCipherFileMissing() {
        assertThat(desCipherFile).doesNotExist();
        GoCipher goCipher = new GoCipher(systemEnvironment);
        assertThat(goCipher.aesEncrypter).isNotNull();
        assertThat(goCipher.desEncrypter).isNull();
    }

    @Test
    public void shouldCreateDesEncryptorIfCipherFileIsPresent() throws IOException {
        resetCipher.setupDESCipherFile();
        assertThat(desCipherFile).exists();
        GoCipher goCipher = new GoCipher(systemEnvironment);
        assertThat(goCipher.aesEncrypter).isNotNull();
        assertThat(goCipher.desEncrypter).isNotNull();
    }

    @Test
    public void shouldConvertFromDESEncryptedTextToAES() throws CryptoException, IOException {
        resetCipher.setupAESCipherFile();
        resetCipher.setupDESCipherFile();
        GoCipher goCipher = new GoCipher(systemEnvironment);
        String cipherText = goCipher.desToAES("mvcX9yrQsM4iPgm1tDxN1A==");
        assertThat(cipherText).startsWith("AES:");
    }
}
