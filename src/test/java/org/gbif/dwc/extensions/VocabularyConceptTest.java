package org.gbif.dwc.extensions;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Unit tests related to {@link VocabularyConcept}
 */
public class VocabularyConceptTest {

  @Test
  public void testCompareTo() {
    VocabularyConcept a = new VocabularyConcept();
    a.setUri("http://2");
    a.setOrder(1);

    VocabularyConcept b = new VocabularyConcept();
    b.setUri("http://1");
    a.setOrder(2);

    assertTrue(a.compareTo(b) > 0);
  }
}
