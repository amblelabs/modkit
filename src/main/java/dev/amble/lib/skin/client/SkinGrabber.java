package dev.amble.lib.skin.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import dev.amble.lib.skin.ConcurrentQueueMap;
import dev.amble.lib.skin.SkinConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.DefaultSkinHelper;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import dev.amble.lib.AmbleKit;

/**
 * Some of this code is referenced from jeryn's regeneration mod, love u craig
 */
@Environment(EnvType.CLIENT)
public class SkinGrabber {
    public static final SkinGrabber INSTANCE = new SkinGrabber();
	public static final String DEFAULT_DIR = "./" + AmbleKit.MOD_ID + "/";
    public static final String SKIN_DIR = DEFAULT_DIR + "/skins/";
    private static final Identifier MISSING = new Identifier(AmbleKit.MOD_ID, "textures/skins/error.png");
    private static final String USER_AGENT = AmbleKit.MOD_ID + "/1.0";

    private final ConcurrentHashMap<String, Identifier> downloads;
    private final ConcurrentHashMap<String, String> urls;
    private final ConcurrentQueueMap<String, String> downloadQueue;
    private final SkinCache cache;
    public final JerynSkins jeryn;

    private int ticks;
    private boolean connection;

    private SkinGrabber() {
        downloads = new ConcurrentHashMap<>();
        urls = new ConcurrentHashMap<>();
        downloadQueue = new ConcurrentQueueMap<>();
        cache = new SkinCache();
        jeryn = new JerynSkins(new ArrayList<>());
    }

    public static Identifier missing() {
        if (MinecraftClient.getInstance().player == null) {
            return MISSING;
        }
        return DefaultSkinHelper.getTexture(MinecraftClient.getInstance().player.getUuid());
    }

	public static boolean isMissingTexture(Identifier id) {
		return id == null || id.equals(missing());
	}

	public List<String> getAllKeys() {
        return List.copyOf(downloads.keySet());
    }

    // These functions were coped from HttpTexture by Mojang (thak you moywang(
    @Nullable private static NativeImage processLegacySkin(NativeImage image) {
        int i = image.getHeight();
        int j = image.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean flag = i == 32;
            if (flag) {
                NativeImage nativeimage = new NativeImage(64, 64, true);
                nativeimage.copyFrom(image);
                image.close();
                image = nativeimage;
                nativeimage.fillRect(0, 32, 64, 32, 0);
                nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            setNoAlpha(image, 0, 0, 32, 16);
            if (flag) {
                doNotchTransparencyHack(image, 32, 0, 64, 32);
            }

            setNoAlpha(image, 0, 16, 64, 32);
            setNoAlpha(image, 16, 48, 48, 64);
            return image;
        } else {
            image.close();
            AmbleKit.LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture", j, i);
            return null;
        }
    }

    private static void doNotchTransparencyHack(NativeImage p_118013_, int p_118014_, int p_118015_, int p_118016_, int p_118017_) {
        for (int i = p_118014_; i < p_118016_; ++i) {
            for (int j = p_118015_; j < p_118017_; ++j) {
                int k = p_118013_.getColor(i, j);
                if ((k >> 24 & 255) < 128) {
                    return;
                }
            }
        }

        for (int l = p_118014_; l < p_118016_; ++l) {
            for (int i1 = p_118015_; i1 < p_118017_; ++i1) {
                p_118013_.setColor(l, i1, p_118013_.getColor(l, i1) & 16777215);
            }
        }

    }

    private static void setNoAlpha(NativeImage p_118023_, int p_118024_, int p_118025_, int p_118026_, int p_118027_) {
        for (int i = p_118024_; i < p_118026_; ++i) {
            for (int j = p_118025_; j < p_118027_; ++j) {
                p_118023_.setColor(i, j, p_118023_.getColor(i, j) | -16777216);
            }
        }
    }

    /**
     * Get the skin for a player
     * This will download the skin if it doesn't exist
     *
     * @param name The name of the player
     * @return The skin, or a missing texture if it doesn't exist / is downloading
     */
    public Identifier getSkin(String name) {
        return getSkinOrDownload(name, SkinConstants.SKIN_URL + name);
    }

    public Optional<Identifier> getPossibleSkin(String id) {
        id = id.toLowerCase().replace(" ", "_");

        if (downloads.containsKey(id)) {
            return Optional.of(downloads.get(id));
        }

        return Optional.empty();
    }

