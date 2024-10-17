# Project 4: Autocomplete

## Project Introduction	

This project uses the autocomplete algorithm, where the user types text and the application suggests possible completions for that text. Although finding terms that contain a query by searching through all possible results is possible, these applications need some way to select only the most useful terms to display. Thus, the algorithm not only needs a way to find terms that start with or contain the prefix, but a way of determining how likely each one is to be useful to the user and displaying "good" terms first. This all needs to be done efficiently so that a user can see completions in real time. We leverage a `Comparator` in Java as well as the binary search algorithm on sorted data to implement an efficient autocompleter, then benchmark and analyze the tradeoffs of these implementations.  

<details>
<summary>Expand for examples</summary>
The left/first was taken in March 2019, the right/second on October 9, 2020)
<div align="center">
  <img width="384" height="344 "src="p4-figures/googleSearch.png">
  <img width="384" height="345" src="p4-figures/googleSearch2.png">
</div>

</details>

This project was developed by Kevin Wayne and Matthew Drabick at Princeton University for their Computer Science 226 class. Former head CompSci 201 UTAs, Arun Ganesh (Trinity '17) and Austin Lu (Trinity '15) adapted the project for Duke with help from Jeff Forbes. Josh Hug updated the assignment and provided more of the testing framework.

</details>

## Part 1: Run Autocomplete Main

Running the `main` method of `AutocompleteMain` launches a GUI that allows the user to select a data file. The data file will determine the set of possible words to be recommended by the autocompleter application, and also includes weights for how common the words are. Several such files are included along with this project. When prompted to enter a term, the most common words that complete what the user has typed so far appear. For example, running `AutocompleteMain` and selecting the file `words-333333.txt` from the data folder leads to the output below shown in the GUI window for the search query **auto**:

<details>
<summary>Expand for example of program running</summary>
<div align="center">
  <img src="p4-figures/astrachanSearch.png">
</div>
</details>

## Part 2: Implement the `compare` method in `PrefixComparator`

A `PrefixComparator` object is obtained by calling `PrefixComparator.getComparator` with an integer argument `r`, the size of the prefix for comparison purposes. The value is stored in the instance variable `myPrefixSize`.

Only the first `myPrefixSize` characters of the words stored in `Term` objects `v` and `w` are passed to `PrefixComparator.compare`. However, if the length of either word is less than `myPrefixSize`, this comparator only compares up ***until the end of the shorter word.***

<details>
<summary>More details on PrefixComparator</summary>

For a `PrefixComparator.getComparator(4)`, `"beeswax"` is greater than `"beekeeper"` since `"bees"` is greater than `"beek"`. But `"bee"` is less than `"beekeeper"` and `"beeswax"` since only the first three characters are compared --- since `"bee"` has only three characters and these three characters are the same. ***The length*** of `"bee"` ***makes it less than*** `"beekeeper"`, just as it is when eight characters are used to compare these words.

This code examines a minimal number of characters as needed using a loop and calling `.charAt` to examine characters. Reference table for the `PrefixComparator` comparator:

|r/prefix|v| |w| Note |
|    -   |-|-|-| -    |
|   4  |bee|<|beekeeper|"bee" < "beek"|
|4|bees|>|beek|‘s’ > ‘k’|
|4|bug|>|beeswax|‘u’ > ‘e’|
|4|bees|=|beeswax|"bees" == "bees"|
|3|beekeeper|=|beeswax|"bee" == "bee"|
|3|bee|=|beeswax|"bee" == "bee"|


</details>

## Part 3: Implement `BinarySearchLibrary`

The class `BinarySearchLibrary` stores static utility methods used in the implementation of the `BinarySearchAutocomplete` class. Two methods are implemented in particular: `firstIndex` and `lastIndex`. `BinarySearchAutocomplete` will use these methods along with the `PrefixComparator` to efficiently determine the *range of possible completions of a given prefix of a word typed so far*.

<details>
<summary>Expand for details on implementing firstIndex and lastIndex</summary>

The code in `BinarySearchLibrary.firstIndexSlow` is a slow method with **O(*N*)** where there are *N* equal values since the code could examine all the values. To meet performance criteria, this code should be changed to **O(log *N*)**, more specifically it should only need $`1 + \lceil log_2N \rceil`$ comparisons -- that is, one more than $`log_2N`$ rounded up.

