*FastStart* is an experimental Minecraft mod (currently for 1.7.10) created to reduce the load time
of large modpacks.

# Show me the numbers!

| Mod name                             | Startup time without FastStart* | Startup time with FastStart* |
| ------------------------------------ | ------------------------------- | ---------------------------- |
| GregTech: New Horizons**             | 06:13                           | 04:57                        |
| TechNodeFirmaCraft                   | 01:34                           | 01:05                        |
| Gregorious: "Iter Vehemens ad Lutum" | 02:05                           | 01:49                        |
| Blightfall                           | 01:07                           | 00:53                        |

*: Times to get to the main menu. Average of 3 runs. Measured using [this script](https://github.com/makamys/FastStart/blob/master/scripts/showtimes.py).

**: One folder resource pack present.

Tests were done on a Lenovo Y520-IKBN laptop (full hardware specs [here](https://gist.github.com/makamys/f90d56ef39bed67fd49ac400cf508223)) running
Linux, using `java 1.8.0_252-8u252-b09-1~18.04-b09`.

# How?

Currently, 3 tweaks are implemented:

## 1. Class loader cache

All classes loaded by Minecraft are run through a chain of transformers, which mods
can add their own transformers to. It is in this way that the functionality of
vanilla classes is changed.

The time spent running the transformers can add up to a significant portion of time.
This tweak changes the transformer chain to just a single transformer which runs
all the other transformers the first time a class is loaded, but on consecutive runs,
loads the results from the past run for instantenous transformation!

**Improvement on my computer:** 10-30s depending on size of modpack (after the first run).

**Risk level:** Medium. The config file may have to be tweaked to get it to work without
crashing. (Contributions of such changes are welcome.)

#### Known issues
* Mods that use DragonAPI cause a crash on startup
* Some mod in Blightfall causes an error message in the menu, but it's harmless

All of these could be fixed with a little rework ~~but I'm too lazy to do it~~.

> Note: you may have to delete classCache.dat when making changes to the modpack, lest the
class cache get stale. There's a system that automatically rebuilds the cache if it detects
mod files are changed, but there are other ways of changing classes that it doesn't
account for, such as installing LiteLoader or changing certain config options of mods.

## 2. More efficient folder resource pack loading

The method Minecraft uses for loading resources out of folder resource packs is horribly
inefficient with large modpacks: it checks every single resource if it's inside the folder,
and does this for all folder resource packs present.

This tweak uses a more effective method that drastically reduces the cost.

**Improvement on my computer:** 30s per resource pack on GTNH. No difference if no folder resource packs are present, of course.

**Risk level:** None?

## 3. Multi-threaded texture stitching I/O

Performs the I/O operations performed during texture stitching on multiple cores.
It doesn't seem to improve much, though.

**Improvement on my computer:** Probably around 0s

**Risk level:** None?
