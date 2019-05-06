/*
 * Copyright 2009-2017 java-diff-utils.
 * Changed copyright 2019 JoJoModding.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jojomodding.decomparer.processing;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.InsertDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRow.Tag;
import org.eclipse.jgit.util.StringUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class for generating DiffRows for side-by-sidy view. You can customize the way of generating. For example, show
 * inline diffs on not, ignoring white spaces or/and blank lines and so on. All parameters for generating are optional.
 * If you do not specify them, the class will use the default values.
 *
 * These values are: showInlineDiffs = false; ignoreWhiteSpaces = true; ignoreBlankLines = true; ...
 *
 * For instantiating the DiffRowGenerator you should use the its builder. Like in example  <code>
 *    DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 *      ignoreWhiteSpaces(true).columnWidth(100).build();
 * </code>
 */
public class DiffRowGenerator {

    public static final BiPredicate<String, String> DEFAULT_EQUALIZER = Object::equals;

    public static final BiPredicate<String, String> IGNORE_WHITESPACE_EQUALIZER = (original, revised)
            -> adjustWhitespace(original).equals(adjustWhitespace(revised));

    /**
     * Splitting lines by character to achieve char by char diff checking.
     */
    public static final Function<String, List<String>> SPLITTER_BY_CHARACTER = line -> {
        List<String> list = new ArrayList<>(line.length());
        for (Character character : line.toCharArray()) {
            list.add(character.toString());
        }
        return list;
    };
    public static final Pattern SPLIT_BY_WORD_PATTERN = Pattern.compile("\\s+|[,.\\[\\](){}/\\\\*+\\-#]");
    /**
     * Splitting lines by word to achieve word by word diff checking.
     */
    public static final Function<String, List<String>> SPLITTER_BY_WORD = line -> splitStringPreserveDelimiter(line, SPLIT_BY_WORD_PATTERN);
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static Builder create() {
        return new Builder();
    }

    private static String adjustWhitespace(String raw) {
        return WHITESPACE_PATTERN.matcher(raw.trim()).replaceAll(" ");
    }

    protected final static List<String> splitStringPreserveDelimiter(String str, Pattern SPLIT_PATTERN) {
        List<String> list = new ArrayList<>();
        if (str != null) {
            Matcher matcher = SPLIT_PATTERN.matcher(str);
            int pos = 0;
            while (matcher.find()) {
                if (pos < matcher.start()) {
                    list.add(str.substring(pos, matcher.start()));
                }
                list.add(matcher.group());
                pos = matcher.end();
            }
            if (pos < str.length()) {
                list.add(str.substring(pos));
            }
        }
        return list;
    }

    /**
     * Wrap the elements in the sequence with the given tag
     *
     * @param startPosition the position from which tag should start. The counting start from a zero.
     * @param endPosition the position before which tag should should be closed.
     */
    static void wrapInTag(List<String> sequence, int startPosition,
                          int endPosition, Function<Boolean, String> tagGenerator) {
        int endPos = endPosition;

        while (endPos >= startPosition) {

            //search position for end tag
            while (endPos > startPosition) {
                if (!"\n".equals(sequence.get(endPos - 1))) {
                    break;
                }
                endPos--;
            }

            if (endPos == startPosition) {
                break;
            }

            sequence.add(endPos, tagGenerator.apply(false));
            endPos--;

            //search position for end tag
            while (endPos > startPosition) {
                if ("\n".equals(sequence.get(endPos - 1))) {
                    break;
                }
                endPos--;
            }

            sequence.add(endPos, tagGenerator.apply(true));
            endPos--;
        }

//        sequence.add(endPosition, tagGenerator.apply(false));
//        sequence.add(startPosition, tagGenerator.apply(true));
    }
    private final int columnWidth;
    private final BiPredicate<String, String> equalizer;
    private final boolean ignoreWhiteSpaces;
    private final Function<String, List<String>> inlineDiffSplitter;
    private final boolean mergeOriginalRevised;
    private final Function<Boolean, String> newTag;
    private final Function<Boolean, String> oldTag;
    private final boolean reportLinesUnchanged;

    private final boolean showInlineDiffs;

    private DiffRowGenerator(Builder builder) {
        showInlineDiffs = builder.showInlineDiffs;
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces;
        oldTag = builder.oldTag;
        newTag = builder.newTag;
        columnWidth = builder.columnWidth;
        mergeOriginalRevised = builder.mergeOriginalRevised;
        inlineDiffSplitter = builder.inlineDiffSplitter;
        equalizer = ignoreWhiteSpaces ? IGNORE_WHITESPACE_EQUALIZER : DEFAULT_EQUALIZER;
        reportLinesUnchanged = builder.reportLinesUnchanged;

        Objects.requireNonNull(inlineDiffSplitter);
    }

