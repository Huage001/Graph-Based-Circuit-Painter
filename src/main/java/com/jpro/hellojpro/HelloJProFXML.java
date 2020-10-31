package com.jpro.hellojpro;

import com.jpro.webapi.JProApplication;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.control.Label;

import static java.lang.Thread.sleep;

public class HelloJProFXML extends JProApplication
{

    final static int Width=1024;
    final static int Height=768;
    final static double INF=1e4;
    final static int helloMsg=9;
    final static int loc=1;
    static List<Stroke> strokes;
    static List<TempShape> circles;
    static Stroke currentStroke;
    static Canvas canvas;
    static Pane root;
    static Cell[][] board;
    static Socket socket;
    static Process process;
    static String fileNameX,fileNameY;
    static Label label;

    //args
    public static boolean debug;
    public static int range;
    public static int boardSize;

    public static void main(String[] args) {
        launch(args);
    }

    private void handleCollision(double x,double y,boolean isEndPoint){
        Cell collisionCell=board[(int)x/boardSize][(int)y/boardSize];
        if(collisionCell!=null&&collisionCell.stroke.mark==Element.NONE&&(isEndPoint||collisionCell.isEndPoint)){
            Stroke collisionStroke=collisionCell.stroke;
            collisionStroke.addNeighbor(currentStroke);
            if(currentStroke.hasSwitched&&currentStroke.containNeighbor(collisionStroke)){
                currentStroke.twiceCollision=true;
            }
            currentStroke.hasSwitched=false;
            if(debug){
                System.out.println("Collision!");
            }
            currentStroke.addNeighbor(collisionStroke);
        }else{
            currentStroke.hasSwitched=true;
        }
    }

    private void writeBoard(){
        for(int t=0;t<currentStroke.getPoints().size();t++){
            Point2D point=currentStroke.getPoints().get(t);
            boolean isEndPoint=(t==0||t==currentStroke.getPoints().size()-1);
            for(int i=Math.max(0,(int)point.getX()-range);i<Math.min(Width,(int)point.getX()+range);i++){
                for(int j=Math.max(0,(int)point.getY()-range);j<Math.min(Height,(int)point.getY()+range);j++){
                    if(board[i][j]==null||isEndPoint||!board[i][j].isEndPoint)
                        board[i][j]=new Cell(currentStroke,isEndPoint);
                }
            }
        }
    }

    private void drawLine(MouseEvent event){
        Line line=new Line(currentStroke.getLastPoint().getX(),
                currentStroke.getLastPoint().getY(),event.getX(), event.getY());
        root.getChildren().add(line);
        currentStroke.addPoint(new Point2D(event.getX(), event.getY()));
    }

    private void handleMousePressed(MouseEvent event){
        currentStroke=new Stroke(new Point2D(event.getX(),event.getY()));
        strokes.add(currentStroke);
        handleCollision(event.getX(),event.getY(),true);
    }

    private void handleMouseDragged(MouseEvent event){
        drawLine(event);
        handleCollision(event.getX(),event.getY(),false);
    }

