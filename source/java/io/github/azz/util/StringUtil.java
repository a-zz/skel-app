/* ****************************************************************************************************************** *
 * StringUtil.java                                                                                                    *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

/**
 * Utility class for string handling and processing
 * 
 * @author a-zz
 */
public class StringUtil {

	/**
	 * Wraps a string to a certaing line lenght.
	 * 	<br/><br/>
	 * 	This is a quick & dirty function for wrapping codes, string keys and the like, not to be used for human-readable 
	 * 	texts as no smart word division is done at end of line. For better results, line-feeds contained in the input
	 * 	string are removed prior to wrapping. 
	 * @param text (String) The text to be wrapped.
	 * @param lineLenght (int) The resulting line length. 0 for no wrapping.
	 * @return (String) The wrapped text.
	 */
	public static String lineWrap(String src, int lineLenght) {
				
		src = src.replace(System.getProperty("line.separator"), " ");
		if(lineLenght==0)
			return src;
		
		String result = "";
		int line = 0;
		for(int i = 0; i<src.length(); i++) {
			line++;
			result += src.charAt(i);
			if(line==64) {
				result += System.getProperty("line.separator");
				line = 0;
			}
		}

		return result;
	}

	/**
	 * Generates a random "Lorem ipsum" text, for layout samples and the like.  
	 * @param paragraphs (int) Number of paragraphs to produce.
	 * @param maxWordsPerPrgrph (int) Maximum number of words per paragraph. 
	 * @param html (true) Sets wether HTML markup should be used (&lt;p&gt;...&lt;/p&gt;)
	 * @return (String) Random text, but always beginning with "Lorem ipsum dolor sit amet"
	 */
	public static String loremIpsum(int paragraphs, int maxWordsPerPrgrph, boolean html) {
		
		String result = "";
				
		for(int p = 0; p<paragraphs; p++) {
			result += html?"<p>":"";
			boolean capitalize = true;
			if(p==0) {
				result += "Lorem ipsum dolor sit amet ";
				capitalize = false;
			}
			int wordsPerPrgrph = (int)Math.floor(Math.random() * maxWordsPerPrgrph);
			for(int w=0; w<wordsPerPrgrph; w++) {
				result += pickRandomWord(capitalize);
				capitalize = false;				
				if(Math.random()>0.9 || w==wordsPerPrgrph-1) {
					result+=". ";
					capitalize = true;
				}
				else if(Math.random()>0.65)
					result += ", ";
				else
					result+=" ";
			}
			result += html?"</p>":"\n\n";
		}
		
		return result;
	}
		
	private static String pickRandomWord(boolean capitalize) {
		
		String word = loremIpsumSource[(int)Math.floor(Math.random() * loremIpsumSource.length)];
		return capitalize?word.substring(0, 1).toUpperCase() + word.substring(1):word;
	}
	
