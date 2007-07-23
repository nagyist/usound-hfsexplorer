package org.catacombae.hfsexplorer;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class DebugConsoleWindow extends JFrame {
    // too advanced for now ;>
//     private static final int BUFFER_NUMBER_OF_COLUMNS = 80;
//     private static final int BUFFER_NUMBER_OF_LINES = 500;
    private static final int WINDOW_NUMBER_OF_COLUMNS = 80;
    private static final int WINDOW_NUMBER_OF_LINES = 25;
    
//     private ArrayList<char[]> buffer = new ArrayList<char[]>
    private JScrollPane debugAreaScroller;
    private JTextArea debugArea;
    
    public final DebugStream debugStream = new DebugStream();
    
    public class DebugStream extends OutputStream {
	LinkedList<Byte> currentLine = new LinkedList<Byte>();
	private DebugStream() {}
	public synchronized void write(byte[] b) throws IOException {
	    write(b, 0, b.length);
	}
	public synchronized void write(byte[] b, int off, int len) throws IOException {
// 	    char[] c = new char[len];
// 	    for(int i = off; i < len; ++i) c[i-off] = b[i];
	    debugArea.append(new String(b, off, len));
// 	    for(int i = off, i < off+len; ++i) {
		
// 		if(b[i] == 0x10) {
// 		    byte[] lineData;
// 		    debugArea.replace
// 		}
// 		else if(b[i] != 0x13)
// 		    currentLine.addLast(
		
	}
	public synchronized void write(int b) throws IOException {
	    write(new byte[] { (byte)b }, 0, 1);
	}
    }
    
    public DebugConsoleWindow() {
	super("Debug Console");
	setLayout(new BorderLayout());
	debugArea = new JTextArea(WINDOW_NUMBER_OF_LINES, WINDOW_NUMBER_OF_COLUMNS);
	debugAreaScroller = new JScrollPane(debugArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	debugArea.setLineWrap(true);
	debugArea.setEditable(false);
	
	add(debugAreaScroller, BorderLayout.CENTER);
	
	pack();
	setLocationRelativeTo(null);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}