```java
public static <T> int binarySearch(List<T> list, T target,
                                   Comparator<T> comp) {
    int low = 0;
    int high = list.size()-1;
    while (low <= high) {
        int mid = (low + high)/2;
        T midval = list.get(mid);
        int cmp = comp.compare(midval,target);

        if (cmp < 0)
            low = mid + 1;
        else if (cmp > 0)
            high = mid - 1;
        else
            return mid; // target found
     }
     return -1;  // target not found
}
```

This method returns an index `i` such that `comp.compare(list.get(i), target)==0` but does *not* guarantee to return the first or last such index `i`. We aim to adapt this approach so that `firstIndex` and `lastIndex` return the first and last such indices respectively, while maintaining the same performance guarantee. To do so, we added a `foundAt` variable and maintained the following **loop invariant** that should be true at the start of every iteration of the `while` loop, where [`low`, `high`] denote the integer values from `low` to `high`, inclusive. The invariant is:

1. `foundAt` should be the *least* (for `firstIndex`) or *greatest* (for `lastIndex`) index outside of [`low`, `high`] containting target (or -1 if there are none).
2. All indices containing target *less than* `foundAt` (for `firstIndex`) or *greater than* `foundAt` (for `lastIndex`) should be inside of [`low`, `high`].

This invariant is initially established by setting `low = 0`, `high = list.size()-1`, and `foundAt = -1`. If it is maintained until `low > high`, then we can `return foundAt` to complete the method.

</details>

<details>
<summary>Expand for example output of BinaryBenchmark</summary>

The values in both `index` columns should be the same: the location of the first occurrence of the prefix shown. The `cslow` column is the number of comparisons made by the slow implementation `firstIndexSlow`. The `cfast` column is the number of comparisons made by `firstIndex`.

```
size of list = 26000
Prefix index    index	  cslow   cfast

aaa	     0	      0	   817	15
fff	  5000	   5000	   693	16
kkk	 10000    10000	   568	16
ppp	 15000    15000	   443	16
uuu	 20000    20000	   318	15
zzz	 25000    25000	   194	16
```
</details>

## Part 4: Finish Implementing `topMatches` in `BinarySearchAutocomplete`

We now implement code for `topMatches` in the `BinarySearchAutocomplete` class -- a method required in the `Autocompletor` interface.

<details>
<summary>Expand for details on topMatches</summary>

Code in static methods `firstIndexOf` and `lastIndexOf` is written to use the API exported by `BinarySearchLibrary`. `Term[]` parameter to these methods is transformed to a `List<Term>` since that's the type of parameter that must be passed to `BinarySearchLibrary` methods. The `Term` object called `dummy` created from the `String` is passed to `topMatches`. The weight for the `Term` doesn't matter since only the `String` field of the `Term` is used in `firstIndex` and `lastIndex` calls.

</details>

The `topMatches` method requires that the weightiest `k` matches that match `prefix` that's a parameter to `topMatches` is returned, in order of weight. The calls to `firstIndex` and `lastIndex` give the first and last indices of `myTerms` that match. Our code returns the `k` greatest `weight` of these in order. If there are fewer than `k` matches, it returns all of the matches in order.

<details>
<summary>Expand for details on efficient implementation of topMatches</summary> 

The binary search in the `firstIndex` and `lastIndex` methods are both `O(log N)`. Then, if there are `M` terms that match the prefix, then the simple method of finding the `M` matches, copying them to a list, sorting them in reverse weight order, and then choosing the first `k` of them will run in the total of the times given below. Using this approach will thus have complexity/performance of `O(log N + M log M)`. 

|Complexity|Reason|
| ---      |  ---  |
|O(log N)|Call firstIndex and lastIndex|
|O(M log(M))|Sort all M elements that match prefix|
|O(k)|Return list of top k matches|

It's quite possible that `k < M`, and often `k` will be *much* less than `M`. Rather than sorting all `M` entries that match the prefix, we use a size-limited priority queue that makes `topMatches` run in `O(log N + M log k)` time instead of `O(log N + M log M)`.

|Complexity|Reason|
| ---      |  ---  |
|O(log N)|Call firstIndex and lastIndex|
|O(M log(k))|Keep best k elements in priority queue|
|O(k log(k))|Return list of top k matches, removing one at a time from priority queue|

</details>

```java
final static String AUTOCOMPLETOR_CLASS_NAME = BRUTE_AUTOCOMPLETE;
//final static String AUTOCOMPLETOR_CLASS_NAME = BINARY_SEARCH_AUTOCOMPLETE;
//final static String AUTOCOMPLETOR_CLASS_NAME = HASHLIST_AUTOCOMPLETE;
```

