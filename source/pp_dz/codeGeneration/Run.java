// MicroJava Virtual Machine
// -------------------------
// Syntax: java MJ.Run fileName [-debug]
// ===========================================================================
// by Hanspeter Moessenboeck, 2002-10-28
// edited by Albrecht Woess, 2002-10-30
// edited by Marko Bojovic, 2005-07-24

package codeGeneration;

import java.io.*;

public class Run {
	static boolean debug;    // debug output on or off

	static byte code[];      // code array
	static int data[];       // global data
	static int heap[];       // dynamic heap
	static int stack[];      // expression stack
	static int local[];      // method stack
	static int dataSize;     // size of global data area
	static int startPC;		   // address of main() method
	static int pc;           // program counter
	static int fp, sp;       // frame pointer, stack pointer on method stack
	static int esp;          // expression stack pointer
	static int free;         // next free heap address

	static final int
		heapSize = 100000,  // size of the heap in words 
		mStackSize = 400,   // size of the method stack in words
		eStackSize = 30;    // size of the expression stack in words

	static final int		// instruction codes
		load        =  1,
		load_0      =  2,
		load_1      =  3,
		load_2      =  4,
		load_3      =  5,
		store       =  6,
		store_0     =  7,
		store_1     =  8,
		store_2     =  9,
		store_3     = 10,
		getstatic   = 11,
		putstatic   = 12,
		getfield    = 13,
		putfield    = 14,
		const_0     = 15,
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
		jcond       = 43,		//  ... 48
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

	static final int  // compare operators
		eq = 0,
		ne = 1,
		lt = 2,
		le = 3,
		gt = 4,
		ge = 5;

	static String[] opcode = {    
    "???        ", "load       ", "load_0     ", "load_1     ", "load_2     ",
    "load_3     ", "store      ", "store_0    ", "store_1    ", "store_2    ", 
    "store_3    ", "getstatic  ", "putstatic  ", "getfield   ", "putfield   ", 
    "const_0    ", "const_1    ", "const_2    ", "const_3    ", "const_4    ", 
    "const_5    ", "const_m1   ", "const      ", "add        ", "sub        ",
    "mul        ", "div        ", "rem        ", "neg        ", "shl        ", 
    "shr        ", "inc        ", "new        ", "newarray   ", "aload      ", 
    "astore     ", "baload     ", "bastore    ", "arraylength", "pop        ", 
    "dup        ", "dup2       ", "jmp        ", "jeq        ", "jne        ",
    "jlt        ", "jle        ", "jgt        ", "jge        ", "call       ",
    "return     ", "enter      ", "exit       ", "read       ", "print      ",
    "bread      ", "bprint     ", "trap       ", "invokevirtual"
	};

	//----- expression stack

	static void push (int val) throws VMException {
		if (esp == eStackSize) throw new VMException("expression stack overflow");
		stack[esp++] = val;
	}

	static int pop () throws VMException {
		if (esp == 0) throw new VMException("expression stack underflow");
		return stack[--esp];
	}

	//----- method stack

	static void PUSH (int val) throws VMException {
		if (sp == mStackSize) throw new VMException("method stack overflow");
		local[sp++] = val;
	}

	static int POP () throws VMException {
		if (sp == 0) throw new VMException("method stack underflow");
		return local[--sp];
	}

	//----- instruction fetch

	static byte next (boolean print) {
		byte b = code[pc++];
		if (debug && print) System.out.print(b + " ");
		return b;
	}

	static short next2 (boolean print) {
		short s = (short)(((next(false) << 8) + (next(false) & 0xff)) << 16 >> 16);
		if (debug && print) System.out.print(s + " ");
		return s;
	}

	static int next4 () {
		int n = (next2(false) << 16) + (next2(false) & 0xffff); 
		if (debug) System.out.print(n + " ");
		return n;
	}

	//----- VM internals

	static void load (String name) throws IOException, FormatException {
		int codeSize;
		byte[] sig = new byte[2];
		DataInputStream in = new DataInputStream(new FileInputStream(name));
		in.read(sig, 0, 2);
		if (sig[0] != 'M' || sig[1] != 'J') 
			throw new FormatException("wrong marker");
		codeSize = in.readInt();
		if (codeSize <= 0) throw new FormatException("codeSize <= 0");
		dataSize = in.readInt();
		if (dataSize < 0) throw new FormatException("dataSize < 0");
		startPC = in.readInt();
		if (startPC < 0 || startPC >= codeSize) 
			throw new FormatException("startPC not in code area");
		code = new byte[codeSize];
		in.read(code, 0, codeSize);
	}

	/** Allocate heap block of size bytes */
	static int alloc (int size) throws VMException {
		int adr = free;
		free += ((size+3) >> 2);		// skip to next free adr 
		                        		// (>> 2 to convert byte to word)
		if (free > heapSize) throw new VMException("heap overflow");
		return adr;
	}

	/** Retrieve byte n from val. Byte 0 is MSB */
	static byte getByte (int val, int n) {
		return (byte)(val << (8*n) >>> 24);
	}