    public Identifier getSkinOrDownload(String id, String url) {
        id = id.toLowerCase().replace(" ", "_");

        Identifier existing = getPossibleSkin(id).orElse(null);
        if (existing != null) {
            return existing;
        }

        if (downloadQueue.get(id) != null) {
            return missing();
        }

        url = url.toLowerCase().replace(" ", "_");
        this.enqueueDownload(id, url);
        return missing();
    }

    public String getUrl(String key) {
        return urls.get(key);
    }

    private Identifier registerSkin(String name) {
        // register new skin to prepare
        File file = new File(SKIN_DIR + name.toLowerCase().replace(" ", "_") + ".png");
        Identifier location = fileToLocation(file);
        downloads.put(name, location);
        return location;
    }

    public void clearTextures() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.world == null) {
            TextureManager manager = minecraft.getTextureManager();

            // Release the textures if the cache isnt empty
            if (!this.downloads.isEmpty()) {
                this.downloads.forEach((key, value) -> manager.destroyTexture(value));
                this.downloads.clear();
            }
        }
    }

    private Identifier fileToLocation(File file) {
        NativeImage image;
        try {
            image = processLegacySkin(NativeImage.read(new FileInputStream(file)));
        } catch (IOException e) {
            AmbleKit.LOGGER.error("Failed to load Identifier from file", e);
            return missing();
        }
        if (image == null) {
            return missing();
        }
        return registerImage(image);
    }

    private Identifier registerImage(NativeImage image) {
        TextureManager manager = MinecraftClient.getInstance().getTextureManager();
        return manager.registerDynamicTexture("player_", new NativeImageBackedTexture(image));
    }

    private static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void downloadImageFromURL(String filename, File filepath, String URL) throws IOException {
        URL url = new URL(URL);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.connect();

        // Print all headers
        Map<String, List<String>> headers = connection.getHeaderFields();
        AmbleKit.LOGGER.info("Headers for {}:", filename);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            AmbleKit.LOGGER.info("{}: {}", entry.getKey(), entry.getValue());
        }

        String variant = connection.getHeaderField("variant");
        if (variant != null && (variant.equalsIgnoreCase("classic") || variant.equalsIgnoreCase("slim"))) {
            AmbleKit.LOGGER.info("Skin variant for {}: {}", filename, variant);
            writeVariantToJson(filename, variant);
        } else {
            writeVariantToJson(filename, "unknown");
        }

        BufferedImage image = ImageIO.read(connection.getInputStream());

        if (!filepath.exists()) {
            filepath.mkdirs();
        }

        ImageIO.write(image, "png", new File(filepath, filename + ".png"));
    }

    private void writeVariantToJson(String filename, String variant) {
        File jsonFile = new File(SKIN_DIR, "variants.json");
        Map<String, String> variants = new HashMap<>();
        if (jsonFile.exists()) {
            try (Reader reader = new FileReader(jsonFile)) {
                variants.putAll(new GsonBuilder().create().fromJson(reader, new TypeToken<HashMap<String, String>>(){}.getType()));
            } catch (IOException e) {
                AmbleKit.LOGGER.error("Failed to read variants.json", e);
            }
        }
        variants.put(filename, variant);
        try (Writer writer = new FileWriter(jsonFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(variants, writer);
        } catch (IOException e) {
            AmbleKit.LOGGER.error("Failed to write variants.json", e);
        }
    }

    public String getVariantFromName(String filename) {
        File jsonFile = new File(SKIN_DIR, "variants.json");
        if (!jsonFile.exists()) return "unknown";
        try (Reader reader = new FileReader(jsonFile)) {
            Map<String, String> variants = new GsonBuilder().create().fromJson(reader, new TypeToken<HashMap<String, String>>(){}.getType());
            return variants.getOrDefault(filename, "unknown");
        } catch (IOException e) {
            AmbleKit.LOGGER.error("Failed to read variants.json", e);
            return "unknown";
        }
    }

    public void tick() {
        ticks++;

        if (/*ticks % 5 != 0 ||*/ connection) return;
        // called every second
        this.downloadNext();

        ticks = 0;
    }

    private void enqueueDownload(String id, String url) {
        this.downloadQueue.put(id, url);

        AmbleKit.LOGGER.info("Enqueued Download {} for {}", url, id);
    }

    private void downloadNext() {
        if (this.downloadQueue.isEmpty()) {
            return;
        }

        Pair<String, String> data = this.downloadQueue.remove();

        this.download(data.getFirst(), data.getSecond());
    }

    private void download(String id, String url) {
        AmbleKit.LOGGER.info("Downloading {} for {}", url, id);

        if (!(isValidUrl(url))) {
            AmbleKit.LOGGER.error("Discarding Invalid URL: {}", url);
            return;
        }

        connection = true;

        // check cache
        SkinCache.CacheData data = cache.get(id).orElse(null);
        if (data != null) {
            try {
                AmbleKit.LOGGER.info("Using cached skin for {}", id);
                urls.put(id, data.url());
                this.registerSkin(id);
                connection = false;
                return;
            } catch (Exception exception) {
                AmbleKit.LOGGER.error("Failed to load cached skin for {}", id, exception);
            }
        }

        urls.put(id, url);

        new Thread(() -> {
            try {
                this.downloadImageFromURL(id, new File(SKIN_DIR), url);
                this.registerSkin(id);
                this.cache.add(id, url);
                AmbleKit.LOGGER.info("Downloaded {} for {}!", url, id);
            } catch (Exception exception) {
                AmbleKit.LOGGER.error("Failed to download {} for {}", url, id, exception);
            } finally {
                connection = false;
            }
        }, AmbleKit.MOD_ID + "-Download").start();
    }

    public boolean hasDownloads() {
        return !downloadQueue.isEmpty();
    }

    public int getDownloadsRemaining() {
        return downloadQueue.size();
    }

    public void onStopping() {
        // this.clearTextures();
        cache.save();
    }

    public interface IDownloadSource {
        default void download() {
            AmbleKit.LOGGER.info("Downloading {}", getId());

            getTracker().connection = true;

            if (this.isDownloaded()) {
                AmbleKit.LOGGER.warn("{} is already downloaded", getId());
            }

            new Thread(() -> {
                try {
                    this.downloadThreaded();
                } catch (Exception exception) {
                    AmbleKit.LOGGER.error("Failed to download {}", getId(), exception);
                } finally {
                    getTracker().connection = false;
                }
            }, this.getId() + "-Download").start();
        }

        void downloadThreaded();

        String getId();

        boolean isDownloaded();

        default SkinGrabber getTracker() {
            return SkinGrabber.INSTANCE;
        }
    }

    public static class JerynSkins implements IDownloadSource {
        public static final String JERYN_URL = "https://api.jeryn.dev/mc/skins/random";
        private final ArrayList<String> keys;

        public JerynSkins(ArrayList<String> keys) {
            this.keys = keys;
        }

        @Override
        public boolean isDownloaded() {
            return !this.getTracker().cache.isJerynOutdated() || !keys.isEmpty() && new HashSet<>(this.getTracker().getAllKeys()).containsAll(keys);
        }

        @Override
        public void downloadThreaded() {
            try {
                if (this.isDownloaded()) {
                    AmbleKit.LOGGER.info("JerynSkins is already downloaded");
                    return;
                }

                this.getTracker().cache.lastJerynCheck = System.currentTimeMillis();

                URL api = new URL(JERYN_URL);
                URLConnection connection = api.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JsonElement data = new GsonBuilder().create().fromJson(stringBuilder.toString(), JsonElement.class);
                if (!data.isJsonArray()) {
                    throw new IllegalStateException("Expected array");
                }

                HashMap<String, String> skins = new HashMap<>();

                data.getAsJsonArray().forEach(element -> {
                    if (!element.isJsonObject()) {
                        throw new IllegalStateException("Expected object");
                    }

                    String url = element.getAsJsonObject().get("link").getAsString();
                    String id = SkinConstants.encodeURL(url);

                    skins.put(id, url);
                });

                keys.clear();
                keys.addAll(skins.keySet());

                skins.forEach(SkinGrabber.INSTANCE::enqueueDownload);
            } catch (Exception exception) {
                AmbleKit.LOGGER.error("Failed to download JerynSkins", exception);
            }
        }

        @Override
        public String getId() {
            return AmbleKit.MOD_ID + "-JerynSkins";
        }
    }

    public static class SimpleDownloadSource implements IDownloadSource {
        protected final Consumer<SimpleDownloadSource> download;
        protected final Function<SimpleDownloadSource, Boolean> isDownloaded;
        private final String id;

        public SimpleDownloadSource(String id, Consumer<SimpleDownloadSource> download, Function<SimpleDownloadSource, Boolean> isDownloaded) {
            this.id = id;
            this.download = download;
            this.isDownloaded = isDownloaded;
        }

        public boolean isDownloaded() {
            return this.isDownloaded.apply(this);
        }

        public void downloadThreaded() {
            this.download.accept(this);
        }

        public String getId() {
            return AmbleKit.MOD_ID + id;
        }
    }
}