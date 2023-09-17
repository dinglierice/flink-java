package com.alibaba.fliggy.orcas;

public enum Player {
    SHD("SHD"),
    YZY("YZY"),
    BYM("BYM"),
    SJF("SJF"),
    YJB("YJB"),
    DL("DL");

    private String name;

    Player(String name) {
        this.name = name;
    }

    private String getName() {
        return name;
    }

    public static Player getPlayerByName(String name) {
        for (Player p : Player.values()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}
