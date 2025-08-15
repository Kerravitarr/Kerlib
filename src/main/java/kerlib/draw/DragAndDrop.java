/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zeus
 */
public class DragAndDrop implements MouseListener, MouseMotionListener{
    public interface AllMoveListener {
        public void accept(int allDx, int allDy);
    }
    public interface StepMoveListener {
        public void accept(int dx, int dy);
    }
    
    ///Компонент, за перемещением на ком мы следим
    private final java.awt.Component perrent;
    ///Точка начала перемещения
    private Point startDrag;
    ///Точка, которую мы нашли в прошлый раз
    private Point previosStep;
    ///Слушатели события перетаскивания
    private List<AllMoveListener> listeners_all = new ArrayList();
    ///Слушатели события перетаскивания
    private List<StepMoveListener> listeners_step = new ArrayList();
    ///Флаг, показывающий, что началось перетаскивание
    private boolean isDrag = false;

    public DragAndDrop(Component perrent) {
        this.perrent = perrent;
        
        this.perrent.addMouseListener(this);
        this.perrent.addMouseMotionListener(this);
    }
    ///@return true, если объект перетаскивается в данный момент
    public boolean isDrag(){return isDrag;}
    public void addAllListener(AllMoveListener l){listeners_all.add(l);}
    public void addStepListener(StepMoveListener l){listeners_step.add(l);}

    @Override public void mouseClicked(MouseEvent e) {}

    @Override public void mousePressed(MouseEvent e) {
        previosStep = startDrag = e.getLocationOnScreen();
    }

    @Override public void mouseReleased(MouseEvent e) {isDrag = false;}

    @Override public void mouseEntered(MouseEvent e) {}

    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        isDrag = true;
        var abs = e.getLocationOnScreen();
        var alldx = abs.x - startDrag.x;
        var alldy = abs.y - startDrag.y;
        var dx = abs.x - previosStep.x;
        var dy = abs.y - previosStep.y;
        listeners_all.forEach(l -> l.accept(alldx, alldy));
        listeners_step.forEach(l -> l.accept(dx, dy));
        previosStep = abs;
    }

    @Override public void mouseMoved(MouseEvent e) {}
}
