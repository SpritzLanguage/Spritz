# Spritz - Overview
The very basics to writing Spritz. Make sure to visit `CODE_STYLE.MD` for instructions
on the ideal way to format your code, and visit `STANDARD_LIBRARY` for documentation
on the `std` library and other default inclusions.

# <u>The basics</u>
Features that are needed to write any amount of Spritz.

## Variables
Declare mutable variables with the `mut` keyword:
`mut [name] = [value]`

E.G.
`mut a = 5`

Declare constant variables with the `const` keyword:
`const [name] = [value]`

E.G.
`const a = 5`

## Tasks
Define a task with the `task` keyword. You can follow this with the return type in `<>`,
then the name, arguments, and body.

```
task<number> a_plus_b(a, b) {
    return a + b
}
```

You can use `=` for single expression tasks as well:
```
task<number> a_plus_b(a, b) = a + b
```

Arguments are optional. If the task does not have any, it should not have `()`:
```
task main = std::println("hello, world!")
```

You can specify the types of arguments with `[name]: [type]`:
```
task<int> a_plus_b(a: int, b: int) = a + b
```

## Classes
Technically, these aren't strictly classes and do not follow true OOP practices,
but they are still similar.

Define a task with the `class` keyword. You follow this with the name, arguments,
and then body.

Like tasks, the arguments are optional, however, the body is also optional.

```
class ExampleNoBody(a: int, b: Int)

class ExampleNoArgs {
    task out {
        std::println("hello!")
    }
}
```

## Accessing contained members
You can use the `::` operator to access a member of
a class or value.

```
[parent]::[child]
```

E.G.

```
std::println("println is a child task")
```

You can also add `?` after the child identifier to specify it
to return `null` if the parent value is null.

```
const a = null
std::println(a::length?()) // will output null
```

## Loops
Declare a `for` loop like this:

```
for ([item identifier] : [list]) {
    // body
}
```

Or, use `->` for a single expression loop:
```
for ([item identifier] : [list]) -> // body
```

E.G.
```
for (i : std::int_range(0, 5, 1)) {
    std::println(i)
}
```

Declare a `while` loop like this:

```
while ([condition]) {
    // body
}
```

Or, like the `for` loop, use `->` for a single expression loop:
```
while ([condition]) -> // body
```

E.G.
```
while (true) {
    const input = std::readln()

    if (input == "quit") {
        break
    }

    std::println(input)
}
```

`break` and `continue` also exist, and can be used to 
exit the loop, or continue onto the next element, respectively.

## List
Declare a list with `[]`

```
const li = [1, 2, 3]
```

## Dictionary
Declare a dictionary with `{}`

```
const dict = {
    [key]: [value]
}
```

`key` must always be a string.

E.G.

```
const ages = {
    "John": 34,
    "Amy": 21,
    "Owen": 12 
}
```

# <u>Advanced Features</u>
Features that are more advanced, for people who have already learnt the basics.

## Natives
You can directly reference JVM classes with the `native` keyword.

Declare references like this:
```
native "[class path]" as [identifier]
```

You can then access that reference when using the identifier.

## Try-Catch
You can handle exceptions by using the `try` keyword.

```
try {
    // body
}

try -> // body
```

You can then add a `catch` block to handle the exception:

```
try {
    // body
} catch [exception identifier] {
    // catch body
}

try -> // body
catch [exception identifier] -> // catch body
```

E.G.
```
try {
    2 + " " // will always throw an exception
} catch exception {
    std::println(exception)
}
```


## Lambdas
You can create an anonymous task using the `lambda` keyword.

If your lambda has no arguments it should look like this:

```
lambda -> {
    // body
}
```

Otherwise, it should look like this:
```
lambda { arg_a, arg_b ->
    // body
}
```

It will then equate to a normal task, but without a name.

E.G.
```
const lambda_variable = lambda { a, b ->
    return a + b
}

std::println(lambda_variable(3, 4))
```

It is worth noting that lambdas can reference values that have previously been defined,
so you need to name your arguments something that hasn't already been defined.

```
const a = 2

// this will NOT work because `a` is already defined
const l = lambda { a ->
    std::println(a)
}

l(45)
```

## Enums
Declare an enum with the `enum` keyword, followed by the name, arguments, and body.

Like classes and tasks, arguments are optional, but the body is not.

```
enum [identifier] ([arguments]) {
    // body
}
```

The body should contain the members, like this:
```
enum Example {
    MEMBER_ONE,
    MEMBER_TWO
}
```

If you have provided arguments, each member should be "called".

```
enum Example(a, b) {
    MEMBER_ONE(1, 2),
    MEMBER_TWO(3, 4)
}
```

If you want to give each member its own body, add `{}` inside the body:

```
enum Example(a, b) {
    MEMBER_ONE(1, 2),
    MEMBER_TWO(3, 4)
    
    {
        task<int> add = a + b
    }
}
```