	/** Replace byte n in val by b */
	static int setByte (int val, int n, byte b) {
		int delta = (3 - n) * 8;
		int mask = ~(255 << delta);		// mask all 1 except on chosen byte
		int by = (((int)b) & 255) << delta;
		return (val & mask) ^ by;
	}
	
	/** Read int from standard input stream */
	static int readInt () throws IOException {
		int val = 0;
		int prev = ' ';
		int b = System.in.read();
		while (b < '0' || b > '9') {
			prev = b; b = System.in.read();
		}
		while (b >= '0' && b <= '9') {
			val = 10 * val + b - '0';
			b = System.in.read();
		}
		if (prev == '-') val = -val;
		return val;
	}

	//----- debug output

	static void printNum (int val, int n) {
		String s = new Integer(val).toString();
		int len = s.length();
		while (len < n) { System.out.print(" "); len++; }
		System.out.print(s);
	}

	static void printInstr () {
		int op = code[pc - 1];
		String instr = (op > 0 && op <= invokevirtual) ? opcode[op] : opcode[0];
		printNum(pc - 1, 5);
		System.out.print(": " + instr + " ");
	}

	static void printStack () {
		for (int i = 0; i < esp; i++) System.out.print(stack[i] + " ");
		System.out.println();
	}

	//----- actual interpretation

