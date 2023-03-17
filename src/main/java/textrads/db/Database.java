package textrads.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import textrads.util.ThreadUtil;

public class Database {
    
    public static interface AllTimeKeys {
        String MARATHON = "all-time-marathon";
        String CONSTANT_LEVEL = "all-time-constant-level";
        String GARBAGE_HEAP = "all-time-garbage-heap";
        String RISING_GARBAGE = "all-time-rising-garbage";
        String THREE_MINUTES = "all-time-three-minutes";
        String FORTY_LINES = "all-time-forty-lines";
        String NO_ROTATION = "all-time-no-rotation";
        String INVISIBLE = "all-time-invisible";
        String VS_AI = "all-time-vs-ai";
    }
    
    public static interface TodaysKeys {
        String MARATHON = "todays-marathon";
        String CONSTANT_LEVEL = "todays-constant-level";
        String GARBAGE_HEAP = "todays-garbage-heap";
        String RISING_GARBAGE = "todays-rising-garbage";
        String THREE_MINUTES = "todays-three-minutes";
        String FORTY_LINES = "todays-forty-lines";
        String NO_ROTATION = "todays-no-rotation";
        String INVISIBLE = "todays-invisible";
        String VS_AI = "todays-vs-ai";
    }
    
    public static interface OtherKeys {
        String PREFERENCES = "preferences";
        String KEY_MAP = "key-map";
    }
    
    private static final String DIR = "data";
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final Map<String, ? super Serializable> values = new ConcurrentHashMap<>();
    
    public void init() {
        final File dir = new File(DIR);
        if (!(dir.exists() && dir.isDirectory())) {
            dir.mkdirs();
        }

        load(AllTimeKeys.MARATHON, RecordList.RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.CONSTANT_LEVEL, RecordList.RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.RISING_GARBAGE, RecordList.RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.THREE_MINUTES, RecordList.RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.INVISIBLE, RecordList.RECORD_LIST_SUPPLIER);
        
        load(AllTimeKeys.GARBAGE_HEAP, RecordList.EXTENDED_RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.FORTY_LINES, RecordList.EXTENDED_RECORD_LIST_SUPPLIER);
        load(AllTimeKeys.VS_AI, RecordList.EXTENDED_RECORD_LIST_SUPPLIER);
        
        load(TodaysKeys.MARATHON, RecordList.RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.CONSTANT_LEVEL, RecordList.RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.RISING_GARBAGE, RecordList.RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.THREE_MINUTES, RecordList.RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.INVISIBLE, RecordList.RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        
        load(TodaysKeys.GARBAGE_HEAP, RecordList.EXTENDED_RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.FORTY_LINES, RecordList.EXTENDED_RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        load(TodaysKeys.VS_AI, RecordList.EXTENDED_RECORD_LIST_SUPPLIER, RecordList.TODAYS_INITIALIZATION_TASK);
        
        load(OtherKeys.PREFERENCES, () -> new Preferences());
        load(OtherKeys.KEY_MAP, () -> new KeyMap());
    }
    
    public RecordList<? super Record> get(final String key, final boolean todaysBest) {
        final RecordList<? super Record> recordList = get(key);
        if (todaysBest) {
            final RecordList<? super Record> updatedRecordList = recordList.removeExpired();
            if (recordList != updatedRecordList) {
                saveAsync(key, updatedRecordList);
                return updatedRecordList;
            }            
        }
        return recordList;
    }
    
    public <T extends Serializable> T get(final String key) {
        return (T) values.get(key);
    }
    
    public <T extends Serializable> void saveAsync(final String key, T value) {
        values.put(key, value);
        executor.execute(() -> save(key, value));
    }
    
    private <T extends Serializable> void save(final String key, T value) {
        final File dataFile = getDataFile(key);
        final File tempFile = getTempFile(key);
        
        save(tempFile, value);        
        dataFile.delete();
        tempFile.renameTo(dataFile);
    }
    
    public <T extends Serializable> void load(final String key, final Supplier<T> defaultValueSupplier) {
        load(key, defaultValueSupplier, null);
    }
    
    public <T extends Serializable> void load(final String key, final Supplier<T> defaultValueSupplier, 
            final Function<T, T> initializationTask) {

        final File dataFile = getDataFile(key);
        final File tempFile = getTempFile(key);       
        
        T obj = load(tempFile);
        if (obj != null) {
            dataFile.delete();
            tempFile.renameTo(dataFile);            
        } else {
            tempFile.delete();
            obj = load(dataFile);            
        }
        
        if (obj == null) {
            dataFile.delete();
            tempFile.delete();
            obj = defaultValueSupplier.get();
        } else if (initializationTask != null) {
            obj = initializationTask.apply(obj);
        }
        
        values.put(key, obj);
    }
    
    private File getDataFile(final String key) {
        return new File(String.format("%s/%s.dat", DIR, key));
    }
    
    private File getTempFile(final String key) {
        return new File(String.format("%s/%s.dat~", DIR, key));
    }
    
    private <T extends Serializable> T load(final File file) {
        
        if (!(file.exists() && file.isFile())) {
            return null;
        }
        
        try (final FileInputStream fis = new FileInputStream(file);
                final BufferedInputStream bis = new BufferedInputStream(fis);
                final ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (final Exception ignored) {
            ignored.printStackTrace(); // TODO REMOVE
        }
        
        return null;
    }
    
    private <T extends Serializable> void save(final File file, T obj) {
        
        try (final FileOutputStream fos = new FileOutputStream(file);
                final BufferedOutputStream bos = new BufferedOutputStream(fos);
                final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        } catch (final Exception ignored) { 
            ignored.printStackTrace(); // TODO REMOVE
        }
    }
    
    public void shutdown() {
        ThreadUtil.shutdownAndAwaitTermination(executor);
    }
}
