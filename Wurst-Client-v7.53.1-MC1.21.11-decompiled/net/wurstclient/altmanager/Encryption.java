package net.wurstclient.altmanager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.class_128;
import net.minecraft.class_129;
import net.minecraft.class_148;
import net.minecraft.class_156;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;
import net.wurstclient.util.json.WsonObject;

public final class Encryption {
    private static final String CHARSET = "UTF-8";
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public Encryption(Path encFolder) throws IOException {
        this.createEncryptionFolder(encFolder);
        KeyPair rsaKeyPair = this.getRsaKeyPair(encFolder.resolve("wurst_rsa_public.txt"), encFolder.resolve("wurst_rsa_private.txt"));
        SecretKey aesKey = this.getAesKey(encFolder.resolve("wurst_aes.txt"), rsaKeyPair);
        try {
            this.encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            this.encryptCipher.init(1, (Key)aesKey, new IvParameterSpec(aesKey.getEncoded()));
            this.decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            this.decryptCipher.init(2, (Key)aesKey, new IvParameterSpec(aesKey.getEncoded()));
        }
        catch (GeneralSecurityException e) {
            throw new class_148(class_128.method_560((Throwable)e, (String)"Creating AES ciphers"));
        }
    }

    private Path createEncryptionFolder(Path encFolder) throws IOException {
        Files.createDirectories(encFolder, new FileAttribute[0]);
        if (class_156.method_668() == class_156.class_158.field_1133) {
            Files.setAttribute(encFolder, "dos:hidden", true, new LinkOption[0]);
        }
        Path readme = encFolder.resolve("READ ME I AM VERY IMPORTANT.txt");
        String readmeText = "DO NOT SHARE THESE FILES WITH ANYONE!\r\nThey are encryption keys that protect your alt list file from being read by someone else.\r\nIf someone is asking you to send these files, they are 100% trying to scam you.\r\n\r\nDO NOT EDIT, RENAME OR DELETE THESE FILES! (unless you know what you're doing)\r\nIf you do, Wurst's Alt Manager can no longer read your alt list and will replace it with a blank one.\r\nIn other words, YOUR ALT LIST WILL BE DELETED.";
        Files.write(readme, readmeText.getBytes(CHARSET), StandardOpenOption.CREATE);
        return encFolder;
    }

    public static Path chooseEncryptionFolder() {
        Path homeEncFolder;
        String userHome = System.getProperty("user.home");
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        String encFolderName = ".Wurst encryption";
        Path encFolder = homeEncFolder = Paths.get(userHome, encFolderName).normalize();
        if (xdgDataHome != null && !xdgDataHome.isEmpty() && !Files.exists(encFolder = Paths.get(xdgDataHome, encFolderName).normalize(), new LinkOption[0]) && Files.isDirectory(homeEncFolder, new LinkOption[0])) {
            Encryption.migrateEncryptionFolder(homeEncFolder, encFolder);
        }
        return encFolder;
    }

    public static void migrateEncryptionFolder(Path oldFolder, Path newFolder) {
        System.out.println("Migrating encryption folder from " + String.valueOf(oldFolder) + " to " + String.valueOf(newFolder));
        try {
            File[] oldFiles;
            Files.createDirectories(newFolder, new FileAttribute[0]);
            for (File oldFile : oldFiles = oldFolder.toFile().listFiles()) {
                Path fileDestination = newFolder.resolve(oldFile.getName());
                Files.copy(oldFile.toPath(), fileDestination, new CopyOption[0]);
            }
            for (File oldFile : oldFiles) {
                oldFile.delete();
            }
            Files.deleteIfExists(oldFolder);
        }
        catch (IOException e) {
            class_128 report = class_128.method_560((Throwable)e, (String)"Migrating Wurst encryption folder");
            class_129 section = report.method_562("Migration");
            section.method_578("Old path", (Object)oldFolder);
            section.method_578("New path", (Object)newFolder);
            throw new class_148(report);
        }
    }

