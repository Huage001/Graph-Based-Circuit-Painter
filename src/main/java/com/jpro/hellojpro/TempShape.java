package com.jpro.hellojpro;

import javafx.scene.shape.Rectangle;

import java.util.Set;

import static com.jpro.hellojpro.HelloJProFXML.range;

public class TempShape {
    
    Set<Stroke> circleStrokes;
    Rectangle rect;
    
    public TempShape(Set<Stroke> circle, Rectangle rect){
        this.circleStrokes=circle;
        this.rect=rect;
    }

    public Set<Stroke> getCircleStrokes() {
        return circleStrokes;
    }
    
    public boolean isInCircle(Stroke stroke){
        return stroke.getMaxX()<=rect.getX()+rect.getWidth()+range&&stroke.getMinX()>=rect.getX()-range
                &&stroke.getMaxY()<=rect.getY()+range+rect.getHeight()&&stroke.getMinY()>=rect.getY()-range;
    }

    public void setRect(Rectangle rect){
        this.rect=rect;
    }

    public Rectangle getRect(){
        return rect;
    }
}
