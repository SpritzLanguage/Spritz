<div style="alignment: center">

# Spritz
<hr/>
An interpreted programming language written in Kotlin, intended for use as a scripting language in JVM applications
</div>

### Sample Code

```
task<int> main(arguments: list<string>) {
    std::println("Hello, world!")
    
    for (i: int = 0 -> 10, step: int = 2) {
        std::println(i)
    }
    
    for (argument in arguments) {
        std::println(argument)
    }
    
    return 0
}
```
