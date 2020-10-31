package com.jpro.hellojpro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jpro.hellojpro.HelloJProFXML.strokes;

public class Graph {

    private Map<Integer,Vertex> vertexInfo;

    Graph(Set<Stroke> subGraph){
        vertexInfo=new HashMap<>();
        for(Stroke stroke:subGraph){
            Vertex vertex=new Vertex();
            for(Stroke neighbor:stroke.neighbors){
                if(subGraph.contains(neighbor)){
                    vertex.neighbors.add(strokes.indexOf(neighbor));
                }
            }
            vertexInfo.put(strokes.indexOf(stroke),vertex);
        }
    }

    static class Vertex{
        boolean color;
        int back;
        int discoverTime;
        int parent;
        Set<Integer> cutEdge;
        Set<Integer> neighbors;
        Vertex(){
            color=false;
            cutEdge=new HashSet<>();
            neighbors=new HashSet<>();
        }
    }

    private int time;

    private void dfsBridge(int u){
        vertexInfo.get(u).color=true;
        time++;
        vertexInfo.get(u).discoverTime=time;
        vertexInfo.get(u).back=vertexInfo.get(u).discoverTime;
        for(int v:vertexInfo.get(u).neighbors){
            if(!vertexInfo.get(v).color){
                vertexInfo.get(v).parent=u;
                dfsBridge(v);
                vertexInfo.get(u).back=Math.min(vertexInfo.get(u).back,vertexInfo.get(v).back);
                if(vertexInfo.get(v).back>vertexInfo.get(u).discoverTime){
                    vertexInfo.get(u).cutEdge.add(v);
                    vertexInfo.get(v).cutEdge.add(u);
                }
            }
            else if(v!=vertexInfo.get(u).parent){
                vertexInfo.get(u).back=Math.min(vertexInfo.get(u).back,vertexInfo.get(v).discoverTime);
            }
        }
    }

    public Set<Stroke> findCircle(){
        time=0;
        for(Map.Entry<Integer, Vertex> entry : vertexInfo.entrySet()){
            if(!entry.getValue().color){
                entry.getValue().parent=-1;
                dfsBridge(entry.getKey());
            }
        }
        Set<Stroke> circle=new HashSet<>();
        for(Map.Entry<Integer, Vertex> entry:vertexInfo.entrySet()){
            for(int i:entry.getValue().neighbors){
                if(!vertexInfo.get(entry.getKey()).cutEdge.contains(i)){
                    circle.add(strokes.get(entry.getKey()));
                    circle.add(strokes.get(i));
                }
            }
        }
        return circle;
    }
}
