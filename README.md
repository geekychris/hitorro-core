# hitorro-core

Foundation utilities for the HiTorro framework. This module provides the low-level infrastructure that all other HiTorro modules build on: JSON property access, file I/O abstractions, caching, iteration, and core utilities.

## Architecture

hitorro-core is the base layer of the HiTorro dependency hierarchy. It has no dependencies on other HiTorro modules and can be used independently.

```
hitorro-core (this module)
     |
hitorro-jsontypesystem (type system, depends on hitorro-core)
     |
hitorro-util (app infrastructure, depends on both)
     |
downstream modules
```

## Key Components

### Propaccess - JSON Path Navigation

The `Propaccess` system provides dot-notation path navigation for Jackson `JsonNode` trees. It is the primary mechanism for reading and writing nested JSON values throughout HiTorro.

```java
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PAContext;

// Create a path
Propaccess path = new Propaccess("title.mls[0].text");

// Navigate into a JsonNode tree
JsonNode value = path.get(null, rootNode, PAContext.AlwaysCreate);

// Set a value (creates intermediate nodes automatically)
path.set(null, rootNode, PAContext.AlwaysCreate, TextNode.valueOf("Hello"));
```

**Path syntax:**
- `field.nested` - dot-separated field access
- `items[0]` - array index access
- `title.mls[0].text` - combined paths
- Paths are cached internally for performance

**PAContext modes:**
- `PAContext.AlwaysCreate` - create missing intermediate nodes
- `PAContext.NeverCreate` - return null for missing paths

### Property Mapping (json.keys)

Declarative mapping between JSON structures and Java types. Properties read values from JSON using Propaccess paths.

```java
import com.hitorro.util.json.keys.*;

// Define typed properties
StringProperty name = new StringProperty("name", "User name", null);
IntegerProperty age = new IntegerProperty("age", "User age", 0);
BooleanProperty active = new BooleanProperty("active", "Is active", false);

// Apply to a JsonNode to extract values
String userName = name.apply(jsonNode);
int userAge = age.apply(jsonNode);
```

Available property types: `StringProperty`, `IntegerProperty`, `LongProperty`, `DoubleProperty`, `BooleanProperty`, `DateProperty`, `FileProperty`, `EnumProperty`, `CollectionProperty`, `MapProperty`, `ClassProperty`, `PropaccessProperty`.

### BaseFile - File System Abstraction

Unified file system API supporting local files, HDFS, and AWS S3.

```java
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;

// Local file system
BaseFile root = FileFileSystem.build("/path/to/dir");
BaseFile child = root.getChild("data/file.json");

// Read JSON
JsonNode node = child.getJsonNode();

// List children
List<BaseFile> files = root.list();
```

**Supported backends:**
- `FileFileSystem` - local file system
- `HadoopFileSystem` - HDFS
- `S3FileSystem` - AWS S3 via hadoop-aws

### Caching

Thread-safe caching infrastructure with lazy initialization and pooling.

```java
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.events.cache.SingletonCache;

// HashCache - keyed cache with loader
HashCache<String, MyObject> cache = new HashCache<>(
    0, true, null, "my-cache",
    key -> loadObject(key)  // loader function
);
MyObject obj = cache.get("key");  // lazy-loads on first access

// SingletonCache - single-value cache
SingletonCache<Config> config = new SingletonCache<>(Config::load);
Config c = config.get();
```

### Iteration Framework

Composable iterator pipeline with mappers, filters, and chaining.

```java
import com.hitorro.util.core.iterator.*;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

// Map over an iterator
AbstractIterator<Output> mapped = new MappingIterator<>(
    sourceIterator,
    new BaseMapper<Input, Output>() {
        public Output apply(Input item) { return transform(item); }
    }
);

// Filter
AbstractIterator<Item> filtered = new FilteringIterator<>(
    sourceIterator,
    item -> item.isValid()
);

// Chain multiple iterators
AbstractIterator<Item> chained = new ChainingIterator<>(iter1, iter2, iter3);
```

### JSON Utilities

```java
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.String2JsonMapper;

// Parse JSON
JsonNode node = new String2JsonMapper().apply("{\"key\": \"value\"}");

// Type-safe extraction
String s = JSONUtil.getString(node);
long l = JSONUtil.getLong(node, 0);
List<String> list = JSONUtil.getStringList(node);
Date d = JSONUtil.getDate(node);
```

### Core Utilities

| Class | Purpose |
|-------|---------|
| `Log` | Logging facade with named loggers (`Log.util`, `Log.test`, etc.) |
| `Env` | Environment and configuration (`getBinConfigBaseFile()`, `getHTHome()`) |
| `Fmt` | String formatting with property resolution (`Fmt.S("Hello %s", name)`) |
| `StringUtil` | String operations (`nullOrEmptyString()`, `join()`, etc.) |
| `ClassUtil` | Reflection utilities |
| `FPHash64` | 64-bit fingerprint hashing |
| `FileUtil` | File I/O utilities, JSON iterators |
| `HTMLParser` | HTML parsing and text extraction |
| `UrlNormalizer` | URL normalization |

### Type System Interfaces

hitorro-core defines the base interfaces that the type system implements:

```java
// Type interface (implemented by Type in hitorro-jsontypesystem)
public interface TypeBaseIntf {
    FieldBaseIntf getField(String field);
    String getName();
    PAContext getPaContext();
}

// Field data types
public enum TypeFieldDataType {
    Long, Int, Short, Byte, Double, Float, String, Date, Boolean, HTSerializable
}
```

## Dependencies

hitorro-core depends only on third-party libraries:

| Dependency | Version | Purpose |
|------------|---------|---------|
| Jackson | 2.18.2 | JSON processing |
| Hadoop Client | 3.4.1 | HDFS file system |
| AWS SDK | 2.29.29 | S3 file system |
| HTTP Components | 4.5.14 / 5.4.1 | Network I/O |
| Commons Codec | 1.16.1 | Encoding utilities |
| Commons Net | 3.11.1 | FTP support |
| NekoHTML | 1.9.22 | HTML parsing |
| Log4j | 1.2.17 | Logging |
| POI | 5.3.0 | Excel/Office files |
| AspectJ | 1.9.22.1 | AOP support |
| JUnit 4 | 4.13.2 | Test framework base |

## Build

```bash
# Build hitorro-core only
mvn clean install -pl hitorro-core

# Run tests
mvn test -pl hitorro-core

# Build with the full project
mvn clean install -DskipTests
```

## Package Structure

```
com.hitorro.util.core/          Core utilities, logging, caching, iteration, error handling
com.hitorro.util.json/          JSON processing, Propaccess path navigation, property mapping
com.hitorro.util.io/            File I/O, CSV processing, resource management
com.hitorro.util.basefile/      File system abstraction (local, HDFS, S3)
com.hitorro.util.typesystem/    Type system base interfaces and annotations
com.hitorro.util.html/          HTML parsing and processing
com.hitorro.util.urlparser/     URL normalization and parsing
com.hitorro.util.testframework/ Test framework (TestPlus interface)
com.hitorro.util.log/           Logger implementation
```
