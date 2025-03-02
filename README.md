<p align="center"><img src="https://github.com/amblelabs/modkit/blob/main/src/main/resources/assets/amblekit/icon.png?raw=true" width="256" height="256" alt="AmbleKit Logo"></p>

# Amble Kit Library
## This a libary mod that can be used for code simplifying for developing on Fabric
***This is used in many of our Fabric mods***

## What does this add?

### Minecraft Registration
Instead of having to manually register each and every thing, you can simple `extend` or `implement` one of our `RegistryContainer` classes.

These utility interfaces are recognised by our mod by sticking `RegistryContainer.register(ClassName.class, MOD_ID` into your mods `#onInitialize` method.

### Datapack Workflow
We provide a custom class called `SimpleDatapackRegistry`

This allows your own classes to be read and registered straight from datapacks with ease!

For the kit to recognise your registry, in your mods `#onInitialize` method, you need to call `register` on your instance OR `AmbleRegistries.register(MyRegistry.getInstance()))`

### Data Generation
We utilise annotations and the previously mentioned registry containers to automatically generate many features.

For example, automatic english translation for blocks - 

By simply creating an instance of `AmbleLanguageProvider` and passing in your `BlockContainer` with the `#withBlocks` method, next time you run datagen all these blocks will have english translations based off their identifiers.

There are more datagen utilities akin to this.

### Embedded Libraries
We have a few useful libraries embedded within this mod.

- [mc-queue](https://github.com/DrTheodor/mc-queue)
- [mc-multidim](https://github.com/DrTheodor/mc-multidim/)
- [mc-scheduler](https://github.com/DrTheodor/mc-scheduler)

### Much more!

### For your readmes

<a href="https://modrinth.com/project/amblekit"><img src="https://github.com/amblelabs/modkit/blob/main/promo/required.png?raw=true" width="512" height="86"></a>

<img src="https://github.com/amblelabs/modkit/blob/main/promo/header.png?raw=true" width="328" height="86">
