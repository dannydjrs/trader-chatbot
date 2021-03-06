package virtualassistant.data.stocks;

import java.util.Calendar;
import java.util.HashMap;

public class HistoricalData extends HashMap<Calendar, HistoricalData.Record> {

  private Calendar latestEntry;

  public HistoricalData() {

    latestEntry = Calendar.getInstance();
    latestEntry.add(Calendar.YEAR, -5);
  }

  @Override
  public Record put(Calendar key, Record value) {
    Record previous = super.put(key, value);

    if (key.after(latestEntry)) {
      latestEntry = key;
    }

    Calendar yearAgo = (Calendar)key.clone();
    yearAgo.add(Calendar.YEAR, -1);

    //Check for dates from more than a year ago.
    for (Calendar entryDate : this.keySet()) {
      if (entryDate.before(yearAgo)) {
        this.remove(entryDate);
      }
    }

    return previous;
  }

  public Calendar getLatestEntryDate() {
    return latestEntry;
  }

  public static class Record {
    public double open;
    public double close;
    public double high;
    public double low;
    public int volume;
  }

}
