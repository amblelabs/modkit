# Bedrock Animation System

AmbleKit provides a powerful Bedrock Edition animation system that lets you use Blockbench-style geometry and animations on your entities and block entities. This system supports the standard Bedrock model/animation JSON format, making it easy to create and import complex animated models.

## Table of Contents
- [Overview](#overview)
- [Model Format](#model-format)
- [Animation Format](#animation-format)
- [Setting Up Animated Entities](#setting-up-animated-entities)
- [Setting Up Animated Block Entities](#setting-up-animated-block-entities)
- [Playing Animations](#playing-animations)
- [Animation Features](#animation-features)
- [Commands](#commands)
- [File Locations](#file-locations)

---

## Overview

The Bedrock Animation System provides:
- **Bedrock Model Support** - Load `.geo.json` models from Blockbench
- **Bedrock Animation Support** - Load `.animation.json` animation files
- **Automatic Renderer Registration** - Use `@HasBedrockModel` annotation for automatic setup
- **Sound Event Integration** - Play sounds at specific animation keyframes
- **Animation Metadata** - Control movement and other behaviors during animations
- **Looping & One-Shot** - Support for both looping and single-play animations

---

## Model Format

AmbleKit uses the standard Bedrock Edition geometry format (`format_version` 1.12.0+).

### Model File Structure

Models should be placed in your resource pack:
```
assets/<namespace>/geo/<model_name>.geo.json
```

### Example Model File

```json
{
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.my_entity",
        "texture_width": 64,
        "texture_height": 64,
        "visible_bounds_width": 2,
        "visible_bounds_height": 3,
        "visible_bounds_offset": [0, 1.5, 0]
      },
      "bones": [
        {
          "name": "root",
          "pivot": [0, 0, 0]
        },
        {
          "name": "body",
          "parent": "root",
          "pivot": [0, 24, 0],
          "cubes": [
            {
              "origin": [-4, 12, -2],
              "size": [8, 12, 4],
              "uv": [16, 16]
            }
          ]
        },
        {
          "name": "head",
          "parent": "body",
          "pivot": [0, 24, 0],
          "cubes": [
            {
              "origin": [-4, 24, -4],
              "size": [8, 8, 8],
              "uv": [0, 0]
            }
          ]
        }
      ]
    }
  ]
}
```

### Supported Model Features

| Feature | Description |
|---------|-------------|
| **Bones** | Hierarchical bone structure with parent-child relationships |
| **Cubes** | Box-shaped geometry with position, size, UV mapping |
| **Pivots** | Rotation pivot points for bones |
| **Rotation** | Default bone rotations |
| **Mirroring** | UV mirroring for symmetric parts |
| **Inflate** | Expansion/contraction of cubes |
| **Locators** | Named positions for particle/effect spawning |

---

## Animation Format

AmbleKit uses the standard Bedrock Edition animation format.

### Animation File Structure

Animations should be placed in your resource pack:
```
assets/<namespace>/animations/<animation_name>.animation.json
```

### Example Animation File

```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.my_entity.walk": {
      "loop": true,
      "animation_length": 1.0,
      "bones": {
        "right_leg": {
          "rotation": {
            "0.0": [22.5, 0, 0],
            "0.5": [-22.5, 0, 0],
            "1.0": [22.5, 0, 0]
          }
        },
        "left_leg": {
          "rotation": {
            "0.0": [-22.5, 0, 0],
            "0.5": [22.5, 0, 0],
            "1.0": [-22.5, 0, 0]
          }
        }
      }
    },
    "animation.my_entity.attack": {
      "loop": false,
      "animation_length": 0.5,
      "bones": {
        "right_arm": {
          "rotation": {
            "0.0": [0, 0, 0],
            "0.25": [-90, 0, 0],
            "0.5": [0, 0, 0]
          }
        }
      }
    }
  }
}
```

### Keyframe Interpolation

| Type | Description |
|------|-------------|
| **Linear** | Default smooth interpolation between keyframes |
| **Smooth (Catmull-Rom)** | Extra-smooth transitions using catmull-rom splines |

### Animation Properties

| Property | Description |
|----------|-------------|
| `loop` | Whether the animation repeats (`true`/`false`) |
| `animation_length` | Duration in seconds |
| `bones` | Map of bone names to transformation timelines |

### Bone Transformation Channels

| Channel | Description |
|---------|-------------|
| `position` | Translate the bone (x, y, z) |
| `rotation` | Rotate the bone (pitch, yaw, roll in degrees) |
| `scale` | Scale the bone (x, y, z multipliers) |

---

## Setting Up Animated Entities

### Step 1: Implement AnimatedEntity

Make your entity implement `AnimatedEntity`:

```java
public class MyEntity extends LivingEntity implements AnimatedEntity {
    private final AnimationState animationState = new AnimationState();
    
    @Override
    public AnimationState getAnimationState() {
        return animationState;
    }
    
    @Override
    public BedrockModelReference getModel() {
        return new BedrockModelReference(new Identifier("mymod", "geo/my_entity.geo.json"));
    }
    
    @Override
    public Identifier getTexture() {
        return new Identifier("mymod", "textures/entity/my_entity.png");
    }
    
    @Override
    public BedrockAnimationReference getDefaultAnimation() {
        return BedrockAnimationReference.parse(new Identifier("mymod", "my_entity.walk"));
    }
}
```

### Step 2: Register with @HasBedrockModel

In your `EntityContainer`, annotate the entity type with `@HasBedrockModel`:

```java
public class MyEntities implements EntityContainer {
    @HasBedrockModel
    public static final EntityType<MyEntity> MY_ENTITY = EntityType.Builder
            .create(MyEntity::new, SpawnGroup.CREATURE)
            .setDimensions(0.6f, 1.8f)
            .build("my_entity");
}
```

The renderer will be automatically registered on the client side.

---

## Setting Up Animated Block Entities

### Step 1: Implement AnimatedBlockEntity

```java
public class MyBlockEntity extends BlockEntity implements AnimatedBlockEntity {
    private final AnimationState animationState = new AnimationState();
    
    @Override
    public AnimationState getAnimationState() {
        return animationState;
    }
    
    @Override
    public BedrockModelReference getModel() {
        return new BedrockModelReference(new Identifier("mymod", "geo/my_block.geo.json"));
    }
    
    @Override
    public Identifier getTexture() {
        return new Identifier("mymod", "textures/block/my_block.png");
    }
}
```

### Step 2: Register the Renderer

Use `BedrockBlockEntityRenderer` for your block entity:

```java
BlockEntityRendererRegistry.register(MY_BLOCK_ENTITY, BedrockBlockEntityRenderer::new);
```

---

## Playing Animations

### Via Java Code

```java
// Get an AnimatedEntity
AnimatedEntity entity = ...;

// Create an animation reference
BedrockAnimationReference animation = BedrockAnimationReference.parse(
    new Identifier("mymod", "my_entity.attack")
);

// Play the animation
entity.playAnimation(animation);
```

### Animation Reference Format

Animation references follow this format:
```
namespace:animation_file.animation_name
```

For example:
- `mymod:my_entity.walk` → loads `animation.my_entity.walk` from `assets/mymod/animations/my_entity.animation.json`

---

## Animation Features

### Sound Events

Add sounds to play at specific times during animations:

```json
{
  "animations": {
    "animation.my_entity.attack": {
      "animation_length": 0.5,
      "sound_effects": {
        "0.0": {
          "effect": "minecraft:entity.player.attack.sweep"
        },
        "0.25": {
          "effect": "mymod:custom_sound"
        }
      }
    }
  }
}
```

### Animation Metadata

Control entity behavior during animations:

```java
// In your AnimatedEntity implementation
@Override
public AnimationMetadata getAnimationMetadata() {
    return new AnimationMetadata(
        false  // movement: false = freeze entity during animation
    );
}
```

### Locators

Define named positions in your model for spawning particles or effects:

```json
{
  "bones": [
    {
      "name": "right_arm",
      "locators": {
        "hand": {
          "offset": [0, -10, 0],
          "rotation": [0, 0, 0]
        }
      }
    }
  ]
}
```

---

## Commands

### Play Animation Command

Operators can play animations on entities via command:

```
/amblekit animation <target> <animation_id>
```

| Argument | Description |
|----------|-------------|
| `target` | Entity selector (e.g., `@e[type=mymod:my_entity,limit=1]`) |
| `animation_id` | Animation identifier (e.g., `mymod:my_entity.attack`) |

**Examples:**
```
/amblekit animation @e[type=mymod:my_entity,limit=1] mymod:my_entity.attack
/amblekit animation @s mymod:player.wave
```

---

## File Locations

### Resource Pack Structure

```
assets/<namespace>/
├── geo/
│   └── my_entity.geo.json          # Model geometry
├── animations/
│   └── my_entity.animation.json    # Animation data
└── textures/
    └── entity/
        └── my_entity.png           # Entity texture
```

### Automatic Registration

When using `@HasBedrockModel`:
- The model is loaded from `assets/<namespace>/geo/<entity_name>.geo.json`
- Animations are loaded from `assets/<namespace>/animations/<entity_name>.animation.json`
- Textures should be at `assets/<namespace>/textures/entity/<entity_name>.png`

---

## Advanced Usage

### Custom Renderers

For advanced rendering needs, extend `BedrockEntityRenderer`:

```java
public class MyCustomRenderer extends BedrockEntityRenderer<MyEntity> {
    public MyCustomRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    
    @Override
    protected void setupAnimations(MyEntity entity, ModelPart root, float tickDelta) {
        super.setupAnimations(entity, root, tickDelta);
        // Custom animation logic
    }
}
```

### Animation Tracking

Query animation state programmatically:

```java
AnimatedEntity entity = ...;

// Check if currently animating
BedrockAnimationReference current = entity.getCurrentAnimation();
if (current != null) {
    // Animation is playing
}

// Check if animation state has changed
if (entity.isAnimationDirty()) {
    // Handle animation change
}
```

---

## See Also

- [Lua Scripting System](LUA_SCRIPTING.md) - Trigger animations from Lua scripts
- [Registry Containers](README.md#minecraft-registration) - Automatic entity registration
