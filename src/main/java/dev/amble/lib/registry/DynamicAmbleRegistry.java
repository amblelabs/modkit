package dev.amble.lib.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class DynamicAmbleRegistry<T> implements AmbleRegistry<T>, Registry<T> {

    private final RegistryKey<? extends Registry<T>> key;
    private final Codec<T> codec;

    public DynamicAmbleRegistry(RegistryKey<? extends Registry<T>> key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    @Override
    public RegistryKey<? extends Registry<T>> key() {
        return key;
    }

    public Codec<T> codec() {
        return codec;
    }

    public void cache(World world) {
        
    }

    public Registry<T> get(World world) {
        return world.getRegistryManager().get(this.key());
    }

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return key();
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return 0;
    }

    @Nullable
    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Nullable
    @Override
    public T get(@Nullable RegistryKey<T> key) {
        return null;
    }

    @Nullable
    @Override
    public T get(@Nullable Identifier id) {
        return null;
    }

    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return null;
    }

    @Override
    public Lifecycle getLifecycle() {
        return null;
    }

    @Override
    public Set<Identifier> getIds() {
        return Set.of();
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Set.of();
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return Set.of();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return Optional.empty();
    }

    @Override
    public boolean containsId(Identifier id) {
        return false;
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return false;
    }

    @Override
    public Registry<T> freeze() {
        return null;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return Optional.empty();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
        return Optional.empty();
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        return null;
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return Stream.empty();
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.empty();
    }

    @Override
    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return null;
    }

    @Override
    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return Stream.empty();
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return Stream.empty();
    }

    @Override
    public void clearTags() {

    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {

    }

    @Override
    public RegistryEntryOwner<T> getEntryOwner() {
        return null;
    }

    @Override
    public RegistryWrapper.Impl<T> getReadOnlyWrapper() {
        return null;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return null;
    }
}
