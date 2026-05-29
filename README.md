# Ringit

A Spring Boot 4.0.6 property editor — store, view and manage typed application
properties (STRING, LONG, DECIMAL, BOOLEAN, DATE) grouped by app name, category
and property name.  Backed by SQLite, served on port 8080 with a built-in dark
themed single-page UI.

## Requirements

| Tool | Version |
|------|---------|
| Java | 25 |
| Maven | 3.9+ |

The helper scripts all call `. j25` to set `JAVA_HOME`.  
`j25` must be a script on your `PATH` (e.g. `/b/j25`) that exports `JAVA_HOME`
pointing to your JDK 25 installation.

---

## Scripts

### `bld` — build
Compiles and packages the fat JAR, then creates the `data/` directory if it does
not already exist.

```bash
./bld
```

### `rune` — run (embedded SQLite)
Starts the application using the embedded SQLite database stored at
`./data/ringitdb.db`.

```bash
./rune
```

### `runs` — run (remote SQLite-compatible server)
Starts the application pointed at a remote SQLite-compatible server.  
SQLite itself has no built-in network server; use a compatible server such as
[sqld / libSQL](https://github.com/tursodatabase/libsql) or
[rqlite](https://github.com/rqlite/rqlite).

Set `RINGIT_DB_URL` before running:

```bash
export RINGIT_DB_URL="http://127.0.0.1:8080"   # libSQL / sqld
# or for rqlite's HTTP API configure accordingly
./runs
```

If `RINGIT_DB_URL` is not set it falls back to the local embedded file, same as
`rune`.

#### Starting a local sqld server (libSQL)
```bash
# Install sqld (requires Rust toolchain or use a pre-built binary)
sqld --db-path ./data/ringitdb.db --http-listen-addr 127.0.0.1:8080
```

#### Starting a local rqlite server
```bash
# Download rqlite from https://github.com/rqlite/rqlite/releases
rqlited -http-addr 127.0.0.1:4001 -raft-addr 127.0.0.1:4002 ./data/rqlite
# Then configure the JDBC URL for the rqlite driver
```

> **Note:** using a remote server requires swapping the JDBC driver in `pom.xml`
> for the appropriate library (e.g. the libSQL Java client).  The embedded SQLite
> driver (`org.xerial:sqlite-jdbc`) only speaks to local files.

### `clr` — clear data
Kills any running Ringit process and deletes the SQLite database file so the
application starts fresh on the next launch.

```bash
./clr
```

---

## Configuration

Key settings live in `src/main/resources/application.yaml` and
`src/main/resources/application.properties`.

| Property | Default | Description |
|----------|---------|-------------|
| `ringit.db.url` | `jdbc:sqlite:./data/ringitdb.db` | JDBC URL for the database |
| `ringit.default-time-zone` | `Asia/Kolkata` | Default timezone for DATE properties |
| `server.port` | `8080` | HTTP port |

Supported timezones (shown in the UI dropdown for DATE type properties):
GMT, America/New_York, America/Chicago, America/Denver, America/Los_Angeles,
Europe/Berlin, Asia/Kolkata, Asia/Singapore, Asia/Tokyo.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/settings` | List all properties |
| `POST` | `/api/settings` | Create or update a property |
| `GET` | `/api/settings/{id}` | Get property by ID |
| `DELETE` | `/api/settings/{id}` | Delete property |
| `GET` | `/api/settings/lookup` | Lookup by `appName`, `category`, `propertyName` |
| `GET` | `/api/apps` | List distinct app names |
| `GET` | `/api/apps/{appName}/categories` | List categories for an app |
| `GET` | `/api/apps/{appName}/categories/{category}/names` | List property names |
| `GET` | `/api/time-zones` | List available timezones (Caffeine cached) |

Actuator endpoints available at `/actuator/health`, `/actuator/metrics`,
`/actuator/caches`.

---

## Typical workflow

```bash
./bld          # build once
./rune         # start
# open http://localhost:8080
./clr          # wipe data and stop when done
```
