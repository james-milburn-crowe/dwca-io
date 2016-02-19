package org.gbif.io;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.text.StrTokenizer;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class CSVReaderTest {

  @Test
  public void testCsvAllwaysQuotes() throws IOException {
    File csv = FileUtils.getClasspathFile("csv_always_quoted.csv");
    CSVReader reader = new CSVReader(csv, "utf8", ",", '"', 1);

    String[] rec = reader.next();
    rec = reader.next();
    assertEquals("18728553", rec[0]);
    assertEquals("-0.25864171259110291", rec[6]);
    assertEquals("Martins Wood, Ightham", rec[10]);
  }

  @Test
  public void testStrTokenizerQuotedDelimiter() throws IOException {
    StrTokenizer tokenizer = new StrTokenizer();
    tokenizer.setDelimiterString(",");
    tokenizer.setQuoteChar('"');
    tokenizer.setIgnoreEmptyTokens(false);

    tokenizer.reset("13,not \"real\"");
    String[] rec = tokenizer.getTokenArray();
    assertEquals(2, rec.length);
    assertEquals("13", rec[0]);
    assertEquals("not \"real\"", rec[1]);

    String x = "15,\"not \"\"real\"\"\"";
    System.out.println(x);
    tokenizer.reset(x);
    rec = tokenizer.getTokenArray();
    assertEquals(2, rec.length);
    assertEquals("15", rec[0]);
    assertEquals("not \"real\"", rec[1]);
  }

  @Test
  public void testCsvQuotedDelimiter() throws IOException {
    String rows =
        "12,\"not real\"\n"
        + "13,not \"real\"\n"
        + "\"14\",noting\n"
        + "15,\"not \"\"real\"\"\"\n"
        + "16,\"no, this is \"\"real\"\"\"\n";

    System.out.println(rows);
    InputStream stream = new ByteArrayInputStream(rows.getBytes(Charsets.UTF_8));
    CSVReader reader = new CSVReader(stream, "utf8", ",", '"', 0);

    String[] rec = reader.next();
    assertEquals(2, rec.length);
    assertEquals("12", rec[0]);
    assertEquals("not real", rec[1]);

    rec = reader.next();
    assertEquals(2, rec.length);
    assertEquals("13", rec[0]);
    assertEquals("not \"real\"", rec[1]);

    rec = reader.next();
    assertEquals(2, rec.length);
    assertEquals("14", rec[0]);
    assertEquals("noting", rec[1]);

    rec = reader.next();
    assertEquals(2, rec.length);
    assertEquals("15", rec[0]);
    assertEquals("not \"real\"", rec[1]);

    rec = reader.next();
    assertEquals(2, rec.length);
    assertEquals("16", rec[0]);
    assertEquals("no, this is \"real\"", rec[1]);

    assertFalse(reader.hasNext());
  }

  @Test
  @Ignore("Empty test?")
  public void testCsvNoQuotes() throws IOException {

  }

  /**
   * csv file with optional quotes generated by excel.
   * single, double quotes and comma within a field are tested.
   */
  @Test
  public void testCsvOptionalQuotes() throws IOException {
    File csv = FileUtils.getClasspathFile("csv_optional_quotes_excel2008CSV.csv");
    CSVReader reader = new CSVReader(csv, "utf8", ",", '"', 1);

    String[] atom = reader.next();
    assertEquals(3, atom.length);
    assertEquals("1", atom[0]);
    assertEquals("This has a, comma", atom[2]);

    atom = reader.next();
    assertEquals("I say this is only a \"quote\"", atom[2]);

    atom = reader.next();
    assertEquals("What though, \"if you have a quote\" and a comma", atom[2]);

    atom = reader.next();
    assertEquals("What, if we have a \"quote, which has a comma, or 2\"", atom[2]);

    reader.close();
  }

  /**
   * tests the csv reader with different number of header rows on the same file and compares the 4th line in the text
   * file for each of them
   */
  @Test
  public void testHeaderRows() throws IOException {
    File source = FileUtils.getClasspathFile("iucn100.csv");
    // assert the headers are the same, no matter how many rows we skip for the iterator
    CSVReader reader = new CSVReader(source, "utf8", ",", '"', 1);
    reader.next();
    reader.next();
    String[] row4h1 = reader.next();
    reader.close();

    reader = new CSVReader(source, "utf8", ",", '"', 0);
    reader.next();
    reader.next();
    reader.next();
    String[] row4h0 = reader.next();
    reader.close();

    reader = new CSVReader(source, "utf8", ",", '"', 3);
    String[] row4h3 = reader.next();
    reader.close();

    assertTrue(row4h0.length == row4h1.length);
    assertTrue(row4h0.length == row4h3.length);
    int idx = row4h0.length;
    while (idx > 0) {
      idx--;
      assertEquals(row4h0[idx], row4h1[idx]);
      assertEquals(row4h0[idx], row4h3[idx]);
    }
  }

  /**
   * Test if skip header rows is working with larger settings.
   */
  @Test
  public void testHeaderRows2() throws IOException {
    File source = FileUtils.getClasspathFile("iucn100.csv");

    CSVReader reader = new CSVReader(source, "utf8", ",", '"', 7);
    for (String[] row : reader) {
      assertEquals("9", row[0]);
      assertEquals("Aaptosyax grypus Rainboth, 1991", row[1]);
      assertEquals("Actinopterygii", row[4]);
      break;
    }
  }


  @Test
  public void testIgnoreEmptyLines() throws IOException {
    File csv = FileUtils.getClasspathFile("empty_line.tab");
    CSVReader reader = new CSVReader(csv, "utf8", "\t", null, 1);
    String[] ids = {"1", "5", "10", "12", "14", "20"};
    int row = 0;
    while (reader.hasNext()) {
      String[] rec = reader.next();
      assertEquals(ids[row], rec[0]);
      row++;
    }
    assertTrue(reader.getEmptyLines().size() > 1);
    assertTrue(reader.getEmptyLines().contains(6));
    assertTrue(reader.getEmptyLines().contains(9));

  }

  /**
   * Testing classic non quoted tab files with escaped \t tabs.
   */
  @Test
  public void testTab() throws IOException {
    // build archive from single tab file
    File source = FileUtils.getClasspathFile("issues/ebird.tab.txt");
    CSVReader reader = new CSVReader(source, "utf8", "\t", null, 1);

    // there should be 8 rows, each with 58 columns
    String[] line;
    int lineCount = 0;
    while ((line = reader.next()) != null) {
      lineCount++;
    }
    assertEquals(8, lineCount);
  }


  /**
   * Testing tab files with " quoted fields
   */
  @Test
  @Ignore("Empty test?")
  public void testTabQuoted() throws IOException {

  }
}
