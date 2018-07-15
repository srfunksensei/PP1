package symboltable;

import hashtable.*;

/**
 * Objekti u MikroJava tabeli simbola: Svaki imenovani objekat u programu se
 * skladisti u Obj cvoru. Svakom opsegu se pridruzuje hes tabela u kojoj se
 * nalaze sva imena deklarisana u okviru opsega.
 * 
 * @author ETF
 * 
 */
public class Obj extends HashNode {
  public static final int Con = 0, Var = 1, Type = 2, Meth = 3, Fld = 4,
      Prog = 5, Elem = 6;

  // Con, Var, Type, Meth, Fld, Prog
  private int kind;

  // tip pridruzen imenu
  private Struct type;

  // konstanta(Con): vrednost
  // Meth, Var, Fld: memorijski ofset
  private int adr;

  // Var: nivo ugnezdavanja
  // Meth: broj formalnih argumenata
  private int level;

  // Meth: redni broj formalnog argumenta u definiciji metode
  private int fpPos;

  // Meth: hes lokalnih promenljivih
  // Prog: tabela simbola programa
  private HashTable locals;

  public Obj(int kind, String name, Struct type) {
    super(name);
    this.kind = kind;
    this.type = type;
  }

  public Obj(int kind, String name, Struct type, int adr, int level) {
    super(name);
    this.kind = kind;
    this.type = type;
    this.adr = adr;
    this.level = level;
  }

  public int getAdr() {
    return adr;
  }

  public void setAdr(int adr) {
    this.adr = adr;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getFpPos() {
    return fpPos;
  }

  public void setFpPos(int fpPos) {
    this.fpPos = fpPos;
  }

  public HashTable getLocals() {
    return locals;
  }

  public void setLocals(HashTable locals) {
    this.locals = locals;
  }

  public Struct getType() {
	return type;
  }

  public void setType(Struct type) {
	this.type = type;
  }

  public int getKind() {
	return kind;
  }

  public void setKind(int kind) {
	this.kind = kind;
  }

  public boolean equals(Object o) {
    if (super.equals(o)) return true; // jednake reference

    if (!(o instanceof Obj)) return false;

    Obj other = (Obj) o;

    return kind == other.kind && name.equals(other.name)
        && type.equals(other.type) && adr == other.adr && level == other.level
        && equalsCompleteHash(locals, other.locals);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    boolean f = false, m = false;

    switch (kind) {
    case Con:
      sb.append("Con ");
      break;
    case Var:
      sb.append("Var ");
      break;
    case Type:
      sb.append("Type ");
      break;
    case Meth:
      sb.append("Meth ");
      m = true;
      break;
    case Fld:
      sb.append("Fld ");
      f = true;
      break;
    case Prog:
      sb.append("Prog ");
      break;
    }
    sb.append(name);
    sb.append(": ");
    HashTable localHash = locals;

    switch (type.getKind()) {
    case Struct.None:
      sb.append("notype");
      break;
    case Struct.Int:
      sb.append("int");
      break;
    case Struct.Char:
      sb.append("char");
      break;
    case Struct.Array:
      sb.append("Arr of ");
      switch (type.getElemType().getKind()) {
      case Struct.None:
        sb.append("notype");
        break;
      case Struct.Int:
        sb.append("int");
        break;
      case Struct.Char:
        sb.append("char");
        break;
      case Struct.Class:
        sb.append("Class");
        break;
      }
      break;
    case Struct.Class:
      sb.append("Class");
      if (!f && !m) localHash = type.getFields();
      break;
    }
    sb.append(", ");
    sb.append(adr);
    sb.append(", ");
    sb.append(level + " ");
    if (localHash != null) {
    	 // *** IMPLEMENT *** add all hash elements to sb
    	
        sb.append("[" + localHash.toString() + "]\n"); 
    }
    return sb.toString();
  }

  /**
   * Poredi dve hes tabele h1 i h2. Dve hes tabele h1 i h2 su jednake ako su: 1.
   * h1 i h2 jednake reference ILI 2. elementi koji se pri obilasku oba hesa, na
   * isti nacin, nalaze na istim pozicijama jednaki (to se proverava metodom
   * equals koja je redefinisana za klasu Obj).
   */
  public static boolean equalsCompleteHash(HashTable h1, HashTable h2) {
    return h1.equals(h2);
  }
}
