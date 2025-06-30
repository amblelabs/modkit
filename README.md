<div align="center">

### **Presented by [Amble Labs](https://amblelabs.github.io)**

![Amble Kit](https://cdn.modrinth.com/data/cached_images/d356695702e6fbdc33ad1ecd0bcbb344dffd4cb6_0.webp)

[<img alt="curseforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/amblekit) <!-- SVG version -->
[<img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/amblekit) <!-- SVG version -->
[<img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">](https://fabricmc.net/) <!-- SVG version -->
[<img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg">](https://files.minecraftforge.net) <!-- SVG version -->

</div>

---
<div align="center">
  
### This a library mod that can be used for code simplifying for developing on Fabric

***This is used in many of our Fabric mods***
</div>

<h2>
  <img src="https://cdn.modrinth.com/data/cached_images/b18b275a0e9bb4000e015b935b65037166301538.png"
       alt="What does this libary add?"
       width="25"
       height="25"
       style="vertical-align: middle; margin-right: 8px;">
  What does this libary add?
</h2>

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

### Much more!

<h2>
  <img src="https://cdn.modrinth.com/data/cached_images/808c7934614530076d21dd0cf5c5e2e992595985.png"
       alt="Where can I start with this?"
       width="25"
       height="25"
       style="vertical-align: middle; margin-right: 8px;">
  Where can I start with this?
</h2>

### You can start with our template for amblekit!

  [Github Template for Fabric 1.20.1 Modkit](https://github.com/amblelabs/modkit-template)

### If you have an already existing mod and want the amblekit then add this to your **build.gradle**!


  ```
  repositories {
      maven {
          url "https://jitpack.io"
  
          metadataSources {
              artifact() // Look directly for artifact
          }
      }
  }

  dependencies {
      modImplementation("com.github.amblelabs:modkit:${project.modkit_version}") {
          exclude(group: "net.fabricmc.fabric-api")
      }
  }
  ```
  or if you are using kotlin
  ```
    repositories {
      maven {
          url = uri("https://jitpack.io")
          metadataSources {
              artifact() // Look directly for artifact
          }
      }
      mavenCentral()
  }
  
  
  dependencies {
      modImplementation("com.github.amblelabs:modkit:${project.property("modkit_version")}")
  }
  ```



<h2>
  <img src="https://cdn.modrinth.com/data/cached_images/23b97ecfe49586f70c6a7d4e4ca63ac14d47e6e1.png"
       alt="Links & Community"
       width="25"
       height="25"
       style="vertical-align: middle; margin-right: 8px;">
  Links & Community
</h2>

  <a href="https://github.com/amblelabs/modkit/" style="text-decoration: none; color: inherit; display: inline-block; margin: 0 8px;">GitHub</a>
  <span style="display: inline-block; margin: 0 4px;">•</span>
  <a href="https://www.curseforge.com/minecraft/mc-mods/amblekit" style="text-decoration: none; color: inherit; display: inline-block; margin: 0 8px;">CurseForge</a>
  <span style="display: inline-block; margin: 0 4px;">•</span>
  <a href="https://modrinth.com/mod/amblekit" style="text-decoration: none; color: inherit; display: inline-block; margin: 0 8px;">Modrinth</a>
  <span style="display: inline-block; margin: 0 4px;">•</span>
  <a href="https://discord.com/invite/WjKhRjavCj" style="text-decoration: none; color: inherit; display: inline-block; margin: 0 8px;">Discord</a>
