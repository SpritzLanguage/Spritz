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
A task with no parameters should not use `()`.
```
task example {
    std::println("hello, world!")
}
```

If a task only performs and returns an expression, it should be considered an `expression` task.

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

## Containers
A container with no parameters should not use `()` and a container with no 
inner tasks or members should not use `{}`.
```
container Example
```

```
container DataContainer(a: int, b: int)
```

```
container WithBody {

    task<int> example {
        return 0
    }
    
}
```

Containers should <b>always</b> use `PascalCase`, rather than `camelCase` or `snake_case`.

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
const a = { "member1": 2, "member2": 3, "member3": 4}

const b = {
    "member1": 2
}
```