EmojiHelper
=============

EmojiHelper is a simple script to help solve [EmojiNation](https://play.google.com/store/apps/details?id=com.digitalclick.emojination&hl=en)'s different puzzles. Given a pattern and the available letters, it'll figure out all possible word combinations that can be created. It requires a wordlist / dictionary file in order to do so.

&nbsp;

How to use
-------------

Compile: ````javac EmojiHelper.java````

Run: ````java EmojiHelper "<pattern>" "<letters>"````

**Note:** EmojiHelper is case sensitive. Wordlists, hints in patterns and the letters should be supplied in lowercase.

&nbsp;

#### Examples

Without hints: ````java EmojiHelper "______ ________" "ccnaelaodetnte"````

With hints: ````java EmojiHelper "c_____ _____nt_" "cnaelaodete"````

#### Alternatives

Without hints: ````java EmojiHelper "...... ........" "ccnaelaodetnte"````

With hints: ````java EmojiHelper "c..... .....nt." "cnaelaodete"````

&nbsp;

Arguments
-------------

By default, this script will load the wordlist from a ````
dictionary.txt```` file contained in the same directory, and expects the first two arguments to be ````pattern```` and ````letters```` in that order. Alternatively, they can be supplied using the ````-p```` and ````-l```` arguments.

#### Full list of arguments

````-p or -pattern "c....."```` - Word or sentence pattern

````-l or -letters "daeal"```` - Available letters to use on the pattern

````-d or -dictionary dictionary.txt ```` - Dictionary file to be used. (Optional) 

````-s or -save results.txt```` - File where to save results. This argument will overrite existing files. (Optional)

````-q or -quiet```` - Quiet mode. No solutions will be printed to the console or terminal. It should be used along with the ````-save```` argument. (Optional)


#### Example

````java EmojiHelper "c..... .....nt." "cnaelaodete" -d dictionary.txt -s results.txt -q````

&nbsp;

How it works
-------------
EmojiHelper uses Regex against a wordlist (````dictionary.txt````) in order to figure out what dictionary words can be created using the letters supplied (including hinted letters), up to the desired length. It then runs a check to make sure matching words don't contain more instances of a letter than the ones supplied. Finally, if the pattern contained hinted letters, it'll run another Regex check to find out if the found word matches the hint.

All the heavy lifting is done by the ````solver```` method, which is a recursive one. Upon finding a match in the wordlist, it'll subtract the letters used for the matching word and call itself in order to figure out the next word in the pattern. 

#### Dictionary optimization
In order to cut on processing time, the words list is split into different lists by word length, after loading the wordlist file into memory. This way the ````solver```` method only has to go through the list that only contains of the desired length, instead of the whole wordlist.

&nbsp;

Where to get wordlists
-------------
Wordlists can be found online. In a pinch, one could download an OpenOffice dictionary extension from the [OpenOffice's Extensions website](http://extensions.openoffice.org/) and clean it up to a proper format using the [Dic Cleaner](https://github.com/SARodrigues/DicCleaner/) script. Either way, it would be a good idea to use it on any wordlist found online, to make sure words are trimmed properly. 

The wordlist and examples found in this repository are for the European Portuguese language. The wordlist was downloaded from [University of Minho](http://www.uminho.pt/en)'s [Projecto Natura's website](http://natura.di.uminho.pt/). 

&nbsp;

Notes
-------------
1. Obviously, if the words needed to solve the puzzle aren't in the dictionary file, the correct solution will not be found. 

2. This script has only been tested with English and European Portuguese languages, I'm not sure how it'll behave with other languages. 

3. This is a simple script I've created for fun. As such, it is provided as is. 

