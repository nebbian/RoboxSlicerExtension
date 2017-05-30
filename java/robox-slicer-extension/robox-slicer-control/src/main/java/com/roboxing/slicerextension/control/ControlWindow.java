/*
 * This file is part of Robox Slicer Extension.
 *
 * Robox Slicer Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robox Slicer Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robox Slicer Extension.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.roboxing.slicerextension.control;

import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;
import static javax.swing.SpringLayout.WEST;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Controller window
 *
 * @author Daniel Sendula
 */
public class ControlWindow extends JFrame {

    private static final Slicer[] SLICERS = {
            new Slicer("Slic3r", true),
            new Slicer("Simplify 3D", false),
            new Slicer("Cura 2.5", false),
            new Slicer("Cura 3.0", false)
    };

    private JPanel buttonsPanel;
    private JButton saveButton;
    private JButton cancelButton;

    private JPanel informationPanel;

    private JPanel mainPanel;

    private JComboBox<Slicer> slicersDropDown;

    private JTextArea logArea;

    public ControlWindow() {
        super("Robox Slicer Extension");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        setSize(640, 400);

        createInformationPanel();
        createButtonsPanel();
        createMainPanel();
    }

    public void setLeaveAction(Runnable action) {
        cancelButton.addActionListener(e -> action.run());
        addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) { }
            @Override public void windowIconified(WindowEvent e) { }
            @Override public void windowDeiconified(WindowEvent e) { }
            @Override public void windowDeactivated(WindowEvent e) { }
            @Override public void windowClosed(WindowEvent e) { }
            @Override public void windowActivated(WindowEvent e) { }

            @Override public void windowClosing(WindowEvent e) {
                action.run();
            }
        });
    }

    private void createInformationPanel() {
        informationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel label = new JLabel("Robox Slicer Extension");
        label.setFont(label.getFont().deriveFont(label.getFont().getSize() * 2.0f));

        informationPanel.add(label);

        this.add(informationPanel, BorderLayout.NORTH);
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        SpringLayout layout = new SpringLayout();

        mainPanel.setLayout(layout);
        JLabel amPathLabel = new JLabel("Select Slicer:");

        slicersDropDown = new JComboBox<Slicer>(SLICERS) {
            @Override
            public void setSelectedItem(Object item) {
                if (item.toString().startsWith("-")) {
                    return;
                }
                super.setSelectedItem(item);
            };
        };
        slicersDropDown.setSelectedIndex(0);
        slicersDropDown.setRenderer(new CustomListCellRenderer());

        logArea = new JTextArea("");

        JScrollPane logAreaScroller = new JScrollPane();
        logAreaScroller.setViewportView(logArea);
        logAreaScroller.setBorder(BorderFactory.createTitledBorder("Output"));

        mainPanel.add(amPathLabel);
        mainPanel.add(slicersDropDown);
        mainPanel.add(logAreaScroller);

        layout.putConstraint(NORTH, amPathLabel, 5, NORTH, mainPanel);
        layout.putConstraint(WEST, amPathLabel, 5, WEST, mainPanel);

        layout.putConstraint(WEST, slicersDropDown, 5, EAST, amPathLabel);


        layout.putConstraint(NORTH, logAreaScroller, 15, SOUTH, amPathLabel);
        layout.putConstraint(WEST, logAreaScroller, 5, WEST, mainPanel);
        layout.putConstraint(EAST, logAreaScroller, -5, EAST, mainPanel);
        layout.putConstraint(SOUTH, logAreaScroller, -5, SOUTH, mainPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    public void log(String s) {
        Document document = logArea.getDocument();
        try {
            document.insertString(document.getLength(), s + "\n", null);
        } catch (BadLocationException ignore) { }
    }

    private class CustomListCellRenderer extends JLabel implements ListCellRenderer<Slicer> {
        public Component getListCellRendererComponent(
                JList<? extends Slicer> list,
                Slicer value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (!value.isEnabled()) {
                setBackground(UIManager.getColor("ComboBox.disabledBackground"));
                setForeground(UIManager.getColor("ComboBox.disabledForeground"));
            } else if (isSelected) {
                setBackground(UIManager.getColor("ComboBox.selectionBackground"));
                setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("ComboBox.background"));
                setForeground(UIManager.getColor("ComboBox.foreground"));
            }

            setText(value.getLabel());

            return this;
        }
    }
}
