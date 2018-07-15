package symboltable;

import hashtable.HashTable;

/**
 * Struktura tipa u MikroJavi.
 * 
 * @author ETF
 * 
 */
public class Struct {

  // kodiranje tipova
  public static final int None = 0;
  public static final int Int = 1;
  public static final int Char = 2;
  public static final int Array = 3;
  public static final int Class = 4;

  private int kind; // None, Int, Char, Array, Class

  private Struct elemType; // niz: tip elementa niza

  // klasa: broj bolja klase
  private int fNum;

  // klasa: referenca na hes tabelu u kojoj se nalaze polja klase
  private HashTable fields;

  public Struct(int kind) {
    this.kind = kind;
  }

  public Struct(int kind, Struct elemType) {
    this.kind = kind;
    if (kind == Array) this.elemType = elemType;
  }

  public Struct(int kind, Struct elemType, HashTable fields) {
    this.kind = kind;
    this.elemType = elemType;
    this.fields = fields;
  }

  public Struct(int kind, int fNum, HashTable fields) {
	    this.kind = kind;
	    this.fNum = fNum;
	    this.fields = fields;
  }
	  
  public int getKind() {
    return kind;
  }

  public Struct getElemType() {
    return elemType;
  }

  public int getFNum() {
    return fNum;
  }

  public HashTable getFields() {
    return fields;
  }

  public boolean equals(Object o) {
    // najpre provera da li su reference jednake
    if (super.equals(o)) return true;

    if (!(o instanceof Struct)) return false;

    return equals((Struct) o);
  }

  public boolean isRefType() {
    return kind == Class || kind == Array;
  }

  public boolean equals(Struct other) {
    if (kind == Array) return other.kind == Array
        && elemType.equals(other.elemType);

    if (kind == Class) return other.kind == Class && fNum == other.fNum
        && Obj.equalsCompleteHash(fields, other.fields);

    // mora biti isti Struct cvor
    return this == other;
  }

  public boolean compatibleWith(Struct other) {
    return this.equals(other) || this == Tab.nullType && other.isRefType()
        || other == Tab.nullType && this.isRefType();
  }

  public boolean assignableTo(Struct dest) {
    return this.equals(dest) || this == Tab.nullType && dest.isRefType()
        || this.kind == Array && dest.kind == Array
        && dest.elemType == Tab.noType;
  }
}
