package org.n3r.diamond.client;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.n3r.diamond.client.impl.DiamondUtils.splitLinesWoComments;

public class DiamondUtilsTest {
    @Test
    public void testSplitLinesWoComments() {
        List<String> strings = splitLinesWoComments("aaa\nbbb", "#");
        assertThat(strings).hasSize(2).contains("aaa", "bbb");

        strings = splitLinesWoComments("aaa\nbbb #这里是注释", "#");
        assertThat(strings).hasSize(2).contains("aaa", "bbb");

        strings = splitLinesWoComments("aaa\n#bbb", "#");
        assertThat(strings).hasSize(1).contains("aaa");

        strings = splitLinesWoComments("\n\naaa\n#bbb", "#");
        assertThat(strings).hasSize(1).contains("aaa");
    }
}
