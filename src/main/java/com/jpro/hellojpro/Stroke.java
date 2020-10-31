package com.jpro.hellojpro;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.*;

import static com.jpro.hellojpro.HelloJProFXML.*;

public class Stroke {

    static boolean isJoint(Point2D a, Point2D b){
        return a.distance(b)<range;
    }

    List<Point2D> points;
    Point2D start;
    Point2D end;
    Set<Stroke> neighbors;
    Element mark;
    double maxX;
    double minX;
    double maxY;
    double minY;
    boolean twiceCollision;
    boolean hasSwitched;

    public double getMaxX() {
        return maxX;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinY() {
        return minY;
    }

    public Stroke(Point2D start){
        points=new ArrayList<>();
        neighbors=new HashSet<>();
        points.add(start);
        this.start=start;
        maxX=-INF;
        minX=INF;
        maxY=-INF;
        minY=INF;
        mark=Element.NONE;
        twiceCollision=false;
        hasSwitched=false;
    }

    public void addPoint(Point2D point){
        points.add(point);
    }

    public Point2D getLastPoint(){
        return points.get(points.size()-1);
    }

    public void setEnd(Point2D point){
        end=point;
    }

    public void addNeighbor(Stroke stroke){
        neighbors.add(stroke);
    }

    public boolean containNeighbor(Stroke stroke){
        return neighbors.contains(stroke);
    }

    public List<Point2D> getPoints(){
        return points;
    }

    public boolean handleSelfCircle(){
        boolean offStart=false;
        for(Point2D point:points){
            if(!offStart){
                if(!isJoint(start,point)){
                    offStart = true;
                }
            }else{
                if(isJoint(start,point)){
                    addNeighbor(this);
                    if(debug) {
                        System.out.println("Self Circle!");
                    }
                    return true;
                }
            }
        }
        boolean offEnd=false;
        for(int i=points.size()-1;i>=0;i--){
            if(!offEnd){
                if(!isJoint(end,points.get(i))) {
                    offEnd = true;
                }
            }else{
                if(isJoint(end,points.get(i))){
                    addNeighbor(this);
                    if(debug) {
                        System.out.println("Self Circle!");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Stroke> getLocalStrokes(int loc){
        long startTime=System.nanoTime();
        Set<Stroke> localStrokes=new HashSet<>(neighbors);
        Set<Stroke> processedStrokes=new HashSet<>();
        Queue<Pair<Stroke,Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(this, 0));
        processedStrokes.add(this);
        while(!queue.isEmpty()){
            int level=queue.element().getValue();
            Stroke head=Objects.requireNonNull(queue.poll()).getKey();
            localStrokes.add(head);
            if(level<loc){
                for(Stroke neighbor:head.neighbors){
                    if(!processedStrokes.contains(neighbor)) {
                        queue.add(new Pair<>(neighbor, level + 1));
                        processedStrokes.add(neighbor);
                    }
                }
            }
        }
        System.out.println(System.nanoTime()-startTime);
        return localStrokes;
    }

    public void calculateRect(){
        for(Point2D point:points){
            if(point.getX()<minX){
                minX=point.getX();
            }
            if(point.getX()>maxX){
                maxX=point.getX();
            }
            if(point.getY()<minY){
                minY=point.getY();
            }
            if(point.getY()>maxY){
                maxY=point.getY();
            }
        }
    }

}