package jmusic.library.backend.cd;

class CDAccessFactory {
    static CDAccess getCDAccess() {
        return new jmusic.library.backend.cd.linux.CDAccess();
    }
}