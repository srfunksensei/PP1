package pp_dz;

import java_cup.runtime.*;
import java.io.IOException;


%%

%class Lexer
%unicode
%cup
%line
%column
%char

%{
	//include info about token position
	private Symbol new_symbol(int type){
		return new Symbol(type, yyline+1, yycolumn);
	}

	//include info about token position
	private Symbol new_symbol(int type, Object value){
		return new Symbol(type, yyline+1, yycolumn, value);
	}

	private void error() throws IOException{
		throw new IOException("illegal text at line = "+yyline+", column = "+yycolumn+", text ='"+yytext()+"'");
	}

	public int line() {return yyline;}
%}


// at the end of file return EOF
%eofval{
	return new_symbol(sym.EOF);
%eofval}

%xstate ONE_LINE_COMMENT


%%
<YYINITIAL> [\n\r\ \t\b\f\012] { /*ignores empty spots*/ }

/*keywords*/
<YYINITIAL> "class"  {return new_symbol(sym.CLASS);}
<YYINITIAL> "final"  {return new_symbol(sym.FINAL);}
<YYINITIAL> "void"   {return new_symbol(sym.VOID);}
<YYINITIAL> "if"     {return new_symbol(sym.IF);}
<YYINITIAL> "else"   {return new_symbol(sym.ELSE);}
<YYINITIAL> "while"  {return new_symbol(sym.WHILE);}
<YYINITIAL> "break"  {return new_symbol(sym.BREAK);}
<YYINITIAL> "return" {return new_symbol(sym.RETURN);}
<YYINITIAL> "read"   {return new_symbol(sym.READ);}
<YYINITIAL> "print"  {return new_symbol(sym.PRINT);}
<YYINITIAL> "new"    {return new_symbol(sym.NEW);}


/*operators*/
<YYINITIAL> "+"	 { return new_symbol(sym.PLUS); }
<YYINITIAL> "-"	 { return new_symbol(sym.MINUS); }
<YYINITIAL> "*"	 { return new_symbol(sym.TIMES); }
<YYINITIAL> "/"	 { return new_symbol(sym.DIV); }
<YYINITIAL> "%"	 { return new_symbol(sym.MOD); }
<YYINITIAL> "==" 	 { return new_symbol(sym.EQUAL); }
<YYINITIAL> "!=" 	 { return new_symbol(sym.NOT_EQUAL); }
<YYINITIAL> ">"	 { return new_symbol(sym.GREATER); }
<YYINITIAL> ">=" 	 { return new_symbol(sym.GREATER_EQUAL); }
<YYINITIAL> "<"	 { return new_symbol(sym.LESS); }
<YYINITIAL> "<=" 	 { return new_symbol(sym.LESS_EQUAL); }
<YYINITIAL> "&&" 	 { return new_symbol(sym.AND); }
<YYINITIAL> "||" 	 { return new_symbol(sym.OR); }
<YYINITIAL> "="	 { return new_symbol(sym.ASSIGNMENT); }
<YYINITIAL> "++" 	 { return new_symbol(sym.INC); }
<YYINITIAL> "--" 	 { return new_symbol(sym.DEC); }
<YYINITIAL> ";"	 { return new_symbol(sym.SEMI); }
<YYINITIAL> ","	 { return new_symbol(sym.COMMA); }
<YYINITIAL> "."	 { return new_symbol(sym.DOT); }
<YYINITIAL> "("	 { return new_symbol(sym.LPAREN); }
<YYINITIAL> ")"	 { return new_symbol(sym.RPAREN); }
<YYINITIAL> "["	 { return new_symbol(sym.LSQUARE); }
<YYINITIAL> "]"	 { return new_symbol(sym.RSQUARE); }
<YYINITIAL> "{"	 { return new_symbol(sym.LBRACE); }
<YYINITIAL> "}"	 { return new_symbol(sym.RBRACE); }


//comments
<YYINITIAL> "//" {yybegin(ONE_LINE_COMMENT);}
<ONE_LINE_COMMENT> . {yybegin(ONE_LINE_COMMENT);}
<ONE_LINE_COMMENT> \r\n {yybegin(YYINITIAL);}


//number, identifier
<YYINITIAL> [0-9]+ { return new_symbol(sym.NUMBER, new Integer(yytext())); }
<YYINITIAL> ([a-z]|[A-Z])[a-z|A-Z|0-9|_]* {return new_symbol(sym.IDENT, yytext());}
<YYINITIAL> "'"[\040-\176]"'" {return new_symbol (sym.CHARCONST, new Character (yytext().charAt(1)));}


//error
<YYINITIAL> . {return new_symbol(sym.INVALID, yytext());}
