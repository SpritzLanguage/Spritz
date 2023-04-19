# Spritz

An interpreted programming language written in Kotlin, designed for interoperability with the JVM.

## Get Started
Add Spritz to your project, with your build system of choice:
<details>
    <summary>Gradle (Groovy)</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.SpritzLanguage:Spritz:1.0.0-alpha'
}
```

</details>

<details>
    <summary>Gradle (Kotlin)</summary>

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.SpritzLanguage:Spritz:1.0.0-alpha")
}
```

</details>

<details>
    <summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.SpritzLanguage</groupId>
    <artifactId>Spritz</artifactId>
    <version>1.0.0-alpha</version>
</dependency>
```

</details>

Create a `SpritzEnvironment` instance to handle scripting:

```kotlin
val env = SpritzEnvironment(Config())
    .setWarningHandler(::println)
    .setErrorHandler(::println)
```

You can then evaluate a file with the `SpritzEnvironment#evaluate` method:

```kotlin
env.evaluate(File("example.sz"))
```

Please read the documentation (coming soon) for more information.