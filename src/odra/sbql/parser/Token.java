//
//  Token.java
//  Odra
//
//  Created by Michal Lentner on 05-05-01.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.sbql.parser;

import java_cup.runtime.Symbol;

public class Token extends Symbol {
	public int line, column;
	
	public Token(int l, int c, int s) {
		super(s, l, c);

		line = l;
		column = c;
	}
		
	public Token(int l, int c, int s, Object val) {
		super(s, l, c, val);

		line = l;
		column = c;
	} 
}
