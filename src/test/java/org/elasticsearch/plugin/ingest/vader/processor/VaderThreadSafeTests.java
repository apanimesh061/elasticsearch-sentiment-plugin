package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.elasticsearch.test.ESTestCase;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;


/**
 * This test confirms that the {@link SentimentAnalyzer} class is thread safe.
 * For this we have used a {@link CountDownLatch} to spin in some threads.
 *
 * @author Animesh Pandey
 */
public class VaderThreadSafeTests extends ESTestCase {
    private SentimentAnalyzer sentimentAnalyzerService;
    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        sentimentAnalyzerService = new SentimentAnalyzer();
        executorService = Executors.newFixedThreadPool(10);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        terminate(executorService);
    }

    public void testThatVaderAnalyzerIsThreadSafe() throws InterruptedException {
        int runs = 20;
        CountDownLatch latch = new CountDownLatch(runs);
        List<SentimentAnalyzerRunnable> runnables = new ArrayList<>();
        List<DocumentPolarityPair> documentPolarityPairList = new ArrayList<>();
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is smart, handsome, and funny.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.254F);
                            put("positive", 0.746F);
                            put("compound", 0.8316F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is smart, handsome, and funny!",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.248F);
                            put("positive", 0.752F);
                            put("compound", 0.8439F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is very smart, handsome, and funny.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.299F);
                            put("positive", 0.701F);
                            put("compound", 0.8545F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is VERY SMART, handsome, and FUNNY.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.246F);
                            put("positive", 0.754F);
                            put("compound", 0.9227F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is VERY SMART, handsome, and FUNNY!!!",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.233F);
                            put("positive", 0.767F);
                            put("compound", 0.9342F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "VADER is VERY SMART, really handsome, and INCREDIBLY FUNNY!!!",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.294F);
                            put("positive", 0.706F);
                            put("compound", 0.9469F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "The book was good.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.508F);
                            put("positive", 0.492F);
                            put("compound", 0.4404F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "The book was kind of good.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.657F);
                            put("positive", 0.343F);
                            put("compound", 0.3832F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "The plot was good, but the characters are uncompelling and the dialog is not great.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.327F);
                            put("neutral", 0.579F);
                            put("positive", 0.094F);
                            put("compound", -0.7042F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "A really bad, horrible book.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.791F);
                            put("neutral", 0.209F);
                            put("positive", 0.0F);
                            put("compound", -0.8211F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "At least it isn't a horrible book.",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.637F);
                            put("positive", 0.363F);
                            put("compound", 0.431F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        ":) and :D",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.124F);
                            put("positive", 0.876F);
                            put("compound", 0.7925F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "",
                        new HashMap<String, Float>() {{
                            put("negative", 0.0F);
                            put("neutral", 0.0F);
                            put("positive", 0.0F);
                            put("compound", 0.0F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "Today sux",
                        new HashMap<String, Float>() {{
                            put("negative", 0.714F);
                            put("neutral", 0.286F);
                            put("positive", 0.0F);
                            put("compound", -0.3612F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "Today sux!",
                        new HashMap<String, Float>() {{
                            put("negative", 0.736F);
                            put("neutral", 0.264F);
                            put("positive", 0.0F);
                            put("compound", -0.4199F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "Today SUX!",
                        new HashMap<String, Float>() {{
                            put("negative", 0.779F);
                            put("neutral", 0.221F);
                            put("positive", 0.0F);
                            put("compound", -0.5461F);
                        }}
                )
        );
        documentPolarityPairList.add(
                new DocumentPolarityPair(
                        "Today kinda sux! But I'll get by, lol",
                        new HashMap<String, Float>() {{
                            put("negative", 0.195F);
                            put("neutral", 0.531F);
                            put("positive", 0.274F);
                            put("compound", 0.2228F);
                        }}
                )
        );

        for (int i = 0; i < runs; i++) {
            int randomIndex = new Random().nextInt(documentPolarityPairList.size());
            DocumentPolarityPair randomPair = documentPolarityPairList.get(randomIndex);
            SentimentAnalyzerRunnable runnable = new SentimentAnalyzerRunnable(i, randomPair, latch);
            runnables.add(runnable);
            executorService.submit(runnable);
        }

        latch.await(30, TimeUnit.SECONDS);
        runnables.forEach(SentimentAnalyzerRunnable::assertResultIsCorrect);
    }

    /**
     * This class passes one text document to one thread and then compares if the current
     * thread is producing the same result as expected.
     */
    private class SentimentAnalyzerRunnable implements Runnable {
        private int index;
        final DocumentPolarityPair currentDocumentPolarityPair;
        private CountDownLatch latch;
        HashMap<String, Float> actualResult;
        HashMap<String, Float> expectedResult;

        SentimentAnalyzerRunnable(int index, DocumentPolarityPair currentDocumentPolarityPair, CountDownLatch latch) {
            this.index = index;
            this.currentDocumentPolarityPair = currentDocumentPolarityPair;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                String currentDocument = currentDocumentPolarityPair.getDocument();
                System.out.print(currentDocument + "\n");
                expectedResult = currentDocumentPolarityPair.getPolarities();
                sentimentAnalyzerService.setInputString(currentDocument);
                sentimentAnalyzerService.setInputStringProperties();
                sentimentAnalyzerService.analyse();
                actualResult = sentimentAnalyzerService.getPolarity();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }

        private void assertResultIsCorrect() {
            assertThat(String.format(Locale.ROOT, "Expected task %s to have result %s", index, expectedResult),
                    expectedResult.entrySet(), equalTo(actualResult.entrySet()));
        }
    }

    /**
     * Class that stores a text document and its sentiment polarity.
     */
    private class DocumentPolarityPair {
        final String document;
        final HashMap<String, Float> polarities;

        DocumentPolarityPair(String document, HashMap<String, Float> polarities) {
            this.document = document;
            this.polarities = polarities;
        }

        String getDocument() {
            return document;
        }

        HashMap<String, Float> getPolarities() {
            return polarities;
        }
    }
}
