# Shape of Things That Were - Design Document

## Overview

**Shape of Things That Were** is an open-source strategy game built with Java and libGDX, using the Artemis entity-component-system (ECS) framework. The game emphasizes **macro-management over micro-management** and **chaos over stability**.

---

## Core Design Philosophy

### 1. Macro Management via Influence

**Decision**: The central gameplay concept is **Influence** - the area controlled by each empire.

**Rationale**:
- Eliminates micro-management (no building individual structures, moving regiments, assigning workers)
- Empires expand by improving influence on surrounding terrain
- Conflict consists of influencing tiles rather than direct unit combat
- Influence flows from sources (cities) across tiles based on terrain difficulty

**Implementation** (`InfluenceSystem.java:41-65`):
- Sources start with `INITIAL_POWER = 100`
- Influence spreads via waterfall algorithm (linear interpolation of target influence)
- Terrain costs affect spread rate (see `Terrain.java:57-59` for move costs)
- Multiple sources compete for tiles - highest influence wins
- Influence expressed as percentages for player clarity

### 2. Chaotic but Logical Systems

**Decision**: Game systems should be unpredictable yet comprehensible.

**Rationale** (from `README.md:19-27`):
- Stable/predictable situations are boring
- Player must react to evolving situations
- Complex enough to prevent micro-management victory
- Clear directional clues so player can make informed decisions
- "Logical but unpredictable" - a chaotic system

**Implementation**:
- Events system triggers discoveries, revolts, disasters probabilistically
- AI empires make decisions based on faction preferences (Military/Economic/Cultural)
- Influence spreads continuously but with terrain-based variation
- Diplomacy states change based on mutual proposals

---

## Architecture

### Entity-Component-System (ECS)

**Decision**: Use Artemis ECS framework for game logic.

**Rationale**:
- Clear separation of data (components) and behavior (systems)
- Efficient entity iteration via aspects
- Natural fit for strategy game with many entities (empires, armies, tiles)
- Systems can be wired and processed independently

**Key Components** (`model/components/`):
- `Empire` - Player/AI identity, color, culture, AI control flag
- `InfluenceSource` - Power, growth, health, terrain modifiers, influenced tiles
- `Discoveries` - Completed discoveries, progress, next possible research
- `Diplomacy` - Relations, proposals, known diplomatic states
- `Army` / `ArmyCommand` - Military power, movement commands
- `Policies` - Stability, growth, active policies
- `EventsCount` - Progress toward next event per event type

**Key Systems** (`model/`):
- `InfluenceSystem` - Core territorial control mechanic
- `DiscoverySystem` - Technology/culture progression
- `EventsSystem` - Turn-based event triggering
- `DiplomaticSystem` - Inter-empire relations
- `EffectsSystem` - Applies discovery/policy effects
- `ArmiesSystem`, `AIArmyCommandSystem`, `AIArmyMovementSystem` - Military AI
- `RevoltSystem`, `DisasterSystem`, `SpecialDiscoveriesSystem` - Chaos injectors

---

## Map & Terrain

### Hexagonal Grid

**Decision**: Hexagonal tile map with midpoint displacement generation.

**Rationale**:
- Natural for strategy games (equal distance to neighbors)
- Midpoint displacement creates realistic, varied continents
- Supports multiple generator types (island, terra, combined)

**Terrain Types** (`Terrain.java:10-28`):
| Terrain | Move Cost | Can Start | Description |
|---------|-----------|-----------|-------------|
| DEEP_WATER | 50 | false | Ocean |
| SHALLOW_WATER | 30 | false | Coastal |
| DESERT | 50 | false | Arid |
| PLAIN | 10 | true | Open land |
| GRASSLAND | 15 | true | Fertile |
| FOREST | 30 | true | Wooded |
| HILLS | 30 | true | Elevated |
| MOUNTAIN | 50 | false | Impassable |
| ARCTIC | 100 | false | Frozen |

