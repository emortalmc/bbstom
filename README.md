# bbstom
Blockbench renderer for Minestom

## Usage
To animate a player, you will need the [Stable Player Display resource pack](https://github.com/bradleyq/stable_player_display)

You may also find it helpful to use the provided [player.bbmodel](https://github.com/emortaldev/bbstom/blob/main/assets/player.bbmodel) (Credit to [emula_izzy](https://emula-izzy.neocities.org/))
```java
byte[] bytes = Files.readAllBytes(Path.of("player.bbmodel"));
final BBModel model = BBModel.fromBytes(bytes);

BBAnimation walkAnim = model.getAnimationByName("walk");

BlockbenchPlayerRenderer walkRenderer = new BlockbenchPlayerRenderer(model, walkAnim, instance, new Pos(0, 1, 0), playerSkin);
walkRenderer.render();
```
If you wish to use your own player model, make sure the limb objects are named as:
- Body
- Left Arm
- Right Arm
- Left Leg
- Right Leg
- Head

(or create your own renderer)

---

Get a specific element's position and rotation

```java
BBModel model = ...
BBAnimation animation = model.getAnimationByName("...");
float time = 0.2f;

String objectUUID = model.getElementUUID("Left Leg");

Vector3f vec = model.getPosition(animation, time, objectUUID).div(16);
Quaternionf quat = model.getRotation(animation, time, objectUUID).premul(yawQuat);
float[] floats = BBModel.quatToFloats(quat); // get float array for use in setLeftRotation/setRightRotation
```
---

Animated Java support is planned, however I am waiting until they update to Blockbench 5.0
