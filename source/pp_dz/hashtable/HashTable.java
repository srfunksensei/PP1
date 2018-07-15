package hashtable;

import symboltable.*;

/**
 * Hash Table. Koristi se odvojeno ulancavanje
 * (separate chaining).
 * 
 * @author ETF
 *
 */
public class HashTable {
  private HashNode[] table; // hash tabela
  
  private int numOfElem = 0;  // broj kljuceva u hash tabeli
  
  private static int INIT_SIZE = 64;
  
  public HashTable() {
    this(INIT_SIZE);
  }
  
  public HashTable(int s) {
    table = new HashNode[s];
  }
  
  public int getNumOfElem() {
    return numOfElem;
  }
  
  public HashNode[] getTable() {
	return table;
  }
  
  public HashNode getHashNode(int i){
	  if(i >= 0 && i<table.length){
		  return table[i];
	  }else {
		  System.out.print("ERROR! Index out of bounds!\n");
		  return null;
	  }
  }
  
  public void setHashNode(int i , HashNode node){
	  if(i >= 0 && i<table.length){
		  table[i] = node;
		  numOfElem++;
	  }else {
		  System.out.print("ERROR! Index out of bounds!\n");
		  return;
	  }
  }

/**
   * Pretrazivanje hash-a na odredjenu vrednost
   * kljuca.
   * 
   * @return HashNode koji sadrzi kljuc po kome
   * se pretrazivalo ukoliko je kljuc nadjen, null
   * u suprotnom
   */
  public HashNode searchKey(String key) {
    int index = hash(key) % table.length; // upotreba hash metode
    
    // *** IMPLEMENT ***
    if (index < 0 ) index = Math.abs(index);
    HashNode tmp = table[index];
    while(tmp != null){
    	if(tmp.name.equalsIgnoreCase(key)) return tmp;
    	else tmp = tmp.getNext();
    }
    return null;
  }
  
  /** 
   * Brisanje elementa sa vrednoscu kljuca key.
   * print == true ako zelimo da se na izlaz ispisuje poruka pri pokusaju
   * brisanja kljuca koji se ne nalazi u hesu.
   * print == false ako NE zelimo da se na izlaz ispisuje poruka pri pokusaju
   * brisanja kljuca koji se ne nalazi u hesu.
   * 
   * @return Uspesnosti operacije brisanja. true je indikacija uspesnog
   * brisanja kljuca iz hash tabele. false u suprotnom
   */    
  public boolean deleteKey(String key, boolean print) {
	  HashNode tmp = searchKey(key);
	  
	  System.out.print("delete\n");
	// *** IMPLEMENT ***
	    if(tmp == null) {
	    	if (print) System.out.println("Error! Deletion of non-exiting key!");
	    	return false;
	    } else {
	    	int indx = hash(key) % table.length;
	    	if (indx <0 ) indx = Math.abs(indx);
	    	
	    	if(tmp.getPrev() == null){
	    		table[indx] = tmp.getNext();
	    		if (tmp.getNext() != null)tmp.getNext().setPrev(null);
	    	} else{
	    		HashNode q = tmp.getPrev(), r = tmp.getNext();
	    		if( r!= null) r.setPrev(q);
	    		q.setNext(r);
	    		
	    	}
	    	
	    }
	    
	    numOfElem--;
	    return true;
  }
  
