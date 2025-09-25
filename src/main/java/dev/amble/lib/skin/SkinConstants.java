package dev.amble.lib.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.amble.lib.AmbleKit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Consumer;

public class SkinConstants {
	public static final String SKIN_URL = "https://mineskin.eu/skin/";

	public static String encodeURL(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hash = md.digest(input.getBytes());
	        StringBuilder hexString = new StringBuilder();
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException(e);
	    }
	}

	public static void fetchPlayerModel(String name, Consumer<Boolean> slimConsumer) {
		new Thread(() -> {
			String uuid;
			try {
				HttpClient httpClient = HttpClient.newHttpClient();
				HttpRequest uuidRequest = HttpRequest.newBuilder(
								URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
						.build();
				HttpResponse<String> uuidResponse = httpClient.send(uuidRequest, HttpResponse.BodyHandlers.ofString());
				JsonObject uuidJson = JsonParser.parseString(uuidResponse.body()).getAsJsonObject();
				if (uuidJson.has("id")) {
					uuid = uuidJson.get("id").getAsString();
				} else {
					slimConsumer.accept(false);
					return;
				}

				HttpRequest profileRequest = HttpRequest.newBuilder(
								URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
						.build();
				HttpResponse<String> profileResponse = httpClient.send(profileRequest, HttpResponse.BodyHandlers.ofString());
				JsonObject json = JsonParser.parseString(profileResponse.body()).getAsJsonObject();
				String base64 = json.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
				String decodedJson = new String(Base64.getDecoder().decode(base64));
				JsonObject decoded = JsonParser.parseString(decodedJson).getAsJsonObject();
				String model = "unknown";
				if (decoded.has("textures")) {
					JsonObject textures = decoded.getAsJsonObject("textures");
					if (textures.has("SKIN")) {
						JsonObject skin = textures.getAsJsonObject("SKIN");
						if (skin.has("metadata")) {
							JsonObject metadata = skin.getAsJsonObject("metadata");
							if (metadata.has("model")) {
								model = metadata.get("model").getAsString();
							}
						}
					}
				}

				AmbleKit.LOGGER.info("Fetched model for {}: {}", name, model);
				slimConsumer.accept(model.equalsIgnoreCase("slim"));
				return;
			} catch (InterruptedException | IOException e) {
				AmbleKit.LOGGER.error(String.valueOf(e));
			}
			slimConsumer.accept(false);
		}, AmbleKit.MOD_ID + "-ModelFetch").start();
	}
}
