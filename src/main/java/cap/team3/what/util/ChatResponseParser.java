package cap.team3.what.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cap.team3.what.dto.VectorMetaData;

public class ChatResponseParser {
    public static VectorMetaData parseChatResponse(String response) {
        String longSummaryPattern = "longSummary: (.+?)(?=\\nshortSummary:)";
        String shortSummaryPattern = "shortSummary: (.+?)(?=\\nkeywords:)";
        String keywordsPattern = "keywords: \\[(.+)]";

        // Extract longSummary
        String longSummary = extractUsingRegex(response, longSummaryPattern);

        // Extract shortSummary
        String shortSummary = extractUsingRegex(response, shortSummaryPattern);

        // Extract keywords and convert to List<String>
        String keywordsString = extractUsingRegex(response, keywordsPattern);
        List<String> keywords = new ArrayList<>();
        if (keywordsString != null && !keywordsString.isEmpty()) {
            String[] splitKeywords = keywordsString.split(",\\s*");
            for (String keyword : splitKeywords) {
                keywords.add(keyword.trim());
            }
        }

        VectorMetaData metaData = new VectorMetaData();
        metaData.setLongSummary(longSummary);
        metaData.setShortSummary(shortSummary);
        metaData.setKeywords(keywords);

        return metaData;
    }

    private static String extractUsingRegex(String input, String pattern) {
        Pattern regex = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = regex.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}