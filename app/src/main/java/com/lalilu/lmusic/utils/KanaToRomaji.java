package com.lalilu.lmusic.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copy from https://github.com/nicolas-raoul/jakaroma
 * time: 2022/01/02
 */
public class KanaToRomaji {

    private static final Map<String, String> dictionary;

    static {
        Map<String, String> aMap = new HashMap<>();

        aMap.put("ア", "a");
        aMap.put("あ", "a");
        aMap.put("イ", "i");
        aMap.put("い", "i");
        aMap.put("ウ", "u");
        aMap.put("う", "u");
        aMap.put("エ", "e");
        aMap.put("え", "e");
        aMap.put("オ", "o");
        aMap.put("お", "o");
        aMap.put("カ", "ka");
        aMap.put("か", "ka");
        aMap.put("キ", "ki");
        aMap.put("き", "ki");
        aMap.put("ク", "ku");
        aMap.put("く", "ku");
        aMap.put("ケ", "ke");
        aMap.put("け", "ke");
        aMap.put("コ", "ko");
        aMap.put("こ", "ke");
        aMap.put("サ", "sa");
        aMap.put("さ", "sa");
        aMap.put("シ", "shi");
        aMap.put("し", "shi");
        aMap.put("ス", "su");
        aMap.put("す", "su");
        aMap.put("セ", "se");
        aMap.put("せ", "se");
        aMap.put("ソ", "so");
        aMap.put("そ", "so");
        aMap.put("タ", "ta");
        aMap.put("た", "ta");
        aMap.put("チ", "chi");
        aMap.put("ち", "chi");
        aMap.put("ツ", "tsu");
        aMap.put("つ", "tsu");
        aMap.put("テ", "te");
        aMap.put("て", "te");
        aMap.put("ト", "to");
        aMap.put("と", "to");
        aMap.put("ナ", "na");
        aMap.put("な", "na");
        aMap.put("ニ", "ni");
        aMap.put("に", "ni");
        aMap.put("ヌ", "nu");
        aMap.put("ぬ", "nu");
        aMap.put("ネ", "ne");
        aMap.put("ね", "ne");
        aMap.put("ノ", "no");
        aMap.put("の", "no");
        aMap.put("ハ", "ha");
        aMap.put("は", "ha");
        aMap.put("ヒ", "hi");
        aMap.put("ひ", "hi");
        aMap.put("フ", "fu");
        aMap.put("ふ", "fu");
        aMap.put("ヘ", "he");
        aMap.put("へ", "he");
        aMap.put("ホ", "ho");
        aMap.put("ほ", "ho");
        aMap.put("マ", "ma");
        aMap.put("ま", "ma");
        aMap.put("ミ", "mi");
        aMap.put("み", "mi");
        aMap.put("ム", "mu");
        aMap.put("む", "mu");
        aMap.put("メ", "me");
        aMap.put("め", "me");
        aMap.put("モ", "mo");
        aMap.put("も", "mo");
        aMap.put("ヤ", "ya");
        aMap.put("や", "ya");
        aMap.put("ユ", "yu");
        aMap.put("ゆ", "yu");
        aMap.put("ヨ", "yo");
        aMap.put("よ", "yo");
        aMap.put("ラ", "ra");
        aMap.put("ら", "ra");
        aMap.put("リ", "ri");
        aMap.put("り", "ri");
        aMap.put("ル", "ru");
        aMap.put("る", "ru");
        aMap.put("レ", "re");
        aMap.put("れ", "re");
        aMap.put("ロ", "ro");
        aMap.put("ろ", "ro");
        aMap.put("ワ", "wa");
        aMap.put("わ", "wa");
        aMap.put("ヲ", "wo");
        aMap.put("を", "wo");
        aMap.put("ン", "n");
        aMap.put("ん", "n");
        aMap.put("ガ", "ga");
        aMap.put("が", "ga");
        aMap.put("ギ", "gi");
        aMap.put("ぎ", "gi");
        aMap.put("グ", "gu");
        aMap.put("ぐ", "gu");
        aMap.put("ゲ", "ge");
        aMap.put("げ", "ge");
        aMap.put("ゴ", "go");
        aMap.put("ご", "go");
        aMap.put("ザ", "za");
        aMap.put("ざ", "za");
        aMap.put("ジ", "ji");
        aMap.put("じ", "ji");
        aMap.put("ズ", "zu");
        aMap.put("ず", "zu");
        aMap.put("ゼ", "ze");
        aMap.put("ぜ", "ze");
        aMap.put("ゾ", "zo");
        aMap.put("ぞ", "zo");
        aMap.put("ダ", "da");
        aMap.put("だ", "da");
        aMap.put("ヂ", "ji");
        aMap.put("ヅ", "zu");
        aMap.put("づ", "zu");
        aMap.put("デ", "de");
        aMap.put("で", "de");
        aMap.put("ド", "do");
        aMap.put("ど", "do");
        aMap.put("バ", "ba");
        aMap.put("ば", "ba");
        aMap.put("ビ", "bi");
        aMap.put("び", "bi");
        aMap.put("ブ", "bu");
        aMap.put("ぶ", "bu");
        aMap.put("ベ", "be");
        aMap.put("べ", "be");
        aMap.put("ボ", "bo");
        aMap.put("ぼ", "bo");
        aMap.put("パ", "pa");
        aMap.put("ぱ", "pa");
        aMap.put("ピ", "pi");
        aMap.put("ぴ", "pi");
        aMap.put("プ", "pu");
        aMap.put("ぷ", "pu");
        aMap.put("ペ", "pe");
        aMap.put("ぺ", "pe");
        aMap.put("ポ", "po");
        aMap.put("ぽ", "po");
        aMap.put("キャ", "kya");
        aMap.put("きゃ", "kya");
        aMap.put("キュ", "kyu");
        aMap.put("きゅ", "kyu");
        aMap.put("キョ", "kyo");
        aMap.put("きょ", "kyo");
        aMap.put("シャ", "sha");
        aMap.put("しゃ", "sha");
        aMap.put("シュ", "shu");
        aMap.put("しゅ", "shu");
        aMap.put("ショ", "sho");
        aMap.put("しょ", "sho");
        aMap.put("チャ", "cha");
        aMap.put("ちゃ", "cha");
        aMap.put("チュ", "chu");
        aMap.put("ちゅ", "chu");
        aMap.put("チョ", "cho");
        aMap.put("ちょ", "cho");
        aMap.put("ニャ", "nya");
        aMap.put("にゃ", "nya");
        aMap.put("ニュ", "nyu");
        aMap.put("にゅ", "nyu");
        aMap.put("ニョ", "nyo");
        aMap.put("にょ", "nyo");
        aMap.put("ヒャ", "hya");
        aMap.put("ひゃ", "hya");
        aMap.put("ヒュ", "hyu");
        aMap.put("ひゅ", "hyu");
        aMap.put("ヒョ", "hyo");
        aMap.put("ひょ", "hyo");
        aMap.put("リャ", "rya");
        aMap.put("りゃ", "rya");
        aMap.put("リュ", "ryu");
        aMap.put("りゅ", "ryu");
        aMap.put("リョ", "ryo");
        aMap.put("りょ", "ryo");
        aMap.put("ギャ", "gya");
        aMap.put("ぎゃ", "gya");
        aMap.put("ギュ", "gyu");
        aMap.put("ぎゅ", "gyu");
        aMap.put("ギョ", "gyo");
        aMap.put("ぎょ", "gyo");
        aMap.put("ジャ", "ja");
        aMap.put("じゃ", "ja");
        aMap.put("ジュ", "ju");
        aMap.put("じゅ", "ju");
        aMap.put("ジョ", "jo");
        aMap.put("じょ", "jo");
        aMap.put("ティ", "ti");
        aMap.put("ディ", "di");
        aMap.put("ツィ", "tsi");
        aMap.put("つぃ", "tsi");
        aMap.put("ヂャ", "dya");
        aMap.put("ぢゃ", "dya");
        aMap.put("ヂュ", "dyu");
        aMap.put("ぢゅ", "dyu");
        aMap.put("ヂョ", "dyo");
        aMap.put("ぢょ", "dyo");
        aMap.put("ビャ", "bya");
        aMap.put("びゃ", "bya");
        aMap.put("ビュ", "byu");
        aMap.put("びゅ", "byu");
        aMap.put("ビョ", "byo");
        aMap.put("びょ", "byo");
        aMap.put("ピャ", "pya");
        aMap.put("ぴゃ", "pya");
        aMap.put("ピュ", "pyu");
        aMap.put("ぴゅ", "pyu");
        aMap.put("ピョ", "pyo");
        aMap.put("ぴょ", "pyo");
        aMap.put("ー", "-");
        aMap.put("チェ", "che");
        aMap.put("ちぇ", "che");
        aMap.put("フィ", "fi");
        aMap.put("ふぃ", "fi");
        aMap.put("フェ", "fe");
        aMap.put("ふぇ", "fe");
        aMap.put("ウィ", "wi");
        aMap.put("うぃ", "wi");
        aMap.put("ウェ", "we");
        aMap.put("うぇ", "we");
        aMap.put("ヴィ", "vi");
        aMap.put("ヴぃ", "vi");
        aMap.put("ヴェ", "ve");
        aMap.put("ヴぇ", "ve");

        aMap.put("「", "\"");
        aMap.put("」", "\"");
        aMap.put("。", ".");

        dictionary = Collections.unmodifiableMap(aMap);
    }

    public static Map<String, String> getDictionary() {
        return dictionary;
    }

    public String convert(String s) {
        StringBuilder t = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i <= s.length() - 2) {
                if (dictionary.containsKey(s.substring(i, i + 2))) {
                    t.append(dictionary.get(s.substring(i, i + 2)));
                    i++;
                } else if (dictionary.containsKey(s.substring(i, i + 1))) {
                    t.append(dictionary.get(s.substring(i, i + 1)));
                }
//                else if (s.charAt(i) == 'ッ') {
//                    t.append(dictionary.get(s.substring(i + 1, i + 2)).charAt(0));
//                } else {
//                    t.append(s.charAt(i));
//                }
            } else {
                if (dictionary.containsKey(s.substring(i, i + 1))) {
                    t.append(dictionary.get(s.substring(i, i + 1)));
                } else {
                    t.append(s.charAt(i));
                }
            }
        }
        return t.toString();
    }

}