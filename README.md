<p align="center">
  <img src="https://cdn.modrinth.com/data/geZ7ilkE/ce24330967cfcfd8d53e2ffca1f3eedb878baf6a_96.webp" width="128" height="128" alt="Beltborne Lanterns icon">
</p>

<p align="center" style="display:flex;justify-content:center;gap:8px;margin:6px 0;">
  <a href="https://modrinth.com/project/beltborne-lanterns-accessories-layer">
    <img alt="Modrinth" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/modrinth_vector.svg">
  </a>&nbsp;
  <a href="https://www.curseforge.com/minecraft/mc-mods/bl-accessories-layer">
    <img alt="CurseForge" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/curseforge_vector.svg">
  </a>&nbsp;
  <a href="https://discord.gg/9JRb3JMAD3">
    <img alt="Discord" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/social/discord-plural_vector.svg">
  </a>&nbsp;
  <a href="https://github.com/Shadscure/Beltborne-Lanterns-Accessories-Layer">
    <img alt="GitHub" height="48" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/github_vector.svg">
  </a>
</p>

<p align="center">
  <a href="https://modrinth.com/project/beltborne-lanterns-accessories-layer">
    <img alt="Modrinth Downloads" src="https://img.shields.io/modrinth/dt/geZ7ilkE?style=flat&logo=modrinth">
  </a>
  <a href="https://modrinth.com/project/beltborne-lanterns-accessories-layer">
    <img alt="CurseForge Downloads" src="https://img.shields.io/curseforge/dt/1342341?style=flat&logo=curseforge">
  </a>
</p>



# Beltborne Lanterns: Accessories Layer

Compatibility bridge between **[Beltborne Lanterns](https://modrinth.com/mod/beltborne-lanterns)** and **[Accessories](https://modrinth.com/mod/accessories)** (WispForest).

Equip your lantern in the Accessories **Belt** slot — it works just like the default belt: sways with physics, casts dynamic light, and keeps your hands free.

## 🧩 What it does

* Registers all Beltborne Lanterns items as valid **Belt** accessories.
* Syncs the belt state — equipping via Accessories or pressing **B** both work seamlessly.
* Fully client & server: renders the lantern on other players and emits light for everyone.

## 📷 Showcase

![img.png](demo/img.png)

## ⚙️ Requirements

* **[Beltborne Lanterns](https://modrinth.com/mod/beltborne-lanterns)** — the core mod
* **[Accessories](https://modrinth.com/mod/accessories)** — the accessory API by WispForest

<details>
<summary><strong>🔧 For Modpack Makers</strong></summary>

By default, lanterns can only be placed in the **Belt** slot. You can customize this by editing `config/bl_accessories_layer.json` (auto-generated on first launch):

```json
{
  "allowed_slots": ["belt"]
}
```

To allow additional slots (e.g. `charm`, `necklace`), add them to the list:

```json
{
  "allowed_slots": ["belt", "charm", "necklace"]
}
```

No datapacks or item tags are needed — the mod handles slot validation automatically.

</details>

## 📜 License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.  
Full license text: [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

<p align="center">
  <sub>Crafted with ❤️ for the Minecraft community</sub>
</p>
