package codeGeneration;

import java.io.*;
import symboltable.*;

/****************************************************
*  Generator koda za mikrojavu
****************************************************/
public class Code {
  public static byte[] buf = new byte[8192]; // prostor za smestanje prevedenog programskog koda
  private static final int bufSize = 8192;  
  public static int pc = 0,      // tekuca adresa za smestanje prevedene instrukcije 
                    mainPc=-1,   // adresa main rutine
                    dataSize=0;  // velicina oblasti globalnih podataka
  public static boolean greska=false; // flag da li je prijavljena neka  greske  
  public static final int		// instruction codes
	load        =  1,
	load_n      =  2,
	load_1      =  3,
	load_2      =  4,
	load_3      =  5,
	store       =  6,
	store_n     =  7,
	store_1     =  8,
	store_2     =  9,
	store_3     = 10,
	getstatic   = 11,
	putstatic   = 12,
	getfield    = 13,
	putfield    = 14,
	const_n     = 15,
	const_1     = 16,
	const_2     = 17,
	const_3     = 18,
	const_4     = 19,
	const_5     = 20,
	const_m1    = 21,
	const_      = 22,
	add         = 23,
	sub         = 24,
	mul         = 25,
	div         = 26,
	rem         = 27,
	neg         = 28,
	shl         = 29,
	shr         = 30,
	inc         = 31,
	new_        = 32,
	newarray    = 33,
	aload       = 34,
	astore      = 35,
	baload      = 36,
	bastore     = 37,
	arraylength = 38,
	pop         = 39,
	dup         = 40,
	dup2        = 41,
	jmp         = 42,
	jcc         = 43,		//  ... 48
	call        = 49,
	return_     = 50,
	enter       = 51,
	exit        = 52,
	read        = 53,
	print       = 54,
	bread       = 55,
	bprint      = 56,
	trap        = 57,
	invokevirtual = 58;
  public static int eq=0, ne=1, lt=2, le=3, gt=4, ge=5;
  public static int inverse[]={ne, eq, ge, gt, le, lt}; //  maps op to the inverse of top
  
	public static void error (String err) {
        System.err.println(err);
        Code.greska=true;
	}
  
  public static void put (int x)  {
		if (pc >= bufSize) {
			if (pc == bufSize) error("Greska: Prevelik objektni kod");
			pc++;
		} else
			buf[pc++] = (byte)x;
	}
  public static void put2 (int x) { put(x>>8); put(x);  }
  public static void put4 (int x) { put2(x>>16); put2(x); }

  public static void put2 (int pos, int x)
  {
    int stari=pc;
    pc=pos;
    put2(x);
    pc=stari;
  }
  public static int get (int pos) { return buf[pos];  }
  public static int get2 (int pos)  { return (get(pos)<<8)+(get(pos+1)&0xFF);  }

  public static void load (Obj o) {
    switch (o.getKind()) {
    	
      case Obj.Con:
        if (o.getType() == Tab.nullType) 
            put(const_n + 0);
        else 
            loadConst(o.getAdr()); 
        break;
        
      case Obj.Var:
        if (o.getLevel()==0) { // global variable 
        	  put(getstatic); put2(o.getAdr()); 
        	  break; 
        }
        // local variable
        if (0 <= o.getAdr() && o.getAdr() <= 3) 
            put(load_n + o.getAdr());
        else { 
        	 put(load); put(o.getAdr()); 
        } 
        break;
        
      case Obj.Fld:
        put(getfield); put2(o.getAdr()); 
        break;
        
      case Obj.Elem:
        if (o.getType().getKind() == Struct.Char) put(baload);
        else put(aload); 
        break;
      
      default:  
         error("Greska: nelegalan operand u Code.load");
    }
  }
  
  public static void loadConst (int n) {
    if (0<=n&&n<=5) put (const_n+n);
    else if (n==-1) put (const_m1);
    else  { put (const_); put4 (n); }
  }
  
  
  /**
   * @param x
   */
  public static void store(Obj o) {
  	switch (o.getKind()) {

      case Obj.Var:
        if (o.getLevel()==0) { // global variable 
            put(putstatic); put2(o.getAdr()); 
            break;
        }
        // local variable 
        if (0 <= o.getAdr() && o.getAdr() <= 3) 
            put(store_n + o.getAdr());
        else { 
        	  put(store); put(o.getAdr()); 
        } 
        break;

      case Obj.Fld:
        put(putfield); put2(o.getAdr()); 
        break;
        
      case Obj.Elem:
        if (o.getType().getKind()== Struct.Char) put(bastore);
        else put(astore); 
        break;
      
      default:
        error("Greska: Na levoj strani dodele mora biti promenljiva!");
    }
  }
  

  
  
  // generates unconditional jump instruction to lab
  //***public static void jump (Label lab) { put (jmp); lab.put ();  }

  // generates conditional jump instruction for true jump
  // x represents the condition
  //***public static void tJump (Obj o) { put (jcc+o.adr); o.tLabel.put (); }

  // generates conditional jump instruction for false jump
  // x represents the condition
  //***public static void fJump (Obj o)  { put (jcc+inverse[o.adr]); o.fLabel.put ();  }

  // Writes the code buffer to the output stream
	public static void write(OutputStream s) {
		int codeSize;
		try {
			codeSize = pc;
			put('M'); put('J');
			put4(codeSize);
			put4(dataSize);
			put4(mainPc);
			s.write(buf, codeSize, pc - codeSize);	// header
			s.write(buf, 0, codeSize);				// code
			s.close();
		} catch(IOException e) {
			 error("Greska pri upisu u izlazni fajl");
		}
	}
}