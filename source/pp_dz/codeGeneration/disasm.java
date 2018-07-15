/* MicroJava Instruction Decoder  (HM 99-05-07)
   =============================
*/
package codeGeneration;
import java.io.*;

public class disasm {

	private static byte[] code = new byte[8192+14];;		// code buffer
	private static int cur=0;			// address of next byte to decode
	private static int adr;			// address of currently decoded instruction
	private static int off;    // size of the header of mj obj file

	private static int get() {
		return ((int)code[cur++])<<24>>>24;
	}
	
	private static int get2() {
		return (get()*256 + get())<<16>>16;
	}
	
	private static int get4() {
		return (get2()<<16) + (get2()<<16>>>16);
	}
	
	private static String jumpDist() {
		int dist = get2();
		int abs = adr + dist;
		return String.valueOf(dist)+" (="+String.valueOf(abs)+")";
	}
	
	private static void P(String s) {
		System.out.println(adr+": "+s);
		adr = cur-off;
	}

	public static void decode(byte[] c, int len) {
		//int op;
		code = c;
		cur = off;
		adr = cur-off;
		while (cur < len) {
			switch(get()) {
				case 1: {P("load "+get()); break;}
				case 2: {P("load_0"); break;}
				case 3: {P("load_1"); break;}
				case 4: {P("load_2"); break;}
				case 5: {P("load_3"); break;}
				case 6: {P("store "+get()); break;}
				case 7: {P("store_0"); break;}
				case 8: {P("store_1"); break;}
				case 9: {P("store_2"); break;}
				case 10: {P("store_3"); break;}
				case 11: {P("getstatic "+get2()); break;}
				case 12: {P("putstatic "+get2()); break;}
				case 13: {P("getfield "+get2()); break;}
				case 14: {P("putfield "+get2()); break;}
				case 15: {P("const_0"); break;}
				case 16: {P("const_1"); break;}
				case 17: {P("const_2"); break;}
				case 18: {P("const_3"); break;}
				case 19: {P("const_4"); break;}
				case 20: {P("const_5"); break;}
				case 21: {P("const_m1"); break;}
				case 22: {P("const "+get4()); break;}
				case 23: {P("add"); break;}
				case 24: {P("sub"); break;}
				case 25: {P("mul"); break;}
				case 26: {P("div"); break;}
				case 27: {P("rem"); break;}
				case 28: {P("neg"); break;}
				case 29: {P("shl"); break;}
				case 30: {P("shr"); break;}
				case 31: {P("inc "+get()+","+get()); break;}
				case 32: {P("new "+get2()); break;}
				case 33: {P("newarray "+get()); break;}
				case 34: {P("aload"); break;}
				case 35: {P("astore"); break;}
				case 36: {P("baload"); break;}
				case 37: {P("bastore"); break;}
				case 38: {P("arraylength"); break;}
				case 39: {P("pop"); break;}
				case 40: {P("dup"); break;}
				case 41: {P("dup2"); break;}
				case 42: {P("jmp "+jumpDist()); break;}
				case 43: {P("jeq "+jumpDist()); break;}
				case 44: {P("jne "+jumpDist()); break;}
				case 45: {P("jlt "+jumpDist()); break;}
				case 46: {P("jle "+jumpDist()); break;}
				case 47: {P("jgt "+jumpDist()); break;}
				case 48: {P("jge "+jumpDist()); break;}
				case 49: {P("call "+jumpDist()); break;}
				case 50: {P("return"); break;}
				case 51: {P("enter "+get()+" "+get()); break;}
				case 52: {P("exit"); break;}
				case 53: {P("read"); break;}
				case 54: {P("print"); break;}
				case 55: {P("bread"); break;}
				case 56: {P("bprint"); break;}
				case 57: {P("trap "+get()); break;}
				case 58: { String name=new String(); 
                int a=get4();
                while (a!=-1) { name+=(char)a; a=get4(); }
                		P("invokevirtual "+name);
	     break;
	   } 
				default: {P("-- error--"); break;}
			}
		}
	}

	public static void main(String[] arg) {
		if (arg.length == 0)
			System.out.println("-- no filename specified");
		else {
			try {
				InputStream s = new FileInputStream(arg[0]); 
				int len = s.read(code);
				if (get()!='M' || get()!='J')
				    System.out.println("-- invalid microjava object file");
				else {
				    System.out.println("codeSize="+get4());
				    System.out.println("dataSize="+get4());
				    System.out.println("mainPC="+get4());
				    off=cur;
				    decode(code, len);
			  }
			} catch (IOException e) {
				System.out.println("-- could not open file " + arg[0]);
			}
		}
	}

}