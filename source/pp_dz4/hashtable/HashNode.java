package hashtable;

/**
 * Klasa koja predstavlja ulaz u HashTable.
 * Treba koristiti tehniku odvojenog ulancavanja
 * (separate chaining). Radi jednostavnije 
 * treba korisiti dvostruko ulancanu listu. U 
 * zaglavlju (hash tabeli) je smesten pokazivac na
 * prvi element liste
 * 
 * @author ETF
 *
 */
public class HashNode {
  protected String name;  // kljuc za pretrazivanje
  private HashNode next;  // naredni element dvostruko ulancane liste
  private HashNode prev;  // prethodni element dvostruko ulancane liste
  
  public HashNode(String na) {
    name = na;
  }
  
  public HashNode(String na, HashNode n) {
    this(na, n, null);
  }
  
  public HashNode(String na, HashNode n, HashNode p) {
    name = na;
    next = n;
    prev = p;
  }
  
  public void setNext(HashNode n) {
    next = n;
  }
  
  public HashNode getNext() {
    return next;
  }
  
  public void setPrev(HashNode p) {
    prev = p;
  }
  
  public HashNode getPrev() {
    return prev;
  }
  
  public String getName() {
    return name;
  }
  
}