## Part 5: Implement `HashListAutocomplete`

This third implementation is based on the use of a `HashMap` instead of the binary search algorithm. This class will provide an `O(1)` implementation of `topMatches` --- with a tradeoff of requiring more memory.

```java
public class HashListAutocomplete implements Autocompletor {

    private static final int MAX_PREFIX = 10;
    private Map<String, List<Term>> myMap;
    private int mySize;
}
```

The class maintains a `HashMap` of _every possible prefix_ (for each term) (up to the number of characters specified by a constant `MAX_PREFIX` that you should set to 10 as shown. The key in the map is a prefix/substring. The value for each prefix key is a weight-sorted list of `Term` objects that share that prefix. The diagram below shows part of such a `HashMap`. Three prefixes are shown---the corresponding values are shown as a weight-sorted list of `Term` objects.

|Prefix|Term Objects|
| --   |    ----    |
|"ch"| ("chat",50), ("chomp",40), ("champ",30), ("chocolate",10)|
|"cho"|("chomp",40), ("chocolate",10)|
|"cha | ("chat",50), ("champ", 30)|

Details on the four methods:

<details>
<summary>Expand for details on the constructor</summary>

This constructor calls checks for invalid conditions and throws exceptions in those cases, otherwise it simply calls the `initialize()` method passing `terms` and `weights`.
</details>

<details>
<summary>Expand for details on the initialize method</summary>

For each `Term` in `initialize`, we use the first `MAX_PREFIX` substrings as a key in the map the class maintains and uses. For each prefix, we store the `Term` objects with that prefix in an `ArrayList` that is the corresponding value for the prefix in the map.

***After*** all keys and values have been entered into the map, we sort every value in the map, that is each `ArrayList` corresponding to each prefix. We use a `Comparator.comparing(Term::getWeight).reversed()` object to sort so that the list is maintained sorted from high to low by weight. We update mySize in creating this map. mySize is a rough estimate of the number of bytes required to create the HashMap (both the String keys and the Term values in the HashMap). For each string you create, we add on `BYTES_PER_CHAR * length` to the number of bytes needed. For each term you store, each string stored contributes `BYTES_PER_CHAR * length` and each double stored contributes `BYTES_PER_DOUBLE`.

```java
Collections.sort(list, Comparator.comparing(Term::getWeight).reversed())`
```

Example: if we initialize HashListAutocomplete with one Term ("hippopotamus", 40), then my HashMap should be:

|Prefix|Term Objects|
| --   |    ----    |
|""| ("hippopotamus", 40)|
|"h"| ("hippopotamus", 40)|
|"hi"| ("hippopotamus", 40)|
|"hip"| ("hippopotamus", 40)|
|"hipp"| ("hippopotamus", 40)|
|"hippo"| ("hippopotamus", 40)|
|"hippop"| ("hippopotamus", 40)|
|"hippopo"| ("hippopotamus", 40)|
|"hippopot"| ("hippopotamus", 40)|
|"hippopota"| ("hippopotamus", 40)|
|"hippopotam"| ("hippopotamus", 40)|

We stop at "hippopotam" since we take prefixes up to `MAX_PREFIX` length.

</details>

<details>
<summary>Expand for details on the topMatches method</summary>

To implement `topMatches`, we first check that the `prefix` parameter has at most `MAX_PREFIX` characters, otherwise shorten it by truncating the trailing characters to `MAX_PREFIX` length. Then, if `prefix` is in the map, we get the corresponding value (a `List` of `Term` objects) and return a sublist of the first `k` entries (or all of the entries if there are fewer than `k`). Otherwise, an empty list is returned.

```java
List<Term> all = myMap.get(prefix);
List<Term> list = all.subList(0, Math.min(k, all.size()));
```

</details>

<details>
<summary>Expand for details on the sizeInBytes method</summary>

The `sizeInBytes` method returns an estimate of the amount of memory (in bytes) necessary to store all of the keys and values in the `HashMap`. This can be computed once the first time `sizeInBytes` is called (that is, when `mySize == 0`) and stored in the instance variable `mySize`; on subsequent calls it can just return `mySize`.

This method accounts for every `Term` object and every String/key in the map. Each string stored contributes `BYTES_PER_CHAR * length` to the bytes need. Each double stored contributes `BYTES_PER_DOUBLE`. We account for every `Term` stored in one of the lists in the map (each consisting of a String and a double) as well as every key (Strings) in the map.

</details>

Coursework from Duke CS 201: Data Structures and Algorithms.
