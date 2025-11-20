package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, enter;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = true;
        if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = true;
        if(code == KeyEvent.VK_ENTER) enter = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = false;
        if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = false;
        if(code == KeyEvent.VK_ENTER) enter = false;
    }
}
