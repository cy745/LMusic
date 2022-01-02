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
        aMap.put("ク", "ku");
        aMap.put("ケ", "ke");
        aMap.put("コ", "ko");
        aMap.put("サ", "sa");
        aMap.put("シ", "shi");
        aMap.put("ス", "su");
        aMap.put("セ", "se");
        aMap.put("ソ", "so");
        aMap.put("タ", "ta");
        aMap.put("チ", "chi");
        aMap.put("ツ", "tsu");
        aMap.put("テ", "te");
        aMap.put("ト", "to");
        aMap.put("ナ", "na");
        aMap.put("ニ", "ni");
        aMap.put("ヌ", "nu");
        aMap.put("ネ", "ne");
        aMap.put("ノ", "no");
        aMap.put("ハ", "ha");
        aMap.put("ヒ", "hi");
        aMap.put("フ", "fu");
        aMap.put("ヘ", "he");
        aMap.put("ホ", "ho");
        aMap.put("マ", "ma");
        aMap.put("ミ", "mi");
        aMap.put("ム", "mu");
        aMap.put("メ", "me");
        aMap.put("モ", "mo");
        aMap.put("ヤ", "ya");
        aMap.put("ユ", "yu");
        aMap.put("ヨ", "yo");
        aMap.put("ラ", "ra");
        aMap.put("リ", "ri");
        aMap.put("ル", "ru");
        aMap.put("レ", "re");
        aMap.put("ロ", "ro");
        aMap.put("ワ", "wa");
        aMap.put("ヲ", "wo");
        aMap.put("ン", "n");
        aMap.put("ガ", "ga");
        aMap.put("ギ", "gi");
        aMap.put("グ", "gu");
        aMap.put("ゲ", "ge");
        aMap.put("ゴ", "go");
        aMap.put("ザ", "za");
        aMap.put("ジ", "ji");
        aMap.put("ズ", "zu");
        aMap.put("ゼ", "ze");
        aMap.put("ゾ", "zo");
        aMap.put("ダ", "da");
        aMap.put("ヂ", "ji");
        aMap.put("ヅ", "zu");
        aMap.put("デ", "de");
        aMap.put("ド", "do");
        aMap.put("バ", "ba");
        aMap.put("ビ", "bi");
        aMap.put("ブ", "bu");
        aMap.put("ベ", "be");
        aMap.put("ボ", "bo");
        aMap.put("パ", "pa");
        aMap.put("ピ", "pi");
        aMap.put("プ", "pu");
        aMap.put("ペ", "pe");
        aMap.put("ポ", "po");
        aMap.put("キャ", "kya");
        aMap.put("キュ", "kyu");
        aMap.put("キョ", "kyo");
        aMap.put("シャ", "sha");
        aMap.put("シュ", "shu");
        aMap.put("ショ", "sho");
        aMap.put("チャ", "cha");
        aMap.put("チュ", "chu");
        aMap.put("チョ", "cho");
        aMap.put("ニャ", "nya");
        aMap.put("ニュ", "nyu");
        aMap.put("ニョ", "nyo");
        aMap.put("ヒャ", "hya");
        aMap.put("ヒュ", "hyu");
        aMap.put("ヒョ", "hyo");
        aMap.put("リャ", "rya");
        aMap.put("リュ", "ryu");
        aMap.put("リョ", "ryo");
        aMap.put("ギャ", "gya");
        aMap.put("ギュ", "gyu");
        aMap.put("ギョ", "gyo");
        aMap.put("ジャ", "ja");
        aMap.put("ジュ", "ju");
        aMap.put("ジョ", "jo");
        aMap.put("ティ", "ti");
        aMap.put("ディ", "di");
        aMap.put("ツィ", "tsi");
        aMap.put("ヂャ", "dya");
        aMap.put("ヂュ", "dyu");
        aMap.put("ヂョ", "dyo");
        aMap.put("ビャ", "bya");
        aMap.put("ビュ", "byu");
        aMap.put("ビョ", "byo");
        aMap.put("ピャ", "pya");
        aMap.put("ピュ", "pyu");
        aMap.put("ピョ", "pyo");
        aMap.put("ー", "-");
        aMap.put("チェ", "che");
        aMap.put("フィ", "fi");
        aMap.put("フェ", "fe");
        aMap.put("ウィ", "wi");
        aMap.put("ウェ", "we");
        aMap.put("ヴィ", "ⅴi");
        aMap.put("ヴェ", "ve");

        aMap.put("「", "\"");
        aMap.put("」", "\"");
        aMap.put("。", ".");

        dictionary = Collections.unmodifiableMap(aMap);
    }

    public String convert(String s) {
        StringBuilder t = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i <= s.length() - 2) {
                System.out.println(s.substring(i, i + 2) + ": " + dictionary.get(s.substring(i, i + 2)));

                if (dictionary.containsKey(s.substring(i, i + 2))) {
                    t.append(dictionary.get(s.substring(i, i + 2)));
                    i++;
                } else if (dictionary.containsKey(s.substring(i, i + 1))) {
                    t.append(dictionary.get(s.substring(i, i + 1)));
                } else if (s.charAt(i) == 'ッ') {
                    t.append(dictionary.get(s.substring(i + 1, i + 2)).charAt(0));
                } else {
                    t.append(s.charAt(i));
                }
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

    public static Map<String, String> getDictionary() {
        return dictionary;
    }

}