package textrads.db;

public interface RecordMaker<T> {
    
    T make(int index);
}
