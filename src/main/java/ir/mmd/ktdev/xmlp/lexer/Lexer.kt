package ir.mmd.ktdev.xmlp.lexer

import ir.mmd.ktdev.xmlp.lexer.Token.Type.*
import ir.mmd.ktdev.xmlp.lexer.provider.StreamProvider
import ir.mmd.ktdev.xmlp.text.Position
import ir.mmd.ktdev.xmlp.text.asSingleRange
import ir.mmd.ktdev.xmlp.text.rangeTo

class Lexer(private val streamProvider: StreamProvider) {
	private val tokens = mutableListOf<Token>()
	private var line = 1
	private var column = 0
	private var current = null as Char?
	
	private inline val position get() = Position(line, column)
	private inline val peek get() = streamProvider.peek
	private inline val next: Char?
		get() = streamProvider.next.also {
			current = it
			if (it == '\n') {
				line++
				column = 1
			} else column++
		}
	
	fun lex(): List<Token> {
		loop@ while (next != null) {
			val startPosition = position
			
			when (current!!) {
				in 'a'..'z',
				in 'A'..'Z',
				'_' -> {
					val builder = StringBuilder("$current")
					while (true) {
						val peeked = peek ?: break
						if (
							peeked !in 'a'..'z' &&
							peeked !in 'A'..'Z' &&
							peeked !in '0'..'9' &&
							peeked !in "_-."
						) break
						builder.append(next)
					}
					tokens += Token(builder.toString(), IDENTIFIER, startPosition..position)
				}
				
				'"', '\'' -> {
					val end = current
					val builder = StringBuilder("$current")
					while (peek != null) {
						builder.append(next)
						
						if (current == end) {
							tokens += Token(builder.toString(), STRING, startPosition..position)
							continue@loop
						}
					}
					tokens += Token(builder.toString(), TEXT, startPosition..position)
				}
				
				in " \t\r\n" -> {
					val builder = StringBuilder("$current")
					while (true) {
						val peeked = peek
						if (peeked == null || peeked !in " \t\r\n") break
						builder.append(next)
					}
					tokens += Token(builder.toString(), WHITE_SPACE, startPosition..position)
				}
				
				'<' -> tokens += when (peek) {
					'/' -> Token("$current$next", END_TAG_START, startPosition..position)
					
					'!' -> {
						val builder = StringBuilder("$current$next")
						val error1 = "Unexpected character '$current'. expected: '-'"
						val error2 = "Unexpected character '$current'. expected: '>'"
						val error3 = "Reached EOF before comment end"
						
						if (next != '-')
							throw error1 at position else builder.append(current)
						if (next != '-')
							throw error1 at position else builder.append(current)
						
						while (true) {
							builder.append(next ?: throw error3 at position)
							
							if (current == '-' && peek == '-') {
								builder.append(next)
								
								if (next != '>')
									throw error2 at position
								else {
									builder.append(current)
									break
								}
							}
						}
						
						Token(builder.toString(), COMMENT, position.asSingleRange)
					}
					
					else -> Token("$current", START_TAG_START, position.asSingleRange)
				}
				
				'/' -> tokens += if (peek == '>')
					Token("$current$next", SINGLE_TAG_END, startPosition..position)
				else
					Token("$current", TEXT, position.asSingleRange)
				
				'>' -> tokens += Token("$current", TAG_END, position.asSingleRange)
				'=' -> tokens += Token("$current", EQUAL_SIGN, position.asSingleRange)
				':' -> tokens += Token("$current", COLON, position.asSingleRange)
				
				else -> {
					val builder = StringBuilder("$current")
					while (true) {
						val peeked = peek
						if (
							peeked == null ||
							peeked == '<'
						) break else builder.append(next)
					}
					tokens += Token(builder.toString(), TEXT, startPosition..position)
				}
			}
		}
		
		return tokens
	}
}