  /** 
   * Umetanje elementa sa novom vrednoscu kljuca.
   * Argument key je vrednost kljuca koji treba umetnuti.
   * Duplikati kljuceva nisu dozvoljeni.
   * print == true ako zelimo da se na izlaz ispisuje poruka pri pokusaju
   * umetanja vec postojeceg kljuca.
   * print == false ako NE zelimo da se na izlaz ispisuje poruka pri pokusaju
   * umetanja vec postojeceg kljuca.
   *
   * @return true ukoliko je umentanje uspesno, false u suprotnom
   */
  public boolean insertKey(String key, boolean print) {
//    HashNode tmp = searchKey(key);
//    
//    // *** IMPLEMENT ***
//    if(tmp != null) {
//    	if (print) System.out.println("Error! Insertion of exiting key!");
//    	return false;
//    } else {
//    	int indx = hash(key) % table.length;
//    	if (indx <0 ) indx = Math.abs(indx);
//    	
//    	HashNode nodeStart = table[indx],
//    			 nodeForInsert = new HashNode(key);
//    	
//    	if(nodeStart == null){//if entry is empty
//    		table[indx] = nodeForInsert;
//    	} else{
//    		while(nodeStart.getNext() != null){
//    			nodeStart = nodeStart.getNext();
//    		}
//    		nodeStart.setNext(nodeForInsert);
//    		nodeForInsert.setPrev(nodeStart);
//    	}
//    }
//    
//    numOfElem++;
    return true;
  }
  
  /** 
   * Umetanje novog elementa node u hes.
   * Element ne sme imati kljuc koji vec postoji u hesu.
   * print == true ako zelimo da se na izlaz ispisuje poruka pri pokusaju
   * umetanja elementa koji sadrzi vec postojeci kljuc.
   * print == false ako NE zelimo da se na izlaz ispisuje poruka pri pokusaju
   * umetanja elementa koji sadrzi vec postojeci kljuc.
   *
   * @return true ukoliko je umetanje uspesno, false u suprotnom
   */
    public boolean insertKey(HashNode node, boolean print) {
      //return insertKey(node.getName(), print);
    	HashNode tmp = searchKey(node.getName());
        
        // *** IMPLEMENT ***
        if(tmp != null) {
        	if (print) System.out.println("Error! Insertion of exiting key!");
        	return false;
        } else {
        	int indx = hash(node.getName()) % table.length;
        	if (indx <0 ) indx = Math.abs(indx);
        	
        	HashNode nodeStart = table[indx],
        			 nodeForInsert = node;
        	
        	if(nodeStart == null){//if entry is empty
        		table[indx] = nodeForInsert;
        	} else{
        		while(nodeStart.getNext() != null){
        			nodeStart = nodeStart.getNext();
        		}
        		nodeStart.setNext(nodeForInsert);
        		nodeForInsert.setPrev(nodeStart);
        	}
        }
        
        numOfElem++;
        return true;
    	
    }
  
  /** 
   * hash funkcija
   */
  private static int hash (String key) {
      /* this algorithm was created for sdbm
       * (a public-domain reimplementation of ndbm) database library.
       * what is included below is the faster version used in gawk.
       */
      int h = 0;
      for (int i=0; i < key.length(); i++) {
          h = key.charAt(i) + (h << 6) + (h << 16) - h;
      }
      return h;  
  }
  
  public boolean equals(HashTable h){
	  if(table.length!=h.getTable().length || numOfElem != h.getNumOfElem()) return false;  
	  else{
		  for(int i=0;i<table.length;i++){
			  HashNode temp1 = table[i];
			  HashNode temp2 = h.getTable()[i];
			  while(temp1!=null && temp2!=null){
				  if(!temp1.name.equals(temp2.name)) return false;
				  temp1 = temp1.getNext();
				  temp2 = temp2.getNext();
			      if(temp1==null && temp2!=null ||temp2==null && temp1!=null )	return false;  
			  }
	    }
		  return true;
    }
  }
  
  public String toString(){
	  StringBuffer sb = new StringBuffer();
	  if (numOfElem != 0){
	  
		  for(int i = 0; i<table.length; i++){
			  Obj node = (Obj)table[i];
			  while(node != null){
				  
				  sb.append(node.toString());
				  if(node.getLocals() == null){
					  if(node.getName() == "i" || node.getName() == "ch" || 
							  node.getName() == "arr"){
						  sb.append("");  
					  } else {
						  sb.append("\n");
					  }
				  }
				  node = (Obj)node.getNext();
			  }
		  }
	  }
	  return sb.toString();
  }
}
