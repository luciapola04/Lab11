package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final long WAIT = TimeUnit.SECONDS.toMillis(10);
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final AgentC agent2 = new AgentC();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        up.addActionListener(e -> agent2.increment());
        down.addActionListener(e -> agent2.decrement());
        stop.addActionListener(e -> this.stopCounting());
        new Thread(agent2).start();
        new Thread(() -> {
            try {
                Thread.sleep(WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace(); //NOPMD
            }
            this.stopCounting();
        }).start();
    }

    private void stopCounting() {
        agent2.stopCounting();
        SwingUtilities.invokeLater(() -> {
            stop.setEnabled(false);
            up.setEnabled(false);
            down.setEnabled(false);
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class AgentC implements Runnable, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private volatile boolean stop;
        private volatile boolean increment = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try { 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter += increment ? 1 : -1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace(); //NOPMD
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to increment counting.
         */
        public void increment() {
            this.increment = true;
        }

        /**
         * External command to decrement counting.
         */
        public void decrement() {
            this.increment = false;
        }
    }
}
