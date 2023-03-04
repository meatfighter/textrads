package textrads.db;

public final class DatabaseSource {

    private static final Database database = new Database();
    
    public static Database getDatabase() {
        return database;
    }
    
    private DatabaseSource() {        
    }
}
