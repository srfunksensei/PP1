package symboltable;

import hashtable.HashTable;

/**
 * Opseg u MikroJava tabeli simbola
 * 
 * @author ETF
 * 
 */
public class Scope {
  Scope outer; // referenca na okruzujuci opseg
  public HashTable locals; // stablo (tabela simbola) za ovaj opseg
  public int nVars; // broj simbola deklarisanih u opsegu
  
  public Scope(){
	outer=null;
	locals=new HashTable();
	nVars=0;
  }
  
}
