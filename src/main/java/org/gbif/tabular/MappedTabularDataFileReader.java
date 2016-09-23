package org.gbif.tabular;

import org.gbif.utils.file.tabular.TabularDataFileReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Decorator of {@link TabularDataFileReader} to map elements of a line to a key of type <T>.
 *
 */
public class MappedTabularDataFileReader<T> implements TabularDataFileReader<MappedTabularDataLine<T>> {

  private final TabularDataFileReader<List<String>> tabularDataFileReader;
  private final T[] columnMapping;
  private int lineNumber = 0;

  /**
   *
   * @param tabularDataFileReader a TabularDataFileReader implementation to read lines as List of String
   * @param columnMapping mapping of the columns. Index of the array matched the index of the element in the data line
   */
  public MappedTabularDataFileReader(TabularDataFileReader<List<String>> tabularDataFileReader, T[] columnMapping){
    this.tabularDataFileReader = tabularDataFileReader;
    this.columnMapping = columnMapping;
  }

  @Override
  public List<String> getHeaderLine() throws IOException {
    return tabularDataFileReader.getHeaderLine();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public MappedTabularDataLine<T> read() throws IOException {
    List<String> tabularLine = tabularDataFileReader.read();

    // check for end of file
    if(tabularLine == null){
      return null;
    }
    int numOfColumns = Math.min(columnMapping.length, tabularLine.size());
    Map<T, String> line = Maps.newHashMapWithExpectedSize(numOfColumns);
    for (int i = 0; i < numOfColumns; i++) {
      line.put(columnMapping[i], tabularLine.get(i));
    }

    lineNumber++;
    return new MappedTabularDataLine(lineNumber, line, tabularLine.size());
  }

  @Override
  public void close() {
    tabularDataFileReader.close();
  }

}
