package com.jpro.hellojpro;

import javafx.scene.paint.Color;

public enum Element {
    NONE(-1,"none",Color.WHITE),
    LIGHT(0,"light",Color.GRAY),
    RES(1,"resistor",Color.GREEN),
    AMT(2,"ammeter",Color.RED),
    VMT(3,"voltmeter",Color.BLUE),
    DIO(4,"diode",Color.PURPLE),
    TST(5,"transistor",Color.ORANGE),
    MOT(6,"motor",Color.PINK),
    BUZ(7,"buzzer",Color.BLACK);

    private final int code;
    private final String name;
    private final Color color;

    Element(int code, String name, Color color){
        this.code=code;
        this.name=name;
        this.color=color;
    }

    public static String getName(int code) {
        for (Element k : values()) {
            if (k.code == code) {
                return k.name;
            }
        }
        return null;
    }

    public static Element getElement(int code){
        for (Element k : values()) {
            if (k.code == code) {
                return k;
            }
        }
        return null;
    }

    public static Color getColor(int code){
        for (Element k : values()) {
            if (k.code == code) {
                return k.color;
            }
        }
        return null;
    }
}