	private static String[] loremIpsumSource = { "a", "ac", "accommodare", "accumsan", "accusata", "ad", "adhuc",
			"adipisci", "adipiscing", "adolescens", "adversarium", "aenean", "aeque", "affert", "agam", "alia",
			"alienum", "aliquam", "aliquet", "aliquid", "aliquip", "altera", "alterum", "amet", "an", "ancillae",
			"animal", "ante", "antiopam", "aperiri", "appareat", "appetere", "aptent", "arcu", "assueverit", "at",
			"atomorum", "atqui", "auctor", "audire", "augue", "autem", "bibendum", "blandit", "brute", "causae",
			"cetero", "ceteros", "civibus", "class", "commodo", "commune", "comprehensam", "conceptam",
			"conclusionemque", "condimentum", "congue", "consectetuer", "consectetur", "consequat", "consetetur",
			"constituam", "constituto", "consul", "contentiones", "conubia", "convallis", "convenire", "corrumpit",
			"cras", "cu", "cubilia", "cum", "curabitur", "curae", "cursus", "dapibus", "debet", "decore", "definiebas",
			"definitionem", "definitiones", "delectus", "delenit", "delicata", "deseruisse", "deserunt", "deterruisset",
			"detracto", "detraxit", "diam", "dicam", "dicant", "dicat", "dicit", "dico", "dicta", "dictas", "dictum",
			"dictumst", "dicunt", "dignissim", "dis", "discere", "disputationi", "dissentiunt", "docendi", "doctus",
			"dolor", "dolore", "dolorem", "dolores", "dolorum", "doming", "donec", "dui", "duis", "duo", "ea", "eam",
			"efficiantur", "efficitur", "egestas", "eget", "ei", "eirmod", "eius", "elaboraret", "electram", "eleifend",
			"elementum", "elit", "elitr", "eloquentiam", "enim", "eos", "epicurei", "epicuri", "equidem", "erat",
			"eripuit", "eros", "errem", "error", "erroribus", "eruditi", "esse", "est", "et", "etiam", "eu", "euismod",
			"eum", "euripidis", "evertitur", "ex", "expetenda", "expetendis", "explicari", "fabellas", "fabulas",
			"facilis", "facilisi", "facilisis", "falli", "fames", "fastidii", "faucibus", "felis", "fermentum", "ferri",
			"feugait", "feugiat", "finibus", "fringilla", "fugit", "fuisset", "fusce", "gloriatur", "graece", "graeci",
			"graecis", "graeco", "gravida", "gubergren", "habemus", "habeo", "habitant", "habitasse", "hac", "harum",
			"has", "hendrerit", "himenaeos", "hinc", "his", "homero", "honestatis", "iaculis", "id", "idque", "ignota",
			"iisque", "imperdiet", "impetus", "in", "inani", "inceptos", "inciderint", "indoctum", "inimicus",
			"instructior", "integer", "intellegat", "intellegebat", "interdum", "interesset", "interpretaris",
			"invenire", "invidunt", "ipsum", "iriure", "iudicabit", "ius", "iusto", "iuvaret", "justo", "labores",
			"lacinia", "lacus", "laoreet", "latine", "laudem", "lectus", "legere", "legimus", "leo", "liber", "libero",
			"libris", "ligula", "litora", "lobortis", "lorem", "luctus", "ludus", "luptatum", "maecenas", "magna",
			"magnis", "maiestatis", "maiorum", "malesuada", "malorum", "maluisset", "mandamus", "massa", "mattis",
			"mauris", "maximus", "mazim", "mea", "mediocrem", "mediocritatem", "mei", "mel", "meliore", "melius",
			"menandri", "mentitum", "metus", "mi", "minim", "mnesarchum", "moderatius", "molestiae", "molestie",
			"mollis", "montes", "morbi", "movet", "mucius", "mus", "mutat", "nam", "nascetur", "natoque", "natum", "ne",
			"nec", "necessitatibus", "neglegentur", "neque", "netus", "nibh", "nihil", "nisi", "nisl", "no", "nobis",
			"noluisse", "nominavi", "non", "nonumes", "nonumy", "noster", "nostra", "nostrum", "novum", "nulla",
			"nullam", "numquam", "nunc", "ocurreret", "odio", "offendit", "omittam", "omittantur", "omnesque",
			"oporteat", "option", "oratio", "orci", "ornare", "ornatus", "partiendo", "parturient", "patrioque",
			"pellentesque", "penatibus", "per", "percipit", "pericula", "periculis", "perpetua", "persecuti",
			"persequeris", "persius", "pertinacia", "pertinax", "petentium", "pharetra", "phasellus", "placerat",
			"platea", "platonem", "ponderum", "populo", "porro", "porta", "porttitor", "posidonium", "posse", "possim",
			"possit", "postea", "postulant", "posuere", "potenti", "praesent", "pretium", "pri", "primis", "principes",
			"pro", "prodesset", "proin", "prompta", "propriae", "pulvinar", "purus", "putent", "quaeque", "quaerendum",
			"quaestio", "qualisque", "quam", "quas", "quem", "qui", "quidam", "quis", "quisque", "quo", "quod", "quot",
			"recteque", "referrentur", "reformidans", "regione", "reprehendunt", "reprimique", "repudiandae",
			"repudiare", "reque", "rhoncus", "ridens", "ridiculus", "risus", "rutrum", "sadipscing", "saepe",
			"sagittis", "sale", "salutatus", "sanctus", "saperet", "sapien", "sapientem", "scelerisque", "scripserit",
			"scripta", "sea", "sed", "sem", "semper", "senectus", "senserit", "sententiae", "signiferumque",
			"similique", "simul", "singulis", "sit", "sociis", "sociosqu", "sodales", "solet", "sollicitudin", "solum",
			"sonet", "splendide", "suas", "suavitate", "sumo", "suscipiantur", "suscipit", "suspendisse", "tacimates",
			"taciti", "tale", "tamquam", "tantas", "tation", "te", "tellus", "tempor", "tempus", "theophrastus",
			"tibique", "tincidunt", "torquent", "tortor", "tota", "tractatos", "tristique", "tritani", "turpis",
			"ubique", "ullamcorper", "ultrices", "ultricies", "unum", "urbanitas", "urna", "usu", "ut", "utamur",
			"utinam", "utroque", "varius", "vehicula", "vel", "velit", "venenatis", "veniam", "verear", "veri",
			"veritus", "vero", "verterem", "vestibulum", "viderer", "vidisse", "vim", "viris", "vis", "vitae",
			"vituperata", "vituperatoribus", "vivamus", "vivendo", "viverra", "vix", "vocent", "vocibus", "volumus",
			"voluptaria", "voluptatibus", "voluptatum", "volutpat", "vulputate", "wisi" };
}
