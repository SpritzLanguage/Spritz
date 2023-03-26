# Standard Library
Documentation for all inbuilt tools inside Spritz.

## Globals
### `true`
The boolean value of true.

### `false`
The boolean value of false.

### `null`
A filler for a non-existent value.

## `std`

### `print(input: any)`
Prints a value (without a new line) to the console

### `printf(input: string, format: any)`
Prints the given `input` to the console (without a new line), but replaces the first instance of `%` in the given `input` with the corresponding value `format`.
If `format` is a `list`, instances of `%` will be replaced with the value at the corresponding instance in `format`.

### `println(input: any)`
Prints a value (with a new line) to the console

### `printlnf(input: string, format: any)`
Prints the given `input` to the console (with a new line), but replaces the first instance of `%` in the given `input` with the corresponding value `format`.
If `format` is a `list`, instances of `%` will be replaced with the value at the corresponding instance in `format`.

### `<string> readln()`
Waits for the console to have input, then returns it.

### `<list> int_range(start: int, end: int, step: int)`
Generates a list of integers based on the start, end, and step.
Supports backwards ranges, where `start` is bigger than `end`.

### `<list> float_range(start: float, end: float, step: float)`
Generates a list of floats based on the start, end, and step.
Supports backwards ranges, where `start` is bigger than `end`.

## Primitives
Tasks included with each primitive type.

Primitives:
<ul>
<li> <code>int</code> </li>
<li> <code>float</code> </li>
<li> <code>number</code> </li>
<li> <code>string</code> </li>
<li> <code>boolean</code> </li>
<li> <code>list</code> </li>
<li> <code>dictionary</code> </li>
<li> <code>class</code> </li>
<li> <code>instance</code> </li>
<li> <code>task</code> </li>
</ul>

### String
### `<int> length()`
Gets the length of the string

### `<boolean> is_empty()`
Checks if the length of the string is 0

### `<string> char_at(index: int)`
Gets the character at the given index

### `<string> replace(old: string, new: string)`
Gets a string where all instances of `old` have been replaced with `new`

### `<string> upper()`
Gets a string with the same content as the current string, but uppercase.

### `<string> lower()`
Gets a string with the same content as the current string, but lowercase.

### `<string> to_char_list()`
Gets a list of `string` instances, with each element being a character in the current string.

### `<list> split(delimiter)`
Splits the string by the delimiter and returns it as a list

### `<list> after(index: int)`
Gets a list of characters after the given index (inclusive).

### `<int> int()`
Converts the string to an integer value.

### `<float> float()`
Converts the string to a float value.

### `<byte> byte()`
Converts the string to a byte value.

### `<boolean> boolean()`
Converts the string to a boolean value.

### List
### `add(value: any)`
Adds the given `value` to the list.

### `<any> get(index: int)`
Gets the element at the given `index`.

### `<boolean> remove(value: any)`
Removes the given `value` from the list, and returns whether removing was successful.

### `<removeAt> removeAt(index: int)`
Removes the element at the given `index`, and returns that element.

### `<int> length()`
Gets the length of the list.

### `<boolean> is_empty()`
Checks if the list is empty.

### `<list> after(index: int)`
Gets a list of elements after the given index (inclusive).

### `<string> join(separator: string, format: string)`
Returns a string that represents the concatenated list values, with the element being formatted by `format`, with `%` representing the item.

### Dictionary
### `set(key: string, value: any)`
Sets the element mapped to the given `key` to `value`.

### `<any> get(key: string)`
Gets the element mapped to the given `key`, or `null`, if it wasn't found.

### `<any> remove(key: string)`
Removes the value mapped to the given `key`, and returns the last value that was mapped to it.

### `<int> length()`
Gets the length of the dictionary.