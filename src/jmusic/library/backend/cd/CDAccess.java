package jmusic.library.backend.cd;

import jmusic.library.LibraryException;
import jmusic.util.ProgressListener;

import java.io.File;
import java.io.InputStream;

public interface CDAccess {
    String getRootPath();

    InputStream getTrackInputStream( File inFile, ProgressListener inListener ) throws LibraryException;
}