**Generation** (`HexMapGenerator.java:7-65`):
- Uses `MidpointDisplacement` for heightmap
- Thresholds define terrain bands (configurable via properties)
- Noise, width, height parameters control variation

---

## Influence System (Core Mechanic)

### Influence Spread Algorithm

**Decision**: Waterfall algorithm with linear interpolation toward target influence.

**Process** (`InfluenceSystem.java:133-362`):
1. **Clear targets** - Reset all tile target influence
2. **Update source power** - Growth based on influenced tiles + tribute
3. **Compute target influence** - For each source:
   - Dijkstra-like spread from source position
   - Target = source power - accumulated terrain costs
   - Armies add secondary influence sources
4. **Apply influence** - Tiles interpolate toward target each turn

### Terrain Costs

**Decision**: Base move cost per terrain, modifiable by discoveries.

**Rationale**:
- Mountains/hills slow expansion (high cost)
- Rivers/coasts can be improved (discoveries reduce cost)
- Creates natural borders and strategic chokepoints

**Modifiers** (`InfluenceSource.Modifiers`):
- Each discovery can grant terrain bonuses
- Reduces effective move cost (minimum 1)
- Example: `Boat` discovery gives `DEEP_WATER: 3`, `SHALLOW_WATER: 2`

### Conquest & Tribute

**Decision**: When a city's tile is dominated by another empire, it becomes a tributary.

**Process** (`InfluenceSystem.java:170-205`):
- Detect when city tile's main influencer ≠ owner
- Loser loses 1 power, winner gains 1 power
- If power ≤ 0: empire destroyed
- Else: diplomatic relation → TRIBUTE/PROTECTORATE
- Influence transferred on city tile + neighbors

---

## Discovery System (Technology/Culture Tree)

### Discovery Data Model

**Decision**: Data-driven discoveries loaded from JSON, with prerequisite graph.

**Structure** (`Discovery.java:21-47`, `prehistory.json`):
```json
{
  "name": "Agriculture",
  "previous": [["Grassland", "Gathering"], ["Animal", "Gathering"]],
  "terrains": ["GRASSLAND", "PLAIN"],
  "effects": {"growth": 2},
  "groups": ["CEREAL"]
}
```

- **previous**: OR of AND prerequisites (any one list satisfies)
- **terrains**: Required terrain in influence for discovery eligibility
- **effects**: Modifiers applied on discovery (see EffectsSystem)
- **groups**: Categorization (CEREAL, FIBER, WEAPON, GOVERNMENT, etc.)

### Discovery Progression

**Decision**: Progress per turn based on capital power + terrain bonuses.

**Mechanics** (`DiscoverySystem.java:266-286`):
- Base progress = `progressPerTurn` (from discoveries/policies)
- +1 per influenced tile matching discovery's terrain requirements
- When progress ≥ threshold: discovery event triggers
- Player chooses from faction-recommended options
- AI chooses based on faction weights

### Faction System

**Decision**: Three factions compete to recommend discoveries.

**Factions** (`Faction.java`):
- **MILITARY** - Favors weapons, military power, war
- **ECONOMIC** - Favors growth, trade, terrain bonuses
- **CULTURAL** - Favors stability, discovery speed, art/religion

**Scoring** (`DiscoverySystem.java:116-127`, `EffectsSystem.java:54-184`):
- Each effect type awards points to factions
- Policy discoveries get 1.5x faction multiplier
- Cultural gets baseline bonus

### Discovery Spread

**Decision**: Discoveries spread to neighbors; once discovered by one, removed from global pool.

**Rationale**:
- Prevents all empires researching same thing
- Encourages contact/trade/war for technology transfer
- Creates asymmetry between empires

**Mechanics** (`DiscoverySystem.java:171-175`, `328-329`):
- Neighbors = diplomatic contacts + shared influence tiles
- Their discoveries added to local pool
- After discovery: removed from `toDiscover` (if has prerequisites)

---

## Events System

### Event Framework

**Decision**: Generic event system with progress accumulation per empire.

