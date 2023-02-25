package textrads.db;

public interface DefaultRecordMaker<T> {
    T make(int index);
}
