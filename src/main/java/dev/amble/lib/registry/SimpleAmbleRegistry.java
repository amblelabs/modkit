package dev.amble.lib.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SimpleAmbleRegistry<T> implements AmbleRegistry<T>, Registry<T> {

    private final RegistryKey<? extends Registry<T>> key;
    private final Registry<T> registry;

    public SimpleAmbleRegistry(
            RegistryKey<? extends Registry<T>> key, Registry<T> registry) {
        this.key = key;
        this.registry = registry;
    }

    @Override
    public Registry<T> cached() {
        return registry;
    }

    @Override
    public RegistryKey<? extends Registry<T>> key() {
        return key;
    }

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return registry.getKey();
    }

    @Override
    public Codec<T> getCodec() {
        return registry.getCodec();
    }

    @Override
    public Codec<RegistryEntry<T>> createEntryCodec() {
        return registry.createEntryCodec();
    }

    @Override
    public <U> Stream<U> keys(DynamicOps<U> ops) {
        return registry.keys(ops);
    }

    @Override
    @Nullable
    public Identifier getId(T value) {
        return registry.getId(value);
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return registry.getKey(entry);
    }

    @Override
    public int getRawId(@Nullable T value) {
        return registry.getRawId(value);
    }

    @Override
    @Nullable
    public T get(@Nullable RegistryKey<T> key) {
        return registry.get(key);
    }

    @Override
    @Nullable
    public T get(@Nullable Identifier id) {
        return registry.get(id);
    }

    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return registry.getEntryLifecycle(entry);
    }

    @Override
    public Lifecycle getLifecycle() {
        return registry.getLifecycle();
    }

    @Override
    public Optional<T> getOrEmpty(@Nullable Identifier id) {
        return registry.getOrEmpty(id);
    }

    @Override
    public Optional<T> getOrEmpty(@Nullable RegistryKey<T> key) {
        return registry.getOrEmpty(key);
    }

    @Override
    public T getOrThrow(RegistryKey<T> key) {
        return registry.getOrThrow(key);
    }

    @Override
    public Set<Identifier> getIds() {
        return registry.getIds();
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return registry.getEntrySet();
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return registry.getKeys();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return registry.getRandom(random);
    }

    @Override
    public Stream<T> stream() {
        return registry.stream();
    }

    @Override
    public boolean containsId(Identifier id) {
        return registry.containsId(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return registry.contains(key);
    }

    @Override
    public Registry<T> freeze() {
        return registry.freeze();
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        return registry.createEntry(value);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return registry.getEntry(rawId);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
        return registry.getEntry(key);
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        return registry.getEntry(value);
    }

    @Override
    public RegistryEntry.Reference<T> entryOf(RegistryKey<T> key) {
        return registry.entryOf(key);
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return registry.streamEntries();
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return registry.getEntryList(tag);
    }

    @Override
    public Iterable<RegistryEntry<T>> iterateEntries(TagKey<T> tag) {
        return registry.iterateEntries(tag);
    }

    @Override
    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return registry.getOrCreateEntryList(tag);
    }

    @Override
    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return registry.streamTagsAndEntries();
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return registry.streamTags();
    }

    @Override
    public void clearTags() {
        registry.clearTags();
    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {
        registry.populateTags(tagEntries);
    }

    @Override
    public IndexedIterable<RegistryEntry<T>> getIndexedEntries() {
        return registry.getIndexedEntries();
    }

    @Override
    public RegistryEntryOwner<T> getEntryOwner() {
        return registry.getEntryOwner();
    }

    @Override
    public RegistryWrapper.Impl<T> getReadOnlyWrapper() {
        return registry.getReadOnlyWrapper();
    }

    @Override
    public RegistryWrapper.Impl<T> getTagCreatingWrapper() {
        return registry.getTagCreatingWrapper();
    }

    @Override
    @Nullable
    public T get(int index) {
        return registry.get(index);
    }

    @Override
    public T getOrThrow(int index) {
        return registry.getOrThrow(index);
    }

    @Override
    public int size() {
        return registry.size();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        registry.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return registry.spliterator();
    }
}