**Design** (`EventsSystem.java:14-36`):
```java
interface EventHandler {
  int getProgress(Entity e);      // Progress this turn
  String getReason(Entity e);     // Display reason
  boolean execute(Entity e);      // Trigger event
  String getType();               // Name
}
```

**Processing** (`EventsSystem.java:62-89`):
- Each empire has score per event type
- Each turn: score += progress
- Highest scoring event executes (if execute() returns true)
- Score resets on execution

### Event Types

| System | Event | Trigger |
|--------|-------|---------|
| `DiscoverySystem` | New discovery available | Progress threshold reached |
| `RevoltSystem` | City revolt | Low stability |
| `DisasterSystem` | Natural disaster | Random/probabilistic |
| `PoliciesSystem` | Policy change | Cultural progress |

---

## Diplomacy System

### Relation States

**Decision**: Five discrete relation states with directed transitions.

**States** (`DiplomaticSystem.java:47-49`):
- `NONE` - No formal relation
- `WAR` - Hostile
- `TREATY` - Peaceful cooperation
- `PROTECTORATE` - One protects other (asymmetric)
- `TRIBUTE` - One pays tribute to other (asymmetric)

### Proposal Mechanism

**Decision**: Mutual proposal compatibility determines state changes.

**Actions** (`DiplomaticSystem.java:51-86`):
| Action | After Me | After You | Valid From | Compatible With |
|--------|----------|-----------|------------|-----------------|
| DECLARE_WAR | WAR | WAR | !WAR | Always |
| MAKE_PEACE | NONE | NONE | WAR | MAKE_PEACE |
| SIGN_TREATY | TREATY | TREATY | WAR,NONE,TRIBUTE,PROTECTORATE | MAKE_PEACE |
| SURRENDER | TRIBUTE | PROTECTORATE | WAR | MAKE_PEACE, SIGN_TREATY |

**Processing** (`DiplomaticSystem.java:130-145`):
- Each turn: check all pending proposals
- If compatible with target's proposal → execute
- Cooldown: `lastChange` prevents rapid flipping

### AI Diplomacy

**Decision**: AI proposes based on faction preferences and relation history.

**Implementation** (`AIDiplomaticSystem.java`):
- Military faction → more DECLARE_WAR
- Economic faction → more SIGN_TREATY
- Cultural → balanced

---

## Effects System

### Effect Types

**Decision**: Centralized effect application with revert support.

**Effects** (`EffectsSystem.java:221-232`):
| Effect Key | Target | Revertible |
|------------|--------|------------|
| stability | Policies.stabilityGrowth/Max | Yes |
| discovery | Discoveries.progressPerTurn | Yes |
| growth | InfluenceSource.growth | Yes |
| health | InfluenceSource.health | Yes |
| diplomacy | Diplomacy.knownStates | No (one-way) |
| militaryPower | ArmyCommand.militaryPower | Yes |
| {TERRAIN} | InfluenceSource.modifiers.terrainBonus | Yes |

### Effect Application

**Decision**: Effects applied twice on discovery (immediate + bonus until next).

**Process** (`DiscoverySystem.java:336-339`):
```java
// Remove previous discovery's 2x bonus
if (last != null) effects.apply(last.effects, empire, true);
// Apply new discovery
effects.apply(target.effects, empire, false);
// Bonus: apply again until next discovery
effects.apply(target.effects, empire, false);
```

**Rationale**: Creates "golden age" feeling after discoveries; prevents stacking.

---

## Military System

### Army Mechanics

**Decision**: Armies as secondary influence sources with movement commands.

**Components** (`model/components/Army.java`, `ArmyCommand.java`):
- `Army` - Current power, position
- `ArmyCommand` - Destination, military power (base + discoveries)

### AI Army Control

**Decision**: Separate systems for command decisions and movement execution.

**Systems**:
- `AIArmyCommandSystem` - Chooses targets (expansion, defense, attack)
- `AIArmyMovementSystem` - Pathfinding and movement per turn
- `AISourceDestinationSystem` - Manages army influence sources