    public byte[] decrypt(byte[] bytes) {
        try {
            return this.decryptCipher.doFinal(Base64.getDecoder().decode(bytes));
        }
        catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new class_148(class_128.method_560((Throwable)e, (String)"Decrypting bytes"));
        }
    }

    public String loadEncryptedFile(Path path) throws IOException {
        try {
            return new String(this.decrypt(Files.readAllBytes(path)), CHARSET);
        }
        catch (class_148 e) {
            throw new IOException(e);
        }
    }

    public JsonElement parseFile(Path path) throws IOException, JsonException {
        JsonElement jsonElement;
        block8: {
            BufferedReader reader = Files.newBufferedReader(path);
            try {
                jsonElement = JsonParser.parseString(this.loadEncryptedFile(path));
                if (reader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (JsonParseException e) {
                    throw new JsonException(e);
                }
            }
            reader.close();
        }
        return jsonElement;
    }

    public WsonArray parseFileToArray(Path path) throws IOException, JsonException {
        JsonElement json = this.parseFile(path);
        if (!json.isJsonArray()) {
            throw new JsonException();
        }
        return new WsonArray(json.getAsJsonArray());
    }

    public WsonObject parseFileToObject(Path path) throws IOException, JsonException {
        JsonElement json = this.parseFile(path);
        if (!json.isJsonObject()) {
            throw new JsonException();
        }
        return new WsonObject(json.getAsJsonObject());
    }

    public byte[] encrypt(byte[] bytes) {
        try {
            return Base64.getEncoder().encode(this.encryptCipher.doFinal(bytes));
        }
        catch (GeneralSecurityException e) {
            throw new class_148(class_128.method_560((Throwable)e, (String)"Encrypting bytes"));
        }
    }

    public void saveEncryptedFile(Path path, String content) throws IOException {
        try {
            Files.write(path, this.encrypt(content.getBytes(CHARSET)), new OpenOption[0]);
        }
        catch (class_148 e) {
            throw new IOException(e);
        }
    }

    public void toEncryptedJson(JsonObject json, Path path) throws IOException, JsonException {
        try {
            this.saveEncryptedFile(path, JsonUtils.PRETTY_GSON.toJson(json));
        }
        catch (JsonParseException e) {
            throw new JsonException(e);
        }
    }

    private KeyPair getRsaKeyPair(Path publicFile, Path privateFile) throws IOException {
        if (Files.notExists(publicFile, new LinkOption[0]) || Files.notExists(privateFile, new LinkOption[0])) {
            return this.createRsaKeys(publicFile, privateFile);
        }
        try {
            return this.loadRsaKeys(publicFile, privateFile);
        }
        catch (IOException | ReflectiveOperationException | GeneralSecurityException e) {
            System.err.println("Couldn't load RSA keypair!");
            e.printStackTrace();
            return this.createRsaKeys(publicFile, privateFile);
        }
    }

    private SecretKey getAesKey(Path path, KeyPair pair) throws IOException {
        if (Files.notExists(path, new LinkOption[0])) {
            return this.createAesKey(path, pair);
        }
        try {
            return this.loadAesKey(path, pair);
        }
        catch (IOException | GeneralSecurityException e) {
            System.err.println("Couldn't load AES key!");
            e.printStackTrace();
            return this.createAesKey(path, pair);
        }
    }

    private KeyPair createRsaKeys(Path publicFile, Path privateFile) throws IOException {
        try {
            KeySpec keySpec;
            System.out.println("Generating RSA keypair.");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            KeyFactory factory = KeyFactory.getInstance("RSA");
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(publicFile, new OpenOption[0]));){
                keySpec = factory.getKeySpec(pair.getPublic(), RSAPublicKeySpec.class);
                out.writeObject(((RSAPublicKeySpec)keySpec).getModulus());
                out.writeObject(((RSAPublicKeySpec)keySpec).getPublicExponent());
            }
            out = new ObjectOutputStream(Files.newOutputStream(privateFile, new OpenOption[0]));
            try {
                keySpec = factory.getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);
                out.writeObject(((RSAPrivateKeySpec)keySpec).getModulus());
                out.writeObject(((RSAPrivateKeySpec)keySpec).getPrivateExponent());
            }
            finally {
                out.close();
            }
            return pair;
        }
        catch (GeneralSecurityException e) {
            throw new class_148(class_128.method_560((Throwable)e, (String)"Creating RSA keypair"));
        }
    }

    private SecretKey createAesKey(Path path, KeyPair pair) throws IOException {
        try {
            System.out.println("Generating AES key.");
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            SecretKey key = keygen.generateKey();
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(1, pair.getPublic());
            Files.write(path, rsaCipher.doFinal(key.getEncoded()), new OpenOption[0]);
            return key;
        }
        catch (GeneralSecurityException e) {
            throw new class_148(class_128.method_560((Throwable)e, (String)"Creating AES key"));
        }
    }

    private KeyPair loadRsaKeys(Path publicFile, Path privateFile) throws GeneralSecurityException, ReflectiveOperationException, IOException {
        PrivateKey privateKey;
        PublicKey publicKey;
        KeyFactory factory = KeyFactory.getInstance("RSA");
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(publicFile, new OpenOption[0]));){
            publicKey = factory.generatePublic(new RSAPublicKeySpec((BigInteger)in.readObject(), (BigInteger)in.readObject()));
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(privateFile, new OpenOption[0]));){
            privateKey = factory.generatePrivate(new RSAPrivateKeySpec((BigInteger)in.readObject(), (BigInteger)in.readObject()));
        }
        return new KeyPair(publicKey, privateKey);
    }

    private SecretKey loadAesKey(Path path, KeyPair pair) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, pair.getPrivate());
        return new SecretKeySpec(cipher.doFinal(Files.readAllBytes(path)), "AES");
    }
}
