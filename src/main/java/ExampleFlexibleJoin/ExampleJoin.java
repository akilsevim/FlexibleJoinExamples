package ExampleFlexibleJoin;

import org.apache.asterix.external.cartilage.base.Configuration;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

class ExampleConfiguration implements Configuration {

}

class ExampleSummary implements Summary<String> {

    @Override
    public void add(String s) {

    }

    @Override
    public void add(Summary<String> summary) {

    }
}

public class ExampleJoin implements FlexibleJoin<String, ExampleConfiguration> {

    @Override
    public Summary<String> createSummarizer1() {
        return new ExampleSummary();
    }

    @Override
    public ExampleConfiguration divide(Summary<String> summary, Summary<String> summary1) {
        return new ExampleConfiguration();
    }

    @Override
    public int[] assign1(String s, ExampleConfiguration exampleConfiguration) {
        return new int[0];
    }

    @Override
    public boolean verify(String s, String t1) {
        return false;
    }
}
