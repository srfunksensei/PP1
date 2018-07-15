package symboltable;

import hashtable.HashTable;

/**
 * MikroJava tabela simbola
 * 
 * @author ETF
 * 
 */
public class Tab {
  // standardni tipovi
  public static final Struct noType = new Struct(Struct.None),
      intType = new Struct(Struct.Int), charType = new Struct(Struct.Char),
      nullType = new Struct(Struct.Class);

  public static final Obj noObj = new Obj(Obj.Var, "noObj", noType);

  public static Obj chrObj, ordObj, lenObj;

  public static Scope topScope; // tekuci opseg
  private static int level; // nivo ugnezdavanja tekuceg opsega

  public static boolean duplicate = false;
  public static boolean error = false;
  public static int errnum = 0;

  /**
   * Inicijalizacija universe opsega, tj. njegovo popunjavanje Obj cvorovima,
   * kao sto je izlozeno na vezbama i predavanjima. Razlika je sto se Obj
   * cvorovu umecu u hes tabelu.
   */
  public static void init() {
    topScope = null;
    Scope s = new Scope();
    topScope = s;
    int k = Obj.Type;
    String n = "int";
    Struct t = intType;
    Obj bucket = new Obj(k, n, t);
    //topScope.locals = new HashTable();
    HashTable universe = topScope.locals;
    universe.insertKey(bucket, false);
    universe.insertKey(new Obj(Obj.Type, "char", charType), false);
    bucket = new Obj(Obj.Con, "eol", charType, 10, 0);
    universe.insertKey(bucket, false);
    bucket = new Obj(Obj.Con, "null", nullType, 0, 0);
    universe.insertKey(bucket, false);
    bucket = new Obj(Obj.Meth, "chr", charType, 0, 1);
    universe.insertKey(bucket, false);
    bucket.setLocals(new HashTable());
    bucket.getLocals().insertKey(new Obj(Obj.Var, "i", intType, 0, 1), false);
    chrObj = bucket;
    bucket = new Obj(Obj.Meth, "ord", intType, 0, 1);
    universe.insertKey(bucket, false);
    bucket.setLocals(new HashTable());
    bucket.getLocals().insertKey(new Obj(Obj.Var, "ch", charType, 0, 1), false);
    ordObj = bucket;
    bucket = new Obj(Obj.Meth, "len", intType, 0, 1);
    universe.insertKey(bucket, false);
    bucket.setLocals(new HashTable());
    bucket.getLocals().insertKey(
        new Obj(Obj.Var, "arr", new Struct(Struct.Array, noType), 0, 1), false);
    lenObj = bucket;
    topScope.nVars = 7;
    level = -1;
  }

  /**
   * Otvaranje novog opsega
   */
  public static void openScope() {
    Scope s = new Scope();
    s.outer = topScope;
    topScope = s;
    level++;
  }

  /**
   * Zatvaranje opsega
   */
  public static void closeScope() {
    topScope = topScope.outer;
    level--;
  }

  /**
   * Pravi se novi Obj cvor sa prosledjenim atributima kind, name i type, pa se
   * zatim ubacuje u tabelu simbola. Povratna vrednost: - novostvoreni cvor, ako
   * cvor sa tim imenom nije vec postojao u tabeli simbola. - postojeci cvor iz
   * tabele simbola, ako je doslo do greske jer smo pokusali da u tabelu simbola
   * za opseg ubacimo cvor sa imenom koje vec postoji.
   */
  public static Obj insert(int kind, String name, Struct type) {
    HashTable scopeSyms = topScope.locals;

    // create a new Object node with kind, name, type
    Obj newObj;
    if (level != 0) newObj = new Obj(kind, name, type, 0, 1); // local
    else
      newObj = new Obj(kind, name, type, 0, 0); // global

    // append the node to the end of the symbol list
    if (!scopeSyms.insertKey(newObj, true)) {
      error("GRESKA: Ime " + name + " je vec deklarisano.");
      duplicate = true;
      return (Obj) scopeSyms.searchKey(name);
    }

    duplicate = false;
    newObj.setAdr(topScope.nVars++);
    
    return newObj;
  }
  
  public static Obj insert(int kind, String name, int line, Struct type) {  
	  HashTable scopeSyms = topScope.locals;

	    // create a new Object node with kind, name, type
	    Obj newObj;
	    if (level != 0) newObj = new Obj(kind, name, type, 0, 1); // local
	    else
	      newObj = new Obj(kind, name, type, 0, 0); // global

	    
	    // append the node to the end of the symbol list
	    if (!scopeSyms.insertKey(newObj, true)) {
	      error("ERROR in line " + line + " (" + name + ") already declared" );
	      duplicate = true; //duplicate
	      return (Obj) scopeSyms.searchKey(name);
	    }

	    duplicate = false;
	    newObj.setAdr(topScope.nVars++);
	    
	    return newObj; 
	  }

  /**
   * U hes tabeli opsega trazi Obj cvor sa imenom name, pocevsi od
   * najugnezdenijeg opsega, pa redom kroz opsege na nizim nivoima. Povratna
   * vrednost: - pronadjeni Obj cvor, ako je pretrazivanje bilo uspesno. -
   * Tab.noObj objekat, ako je pretrazivanje bilo neuspesno.
   */
  public static Obj find(String name) {
    HashTable scopeSyms;
    Obj o = null;
    for (Scope s = topScope; s != null; s = s.outer) {
    	
      scopeSyms = s.locals;
      o = (Obj)scopeSyms.searchKey(name);
      if(o != null) return o;
      
//      if (scopeSyms != null) {
//        o = (Obj) scopeSyms.searchKey(name);
//      }
    }
    if (o == null) {
      error("GRESKA: Simbol " + name + " nije pronadjeno.");
      return noObj;
    } else {
      return o;
    }
  }


  /** Stampa poruku o gresci. */
  public static void error(String err) {
    System.err.println(err);
    Tab.error = true;
    Tab.errnum++;
  }

  /** Stampa sadrzaj tabele simbola. */
  public static void dump() {
    int l = level;
    System.out
        .println("=====================SYMBOL TABLE DUMP=========================");
    for (Scope s = topScope; s != null; s = s.outer) {
      System.out.println("(Level " + l + ")");
      
      HashTable scopeHash = s.locals;
      System.out.print(scopeHash.toString() + "\n");
      

      l--;
    }
  }
}