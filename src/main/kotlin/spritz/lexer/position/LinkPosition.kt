package spritz.lexer.position

/**
 * @author surge
 * @since 03/03/2023
 */
class LinkPosition(name: String, contents: String) : Position(name, contents, 0, 0, 0) {

    constructor() : this("", "")

}