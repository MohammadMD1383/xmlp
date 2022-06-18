import ir.mmd.ktdev.xmlp.lexer.Lexer
import ir.mmd.ktdev.xmlp.lexer.LexerException
import ir.mmd.ktdev.xmlp.lexer.Token.Type.*
import ir.mmd.ktdev.xmlp.lexer.provider.streamProvider
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContentEquals

fun lexer(string: String) = Lexer(string.streamProvider)
fun String.lex() = Lexer(streamProvider).lex()

class LexerTests {
	@Test
	fun singleTag() {
		val lexer = lexer("<some />")
		assertContentEquals(
			listOf(
				START_TAG_START,
				IDENTIFIER,
				WHITE_SPACE,
				SINGLE_TAG_END
			),
			lexer.lex().map { it.type }
		)
	}
	
	@Test
	fun commentWithSingleTag() {
		val lexer = lexer("<!-- some comment --><help/>")
		assertContentEquals(
			listOf(
				COMMENT,
				START_TAG_START,
				IDENTIFIER,
				SINGLE_TAG_END
			),
			lexer.lex().map { it.type }
		)
	}
	
	@Test
	fun incompleteCommentStart() {
		assertThrows<LexerException> {
			"<!".lex()
		}
	}
	
	@Test
	fun twoDashesInsideComment() {
		assertThrows<LexerException> {
			"<!-- some comment -- -->".lex()
		}
	}
	
	@Test
	fun tagWithChildren() {
		val lexer = lexer(
			"""
			<parent>
				<child>text></child>
			</some>
		""".trimIndent()
		)
		assertContentEquals(
			listOf(
				//@formatter:off
				START_TAG_START to "<",
				IDENTIFIER      to "parent",
				TAG_END         to ">",
				WHITE_SPACE     to "\n\t",
				START_TAG_START to "<",
				IDENTIFIER      to "child",
				TAG_END         to ">",
				IDENTIFIER      to "text",
				TAG_END         to ">",
				END_TAG_START   to "</",
				IDENTIFIER      to "child",
				TAG_END         to ">",
				WHITE_SPACE     to "\n",
				END_TAG_START   to "</",
				IDENTIFIER      to "some",
				TAG_END         to ">"
				//@formatter:on
			),
			lexer.lex().map { it.type to it.value }
		)
	}
	
	@Test
	fun symbols() {
		val result = "<><!---->/>:=\"some\"".lex().map { it.type to it.value }
		assertContentEquals(
			listOf(
				//@formatter:off
				START_TAG_START to result[0].second,
				TAG_END         to result[1].second,
				COMMENT         to result[2].second,
				SINGLE_TAG_END  to result[3].second,
				COLON           to result[4].second,
				EQUAL_SIGN      to result[5].second,
				STRING          to "\"some\""
				//@formatter:on
			),
			result
		)
	}
	
	@Test
	fun naming() {
		assertContentEquals(
			listOf(
				//@formatter:off
				START_TAG_START to "<",
				IDENTIFIER      to "_root2-d.das.-",
				SINGLE_TAG_END  to "/>"
				//@formatter:on
			),
			"<_root2-d.das.-/>".lex().map { it.type to it.value }
		)
		
		assertContentEquals(
			listOf(
				//@formatter:off
				START_TAG_START to "<",
				IDENTIFIER      to "_root2-d.das.-",
				TAG_END         to ">",
				END_TAG_START   to "</",
				IDENTIFIER      to "end",
				TAG_END         to ">"
				//@formatter:on
			),
			"<_root2-d.das.-></end>".lex().map { it.type to it.value }
		)
	}
	
	@Test
	fun attrQuotes() {
		assertContentEquals(
			listOf(
				//@formatter:off
				IDENTIFIER to "attr",
				EQUAL_SIGN to "=",
				STRING     to "'value'"
				//@formatter:on
			),
			"attr='value'".lex().map { it.type to it.value }
		)
		
		assertContentEquals(
			listOf(
				//@formatter:off
				IDENTIFIER to "attr",
				EQUAL_SIGN to "=",
				STRING     to "\"value\""
				//@formatter:on
			),
			"attr=\"value\"".lex().map { it.type to it.value }
		)
	}
}