    private void postProcess(){
        boolean selfCircle=currentStroke.handleSelfCircle();
        writeBoard();
        currentStroke.calculateRect();
        Set<Stroke> circle=null;
        int circleIndex=-1;
        for(TempShape tempShape:circles){//Step 0
            if(tempShape.isInCircle(currentStroke)){
                circle=tempShape.getCircleStrokes();
                circleIndex=circles.indexOf(tempShape);
                break;
            }
        }
        if(circleIndex!=-1){
            circle.add(currentStroke);
        }else {
            if(selfCircle) {
                circle=new HashSet<>();
                circle.add(currentStroke);
            }else {
                Set<Stroke> subGraph = currentStroke.getLocalStrokes(loc);//Step 1
                Rectangle rect = boundingBox(subGraph);//Step 2
                if (debug) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.RED);
                    //root.getChildren().add(rect);
                }
                findClosure(subGraph, rect);//Step 3
                if (debug) {
                    System.out.print("Rect:");
                    for (Stroke stroke : subGraph) {
                        System.out.print(" " + strokes.indexOf(stroke));
                    }
                    System.out.print("\n");
                }
                Graph graph = new Graph(subGraph);//Step 4
                circle = graph.findCircle();//Step 5
                if (currentStroke.twiceCollision) {
                    circle.add(currentStroke);
                    circle.addAll(currentStroke.neighbors);
                }
            }
            if (debug) {
                System.out.print("Loop:");
                for (Stroke stroke : circle) {
                    System.out.print(" " + strokes.indexOf(stroke));
                }
                System.out.print("\n");
            }
        }
        int classification=-1;
        if(!circle.isEmpty()){
            //TODO:pre-check
            classification=classifier(circle);//Step 6
            if(debug){
                System.out.println("Classifier: "+Element.getName(classification));
                label.setText(Element.getName(classification));
            }
        }
        if(classification!=-1) {
            Rectangle box=markCircle(circle,Element.getElement(classification));//Step 7
            if(circleIndex==-1) {
                circles.add(new TempShape(circle, box));
            }else{
                if(debug){
                    root.getChildren().remove(circles.get(circleIndex).getRect());
                }
                circles.get(circleIndex).setRect(box);
            }
            if(debug){
                box.setFill(Color.TRANSPARENT);
                box.setStroke(Element.getColor(classification));
                root.getChildren().add(box);
            }
        }
    }

    private Rectangle markCircle(Set<Stroke> circle,Element classification) {
        for(Stroke stroke:circle){
            stroke.mark=classification;
        }
        return boundingBox(circle);
    }

    private int classifier(Set<Stroke> circle) {
        File fileX=new File("./src/main/resources/py/result/"+fileNameX);
        File fileY=new File("./src/main/resources/py/result/"+fileNameY);
        if(!fileX.exists()){
            try {
                boolean successX=fileX.createNewFile();
                boolean successY=fileY.createNewFile();
                if(!successX||!successY){
                    return -1;
                }
            }catch(IOException e){
                e.printStackTrace();
                return -1;
            }
        }
        PrintStream psOld=System.out;
        try{
            System.setOut(new PrintStream(fileX));
        }catch(IOException e){
            e.printStackTrace();
            return -1;
        }
        for(Stroke stroke:circle){
            for(int i=0;i<stroke.getPoints().size();i++){
                System.out.print(stroke.getPoints().get(i).getX());
                if(i!=stroke.getPoints().size()-1){
                    System.out.print(",");
                }else{
                    System.out.print("\n");
                }
            }
        }
        try{
            System.setOut(new PrintStream(fileY));
        }catch(IOException e){
            e.printStackTrace();
            return -1;
        }
        for(Stroke stroke:circle){
            for(int i=0;i<stroke.getPoints().size();i++){
                System.out.print(stroke.getPoints().get(i).getY());
                if(i!=stroke.getPoints().size()-1){
                    System.out.print(",");
                }else{
                    System.out.print("\n");
                }
            }
        }
        System.setOut(psOld);
        if(sendMsg("ready")==-1){
            return -1;
        }
        int result=recvMsg();
        if(result==-1||result==helloMsg) {
            System.out.println("Connect failed!");
        }
        return result;
    }

    private int sendMsg(String msg){
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write(msg.getBytes());
            return 0;
        }catch(IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    private int recvMsg(){
        try{
            InputStream inputStream=socket.getInputStream();
            byte[] buf=new byte[16];
            int len=inputStream.read(buf,0,1);
            String msg=new String(buf,0, len);
            return Integer.parseInt(msg);
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    private void findClosure(Set<Stroke> subGraph,Rectangle rect) {
        for(Stroke stroke:strokes){
            if(stroke.mark==Element.NONE&&stroke.getMaxX()<=rect.getX()+rect.getWidth()&&stroke.getMinX()>=rect.getX()
                    &&stroke.getMaxY()<=rect.getY()+rect.getHeight()&&stroke.getMinY()>=rect.getY()){
                subGraph.add(stroke);
            }
        }
    }

    private Rectangle boundingBox(Set<Stroke> subGraph) {
        double x1=INF;
        double y1=INF;
        double x2=-INF;
        double y2=-INF;
        for(Stroke stroke:subGraph){//Step 2
            if(stroke.getMinX()<x1){
                x1=stroke.getMinX();
            }
            if(stroke.getMaxX()>x2){
                x2=stroke.getMaxX();
            }
            if(stroke.getMinY()<y1){
                y1=stroke.getMinY();
            }
            if(stroke.getMaxY()>y2){
                y2=stroke.getMaxY();
            }
        }
        x1=Math.max(0,x1-range);
        x2=Math.min(Width,x2+range);
        y1=Math.max(0,y1-range);
        y2=Math.min(Height,y2+range);
        return new Rectangle(x1, y1, x2-x1, y2-y1);
    }

    private void handleMouseReleased(MouseEvent event){
        drawLine(event);
        handleCollision(event.getX(),event.getY(),true);
        currentStroke.setEnd(new Point2D(event.getX(),event.getY()));
        postProcess();
    }

    @Override
    public void start(Stage stage)
    {
        range = 10;
        boardSize = 1;
        debug = true;
        board = new Cell[Width / boardSize][Height / boardSize];

        try {
            sleep(1200);
            socket = new Socket("127.0.0.1",2335);
            fileNameX=System.currentTimeMillis()+"X.txt";
            fileNameY=System.currentTimeMillis()+"Y.txt";
            sendMsg(fileNameX+","+fileNameY);
            if(recvMsg()!=helloMsg){
                System.out.println("Connect failed!");
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Start failed!");
            return;
        }

        root = new Pane();
        canvas=new Canvas();
        strokes=new ArrayList<>();
        circles=new ArrayList<>();

        stage.addEventHandler(MouseEvent.MOUSE_PRESSED,this::handleMousePressed);
        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED,this::handleMouseDragged);
        stage.addEventHandler(MouseEvent.MOUSE_RELEASED,this::handleMouseReleased);
        stage.setOnCloseRequest(windowEvent -> {
            if(process!=null){
                process.destroy();
            }
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        label = new Label("Hello!");
        label.setFont(new Font(50));
        label.setAlignment(Pos.TOP_CENTER);

        root.getChildren().add(canvas);
        root.getChildren().add(label);
        Scene scene=new Scene(root,Width,Height);
        stage.setScene(scene);
        stage.setTitle("Circuit Graph Painter");
        stage.show();
    }
}