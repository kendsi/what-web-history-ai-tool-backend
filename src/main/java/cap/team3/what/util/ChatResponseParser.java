package cap.team3.what.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cap.team3.what.dto.ParsedChatResponse;

public class ChatResponseParser {
    public static ParsedChatResponse parseChatResponse(String response) {
        String titlePattern = "title: (.+?)(?=\\nlongSummary:)";
        String longSummaryPattern = "longSummary: (.+?)(?=\\nshortSummary:)";
        String shortSummaryPattern = "shortSummary: (.+?)(?=\\ncategory:)";
        String categoryPattern = "category: (.+?)(?=\\nkeywords:)";
        String keywordsPattern = "keywords: \\[(.+)]";

        // Extract title
        String title = extractUsingRegex(response, titlePattern);

        // Extract longSummary
        String longSummary = extractUsingRegex(response, longSummaryPattern);

        // Extract shortSummary
        String shortSummary = extractUsingRegex(response, shortSummaryPattern);

        // Extract category
        String category = extractUsingRegex(response, categoryPattern);

        // Extract keywords and convert to List<String>
        String keywordsString = extractUsingRegex(response, keywordsPattern);
        List<String> keywords = new ArrayList<>();
        if (keywordsString != null && !keywordsString.isEmpty()) {
            String[] splitKeywords = keywordsString.split(",\\s*");
            for (String keyword : splitKeywords) {
                keywords.add(keyword.trim());
            }
        }

        ParsedChatResponse parsedChatResponse = new ParsedChatResponse(title, shortSummary, longSummary, category, keywords);

        return parsedChatResponse;
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