**Pathfinding** (`model/map/PathFinding.java`):
- A* on hex grid with terrain costs
- Respects influence borders (treaty = cannot enter)

---

## Policies & Stability

### Policy System

**Decision**: Policies unlocked by discoveries, provide ongoing modifiers.

**Mechanics** (`PoliciesSystem.java`):
- Discoveries in certain groups (GOVERNMENT, VALUE, etc.) unlock policies
- Player chooses active policy per category
- Policies modify: stability, growth, military, discovery speed

### Stability

**Decision**: Empire stability affects revolt probability.

**Mechanics**:
- `Policies.stabilityGrowth` - Per-turn stability change
- `Policies.stabilityMax` - Cap
- Low stability → revolt events (`RevoltSystem`)
- Revolts reduce city power, can destroy empire

---

## Special Discoveries

**Decision**: Certain discoveries trigger unique one-time effects.

**Examples** (`SpecialDiscoveriesSystem.java`):
- `City` - Spawns new city entity
- `Village` - Upgrades settlement
- `Writing` - Unlocks diplomacy states (TREATY, PROTECTORATE)
- `Wheel` / `Road` - Movement bonuses
- `Coinage` / `Money` - Economic effects

---

## Data Files

### Cultures (`cultures/antiquity.json`)
- Name, city name pool, AI faction preferences

### Discoveries (per era: `prehistory.json`, `antiquity.json`, `classic.json`, `nature.json`)
- Full tech tree with prerequisites, terrain requirements, effects

### Map Settings (`SessionSettings.java`)
- Map generator type, size, noise
- Number of empires, difficulty
- Starting discoveries

---

## Design Decisions Summary

| Area | Decision | Key Rationale |
|------|----------|---------------|
| Core Loop | Influence spread per turn | Macro management, visual clarity |
| Progression | Discovery tree with faction choice | Replayability, roleplay, asymmetry |
| Conflict | Influence competition + armies | No micro combat, territorial focus |
| Diplomacy | Proposal compatibility | Emergent narratives, player agency |
| Map Gen | Midpoint displacement | Natural continents, configurable |
| Events | Accumulating progress + highest wins | Predictable timing, chaotic outcomes |
| AI | Faction-weighted decisions | Personality, exploitable patterns |
| Data | JSON-driven content | Moddable, designer-friendly |

---

## Known Issues / FIXMEs

1. **`ThingsThatWereGame.java:35-41`**: Window height > screen height causes menu layout bug on resize
2. **`Culture.java:12`**: TODO - prevent modification after loading
3. **Diplomacy AI** (`README.md:35`): "Quite erratic with AI empires"
4. **Discovery UI** (`README.md:36`): "Need graphical enhancements (and more discoveries past Antiquity)"
5. **Player Control** (`README.md:37`): "Still too small"

---

## Extension Points

### Adding New Discoveries
1. Add entry to appropriate era JSON
2. Define prerequisites, terrains, effects, groups
3. Run `DiscoveriesToPlantuml.java` to update dependency graph

### Adding New Terrain
1. Add to `Terrain.java` enum
2. Set move cost, color, canStart
3. Update `HexMapGenerator` thresholds
4. Add terrain effect handler in `EffectsSystem`

### Adding New Event Type
1. Implement `EventHandler` interface
2. Register in `DiscoverySystem.initialize()` or similar
3. Add UI notification in `NotificationsSystem`

### Adding New Policy
1. Add to `Policy.java` enum
2. Link to discovery group in `PoliciesSystem.getPolicies()`
3. Implement effect in `PoliciesSystem.apply()`

---

## Generated Documentation

This document was generated from:
- Source code (`core/src/com/galvarez/ttw/`)
- JSON data files (`core/assets/data/`)
- PlantUML dependency graph (`discoveries.plantuml`)
- Commit messages (via `git log`)
- README.md design objectives

**Last Updated**: 2026-11-21
**Version**: Based on commit `d20b7f6` (Gradle 9.9.2 / Java 25)