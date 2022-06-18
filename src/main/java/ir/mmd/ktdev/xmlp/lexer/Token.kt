package ir.mmd.ktdev.xmlp.lexer

import ir.mmd.ktdev.xmlp.text.TextRange

data class Token(
	val value: String,
	val type: Type,
	val range: TextRange
) {
	enum class Type {
		IDENTIFIER,
		WHITE_SPACE,
		COLON,
		EQUAL_SIGN,
		STRING,
		TEXT,
		COMMENT,
		START_TAG_START,
		END_TAG_START,
		TAG_END,
		SINGLE_TAG_END;
		
		override fun toString() = "[$name]"
	}
}