    /**
     * Get the DiffRows describing the difference between original and revised texts using the given patch. Useful for
     * displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(List<String> original, List<String> revised) throws DiffException {
        return generateDiffRows(original, DiffUtils.diff(original, revised, equalizer));
    }

    /**
     * Generates the DiffRows describing the difference between original and revised texts using the given patch. Useful
     * for displaying side-by-side diff.
     *
     * @param original the original text
     * @param patch the given patch
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(final List<String> original, Patch<String> patch) throws DiffException {
        List<DiffRow> diffRows = new ArrayList<>();
        int endPos = 0;
        final List<AbstractDelta<String>> deltaList = patch.getDeltas();
        for (int i = 0; i < deltaList.size(); i++) {
            AbstractDelta<String> delta = deltaList.get(i);
            Chunk<String> orig = delta.getSource();
            Chunk<String> rev = delta.getTarget();

            for (String line : original.subList(endPos, orig.getPosition())) {
                diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
            }

            // Inserted DiffRow
            if (delta instanceof InsertDelta) {
                endPos = orig.last() + 1;
                for (String line : (List<String>) rev.getLines()) {
                    diffRows.add(buildDiffRow(Tag.INSERT, "", line));
                }
                continue;
            }

            // Deleted DiffRow
            if (delta instanceof DeleteDelta) {
                endPos = orig.last() + 1;
                for (String line : (List<String>) orig.getLines()) {
                    diffRows.add(buildDiffRow(Tag.DELETE, line, ""));
                }
                continue;
            }

            if (showInlineDiffs) {
                diffRows.addAll(generateInlineDiffs(delta));
            } else {
                for (int j = 0; j < Math.max(orig.size(), rev.size()); j++) {
                    diffRows.add(buildDiffRow(Tag.CHANGE,
                            orig.getLines().size() > j ? orig.getLines().get(j) : "",
                            rev.getLines().size() > j ? rev.getLines().get(j) : ""));
                }
            }
            endPos = orig.last() + 1;
        }

        // Copy the final matching chunk if any.
        for (String line : original.subList(endPos, original.size())) {
            diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
        }
        return diffRows;
    }

    private DiffRow buildDiffRow(Tag type, String orgline, String newline) {
        if (reportLinesUnchanged) {
            return new DiffRow(type, orgline, newline);
        } else {
            String wrapOrg = preprocessLine(orgline);
            if (Tag.DELETE == type) {
                if (mergeOriginalRevised || showInlineDiffs) {
                    wrapOrg = oldTag.apply(true) + wrapOrg + oldTag.apply(false);
                }
            }
            String wrapNew = preprocessLine(newline);
            if (Tag.INSERT == type) {
                if (mergeOriginalRevised) {
                    wrapOrg = newTag.apply(true) + wrapNew + newTag.apply(false);
                } else if (showInlineDiffs) {
                    wrapNew = newTag.apply(true) + wrapNew + newTag.apply(false);
                }
            }
            return new DiffRow(type, wrapOrg, wrapNew);
        }
    }

    private DiffRow buildDiffRowWithoutNormalizing(Tag type, String orgline, String newline) {
        return new DiffRow(type,orgline,newline);
    }

    /**
     * Add the inline diffs for given delta
     *
     * @param delta the given delta
     */
    private List<DiffRow> generateInlineDiffs(AbstractDelta<String> delta) throws DiffException {
        List<String> orig = delta.getSource().getLines().stream().map(this::preprocessLine).collect(Collectors.toList());
        List<String> rev = delta.getTarget().getLines().stream().map(this::preprocessLine).collect(Collectors.toList());
        List<String> origList;
        List<String> revList;
        String joinedOrig = String.join("\n", orig);
        String joinedRev = String.join("\n", rev);

        origList = inlineDiffSplitter.apply(joinedOrig);
        revList = inlineDiffSplitter.apply(joinedRev);

        List<AbstractDelta<String>> inlineDeltas = DiffUtils.diff(origList, revList).getDeltas();

        Collections.reverse(inlineDeltas);
        for (AbstractDelta<String> inlineDelta : inlineDeltas) {
            Chunk<String> inlineOrig = inlineDelta.getSource();
            Chunk<String> inlineRev = inlineDelta.getTarget();
            if (inlineDelta instanceof DeleteDelta) {
                wrapInTag(origList, inlineOrig.getPosition(), inlineOrig
                        .getPosition()
                        + inlineOrig.size(), oldTag);
            } else if (inlineDelta instanceof InsertDelta) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition(),
                            revList.subList(inlineRev.getPosition(), inlineRev.getPosition()
                                    + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition(), inlineOrig.getPosition()
                            + inlineRev.size(), newTag);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition()
                            + inlineRev.size(), newTag);
                }
            } else if (inlineDelta instanceof ChangeDelta) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition() + inlineOrig.size(),
                            revList.subList(inlineRev.getPosition(), inlineRev.getPosition()
                                    + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition() + inlineOrig.size(), inlineOrig.getPosition() + inlineOrig.size()
                            + inlineRev.size(), newTag);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition()
                            + inlineRev.size(), newTag);
                }
                wrapInTag(origList, inlineOrig.getPosition(), inlineOrig
                        .getPosition()
                        + inlineOrig.size(), oldTag);
            }
        }
        StringBuilder origResult = new StringBuilder();
        StringBuilder revResult = new StringBuilder();
        for (String character : origList) {
            origResult.append(character);
        }
        for (String character : revList) {
            revResult.append(character);
        }

        List<String> original = Arrays.asList(origResult.toString().split("\n"));
        List<String> revised = Arrays.asList(revResult.toString().split("\n"));
        List<DiffRow> diffRows = new ArrayList<>();
        for (int j = 0; j < Math.max(original.size(), revised.size()); j++) {
            diffRows.
                    add(buildDiffRowWithoutNormalizing(Tag.CHANGE,
                            original.size() > j ? original.get(j) : "",
                            revised.size() > j ? revised.get(j) : ""));
        }
        return diffRows;
    }

    private String preprocessLine(String line) {
        return line.replace("\t", "    ");
    }

    /**
     * This class used for building the DiffRowGenerator.
     *
     * @author dmitry
     *
     */
    public static class Builder {

        private boolean showInlineDiffs = false;
        private boolean ignoreWhiteSpaces = false;

        private Function<Boolean, String> oldTag = f -> f ? "<span class=\"editOldInline\">" : "</span>";
        private Function<Boolean, String> newTag = f -> f ? "<span class=\"editNewInline\">" : "</span>";

        private int columnWidth = 0;
        private boolean mergeOriginalRevised = false;
        private boolean reportLinesUnchanged = false;
        private Function<String, List<String>> inlineDiffSplitter = SPLITTER_BY_CHARACTER;

        private Builder() {
        }

        /**
         * Show inline diffs in generating diff rows or not.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured showInlineDiff parameter
         */
        public Builder showInlineDiffs(boolean val) {
            showInlineDiffs = val;
            return this;
        }

        /**
         * Ignore white spaces in generating diff rows or not.
         *
         * @param val the value to set. Default: true.
         * @return builder with configured ignoreWhiteSpaces parameter
         */
        public Builder ignoreWhiteSpaces(boolean val) {
            ignoreWhiteSpaces = val;
            return this;
        }

        /**
         * Give the originial old and new text lines to Diffrow without any additional processing.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured reportLinesUnWrapped parameter
         */
        public Builder reportLinesUnchanged(final boolean val) {
            reportLinesUnchanged = val;
            return this;
        }

        /**
         * Generator for Old-Text-Tags.
         *
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder oldTag(Function<Boolean, String> generator) {
            this.oldTag = generator;
            return this;
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        public Builder newTag(Function<Boolean, String> generator) {
            this.newTag = generator;
            return this;
        }

        /**
         * Set the column with of generated lines of original and revised texts.
         *
         * @param width the width to set. Making it < 0 doesn't have any sense. Default 80. @return builder with config
         * ured ignoreBlankLines parameter
         */
        public Builder columnWidth(int width) {
            if (width >= 0) {
                columnWidth = width;
            }
            return this;
        }

        /**
         * Build the DiffRowGenerator. If some parameters is not set, the default values are used.
         *
         * @return the customized DiffRowGenerator
         */
        public DiffRowGenerator build() {
            return new DiffRowGenerator(this);
        }

        /**
         * Merge the complete result within the original text. This makes sense for one line display.
         *
         * @param mergeOriginalRevised
         * @return
         */
        public Builder mergeOriginalRevised(boolean mergeOriginalRevised) {
            this.mergeOriginalRevised = mergeOriginalRevised;
            return this;
        }

        /**
         * Per default each character is separatly processed. This variant introduces processing by word, which should
         * deliver no in word changes.
         */
        public Builder inlineDiffByWord(boolean inlineDiffByWord) {
            inlineDiffSplitter = inlineDiffByWord ? SPLITTER_BY_WORD : SPLITTER_BY_CHARACTER;
            return this;
        }

        public Builder inlineDiffBySplitter(Function<String, List<String>> inlineDiffSplitter) {
            this.inlineDiffSplitter = inlineDiffSplitter;
            return this;
        }
    }
}
