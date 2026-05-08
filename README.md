# Business Game 🏦

A Monopoly-style business simulation game built in Java, featuring real-time stock price fluctuations, property trading across 26 world cities, and multi-player account management — all played through an interactive console.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [How to Play](#how-to-play)
- [Class Reference](#class-reference)
- [Known Bugs Fixed](#known-bugs-fixed)
- [Future Improvements](#future-improvements)

---

## Features

| Feature | Description |
|---|---|
| **Multi-player accounts** | Up to 100 player accounts with PIN authentication |
| **Live stock market** | Background thread fluctuates the share price ±$10 every 10 seconds |
| **ASCII stock chart** | Price history visualised in the terminal |
| **Property trading** | Buy/sell city properties across 26 world cities |
| **Round inflation** | Type `11` to end a round and apply 5 % price inflation to all properties |
| **Fund transfers** | Securely move money between accounts |
| **Save to file** | Persist account balances to `accounts.txt` |

---

## Project Structure

```
BusinessGame/
├── pom.xml                                 ← Maven build file
├── README.md
└── src/
    └── main/
        └── java/
            └── com/businessgame/
                ├── Main.java               ← Entry point & dependency wiring
                │
                ├── model/                  ← Pure domain objects (no I/O)
                │   ├── Account.java        ← Player account: balance, shares, properties
                │   └── Property.java       ← City/country: name, price, index
                │
                ├── service/                ← Business logic layer
                │   ├── AccountRegistry.java    ← In-memory account store
                │   ├── PropertyMarket.java     ← Property catalogue & round inflation
                │   ├── StockMarket.java        ← Thread-safe share price + fluctuator
                │   ├── StockChart.java         ← ASCII chart renderer
                │   └── SaveService.java        ← Persist accounts to file
                │
                ├── ui/                     ← Console I/O handlers
                │   ├── GameMenu.java           ← Main game loop & top-level menu
                │   ├── AccountUI.java          ← Account commands (create, balance, …)
                │   ├── PropertyUI.java         ← Property sub-menu
                │   └── MarketUI.java           ← Stock market sub-menu
                │
                └── util/
                    └── InputHelper.java        ← Safe Scanner wrapper with retry logic
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────┐
│                   Main.java                 │
│  Wires services → GameMenu → runs game loop │
└───────────────────┬─────────────────────────┘
                    │
        ┌───────────▼───────────┐
        │       GameMenu        │
        │  (top-level commands) │
        └──┬──────┬──────┬──────┘
           │      │      │
     AccountUI PropertyUI MarketUI
           │      │      │
           └──────┴──┬───┘
                     │  delegates to
          ┌──────────▼──────────┐
          │      Services       │
          │  AccountRegistry    │
          │  PropertyMarket     │
          │  StockMarket        │
          │  SaveService        │
          └──────────┬──────────┘
                     │  owns
          ┌──────────▼──────────┐
          │       Models        │
          │  Account            │
          │  Property           │
          └─────────────────────┘
```

All I/O is confined to the `ui` package. Services and models have **zero** `System.out` dependencies — they throw typed exceptions that the UI layer catches and presents as friendly messages.

---

## Getting Started

### Prerequisites

- **Java 17** or later
- **Maven 3.8+**  (or use the included wrapper if present)

### Build

```bash
mvn clean package
```

This produces a runnable fat-jar at `target/business-game-1.0.0.jar`.

### Run

```bash
java -jar target/business-game-1.0.0.jar
```

Or run directly from source:

```bash
mvn exec:java -Dexec.mainClass=com.businessgame.Main
```

---

## How to Play

On launch you will see the main menu. Type the number for the action you want and press Enter.

```
╔══════════════════════════════════════╗
║         BUSINESS GAME  v1.0          ║
╠══════════════════════════════════════╣
║   0  Check balance                   ║
║   1  Add balance                     ║
║   2  Withdraw funds                  ║
║   3  Property dealing                ║
║   4  Transfer funds                  ║
║   5  Stock market                    ║
║   7  Save game to file               ║
║   9  New account                     ║
║  11  End of round                    ║
║  99  Quit                            ║
╚══════════════════════════════════════╝
```

### Typical game flow

1. **Create accounts** — each player types `9` to register with a unique account number and PIN.
2. **Add starting money** — the banker types `1` and deposits the agreed starting amount (a 10 % bonus is added automatically).
3. **Take turns** — players buy/sell properties (`3`) and trade on the stock market (`5`).
4. **End each round** — type `11` to advance the round; all property prices rise by 5 %.
5. **Save progress** — type `7` to write balances to `accounts.txt`.
6. **Quit** — type `99` when the game ends.

### Available cities / countries

| Code | City         | Start Price |
|------|-------------|-------------|
| new  | New York    | $8,500      |
| rom  | Rome        | $3,000      |
| hon  | Hong Kong   | $2,500      |
| syd  | Sydney      | $4,500      |
| dub  | Dubai       | $5,000      |
| kua  | Kuala Lumpur| $3,500      |
| deh  | Delhi       | $4,500      |
| ber  | Berlin      | $5,000      |
| rai  | Riyadh      | $9,500      |
| zur  | Zurich      | $5,500      |
| tok  | Tokyo       | $3,500      |
| lon  | London      | $4,500      |
| air  | (top city)  | $10,500     |
| mos  | Moscow      | $5,000      |
| …    | …           | …           |

---

## Class Reference

### `model/Account`
Encapsulates all per-player state: balance, share count, property holdings, and PIN. Throws `IllegalArgumentException` / `IllegalStateException` on invalid operations — callers never check raw array bounds.

### `model/Property`
Immutable city descriptor with a mutable price. `applyRoundInflation()` applies the 5 % round increase.

### `service/AccountRegistry`
`HashMap`-backed store replacing the original parallel arrays. Provides `find(accountNumber)` and `authenticate(accountNumber, password)` returning `Optional<Account>`.

### `service/PropertyMarket`
Holds the catalogue of 26 `Property` objects. `findByName()` is case-insensitive. `applyRoundInflation()` delegates to each property.

### `service/StockMarket`
Maintains an `AtomicInteger` share price, safe for concurrent access. Starts a daemon thread that fluctuates the price ±$10 every 10 seconds and prints an updated chart.

### `service/StockChart`
Static utility — renders an ASCII bar chart of a price-history array. Separated from `StockMarket` so it can be tested independently.

### `service/SaveService`
Writes all account balances to `accounts.txt` using a `BufferedWriter` inside a proper try-with-resources block.

### `ui/GameMenu`
Main loop. Maps integer commands to handler calls. All sub-menus loop internally and return control here when the user exits.

### `ui/AccountUI`, `ui/PropertyUI`, `ui/MarketUI`
Thin console-interaction layers. Each reads input via `InputHelper`, calls service/model methods, and prints results or exception messages.

### `util/InputHelper`
Wraps `Scanner`. `readInt()`, `readLong()`, and `readWord()` retry indefinitely on bad input instead of crashing with `InputMismatchException`.

---

## Known Bugs Fixed

| # | Original issue | Fix |
|---|---|---|
| 1 | Data race on `static int stockprice` mutated from two threads | Replaced with `AtomicInteger` in `StockMarket` |
| 2 | `InputMismatchException` crash on non-integer input | `InputHelper` retries with a clear error message |
| 3 | Fernflower decompiler garbage (`var0`, `var10000`, nested `Throwable` catch) in `saveToFile` | Rewritten with proper try-with-resources |
| 4 | `static int o` counter in `main()` breaks if account creation fails (counter still incremented) | `AccountRegistry` uses a `HashMap`; no index needed |
| 5 | `checkPrice()` had a pointless `try/catch(Exception)` around `System.out.println` | Removed |
| 6 | `propertyDealing()` used `while(as == 0)` flag instead of a boolean | Replaced with clean `boolean active` loop |
| 7 | No validation — negative amounts, zero-division, array out-of-bounds all possible | Every model method validates input and throws typed exceptions |
| 8 | Stock price could go to 0 or negative with repeated bad ticks | `Math.max(MIN_PRICE, ...)` guard in fluctuator |
| 9 | `createAccount` used a separate `o` index that was never bounds-checked | Registry enforces `MAX_ACCOUNTS` with a clear error |
| 10 | No way to exit the game cleanly | Added command `99` and a JVM shutdown hook |

---

## Future Improvements

- **Persistence** — load `accounts.txt` on startup to resume a saved game
- **Password hashing** — use BCrypt instead of storing plain-text PINs
- **GUI** — Swing or JavaFX board view
- **Network play** — expose services over sockets or REST for remote players
- **Unit tests** — JUnit 5 tests for model layer (no I/O required)
- **Event system** — Chance/Community Chest cards that trigger random events
