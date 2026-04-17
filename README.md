# hitorro-core

Foundation module for the HiTorro framework (v3.0.1, Java 21). This is the root dependency — all other HiTorro modules build on it. It provides JSON path navigation, file system abstraction with transparent compression, a composable iterator/mapper/sink pipeline, caching, HTML parsing, URL normalization, and 780+ utility classes.

hitorro-core has **zero dependencies on other HiTorro modules** and can be used standalone.

---

## Table of Contents

- [Build & Test](#build--test)
- [Architecture](#architecture)
- [Package Map](#package-map)
- [Propaccess — JSON Path Navigation](#propaccess--json-path-navigation)
- [Property Mapping](#property-mapping-jsonkeys)
- [BaseFile — File System Abstraction](#basefile--file-system-abstraction)
- [Compression](#compression)
- [Iterator / Mapper / Sink Pipeline](#iterator--mapper--sink-pipeline)
- [Caching & Event System](#caching--event-system)
- [HTML Parsing](#html-parsing)
- [URL Parsing & Normalization](#url-parsing--normalization)
- [JSON Utilities](#json-utilities)
- [Core Utilities Quick Reference](#core-utilities-quick-reference)
- [Type System Base Interfaces](#type-system-base-interfaces)
- [I/O & CSV Processing](#io--csv-processing)
- [Threading](#threading)
- [Dependencies](#dependencies)
- [Test Coverage](#test-coverage)

---

## Build & Test

```bash
# Build hitorro-core and install to local Maven repo
mvn clean install -pl hitorro-core

# Build without tests
mvn clean install -pl hitorro-core -DskipTests

# Run all tests (534 tests)
mvn test -pl hitorro-core

# Run tests for a specific package
mvn test -pl hitorro-core -Dtest="com.hitorro.util.html.*"

# Run a single test class
mvn test -pl hitorro-core -Dtest=HTMLParserTest

# Run a single test method
mvn test -pl hitorro-core -Dtest="HTMLParserTest#shouldExtractTextFromBody"

# Deploy to the local file-based Maven repo (~/code/hitorro-maven)
./build-and-deploy.sh --clean

# Check dependency tree
mvn dependency:tree -pl hitorro-core
```

**Requirements:** Java 21+, Maven 3.8+

**Maven coordinates:**
```xml
<dependency>
    <groupId>com.hitorro</groupId>
    <artifactId>hitorro-core</artifactId>
    <version>3.0.1</version>
</dependency>
```

---

## Architecture

hitorro-core is the base layer of the HiTorro dependency hierarchy:

```
hitorro-core                 ← this module (no HiTorro dependencies)
     │
hitorro-jsontypesystem       ← JVS, type definitions, NLP
     │
hitorro-util                 ← command framework, scheduling, networking
     │
hitorro-base                 ← document processing, XML-RPC
     │
domain modules               ← features, index, kvstore, text, analysis, etc.
```

**Design principles:**
- **Zero-allocation path navigation** — Propaccess caches parsed paths
- **Transparent compression** — BaseFile auto-detects .gz/.bz2/.zstd and wraps streams
- **Composable pipelines** — AbstractIterator chains map/filter/flatMap/sink lazily
- **Protocol-agnostic I/O** — same BaseFile API for local files, HDFS, S3, FTP, ZIP
- **Weak-reference event bus** — LocalEventHub prevents memory leaks in long-running services
- **Configuration-driven** — property types read from JSON configs via Propaccess

---

## Package Map

```
com.hitorro.util.core/                   Core utilities (376 files)
├── classes/                             Reflection: ClassUtil, ClassPathDeepIterator
├── string/                              StringUtil, Fmt, StringBuilderUtil
├── hash/                                FPHash64 (64-bit fingerprint hashing)
├── iterator/                            AbstractIterator + 47 iterator classes
│   ├── mappers/                         BaseMapper, MapperCollection, DummyBaseMapper
│   ├── sinks/                           Sink, BaseSink, SinkList, TeeSink, MappingSink
│   ├── reducers/                        ListReducer
│   └── queue/                           AbstractEnqueue, AbstractDequeue
├── events/                              LocalEventHub, EventListener, WeakReferenceList
│   └── cache/                           HashCache, SingletonCache, PooledObjectCache
├── map/                                 LRUHashMap, MapUtil
├── opers/                               HTPredicate, LogicalAnd/Or/NotOperator
├── params/                              HTProperties, GlobalProperties, JsonKeyMap
├── thread/                              EnhancedThreadFactory, CountingSemaphore
├── error/                               ErrorCode
├── collection/                          Specialized collections
├── date/                                Date utilities
└── http/                                HTTPClient

com.hitorro.util.json/                   JSON processing (87 files)
├── keys/                                23 Property types (StringProperty, IntegerProperty, etc.)
│   ├── propaccess/                      Propaccess, PAContext, PropaccessIterator
│   └── mappers/                         JSON-to-type mappers
├── iterators/                           HTJSONIterator, KeyValueIterator
├── operators/                           JSON operators
└── visitors/                            JSON tree visitors

com.hitorro.util.io/                     I/O layer (141 files)
├── csv/                                 CSV reading/writing, formatters, consumers
├── largedata/                           Blob storage, bucketed writers
│   └── compressedstreams/               CInputStream, COutputStream, FSInputStream
├── resourcecache/                       Resource caching with polling
├── filedirwatch/                        File system monitoring
└── net/                                 Network utilities

com.hitorro.util.basefile/               File system abstraction (88 files)
├── fs/                                  BaseFile, BaseFileSystem, CompressionType
│   ├── file/                            FileFile, FileFileSystem (local)
│   ├── dfs/                             DFSFile, DFSFileSystem (HDFS)
│   ├── s3/                              HTS3FileSystem, S3Config (AWS S3)
│   ├── ftp/                             FileTranProtFile, FTPFileSystem
│   └── jarfile/                         JarFileFile, ZipFileFile (archives)
├── filters/                             FileExtension, GlobFilter, IsDir, etc.
├── tools/                               BaseLock, BaseFileUtil, directory tools
│   └── queue/                           Queue-based writers, partitioners
└── configfactories/                     Protocol adapters, config readers

com.hitorro.util.html/                   HTML parsing (31 files)
├── constraint/                          Link constraints (And, Or, Type, URL-based)
└── contentsniffers/                      MIME type detection (HTML, RSS, Atom, CSS)

com.hitorro.util.urlparser/              URL utilities (18 files)
com.hitorro.util.typesystem/             Type system base interfaces (31 files)
├── annotation/                          TypeClassMetaInfo, etc.
└── valuesource/                         Value source abstraction

com.hitorro.util.testframework/          Test utilities (6 files)
com.hitorro.util.log/                    Logger implementation
```

---

## Propaccess — JSON Path Navigation

The `Propaccess` system is the backbone of HiTorro's JSON processing. It provides dot-notation path navigation with caching for Jackson `JsonNode` trees.

```java
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PAContext;

// Create a path (cached internally)
Propaccess path = new Propaccess("title.mls[0].text");

// Read a value — returns null if path doesn't exist
JsonNode value = path.get(null, rootNode, PAContext.NeverCreate);

// Write a value — creates intermediate objects/arrays automatically
path.set(null, rootNode, PAContext.AlwaysCreate, TextNode.valueOf("Hello"));
```

**Path syntax:**
| Pattern | Meaning |
|---------|---------|
| `field.nested` | Dot-separated field traversal |
| `items[0]` | Array index access |
| `title.mls[0].text` | Combined object + array paths |
| `metadata.tags[*]` | Wildcard array iteration |

**PAContext modes:**
- `PAContext.AlwaysCreate` — creates missing intermediate objects and arrays
- `PAContext.NeverCreate` — returns null for any missing path segment

---

## Property Mapping (json.keys)

Declarative, type-safe property extraction from JSON. Each property wraps a Propaccess path with type conversion and defaults.

```java
import com.hitorro.util.json.keys.*;

// Declare properties with path, description, and default value
StringProperty name = new StringProperty("user.name", "User's full name", null);
IntegerProperty age = new IntegerProperty("user.age", "Age in years", 0);
BooleanProperty active = new BooleanProperty("user.active", "Account active", false);
DateProperty created = new DateProperty("user.createdAt", "Creation date");
FileProperty config = new FileProperty("paths.config", "Config file path", "");

// Apply to a JsonNode
String userName = name.apply(jsonNode);  // null if missing
int userAge = age.apply(jsonNode);       // 0 if missing (default)
boolean isActive = active.apply(jsonNode);
```

**Available property types (23):**

| Type | Java Type | Notes |
|------|-----------|-------|
| `StringProperty` | String | With optional validation |
| `IntegerProperty` | int | |
| `LongProperty` | long | |
| `DoubleProperty` | double | |
| `BooleanProperty` | boolean | |
| `DateProperty` | Date | Multiple format support |
| `FileProperty` | File | Path resolution |
| `EnumProperty<E>` | E | Enum mapping |
| `CollectionProperty` | List | JSON array → Java list |
| `MapProperty` | Map | JSON object → Java map |
| `ClassProperty` | Class<?> | Class loading by name |
| `ClassInstantiationProperty` | Object | Load + instantiate |
| `URLProperty` | URL | URL parsing |
| `BasefileProperty` | BaseFile | BaseFile resolution |
| `ValidatedStringProperty` | String | With validation predicates |
| `ResolvableStringProperty` | String | Environment variable resolution |
| `ArrayProperty` | Array | Typed array extraction |
| `PropaccessProperty` | Propaccess | Nested path extraction |

---

## BaseFile — File System Abstraction

Unified API for local files, HDFS, S3, FTP, and ZIP archives. Transparent compression means a file named `data.csv.gz` is automatically gzip-compressed on write and decompressed on read.

### Basic Usage

```java
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.fs.BaseFile;

// Local file system rooted at a directory
FileFileSystem fs = new FileFileSystem(new File("/data"));
BaseFile file = fs.getFile("documents/report.json");

// Read/write
file.writeString("Hello, World!");
String content = file.readString();

// JSON
JsonNode node = file.getJsonNode();
file.writeJson(objectNode);

// Directory operations
BaseFile dir = fs.getFile("output");
dir.mkdir();
BaseFile[] children = dir.listFiles();
BaseFile[] jsonFiles = dir.listFiles("*.json");  // glob pattern

// Navigation
BaseFile parent = file.getParent();
BaseFile sibling = file.getPeer("other.json");
BaseFile child = dir.getChild("subdir/file.txt");
```

### Protocol-Based Access

```java
import com.hitorro.util.basefile.fs.BaseFileSystem;

// Automatic protocol detection
BaseFile local = BaseFileSystem.getBaseFileFromPath("file:///data/file.txt");
BaseFile hdfs  = BaseFileSystem.getBaseFileFromPath("hdfs://namenode:9000/data/file.txt");
BaseFile s3    = BaseFileSystem.getBaseFileFromPath("s3a://bucket/key/file.txt");
```

### File Filters

```java
import com.hitorro.util.basefile.filters.FileFilterBase;

BaseFile[] csvFiles = dir.listFiles(FileFilterBase.hasExt("csv"));
BaseFile[] logs     = dir.listFiles(FileFilterBase.startsWith("log-"));
BaseFile[] dirs     = dir.listFiles(FileFilterBase.isDir());
BaseFile[] notTmp   = dir.listFiles(FileFilterBase.not(FileFilterBase.endsWith(".tmp")));
BaseFile[] glob     = dir.listFiles(FileFilterBase.glob("data-*.{csv,json}"));
```

### Streams (with Transparent Compression)

```java
// Compressed automatically based on file extension
try (OutputStream os = file.getOutputStream()) {   // compresses if .gz/.bz2/.zstd
    os.write(data);
}
try (InputStream is = file.getInputStream()) {     // decompresses automatically
    byte[] data = is.readAllBytes();
}

// Raw access (bypass compression)
InputStream raw = file.getInputStreamRaw();
```

### Path Bridge (Local Files)

```java
// Bridge to java.nio.file.Path (FileFile only)
FileFile localFile = fs.getFile("data.txt");
java.nio.file.Path path = localFile.toPath();
java.io.File javaFile = localFile.getLocalFileIfPossible();
```

---

## Compression

Transparent compression via the `CompressionType` enum. Detection is by file extension.

| Extension | Type | Library |
|-----------|------|---------|
| `.gz` | GZIP | java.util.zip |
| `.bz2` | BZIP2 | Hadoop BZip2Codec |
| `.zstd` | Zstandard | zstd-jni |
| (other) | None | Pass-through |

```java
// Compression is transparent through BaseFile
BaseFile gzipped = fs.getFile("data.csv.gz");
gzipped.writeString("large dataset...");  // auto-compressed
String data = gzipped.readString();        // auto-decompressed

// Direct compression type access
CompressionType ct = CompressionType.getFilterByFileName("data.csv.gz"); // → gz
InputStream decompressed = ct.getInputCompressed(rawInputStream);
OutputStream compressed = ct.getOutputStreamCompressed(rawOutputStream);
```

---

## Iterator / Mapper / Sink Pipeline

A composable, lazy-evaluation data processing pipeline. `AbstractIterator` extends `Iterator<E>` and `Iterable<E>` with chainable operations.

### Chaining Operations

```java
import com.hitorro.util.core.iterator.*;

// Build a pipeline
List<String> results = sourceIterator
    .filter(item -> item.isValid())         // filter
    .map(item -> item.transform())          // transform
    .peek(item -> log.info("Processing: {}", item))  // side-effect
    .distinct()                             // remove duplicates
    .takeWhile(item -> item.score() > 0.5)  // stop when condition fails
    .toList();                              // terminal: collect to list

// Pagination
List<Item> page = iterator.skipNTakeM(100, 25, false).toList();  // skip 100, take 25

// Counting
AtomicLong counter = new AtomicLong();
iterator.count(counter).forEach(item -> process(item));
// counter.get() == number of items processed

// Parallel processing
List<Result> results = iterator
    .mapParallel(item -> expensiveTransform(item), 8, 100, 100, "transform")
    .toList();
```

### Flattening (flatMap / nest)

```java
// Expand each item into multiple items
AbstractIterator<Line> allLines = fileIterator
    .flatMap(file -> new LineReaderIterator(file.getReader()));
```

### Sink (Terminal Operations)

```java
import com.hitorro.util.core.iterator.sinks.*;

// Sink to a list
SinkList<Item> sink = new SinkList<>();
iterator.sink(sink);
List<Item> items = sink.getList();

// Sink from a lambda
Sink<Item> dbSink = Sink.of(item -> database.save(item));
int count = iterator.sink(dbSink);

// Counting sink
CountingSink<Item> counter = Sink.counting();
iterator.sink(counter);
long total = counter.getCount();

// Compose sinks
Sink<Item> pipeline = targetSink
    .filter(item -> item.isValid())    // filter before sink
    .map(item -> item.toEntity())       // transform before sink
    .tee(auditSink);                    // broadcast to two sinks
```

### Mapper Composition

```java
import com.hitorro.util.core.iterator.mappers.BaseMapper;

// Reusable mapper chains
BaseMapper<BaseFile, InputStream> fileToStream = ...;
BaseMapper<InputStream, List<String>> streamToLines = ...;
BaseMapper<BaseFile, List<String>> fileToLines = fileToStream.combine(streamToLines);

// Apply
List<String> lines = fileToLines.apply(someFile);
```

### Stream Interop

```java
// AbstractIterator → Java Stream
try (Stream<Item> stream = iterator.toStream()) {
    stream.filter(...).map(...).collect(Collectors.toList());
}

// Java Stream → AbstractIterator
AbstractIterator<Item> iter = AbstractIterator.fromStream(stream);
iter.filter(...).map(...).sink(sink);

// Java Collector support
String joined = iterator.collect(Collectors.joining(", "));
```

---

## Caching & Event System

### LocalEventHub

Lightweight publish-subscribe event bus with weak references (listeners are GC'd automatically if not held elsewhere).

```java
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.events.EventListener;

// Register a listener
LocalEventHub.get().addEventListener(myListener, "myTopic");

// Fire events
LocalEventHub.get().event("myTopic", "subEvent", payloadObject);
LocalEventHub.get().fire("myTopic", "subEvent");  // no-args shorthand

// Remove listener
LocalEventHub.get().removeEventListener(myListener, "myTopic");

// Diagnostics
int count = LocalEventHub.get().getListenerCount("myTopic");
```

### Caches (Event-Driven)

All caches register with LocalEventHub for flush/invalidation events.

```java
// SingletonCache — one value, lazy-loaded
SingletonCache<Config> configCache = new SingletonCache<>("config", loader);
Config config = configCache.get();  // loaded on first access

// HashCache — keyed cache
HashCache<String, UserProfile> userCache = new HashCache<>(
    60000,  // refresh interval ms (0 = no auto-refresh)
    true,   // allow nulls
    null,   // null flyweight
    "users",
    key -> loadProfile(key)  // loader
);
UserProfile profile = userCache.get("user123");

// Flush via event
LocalEventHub.get().event("users", Cache.FlushCache, null);
```

---

## HTML Parsing

Parse HTML documents, extract text, links, metadata, and structured data. Uses CyberNeko HTML Parser for tolerant parsing of malformed HTML.

```java
import com.hitorro.util.html.HTMLParser;

HTMLParser parser = new HTMLParser(htmlString);

// Text extraction
String bodyText = parser.getBodyText();        // text from <body>, scripts/styles filtered
String title = parser.getTitleText();           // <title> text
String description = parser.getMetaDescription();
String keywords = parser.getMetaKeywords();

// Link extraction
List<Link> links = parser.getLinks("HTML", new URL("http://example.com"));
List<Link> anchors = parser.getLinks("HTML", sourceUrl,
    new TypeLinkConstraint(Link.LinkType.Anchor), null);

// Structured data (JSON-LD)
List<JsonNode> jsonLd = parser.getStructuredData();

// Open Graph tags
Map<String, String> ogTags = parser.getOpenGraphTags();

// Canonical URL
String canonical = parser.getCanonicalUrl();

// Headings hierarchy
List<GenericKeyValue<String, String>> headings = parser.getHeadings();

// Individual text blocks per tag
List<String> paragraphs = parser.getTextsByTag("p");

// Redirect detection
String redirectUrl = parser.getRedirectURL("http://original.com");

// HTML entity decoding
String decoded = HTMLEncoder.decodeHtml("&amp; &lt;p&gt; &euro;");
```

---

## URL Parsing & Normalization

Zero-allocation URL tokenization and RFC-based normalization.

```java
import com.hitorro.util.urlparser.*;

// URL cleanup and normalization
String clean = URLUtil.cleanupUrl("http://example.com/path%20to/page");

// Extract components
String host = URLUtil.getHost("http://www.example.com/path");   // "www.example.com"
String path = URLUtil.getPath("http://example.com/foo/bar");    // "/foo/bar"
String site = URLUtil.getSiteFromURL("http://www.cnn.com/world"); // "http://www.cnn.com"

// Query parameter extraction
Map<String, String> params = URLUtil.getQueryParameters(
    "http://example.com/search?q=hello+world&lang=en");
// {"q": "hello world", "lang": "en"}

// Validation
boolean valid = URLUtil.isValidUrl("http://example.com");  // true
boolean invalid = URLUtil.isValidUrl("not-a-url");         // false

// URL normalization (RFC rules)
UrlNormalizer normalizer = new UrlNormalizer(false, true, true);  // flipHost, includeHttp, removeWww
String normalized = normalizer.normalize("HTTP://WWW.Example.COM:80/path/../page?z=1&a=2");
// → "http://example.com/page?a=2&z=1"  (lowercased, www removed, port 80 stripped, path resolved, args sorted)

// Zero-allocation URL cursor (for high-throughput parsing)
UrlCursor cursor = new UrlCursor();
cursor.setUrl("http://example.com/path?key=value");
while (cursor.nextToken()) {
    UrlCursor.Part type = cursor.getUrlPartType();  // Host, Path, Argument, Port, Anchor
    String token = cursor.getToken();
}

// Build URLs with encoded parameters
UrlCreator creator = new UrlCreator();
creator.setUrl("http://api.example.com/search");
creator.addArg("q", "hello world");
creator.addArg("lang", "en");
String url = creator.getUrl();  // params URL-encoded
```

---

## JSON Utilities

```java
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.String2JsonMapper;

// Parse JSON from string
JsonNode node = new String2JsonMapper().apply("{\"key\": \"value\"}");

// Type-safe extraction with defaults
String s = JSONUtil.getString(node);          // string value or null
long l = JSONUtil.getLong(node, 0L);           // long value or default
boolean b = JSONUtil.getBoolean(node, false);
List<String> list = JSONUtil.getStringList(node);
Date d = JSONUtil.getDate(node);

// Check for content
boolean empty = JSONUtil.isNullOrEmpty(node);
```

---

## Core Utilities Quick Reference

| Class | Package | Key Methods |
|-------|---------|-------------|
| `Log` | `core` | `Log.util.info(...)`, `Log.test.error(...)` — named logger facade |
| `Env` | `core` | `getBin()`, `getHTHome()`, `getBaseFile(File)` — environment access |
| `Fmt` | `core.string` | `Fmt.S("Hello %s", name)` — printf-style formatting |
| `StringUtil` | `core.string` | `nullOrEmptyString()`, `join()`, `truncateToLength()`, `tokenize()` |
| `ClassUtil` | `core.classes` | `isAbstract()`, `isSubClass()`, `getBareName()`, `translateClassFilename()` |
| `FPHash64` | `core.hash` | `getFP(String)` — 64-bit fingerprint hash |
| `ArrayUtil` | `core` | `nullOrEmpty()`, array operations |
| `FileUtil` | `io` | `getFileExtension()`, `ensureParentDirectories()`, stream utilities |
| `IOUtil` | `io` | `copyStream()` — stream copying with size validation |
| `CPUInfo` | `core` | `getCPUClockSpeedGHz()` — cross-platform CPU speed detection |
| `Timer` | `core` | Simple elapsed-time measurement |
| `Constants` | `core` | Time constants (`MillisInDay`, `KBytes`, `MBytes`), ASCII values |

---

## Type System Base Interfaces

hitorro-core defines the interfaces that the JSON Type System (hitorro-jsontypesystem) implements:

```java
// Type interface
public interface TypeBaseIntf {
    FieldBaseIntf getField(String field);
    String getName();
    PAContext getPaContext();
}

// Field data types
public enum TypeFieldDataType {
    Long, Int, Short, Byte, Double, Float, String, Date, Boolean, HTSerializable
}

// Serialization
public interface HTSerializable {
    void serialize(HTObjectOutputStream os) throws IOException;
    void deserialize(HTObjectInputStream os) throws IOException;
    int getSerializationVersion();
}
```

---

## I/O & CSV Processing

```java
// CSV reading
CSVIterator csv = new CSVIterator(reader, ',', true);  // reader, delimiter, hasHeader
while (csv.hasNext()) {
    String[] row = csv.next();
}

// Line-by-line file reading
AbstractIterator<String> lines = new LineReaderIterator(file.getReader());
lines.filter(line -> !line.startsWith("#"))
     .map(String::trim)
     .forEach(System.out::println);

// JSON streaming from file
AbstractIterator<JsonNode> jsonIter = file.getJsonIterator();
jsonIter.map(node -> process(node)).sink(outputSink);

// Resource caching (poll for changes)
ResourceToPoll resource = new ResourceToPoll(file, interval);
```

---

## Threading

```java
import com.hitorro.util.core.thread.*;

// Enhanced thread factory (named threads for debugging)
ExecutorService exec = Executors.newCachedThreadPool(
    new EnhancedThreadFactory("pool", "Worker-%d", true));

// Parallel iterator processing
AbstractIterator<Result> results = sourceIterator
    .mapParallel(item -> compute(item), 8, 100, 100, "compute-pool");
```

---

## Dependencies

hitorro-core depends only on third-party libraries (no HiTorro module dependencies):

| Dependency | Version | Purpose |
|------------|---------|---------|
| Jackson | 2.18.2 | JSON processing (databind, core, annotations) |
| Hadoop Client | 3.4.1 | HDFS and S3A file system support |
| AWS SDK v2 | 2.29.29 | S3 via hadoop-aws |
| Apache HttpClient | 4.5.14 / 5.4.1 | HTTP I/O |
| Commons Codec | 1.16.1 | MD5, Base64, encoding |
| Commons Net | 3.11.1 | FTP protocol |
| NekoHTML | 1.9.22 | Tolerant HTML parsing |
| Log4j | 1.2.17 | Logging backend |
| Apache POI | 5.3.0 | Excel/Office file reading |
| Groovy | 3.0.23 | Groovy script support |
| Trove4j | 3.1.0 | Primitive collections |
| Zstandard (zstd-jni) | 1.5.6-4 | Zstandard compression |
| AspectJ | 1.9.22.1 | AOP support |
| Metadata Extractor | 2.19.0 | Image/media metadata |
| Colt | 1.2.0 | Matrix/scientific computing |
| JUnit 5 | 5.x | Testing (test scope) |
| AssertJ | 3.27.0 | Fluent assertions (test scope) |

---

## Test Coverage

534 tests across 29 test classes covering:

| Area | Test Classes | Tests |
|------|-------------|-------|
| HTML Parser | HTMLParserTest, HTMLEncoderTest, HTMLUtilTest, TagFinderTest, LinkTest | 99 |
| BaseFile & Filters | BaseFileTest, FileFileTest, CompressionTypeTest, FileFiltersTest | 82 |
| Iterators & Sinks | AbstractIteratorTest, FilteringIteratorTest, MappingIteratorTest, SinkTest, MapperTest, TupleIteratorTest, IteratorInfrastructureTest | 49+ |
| URL Parser | URLUtilTest, UrlCursorTest, UrlNormalizerTest, UrlCreatorTest, UrlEditDistanceTest | 65 |
| Event System | LocalEventHubTest, WeakReferenceListTest | 24 |
| Core Utilities | StringUtilTest, JSONUtilTest, FileUtilTest, and others | 200+ |

Run the full suite with:
```bash
mvn test -pl hitorro-core
# Tests run: 534, Failures: 0, Errors: 0
```
