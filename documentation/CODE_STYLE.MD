# <u>Code Styling</u>
An overview of how Spritz code should be formatted.

## General
Braces should ideally follow the `1TBS` brace style. In short, braces should
<b>never</b> be on their own line, like various `C` style languages.

### Correct Braces
```
task main {
    if (true) {
        std::println("always gonna print...")
    }
}
```

### Incorrect braces
```
task main
{
    if (true) 
    {
        std::println("always gonna print...")
    }
}
```

## Tasks
### Syntax
```
task [return type] [name] [arguments] {
    [body]
}
```

If you are specifying a return type, it should be encased inside `<>`:
```
task<int> a = 2 + 2
```

A task with no parameters should not use `()`.
```
task example {
    std::println("hello, world!")
}
```

If a task only performs and returns an expression, it should be considered an `expression` task.

An anonymous function can be created by omitting the name.

### Correct
```
task<int> expression = 2 + 4
```

### Incorrect
```
task<int> expression {
    return 2 + 4
}
```

Tasks should follow the `snake_case` name formatting, and not `camelCase` or `PascalCase`.

## Classes
A class with no parameters should not use `()` and a class with no 
inner tasks or members should not use `{}`.
```
class Example
```

```
class DataClass(a: int, b: int)
```

```
class WithBody {

    task<int> example {
        return 0
    }
    
}
```

Classes should <b>always</b> use `PascalCase`, rather than `camelCase` or `snake_case`.

## Dictionaries
Dictionaries should <b>always</b> be declared on multiple lines, unless its initialised with only a single value.

### Correct
```
const a = {
    "member1": 2,
    "member2": 3,
    "member3": 4
}

const b = { "member1": 2 } 
```

### Incorrect
```
const a = { "member1": 2, "member2": 3, "member3": 4 }

const b = {
    "member1": 2
}
```

## Try-Catch statements
A Try-Catch statement where nothing happens on an exception should omit the `catch` statement, as per the following:

### Correct
```
try {
    const a = 2
    const b = "2"
    
    std::println(a + b)
}

try -> std::println(2 + "2")
```

### Incorrect
```
try {
    const a = 2
    const b = "2"
    
    std::println(a + b)
} catch exception {}

try -> std::println(2 + "2")
catch exception {}
```