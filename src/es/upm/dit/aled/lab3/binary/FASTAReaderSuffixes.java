package es.upm.dit.aled.lab3.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.dit.aled.lab3.FASTAReader;

/**
 * Reads a FASTA file containing genetic information and allows for the search
 * of specific patterns within these data. The information is stored as an array
 * of bytes that contain nucleotides in the FASTA format. Since this array is
 * usually created before knowing how many characters in the origin FASTA file
 * are valid, an int indicating how many bytes of the array are valid is also
 * stored. All valid characters will be at the beginning of the array.
 * 
 * This extension of the FASTAReader uses a sorted dictionary of suffixes to
 * allow for the implementation of binary search.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FASTAReaderSuffixes extends FASTAReader {
	protected Suffix[] suffixes;

	/**
	 * Creates a new FASTAReader from a FASTA file.
	 * 
	 * At the end of the constructor, the data is sorted through an array of
	 * suffixes.
	 * 
	 * @param fileName The name of the FASTA file.
	 */
	public FASTAReaderSuffixes(String fileName) {
		// Calls the parent constructor
		super(fileName);
		this.suffixes = new Suffix[validBytes];
		for (int i = 0; i < validBytes; i++)
			suffixes[i] = new Suffix(i);
		// Sorts the data
		sort();
	}

	/*
	 * Helper method that creates a array of integers that contains the positions of
	 * all suffixes, sorted alphabetically by the suffix.
	 */
	private void sort() {
		// Instantiate the external SuffixComparator, passing 'this' (the reader)
		// so it can access the content and validBytes fields.
		SuffixComparator suffixComparator = new SuffixComparator(this);
		// Use the external Comparator for sorting.
		Arrays.sort(this.suffixes, suffixComparator);
	}

	/**
	 * Prints a list of all the suffixes and their position in the data array.
	 */
	public void printSuffixes() {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Index | Sequence");
		System.out.println("-------------------------------------------------------------------------");
		for (int i = 0; i < suffixes.length; i++) {
			int index = suffixes[i].suffixIndex;
			String ith = "\"" + new String(content, index, Math.min(50, validBytes - index)) + "\"";
			System.out.printf("  %3d | %s\n", index, ith);
		}
		System.out.println("-------------------------------------------------------------------------");
	}

	/**
	 * Implements a binary search to look for the provided pattern in the data
	 * array. Returns a List of Integers that point to the initial positions of all
	 * the occurrences of the pattern in the data.
	 * 
	 * @param pattern The pattern to be found.
	 * @return All the positions of the first character of every occurrence of the
	 *         pattern in the data.
	 */
	@Override
	public List<Integer> search(byte[] pattern) {
	//Inicialización 
	List <Integer> resultados = new ArrayList<Integer>();
	int lo = 0;
	int hi = suffixes.length; 
	boolean found = false; 
	int index = 0; 
	
	//Mientras found sea false y mientras que hi-lo sea mayor que 1, entonces se ejecuta el bucle while. 
	//En el enunciado de la práctica, realmente lo pone "al revés", porque nos da la condición de terminación,
	//pero los bucles while se usan poniendo entre paréntesis la condición que se tiene que dar para que siga ejecut. 
	while (!found && hi-lo>1) {
		
		//La m siempre es así en búsqueda binaria
		int m = (lo + hi)/2;
		
		//Me voy a guardar en una variable que se llama posSuffix el numerito interno que hay dentro de la pos m 
		//del array suffixes. Lo que guardo en esa variable, ES UN OBJETO SUFFIX!!!!
		int posSuffix = this.suffixes[m].suffixIndex;
		
		//Ahora buscamos como tal la palabara que queremos dentro del sufijo. A medida que van coicidiendo las letras,
		//voy aumentando la variable index, de forma que si la palabra que queria tenía 3 letras y he incrementado 
		//la variable index 3 veces, querrá decir que he encontrado la palabra. 
		//Tenemos que tener cuidado de que el index no se salga ni del pattern (palabra que buscamos), ni del 
		//content[posSuffix] (sufijo donde estamos buscando). Esto son las dos sentencias iniciales del while. Ya la
		//tercera es que vaya coincidiendo las letras (pattern [index]== content[posSuffix]). 
		while(posSuffix + index  < content.length && index < pattern.length && pattern [index]== content[posSuffix]);
			index ++;
			
	
		if (index == pattern.length) {
			resultados.add(posSuffix);
			found = true; //con esto solo encontramos una coincidencia 
			
			//recorro hacia detrás 
			int i = 1;
			do {
				index = 0; 
				//recorro hacia detrás 
				posSuffix = suffixes[m-i].suffixIndex;
				//misma sentencia de comprobación de cada una de las letras 
				while(posSuffix + index  < content.length && index < pattern.length && pattern [index]== content[posSuffix])
				index ++;
				
			if (index == pattern.length)
				resultados.add(posSuffix);
			i ++; 
			
			}
			while(index==pattern.length);
		
		//recorro hacia delante
	    i = 1;
		do {
			index = 0; 
			//recorro hacia delante 
			posSuffix = suffixes[m+i].suffixIndex;
			//misma sentencia de comprobación de cada una de las letras 
			while(posSuffix + index  < content.length && index < pattern.length && pattern [index]== content[posSuffix])
				index ++;
			
		if (index == pattern.length) 
			resultados.add(posSuffix);
		i ++; 
		
	} 
		while(index==pattern.length);
			
}
		//He buscado en medio pero no era ahí, ahora me voy para un lado o para otro. Irme o un lado a otro me lo dirá
		//si la letra está alfabéticamente antes o despu
		else {
			if (pattern [index]< content[posSuffix + index])
				hi = m--; //descartamos la mitad derecha. TE VAS A LA IZQUIERDA 
			else 
				lo = m++; //descartamos la mitad izquierda. TE VAS A LA DERECHA 
			index = 0; 
		}
	
	}
	return resultados; 

}

	public static void main(String[] args) {
		long t1 = System.nanoTime();
		FASTAReaderSuffixes reader = new FASTAReaderSuffixes(args[0]);
		if (args.length == 1)
			return;
		byte[] patron = args[1].getBytes();
		System.out.println("Tiempo de apertura de fichero: " + (System.nanoTime() - t1));
		long t2 = System.nanoTime();
		System.out.println("Tiempo de ordenación: " + (System.nanoTime() - t2));
		reader.printSuffixes();
		long t3 = System.nanoTime();
		List<Integer> posiciones = reader.search(patron);
		System.out.println("Tiempo de búsqueda: " + (System.nanoTime() - t3));
		if (posiciones.size() > 0) {
			for (Integer pos : posiciones)
				System.out.println("Encontrado " + args[1] + " en " + pos);
		} else
			System.out.println("No he encontrado " + args[1] + " en ningún sitio.");
		System.out.println("Tiempo total: " + (System.nanoTime() - t1));
	}
}
