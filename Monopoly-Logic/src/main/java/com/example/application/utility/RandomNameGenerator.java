package com.example.application.utility;

import java.util.Random;

public class RandomNameGenerator {

    private static final String[] ADJECTIVES = {
            "admiring", "adoring", "affectionate", "agitated", "amazing",
            "angry", "awesome", "blissful", "boring", "brave", "clever",
            "cool", "compassionate", "competent", "confident", "cranky",
            "crazy", "dazzling", "determined", "distracted", "dreamy",
            "eager", "ecstatic", "elastic", "elated", "elegant", "epic",
            "fervent", "festive", "flamboyant", "focused", "friendly",
            "frosty", "gallant", "gifted", "goofy", "gracious", "great",
            "happy", "hardcore", "heuristic", "hopeful", "hungry", "infallible",
            "inspiring", "jolly", "jovial", "keen", "kind", "laughing",
            "loving", "lucid", "mystifying", "modest", "musing", "naughty",
            "nervous", "nice", "nifty", "nostalgic", "objective", "optimistic",
            "peaceful", "pedantic", "pensive", "practical", "priceless",
            "quirky", "quizzical", "recursing", "relaxed", "reverent", "romantic",
            "sad", "serene", "sharp", "silly", "sleepy", "stoic", "stupefied",
            "suspicious", "sweet", "tender", "thirsty", "trusting", "unruffled",
            "upbeat", "vibrant", "vigilant", "vigorous", "wizardly", "wonderful",
            "xenodochial", "youthful", "zealous", "zen"
    };

    private static final String[] NAMES = {
            "albattani", "allen", "almeida", "agnesi", "archimedes", "ardinghelli", "aryabhata",
            "austin", "babbage", "banach", "banzai", "bardeen", "bartik", "bassi", "beaver",
            "bell", "benz", "bhabha", "bhaskara", "blackwell", "bohr", "booth", "borg",
            "bose", "boyd", "brahmagupta", "brattain", "brown", "buck", "burnell", "cannon",
            "carson", "cartwright", "cerf", "chandrasekhar", "shannon", "clarke", "colden",
            "cori", "cray", "curie", "darwin", "davinci", "dijkstra", "dubinsky", "easley",
            "edison", "einstein", "elion", "engelbart", "euclid", "euler", "fermat", "fermi",
            "feynman", "franklin", "galileo", "gates", "goldberg", "goldstine", "goldwasser",
            "golick", "goodall", "haibt", "hamilton", "hawking", "heisenberg", "hermann",
            "herschel", "hertz", "heyrovsky", "hodgkin", "hoover", "hopper", "hugle",
            "hypatia", "ishizaka", "jackson", "jang", "jennings", "jepsen", "johnson",
            "joliot", "jones", "kalam", "kare", "keldysh", "keller", "kepler", "khayyam",
            "khorana", "kilby", "kirch", "knuth", "kowalevski", "lalande", "lamarr", "lamé",
            "leakey", "leavitt", "lederberg", "lehmann", "lewin", "lichterman", "liskov",
            "lovelace", "lumiere", "mahavira", "margulis", "matsumoto", "maxwell", "mayer",
            "mccarthy", "mcclintock", "mclaren", "mclean", "mcnulty", "meitner", "meninsky",
            "mestorf", "minsky", "mirzakhani", "moore", "morse", "murdock", "newton", "nightingale",
            "nobel", "noether", "northcutt", "noyce", "panini", "pare", "pasteur", "payne",
            "perlman", "pike", "poincare", "poitras", "ptolemy", "raman", "ramanujan", "ride",
            "ritchie", "rhodes", "roentgen", "rosalind", "saha", "sammet", "shaw", "shirley",
            "shockley", "shtern", "sinoussi", "snyder", "spence", "stallman", "stonebraker",
            "swanson", "swartz", "swirles", "taussig", "tereshkova", "tesla", "tharp", "thompson",
            "torvalds", "tu", "turing", "varahamihira", "visvesvaraya", "volhard", "villani",
            "wescoff", "wilbur", "wiles", "williams", "wilson", "wing", "wozniak", "wright",
            "wu", "yalow", "yonath"
    };

    private static final Random RANDOM = new Random();

    public static String generateName() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String name = NAMES[RANDOM.nextInt(NAMES.length)];
        return adjective + "_" + name;
    }

}
