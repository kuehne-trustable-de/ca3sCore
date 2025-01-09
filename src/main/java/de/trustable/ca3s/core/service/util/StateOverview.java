package de.trustable.ca3s.core.service.util;

public class StateOverview {
    private int all = 0;
    private int active = 0;
    private int inactive = 0;
    private int expiringSoon = 0;

    public int incAll() {
        return ++all;
    }

    public int incActive() {
        return ++active;
    }

    public int incInactive() {
        return ++inactive;
    }

    public int incExpiringSoon() {
        return ++expiringSoon;
    }
    public int getAll() {
        return all;
    }

    public int getActive() {
        return active;
    }

    public int getInactive() {
        return inactive;
    }

    public int getExpiringSoon() {
        return expiringSoon;
    }

}
