/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlScript supporting Oracle-specific PL/SQL constructs.
 */
public class OracleSqlScript extends SqlScript {
    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final String PLSQL_DELIMITER = "/";

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public OracleSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        String upperCaseLine = line.toUpperCase();
        if (upperCaseLine.matches("DECLARE|DECLARE\\s.*") || upperCaseLine.matches("BEGIN|BEGIN\\s.*")) {
            return PLSQL_DELIMITER;
        }

        String upperCaseStatement = statement.toUpperCase();
        if (upperCaseStatement.matches("CREATE\\W*FUNCTION.*")
                || upperCaseStatement.matches("CREATE\\W+PROCEDURE.*")
                || upperCaseStatement.matches("CREATE\\W+PACKAGE.*")
                || upperCaseStatement.matches("CREATE\\W+OR\\W+REPLACE\\W+FUNCTION.*")
                || upperCaseStatement.matches("CREATE\\W+OR\\W+REPLACE\\W+PROCEDURE.*")
                || upperCaseStatement.matches("CREATE\\W+OR\\W+REPLACE\\W+PACKAGE.*")) {
            return PLSQL_DELIMITER;
        }

        return delimiter;
    }

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        String filteredStatementForParensQQuotes =
                com.googlecode.flyway.core.util.StringUtils.replaceAll(statement, "q'(", "q'[");
        filteredStatementForParensQQuotes =
                com.googlecode.flyway.core.util.StringUtils.replaceAll(filteredStatementForParensQQuotes, ")'", "]'");

        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(filteredStatementForParensQQuotes, " ;=|(),");

        List<Token> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        boolean insideQuoteStringLiteral = false;
        boolean insideQStringLiteral = false;

        for (Token delimitingToken : delimitingTokens) {
            boolean moreTokensApplicable = true;
            for (TokenType tokenType : delimitingToken.tokenTypes) {
                if (!moreTokensApplicable) {
                    continue;
                }

                if (!insideQStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.QUOTE_OPEN)) {
                    insideQuoteStringLiteral = true;
                    if (delimitingToken.singleTypeApplicable) {
                        moreTokensApplicable = false;
                    }
                    continue;
                }
                if (insideQuoteStringLiteral && (tokenType == TokenType.QUOTE_CLOSE)) {
                    insideQuoteStringLiteral = false;
                    moreTokensApplicable = false;
                    continue;
                }
                if (!insideQStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.Q_OPEN)) {
                    insideQStringLiteral = true;
                    continue;
                }
                if (insideQStringLiteral && (tokenType == TokenType.Q_CLOSE)) {
                    insideQStringLiteral = false;
                    moreTokensApplicable = false;
                }
            }
        }

        return insideQuoteStringLiteral || insideQStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<Token> extractStringLiteralDelimitingTokens(String[] tokens) {
        String qCloseToken = "]'";

        List<Token> delimitingTokens = new ArrayList<Token>();
        for (String token : tokens) {
            //Remove escaped quotes as they do not form a string literal delimiter
            String cleanToken = StringUtils.replace(token, "''", "");

            List<TokenType> tokenTypes = new ArrayList<TokenType>();

            if (cleanToken.startsWith("'")) {
                tokenTypes.add(TokenType.QUOTE_OPEN);
            }

            if (cleanToken.endsWith("'")) {
                tokenTypes.add(TokenType.QUOTE_CLOSE);
            }

            if (cleanToken.startsWith("q'") && (cleanToken.length() >= 3)) {
                String qOpenToken = cleanToken.substring(0, 3);
                qCloseToken = computeQCloseToken(qOpenToken);

                tokenTypes.add(TokenType.Q_OPEN);
            }

            if (cleanToken.endsWith(qCloseToken)) {
                tokenTypes.add(TokenType.Q_CLOSE);
            }

            if (!tokenTypes.isEmpty()) {
                Token parsedToken = new Token();
                parsedToken.tokenTypes = tokenTypes;
                parsedToken.singleTypeApplicable = token.length() == 1;
                delimitingTokens.add(parsedToken);
            }
        }

        return delimitingTokens;
    }

    /**
     * Computes the closing token for a q-quote string starting with this opening token.
     *
     * @param qOpenToken The opening token.
     * @return The closing token.
     */
    private String computeQCloseToken(String qOpenToken) {
        char specialChar = qOpenToken.charAt(2);
        switch (specialChar) {
            case '[':
                return "]'";
            case '(':
                return ")'";
            case '{':
                return "}'";
            case '<':
                return ">'";
            default:
                return specialChar + "'";
        }
    }

    /**
     * A parsed token.
     */
    private class Token {
        /**
         * TokenTypes of this token.
         */
        public List<TokenType> tokenTypes;

        /**
         * Flag indicating whether only a single tokenType may influence the parsing.
         */
        public boolean singleTypeApplicable;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens ' string literal
         */
        QUOTE_OPEN,

        /**
         * Token closes ' string literal
         */
        QUOTE_CLOSE,

        /**
         * Token opens q' string literal
         */
        Q_OPEN,

        /**
         * Token closes q' string literal
         */
        Q_CLOSE
    }
}
