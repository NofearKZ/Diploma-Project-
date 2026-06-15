package com.example.utils

object TextSplitter {
    private val vowels = setOf(
        'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я',
        'ә', 'і', 'ө', 'ұ', 'ү'
    )

    fun splitIntoSyllables(word: String): String {
        if (word.length <= 3) return word
        
        val builder = java.lang.StringBuilder()
        val chars = word.toCharArray()
        var vowelSeen = false

        for (i in chars.indices) {
            val c = chars[i].lowercaseChar()
            val isVowel = vowels.contains(c)
            builder.append(chars[i])
            
            if (isVowel) {
                vowelSeen = true
                val remainingChars = chars.drop(i + 1)
                val hasMoreVowels = remainingChars.any { vowels.contains(it.lowercaseChar()) }
                if (hasMoreVowels) {
                     if (i + 1 < chars.size && i + 2 < chars.size) {
                         val c1 = chars[i+1].lowercaseChar()
                         val c2 = chars[i+2].lowercaseChar()
                         if (!vowels.contains(c1) && vowels.contains(c2)) {
                             // consonant - vowel pair ahead -> split
                             builder.append("-")
                             vowelSeen = false
                         } else if (vowels.contains(c1)) {
                             // vowel - vowel -> split
                             builder.append("-")
                             vowelSeen = false
                         }
                     }
                }
            } else if (vowelSeen) {
                 val remainingChars = chars.drop(i + 1)
                 val hasMoreVowels = remainingChars.any { vowels.contains(it.lowercaseChar()) }
                 if (hasMoreVowels) {
                     if (i + 1 < chars.size) {
                         val c1 = chars[i+1].lowercaseChar()
                         if (!vowels.contains(c1)) {
                             builder.append("-")
                             vowelSeen = false
                         }
                     }
                 }
            }
        }
        return builder.toString()
    }
}