	static void interpret () {
		int op, adr, val, val2, off, idx, len, i;
		pc = startPC;
		
		if (debug) {		// header for debug output
			System.out.println();
			System.out.println("  pos: instruction operands");
			System.out.println("     | expressionstack");
			System.out.println("-----------------------------");
		}
		
		try {
			for (;;) { // terminated by return instruction
				op = next(false);
				if (debug) printInstr();
				switch((int)op) {

					// load/store local variables
					case load:
						push(local[fp + next(true)]);
						break;
					case load_0: case load_1: case load_2: case load_3:
						op -= load_0; // mapping on range 0..3
						push(local[fp + op]);
						break;
					case store:
						local[fp + next(true)] = pop();
						break;
					case store_0: case store_1: case store_2: case store_3:
						op -= store_0; // mapping on range 0..3
						local[fp + op] = pop();
						break;

					// load/store global variables
					case getstatic:
						push(data[next2(true)]);
						break;
					case putstatic:
						data[next2(true)] = pop();
						break;

					// load/store object fields
					case getfield:
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						push(heap[adr + next2(true)]);
						break;
					case putfield:
						val = pop();
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						heap[adr + next2(true)] = val;
						break;

					// load constants
					case const_0: case const_1: case const_2: 
					case const_3: case const_4: case const_5:
						push(op - const_0); // map opcode to 0..5
						break;
					case const_m1:
						push(-1);
						break;
					case const_:
						push(next4());
						break;

					// arithmetic operations
					case add:
						push(pop() + pop());
						break;
					case sub:
						push(-pop() + pop());
						break;
					case mul:
						push(pop() * pop());
						break;
					case div:
						val = pop();
						if (val == 0) throw new VMException("division by zero");
						push(pop() / val);
						break;
					case rem:
						val = pop();
						if (val == 0) throw new VMException("division by zero");
						push(pop() % val);
						break;
					case neg:
						push(-pop());
						break;
					case shl:
						val = pop();
						push(pop() << val);
						break;
					case shr:
						val = pop();
						push(pop() >> val);
						break;
					case inc:
						off = fp + next(true);
						local[off] += next(true);
						break;

					// object creation
					case new_:
						push(alloc(next2(true)));
						break;
					case newarray:
						val = next(true);
						len = pop();
						if (val == 0) adr = alloc(len + 4); 
						else adr = alloc(len * 4 + 4);
						heap[adr] = len;
						push(adr + 1);   // skip length field of array
						break;

					// array access
					case aload:
						idx = pop();
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						len = heap[adr - 1];
						if (idx < 0 || idx >= len) throw new VMException("index out of bounds");
						push(heap[adr+idx]);
						break;
					case astore:
						val = pop();
						idx = pop();
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						len = heap[adr - 1];
						if (debug) {
							System.out.println("\nArraylength = " + len);
							System.out.println("Address = " + adr);
							System.out.println("Index = " + idx);
							System.out.println("Value = " + val);
						}
						if (idx < 0 || idx >= len) throw new VMException("index out of bounds");
						heap[adr+idx] = val;
						break;
					case baload:
						idx = pop();
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						len = heap[adr - 1];
						if (idx < 0 || idx >= len) throw new VMException("index out of bounds");
						push(getByte(heap[adr + idx/4], idx % 4));
						break;
					case bastore:
						val = pop();
						idx = pop();
						adr = pop();
						if (adr == 0) throw new VMException("null reference used");
						len = heap[adr - 1];
						if (idx < 0 || idx >= len) throw new VMException("index out of bounds");
						heap[adr + idx/4] = setByte(heap[adr + idx/4], idx % 4, (byte)val);
						break;
					case arraylength:
						adr = pop();
						if (adr==0) throw new VMException("null reference used");
						push(heap[adr - 1]);
						break;

					// stack manipulation
					case pop:
						pop();
						break;
					case dup:
						val = pop(); push(val); push(val);
						break;
					case dup2:
						val = pop(); val2 = pop();
						push(val2); push(val); push(val2); push(val);
						break;

					// jumps
					case jmp:
						off = next2(true);
						pc += off - 3;
						break;
					case jcond+eq: case jcond+ne: case jcond+lt:
					case jcond+le: case jcond+gt: case jcond+ge:
						off = next2(true);
						val2 = pop(); val = pop();
						boolean cond = false;
						switch(op - jcond) {
							case eq: cond = val == val2; break;
							case ne: cond = val != val2; break;
							case lt: cond = val < val2;  break;
							case le: cond = val <= val2; break;
							case gt: cond = val > val2;  break;
							case ge: cond = val >= val2; break;
						}
						if (cond) pc += off - 3;
						break;

					// static method calls
					case call:
						off = next2(true);
						PUSH(pc);
						pc += off - 3;
						break;
					case return_:
						if (sp == 0) return; else pc = POP();
						break;
					case enter:
						int psize = next(true);
						int lsize = next(true);
						PUSH(fp);
						fp = sp;
						for (i = 0; i < lsize; i++) PUSH(0);
						for (i = psize - 1; i >= 0; i--) local[fp + i] = pop();
						break;
					case exit:
						sp = fp;
						fp = POP();
						break;

					// dynamic method dispatch
					case invokevirtual:
						int nameStart = pc;
						int methAdr = 0;
						boolean p = false;
						int ch = -1;
						adr = pop();
						val = data[adr++];
						for ( ; val != -2; val = data[adr++]) {
						ch = next4();
						if (ch == val && ch != -1) continue;
						else if (ch == -1 && val == -1) {
											    methAdr = data[adr];
											    p = true;
											    break;
											  }
							else if (ch == -1 && val != -1) {
												    pc = nameStart;
												    for ( ; val != -1; val = data[adr++]);
												    val = data[++adr];
												  }
								else { pc = nameStart;
									 for ( ; val != -1; val = data[adr++]);
									 val = data[++adr];
								     }
						}
						if (!p) { for ( ; ch != -1; ch = next4()); throw new VMException("method address not found"); }
						else { PUSH(pc); pc = methAdr; }
						break; 

					// I/O
					case read:
						try {
							val = readInt();
							push(val);
						} catch (IOException ex) {
							throw new VMException("unexpected end of input");
						}
						break;
					case print:
						len = pop();
						val = pop();
						String s = new Integer(val).toString();
						len = len - s.length();
						for (i = 0; i < len; i++) System.out.print(' ');
						System.out.print(s);		// AW: does the same as the for-loop below
						// for (i = 0; i < s.length(); i++) System.out.print(s.charAt(i));
						break;
					case bread:
						try {
							push(System.in.read());
						} catch (IOException ex) {
							throw new VMException("end of input");
						}
						break;
					case bprint:
						len = pop() - 1;
						val = pop();
						for (i = 0; i < len; i++) System.out.print(' ');
						System.out.print((char)val);
						break;
					case trap:
						throw new VMException("trap(" + next(true) + ")");
					default:
						throw new VMException("wrong opcode " + op);
				}
				if (debug) {
					System.out.println();
					System.out.print("     | ");
					printStack();
				}
			}
		} catch (VMException e) {
			System.out.println("\n-- exception at address " + (pc-1) + 
			                   ": " + e.getMessage());
		}
	}

	public static void main (String[] arg) {
		String fileName = null;
		debug = false;
		for (int i = 0; i < arg.length; i++) {
			if (arg[i].equals("-debug")) debug = true;
			else fileName = arg[i];
		}
		if (fileName == null) {
			System.out.println("Syntax: java ssw.mj.Run filename [-debug]");
			return;
		}
		try {
			load(fileName);
			heap  = new int[heapSize];    // fixed sized heap
			data  = new int[dataSize];    // global data as specified in classfile
			stack = new int[eStackSize];  // expression stack
			local = new int[mStackSize];  // method stack
			fp = 0; sp = 0;
			esp = 0;
			free = 1;		// no block should start at address 0
			long startTime = System.currentTimeMillis();

			interpret();

			System.out.print("\nCompletion took " + 
			                 (System.currentTimeMillis()-startTime) + " ms");
		} catch (FileNotFoundException e) {
			System.out.println("-- file " + fileName + " not found");
		} catch (IOException e) {
			System.out.println("-- error reading file " + fileName);
		} catch (FormatException e) {
			System.out.println("-- corrupted object file " + fileName + 
			                   ": " + e.getMessage());
		}
	}
}

class FormatException extends Exception {
	FormatException(String s) { super(s); }
}

class VMException extends Exception {
	VMException(String s) { super(s); }
}
