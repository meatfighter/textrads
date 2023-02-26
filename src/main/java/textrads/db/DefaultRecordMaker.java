package textrads.db;

import textrads.Textrads;

public enum DefaultRecordMaker implements RecordMaker<Record> {
    RECORD {
        @Override
        public Record make(final int index) {
            final char initial = (char) ('A' + index);
            return new Record(new StringBuilder().append(initial).append(initial).append(initial).toString(), 
                    10_000 * (10 - index), (short) 0, 0L);
        }            
    },
    EXTENDED_RECORD {
        @Override
        public ExtendedRecord make(final int index) {
            final char initial = (char) ('A' + index);
            return new ExtendedRecord(
                    new StringBuilder().append(initial).append(initial).append(initial).toString(),
                    (byte) 0, (short) 0, (10 + index) * 60 * Textrads.FRAMES_PER_SECOND, 1000 * (10 - index), 0L);
        }
    }
}
