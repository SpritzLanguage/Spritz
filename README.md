<div style="text-align: center">

# Spritz
<hr/>
An interpreted programming language written in Kotlin, intended for use as a scripting language in JVM applications

Spritz is still in a development phase and <b> should not </b> be used for production use.
</div>
<br/>

### Sample Code

```
task<int> main(arguments: list) {
    std::println("Hello, world!")
    
    for (i : std::int_range(0, 10, 2)) {
        std::println(i)
    }
    
    for (argument : arguments) {
        std::println(argument)
    }
    
    return 0
}
```

### Planned Features
#### Inbuilt Unit Testing
An ability to use unit tests within the language, without any external dependencies.
This could be a library, <br> but I would like to have it syntactically possible.
Possibly like the following:

```
task<int> func(a: int, b: int) -> (func(5, 5) == 10, func(53, 23) == 432) {
    return a + b
}
```

Then, you would add `-RunTests` to the command line arguments, and it would run these tests when
the task is first defined. We could also have a `-ExitOnTestFailure` command that exits the program
if any of the tests failed, otherwise, just print out the test results?

#### Null Safety
Obviously, optional. I think a syntactic element should be added after the type, which would declare
it as non-null. Some options:

```
const exclamation: int! = 5
const asterisk: int* = 5
const dollar: $int = 5 
```

Then, we would need something to declare a reference as non-null (similar to `!!` in Kotlin):

```
std::println(exclamation!!)
std::println(asterisk*)
std::println(dollar$)
std::println(question?)
```

# TODO
- [x] Lexing
- [x] Parsing
- [x] Interpreting
- [x] Variables (`mut`, `const`)
    - [ ] Actually enforce `const` variables. (No error is thrown.) 
- [x] Basic Types
- [ ] Dictionaries / Maps
- [x] Tasks
- [ ] Enums
- [ ] Add more to this TODO list
