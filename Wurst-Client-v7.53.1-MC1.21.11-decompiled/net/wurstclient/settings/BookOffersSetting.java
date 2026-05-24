package net.wurstclient.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.BookOffersEditButton;
import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;
import net.wurstclient.util.text.WText;

public final class BookOffersSetting
extends Setting {
    private final ArrayList<BookOffer> offers = new ArrayList();
    private final BookOffer[] defaultOffers;

    public BookOffersSetting(String name, WText description, String ... enchantments) {
        super(name, description);
        Arrays.stream(enchantments).filter(Objects::nonNull).map(s -> {
            String[] parts = s.split(";");
            return new BookOffer(parts[0], Integer.parseInt(parts[1]), 64);
        }).filter(BookOffer::isMostlyValid).distinct().sorted().forEach(this.offers::add);
        this.defaultOffers = this.offers.toArray(new BookOffer[0]);
    }

    public BookOffersSetting(String name, String descriptionKey, String ... enchantments) {
        this(name, WText.translated(descriptionKey, new Object[0]), enchantments);
    }

    public List<BookOffer> getOffers() {
        return Collections.unmodifiableList(this.offers);
    }

    public int indexOf(BookOffer offer) {
        return Collections.binarySearch(this.offers, offer);
    }

    public boolean contains(BookOffer offer) {
        return this.indexOf(offer) >= 0;
    }

    public boolean isWanted(BookOffer offer) {
        int index = this.indexOf(offer);
        if (index < 0) {
            return false;
        }
        int maxPrice = this.offers.get(index).price();
        return offer.price() <= maxPrice;
    }

    public void add(BookOffer offer) {
        if (offer == null || !offer.isFullyValid()) {
            return;
        }
        if (this.contains(offer)) {
            return;
        }
        this.offers.add(offer);
        Collections.sort(this.offers);
        WurstClient.INSTANCE.saveSettings();
    }

    public void remove(int index) {
        if (index < 0 || index >= this.offers.size()) {
            return;
        }
        this.offers.remove(index);
        WurstClient.INSTANCE.saveSettings();
    }

    public void replace(int index, BookOffer offer) {
        if (index < 0 || index >= this.offers.size()) {
            return;
        }
        if (offer == null || !offer.isFullyValid()) {
            return;
        }
        if (!offer.equals(this.offers.get(index)) && this.contains(offer)) {
            return;
        }
        this.offers.remove(index);
        this.offers.add(offer);
        Collections.sort(this.offers);
        WurstClient.INSTANCE.saveSettings();
    }

    public void resetToDefaults() {
        this.offers.clear();
        this.offers.addAll(Arrays.asList(this.defaultOffers));
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new BookOffersEditButton(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            this.offers.clear();
            if (JsonUtils.getAsString(json, "nope").equals("default")) {
                this.offers.addAll(Arrays.asList(this.defaultOffers));
                return;
            }
            JsonUtils.getAsArray(json).getAllObjects().parallelStream().map(this::loadOffer).filter(Objects::nonNull).filter(BookOffer::isMostlyValid).distinct().sorted().forEachOrdered(this.offers::add);
        }
        catch (JsonException e) {
            System.out.println("Invalid book offer list: " + String.valueOf(json));
            e.printStackTrace();
            this.resetToDefaults();
        }
    }

    private BookOffer loadOffer(WsonObject wson) {
        try {
            String id = wson.getString("id");
            int level = wson.getInt("level");
            int price = wson.getInt("max_price", 64);
            return new BookOffer(id, level, price);
        }
        catch (JsonException e) {
            System.out.println("Invalid book offer: " + String.valueOf(wson));
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonElement toJson() {
        if (this.offers.equals(Arrays.asList(this.defaultOffers))) {
            return new JsonPrimitive("default");
        }
        JsonArray json = new JsonArray();
        this.offers.forEach(offer -> {
            JsonObject jsonOffer = new JsonObject();
            jsonOffer.addProperty("id", offer.id());
            jsonOffer.addProperty("level", offer.level());
            if (offer.price() < 64) {
                jsonOffer.addProperty("max_price", offer.price());
            }
            json.add(jsonOffer);
        });
        return json;
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "BookOffers");
        JsonArray jsonDefaultOffers = new JsonArray();
        for (BookOffer offer : this.defaultOffers) {
            JsonObject jsonOffer = new JsonObject();
            jsonOffer.addProperty("id", offer.id());
            jsonOffer.addProperty("level", offer.level());
            if (offer.price() < 64) {
                jsonOffer.addProperty("max_price", offer.price());
            }
            jsonDefaultOffers.add(jsonOffer);
        }
        json.add("defaultOffers", jsonDefaultOffers);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        return new LinkedHashSet<PossibleKeybind>();
    }
}
