package org.gbif.dwc;

import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.record.Record;
import org.gbif.dwc.record.StarRecord;
import org.gbif.utils.file.ClosableIterator;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests related to {@link DwcFiles}.
 * For methods where the call is delegated to {@link InternalDwcFileFactory}, the corresponding test is in
 * {@link InternalDwcFileFactoryTest}.
 */
public class DwcFilesTest {

  @Test
  public void testNormalizeIfRequired() throws Exception {
    ArchiveFile testArchiveFile = new ArchiveFile();
    testArchiveFile.setFieldsEnclosedBy(null);
    assertNull(testArchiveFile.normalizeIfRequired());
  }

  @Test
  public void testDwcRecordIterator() throws IOException {
    Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("archive-dwc").toPath());
    int count=0;
    try(ClosableIterator<Record> it = arch.getCore().iterator()) {
      while (it.hasNext()) {
        it.next();
        count++;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(3248, count);
  }

  @Test
  public void testStarRecordIterator() throws IOException {

    Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("archive-dwc").toPath());

    try (ClosableIterator<StarRecord> it = arch.iterator(false, false)) {
      assertNotNull(arch.getCore());
      assertEquals(2, arch.getExtensions().size());
      int found = 0;
      int extensionRecords = 0;
      while (it.hasNext()) {
        StarRecord rec = it.next();
        //System.out.println(rec.core().id() + " → " + rec.size());
        // count all extension records
        extensionRecords += rec.size();

        if (rec.core().id().equals("544382")) {
          found++;
          assertEquals("Tursiops truncatus truncatus Montagu", rec.core().value(DwcTerm.scientificName));
          // test extension iter
          int i = 0;
          for (Record er : rec) {
            i++;
          }
          assertEquals(27, i);
        } else if (rec.core().id().equals("105833")) {
          found++;
//        105833  Chinese river dolphin English
//        105833  Chinese lake dolphin  English
//        105833  Pei c’hi	Chinese
//        105833  White flag dolphin  English
          int i = 0;
          for (Record er : rec) {
            i++;
            if ("Chinese river dolphin".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("English", er.value(DcTerm.language));
              assertEquals("English", er.value(DcTerm.language));
            } else if ("Chinese lake dolphin".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("English", er.value(DcTerm.language));
            } else if ("Pei c’hi".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("Chinese", er.value(DcTerm.language));
            } else {
              assertEquals("White flag dolphin", er.value(DwcTerm.vernacularName));
              assertEquals("English", er.value(DcTerm.language));
            }
          }
          assertEquals(4, i);
        } else if (rec.core().id().equals("105838")) {
          found++;
//      105838  Delfin de La Plata  Spanish
//      105838  Franciscana Spanish
//      105838  Franciscano Portuguese
//      105838  La Plata dolphin  English
//      105838  Tonina  Spanish
//      105838  Toninha Portuguese
          int i = 0;
          for (Record er : rec) {
            i++;
            if ("Delfin de La Plata".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("Spanish", er.value(DcTerm.language));
            } else if ("Franciscana".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("Spanish", er.value(DcTerm.language));
            } else if ("Franciscano".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("Portuguese", er.value(DcTerm.language));
            } else if ("La Plata dolphin".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("English", er.value(DcTerm.language));
            } else if ("Tonina".equals(er.value(DwcTerm.vernacularName))) {
              assertEquals("Spanish", er.value(DcTerm.language));
            } else {
              assertEquals("Toninha", er.value(DwcTerm.vernacularName));
              assertEquals("Portuguese", er.value(DcTerm.language));
            }
          }
          assertEquals(6, i);
        }
      }
      assertEquals(3, found);
      assertEquals(1057, extensionRecords);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

  }

  @Test
  public void testNormalizeAndSort() throws IOException, InterruptedException {

    Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("archive-dwc").toPath());
    ArchiveFile core = arch.getCore();
    File sortedFile = ArchiveFile.getLocationFileSorted(core.getLocationFile());

    //ensure the sorted file for the core doesn't exist
    if(sortedFile.exists()) {
      sortedFile.delete();
    }
    assertTrue(arch.getCore().normalizeAndSort());
    assertTrue(sortedFile.exists());

    //call the method again. Should return false since we already have the sorted file available.
    assertFalse(arch.getCore().normalizeAndSort());
  }

  /**
   * Basic validation of archives, that the declared files exist and have basic, valid structure.
   */
  @Test
  public void testInvalidArchives() throws IOException {
    System.out.println("Starting on invalids");
    // Simple archive problems
    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/empty").toPath());
      arch.validate();
      fail("Empty archive should not be opened.");
    } catch (UnsupportedArchiveException e) {}

    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/meta-file-location-missing").toPath());
      arch.validate();

      arch.getCore().iterator().hasNext();

      fail("Archive with missing file location in meta.xml should not be opened.");
    } catch (UnsupportedArchiveException e) {}

    // Extension archive problems
    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/extension-missing").toPath());
      arch.validate();
      fail("Archive with missing extension file should not be opened.");
    } catch (UnsupportedArchiveException e) {}

    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/extension-location-missing").toPath());
      arch.validate();
      fail("Archive with missing extension file location in meta.xml should not be opened.");
    } catch (UnsupportedArchiveException e) {}

    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/extension-core-id-missing").toPath());
      arch.validate();
      fail("Archive with extension lacking coreid in meta.xml should not be opened.");
    } catch (UnsupportedArchiveException e) {}

    try {
      Archive arch = DwcFiles.fromLocation(FileUtils.getClasspathFile("invalid/extension-id-missing").toPath());
      arch.validate();
      fail("Archive with extension and core missing id in meta.xml should not be opened.");
    } catch (UnsupportedArchiveException e) {}
  }
}
